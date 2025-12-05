package com.brasfm;

import com.brasfm.ui.MainWindow;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;

/**
 * Ponto de entrada do BrasFM.
 * O jogo para quem entende de futebol.
 */
public class Main {

    public static void main(String[] args) {
        // Configura FlatLaf Dark para interface moderna
        try {
            FlatDarkLaf.setup();

            // Configurações adicionais para melhor aparência
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);

        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível configurar o FlatLaf: " + e.getMessage());
        }

        // Inicia a interface gráfica na thread do Swing
        SwingUtilities.invokeLater(() -> {
            System.out.println("╔══════════════════════════════════════╗");
            System.out.println("║           BrasFM v1.0.0              ║");
            System.out.println("║  O jogo para quem entende de futebol ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.println();
            System.out.println("Iniciando o jogo...");

            MainWindow window = new MainWindow();
            window.setVisible(true);

            System.out.println("Jogo iniciado com sucesso!");
        });
    }
}
