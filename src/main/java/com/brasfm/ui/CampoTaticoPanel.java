package com.brasfm.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import com.brasfm.model.*;
import com.brasfm.model.enums.Position;

/**
 * Painel visual do campo tático com posições dos jogadores.
 * Permite visualizar a formação e arrastar jogadores.
 */
public class CampoTaticoPanel extends JPanel {

    // Cores
    private static final Color GRAMADO = new Color(34, 139, 34);
    private static final Color GRAMADO_LISTRAS = new Color(40, 160, 40);
    private static final Color LINHA_BRANCA = new Color(255, 255, 255, 200);
    private static final Color JOGADOR_BG = new Color(30, 30, 30, 220);
    private static final Color JOGADOR_TITULAR = new Color(46, 204, 113);
    private static final Color JOGADOR_RESERVA = new Color(241, 196, 15);
    private static final Color JOGADOR_SELECIONADO = new Color(52, 152, 219);
    private static final Color GOLEIRO_COR = new Color(155, 89, 182);

    private Team time;
    private List<JogadorVisual> jogadoresVisuais;
    private JogadorVisual jogadorSelecionado;
    private JogadorVisual jogadorArrastando;
    private Point pontoArraste;

    // Posições no campo para cada formação
    private Map<Position, Point2D.Double> posicoesFormacao;

