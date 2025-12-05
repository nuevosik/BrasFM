package com.brasfm.ui.panels;

import javax.swing.*;
import java.awt.*;
import com.brasfm.model.*;
import com.brasfm.engine.TrainingSystem;
import com.brasfm.engine.TrainingSystem.*;

/**
 * Painel de Treinamento do time.
 */
public class TreinamentoPanel extends JPanel {

    private static final Color DARK_BG = new Color(30, 30, 30);
    private static final Color PANEL_BG = new Color(40, 44, 52);
    private static final Color ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color TEXT_WHITE = new Color(236, 240, 241);
    private static final Color TEXT_GRAY = new Color(149, 165, 166);
    private static final Color ENERGY_GREEN = new Color(46, 204, 113);
    private static final Color ENERGY_YELLOW = new Color(241, 196, 15);
    private static final Color ENERGY_RED = new Color(231, 76, 60);

    private Team time;
    @SuppressWarnings("unused")
    private TrainingSystem trainingSystem;
    private TipoTreino tipoTreinoSelecionado = TipoTreino.TATICO;
    private IntensidadeTreino intensidadeSelecionada = IntensidadeTreino.NORMAL;
    private JTextArea logTreino;
    private JPanel painelJogadores;

    public TreinamentoPanel(Team time) {
        this.time = time;
        this.trainingSystem = new TrainingSystem();
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
        JLabel titulo = new JLabel("🏋️ Centro de Treinamento - " + time.getNome());
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(ACCENT_GREEN);
        add(titulo, BorderLayout.NORTH);

        // Painel principal dividido
        JPanel painelPrincipal = new JPanel(new BorderLayout(15, 0));
        painelPrincipal.setBackground(DARK_BG);

        // Esquerda: Configuração de treino
        JPanel painelConfig = criarPainelConfiguracaoTreino();
        painelPrincipal.add(painelConfig, BorderLayout.WEST);

        // Centro: Lista de jogadores
        painelJogadores = criarPainelJogadores();
        painelPrincipal.add(new JScrollPane(painelJogadores), BorderLayout.CENTER);

        // Direita: Log de resultados
        JPanel painelLog = criarPainelLog();
        painelPrincipal.add(painelLog, BorderLayout.EAST);

        add(painelPrincipal, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel criarPainelConfiguracaoTreino() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(PANEL_BG);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        painel.setPreferredSize(new Dimension(250, 0));

        JLabel lblTipoTreino = new JLabel("📋 Tipo de Treino");
        lblTipoTreino.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTipoTreino.setForeground(ACCENT_GREEN);
        lblTipoTreino.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblTipoTreino);
        painel.add(Box.createVerticalStrut(10));

        // Botões de tipo de treino
        ButtonGroup grupTipo = new ButtonGroup();
        for (TipoTreino tipo : TipoTreino.values()) {
            JRadioButton rb = new JRadioButton(tipo.getNome());
            rb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            rb.setForeground(TEXT_WHITE);
            rb.setBackground(PANEL_BG);
            rb.setFocusPainted(false);
            rb.setAlignmentX(Component.LEFT_ALIGNMENT);
            rb.setSelected(tipo == tipoTreinoSelecionado);
            rb.addActionListener(e -> tipoTreinoSelecionado = tipo);
            grupTipo.add(rb);
            painel.add(rb);

            // Tooltip com descrição
            rb.setToolTipText(tipo.getDescricao());
        }

        painel.add(Box.createVerticalStrut(20));

        JLabel lblIntensidade = new JLabel("⚡ Intensidade");
        lblIntensidade.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblIntensidade.setForeground(ACCENT_GREEN);
        lblIntensidade.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblIntensidade);
        painel.add(Box.createVerticalStrut(10));

