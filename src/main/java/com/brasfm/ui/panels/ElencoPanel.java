package com.brasfm.ui.panels;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import com.brasfm.model.*;
import com.brasfm.ui.model.ElencoTableModel;

/**
 * Painel de Elenco do time com tabela de jogadores.
 */
public class ElencoPanel extends JPanel {

    private static final Color DARK_BG = new Color(30, 30, 30);
    private static final Color PANEL_BG = new Color(40, 44, 52);
    private static final Color ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color TEXT_WHITE = new Color(236, 240, 241);
    private static final Color TEXT_GRAY = new Color(149, 165, 166);
    private static final Color ENERGY_GREEN = new Color(46, 204, 113);
    private static final Color ENERGY_YELLOW = new Color(241, 196, 15);
    private static final Color ENERGY_RED = new Color(231, 76, 60);

    private Team time;
    private JTable tabelaElenco;
    private ElencoTableModel tableModel;
    private JPanel painelInfoJogador;
    private Player jogadorSelecionado;

    public ElencoPanel(Team time) {
        this.time = time;
        setLayout(new BorderLayout(10, 0));
        setBackground(DARK_BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initComponents();
    }

    public void setTime(Team time) {
        this.time = time;
        initComponents();
    }

    public void atualizar() {
        if (tableModel != null) {
            tableModel.fireTableDataChanged();
        }
    }

    private void initComponents() {
        removeAll();

        // Painel Central - Tabela de Elenco
        JPanel painelCentral = criarPainelCentral();
        add(painelCentral, BorderLayout.CENTER);

        // Painel Direito - Info do Jogador Selecionado
        painelInfoJogador = criarPainelInfoJogador();
        add(painelInfoJogador, BorderLayout.EAST);

        revalidate();
        repaint();
    }

    private JPanel criarPainelCentral() {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(DARK_BG);

        // Título
        JLabel titulo = new JLabel("👥 Elenco - " + time.getNome() + " (" + time.getJogadores().size() + " jogadores)");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(ACCENT_GREEN);
        painel.add(titulo, BorderLayout.NORTH);

        // Tabela
        tableModel = new ElencoTableModel(time.getJogadores());
        tabelaElenco = new JTable(tableModel);
        configurarTabela();

        JScrollPane scroll = new JScrollPane(tabelaElenco);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        scroll.getViewport().setBackground(PANEL_BG);
        painel.add(scroll, BorderLayout.CENTER);

        return painel;
    }

    private JPanel criarPainelInfoJogador() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(PANEL_BG);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        painel.setPreferredSize(new Dimension(250, 0));

        JLabel lblTitulo = new JLabel("📋 Jogador");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(ACCENT_GREEN);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblTitulo);
        painel.add(Box.createVerticalStrut(10));

        JLabel lblInfo = new JLabel("<html><center>Selecione um jogador<br>na tabela</center></html>");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInfo.setForeground(TEXT_GRAY);
        lblInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        painel.add(lblInfo);

