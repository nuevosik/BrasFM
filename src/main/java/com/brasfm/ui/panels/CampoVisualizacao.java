package com.brasfm.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Visualização 2D do campo durante partidas.
 * Mostra a bola se movendo baseado na ação do jogo.
 */
public class CampoVisualizacao extends JPanel {

    private static final Color CAMPO_VERDE = new Color(34, 139, 34);
    private static final Color LINHA_BRANCA = new Color(255, 255, 255, 200);
    private static final Color BOLA_COR = Color.WHITE;
    private static final Color GOL_COR = new Color(255, 255, 255, 150);

    private double bolaPosX = 0.5; // 0.0 = esquerda, 1.0 = direita
    private double bolaPosY = 0.5; // 0.0 = topo, 1.0 = base
    private boolean timeCasaAtaca = true;
    private int intensidade = 0; // 0-100, usado para efeitos visuais

    public CampoVisualizacao() {
        setPreferredSize(new Dimension(350, 200));
        setBackground(CAMPO_VERDE);
        setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Fundo do campo
        g2d.setColor(CAMPO_VERDE);
        g2d.fillRect(0, 0, w, h);

        // Desenha linhas do campo
        g2d.setColor(LINHA_BRANCA);
        g2d.setStroke(new BasicStroke(2));

        // Linha central
        g2d.drawLine(w / 2, 0, w / 2, h);

        // Círculo central
        int circleRadius = h / 4;
        g2d.drawOval(w / 2 - circleRadius, h / 2 - circleRadius, circleRadius * 2, circleRadius * 2);

        // Áreas (simplificadas)
        int areaWidth = w / 6;
        int areaHeight = h / 2;
        int areaY = h / 4;

        // Área esquerda (gol casa)
        g2d.drawRect(0, areaY, areaWidth, areaHeight);
        g2d.setColor(GOL_COR);
        g2d.fillRect(0, areaY + areaHeight / 4, 8, areaHeight / 2);

        g2d.setColor(LINHA_BRANCA);
        // Área direita (gol fora)
        g2d.drawRect(w - areaWidth, areaY, areaWidth, areaHeight);
        g2d.setColor(GOL_COR);
        g2d.fillRect(w - 8, areaY + areaHeight / 4, 8, areaHeight / 2);

        // Desenha a bola
        int bolaX = (int) (bolaPosX * (w - 20)) + 10;
        int bolaY = (int) (bolaPosY * (h - 20)) + 10;
        int bolaSize = 12;

        // Efeito de "calor" se intensidade alta
        if (intensidade > 50) {
            g2d.setColor(new Color(255, 200, 0, intensidade));
            g2d.fillOval(bolaX - bolaSize, bolaY - bolaSize, bolaSize * 2 + 4, bolaSize * 2 + 4);
        }

        g2d.setColor(BOLA_COR);
        g2d.fillOval(bolaX - bolaSize / 2, bolaY - bolaSize / 2, bolaSize, bolaSize);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(bolaX - bolaSize / 2, bolaY - bolaSize / 2, bolaSize, bolaSize);

        // Indicador de direção de ataque
        g2d.setColor(new Color(255, 255, 255, 100));
        int arrowY = h - 15;
        if (timeCasaAtaca) {
            // Seta para direita
            g2d.drawLine(w / 2 - 20, arrowY, w / 2 + 20, arrowY);
            g2d.drawLine(w / 2 + 10, arrowY - 5, w / 2 + 20, arrowY);
            g2d.drawLine(w / 2 + 10, arrowY + 5, w / 2 + 20, arrowY);
        } else {
            // Seta para esquerda
            g2d.drawLine(w / 2 - 20, arrowY, w / 2 + 20, arrowY);
            g2d.drawLine(w / 2 - 10, arrowY - 5, w / 2 - 20, arrowY);
            g2d.drawLine(w / 2 - 10, arrowY + 5, w / 2 - 20, arrowY);
        }
    }

    /**
     * Move a bola para uma nova posição.
     * 
     * @param x posição X (0.0 a 1.0)
     * @param y posição Y (0.0 a 1.0)
     */
    public void moverBola(double x, double y) {
        this.bolaPosX = Math.max(0, Math.min(1, x));
        this.bolaPosY = Math.max(0, Math.min(1, y));
        repaint();
    }

    /**
     * Define qual time está atacando.
     */
    public void setTimeCasaAtaca(boolean casaAtaca) {
        this.timeCasaAtaca = casaAtaca;
        repaint();
    }

    /**
     * Define intensidade da jogada (para efeitos visuais).
     */
    public void setIntensidade(int intensidade) {
        this.intensidade = Math.max(0, Math.min(100, intensidade));
        repaint();
    }

    /**
     * Simula um evento de jogo e move a bola de acordo.
     */
    public void simularEvento(String tipoEvento, boolean casaAtaca) {
        this.timeCasaAtaca = casaAtaca;

        switch (tipoEvento.toLowerCase()) {
            case "gol":
                bolaPosX = casaAtaca ? 0.95 : 0.05;
                bolaPosY = 0.5;
                intensidade = 100;
                break;
            case "chute":
                bolaPosX = casaAtaca ? 0.85 : 0.15;
                bolaPosY = 0.4 + Math.random() * 0.2;
                intensidade = 70;
                break;
            case "defesa":
                bolaPosX = casaAtaca ? 0.9 : 0.1;
                bolaPosY = 0.5;
                intensidade = 50;
                break;
            case "ataque":
                bolaPosX = casaAtaca ? 0.7 : 0.3;
                bolaPosY = 0.3 + Math.random() * 0.4;
                intensidade = 40;
                break;
            case "meio":
            default:
                bolaPosX = 0.5;
                bolaPosY = 0.5;
                intensidade = 0;
                break;
        }
        repaint();
    }
}
