package com.brasfm.ui;

import com.brasfm.model.*;
import com.brasfm.model.enums.*;
import com.brasfm.engine.*;
import com.brasfm.championship.*;
import com.brasfm.audio.SoundSystem;
import com.brasfm.persistence.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Janela principal do BrasFM.
 */
public class MainWindow extends JFrame {
    // Cores do tema
    private static final Color COR_FUNDO = new Color(18, 18, 18);
    private static final Color COR_PAINEL = new Color(30, 30, 30);
    private static final Color COR_DESTAQUE = new Color(0, 150, 136);
    private static final Color COR_TEXTO = new Color(240, 240, 240);
    private static final Color COR_TEXTO_SEC = new Color(160, 160, 160);
    private static final Color COR_VERMELHO = new Color(220, 53, 69);
    private static final Color COR_VERDE = new Color(40, 167, 69);
    private static final Color COR_AMARELO = new Color(255, 193, 7);

    // Componentes
    private JPanel painelPrincipal;
    private JPanel painelMenu;
    private JPanel painelConteudo;

    // Dados do jogo
    private Team timeSelecionado;
    private List<Team> todosOsTimes;
    private League campeonato;
    private MatchEngine matchEngine;
    private MatchEngine advancedEngine;
    private TeamGenerator teamGenerator;
    private SoundSystem soundSystem;
    private GameSaveManager saveManager;

    public MainWindow() {
        super("BrasFM - O jogo para quem entende de futebol");

        // Inicializa sistema de som
        soundSystem = new SoundSystem();
        soundSystem.carregarSons();

        // Inicializa componentes do jogo
        matchEngine = new MatchEngine();
        advancedEngine = new MatchEngine();
        advancedEngine.setSoundSystem(soundSystem);

        teamGenerator = new TeamGenerator();
        todosOsTimes = teamGenerator.gerarTimesBrasileiros();
        saveManager = new GameSaveManager();

        // Configura janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1024, 600));

        // Define look and feel escuro
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Cria interface
        criarInterface();

        // Mostra tela inicial
        mostrarTelaInicial();
    }

    private void criarInterface() {
        painelPrincipal = new JPanel(new BorderLayout());
        painelPrincipal.setBackground(COR_FUNDO);

        // Menu lateral
        painelMenu = criarMenuLateral();
        painelPrincipal.add(painelMenu, BorderLayout.WEST);

        // Área de conteúdo
        painelConteudo = new JPanel(new BorderLayout());
        painelConteudo.setBackground(COR_FUNDO);
        painelConteudo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        painelPrincipal.add(painelConteudo, BorderLayout.CENTER);

        setContentPane(painelPrincipal);
    }

    private JPanel criarMenuLateral() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(COR_PAINEL);
        menu.setPreferredSize(new Dimension(200, 0));
        menu.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Logo/Título
        JLabel logo = new JLabel("⚽ BrasFM");
        logo.setForeground(COR_DESTAQUE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        menu.add(logo);

        menu.add(Box.createVerticalStrut(30));

        // Botões do menu
        String[][] botoesMenu = {
                { "🏠", "Início" },
                { "👥", "Meu Time" },
                { "📋", "Escalação" },
                { "🏆", "Campeonato" },
                { "📊", "Tabela" },
                { "⚽", "Jogar" },
                { "💰", "Finanças" },
                { "💾", "Salvar" },
                { "📂", "Carregar" }
        };

        for (String[] btn : botoesMenu) {
            JButton botao = criarBotaoMenu(btn[0] + "  " + btn[1]);

            final String acao = btn[1];
            botao.addActionListener(e -> {
                switch (acao) {
                    case "Início":
                        mostrarTelaInicial();
                        break;
                    case "Meu Time":
                        mostrarMeuTime();
                        break;
                    case "Escalação":
                        mostrarEscalacao();
                        break;
                    case "Campeonato":
                        mostrarCampeonato();
                        break;
                    case "Tabela":
                        mostrarTabela();
                        break;
                    case "Jogar":
                        jogarProximaPartida();
                        break;
                    case "Finanças":
                        mostrarFinancas();
                        break;
                    case "Salvar":
                        salvarJogo();
                        break;
                    case "Carregar":
                        carregarJogo();
                        break;
                }
            });

            menu.add(botao);
            menu.add(Box.createVerticalStrut(5));
        }

        menu.add(Box.createVerticalGlue());

        // Versão
        JLabel versao = new JLabel("v1.0.0");
        versao.setForeground(COR_TEXTO_SEC);
        versao.setAlignmentX(Component.CENTER_ALIGNMENT);
        menu.add(versao);

        return menu;
    }

