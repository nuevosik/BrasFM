package com.brasfm.ui.panels;

import javax.swing.*;
import java.awt.*;
import com.brasfm.model.Team;

/**
 * Painel de Finanças do time.
 */
public class FinancasPanel extends JPanel {

    private static final Color DARK_BG = new Color(30, 30, 30);
    private static final Color PANEL_BG = new Color(40, 44, 52);
    private static final Color ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color TEXT_WHITE = new Color(236, 240, 241);
    private static final Color TEXT_GRAY = new Color(149, 165, 166);
    private static final Color ENERGY_RED = new Color(231, 76, 60);

    private Team time;

    public FinancasPanel(Team time) {
        this.time = time;
        setLayout(new BorderLayout());
        setBackground(DARK_BG);
        initComponents();
    }

    public void setTime(Team time) {
        this.time = time;
        initComponents();
    }

    private void initComponents() {
        removeAll();

        JPanel painelFinancas = new JPanel();
        painelFinancas.setLayout(new BoxLayout(painelFinancas, BoxLayout.Y_AXIS));
        painelFinancas.setBackground(DARK_BG);
        painelFinancas.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JLabel titulo = new JLabel("💰 Finanças - " + time.getNome());
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(ACCENT_GREEN);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelFinancas.add(titulo);
        painelFinancas.add(Box.createVerticalStrut(30));

        // Painel com informações financeiras
        JPanel cardFinancas = new JPanel();
        cardFinancas.setLayout(new BoxLayout(cardFinancas, BoxLayout.Y_AXIS));
        cardFinancas.setBackground(PANEL_BG);
        cardFinancas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_GREEN, 1),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)));
        cardFinancas.setMaximumSize(new Dimension(600, 400));
        cardFinancas.setAlignmentX(Component.CENTER_ALIGNMENT);

        long saldo = time.getSaldo();
        int folhaSalarial = calcularFolhaSalarial();
        int patrocinio = time.getPatrocinioAnual();
        int rendaEstimada = time.getEstadio() != null ? time.getEstadio().getCapacidade() * 30 : 500000;

        addLinhaFinanca(cardFinancas, "💵 Saldo Atual", String.format("R$ %,d", saldo));
        addLinhaFinanca(cardFinancas, "📋 Folha Salarial Mensal", String.format("R$ %,d", folhaSalarial));
        addLinhaFinanca(cardFinancas, "🤝 Patrocínio Anual", String.format("R$ %,d", patrocinio));
        addLinhaFinanca(cardFinancas, "🎟️ Renda Estimada/Jogo", String.format("R$ %,d", rendaEstimada));
        cardFinancas.add(Box.createVerticalStrut(20));

        long balancoMensal = (patrocinio / 12 + rendaEstimada * 2) - folhaSalarial;
        JLabel lblBalanco = new JLabel(String.format("📊 Balanço Mensal Estimado: R$ %,d", balancoMensal));
        lblBalanco.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblBalanco.setForeground(balancoMensal >= 0 ? ACCENT_GREEN : ENERGY_RED);
        lblBalanco.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardFinancas.add(lblBalanco);

        painelFinancas.add(cardFinancas);

        add(new JScrollPane(painelFinancas), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void addLinhaFinanca(JPanel painel, String label, String valor) {
        JPanel linha = new JPanel(new BorderLayout());
        linha.setOpaque(false);
        linha.setMaximumSize(new Dimension(500, 35));
        linha.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT_GRAY);
        linha.add(lbl, BorderLayout.WEST);

        JLabel val = new JLabel(valor);
        val.setFont(new Font("Segoe UI", Font.BOLD, 14));
        val.setForeground(TEXT_WHITE);
        linha.add(val, BorderLayout.EAST);

        painel.add(linha);
        painel.add(Box.createVerticalStrut(10));
    }

    private int calcularFolhaSalarial() {
        int total = 0;
        for (var p : time.getJogadores()) {
            total += p.getSalario();
        }
        return total;
    }
}