        return painel;
    }

    private void configurarTabela() {
        tabelaElenco.setBackground(PANEL_BG);
        tabelaElenco.setForeground(TEXT_WHITE);
        tabelaElenco.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaElenco.setRowHeight(32);
        tabelaElenco.setSelectionBackground(ACCENT_GREEN.darker());
        tabelaElenco.setGridColor(new Color(60, 60, 60));
        tabelaElenco.setShowGrid(true);

        // Header
        JTableHeader header = tabelaElenco.getTableHeader();
        header.setBackground(new Color(25, 28, 35));
        header.setForeground(ACCENT_GREEN);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_GREEN));

        // Renderer para coluna de energia
        tabelaElenco.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JProgressBar bar = new JProgressBar(0, 100);
                int val = (Integer) value;
                bar.setValue(val);
                bar.setStringPainted(true);
                bar.setString(val + "%");
                bar.setBackground(PANEL_BG);
                bar.setForeground(val >= 75 ? ENERGY_GREEN : val >= 50 ? ENERGY_YELLOW : ENERGY_RED);
                return bar;
            }
        });

        // Seleção de jogador
        tabelaElenco.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tabelaElenco.getSelectedRow();
                if (row >= 0) {
                    jogadorSelecionado = time.getJogadores().get(row);
                    atualizarPainelJogador();
                }
            }
        });

        // Menu de contexto
        tabelaElenco.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger())
                    mostrarMenuContexto(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    mostrarMenuContexto(e);
            }
        });
    }

    private void mostrarMenuContexto(MouseEvent e) {
        int row = tabelaElenco.rowAtPoint(e.getPoint());
        if (row >= 0 && row < time.getJogadores().size()) {
            tabelaElenco.setRowSelectionInterval(row, row);
            Player jogador = time.getJogadores().get(row);

            JPopupMenu menu = new JPopupMenu();

            // Titular/Reserva
            if (time.getTitulares().contains(jogador)) {
                JMenuItem itemReserva = new JMenuItem("📤 Enviar para Reserva");
                itemReserva.addActionListener(ev -> {
                    time.getTitulares().remove(jogador);
                    time.getReservas().add(jogador);
                    tableModel.fireTableDataChanged();
                });
                menu.add(itemReserva);
            } else {
                JMenuItem itemTitular = new JMenuItem("📥 Escalar como Titular");
                itemTitular.addActionListener(ev -> {
                    if (time.getTitulares().size() < 11) {
                        time.getReservas().remove(jogador);
                        time.getTitulares().add(jogador);
                        tableModel.fireTableDataChanged();
                    }
                });
                menu.add(itemTitular);
            }

            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private void atualizarPainelJogador() {
        if (jogadorSelecionado == null)
            return;

        painelInfoJogador.removeAll();

        // Nome
        JLabel lblNome = new JLabel(jogadorSelecionado.getNome());
        lblNome.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNome.setForeground(ACCENT_GREEN);
        lblNome.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelInfoJogador.add(lblNome);
        painelInfoJogador.add(Box.createVerticalStrut(5));

        // Posição
        String posNome = jogadorSelecionado.getPosicao() != null ? jogadorSelecionado.getPosicao().getNome() : "N/A";
        JLabel lblPos = new JLabel(posNome);
        lblPos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPos.setForeground(TEXT_GRAY);
        lblPos.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelInfoJogador.add(lblPos);
        painelInfoJogador.add(Box.createVerticalStrut(15));

        // Stats
        addInfoLinha("Idade", jogadorSelecionado.getIdade() + " anos");
        addInfoLinha("Força", String.valueOf(jogadorSelecionado.getForca()));
        addInfoLinha("Energia", jogadorSelecionado.getEnergia() + "%");
        addInfoLinha("Moral", jogadorSelecionado.getMoral() + "%");
        addInfoLinha("Salário", "R$ " + String.format("%,d", jogadorSelecionado.getSalario()));

        painelInfoJogador.add(Box.createVerticalGlue());

        // Status
        String status = time.getTitulares().contains(jogadorSelecionado) ? "⭐ TITULAR" : "📋 RESERVA";
        JLabel lblStatus = new JLabel(status);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStatus.setForeground(time.getTitulares().contains(jogadorSelecionado) ? ACCENT_GREEN : ENERGY_YELLOW);
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelInfoJogador.add(lblStatus);

        painelInfoJogador.revalidate();
        painelInfoJogador.repaint();
    }

    private void addInfoLinha(String label, String valor) {
        JPanel linha = new JPanel(new BorderLayout());
        linha.setOpaque(false);
        linha.setMaximumSize(new Dimension(220, 25));
        linha.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_GRAY);
        linha.add(lbl, BorderLayout.WEST);

        JLabel val = new JLabel(valor);
        val.setFont(new Font("Segoe UI", Font.BOLD, 11));
        val.setForeground(TEXT_WHITE);
        linha.add(val, BorderLayout.EAST);

        painelInfoJogador.add(linha);
        painelInfoJogador.add(Box.createVerticalStrut(5));
    }
}
