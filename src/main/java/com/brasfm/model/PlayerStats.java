package com.brasfm.model;

import java.util.*;

/**
 * Estatísticas detalhadas de um jogador para análise de dados (Moneyball).
 * Permite identificar jogadores que performam acima dos atributos.
 */
public class PlayerStats {

    private Player jogador;
    private int temporada;

    // Estatísticas básicas
    private int jogos = 0;
    private int minutosJogados = 0;
    private int gols = 0;
    private int assistencias = 0;
    private int cartoesAmarelos = 0;
    private int cartoesVermelhos = 0;

    // Estatísticas avançadas de ataque
    private double xgTotal = 0; // Expected Goals acumulado
    private int chutes = 0;
    private int chutesNoGol = 0;
    private int grandesChances = 0;
    private int grandesChancesPerdidas = 0;
    private int dribles = 0;
    private int dribesCompletos = 0;

    // Estatísticas avançadas de passe
    private int passesTotal = 0;
    private int passesCertos = 0;
    private int passesProgressivos = 0; // Passes que ganham terreno
    private int passesParaFinalizacao = 0; // Key passes
    private int cruzamentos = 0;
    private int cruzamentosCertos = 0;

    // Estatísticas defensivas
    private int desarmes = 0;
    private int desarmesCertos = 0;
    private int interceptacoes = 0;
    private int bloqueios = 0;
    private int duelos = 0;
    private int duelosGanhos = 0;
    private int duelosAereos = 0;
    private int duelosAereosGanhos = 0;

    // Estatísticas de goleiro
    private int defesas = 0;
    private int defesasDificeis = 0;
    private int golsSofridos = 0;
    private double xgContra = 0; // xG dos chutes enfrentados
    private int penaltisDefendidos = 0;

    // Mapa de calor (zonas do campo 3x3)
    private int[][] mapaCalo = new int[3][3];

    public PlayerStats(Player jogador, int temporada) {
        this.jogador = jogador;
        this.temporada = temporada;
    }

    // ==================== MÉTRICAS POR 90 MINUTOS ====================

    public double getGolsPor90() {
        return minutosJogados > 0 ? (gols * 90.0) / minutosJogados : 0;
    }

    public double getAssistenciasPor90() {
        return minutosJogados > 0 ? (assistencias * 90.0) / minutosJogados : 0;
    }

    public double getxGPor90() {
        return minutosJogados > 0 ? (xgTotal * 90.0) / minutosJogados : 0;
    }

    public double getChutesPor90() {
        return minutosJogados > 0 ? (chutes * 90.0) / minutosJogados : 0;
    }

    public double getDesarmesPor90() {
        return minutosJogados > 0 ? (desarmes * 90.0) / minutosJogados : 0;
    }

    public double getPassesProgressivosPor90() {
        return minutosJogados > 0 ? (passesProgressivos * 90.0) / minutosJogados : 0;
    }

    public double getInterceptacoesPor90() {
        return minutosJogados > 0 ? (interceptacoes * 90.0) / minutosJogados : 0;
    }

    // ==================== MÉTRICAS DE EFICIÊNCIA ====================

    /**
     * Gols - xG: Positivo = finaliza acima do esperado.
     */
    public double getGolsMenosXG() {
        return gols - xgTotal;
    }

    /**
     * Porcentagem de conversão de chutes.
     */
    public double getTaxaConversao() {
        return chutes > 0 ? (double) gols / chutes * 100 : 0;
    }

    /**
     * Porcentagem de precisão de passes.
     */
    public double getPrecisaoPasse() {
        return passesTotal > 0 ? (double) passesCertos / passesTotal * 100 : 0;
    }

    /**
     * Porcentagem de desarmes bem-sucedidos.
     */
    public double getTaxaDesarme() {
        return desarmes > 0 ? (double) desarmesCertos / desarmes * 100 : 0;
    }

    /**
     * Porcentagem de duelos ganhos.
     */
    public double getTaxaDuelos() {
        return duelos > 0 ? (double) duelosGanhos / duelos * 100 : 0;
    }

    /**
     * Para goleiros: Gols Sofridos - xG Contra
     * Negativo = defende acima do esperado
     */
    public double getGolsSofridosMenosXG() {
        return golsSofridos - xgContra;
    }

    // ==================== IDENTIFICAÇÃO MONEYBALL ====================

