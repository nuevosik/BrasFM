package com.brasfm.social;

import com.brasfm.model.*;
import java.util.*;

/**
 * Sistema de palestras/team talks para gestão de moral.
 */
public class TeamTalkSystem {

    public enum TipoPalestra {
        // Antes do jogo
        MOTIVACIONAL("Motivacional", "Inspira a equipe", 5, -3),
        CALMA("Manter a Calma", "Reduz ansiedade", 2, 2),
        PRESSAO("Colocar Pressão", "Exige resultados", 8, -10),
        CONFIANCA("Mostrar Confiança", "Demonstra fé na equipe", 4, 0),
        SEM_PRESSAO("Sem Pressão", "Tira peso das costas", 0, 5),

        // Intervalo
        ELOGIO("Elogiar Primeiro Tempo", "Reconhece bom trabalho", 6, -2),
        BRONCA("Dar Bronca", "Exige melhoria", 10, -15),
        TATICO("Ajuste Tático", "Foco técnico, não emocional", 2, 1),
        CALMARIA("Acalmar Nervos", "Para jogos tensos", 0, 8),

        // Pós-jogo
        PARABENS("Parabéns Pela Vitória", "Celebra conquista", 8, 0),
        PUXAORELHA("Puxão de Orelha", "Crítica construtiva", 3, -5),
        APOIO_DERROTA("Apoio Após Derrota", "Levanta moral", 5, 3),
        CRITICA_DURA("Crítica Dura", "Inaceitável", -5, -15);

        private final String nome;
        private final String descricao;
        private final int impactoPositivo; // Se funcionar
        private final int impactoNegativo; // Se falhar

        TipoPalestra(String nome, String descricao, int positivo, int negativo) {
            this.nome = nome;
            this.descricao = descricao;
            this.impactoPositivo = positivo;
            this.impactoNegativo = negativo;
        }

        public String getNome() {
            return nome;
        }

        public String getDescricao() {
            return descricao;
        }

        public int getImpactoPositivo() {
            return impactoPositivo;
        }

        public int getImpactoNegativo() {
            return impactoNegativo;
        }
    }

    public enum Momento {
        PRE_JOGO, INTERVALO, POS_JOGO
    }

    private Random random = new Random();
    private MoraleSystem moraleSystem;

    public TeamTalkSystem(MoraleSystem moraleSystem) {
        this.moraleSystem = moraleSystem;
    }

    /**
     * Aplica palestra a um jogador individual.
     */
    public String aplicarPalestraIndividual(Player jogador, TipoPalestra tipo) {
        boolean sucesso = calcularSucesso(jogador, tipo);

        int impacto = sucesso ? tipo.getImpactoPositivo() : tipo.getImpactoNegativo();
        int moralAtual = moraleSystem.getMoral(jogador);
        moraleSystem.setMoral(jogador, moralAtual + impacto);

        String resultado = sucesso ? "✓" : "✗";
        String reacao = sucesso ? "Reagiu bem" : "Não aceitou";

        return String.format("%s %s - %s (%+d)", resultado, jogador.getNome(), reacao, impacto);
    }

    /**
     * Aplica palestra a todo o time.
     */
    public TeamTalkResult aplicarPalestraColetiva(List<Player> jogadores, TipoPalestra tipo) {
        TeamTalkResult resultado = new TeamTalkResult(tipo);

        for (Player p : jogadores) {
            boolean sucesso = calcularSucesso(p, tipo);
            int impacto = sucesso ? tipo.getImpactoPositivo() : tipo.getImpactoNegativo();

            int moralAtual = moraleSystem.getMoral(p);
            moraleSystem.setMoral(p, moralAtual + impacto);

            if (sucesso) {
                resultado.addSucesso(p);
            } else {
                resultado.addFalha(p);
            }
        }

        return resultado;
    }

