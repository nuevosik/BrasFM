package com.brasfm.ui.panels;

import javax.swing.*;
import java.awt.*;

/**
 * Painel de estatísticas em tempo real durante partidas.
 * Mostra posse de bola, chutes e outras stats.
 */
public class StatsPanel extends JPanel {

    private static final Color DARK_BG = new Color(30, 30, 30);
    private static final Color BAR_BG = new Color(60, 60, 60);
    private static final Color CASA_COR = new Color(46, 204, 113); // Verde
    private static final Color FORA_COR = new Color(231, 76, 60); // Vermelho
    private static final Color TEXT_WHITE = new Color(236, 240, 241);
    private static final Color TEXT_GRAY = new Color(149, 165, 166);

    private JProgressBar barraPosse;
    private JProgressBar barraChutes;
    private JLabel lblPosseCasa, lblPosseFora;
    private JLabel lblChutesCasa, lblChutesFora;

    private int posseCasa = 50;
    private int posseFora = 50;
    private int chutesCasa = 0;
    private int chutesFora = 0;

    private String siglaCasa = "CAS";
    private String siglaFora = "FOR";

    public StatsPanel() {
        setLayout(new GridLayout(2, 1, 5, 5));
        setBackground(DARK_BG);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        initComponents();
    }

    private void initComponents() {
        // Painel de posse
        JPanel painelPosse = criarPainelStat("⚽ Posse");
        barraPosse = criarBarraStat();
        barraPosse.setValue(50);

        lblPosseCasa = new JLabel("50%");
        lblPosseCasa.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPosseCasa.setForeground(CASA_COR);

        lblPosseFora = new JLabel("50%");
        lblPosseFora.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPosseFora.setForeground(FORA_COR);

        JPanel posseLinha = new JPanel(new BorderLayout(5, 0));
        posseLinha.setOpaque(false);
        posseLinha.add(lblPosseCasa, BorderLayout.WEST);
        posseLinha.add(barraPosse, BorderLayout.CENTER);
        posseLinha.add(lblPosseFora, BorderLayout.EAST);

        painelPosse.add(posseLinha, BorderLayout.CENTER);
        add(painelPosse);

        // Painel de chutes
        JPanel painelChutes = criarPainelStat("🎯 Chutes");
        barraChutes = criarBarraStat();
        barraChutes.setValue(50);

        lblChutesCasa = new JLabel("0");
        lblChutesCasa.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblChutesCasa.setForeground(CASA_COR);

        lblChutesFora = new JLabel("0");
        lblChutesFora.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblChutesFora.setForeground(FORA_COR);

        JPanel chutesLinha = new JPanel(new BorderLayout(5, 0));
        chutesLinha.setOpaque(false);
        chutesLinha.add(lblChutesCasa, BorderLayout.WEST);
        chutesLinha.add(barraChutes, BorderLayout.CENTER);
        chutesLinha.add(lblChutesFora, BorderLayout.EAST);

        painelChutes.add(chutesLinha, BorderLayout.CENTER);
        add(painelChutes);
    }

    private JPanel criarPainelStat(String titulo) {
        JPanel painel = new JPanel(new BorderLayout(5, 2));
        painel.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblTitulo.setForeground(TEXT_GRAY);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        painel.add(lblTitulo, BorderLayout.NORTH);

        return painel;
    }

    private JProgressBar criarBarraStat() {
        JProgressBar barra = new JProgressBar(0, 100);
        barra.setValue(50);
        barra.setStringPainted(false);
        barra.setBackground(FORA_COR);
        barra.setForeground(CASA_COR);
        barra.setBorder(null);
        barra.setPreferredSize(new Dimension(150, 12));
        return barra;
    }

    /**
     * Atualiza a posse de bola.
     */
    public void setPosse(int casa, int fora) {
        this.posseCasa = casa;
        this.posseFora = fora;
        barraPosse.setValue(casa);
        lblPosseCasa.setText(casa + "%");
        lblPosseFora.setText(fora + "%");
    }

    /**
     * Atualiza os chutes.
     */
    public void setChutes(int casa, int fora) {
        this.chutesCasa = casa;
        this.chutesFora = fora;
        int total = casa + fora;
        if (total > 0) {
            barraChutes.setValue((casa * 100) / total);
        } else {
            barraChutes.setValue(50);
        }
        lblChutesCasa.setText(String.valueOf(casa));
        lblChutesFora.setText(String.valueOf(fora));
    }

    /**
     * Adiciona um chute para o time.
     */
    public void addChute(boolean casaChutou) {
        if (casaChutou) {
            chutesCasa++;
        } else {
            chutesFora++;
        }
        setChutes(chutesCasa, chutesFora);
    }

    /**
     * Atualiza posse baseado em eventos.
     */
    public void atualizarPossePorEvento(boolean casaPossuiBola) {
        if (casaPossuiBola) {
            posseCasa = Math.min(80, posseCasa + 2);
        } else {
            posseFora = Math.min(80, posseFora + 2);
        }
        // Normaliza para 100%
        int total = posseCasa + posseFora;
        posseCasa = (posseCasa * 100) / total;
        posseFora = 100 - posseCasa;
        setPosse(posseCasa, posseFora);
    }

    /**
     * Define as siglas dos times.
     */
    public void setTimes(String siglaCasa, String siglaFora) {
        this.siglaCasa = siglaCasa;
        this.siglaFora = siglaFora;
    }

    /**
     * Reseta as estatísticas.
     */
    public void reset() {
        posseCasa = 50;
        posseFora = 50;
        chutesCasa = 0;
        chutesFora = 0;
        setPosse(50, 50);
        setChutes(0, 0);
    }
}
