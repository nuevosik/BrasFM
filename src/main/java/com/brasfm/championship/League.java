package com.brasfm.championship;

import com.brasfm.model.Team;
import com.brasfm.model.Match;
import com.brasfm.engine.MatchEngine;
import java.util.*;

/**
 * Representa uma liga (campeonato de pontos corridos).
 */
public class League {
    private String nome;
    private String pais;
    private int divisao;
    private List<Team> times;
    private List<Match> jogos;
    private int rodadaAtual;
    private boolean finalizado;

    private MatchEngine matchEngine;

    public League(String nome, String pais, int divisao) {
        this.nome = nome;
        this.pais = pais;
        this.divisao = divisao;
        this.times = new ArrayList<>();
        this.jogos = new ArrayList<>();
        this.rodadaAtual = 0;
        this.matchEngine = new MatchEngine();
    }

    /**
     * Construtor conveniente que aceita uma lista de times.
     */
    public League(String nome, java.util.List<Team> times) {
        this(nome, "Brasil", 1);
        for (Team t : times) {
            addTime(t);
        }
    }

    /**
     * Adiciona um time à liga.
     */
    public void addTime(Team time) {
        times.add(time);
        time.setDivisao(divisao);
    }

    /**
     * Gera o calendário de jogos (turno e returno).
     */
    public void gerarCalendario() {
        jogos.clear();
        int n = times.size();

        if (n < 2)
            return;

        // Algoritmo round-robin
        List<Team> timesRotativos = new ArrayList<>(times);
        Team fixo = timesRotativos.remove(0);

        int rodadas = n - 1;
        int jogosRodada = n / 2;

        // Primeiro turno
        for (int rodada = 0; rodada < rodadas; rodada++) {
            // Primeiro time contra o último na lista rotativa
            Match jogo1;
            if (rodada % 2 == 0) {
                jogo1 = new Match(fixo, timesRotativos.get(0));
            } else {
                jogo1 = new Match(timesRotativos.get(0), fixo);
            }
            jogo1.setCompeticao(nome);
            jogo1.setFase("Rodada " + (rodada + 1));
            jogos.add(jogo1);

            // Demais jogos da rodada
            for (int i = 1; i < jogosRodada; i++) {
                int casa = i;
                int fora = timesRotativos.size() - i;

                Match jogo;
                if ((rodada + i) % 2 == 0) {
                    jogo = new Match(timesRotativos.get(casa), timesRotativos.get(fora));
                } else {
                    jogo = new Match(timesRotativos.get(fora), timesRotativos.get(casa));
                }
                jogo.setCompeticao(nome);
                jogo.setFase("Rodada " + (rodada + 1));
                jogos.add(jogo);
            }

            // Rotaciona os times
            timesRotativos.add(0, timesRotativos.remove(timesRotativos.size() - 1));
        }

        // Segundo turno (inverte mandos)
        int jogosT1 = jogos.size();
        for (int i = 0; i < jogosT1; i++) {
            Match jogoT1 = jogos.get(i);
            Match jogoT2 = new Match(jogoT1.getVisitante(), jogoT1.getMandante());
            jogoT2.setCompeticao(nome);
            int rodadaT2 = Integer.parseInt(jogoT1.getFase().replace("Rodada ", "")) + rodadas;
            jogoT2.setFase("Rodada " + rodadaT2);
            jogos.add(jogoT2);
        }
    }

    /**
     * Retorna os jogos da rodada atual.
     */
    public List<Match> getJogosRodada(int rodada) {
        List<Match> resultado = new ArrayList<>();
        String faseRodada = "Rodada " + rodada;

        for (Match jogo : jogos) {
            if (faseRodada.equals(jogo.getFase())) {
                resultado.add(jogo);
            }
        }
        return resultado;
    }

    /**
     * Simula todos os jogos de uma rodada usando AdvancedMatchEngine.
     */
    public void simularRodada(int rodada) {
        List<Match> jogosRodada = getJogosRodada(rodada);

        for (Match jogoCalendario : jogosRodada) {
            if (!jogoCalendario.isFinalizada()) {
                Team mandante = jogoCalendario.getMandante();
                Team visitante = jogoCalendario.getVisitante();

                // Simula a partida com AdvancedMatchEngine (3o param = jogo importante)
                Match resultado = matchEngine.simular(mandante, visitante, false);

                // Copia os gols para o jogo do calendário usando registrarGol
                int golsCasa = resultado.getGolsMandante();
                int golsFora = resultado.getGolsVisitante();

                // Registra os gols no jogo do calendário
                for (int i = 0; i < golsCasa; i++) {
                    jogoCalendario.registrarGol(mandante, null, null);
                }
                for (int i = 0; i < golsFora; i++) {
                    jogoCalendario.registrarGol(visitante, null, null);
                }

                // Finaliza o jogo (atualiza estatísticas dos times automaticamente)
                jogoCalendario.finalizar();
            }
        }

        rodadaAtual = rodada;

        // Verifica se terminou
        if (rodada >= getTotalRodadas()) {
            finalizado = true;
        }
    }

    /**
     * Retorna a classificação ordenada.
     */
    public List<Team> getClassificacao() {
        List<Team> classificacao = new ArrayList<>(times);

        classificacao.sort((a, b) -> {
            // Pontos
            if (b.getPontos() != a.getPontos()) {
                return Integer.compare(b.getPontos(), a.getPontos());
            }
            // Vitórias
            if (b.getVitorias() != a.getVitorias()) {
                return Integer.compare(b.getVitorias(), a.getVitorias());
            }
            // Saldo de gols
            if (b.getSaldoGols() != a.getSaldoGols()) {
                return Integer.compare(b.getSaldoGols(), a.getSaldoGols());
            }
            // Gols pró
            return Integer.compare(b.getGolsPro(), a.getGolsPro());
        });

        return classificacao;
    }

    /**
     * Retorna o total de rodadas.
     */
    public int getTotalRodadas() {
        return (times.size() - 1) * 2;
    }

    /**
     * Retorna o campeão (se finalizado).
     */
    public Team getCampeao() {
        if (!finalizado) {
            return null;
        }
        List<Team> classificacao = getClassificacao();
        return classificacao.isEmpty() ? null : classificacao.get(0);
    }

    /**
     * Retorna os times rebaixados.
     */
    public List<Team> getRebaixados(int quantidade) {
        List<Team> classificacao = getClassificacao();
        List<Team> rebaixados = new ArrayList<>();

        int inicio = classificacao.size() - quantidade;
        for (int i = inicio; i < classificacao.size(); i++) {
            rebaixados.add(classificacao.get(i));
        }

        return rebaixados;
    }

    /**
     * Retorna os times classificados para a Libertadores.
     */
    public List<Team> getClassificadosLibertadores(int quantidade) {
        List<Team> classificacao = getClassificacao();
        List<Team> classificados = new ArrayList<>();

        for (int i = 0; i < Math.min(quantidade, classificacao.size()); i++) {
            classificados.add(classificacao.get(i));
        }

        return classificados;
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public String getPais() {
        return pais;
    }

    public int getDivisao() {
        return divisao;
    }

    public List<Team> getTimes() {
        return times;
    }

    public List<Match> getJogos() {
        return jogos;
    }

    public int getRodadaAtual() {
        return rodadaAtual;
    }

    public boolean isFinalizado() {
        return finalizado;
    }

    @Override
    public String toString() {
        return nome + " - " + pais;
    }
}
