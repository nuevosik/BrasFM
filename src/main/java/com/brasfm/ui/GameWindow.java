package com.brasfm.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.brasfm.model.*;
import com.brasfm.engine.*;
import com.brasfm.championship.*;
import com.brasfm.audio.SoundSystem;
import com.brasfm.ui.panels.*;
import com.formdev.flatlaf.FlatDarkLaf;
import java.util.List;

/**
 * Interface principal do BrasFM - Refatorada com CardLayout.
 * Usa painéis separados para cada tela, mantendo o código organizado.
 */
public class GameWindow extends JFrame {

    // Cores do tema
    private static final Color DARK_BG = new Color(30, 30, 30);
    private static final Color PANEL_BG = new Color(40, 44, 52);
    private static final Color ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color TEXT_WHITE = new Color(236, 240, 241);
    private static final Color TEXT_GRAY = new Color(149, 165, 166);

    // Componentes principais
    private Team timeAtual;
    private List<Team> todosOsTimes;
    private League campeonato;
    private SoundSystem soundSystem;
    private TeamGenerator teamGenerator;

    // CardLayout para navegação
    private CardLayout cardLayout;
    private JPanel mainContainer;

    // Painéis
    private ElencoPanel elencoPanel;
    private TaticaPanel taticaPanel;
    private TabelaPanel tabelaPanel;
    private FinancasPanel financasPanel;
    private ProximoJogoPanel proximoJogoPanel;
    private TreinamentoPanel treinamentoPanel;
    private OlheirosPanel olheirosPanel;
    private MercadoPanel mercadoPanel;

    // Tela atual
    private String telaAtual = "ELENCO";

    // Nome do técnico
    private String nomeTecnico = "Técnico";