    private JButton criarBotaoMenu(String texto) {
        JButton botao = new JButton(texto);
        botao.setMaximumSize(new Dimension(180, 40));
        botao.setPreferredSize(new Dimension(180, 40));
        botao.setBackground(COR_PAINEL);
        botao.setForeground(COR_TEXTO);
        botao.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        botao.setBorderPainted(false);
        botao.setFocusPainted(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setHorizontalAlignment(SwingConstants.LEFT);

        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(COR_DESTAQUE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(COR_PAINEL);
            }
        });

        return botao;
    }

    private void mostrarTelaInicial() {
        painelConteudo.removeAll();

        JPanel tela = new JPanel();
        tela.setLayout(new BoxLayout(tela, BoxLayout.Y_AXIS));
        tela.setBackground(COR_FUNDO);

        // Título
        JLabel titulo = new JLabel("Bem-vindo ao BrasFM!");
        titulo.setForeground(COR_TEXTO);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        tela.add(Box.createVerticalStrut(50));
        tela.add(titulo);

        JLabel subtitulo = new JLabel("O jogo para quem entende de futebol");
        subtitulo.setForeground(COR_TEXTO_SEC);
        subtitulo.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        tela.add(Box.createVerticalStrut(10));
        tela.add(subtitulo);

        tela.add(Box.createVerticalStrut(50));

        if (timeSelecionado == null) {
            JLabel instrucao = new JLabel("Escolha um time para começar:");
            instrucao.setForeground(COR_TEXTO);
            instrucao.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            instrucao.setAlignmentX(Component.CENTER_ALIGNMENT);
            tela.add(instrucao);

            tela.add(Box.createVerticalStrut(20));

            // Grid de times
            JPanel gridTimes = new JPanel(new GridLayout(4, 5, 15, 15));
            gridTimes.setBackground(COR_FUNDO);
            gridTimes.setMaximumSize(new Dimension(900, 400));

            for (Team time : todosOsTimes) {
                JButton btnTime = new JButton();
                btnTime.setLayout(new BorderLayout(5, 5));
                btnTime.setPreferredSize(new Dimension(150, 100));
                btnTime.setBackground(COR_PAINEL);
                btnTime.setForeground(COR_TEXTO);
                btnTime.setBorderPainted(false);
                btnTime.setFocusPainted(false);
                btnTime.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                // Carregar logo do time
                if (time.getEscudoPath() != null && !time.getEscudoPath().isEmpty()) {
                    try {
                        String logoPath = time.getEscudoPath();
                        java.io.File logoFile = new java.io.File(logoPath);
                        if (logoFile.exists()) {
                            ImageIcon originalIcon = new ImageIcon(logoPath);
                            Image scaledImage = originalIcon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                            JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
                            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            btnTime.add(logoLabel, BorderLayout.CENTER);
                        }
                    } catch (Exception ex) {
                        // Se não conseguir carregar o logo, continua sem ele
                    }
                }

                // Nome e sigla do time
                JLabel nomeLabel = new JLabel("<html><center><b>" + time.getSigla() + "</b><br>" +
                        "<font size='2'>" + time.getNome() + "</font></center></html>");
                nomeLabel.setForeground(COR_TEXTO);
                nomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
                btnTime.add(nomeLabel, BorderLayout.SOUTH);

                btnTime.addActionListener(e -> selecionarTime(time));

                btnTime.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        btnTime.setBackground(COR_DESTAQUE);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        btnTime.setBackground(COR_PAINEL);
                    }
                });

                gridTimes.add(btnTime);
            }

            JScrollPane scroll = new JScrollPane(gridTimes);
            scroll.setBorder(null);
            scroll.getViewport().setBackground(COR_FUNDO);
            tela.add(scroll);
        } else {
            // Mostra resumo do time selecionado
            mostrarResumoTime(tela);
        }

        painelConteudo.add(tela, BorderLayout.CENTER);
        painelConteudo.revalidate();
        painelConteudo.repaint();
    }

    private void selecionarTime(Team time) {
        timeSelecionado = time;
        time.setTimeHumano(true);

        // Cria campeonato
        campeonato = new League("Campeonato Brasileiro Série A", "Brasil", 1);
        for (Team t : todosOsTimes) {
            campeonato.addTime(t);
        }
        campeonato.gerarCalendario();

        JOptionPane.showMessageDialog(this,
                "Você agora é o técnico do " + time.getNome() + "!\n" +
                        "Boa sorte na temporada!",
                "Time Selecionado",
                JOptionPane.INFORMATION_MESSAGE);

        mostrarTelaInicial();
    }

    private void mostrarResumoTime(JPanel tela) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COR_PAINEL);
        card.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        card.setMaximumSize(new Dimension(600, 400));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nomeTime = new JLabel(timeSelecionado.getNome());
        nomeTime.setForeground(COR_DESTAQUE);
        nomeTime.setFont(new Font("Segoe UI", Font.BOLD, 28));
        nomeTime.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(nomeTime);

        card.add(Box.createVerticalStrut(20));

        // Estatísticas
        String[][] stats = {
                { "Força do Time", String.valueOf(timeSelecionado.getForcaTime()) },
                { "Jogadores", String.valueOf(timeSelecionado.getJogadores().size()) },
                { "Saldo", "R$ " + String.format("%,d", timeSelecionado.getSaldo()) },
                { "Estádio", timeSelecionado.getEstadio().getNome() },
                { "Capacidade", String.format("%,d", timeSelecionado.getEstadio().getCapacidade()) }
        };

        for (String[] stat : stats) {
            JPanel linha = new JPanel(new BorderLayout());
            linha.setBackground(COR_PAINEL);
            linha.setMaximumSize(new Dimension(400, 30));

            JLabel label = new JLabel(stat[0]);
            label.setForeground(COR_TEXTO_SEC);
            linha.add(label, BorderLayout.WEST);

            JLabel valor = new JLabel(stat[1]);
            valor.setForeground(COR_TEXTO);
            valor.setFont(new Font("Segoe UI", Font.BOLD, 14));
            linha.add(valor, BorderLayout.EAST);

            card.add(linha);
            card.add(Box.createVerticalStrut(10));
        }

        // Posição no campeonato
        if (campeonato != null) {
            card.add(Box.createVerticalStrut(10));

            List<Team> classificacao = campeonato.getClassificacao();
            int posicao = classificacao.indexOf(timeSelecionado) + 1;

            JLabel labelCamp = new JLabel("Campeonato: " + posicao + "º lugar - " +
                    timeSelecionado.getPontos() + " pts");
            labelCamp.setForeground(COR_DESTAQUE);
            labelCamp.setFont(new Font("Segoe UI", Font.BOLD, 16));
            labelCamp.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(labelCamp);
        }

        tela.add(card);
    }

    private void mostrarMeuTime() {
        if (timeSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um time primeiro!");
            return;
        }

        painelConteudo.removeAll();

        JPanel tela = new JPanel(new BorderLayout(20, 20));
        tela.setBackground(COR_FUNDO);

        // Título
        JLabel titulo = new JLabel("Elenco - " + timeSelecionado.getNome());
        titulo.setForeground(COR_TEXTO);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        tela.add(titulo, BorderLayout.NORTH);

        // Tabela de jogadores
        String[] colunas = { "Nome", "Pos", "Idade", "Força", "Energia", "Moral", "Salário" };
        Object[][] dados = new Object[timeSelecionado.getJogadores().size()][7];

        int i = 0;
        for (Player p : timeSelecionado.getJogadores()) {
            dados[i][0] = p.getNome();
            dados[i][1] = p.getPosicaoOriginal().getSigla();
            dados[i][2] = p.getIdade();
            dados[i][3] = p.getForca();
            dados[i][4] = p.getEnergia() + "%";
            dados[i][5] = p.getMoral() + "%";
            dados[i][6] = "R$ " + String.format("%,d", p.getSalario());
            i++;
        }

        JTable tabela = new JTable(dados, colunas);
        tabela.setBackground(COR_PAINEL);
        tabela.setForeground(COR_TEXTO);
        tabela.setGridColor(COR_FUNDO);
        tabela.setRowHeight(30);
        tabela.getTableHeader().setBackground(COR_DESTAQUE);
        tabela.getTableHeader().setForeground(COR_TEXTO);
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COR_PAINEL);
        tela.add(scroll, BorderLayout.CENTER);

        painelConteudo.add(tela);
        painelConteudo.revalidate();
        painelConteudo.repaint();
    }

    private void mostrarEscalacao() {
        if (timeSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um time primeiro!");
            return;
        }

        painelConteudo.removeAll();

        JPanel tela = new JPanel(new BorderLayout(20, 20));
        tela.setBackground(COR_FUNDO);

        // Título
        JLabel titulo = new JLabel("Escalação - " + timeSelecionado.getTatica().getFormacao().getNome());
        titulo.setForeground(COR_TEXTO);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        tela.add(titulo, BorderLayout.NORTH);

        // Campo
        JPanel campo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fundo do campo
                g2.setColor(new Color(34, 139, 34));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Linhas
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));

                // Bordas
                int margem = 30;
                g2.drawRect(margem, margem, getWidth() - 2 * margem, getHeight() - 2 * margem);

                // Meio campo
                int meioY = getHeight() / 2;
                g2.drawLine(margem, meioY, getWidth() - margem, meioY);

                // Círculo central
                int raio = 50;
                g2.drawOval(getWidth() / 2 - raio, meioY - raio, raio * 2, raio * 2);

                // Áreas
                int areaW = 150;
                int areaH = 100;
                g2.drawRect(getWidth() / 2 - areaW / 2, margem, areaW, areaH);
                g2.drawRect(getWidth() / 2 - areaW / 2, getHeight() - margem - areaH, areaW, areaH);
            }
        };
        campo.setPreferredSize(new Dimension(600, 500));
        campo.setLayout(null);

        // Desenha jogadores
        int[][] posicoes = timeSelecionado.getTatica().getFormacao().getPosicoesCampo();
        List<Player> titulares = timeSelecionado.getTitulares();

        for (int i = 0; i < Math.min(11, titulares.size()); i++) {
            Player p = titulares.get(i);
            int x = (int) (posicoes[i][0] * 5.5) + 30;
            int y = (int) ((100 - posicoes[i][1]) * 4.5) + 30;

            JLabel jogador = new JLabel("<html><center><b>" + p.getForca() + "</b><br>" +
                    p.getNome().split(" ")[0] + "</center></html>");
            jogador.setOpaque(true);
            jogador.setBackground(p.isGoleiro() ? COR_AMARELO : COR_VERDE);
            jogador.setForeground(Color.BLACK);
            jogador.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            jogador.setHorizontalAlignment(SwingConstants.CENTER);
            jogador.setBounds(x - 30, y - 20, 60, 40);
            campo.add(jogador);
        }

        tela.add(campo, BorderLayout.CENTER);

        // Painel lateral com reservas
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(COR_PAINEL);
        lateral.setPreferredSize(new Dimension(200, 0));
        lateral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblReservas = new JLabel("Reservas");
        lblReservas.setForeground(COR_DESTAQUE);
        lblReservas.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lateral.add(lblReservas);
        lateral.add(Box.createVerticalStrut(10));

        for (Player p : timeSelecionado.getReservas()) {
            JLabel jogador = new JLabel(p.getPosicaoOriginal().getSigla() + " - " +
                    p.getNome().split(" ")[0] + " (" + p.getForca() + ")");
            jogador.setForeground(COR_TEXTO);
            lateral.add(jogador);
            lateral.add(Box.createVerticalStrut(5));
        }

        tela.add(lateral, BorderLayout.EAST);

        painelConteudo.add(tela);
        painelConteudo.revalidate();
        painelConteudo.repaint();
    }

    private void mostrarTabela() {
        if (campeonato == null) {
            JOptionPane.showMessageDialog(this, "Inicie um campeonato primeiro!");
            return;
        }

        painelConteudo.removeAll();

        JPanel tela = new JPanel(new BorderLayout(20, 20));
        tela.setBackground(COR_FUNDO);

        // Título
        JLabel titulo = new JLabel("Tabela - " + campeonato.getNome());
        titulo.setForeground(COR_TEXTO);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        tela.add(titulo, BorderLayout.NORTH);

        // Tabela
        String[] colunas = { "#", "Time", "P", "J", "V", "E", "D", "GP", "GC", "SG" };
        List<Team> classificacao = campeonato.getClassificacao();
        Object[][] dados = new Object[classificacao.size()][10];

        for (int i = 0; i < classificacao.size(); i++) {
            Team t = classificacao.get(i);
            dados[i][0] = i + 1;
            dados[i][1] = t.getNome();
            dados[i][2] = t.getPontos();
            dados[i][3] = t.getVitorias() + t.getEmpates() + t.getDerrotas();
            dados[i][4] = t.getVitorias();
            dados[i][5] = t.getEmpates();
            dados[i][6] = t.getDerrotas();
            dados[i][7] = t.getGolsPro();
            dados[i][8] = t.getGolsContra();
            dados[i][9] = t.getSaldoGols();
        }

        JTable tabela = new JTable(dados, colunas) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                // Destaca zona de classificação
                if (row < 4) {
                    c.setBackground(new Color(0, 100, 200, 50));
                } else if (row >= classificacao.size() - 4) {
                    c.setBackground(new Color(200, 50, 50, 50));
                } else {
                    c.setBackground(COR_PAINEL);
                }

                // Destaca time do jogador
                if (classificacao.get(row) == timeSelecionado) {
                    c.setBackground(COR_DESTAQUE);
                }

                return c;
            }
        };

        tabela.setBackground(COR_PAINEL);
        tabela.setForeground(COR_TEXTO);
        tabela.setGridColor(COR_FUNDO);
        tabela.setRowHeight(30);
        tabela.getTableHeader().setBackground(COR_DESTAQUE);
        tabela.getTableHeader().setForeground(COR_TEXTO);

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COR_PAINEL);
        tela.add(scroll, BorderLayout.CENTER);

        // Legenda
        JPanel legenda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        legenda.setBackground(COR_FUNDO);

        JLabel legLibert = new JLabel("● Libertadores");
        legLibert.setForeground(new Color(0, 150, 255));
        legenda.add(legLibert);

        JLabel legRebaixamento = new JLabel("● Rebaixamento");
        legRebaixamento.setForeground(COR_VERMELHO);
        legenda.add(legRebaixamento);

        tela.add(legenda, BorderLayout.SOUTH);

        painelConteudo.add(tela);
        painelConteudo.revalidate();
        painelConteudo.repaint();
    }

    private void mostrarCampeonato() {
        if (campeonato == null) {
            JOptionPane.showMessageDialog(this, "Inicie um campeonato primeiro!");
            return;
        }

        painelConteudo.removeAll();

        JPanel tela = new JPanel(new BorderLayout(20, 20));
        tela.setBackground(COR_FUNDO);

        // Título
        JLabel titulo = new JLabel("Jogos - Rodada " + (campeonato.getRodadaAtual() + 1));
        titulo.setForeground(COR_TEXTO);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        tela.add(titulo, BorderLayout.NORTH);

        // Lista de jogos da próxima rodada
        JPanel listaJogos = new JPanel();
        listaJogos.setLayout(new BoxLayout(listaJogos, BoxLayout.Y_AXIS));
        listaJogos.setBackground(COR_FUNDO);

        List<Match> jogos = campeonato.getJogosRodada(campeonato.getRodadaAtual() + 1);

        for (Match jogo : jogos) {
            JPanel jogoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            jogoPanel.setBackground(COR_PAINEL);
            jogoPanel.setMaximumSize(new Dimension(600, 50));

            boolean meuJogo = jogo.getMandante() == timeSelecionado || jogo.getVisitante() == timeSelecionado;

            JLabel mandante = new JLabel(jogo.getMandante().getNome());
            mandante.setForeground(meuJogo ? COR_DESTAQUE : COR_TEXTO);
            mandante.setFont(new Font("Segoe UI", meuJogo ? Font.BOLD : Font.PLAIN, 14));

            JLabel vs = new JLabel(
                    jogo.isFinalizada() ? jogo.getGolsMandante() + " x " + jogo.getGolsVisitante() : "vs");
            vs.setForeground(COR_TEXTO_SEC);

            JLabel visitante = new JLabel(jogo.getVisitante().getNome());
            visitante.setForeground(meuJogo ? COR_DESTAQUE : COR_TEXTO);
            visitante.setFont(new Font("Segoe UI", meuJogo ? Font.BOLD : Font.PLAIN, 14));

            jogoPanel.add(mandante);
            jogoPanel.add(vs);
            jogoPanel.add(visitante);

            listaJogos.add(jogoPanel);
            listaJogos.add(Box.createVerticalStrut(5));
        }

        JScrollPane scroll = new JScrollPane(listaJogos);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COR_FUNDO);
        tela.add(scroll, BorderLayout.CENTER);

        painelConteudo.add(tela);
        painelConteudo.revalidate();
        painelConteudo.repaint();
    }

    private void jogarProximaPartida() {
        if (campeonato == null || timeSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um time e inicie o campeonato!");
            return;
        }

        if (!timeSelecionado.escalacaoValida()) {
            JOptionPane.showMessageDialog(this, "Escalação inválida! Verifique os 11 titulares.");
            return;
        }

        int proximaRodada = campeonato.getRodadaAtual() + 1;

        if (proximaRodada > campeonato.getTotalRodadas()) {
            Team campeao = campeonato.getCampeao();
            JOptionPane.showMessageDialog(this,
                    "Campeonato encerrado!\n\nCampeão: " + campeao.getNome(),
                    "Fim da Temporada",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Busca o jogo do meu time
        Match meuJogo = null;
        for (Match jogo : campeonato.getJogosRodada(proximaRodada)) {
            if (jogo.getMandante() == timeSelecionado || jogo.getVisitante() == timeSelecionado) {
                meuJogo = jogo;
                break;
            }
        }

        if (meuJogo != null) {
            // Mostra tela da partida
            mostrarPartida(meuJogo);
        } else {
            // Simula rodada sem jogo do meu time - usa SwingWorker para não congelar
            final int rodada = proximaRodada;

            // Mostra diálogo de progresso
            JDialog dialogoProgresso = new JDialog(this, "Simulando...", false);
            dialogoProgresso.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            JPanel painelProgresso = new JPanel(new BorderLayout(10, 10));
            painelProgresso.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            painelProgresso.setBackground(COR_PAINEL);

            JLabel lblMsg = new JLabel("Simulando rodada " + rodada + "...");
            lblMsg.setForeground(COR_TEXTO);
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);

            painelProgresso.add(lblMsg, BorderLayout.NORTH);
            painelProgresso.add(progressBar, BorderLayout.CENTER);
            dialogoProgresso.add(painelProgresso);
            dialogoProgresso.pack();
            dialogoProgresso.setLocationRelativeTo(this);

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    campeonato.simularRodada(rodada);
                    return null;
                }

                @Override
                protected void done() {
                    dialogoProgresso.dispose();
                    mostrarTabela();
                }
            };

            worker.execute();
            dialogoProgresso.setVisible(true);
        }
    }

    private void mostrarPartida(Match jogo) {
        painelConteudo.removeAll();

        JPanel tela = new JPanel(new BorderLayout(20, 20));
        tela.setBackground(COR_FUNDO);

        // Painel do placar
        JPanel placar = new JPanel();
        placar.setLayout(new BoxLayout(placar, BoxLayout.Y_AXIS));
        placar.setBackground(COR_PAINEL);
        placar.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JLabel competicao = new JLabel(jogo.getCompeticao() + " - " + jogo.getFase());
        competicao.setForeground(COR_TEXTO_SEC);
        competicao.setAlignmentX(Component.CENTER_ALIGNMENT);
        placar.add(competicao);

        placar.add(Box.createVerticalStrut(20));

        JPanel times = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        times.setBackground(COR_PAINEL);

        JLabel mandante = new JLabel(jogo.getMandante().getNome());
        mandante.setForeground(COR_TEXTO);
        mandante.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel vs = new JLabel("vs");
        vs.setForeground(COR_DESTAQUE);
        vs.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel visitante = new JLabel(jogo.getVisitante().getNome());
        visitante.setForeground(COR_TEXTO);
        visitante.setFont(new Font("Segoe UI", Font.BOLD, 24));

        times.add(mandante);
        times.add(vs);
        times.add(visitante);
        placar.add(times);

        placar.add(Box.createVerticalStrut(30));

        // Botão jogar
        JButton btnJogar = new JButton("⚽ JOGAR PARTIDA");
        btnJogar.setBackground(COR_DESTAQUE);
        btnJogar.setForeground(Color.WHITE);
        btnJogar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnJogar.setBorderPainted(false);
        btnJogar.setFocusPainted(false);
        btnJogar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnJogar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnJogar.setMaximumSize(new Dimension(250, 50));

        btnJogar.addActionListener(e -> {
            // Desabilita botão durante simulação
            btnJogar.setEnabled(false);
            btnJogar.setText("Simulando...");

            // Usa SwingWorker para não congelar a UI
            SwingWorker<Match, Integer> worker = new SwingWorker<>() {
                @Override
                protected Match doInBackground() throws Exception {
                    // Simula a partida do jogador
                    Match resultado = matchEngine.simular(jogo.getMandante(), jogo.getVisitante(), false);

                    // Simula demais jogos da rodada em background
                    campeonato.simularRodada(campeonato.getRodadaAtual() + 1);

                    return resultado;
                }

                @Override
                protected void done() {
                    try {
                        Match resultado = get();
                        // Mostra resultado na EDT
                        mostrarResultado(resultado);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainWindow.this,
                                "Erro ao simular partida: " + ex.getMessage(),
                                "Erro", JOptionPane.ERROR_MESSAGE);
                        btnJogar.setEnabled(true);
                        btnJogar.setText("⚽ JOGAR PARTIDA");
                    }
                }
            };

            worker.execute();
        });

        placar.add(btnJogar);

        tela.add(placar, BorderLayout.CENTER);

        painelConteudo.add(tela);
        painelConteudo.revalidate();
        painelConteudo.repaint();
    }

    private void mostrarResultado(Match jogo) {
        painelConteudo.removeAll();

        JPanel tela = new JPanel(new BorderLayout(20, 20));
        tela.setBackground(COR_FUNDO);

        // Placar final
        JPanel placar = new JPanel();
        placar.setLayout(new BoxLayout(placar, BoxLayout.Y_AXIS));
        placar.setBackground(COR_PAINEL);
        placar.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JLabel lblFim = new JLabel("FIM DE JOGO");
        lblFim.setForeground(COR_DESTAQUE);
        lblFim.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFim.setAlignmentX(Component.CENTER_ALIGNMENT);
        placar.add(lblFim);

        placar.add(Box.createVerticalStrut(30));

        JLabel resultado = new JLabel(jogo.getMandante().getSigla() + " " +
                jogo.getGolsMandante() + " x " + jogo.getGolsVisitante() + " " +
                jogo.getVisitante().getSigla());
        resultado.setForeground(COR_TEXTO);
        resultado.setFont(new Font("Segoe UI", Font.BOLD, 48));
        resultado.setAlignmentX(Component.CENTER_ALIGNMENT);
        placar.add(resultado);

        placar.add(Box.createVerticalStrut(20));

        // Eventos do jogo
        JPanel eventos = new JPanel();
        eventos.setLayout(new BoxLayout(eventos, BoxLayout.Y_AXIS));
        eventos.setBackground(COR_PAINEL);

        for (MatchEvent evento : jogo.getEventos()) {
            if (evento.getTipo() == MatchEvent.TipoEvento.GOL ||
                    evento.getTipo() == MatchEvent.TipoEvento.CARTAO_AMARELO ||
                    evento.getTipo() == MatchEvent.TipoEvento.CARTAO_VERMELHO) {

                JLabel lblEvento = new JLabel(evento.toString());
                lblEvento.setForeground(COR_TEXTO);
                lblEvento.setAlignmentX(Component.CENTER_ALIGNMENT);
                eventos.add(lblEvento);
                eventos.add(Box.createVerticalStrut(5));
            }
        }

        JScrollPane scrollEventos = new JScrollPane(eventos);
        scrollEventos.setPreferredSize(new Dimension(500, 200));
        scrollEventos.setBorder(null);
        scrollEventos.getViewport().setBackground(COR_PAINEL);
        placar.add(scrollEventos);

        placar.add(Box.createVerticalStrut(20));

        // Botão continuar
        JButton btnContinuar = new JButton("Continuar");
        btnContinuar.setBackground(COR_DESTAQUE);
        btnContinuar.setForeground(Color.WHITE);
        btnContinuar.setBorderPainted(false);
        btnContinuar.setFocusPainted(false);
        btnContinuar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnContinuar.addActionListener(e -> mostrarTabela());
        placar.add(btnContinuar);

        tela.add(placar, BorderLayout.CENTER);

        painelConteudo.add(tela);
        painelConteudo.revalidate();
        painelConteudo.repaint();
    }

    private void mostrarFinancas() {
        if (timeSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um time primeiro!");
            return;
        }

        painelConteudo.removeAll();

        JPanel tela = new JPanel(new BorderLayout(20, 20));
        tela.setBackground(COR_FUNDO);

        JLabel titulo = new JLabel("Finanças - " + timeSelecionado.getNome());
        titulo.setForeground(COR_TEXTO);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        tela.add(titulo, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(COR_PAINEL);
        info.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        int totalSalarios = timeSelecionado.getJogadores().stream()
                .mapToInt(Player::getSalario).sum();

        String[][] dados = {
                { "Saldo Atual", "R$ " + String.format("%,d", timeSelecionado.getSaldo()) },
                { "Patrocínio Anual", "R$ " + String.format("%,d", timeSelecionado.getPatrocinioAnual()) },
                { "Folha Salarial", "R$ " + String.format("%,d", totalSalarios) + " /semana" },
                { "Valor do Elenco", "R$ " + String.format("%,d", calcularValorElenco()) }
        };

        for (String[] dado : dados) {
            JPanel linha = new JPanel(new BorderLayout());
            linha.setBackground(COR_PAINEL);
            linha.setMaximumSize(new Dimension(400, 40));

            JLabel lbl = new JLabel(dado[0]);
            lbl.setForeground(COR_TEXTO_SEC);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));

            JLabel valor = new JLabel(dado[1]);
            valor.setForeground(
                    dado[0].equals("Saldo Atual") && timeSelecionado.getSaldo() < 0 ? COR_VERMELHO : COR_VERDE);
            valor.setFont(new Font("Segoe UI", Font.BOLD, 18));

            linha.add(lbl, BorderLayout.WEST);
            linha.add(valor, BorderLayout.EAST);

            info.add(linha);
            info.add(Box.createVerticalStrut(15));
        }

        tela.add(info, BorderLayout.CENTER);

        painelConteudo.add(tela);
        painelConteudo.revalidate();
        painelConteudo.repaint();
    }

    private long calcularValorElenco() {
        return timeSelecionado.getJogadores().stream()
                .mapToLong(p -> (long) (p.getForca() * p.getForca() * 1000))
                .sum();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }

    /**
     * Salva o jogo atual em arquivo JSON.
     */
    private void salvarJogo() {
        if (timeSelecionado == null || campeonato == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um time e inicie o campeonato antes de salvar!",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Cria o SaveGame
        SaveGame saveGame = new SaveGame(timeSelecionado, campeonato);

        // Pergunta nome do save
        String nome = JOptionPane.showInputDialog(this,
                "Nome do save:",
                "Salvar Jogo",
                JOptionPane.PLAIN_MESSAGE);

        if (nome != null && !nome.trim().isEmpty()) {
            boolean sucesso = saveManager.save(saveGame, nome.trim());

            if (sucesso) {
                JOptionPane.showMessageDialog(this,
                        "✅ Jogo salvo com sucesso!\n\nArquivo: saves/" + nome.trim() + ".json",
                        "Jogo Salvo",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Erro ao salvar o jogo!",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Carrega um jogo salvo de arquivo JSON.
     */
    private void carregarJogo() {
        JFileChooser fileChooser = new JFileChooser(saveManager.getSavesPath().toFile());
        fileChooser.setDialogTitle("Carregar Jogo");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Save Files (*.json)", "json"));

        int resultado = fileChooser.showOpenDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            java.io.File arquivo = fileChooser.getSelectedFile();
            SaveGame saveGame = saveManager.load(arquivo);

            if (saveGame != null) {
                // Restaura o estado do jogo
                this.timeSelecionado = saveGame.getTimeJogador();
                this.campeonato = saveGame.getLiga();

                // Atualiza lista de times a partir do campeonato carregado
                if (campeonato != null) {
                    this.todosOsTimes = campeonato.getTimes();
                }

                JOptionPane.showMessageDialog(this,
                        "✅ Jogo carregado com sucesso!\n\n" +
                                "Time: " + (timeSelecionado != null ? timeSelecionado.getNome() : "N/A") + "\n" +
                                "Rodada: " + (campeonato != null ? campeonato.getRodadaAtual() : 0),
                        "Jogo Carregado",
                        JOptionPane.INFORMATION_MESSAGE);

                // Atualiza a tela
                mostrarTelaInicial();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Erro ao carregar o jogo!\nVerifique se o arquivo é válido.",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