    public CampoTaticoPanel(Team time) {
        this.time = time;
        this.jogadoresVisuais = new ArrayList<>();
        this.posicoesFormacao = new HashMap<>();

        setPreferredSize(new Dimension(400, 550));
        setBackground(GRAMADO);

        calcularPosicoesFormacao();
        criarJogadoresVisuais();

        // Mouse listeners para drag & drop
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (JogadorVisual jv : jogadoresVisuais) {
                    if (jv.contem(e.getPoint())) {
                        jogadorSelecionado = jv;
                        jogadorArrastando = jv;
                        pontoArraste = e.getPoint();
                        repaint();
                        break;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (jogadorArrastando != null) {
                    // Verifica se soltou sobre outro jogador para trocar
                    for (JogadorVisual jv : jogadoresVisuais) {
                        if (jv != jogadorArrastando && jv.contem(e.getPoint())) {
                            trocarJogadores(jogadorArrastando, jv);
                            break;
                        }
                    }
                    jogadorArrastando = null;
                    pontoArraste = null;
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (jogadorArrastando != null) {
                    pontoArraste = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // Duplo clique alterna titular/reserva
                    for (JogadorVisual jv : jogadoresVisuais) {
                        if (jv.contem(e.getPoint())) {
                            alternarTitularReserva(jv.jogador);
                            criarJogadoresVisuais();
                            repaint();
                            break;
                        }
                    }
                }
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    private void calcularPosicoesFormacao() {
        // Posições relativas (0.0 a 1.0) no campo
        // Campo é vertical: gol embaixo, ataque em cima

        posicoesFormacao.clear();

        // Goleiro
        posicoesFormacao.put(Position.GOLEIRO, new Point2D.Double(0.5, 0.92));

        // Zagueiros
        posicoesFormacao.put(Position.ZAGUEIRO, new Point2D.Double(0.35, 0.78));

        // Laterais
        posicoesFormacao.put(Position.LATERAL_DIREITO, new Point2D.Double(0.85, 0.72));
        posicoesFormacao.put(Position.LATERAL_ESQUERDO, new Point2D.Double(0.15, 0.72));

        // Volantes
        posicoesFormacao.put(Position.VOLANTE, new Point2D.Double(0.5, 0.58));

        // Meias
        posicoesFormacao.put(Position.MEIA, new Point2D.Double(0.35, 0.45));
        posicoesFormacao.put(Position.MEIA_ATACANTE, new Point2D.Double(0.5, 0.35));

        // Pontas
        posicoesFormacao.put(Position.PONTA_DIREITA, new Point2D.Double(0.82, 0.32));
        posicoesFormacao.put(Position.PONTA_ESQUERDA, new Point2D.Double(0.18, 0.32));

        // Atacantes
        posicoesFormacao.put(Position.CENTROAVANTE, new Point2D.Double(0.5, 0.18));
        posicoesFormacao.put(Position.ATACANTE, new Point2D.Double(0.65, 0.25));
    }

    private void criarJogadoresVisuais() {
        jogadoresVisuais.clear();

        int w = getWidth() > 0 ? getWidth() : 400;
        int h = getHeight() > 0 ? getHeight() : 550;

        // Mapa para contar jogadores por posição
        Map<Position, Integer> contadorPosicao = new HashMap<>();

        // Primeiro os titulares
        for (Player p : time.getTitulares()) {
            Position pos = p.getPosicao();
            if (pos == null)
                pos = p.getPosicaoOriginal();
            if (pos == null)
                continue;

            int offset = contadorPosicao.getOrDefault(pos, 0);
            contadorPosicao.put(pos, offset + 1);

            Point2D.Double posRel = posicoesFormacao.get(pos);
            if (posRel == null) {
                posRel = new Point2D.Double(0.5, 0.5);
            }

            // Ajusta offset para múltiplos jogadores na mesma posição
            double offsetX = (offset % 2) * 0.12 - 0.06;
            double offsetY = (offset / 2) * 0.08;

            int x = (int) ((posRel.x + offsetX) * w);
            int y = (int) ((posRel.y + offsetY) * h);

            jogadoresVisuais.add(new JogadorVisual(p, x, y, true));
        }

        // Reservas na lateral
        int reservaY = 50;
        for (Player p : time.getReservas()) {
            jogadoresVisuais.add(new JogadorVisual(p, w - 45, reservaY, false));
            reservaY += 45;
            if (reservaY > h - 50)
                break;
        }
    }

    private void trocarJogadores(JogadorVisual jv1, JogadorVisual jv2) {
        Player p1 = jv1.jogador;
        Player p2 = jv2.jogador;

        boolean p1Titular = time.getTitulares().contains(p1);
        boolean p2Titular = time.getTitulares().contains(p2);

        if (p1Titular && p2Titular) {
            // Troca posições
            Position temp = p1.getPosicao();
            p1.setPosicao(p2.getPosicao());
            p2.setPosicao(temp);
        } else if (p1Titular && !p2Titular) {
            // P1 vai pro banco, P2 entra
            time.getTitulares().remove(p1);
            time.getReservas().remove(p2);
            time.getTitulares().add(p2);
            time.getReservas().add(p1);
            p2.setPosicao(p1.getPosicao());
        } else if (!p1Titular && p2Titular) {
            // P2 vai pro banco, P1 entra
            time.getTitulares().remove(p2);
            time.getReservas().remove(p1);
            time.getTitulares().add(p1);
            time.getReservas().add(p2);
            p1.setPosicao(p2.getPosicao());
        }

        criarJogadoresVisuais();
    }

    private void alternarTitularReserva(Player p) {
        if (time.getTitulares().contains(p)) {
            if (time.getTitulares().size() > 1) {
                time.getTitulares().remove(p);
                time.getReservas().add(0, p);
            }
        } else if (time.getReservas().contains(p)) {
            if (time.getTitulares().size() < 11) {
                time.getReservas().remove(p);
                time.getTitulares().add(p);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Desenha gramado com listras
        g2.setColor(GRAMADO);
        g2.fillRect(0, 0, w, h);

        g2.setColor(GRAMADO_LISTRAS);
        for (int i = 0; i < h; i += 40) {
            if ((i / 40) % 2 == 0) {
                g2.fillRect(0, i, w, 20);
            }
        }

        // Borda do campo
        g2.setColor(LINHA_BRANCA);
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(20, 20, w - 40, h - 40);

        // Linha do meio
        g2.drawLine(20, h / 2, w - 20, h / 2);

        // Círculo central
        int circuloR = 50;
        g2.drawOval(w / 2 - circuloR, h / 2 - circuloR, circuloR * 2, circuloR * 2);
        g2.fillOval(w / 2 - 5, h / 2 - 5, 10, 10);

        // Área grande (embaixo - nosso gol)
        int areaW = 180;
        int areaH = 70;
        g2.drawRect(w / 2 - areaW / 2, h - 20 - areaH, areaW, areaH);

        // Área pequena
        int areaPequenaW = 90;
        int areaPequenaH = 30;
        g2.drawRect(w / 2 - areaPequenaW / 2, h - 20 - areaPequenaH, areaPequenaW, areaPequenaH);

        // Área grande (em cima - gol adversário)
        g2.drawRect(w / 2 - areaW / 2, 20, areaW, areaH);
        g2.drawRect(w / 2 - areaPequenaW / 2, 20, areaPequenaW, areaPequenaH);

        // Meia-lua da área
        g2.drawArc(w / 2 - 40, h - 20 - areaH - 25, 80, 50, 0, 180);
        g2.drawArc(w / 2 - 40, 20 + areaH - 25, 80, 50, 180, 180);

        // Desenha jogadores
        for (JogadorVisual jv : jogadoresVisuais) {
            if (jv != jogadorArrastando) {
                desenharJogador(g2, jv);
            }
        }

        // Desenha jogador sendo arrastado por último
        if (jogadorArrastando != null && pontoArraste != null) {
            JogadorVisual temp = new JogadorVisual(
                    jogadorArrastando.jogador,
                    pontoArraste.x,
                    pontoArraste.y,
                    jogadorArrastando.titular);
            desenharJogador(g2, temp);
        }

        // Legenda
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(Color.WHITE);
        g2.drawString("⬅ Arrastar para trocar | Duplo clique: Titular ↔ Reserva", 25, h - 5);
    }

    private void desenharJogador(Graphics2D g2, JogadorVisual jv) {
        int x = jv.x;
        int y = jv.y;
        int raio = 20;

        // Sombra
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillOval(x - raio + 3, y - raio + 3, raio * 2, raio * 2);

        // Círculo do jogador
        Color corJogador;
        if (jv == jogadorSelecionado) {
            corJogador = JOGADOR_SELECIONADO;
        } else if (jv.jogador.isGoleiro()) {
            corJogador = GOLEIRO_COR;
        } else if (jv.titular) {
            corJogador = JOGADOR_TITULAR;
        } else {
            corJogador = JOGADOR_RESERVA;
        }

        g2.setColor(corJogador);
        g2.fillOval(x - raio, y - raio, raio * 2, raio * 2);

        // Borda
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x - raio, y - raio, raio * 2, raio * 2);

        // Número da camisa (baseado na posição na lista)
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        String numero = jv.jogador.getPosicao() != null ? String.valueOf(jv.jogador.getPosicao().ordinal() + 1) : "?";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(numero, x - fm.stringWidth(numero) / 2, y + 5);

        // Nome abaixo
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g2.setColor(JOGADOR_BG);
        String nome = jv.jogador.getNome().length() > 10 ? jv.jogador.getNome().substring(0, 10) : jv.jogador.getNome();
        fm = g2.getFontMetrics();
        int nomeW = fm.stringWidth(nome);
        g2.fillRoundRect(x - nomeW / 2 - 3, y + raio + 2, nomeW + 6, 14, 4, 4);
        g2.setColor(Color.WHITE);
        g2.drawString(nome, x - nomeW / 2, y + raio + 13);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        criarJogadoresVisuais();
    }

    /**
     * Classe interna para representar visualmente um jogador no campo.
     */
    private class JogadorVisual {
        Player jogador;
        int x, y;
        boolean titular;

        JogadorVisual(Player jogador, int x, int y, boolean titular) {
            this.jogador = jogador;
            this.x = x;
            this.y = y;
            this.titular = titular;
        }

        boolean contem(Point p) {
            return p.distance(x, y) <= 25;
        }
    }
}
