package com.brasfm.ui.panels;

import com.brasfm.model.Player;
import com.brasfm.model.Team;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Painel do Mercado de Transferências.
 */
public class MercadoPanel extends JPanel {

    private static final Color DARK_BG = new Color(30, 30, 30);
    private static final Color PANEL_BG = new Color(40, 44, 52);
    private static final Color ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color TEXT_WHITE = new Color(236, 240, 241);
    private static final Color TEXT_GRAY = new Color(149, 165, 166);

    private Team meuTime;
    private List<Team> todosOsTimes;
    private JTable tabelaJogadores;
    private DefaultTableModel tableModel;
    private JComboBox<String> filtroPos;
    private List<Player> jogadoresDisponiveis;
    private Random random = new Random();

    public MercadoPanel(Team meuTime, List<Team> todosOsTimes) {
        this.meuTime = meuTime;
        this.todosOsTimes = todosOsTimes;
        this.jogadoresDisponiveis = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBackground(DARK_BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initComponents();
        atualizarListaJogadores();
    }

    private void initComponents() {
        // Título e filtros
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(DARK_BG);

        JLabel titulo = new JLabel("🏪 Mercado de Transferências");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(ACCENT_GREEN);
        topPanel.add(titulo, BorderLayout.WEST);

        // Filtros
        JPanel filtrosPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filtrosPanel.setBackground(DARK_BG);

        JLabel lblFiltro = new JLabel("Posição:");
        lblFiltro.setForeground(TEXT_GRAY);
        filtrosPanel.add(lblFiltro);

        String[] posicoes = { "Todas", "GOL", "ZAG", "LD", "LE", "VOL", "MC", "MEI", "PE", "PD", "CA" };
        filtroPos = new JComboBox<>(posicoes);
        filtroPos.addActionListener(e -> filtrarJogadores());
        filtrosPanel.add(filtroPos);

        JButton btnAtualizar = new JButton("🔄 Atualizar");
        btnAtualizar.setBackground(PANEL_BG);
        btnAtualizar.setForeground(TEXT_WHITE);
        btnAtualizar.addActionListener(e -> atualizarListaJogadores());
        filtrosPanel.add(btnAtualizar);

        topPanel.add(filtrosPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Tabela de jogadores
        String[] colunas = { "Status", "Jogador", "Pos", "Força", "Idade", "Valor", "Time", "Motivo" };
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabelaJogadores = new JTable(tableModel);
        tabelaJogadores.setBackground(PANEL_BG);
        tabelaJogadores.setForeground(TEXT_WHITE);
        tabelaJogadores.setGridColor(new Color(60, 60, 60));
        tabelaJogadores.setRowHeight(28);
        tabelaJogadores.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaJogadores.setSelectionBackground(ACCENT_GREEN.darker());
        tabelaJogadores.setSelectionForeground(TEXT_WHITE);

        JTableHeader header = tabelaJogadores.getTableHeader();
        header.setBackground(new Color(50, 55, 65));
        header.setForeground(ACCENT_GREEN);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Larguras das colunas
        tabelaJogadores.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabelaJogadores.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabelaJogadores.getColumnModel().getColumn(2).setPreferredWidth(50);
        tabelaJogadores.getColumnModel().getColumn(3).setPreferredWidth(50);
        tabelaJogadores.getColumnModel().getColumn(4).setPreferredWidth(50);
        tabelaJogadores.getColumnModel().getColumn(5).setPreferredWidth(100);
        tabelaJogadores.getColumnModel().getColumn(6).setPreferredWidth(120);
        tabelaJogadores.getColumnModel().getColumn(7).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(tabelaJogadores);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        scrollPane.getViewport().setBackground(PANEL_BG);
        add(scrollPane, BorderLayout.CENTER);

        // Painel de ações
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        actionsPanel.setBackground(DARK_BG);

        JButton btnProposta = new JButton("💰 Fazer Proposta");
        btnProposta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnProposta.setBackground(ACCENT_GREEN);
        btnProposta.setForeground(Color.WHITE);
        btnProposta.setFocusPainted(false);
        btnProposta.addActionListener(e -> fazerProposta());
        actionsPanel.add(btnProposta);

        JButton btnInfo = new JButton("📋 Ver Perfil");
        btnInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnInfo.setBackground(PANEL_BG);
        btnInfo.setForeground(TEXT_WHITE);
        btnInfo.setFocusPainted(false);
        btnInfo.addActionListener(e -> verPerfil());
        actionsPanel.add(btnInfo);

        add(actionsPanel, BorderLayout.SOUTH);
    }

    private void atualizarListaJogadores() {
        jogadoresDisponiveis.clear();
        tableModel.setRowCount(0);

        for (Team time : todosOsTimes) {
            if (time == meuTime)
                continue;

            for (Player p : time.getJogadores()) {
                String motivo = null;
                String status = "";

                // Verifica disponibilidade
                if (p.getMoral() < 50) {
                    motivo = "Insatisfeito";
                    status = "🔴";
                } else if (p.getSemanasContrato() < 26) {
                    motivo = "Fim de contrato";
                    status = "🟡";
                } else if (random.nextInt(100) < 15) {
                    motivo = "À venda";
                    status = "🟢";
                }

                if (motivo != null) {
                    jogadoresDisponiveis.add(p);
                    Object[] row = {
                            status,
                            p.getNome(),
                            p.getPosicao().getSigla(),
                            p.getForca(),
                            p.getIdade(),
                            formatValor(calcularValorMercado(p)),
                            time.getSigla(),
                            motivo
                    };
                    tableModel.addRow(row);
                }
            }
        }
    }

    private void filtrarJogadores() {
        String filtro = (String) filtroPos.getSelectedItem();
        tableModel.setRowCount(0);

        for (int i = 0; i < jogadoresDisponiveis.size(); i++) {
            Player p = jogadoresDisponiveis.get(i);

            if (filtro.equals("Todas") || p.getPosicao().getSigla().equals(filtro)) {
                String status = p.getMoral() < 50 ? "🔴" : (p.getSemanasContrato() < 26 ? "🟡" : "🟢");
                String motivo = p.getMoral() < 50 ? "Insatisfeito"
                        : (p.getSemanasContrato() < 26 ? "Fim de contrato" : "À venda");

                Team timeJogador = encontrarTime(p);
                Object[] row = {
                        status,
                        p.getNome(),
                        p.getPosicao().getSigla(),
                        p.getForca(),
                        p.getIdade(),
                        formatValor(calcularValorMercado(p)),
                        timeJogador != null ? timeJogador.getSigla() : "?",
                        motivo
                };
                tableModel.addRow(row);
            }
        }
    }

    private void fazerProposta() {
        int row = tabelaJogadores.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um jogador primeiro!",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Player jogador = jogadoresDisponiveis.get(row);
        Team timeVendedor = encontrarTime(jogador);

        if (timeVendedor == null)
            return;

        double valorMercado = calcularValorMercado(jogador);

        // Diálogo de proposta
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBackground(PANEL_BG);

        JLabel lblJogador = new JLabel(jogador.getNome());
        lblJogador.setForeground(ACCENT_GREEN);
        lblJogador.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel lblValorRef = new JLabel(formatValor(valorMercado));
        lblValorRef.setForeground(TEXT_WHITE);

        JTextField txtOferta = new JTextField(formatValor(valorMercado * 0.9));

        panel.add(new JLabel("Jogador:"));
        panel.add(lblJogador);
        panel.add(new JLabel("Valor de mercado:"));
        panel.add(lblValorRef);
        panel.add(new JLabel("Sua oferta (R$):"));
        panel.add(txtOferta);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "💰 Fazer Proposta - " + timeVendedor.getNome(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String valorStr = txtOferta.getText().replaceAll("[^0-9]", "");
                double oferta = Double.parseDouble(valorStr);

                processarProposta(jogador, timeVendedor, oferta, valorMercado);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Valor inválido!",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void processarProposta(Player jogador, Team timeVendedor, double oferta, double valorMercado) {
        double percentual = oferta / valorMercado;
        int chanceAceite;

        // Calcula chance de aceite baseado na oferta
        if (percentual >= 1.2) {
            chanceAceite = 95;
        } else if (percentual >= 1.0) {
            chanceAceite = 80;
        } else if (percentual >= 0.85) {
            chanceAceite = 50;
        } else if (percentual >= 0.7) {
            chanceAceite = 25;
        } else {
            chanceAceite = 5;
        }

        // Bônus se jogador insatisfeito
        if (jogador.getMoral() < 50) {
            chanceAceite += 20;
        }

        // Bônus se fim de contrato
        if (jogador.getSemanasContrato() < 26) {
            chanceAceite += 15;
        }

        chanceAceite = Math.min(95, chanceAceite);

        boolean aceito = random.nextInt(100) < chanceAceite;

        if (aceito) {
            // Transferência aceita!
            timeVendedor.getJogadores().remove(jogador);
            meuTime.getJogadores().add(jogador);
            jogador.setMoral(80); // Jogador feliz com novo clube

            JOptionPane.showMessageDialog(this,
                    "✅ Proposta ACEITA!\n\n" +
                            jogador.getNome() + " agora faz parte do seu elenco!\n\n" +
                            "Valor: " + formatValor(oferta),
                    "Transferência Concluída", JOptionPane.INFORMATION_MESSAGE);

            atualizarListaJogadores();
        } else {
            // Proposta recusada
            double contraproposta = valorMercado * (1.1 + random.nextDouble() * 0.3);

            JOptionPane.showMessageDialog(this,
                    "❌ Proposta RECUSADA!\n\n" +
                            timeVendedor.getNome() + " pede " + formatValor(contraproposta) +
                            "\npelo jogador.",
                    "Proposta Recusada", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void verPerfil() {
        int row = tabelaJogadores.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um jogador primeiro!",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Player p = jogadoresDisponiveis.get(row);
        Team time = encontrarTime(p);

        String info = String.format(
                "👤 %s\n\n" +
                        "📍 Posição: %s\n" +
                        "⭐ Força: %d\n" +
                        "🎂 Idade: %d anos\n" +
                        "📋 Contrato: %d semanas\n" +
                        "😊 Moral: %d%%\n" +
                        "💰 Valor: %s\n\n" +
                        "🏟️ Time: %s",
                p.getNome(),
                p.getPosicao().getNome(),
                p.getForca(),
                p.getIdade(),
                p.getSemanasContrato(),
                p.getMoral(),
                formatValor(calcularValorMercado(p)),
                time != null ? time.getNome() : "?");

        JOptionPane.showMessageDialog(this, info,
                "Perfil do Jogador", JOptionPane.INFORMATION_MESSAGE);
    }

    private Team encontrarTime(Player p) {
        for (Team t : todosOsTimes) {
            if (t.getJogadores().contains(p)) {
                return t;
            }
        }
        return null;
    }

    private double calcularValorMercado(Player p) {
        double base = p.getForca() * 100000;

        // Ajusta por idade
        if (p.getIdade() < 23) {
            base *= 1.5; // Jovens valem mais
        } else if (p.getIdade() > 30) {
            base *= 0.6; // Veteranos valem menos
        }

        return base;
    }

    private String formatValor(double valor) {
        if (valor >= 1000000) {
            return String.format("R$ %.1fM", valor / 1000000);
        } else if (valor >= 1000) {
            return String.format("R$ %.0fK", valor / 1000);
        }
        return String.format("R$ %.0f", valor);
    }

    public void atualizar() {
        atualizarListaJogadores();
    }
}
