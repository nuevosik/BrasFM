package com.brasfm.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import com.brasfm.model.*;
import com.brasfm.engine.ScoutingNetwork;

/**
 * Painel de Olheiros e Busca de Jogadores.
 */
public class OlheirosPanel extends JPanel {

    private static final Color DARK_BG = new Color(30, 30, 30);
    private static final Color PANEL_BG = new Color(40, 44, 52);
    private static final Color ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color ACCENT_BLUE = new Color(52, 152, 219);
    private static final Color TEXT_WHITE = new Color(236, 240, 241);
    private static final Color TEXT_GRAY = new Color(149, 165, 166);
    private static final Color ACCENT_GOLD = new Color(241, 196, 15);

    private Team time;
    private ScoutingNetwork scoutingNetwork;
    private List<Player> jogadoresDescobertos;
    private JPanel painelJogadoresDisponiveis;
    private JTextArea logOlheiros;

    // Regiões de busca
    private static final String[] REGIOES = {
            "São Paulo", "Rio de Janeiro", "Minas Gerais", "Rio Grande do Sul",
            "Paraná", "Bahia", "Nordeste", "Argentina", "Uruguai", "Colômbia"
    };

    public OlheirosPanel(Team time, List<Team> todosOsTimes) {
        this.time = time;
        this.scoutingNetwork = new ScoutingNetwork(5000000); // R$ 5mi de orçamento
        // Gera jogadores descobertos de outros times
        this.jogadoresDescobertos = new ArrayList<>();
        for (Team t : todosOsTimes) {
            if (t != time) {
                // Adiciona alguns jogadores de cada time
                List<Player> jogadores = t.getJogadores();
                int quantidade = Math.min(3, jogadores.size());
                for (int i = 0; i < quantidade; i++) {
                    jogadoresDescobertos.add(jogadores.get(i));
                }
            }
        }

        setLayout(new BorderLayout(15, 15));
        setBackground(DARK_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initComponents();
    }

    public void setTime(Team time) {
        this.time = time;
        initComponents();
    }

    public void atualizar() {
        initComponents();
    }

    private void initComponents() {
        removeAll();

        // Título
        JLabel titulo = new JLabel("🔍 Departamento de Olheiros - " + time.getNome());
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(ACCENT_GREEN);
        add(titulo, BorderLayout.NORTH);

        // Painel principal
        JPanel painelPrincipal = new JPanel(new BorderLayout(15, 0));
        painelPrincipal.setBackground(DARK_BG);

        // Esquerda: Controles de busca
        JPanel painelBusca = criarPainelBusca();
        painelPrincipal.add(painelBusca, BorderLayout.WEST);

        // Centro: Jogadores disponíveis
        painelJogadoresDisponiveis = criarPainelJogadores();
        JScrollPane scrollJogadores = new JScrollPane(painelJogadoresDisponiveis);
        scrollJogadores.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                "📋 Jogadores Encontrados", 0, 0,
                new Font("Segoe UI", Font.BOLD, 12), TEXT_GRAY));
        scrollJogadores.getViewport().setBackground(DARK_BG);
        painelPrincipal.add(scrollJogadores, BorderLayout.CENTER);

        // Direita: Shortlist e Log
        JPanel painelDireita = criarPainelDireita();
        painelPrincipal.add(painelDireita, BorderLayout.EAST);

        add(painelPrincipal, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel criarPainelBusca() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(PANEL_BG);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        painel.setPreferredSize(new Dimension(220, 0));

        JLabel lblFiltros = new JLabel("🔎 Filtros de Busca");
        lblFiltros.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFiltros.setForeground(ACCENT_GREEN);
        lblFiltros.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblFiltros);
        painel.add(Box.createVerticalStrut(15));

