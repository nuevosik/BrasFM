package com.brasfm.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma partida de futebol.
 */
public class Match {
    private Team mandante;
    private Team visitante;
    private Stadium estadio;

    // Placar
    private int golsMandante;
    private int golsVisitante;

    // Tempo
    private int minutoAtual;
    private boolean emAndamento;
    private boolean finalizada;
    private boolean intervalo;

    // Eventos
    private List<MatchEvent> eventos;

    // Estatísticas
    private int posseMandante; // porcentagem 0-100
    private int chutesMandante;
    private int chutesVisitante;
    private int faltasMandante;
    private int faltasVisitante;
    private int escanteiosMandante;
    private int escanteiosVisitante;

    // Público
    private int publico;
    private int renda;

    // Competição
    private String competicao;
    private String fase;
    private boolean decisivo; // mata-mata

    public Match(Team mandante, Team visitante) {
        this.mandante = mandante;
        this.visitante = visitante;
        this.estadio = mandante.getEstadio();
        this.eventos = new ArrayList<>();
        this.posseMandante = 50;
    }

    /**
     * Inicia a partida.
     */
    public void iniciar() {
        this.emAndamento = true;
        this.minutoAtual = 0;
        addEvento(new MatchEvent(0, MatchEvent.TipoEvento.INICIO_PRIMEIRO_TEMPO, null));
    }

    /**
     * Adiciona um evento à partida.
     */
    public void addEvento(MatchEvent evento) {
        eventos.add(evento);
    }

    /**
     * Registra um gol.
     */
    public void registrarGol(Team time, Player marcador, Player assistente) {
        if (time == mandante) {
            golsMandante++;
        } else {
            golsVisitante++;
        }

        MatchEvent evento = new MatchEvent(minutoAtual, MatchEvent.TipoEvento.GOL, time, marcador, assistente);
        addEvento(evento);

        if (marcador != null) {
            marcador.addGol();
        }
        if (assistente != null) {
            assistente.addAssistencia();
        }
    }

    /**
     * Registra um chute.
     */
    public void registrarChute(Team time, boolean noGol) {
        if (time == mandante) {
            chutesMandante++;
        } else {
            chutesVisitante++;
        }
    }

    /**
     * Registra uma falta.
     */
    public void registrarFalta(Team time) {
        if (time == mandante) {
            faltasMandante++;
        } else {
            faltasVisitante++;
        }
    }

    /**
     * Registra um cartão amarelo.
     */
    public void registrarCartaoAmarelo(Team time, Player jogador) {
        addEvento(new MatchEvent(minutoAtual, MatchEvent.TipoEvento.CARTAO_AMARELO, time, jogador));
        jogador.addCartaoAmarelo();
    }

    /**
     * Registra um cartão vermelho.
     */
    public void registrarCartaoVermelho(Team time, Player jogador) {
        addEvento(new MatchEvent(minutoAtual, MatchEvent.TipoEvento.CARTAO_VERMELHO, time, jogador));
        jogador.setSuspenso(true);

        // Remove o jogador do campo
        time.getTitulares().remove(jogador);
    }

    /**
     * Realiza uma substituição.
     */
    public boolean substituir(Team time, Player entra, Player sai) {
        if (!time.getReservas().contains(entra) || !time.getTitulares().contains(sai)) {
            return false;
        }

        time.getTitulares().remove(sai);
        time.getReservas().remove(entra);
        time.getTitulares().add(entra);
        entra.setPosicao(sai.getPosicao());

        addEvento(new MatchEvent(minutoAtual, MatchEvent.TipoEvento.SUBSTITUICAO, time, entra, sai));
        return true;
    }

    /**
     * Avança o tempo da partida.
     */
    public void avancarTempo(int minutos) {
        this.minutoAtual += minutos;

        if (minutoAtual >= 45 && !intervalo && minutoAtual < 46) {
            intervalo = true;
            addEvento(new MatchEvent(45, MatchEvent.TipoEvento.FIM_PRIMEIRO_TEMPO, null));
        }

        if (intervalo && minutoAtual >= 46) {
            intervalo = false;
            addEvento(new MatchEvent(46, MatchEvent.TipoEvento.INICIO_SEGUNDO_TEMPO, null));
        }
    }

    /**
     * Finaliza a partida.
     */
    public void finalizar() {
        this.emAndamento = false;
        this.finalizada = true;
        addEvento(new MatchEvent(90, MatchEvent.TipoEvento.FIM_JOGO, null));

        // Registra resultados nos times
        mandante.registrarResultado(golsMandante, golsVisitante);
        visitante.registrarResultado(golsVisitante, golsMandante);

        // Calcula renda
        if (estadio != null) {
            this.renda = estadio.calcularRenda(publico);
            mandante.receberRenda(renda);
        }
    }

    /**
     * Retorna o placar formatado.
     */
    public String getPlacar() {
        return mandante.getSigla() + " " + golsMandante + " x " + golsVisitante + " " + visitante.getSigla();
    }

    /**
     * Retorna o vencedor (null se empate).
     */
    public Team getVencedor() {
        if (golsMandante > golsVisitante) {
            return mandante;
        } else if (golsVisitante > golsMandante) {
            return visitante;
        }
        return null;
    }

    /**
     * Verifica se houve empate.
     */
    public boolean isEmpate() {
        return golsMandante == golsVisitante && finalizada;
    }

    // Getters e Setters
    public Team getMandante() {
        return mandante;
    }

    public Team getVisitante() {
        return visitante;
    }

    public Stadium getEstadio() {
        return estadio;
    }

    public int getGolsMandante() {
        return golsMandante;
    }

    public int getGolsVisitante() {
        return golsVisitante;
    }

    public int getMinutoAtual() {
        return minutoAtual;
    }

    public void setMinutoAtual(int minutoAtual) {
        this.minutoAtual = minutoAtual;
    }

    public boolean isEmAndamento() {
        return emAndamento;
    }

    public boolean isFinalizada() {
        return finalizada;
    }

    public boolean isIntervalo() {
        return intervalo;
    }

    public List<MatchEvent> getEventos() {
        return eventos;
    }

    public int getPosseMandante() {
        return posseMandante;
    }

    public void setPosseMandante(int posseMandante) {
        this.posseMandante = posseMandante;
    }

    public int getPosseVisitante() {
        return 100 - posseMandante;
    }

    public int getChutesMandante() {
        return chutesMandante;
    }

    public int getChutesVisitante() {
        return chutesVisitante;
    }

    public int getFaltasMandante() {
        return faltasMandante;
    }

    public int getFaltasVisitante() {
        return faltasVisitante;
    }

    public int getEscanteiosMandante() {
        return escanteiosMandante;
    }

    public int getEscanteiosVisitante() {
        return escanteiosVisitante;
    }

    public int getPublico() {
        return publico;
    }

    public void setPublico(int publico) {
        this.publico = publico;
    }

    public int getRenda() {
        return renda;
    }

    public String getCompeticao() {
        return competicao;
    }

    public void setCompeticao(String competicao) {
        this.competicao = competicao;
    }

    public String getFase() {
        return fase;
    }

    public void setFase(String fase) {
        this.fase = fase;
    }

    public boolean isDecisivo() {
        return decisivo;
    }

    public void setDecisivo(boolean decisivo) {
        this.decisivo = decisivo;
    }

    @Override
    public String toString() {
        return getPlacar();
    }
}