    public GameWindow() {
        setTitle("BrasFM - Football Manager Brasileiro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));

        // Inicializa
        teamGenerator = new TeamGenerator();
        todosOsTimes = teamGenerator.gerarTimesBrasileiros();

        // Inicia sistema de som
        soundSystem = new SoundSystem();
        soundSystem.carregarSons();

        // Mostra tela de seleção primeiro
        mostrarTelaSelecao();
    }

    private void mostrarTelaSelecao() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(DARK_BG);

        // Título
        JLabel titulo = new JLabel("⚽ BrasFM - Escolha seu Time");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titulo.setForeground(ACCENT_GREEN);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        add(titulo, BorderLayout.NORTH);

        // Grid de times
        JPanel gridTimes = new JPanel(new GridLayout(4, 5, 15, 15));
        gridTimes.setBackground(DARK_BG);
        gridTimes.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));

        for (Team time : todosOsTimes) {
            JButton btn = criarBotaoTime(time);
            gridTimes.add(btn);
        }

        add(new JScrollPane(gridTimes), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JButton criarBotaoTime(Team time) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout(5, 5));
        btn.setPreferredSize(new Dimension(150, 120));
        btn.setBackground(PANEL_BG);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Carregar logo do time
        if (time.getEscudoPath() != null && !time.getEscudoPath().isEmpty()) {
            try {
                String logoPath = time.getEscudoPath();
                java.io.File logoFile = new java.io.File(logoPath);
                if (logoFile.exists()) {
                    javax.swing.ImageIcon originalIcon = new javax.swing.ImageIcon(logoPath);
                    java.awt.Image scaledImage = originalIcon.getImage().getScaledInstance(48, 48,
                            java.awt.Image.SCALE_SMOOTH);
                    JLabel logoLabel = new JLabel(new javax.swing.ImageIcon(scaledImage));
                    logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    btn.add(logoLabel, BorderLayout.NORTH);
                }
            } catch (Exception ex) {
                // Se não conseguir carregar o logo, continua sem ele
                System.err.println("Erro ao carregar logo: " + time.getEscudoPath() + " - " + ex.getMessage());
            }
        }

        // Nome e força
        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);

        JLabel lblNome = new JLabel(time.getNome());
        lblNome.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNome.setForeground(TEXT_WHITE);
        lblNome.setHorizontalAlignment(SwingConstants.CENTER);
        info.add(lblNome);

        JLabel lblForca = new JLabel("⭐ " + time.getForcaTime());
        lblForca.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblForca.setForeground(ACCENT_GREEN);
        lblForca.setHorizontalAlignment(SwingConstants.CENTER);
        info.add(lblForca);

        btn.add(info, BorderLayout.CENTER);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ACCENT_GREEN.darker());
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_GREEN, 2),
                        BorderFactory.createEmptyBorder(9, 9, 9, 9)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(PANEL_BG);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            }
        });

        btn.addActionListener(e -> selecionarTime(time));
        return btn;
    }

    private void selecionarTime(Team time) {
        this.timeAtual = time;

        // Pede o nome do técnico
        String nome = (String) JOptionPane.showInputDialog(
                this,
                "Digite o nome do seu técnico:",
                "⚽ Novo Jogo - " + time.getNome(),
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "Técnico");

        if (nome == null || nome.trim().isEmpty()) {
            // Usuário cancelou ou não digitou nada
            return;
        }

        this.nomeTecnico = nome.trim();

        // Cria campeonato
        campeonato = new League("Brasileirão Série A", todosOsTimes);
        campeonato.gerarCalendario();

        // Inicializa interface principal com CardLayout
        initMainInterface();
    }

    /**
     * Inicializa a interface principal com CardLayout para navegação.
     */
    private void initMainInterface() {
        getContentPane().removeAll();
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(DARK_BG);

        // Barra superior com menu
        add(criarBarraSuperior(), BorderLayout.NORTH);

        // Container com CardLayout para trocar telas
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(DARK_BG);

        // Cria todos os painéis
        criarPaineis();

        // Adiciona painéis ao CardLayout
        mainContainer.add(criarTelaElenco(), "ELENCO");
        mainContainer.add(taticaPanel, "TATICA");
        mainContainer.add(tabelaPanel, "TABELA");
        mainContainer.add(financasPanel, "FINANCAS");
        mainContainer.add(treinamentoPanel, "TREINO");
        mainContainer.add(olheirosPanel, "OLHEIROS");
        mainContainer.add(mercadoPanel, "MERCADO");

        add(mainContainer, BorderLayout.CENTER);

        // News Ticker na parte inferior
        NewsTickerPanel newsTicker = new NewsTickerPanel();
        add(newsTicker, BorderLayout.SOUTH);

        // Mostra elenco por padrão
        cardLayout.show(mainContainer, "ELENCO");
        telaAtual = "ELENCO";

        revalidate();
        repaint();
    }

    private void criarPaineis() {
        elencoPanel = new ElencoPanel(timeAtual);
        taticaPanel = new TaticaPanel(timeAtual);
        tabelaPanel = new TabelaPanel(campeonato);
        financasPanel = new FinancasPanel(timeAtual);
        proximoJogoPanel = new ProximoJogoPanel(timeAtual, campeonato, this::jogarPartida);
        treinamentoPanel = new TreinamentoPanel(timeAtual);
        olheirosPanel = new OlheirosPanel(timeAtual, todosOsTimes);
        mercadoPanel = new MercadoPanel(timeAtual, todosOsTimes);
    }

    /**
     * Cria a tela de elenco com painel lateral de próximo jogo.
     */
    private JPanel criarTelaElenco() {
        JPanel tela = new JPanel(new BorderLayout(10, 0));
        tela.setBackground(DARK_BG);
        tela.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Painel esquerdo com info do time e próximo jogo
        JPanel painelEsquerdo = new JPanel();
        painelEsquerdo.setLayout(new BoxLayout(painelEsquerdo, BoxLayout.Y_AXIS));
        painelEsquerdo.setBackground(DARK_BG);
        painelEsquerdo.setPreferredSize(new Dimension(220, 0));

        painelEsquerdo.add(criarPainelInfoTime());
        painelEsquerdo.add(Box.createVerticalStrut(15));
        painelEsquerdo.add(proximoJogoPanel);
        painelEsquerdo.add(Box.createVerticalGlue());

        tela.add(painelEsquerdo, BorderLayout.WEST);
        tela.add(elencoPanel, BorderLayout.CENTER);

        return tela;
    }

    private JPanel criarPainelInfoTime() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(PANEL_BG);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel lblNome = new JLabel(timeAtual.getNome());
        lblNome.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblNome.setForeground(ACCENT_GREEN);
        lblNome.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblNome);
        painel.add(Box.createVerticalStrut(5));

        JLabel lblTecnico = new JLabel("👤 " + nomeTecnico);
        lblTecnico.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblTecnico.setForeground(TEXT_WHITE);
        lblTecnico.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblTecnico);
        painel.add(Box.createVerticalStrut(10));

        JLabel lblForca = new JLabel("⭐ Força: " + timeAtual.getForcaTime());
        lblForca.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblForca.setForeground(TEXT_GRAY);
        lblForca.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblForca);

        JLabel lblPontos = new JLabel("🏆 Pontos: " + timeAtual.getPontos());
        lblPontos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPontos.setForeground(TEXT_GRAY);
        lblPontos.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblPontos);

        return painel;
    }

    private JPanel criarBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(new Color(20, 26, 36));
        barra.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        barra.setPreferredSize(new Dimension(0, 50));

        JLabel logo = new JLabel("⚽ BrasFM");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(ACCENT_GREEN);
        barra.add(logo, BorderLayout.WEST);

        JPanel menuNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        menuNav.setOpaque(false);

        String[] menus = { "Elenco", "Tática", "Mercado", "Treino", "Olheiros", "Tabela", "Finanças" };
        for (String menu : menus) {
            JButton btn = criarBotaoMenu(menu);
            menuNav.add(btn);
        }
        barra.add(menuNav, BorderLayout.CENTER);

        JLabel lblData = new JLabel("📅 Rodada " + (campeonato.getRodadaAtual() + 1));
        lblData.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblData.setForeground(TEXT_GRAY);
        barra.add(lblData, BorderLayout.EAST);

        return barra;
    }

    private JButton criarBotaoMenu(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_WHITE);
        btn.setBackground(PANEL_BG);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ACCENT_GREEN);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(PANEL_BG);
            }
        });

        // Navegação com CardLayout
        btn.addActionListener(e -> navegarPara(texto));

        return btn;
    }

    /**
     * Navega para a tela especificada usando CardLayout.
     */
    private void navegarPara(String tela) {
        switch (tela) {
            case "Elenco":
                elencoPanel.atualizar();
                cardLayout.show(mainContainer, "ELENCO");
                telaAtual = "ELENCO";
                break;
            case "Tática":
                taticaPanel.atualizar();
                cardLayout.show(mainContainer, "TATICA");
                telaAtual = "TATICA";
                break;
            case "Treino":
                treinamentoPanel.atualizar();
                cardLayout.show(mainContainer, "TREINO");
                telaAtual = "TREINO";
                break;
            case "Olheiros":
                olheirosPanel.atualizar();
                cardLayout.show(mainContainer, "OLHEIROS");
                telaAtual = "OLHEIROS";
                break;
            case "Tabela":
                tabelaPanel.atualizar();
                cardLayout.show(mainContainer, "TABELA");
                telaAtual = "TABELA";
                break;
            case "Mercado":
                mercadoPanel.atualizar();
                cardLayout.show(mainContainer, "MERCADO");
                telaAtual = "MERCADO";
                break;
            case "Finanças":
                cardLayout.show(mainContainer, "FINANCAS");
                telaAtual = "FINANCAS";
                break;
        }
    }

    /**
     * Inicia uma partida ao vivo.
     */
    private void jogarPartida() {
        int proximaRodada = campeonato.getRodadaAtual() + 1;
        if (proximaRodada > campeonato.getTotalRodadas()) {
            JOptionPane.showMessageDialog(this, "🏆 Campeonato encerrado!\nCampeão: " +
                    campeonato.getCampeao().getNome());
            return;
        }

        // Encontra o jogo do time do jogador
        List<Match> jogosRodada = campeonato.getJogosRodada(proximaRodada);
        Match jogoJogador = null;
        for (Match m : jogosRodada) {
            if (m.getMandante() == timeAtual || m.getVisitante() == timeAtual) {
                jogoJogador = m;
                break;
            }
        }

        if (jogoJogador == null)
            return;

        // Abre a janela de partida ao vivo
        iniciarPartidaVisual(jogoJogador, proximaRodada);
    }

    /**
     * Abre a janela de partida ao vivo com narração em tempo real.
     */
    private void iniciarPartidaVisual(Match partida, int rodada) {
        JDialog janelaJogo = new JDialog(this, "⚽ Ao Vivo - Rodada " + rodada, true);
        janelaJogo.setSize(900, 600);
        janelaJogo.setLocationRelativeTo(this);
        janelaJogo.setLayout(new BorderLayout(0, 0));
        janelaJogo.getContentPane().setBackground(DARK_BG);

        // Painel do placar
        JPanel painelPlacar = new JPanel(new BorderLayout());
        painelPlacar.setBackground(new Color(20, 26, 36));
        painelPlacar.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Time da casa
        JPanel painelCasa = new JPanel();
        painelCasa.setLayout(new BoxLayout(painelCasa, BoxLayout.Y_AXIS));
        painelCasa.setOpaque(false);
        JLabel lblCasaNome = new JLabel(partida.getMandante().getSigla());
        lblCasaNome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblCasaNome.setForeground(partida.getMandante() == timeAtual ? ACCENT_GREEN : TEXT_WHITE);
        lblCasaNome.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelCasa.add(lblCasaNome);

        JLabel lblCasaFull = new JLabel(partida.getMandante().getNome());
        lblCasaFull.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCasaFull.setForeground(TEXT_GRAY);
        lblCasaFull.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelCasa.add(lblCasaFull);

        // Placar central
        JLabel lblPlacar = new JLabel("0 x 0");
        lblPlacar.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblPlacar.setForeground(TEXT_WHITE);
        lblPlacar.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblMinuto = new JLabel("0'");
        lblMinuto.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMinuto.setForeground(ACCENT_GREEN);
        lblMinuto.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel painelCentro = new JPanel();
        painelCentro.setLayout(new BoxLayout(painelCentro, BoxLayout.Y_AXIS));
        painelCentro.setOpaque(false);
        lblPlacar.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblMinuto.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelCentro.add(lblPlacar);
        painelCentro.add(lblMinuto);

        // Time visitante
        JPanel painelFora = new JPanel();
        painelFora.setLayout(new BoxLayout(painelFora, BoxLayout.Y_AXIS));
        painelFora.setOpaque(false);
        JLabel lblForaNome = new JLabel(partida.getVisitante().getSigla());
        lblForaNome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblForaNome.setForeground(partida.getVisitante() == timeAtual ? ACCENT_GREEN : TEXT_WHITE);
        lblForaNome.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelFora.add(lblForaNome);

        JLabel lblForaFull = new JLabel(partida.getVisitante().getNome());
        lblForaFull.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblForaFull.setForeground(TEXT_GRAY);
        lblForaFull.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelFora.add(lblForaFull);

        painelPlacar.add(painelCasa, BorderLayout.WEST);
        painelPlacar.add(painelCentro, BorderLayout.CENTER);
        painelPlacar.add(painelFora, BorderLayout.EAST);

        janelaJogo.add(painelPlacar, BorderLayout.NORTH);

        // Painel central com campo 2D, stats e narração
        JPanel painelCentralPrincipal = new JPanel(new BorderLayout(10, 0));
        painelCentralPrincipal.setBackground(DARK_BG);
        painelCentralPrincipal.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Painel esquerdo: Campo 2D + Stats
        JPanel painelVisual = new JPanel();
        painelVisual.setLayout(new BoxLayout(painelVisual, BoxLayout.Y_AXIS));
        painelVisual.setBackground(DARK_BG);
        painelVisual.setPreferredSize(new Dimension(280, 0));

        // Campo 2D
        CampoVisualizacao campoVisual = new CampoVisualizacao();
        campoVisual.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelVisual.add(campoVisual);
        painelVisual.add(Box.createVerticalStrut(10));

        // Painel de estatísticas
        StatsPanel statsPanel = new StatsPanel();
        statsPanel.setTimes(partida.getMandante().getSigla(), partida.getVisitante().getSigla());
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelVisual.add(statsPanel);

        painelCentralPrincipal.add(painelVisual, BorderLayout.WEST);

        // Área de narração
        JTextArea narracao = new JTextArea();
        narracao.setEditable(false);
        narracao.setBackground(PANEL_BG);
        narracao.setForeground(TEXT_WHITE);
        narracao.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        narracao.setLineWrap(true);
        narracao.setWrapStyleWord(true);
        narracao.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JScrollPane scrollNarracao = new JScrollPane(narracao);
        scrollNarracao.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                "📝 Narração", 0, 0, new Font("Segoe UI", Font.BOLD, 12), TEXT_GRAY));
        scrollNarracao.getViewport().setBackground(PANEL_BG);

        painelCentralPrincipal.add(scrollNarracao, BorderLayout.CENTER);
        janelaJogo.add(painelCentralPrincipal, BorderLayout.CENTER);

        // Barra de progresso
        JProgressBar barraProgresso = new JProgressBar(0, 90);
        barraProgresso.setValue(0);
        barraProgresso.setStringPainted(true);
        barraProgresso.setString("Início do jogo");
        barraProgresso.setForeground(ACCENT_GREEN);
        barraProgresso.setBackground(PANEL_BG);
        barraProgresso.setBorder(BorderFactory.createEmptyBorder(10, 30, 15, 30));

        janelaJogo.add(barraProgresso, BorderLayout.SOUTH);

        // Cria a engine de partida ao vivo
        LiveMatchEngine engine = new LiveMatchEngine(partida);

        // Configura callbacks para atualizar a UI
        engine.setCallbacks(
                // onNarracao
                texto -> {
                    narracao.append(texto + "\n");
                    narracao.setCaretPosition(narracao.getDocument().getLength());
                },
                // onMinutoChange
                min -> {
                    lblMinuto.setText(min + "'");
                    barraProgresso.setValue(min);
                },
                // onEvento
                evento -> {
                    // Atualiza placar
                    lblPlacar.setText(engine.getPlacar());

                    // Atualiza campo visual
                    switch (evento.tipo) {
                        case GOL_CASA:
                            campoVisual.simularEvento("gol", true);
                            if (soundSystem != null && partida.getMandante() == timeAtual)
                                soundSystem.tocarGol();
                            break;
                        case GOL_FORA:
                            campoVisual.simularEvento("gol", false);
                            if (soundSystem != null && partida.getVisitante() == timeAtual)
                                soundSystem.tocarGol();
                            break;
                        case CHUTE_CASA:
                            campoVisual.simularEvento("chute", true);
                            break;
                        case CHUTE_FORA:
                            campoVisual.simularEvento("chute", false);
                            break;
                        case ATAQUE_CASA:
                            campoVisual.simularEvento("ataque", true);
                            break;
                        case ATAQUE_FORA:
                            campoVisual.simularEvento("ataque", false);
                            break;
                        case DEFESA:
                            campoVisual.simularEvento("defesa", evento.casaAtaca);
                            break;
                        default:
                            campoVisual.simularEvento("meio", true);
                    }

                    // Atualiza estatísticas
                    statsPanel.setPosse(engine.getPosseCasa(), engine.getPosseFora());
                    statsPanel.setChutes(engine.getChutesCasa(), engine.getChutesFora());
                },
                // onIntervalo
                () -> barraProgresso.setString("⏸️ Intervalo"),
                // onFimJogo
                () -> {
                    barraProgresso.setString("⏹️ Fim de Jogo!");

                    // Simula outros jogos da rodada
                    for (Match m : campeonato.getJogosRodada(rodada)) {
                        if (m != partida && !m.isFinalizada()) {
                            int gM = (int) (Math.random() * 4);
                            int gV = (int) (Math.random() * 4);
                            for (int i = 0; i < gM; i++)
                                m.registrarGol(m.getMandante(), null, null);
                            for (int i = 0; i < gV; i++)
                                m.registrarGol(m.getVisitante(), null, null);
                            m.finalizar();
                        }
                    }

                    // Botão fechar
                    JButton btnFechar = new JButton("✓ Continuar");
                    btnFechar.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    btnFechar.setBackground(ACCENT_GREEN);
                    btnFechar.setForeground(Color.WHITE);
                    btnFechar.addActionListener(ev -> {
                        janelaJogo.dispose();
                        atualizarTodosPaineis();
                    });

                    JPanel painelBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    painelBtn.setOpaque(false);
                    painelBtn.add(btnFechar);

                    janelaJogo.remove(barraProgresso);
                    janelaJogo.add(painelBtn, BorderLayout.SOUTH);
                    janelaJogo.revalidate();
                });

        // Inicia a partida
        engine.iniciar();
        janelaJogo.setVisible(true);
    }

    /**
     * Atualiza todos os painéis após mudanças.
     */
    private void atualizarTodosPaineis() {
        elencoPanel.atualizar();
        tabelaPanel.atualizar();
        proximoJogoPanel.atualizar();

        // Recria a barra superior para atualizar rodada
        initMainInterface();
    }

    public static void main(String[] args) {
        try {
            FlatDarkLaf.setup();
        } catch (Exception e) {
            System.err.println("Falha ao iniciar tema FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }
}