    /**
     * Calcula índice de "overperformance" - jogador que produz acima dos atributos.
     * 
     * @return Score de 0-100, onde >70 indica overperformer
     */
    public int calcularIndiceOverperformance() {
        if (minutosJogados < 450)
            return 50; // Poucos dados

        int forca = jogador.getForca();
        int score = 50;

        // Atacantes/Meias: gols vs xG
        if (jogador.getPosicao().isOfensiva()) {
            double golsMenosXG = getGolsMenosXG();
            if (golsMenosXG > 2)
                score += 15;
            else if (golsMenosXG > 0)
                score += 8;
            else if (golsMenosXG < -2)
                score -= 10;

            // Produzindo mais que atributo sugere?
            double golsEsperados = (forca / 100.0) * (minutosJogados / 90.0) * 0.3;
            if (gols > golsEsperados * 1.3)
                score += 12;
        }

        // Defensores: interceptações e desarmes
        if (jogador.getPosicao().isDefensiva()) {
            if (getTaxaDesarme() > 70)
                score += 10;
            if (getInterceptacoesPor90() > 2)
                score += 10;
        }

        // Meio-campistas: passes progressivos
        if (!jogador.getPosicao().isDefensiva() && !jogador.getPosicao().isOfensiva()) {
            if (getPassesProgressivosPor90() > 5)
                score += 10;
            if (getPrecisaoPasse() > 85)
                score += 8;
        }

        // Consistência (menos variação = mais confiável)
        if (jogos > 5) {
            double mediaNotas = jogador.getMediaNotas();
            if (mediaNotas > 7)
                score += 10;
        }

        return Math.max(0, Math.min(100, score));
    }

    /**
     * Gera resumo para análise de dados.
     */
    public String gerarResumoAnalitico() {
        StringBuilder sb = new StringBuilder();

        sb.append("══════ ANÁLISE DE DADOS ══════\n\n");
        sb.append("Jogador: ").append(jogador.getNome()).append("\n");
        sb.append("Minutos: ").append(minutosJogados).append(" (").append(jogos).append(" jogos)\n\n");

        sb.append("─── Produção Ofensiva ───\n");
        sb.append(String.format("Gols: %d (%.2f por 90)\n", gols, getGolsPor90()));
        sb.append(String.format("xG: %.2f (%.2f por 90)\n", xgTotal, getxGPor90()));
        sb.append(String.format("G-xG: %+.2f %s\n", getGolsMenosXG(),
                getGolsMenosXG() > 0 ? "⬆️" : getGolsMenosXG() < 0 ? "⬇️" : ""));
        sb.append(String.format("Conversão: %.1f%%\n\n", getTaxaConversao()));

        sb.append("─── Criação ───\n");
        sb.append(String.format("Assistências: %d (%.2f por 90)\n", assistencias, getAssistenciasPor90()));
        sb.append(String.format("Passes Prog.: %.1f por 90\n", getPassesProgressivosPor90()));
        sb.append(String.format("Precisão: %.1f%%\n\n", getPrecisaoPasse()));

        sb.append("─── Defesa ───\n");
        sb.append(String.format("Desarmes: %.1f por 90 (%.0f%% sucesso)\n",
                getDesarmesPor90(), getTaxaDesarme()));
        sb.append(String.format("Duelos: %.0f%% ganhos\n\n", getTaxaDuelos()));

        int indice = calcularIndiceOverperformance();
        sb.append("═══════════════════════════\n");
        sb.append(String.format("ÍNDICE ANALÍTICO: %d/100\n", indice));
        if (indice >= 70) {
            sb.append("📈 OVERPERFORMER - Produz acima do esperado!\n");
        } else if (indice <= 35) {
            sb.append("📉 UNDERPERFORMER - Produz abaixo do esperado\n");
        }

        return sb.toString();
    }

    // ==================== REGISTRO DE EVENTOS ====================

    public void registrarGol(double xgDoChute) {
        gols++;
        chutes++;
        chutesNoGol++;
        xgTotal += xgDoChute;
    }

    public void registrarChute(double xg, boolean noGol) {
        chutes++;
        xgTotal += xg;
        if (noGol)
            chutesNoGol++;
    }

    public void registrarAssistencia() {
        assistencias++;
    }

    public void registrarPasse(boolean certo, boolean progressivo) {
        passesTotal++;
        if (certo)
            passesCertos++;
        if (progressivo)
            passesProgressivos++;
    }

    public void registrarDesarme(boolean sucesso) {
        desarmes++;
        if (sucesso)
            desarmesCertos++;
    }

    public void registrarDuelo(boolean ganho, boolean aereo) {
        duelos++;
        if (ganho)
            duelosGanhos++;
        if (aereo) {
            duelosAereos++;
            if (ganho)
                duelosAereosGanhos++;
        }
    }

    public void registrarMinutos(int min) {
        minutosJogados += min;
        jogos++;
    }

    // Getters básicos
    public Player getJogador() {
        return jogador;
    }

    public int getTemporada() {
        return temporada;
    }

    public int getJogos() {
        return jogos;
    }

    public int getMinutosJogados() {
        return minutosJogados;
    }

    public int getGols() {
        return gols;
    }

    public int getAssistencias() {
        return assistencias;
    }

    public double getXgTotal() {
        return xgTotal;
    }
}