        // Região
        JLabel lblRegiao = new JLabel("📍 Região");
        lblRegiao.setForeground(TEXT_GRAY);
        lblRegiao.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblRegiao);

        JComboBox<String> cbRegiao = new JComboBox<>(REGIOES);
        cbRegiao.setBackground(PANEL_BG);
        cbRegiao.setForeground(TEXT_WHITE);
        cbRegiao.setMaximumSize(new Dimension(200, 30));
        cbRegiao.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(cbRegiao);
        painel.add(Box.createVerticalStrut(10));

        // Posição
        JLabel lblPosicao = new JLabel("⚽ Posição");
        lblPosicao.setForeground(TEXT_GRAY);
        lblPosicao.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblPosicao);

        String[] posicoes = { "Todas", "Goleiro", "Zagueiro", "Lateral", "Volante", "Meia", "Atacante" };
        JComboBox<String> cbPosicao = new JComboBox<>(posicoes);
        cbPosicao.setBackground(PANEL_BG);
        cbPosicao.setForeground(TEXT_WHITE);
        cbPosicao.setMaximumSize(new Dimension(200, 30));
        cbPosicao.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(cbPosicao);
        painel.add(Box.createVerticalStrut(10));

        // Idade
        JLabel lblIdade = new JLabel("📅 Idade máxima");
        lblIdade.setForeground(TEXT_GRAY);
        lblIdade.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblIdade);

        JSlider sliderIdade = new JSlider(18, 35, 25);
        sliderIdade.setBackground(PANEL_BG);
        sliderIdade.setForeground(TEXT_WHITE);
        sliderIdade.setMajorTickSpacing(5);
        sliderIdade.setMinorTickSpacing(1);
        sliderIdade.setPaintTicks(true);
        sliderIdade.setPaintLabels(true);
        sliderIdade.setMaximumSize(new Dimension(200, 50));
        sliderIdade.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(sliderIdade);
        painel.add(Box.createVerticalStrut(15));

        // Botão buscar
        JButton btnBuscar = new JButton("🔍 Buscar Jogadores");
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBuscar.setBackground(ACCENT_GREEN);
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFocusPainted(false);
        btnBuscar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBuscar.setMaximumSize(new Dimension(200, 35));
        btnBuscar.addActionListener(e -> {
            buscarJogadores((String) cbPosicao.getSelectedItem(), sliderIdade.getValue());
        });
        painel.add(btnBuscar);

        painel.add(Box.createVerticalStrut(20));

        // Orçamento
        JLabel lblOrcamento = new JLabel("💰 Orçamento Olheiros");
        lblOrcamento.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblOrcamento.setForeground(ACCENT_GOLD);
        lblOrcamento.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblOrcamento);

        JLabel lblValor = new JLabel(String.format("R$ %,d disponível", scoutingNetwork.getOrcamentoDisponivel()));
        lblValor.setForeground(TEXT_WHITE);
        lblValor.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblValor);

        painel.add(Box.createVerticalGlue());

        return painel;
    }

    private JPanel criarPainelJogadores() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(DARK_BG);

        // Mostra alguns jogadores
        int count = 0;
        for (Player p : jogadoresDescobertos) {
            if (count++ >= 15)
                break;
            painel.add(criarCardJogadorDisponivel(p));
            painel.add(Box.createVerticalStrut(5));
        }

        if (jogadoresDescobertos.isEmpty()) {
            JLabel lblVazio = new JLabel("Nenhum jogador encontrado. Use os filtros para buscar.");
            lblVazio.setForeground(TEXT_GRAY);
            lblVazio.setAlignmentX(Component.CENTER_ALIGNMENT);
            painel.add(lblVazio);
        }

        return painel;
    }

    private JPanel criarCardJogadorDisponivel(Player jogador) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Info do jogador
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setOpaque(false);

        JLabel lblNome = new JLabel(jogador.getNome());
        lblNome.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNome.setForeground(TEXT_WHITE);
        infoPanel.add(lblNome);

        String posNome = jogador.getPosicao() != null ? jogador.getPosicao().getNome() : "N/A";
        JLabel lblPos = new JLabel(posNome + " | " + jogador.getIdade() + " anos");
        lblPos.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPos.setForeground(TEXT_GRAY);
        infoPanel.add(lblPos);

        JLabel lblForca = new JLabel(
                "⭐ Força: " + jogador.getForca() + " | Salário: R$ " + String.format("%,d", jogador.getSalario()));
        lblForca.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblForca.setForeground(ACCENT_GREEN);
        infoPanel.add(lblForca);

        card.add(infoPanel, BorderLayout.CENTER);

        // Botões de ação
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        botoesPanel.setOpaque(false);

        JButton btnShortlist = new JButton("⭐");
        btnShortlist.setToolTipText("Adicionar à Shortlist");
        btnShortlist.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnShortlist.setBackground(ACCENT_GOLD);
        btnShortlist.setForeground(Color.WHITE);
        btnShortlist.setFocusPainted(false);
        btnShortlist.setPreferredSize(new Dimension(35, 30));
        btnShortlist.addActionListener(e -> {
            scoutingNetwork.addShortlist(jogador);
            logOlheiros.append("⭐ " + jogador.getNome() + " adicionado à shortlist\n");
        });
        botoesPanel.add(btnShortlist);

        JButton btnObservar = new JButton("🔍");
        btnObservar.setToolTipText("Observar jogador");
        btnObservar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnObservar.setBackground(ACCENT_BLUE);
        btnObservar.setForeground(Color.WHITE);
        btnObservar.setFocusPainted(false);
        btnObservar.setPreferredSize(new Dimension(35, 30));
        btnObservar.addActionListener(e -> {
            logOlheiros.append("🔍 Observando " + jogador.getNome() + "...\n");
        });
        botoesPanel.add(btnObservar);

        card.add(botoesPanel, BorderLayout.EAST);

        return card;
    }

    private JPanel criarPainelDireita() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(DARK_BG);
        painel.setPreferredSize(new Dimension(250, 0));

        // Shortlist
        JPanel painelShortlist = new JPanel();
        painelShortlist.setLayout(new BoxLayout(painelShortlist, BoxLayout.Y_AXIS));
        painelShortlist.setBackground(PANEL_BG);
        painelShortlist.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel lblShortlist = new JLabel("⭐ Shortlist (" + scoutingNetwork.getShortlist().size() + ")");
        lblShortlist.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblShortlist.setForeground(ACCENT_GOLD);
        lblShortlist.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelShortlist.add(lblShortlist);
        painelShortlist.add(Box.createVerticalStrut(10));

        if (scoutingNetwork.getShortlist().isEmpty()) {
            JLabel lblVazio = new JLabel("Nenhum jogador na lista");
            lblVazio.setForeground(TEXT_GRAY);
            lblVazio.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            painelShortlist.add(lblVazio);
        } else {
            for (Player p : scoutingNetwork.getShortlist()) {
                JLabel lbl = new JLabel("• " + p.getNome() + " (" + p.getForca() + ")");
                lbl.setForeground(TEXT_WHITE);
                lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                painelShortlist.add(lbl);
            }
        }

        painel.add(painelShortlist);
        painel.add(Box.createVerticalStrut(15));

        // Log
        JPanel painelLog = new JPanel(new BorderLayout());
        painelLog.setBackground(PANEL_BG);
        painelLog.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel lblLog = new JLabel("📝 Atividades");
        lblLog.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLog.setForeground(ACCENT_GREEN);
        painelLog.add(lblLog, BorderLayout.NORTH);

        logOlheiros = new JTextArea();
        logOlheiros.setEditable(false);
        logOlheiros.setBackground(DARK_BG);
        logOlheiros.setForeground(TEXT_WHITE);
        logOlheiros.setFont(new Font("Consolas", Font.PLAIN, 10));
        logOlheiros.setText("Sistema de olheiros iniciado.\n");

        JScrollPane scrollLog = new JScrollPane(logOlheiros);
        scrollLog.setBorder(null);
        scrollLog.setPreferredSize(new Dimension(0, 200));
        painelLog.add(scrollLog, BorderLayout.CENTER);

        painel.add(painelLog);

        return painel;
    }

    private void buscarJogadores(String posicaoFiltro, int idadeMax) {
        painelJogadoresDisponiveis.removeAll();

        logOlheiros.append("\n🔍 Buscando: " + posicaoFiltro + ", até " + idadeMax + " anos...\n");

        int encontrados = 0;
        for (Player p : jogadoresDescobertos) {
            // Filtro de idade
            if (p.getIdade() > idadeMax)
                continue;

            // Filtro de posição
            if (!posicaoFiltro.equals("Todas")) {
                String posNome = p.getPosicao() != null ? p.getPosicao().getNome() : "";
                if (!posNome.toLowerCase().contains(posicaoFiltro.toLowerCase()))
                    continue;
            }

            painelJogadoresDisponiveis.add(criarCardJogadorDisponivel(p));
            painelJogadoresDisponiveis.add(Box.createVerticalStrut(5));
            encontrados++;

            if (encontrados >= 20)
                break;
        }

        if (encontrados == 0) {
            JLabel lblVazio = new JLabel("Nenhum jogador encontrado com esses critérios.");
            lblVazio.setForeground(TEXT_GRAY);
            painelJogadoresDisponiveis.add(lblVazio);
        }

        logOlheiros.append("✓ " + encontrados + " jogador(es) encontrado(s)\n");

        painelJogadoresDisponiveis.revalidate();
        painelJogadoresDisponiveis.repaint();
    }
}
