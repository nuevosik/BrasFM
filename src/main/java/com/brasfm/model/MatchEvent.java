package com.brasfm.model;

/**
 * Representa um evento durante uma partida.
 */
public class MatchEvent {

    public enum TipoEvento {
        GOL("⚽ Gol"),
        GOL_CONTRA("⚽ Gol Contra"),
        CARTAO_AMARELO("🟨 Cartão Amarelo"),
        CARTAO_VERMELHO("🟥 Cartão Vermelho"),
        SUBSTITUICAO("🔄 Substituição"),
        FALTA("Falta"),
        ESCANTEIO("Escanteio"),
        DEFESA_DIFICIL("Defesa Difícil"),
        CHUTE_PARA_FORA("Chute para Fora"),
        IMPEDIMENTO("Impedimento"),
        PENALTI("Pênalti"),
        PENALTI_DEFENDIDO("Pênalti Defendido"),
        INICIO_PRIMEIRO_TEMPO("Início 1º Tempo"),
        FIM_PRIMEIRO_TEMPO("Fim 1º Tempo"),
        INICIO_SEGUNDO_TEMPO("Início 2º Tempo"),
        FIM_JOGO("Fim de Jogo");

        private final String descricao;

        TipoEvento(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    private int minuto;
    private TipoEvento tipo;
    private Team time;
    private Player jogadorPrincipal;
    private Player jogadorSecundario; // assistência ou substituído
    private String descricao;

    public MatchEvent(int minuto, TipoEvento tipo, Team time) {
        this.minuto = minuto;
        this.tipo = tipo;
        this.time = time;
    }

    public MatchEvent(int minuto, TipoEvento tipo, Team time, Player jogador) {
        this(minuto, tipo, time);
        this.jogadorPrincipal = jogador;
    }

    public MatchEvent(int minuto, TipoEvento tipo, Team time, Player jogador, Player assistente) {
        this(minuto, tipo, time, jogador);
        this.jogadorSecundario = assistente;
    }

    public String getTextoCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append(minuto).append("' - ").append(tipo.getDescricao());

        if (time != null) {
            sb.append(" (").append(time.getSigla()).append(")");
        }

        if (jogadorPrincipal != null) {
            sb.append(" - ").append(jogadorPrincipal.getNome());

            if (jogadorSecundario != null) {
                if (tipo == TipoEvento.GOL) {
                    sb.append(" (Assist: ").append(jogadorSecundario.getNome()).append(")");
                } else if (tipo == TipoEvento.SUBSTITUICAO) {
                    sb.append(" entra, sai ").append(jogadorSecundario.getNome());
                }
            }
        }

        if (descricao != null && !descricao.isEmpty()) {
            sb.append(" - ").append(descricao);
        }

        return sb.toString();
    }

    // Getters
    public int getMinuto() {
        return minuto;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public Team getTime() {
        return time;
    }

    public Player getJogadorPrincipal() {
        return jogadorPrincipal;
    }

    public Player getJogadorSecundario() {
        return jogadorSecundario;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return getTextoCompleto();
    }
}