    /**
     * Calcula se palestra terá sucesso com jogador.
     */
    private boolean calcularSucesso(Player jogador, TipoPalestra tipo) {
        int chanceBase = 50;

        PlayerPersonality pers = jogador.getPersonality();
        MoraleSystem.EstadoMoral estado = moraleSystem.getEstado(jogador);

        // Personalidade afeta receptividade
        if (pers != null) {
            // Profissionais aceitam críticas
            if (tipo == TipoPalestra.BRONCA || tipo == TipoPalestra.CRITICA_DURA) {
                chanceBase += (pers.getProfissionalismo() - 10) * 3;
            }

            // Determinados respondem a pressão
            if (tipo == TipoPalestra.PRESSAO) {
                chanceBase += (pers.getPressao() - 10) * 3;
            }

            // Temperamentais reagem mal a críticas
            if (pers.getTemperamento() < 10) {
                if (tipo == TipoPalestra.BRONCA || tipo == TipoPalestra.CRITICA_DURA) {
                    chanceBase -= 20;
                }
            }
        }

        // Estado moral atual afeta receptividade
        switch (estado) {
            case DEPRIMIDO:
                // Precisam de apoio, não crítica
                if (tipo == TipoPalestra.APOIO_DERROTA || tipo == TipoPalestra.CONFIANCA) {
                    chanceBase += 20;
                } else if (tipo == TipoPalestra.BRONCA) {
                    chanceBase -= 30;
                }
                break;

            case EUFORICO:
                // Podem ignorar avisos
                if (tipo == TipoPalestra.CALMA) {
                    chanceBase -= 15;
                }
                break;

            case FOCADO:
                // Receptivos a tudo
                chanceBase += 10;
                break;
        }

        return random.nextInt(100) < chanceBase;
    }

    /**
     * Retorna palestras recomendadas para o momento.
     */
    public List<TipoPalestra> getPalestrasRecomendadas(Momento momento,
            int placarMandante, int placarVisitante, boolean mandante) {

        List<TipoPalestra> recomendadas = new ArrayList<>();
        int saldo = mandante ? placarMandante - placarVisitante : placarVisitante - placarMandante;

        switch (momento) {
            case PRE_JOGO:
                recomendadas.add(TipoPalestra.MOTIVACIONAL);
                recomendadas.add(TipoPalestra.CONFIANCA);
                recomendadas.add(TipoPalestra.CALMA);
                break;

            case INTERVALO:
                if (saldo >= 2) {
                    recomendadas.add(TipoPalestra.ELOGIO);
                    recomendadas.add(TipoPalestra.CALMA);
                } else if (saldo >= 0) {
                    recomendadas.add(TipoPalestra.TATICO);
                    recomendadas.add(TipoPalestra.MOTIVACIONAL);
                } else {
                    recomendadas.add(TipoPalestra.BRONCA);
                    recomendadas.add(TipoPalestra.CALMARIA);
                }
                break;

            case POS_JOGO:
                if (saldo > 0) {
                    recomendadas.add(TipoPalestra.PARABENS);
                } else if (saldo == 0) {
                    recomendadas.add(TipoPalestra.PUXAORELHA);
                    recomendadas.add(TipoPalestra.APOIO_DERROTA);
                } else {
                    recomendadas.add(TipoPalestra.APOIO_DERROTA);
                    recomendadas.add(TipoPalestra.CRITICA_DURA);
                }
                break;
        }

        return recomendadas;
    }

    /**
     * Resultado de uma palestra coletiva.
     */
    public static class TeamTalkResult {
        private TipoPalestra tipo;
        private List<Player> sucessos = new ArrayList<>();
        private List<Player> falhas = new ArrayList<>();

        public TeamTalkResult(TipoPalestra tipo) {
            this.tipo = tipo;
        }

        public void addSucesso(Player p) {
            sucessos.add(p);
        }

        public void addFalha(Player p) {
            falhas.add(p);
        }

        public int getTaxaSucesso() {
            int total = sucessos.size() + falhas.size();
            return total > 0 ? (sucessos.size() * 100) / total : 0;
        }

        public String getResumo() {
            return String.format("%s: %d/%d aceitaram (%d%%)",
                    tipo.getNome(), sucessos.size(), sucessos.size() + falhas.size(), getTaxaSucesso());
        }

        public List<Player> getSucessos() {
            return sucessos;
        }

        public List<Player> getFalhas() {
            return falhas;
        }
    }
}
