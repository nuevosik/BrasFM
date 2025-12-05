package com.brasfm.ui.panels;

import javax.swing.*;
import java.awt.*;
import com.brasfm.model.Team;
import com.brasfm.model.Match;
import com.brasfm.championship.League;

/**
 * Painel com informações e ações do próximo jogo.
 */
public class ProximoJogoPanel extends JPanel {

    private static final Color DARK_BG = new Color(30, 30, 30);
    private static final Color PANEL_BG = new Color(40, 44, 52);
    private static final Color ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color TEXT_WHITE = new Color(236, 240, 241);
    private static final Color TEXT_GRAY = new Color(149, 165, 166);

    private Team time;
    private League campeonato;
    private Runnable onJogarAction;

    public ProximoJogoPanel(Team time, League campeonato, Runnable onJogarAction) {
        this.time = time;
        this.campeonato = campeonato;
        this.onJogarAction = onJogarAction;
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        initComponents();
    }

    public void atualizar() {
        initComponents();
    }

    private void initComponents() {
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel("⚽ Próximo Jogo");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(ACCENT_GREEN);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblTitulo);
        add(Box.createVerticalStrut(15));

        int rodada = campeonato.getRodadaAtual() + 1;
        if (rodada <= campeonato.getTotalRodadas()) {
            Match proximoJogo = null;
            for (Match m : campeonato.getJogosRodada(rodada)) {
                if (m.getMandante() == time || m.getVisitante() == time) {
                    proximoJogo = m;
                    break;
                }
            }

            if (proximoJogo != null) {
                // Placar visual
                JPanel placar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
                placar.setOpaque(false);
                placar.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel lblCasa = new JLabel(proximoJogo.getMandante().getSigla());
                lblCasa.setFont(new Font("Segoe UI", Font.BOLD, 18));
                lblCasa.setForeground(proximoJogo.getMandante() == time ? ACCENT_GREEN : TEXT_WHITE);

                JLabel lblVs = new JLabel(" vs ");
                lblVs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                lblVs.setForeground(TEXT_GRAY);

                JLabel lblFora = new JLabel(proximoJogo.getVisitante().getSigla());
                lblFora.setFont(new Font("Segoe UI", Font.BOLD, 18));
                lblFora.setForeground(proximoJogo.getVisitante() == time ? ACCENT_GREEN : TEXT_WHITE);

                placar.add(lblCasa);
                placar.add(lblVs);
                placar.add(lblFora);
                add(placar);
                add(Box.createVerticalStrut(10));

                // Info
                JLabel lblRodada = new JLabel("📅 Rodada " + rodada);
                lblRodada.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblRodada.setForeground(TEXT_GRAY);
                lblRodada.setAlignmentX(Component.LEFT_ALIGNMENT);
                add(lblRodada);

                String local = proximoJogo.getMandante() == time ? "🏠 Em Casa" : "✈️ Fora";
                JLabel lblLocal = new JLabel(local);
                lblLocal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblLocal.setForeground(TEXT_GRAY);
                lblLocal.setAlignmentX(Component.LEFT_ALIGNMENT);
                add(lblLocal);
            }
        } else {
            JLabel lblFim = new JLabel("🏆 Campeonato Encerrado!");
            lblFim.setForeground(ACCENT_GREEN);
            lblFim.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(lblFim);
        }

        add(Box.createVerticalStrut(15));

        // Botão Jogar
        JButton btnJogar = new JButton("⚽ Jogar Partida");
        btnJogar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnJogar.setBackground(ACCENT_GREEN);
        btnJogar.setForeground(Color.WHITE);
        btnJogar.setFocusPainted(false);
        btnJogar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnJogar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnJogar.setMaximumSize(new Dimension(200, 40));
        btnJogar.addActionListener(e -> {
            if (onJogarAction != null) {
                onJogarAction.run();
            }
        });
        add(btnJogar);

        revalidate();
        repaint();
    }
}
