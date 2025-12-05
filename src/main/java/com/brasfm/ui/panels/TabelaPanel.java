package com.brasfm.ui.panels;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import com.brasfm.model.Team;
import com.brasfm.championship.League;

/**
 * Painel da Tabela de Classificação do Campeonato.
 */
public class TabelaPanel extends JPanel {

    private static final Color DARK_BG = new Color(30, 30, 30);
    private static final Color PANEL_BG = new Color(40, 44, 52);
    private static final Color ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color TEXT_WHITE = new Color(236, 240, 241);

    private League campeonato;

    public TabelaPanel(League campeonato) {
        this.campeonato = campeonato;
        setLayout(new BorderLayout());
        setBackground(DARK_BG);
        initComponents();
    }

    public void setCampeonato(League campeonato) {
        this.campeonato = campeonato;
        initComponents();
    }

    public void atualizar() {
        initComponents();
    }

    private void initComponents() {
        removeAll();

        JPanel painelTabela = new JPanel(new BorderLayout(0, 20));
        painelTabela.setBackground(DARK_BG);
        painelTabela.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titulo = new JLabel("🏆 " + campeonato.getNome());
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(ACCENT_GREEN);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        painelTabela.add(titulo, BorderLayout.NORTH);

        // Cria tabela de classificação
        String[] colunas = { "Pos", "Time", "P", "J", "V", "E", "D", "GP", "GC", "SG" };
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

        JTable tabela = new JTable(dados, colunas);
        tabela.setBackground(PANEL_BG);
        tabela.setForeground(TEXT_WHITE);
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabela.setRowHeight(28);
        tabela.getTableHeader().setBackground(new Color(40, 44, 52));
        tabela.getTableHeader().setForeground(ACCENT_GREEN);
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabela.setSelectionBackground(ACCENT_GREEN.darker());
        tabela.setGridColor(new Color(60, 60, 60));

        // Centralizar colunas numéricas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tabela.getColumnCount(); i++) {
            if (i != 1) { // Exceto coluna do nome
                tabela.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // Ajustar larguras
        tabela.getColumnModel().getColumn(0).setPreferredWidth(40); // Pos
        tabela.getColumnModel().getColumn(1).setPreferredWidth(180); // Time
        tabela.getColumnModel().getColumn(2).setPreferredWidth(40); // P

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(DARK_BG);
        painelTabela.add(scroll, BorderLayout.CENTER);

        add(painelTabela, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
