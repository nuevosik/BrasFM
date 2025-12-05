package com.brasfm.ui.panels;

import javax.swing.*;
import java.awt.*;
import com.brasfm.model.Team;
import com.brasfm.ui.CampoTaticoPanel;

/**
 * Painel de Tática com campo visual.
 */
public class TaticaPanel extends JPanel {

    private static final Color DARK_BG = new Color(30, 30, 30);
    private static final Color PANEL_BG = new Color(40, 44, 52);
    private static final Color ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color TEXT_GRAY = new Color(149, 165, 166);

    private Team time;
    private CampoTaticoPanel campoTatico;

    public TaticaPanel(Team time) {
        this.time = time;
        setLayout(new BorderLayout());
        setBackground(DARK_BG);
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

        // Painel principal dividido em duas áreas
        JPanel painelPrincipal = new JPanel(new BorderLayout(20, 0));
        painelPrincipal.setBackground(DARK_BG);
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Campo tático visual (esquerda)
        JPanel painelCampo = new JPanel(new BorderLayout());
        painelCampo.setBackground(DARK_BG);

        JLabel lblCampo = new JLabel("⚽ Formação Tática - " + time.getNome());
        lblCampo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCampo.setForeground(ACCENT_GREEN);
        lblCampo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        painelCampo.add(lblCampo, BorderLayout.NORTH);

        campoTatico = new CampoTaticoPanel(time);
        campoTatico.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 2));
        painelCampo.add(campoTatico, BorderLayout.CENTER);

        painelPrincipal.add(painelCampo, BorderLayout.CENTER);

        // Painel de configurações (direita)
        JPanel painelConfig = new JPanel();
        painelConfig.setLayout(new BoxLayout(painelConfig, BoxLayout.Y_AXIS));
        painelConfig.setBackground(PANEL_BG);
        painelConfig.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        painelConfig.setPreferredSize(new Dimension(300, 0));

        JLabel lblConfig = new JLabel("⚙️ Configurações");
        lblConfig.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblConfig.setForeground(ACCENT_GREEN);
        lblConfig.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelConfig.add(lblConfig);
        painelConfig.add(Box.createVerticalStrut(20));

        var tatica = time.getTatica();

        // Formação
        JPanel cardFormacao = criarCardTatica("📐 Formação", tatica.getFormacao().getNome());
        painelConfig.add(cardFormacao);
        painelConfig.add(Box.createVerticalStrut(10));

        // Estilo de Jogo
        JPanel cardEstilo = criarCardTatica("🎯 Estilo", tatica.getEstiloJogo().getNome());
        painelConfig.add(cardEstilo);
        painelConfig.add(Box.createVerticalStrut(10));

        // Tipo de Marcação
        JPanel cardMarcacao = criarCardTatica("🛡️ Marcação", tatica.getTipoMarcacao().getNome());
        painelConfig.add(cardMarcacao);
        painelConfig.add(Box.createVerticalStrut(20));

        // Legenda
        JLabel lblLegenda = new JLabel("<html><b>Legenda:</b><br>" +
                "🟢 Titulares<br>" +
                "🟡 Reservas<br>" +
                "🟣 Goleiro</html>");
        lblLegenda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLegenda.setForeground(TEXT_GRAY);
        lblLegenda.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelConfig.add(lblLegenda);
        painelConfig.add(Box.createVerticalStrut(15));

        // Instruções
        JLabel lblInstrucoes = new JLabel("<html><b>Instruções:</b><br>" +
                "• Arraste para trocar posição<br>" +
                "• Duplo clique: Titular ↔ Reserva</html>");
        lblInstrucoes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInstrucoes.setForeground(TEXT_GRAY);
        lblInstrucoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelConfig.add(lblInstrucoes);

        painelConfig.add(Box.createVerticalGlue());

        painelPrincipal.add(painelConfig, BorderLayout.EAST);

        add(painelPrincipal, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel criarCardTatica(String label, String valor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(50, 54, 62));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        card.setMaximumSize(new Dimension(260, 50));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_GRAY);
        card.add(lbl, BorderLayout.WEST);

        JLabel val = new JLabel(valor);
        val.setFont(new Font("Segoe UI", Font.BOLD, 13));
        val.setForeground(ACCENT_GREEN);
        card.add(val, BorderLayout.EAST);

        return card;
    }
}
