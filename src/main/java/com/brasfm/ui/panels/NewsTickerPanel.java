package com.brasfm.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * News Ticker - Barra de notícias passantes na tela principal.
 */
public class NewsTickerPanel extends JPanel {

    private static final Color TICKER_BG = new Color(20, 26, 36);
    private static final Color TICKER_TEXT = new Color(236, 240, 241);
    private static final Color HIGHLIGHT = new Color(46, 204, 113);

    private List<String> noticias;
    private int noticiaAtual = 0;
    private int offsetX = 0;
    private Timer timerScroll;
    private Timer timerTroca;
    private JLabel lblNoticia;
    private Random random = new Random();

    // Notícias pré-definidas
    private static final String[] NOTICIAS_PADRAO = {
            "🔴 Flamengo anuncia contratação de destaque sul-americano",
            "🟢 Palmeiras renova contrato com jogador até 2027",
            "⚽ Brasileirão: próxima rodada terá clássico regional",
            "📊 Artilheiro lidera goleadores com 15 gols na temporada",
            "🏥 Atacante sofre lesão muscular e desfalca time por 3 semanas",
            "💰 Mercado: clube europeu faz proposta milionária",
            "🏆 Copa do Brasil: sorteio define confrontos das oitavas",
            "📰 Técnico faz mudanças no time para jogo decisivo",
            "⚡ Revelação da base impressiona em treino",
            "🎯 Goleiro bate recorde de defesas na temporada",
            "📈 Time sobe na tabela após sequência de vitórias",
            "❌ Cartão vermelho: jogador suspenso por 2 partidas",
            "🔄 Janela de transferências fecha em 7 dias",
            "👔 Presidente do clube faz pronunciamento sobre finanças",
            "🏟️ Estádio terá público recorde no próximo jogo"
    };

    public NewsTickerPanel() {
        setLayout(new BorderLayout());
        setBackground(TICKER_BG);
        setPreferredSize(new Dimension(0, 28));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 60)));

        noticias = new ArrayList<>();
        for (String n : NOTICIAS_PADRAO) {
            noticias.add(n);
        }

        initComponents();
        iniciarAnimacao();
    }

    private void initComponents() {
        // Ícone de notícias
        JLabel lblIcon = new JLabel(" 📰 ");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        lblIcon.setForeground(HIGHLIGHT);
        lblIcon.setOpaque(true);
        lblIcon.setBackground(TICKER_BG);
        add(lblIcon, BorderLayout.WEST);

        // Área de notícias
        JPanel areaNoticia = new JPanel(new BorderLayout());
        areaNoticia.setBackground(TICKER_BG);

        lblNoticia = new JLabel(noticias.get(0));
        lblNoticia.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblNoticia.setForeground(TICKER_TEXT);
        areaNoticia.add(lblNoticia, BorderLayout.CENTER);

        add(areaNoticia, BorderLayout.CENTER);

        // Hora
        JLabel lblHora = new JLabel("  🕐 " + java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + " ");
        lblHora.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblHora.setForeground(new Color(149, 165, 166));
        add(lblHora, BorderLayout.EAST);
    }

    private void iniciarAnimacao() {
        // Timer para trocar notícias a cada 5 segundos
        timerTroca = new Timer(5000, e -> {
            noticiaAtual = (noticiaAtual + 1) % noticias.size();

            // Efeito de fade (simples com blink)
            lblNoticia.setForeground(HIGHLIGHT);
            lblNoticia.setText(noticias.get(noticiaAtual));

            Timer resetColor = new Timer(300, ev -> {
                lblNoticia.setForeground(TICKER_TEXT);
            });
            resetColor.setRepeats(false);
            resetColor.start();
        });
        timerTroca.start();
    }

    /**
     * Adiciona uma nova notícia ao ticker.
     */
    public void addNoticia(String noticia) {
        noticias.add(0, noticia);
        // Mantém máximo de 20 notícias
        if (noticias.size() > 20) {
            noticias.remove(noticias.size() - 1);
        }
    }

    /**
     * Gera uma notícia aleatória de transferência.
     */
    public void gerarNoticiaTransferencia(String jogador, String timeOrigem, String timeDestino, double valor) {
        String noticia = String.format("💰 TRANSFERÊNCIA: %s deixa %s e acerta com %s por R$ %.1fM",
                jogador, timeOrigem, timeDestino, valor / 1000000);
        addNoticia(noticia);
    }

    /**
     * Gera uma notícia de resultado de jogo.
     */
    public void gerarNoticiaResultado(String timeCasa, int golsCasa, String timeFora, int golsFora) {
        String noticia = String.format("⚽ RESULTADO: %s %d x %d %s",
                timeCasa, golsCasa, golsFora, timeFora);
        addNoticia(noticia);
    }

    /**
     * Gera uma notícia de lesão.
     */
    public void gerarNoticiaLesao(String jogador, String time, int semanas) {
        String noticia = String.format("🏥 LESÃO: %s (%s) fora por %d semanas",
                jogador, time, semanas);
        addNoticia(noticia);
    }

    /**
     * Para a animação.
     */
    public void parar() {
        if (timerTroca != null)
            timerTroca.stop();
    }

    /**
     * Reinicia a animação.
     */
    public void reiniciar() {
        if (timerTroca != null)
            timerTroca.start();
    }
}