        // Slider de intensidade
        JComboBox<IntensidadeTreino> cbIntensidade = new JComboBox<>(IntensidadeTreino.values());
        cbIntensidade.setSelectedItem(intensidadeSelecionada);
        cbIntensidade.setBackground(PANEL_BG);
        cbIntensidade.setForeground(TEXT_WHITE);
        cbIntensidade.setMaximumSize(new Dimension(200, 30));
        cbIntensidade.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbIntensidade.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof IntensidadeTreino) {
                    IntensidadeTreino i = (IntensidadeTreino) value;
                    setText(i.getNome() + " (x" + i.getFatorEvolucao() + ")");
                }
                return this;
            }
        });
        cbIntensidade
                .addActionListener(e -> intensidadeSelecionada = (IntensidadeTreino) cbIntensidade.getSelectedItem());
        painel.add(cbIntensidade);

        painel.add(Box.createVerticalStrut(15));

        // Aviso
        JLabel lblAviso = new JLabel("<html><small>⚠️ Treino Duplo pode causar lesões!</small></html>");
        lblAviso.setForeground(ENERGY_YELLOW);
        lblAviso.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblAviso);

        painel.add(Box.createVerticalStrut(20));

        // Botão treinar todos
        JButton btnTreinarTodos = new JButton("🏋️ Treinar Todos");
        btnTreinarTodos.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTreinarTodos.setBackground(ACCENT_GREEN);
        btnTreinarTodos.setForeground(Color.WHITE);
        btnTreinarTodos.setFocusPainted(false);
        btnTreinarTodos.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTreinarTodos.setMaximumSize(new Dimension(200, 40));
        btnTreinarTodos.addActionListener(e -> treinarTodosJogadores());
        painel.add(btnTreinarTodos);

        painel.add(Box.createVerticalStrut(10));

        // Botão recuperação
        JButton btnRecuperacao = new JButton("💤 Sessão de Recuperação");
        btnRecuperacao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRecuperacao.setBackground(new Color(52, 152, 219));
        btnRecuperacao.setForeground(Color.WHITE);
        btnRecuperacao.setFocusPainted(false);
        btnRecuperacao.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRecuperacao.setMaximumSize(new Dimension(200, 35));
        btnRecuperacao.addActionListener(e -> sessaoRecuperacao());
        painel.add(btnRecuperacao);

        painel.add(Box.createVerticalGlue());

        return painel;
    }

    private JPanel criarPainelJogadores() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(DARK_BG);

        JLabel lblTitulo = new JLabel("👥 Jogadores");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(ACCENT_GREEN);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblTitulo);
        painel.add(Box.createVerticalStrut(10));

        for (Player p : time.getJogadores()) {
            JPanel cardJogador = criarCardJogador(p);
            painel.add(cardJogador);
            painel.add(Box.createVerticalStrut(5));
        }

        return painel;
    }

    private JPanel criarCardJogador(Player jogador) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Nome e posição
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);

        JLabel lblNome = new JLabel(jogador.getNome());
        lblNome.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNome.setForeground(TEXT_WHITE);
        infoPanel.add(lblNome);

        String posNome = jogador.getPosicao() != null ? jogador.getPosicao().getSigla() : "N/A";
        JLabel lblPos = new JLabel(posNome + " | Força: " + jogador.getForca());
        lblPos.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblPos.setForeground(TEXT_GRAY);
        infoPanel.add(lblPos);

        card.add(infoPanel, BorderLayout.WEST);

        // Energia
        JProgressBar barraEnergia = new JProgressBar(0, 100);
        barraEnergia.setValue(jogador.getEnergia());
        barraEnergia.setStringPainted(true);
        barraEnergia.setString(jogador.getEnergia() + "%");
        barraEnergia.setPreferredSize(new Dimension(100, 20));
        int energia = jogador.getEnergia();
        barraEnergia.setForeground(energia >= 75 ? ENERGY_GREEN : energia >= 50 ? ENERGY_YELLOW : ENERGY_RED);
        barraEnergia.setBackground(DARK_BG);

        card.add(barraEnergia, BorderLayout.EAST);

        return card;
    }

    private JPanel criarPainelLog() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(PANEL_BG);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        painel.setPreferredSize(new Dimension(280, 0));

        JLabel lblLog = new JLabel("📝 Relatório de Treino");
        lblLog.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLog.setForeground(ACCENT_GREEN);
        painel.add(lblLog, BorderLayout.NORTH);

        logTreino = new JTextArea();
        logTreino.setEditable(false);
        logTreino.setBackground(DARK_BG);
        logTreino.setForeground(TEXT_WHITE);
        logTreino.setFont(new Font("Consolas", Font.PLAIN, 11));
        logTreino.setText("Aguardando sessão de treino...\n");

        JScrollPane scroll = new JScrollPane(logTreino);
        scroll.setBorder(null);
        painel.add(scroll, BorderLayout.CENTER);

        return painel;
    }

    private void treinarTodosJogadores() {
        logTreino.setText("");
        logTreino.append("═══ SESSÃO DE TREINO ═══\n");
        logTreino.append("Tipo: " + tipoTreinoSelecionado.getNome() + "\n");
        logTreino.append("Intensidade: " + intensidadeSelecionada.getNome() + "\n\n");

        int treinados = 0;
        int lesionados = 0;

        for (Player p : time.getJogadores()) {
            if (p.getEnergia() < 20) {
                logTreino.append("⚠️ " + p.getNome() + " muito cansado!\n");
                continue;
            }

            // Treina o jogador (simplificado - sem ClubFacilities)
            double resultado = treinarJogadorSimplificado(p);

            if (resultado < 0) {
                logTreino.append("🚑 " + p.getNome() + " LESIONADO!\n");
                lesionados++;
            } else {
                logTreino.append("✓ " + p.getNome() + ": +" + String.format("%.1f", resultado) + " XP\n");
                treinados++;
            }
        }

        logTreino.append("\n─────────────────\n");
        logTreino.append("Treinados: " + treinados + "\n");
        if (lesionados > 0) {
            logTreino.append("⚠️ Lesões: " + lesionados + "\n");
        }

        // Atualiza lista de jogadores
        painelJogadores.removeAll();
        JLabel lblTitulo = new JLabel("👥 Jogadores");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(ACCENT_GREEN);
        painelJogadores.add(lblTitulo);
        painelJogadores.add(Box.createVerticalStrut(10));
        for (Player p : time.getJogadores()) {
            painelJogadores.add(criarCardJogador(p));
            painelJogadores.add(Box.createVerticalStrut(5));
        }
        painelJogadores.revalidate();
        painelJogadores.repaint();
    }

    private double treinarJogadorSimplificado(Player jogador) {
        // Versão simplificada do treino
        double xpBase = 10;
        xpBase *= intensidadeSelecionada.getFatorEvolucao();

        // Fator idade
        int idade = jogador.getIdade();
        if (idade <= 21)
            xpBase *= 1.3;
        else if (idade >= 30)
            xpBase *= 0.7;

        // Gasta energia
        int fadigaBase = (int) (10 * intensidadeSelecionada.getFatorFadiga());
        jogador.gastarEnergia(fadigaBase);

        // Risco de lesão em treino duplo
        if (intensidadeSelecionada == IntensidadeTreino.DUPLO) {
            if (Math.random() < 0.03) {
                jogador.setContundido(true);
                return -1;
            }
        }

        // Melhora força levemente
        if (Math.random() < 0.05 * intensidadeSelecionada.getFatorEvolucao()) {
            jogador.setForca(jogador.getForca() + 1);
        }

        return xpBase;
    }

    private void sessaoRecuperacao() {
        logTreino.setText("");
        logTreino.append("═══ SESSÃO DE RECUPERAÇÃO ═══\n\n");

        for (Player p : time.getJogadores()) {
            int antes = p.getEnergia();
            p.recuperarEnergia(20);
            int depois = p.getEnergia();
            logTreino.append("💤 " + p.getNome() + ": " + antes + "% → " + depois + "%\n");
        }

        // Atualiza lista
        painelJogadores.removeAll();
        JLabel lblTitulo = new JLabel("👥 Jogadores");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(ACCENT_GREEN);
        painelJogadores.add(lblTitulo);
        painelJogadores.add(Box.createVerticalStrut(10));
        for (Player p : time.getJogadores()) {
            painelJogadores.add(criarCardJogador(p));
            painelJogadores.add(Box.createVerticalStrut(5));
        }
        painelJogadores.revalidate();
        painelJogadores.repaint();
    }
}
