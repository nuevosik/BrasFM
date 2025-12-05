package com.brasfm.social;

import com.brasfm.model.*;
import java.util.*;

/**
 * Sistema de Moral com "Sweet Spot" - evita extremos de euforia e depressão.
 */
public class MoraleSystem {

    public enum EstadoMoral {
        DEPRIMIDO(0, 20, -0.20, "Sem confiança, comete erros frequentes"),
        DESMORALIZADO(21, 35, -0.10, "Desmotivado, baixa intensidade"),
        INSATISFEITO(36, 45, -0.05, "Infeliz mas funcional"),
        NEUTRO(46, 55, 0.00, "Estado normal"),
        MOTIVADO(56, 70, 0.05, "Bom estado mental"),
        FOCADO(71, 80, 0.08, "Estado ideal - concentrado e confiante"), // SWEET SPOT
        CONFIANTE(81, 90, 0.05, "Muito confiante"),
        EUFORICO(91, 100, 0.00, "Risco de complacência"); // Pode virar negativo

        private final int min;
        private final int max;
        private final double modificador;
        private final String descricao;

        EstadoMoral(int min, int max, double modificador, String descricao) {
            this.min = min;
            this.max = max;
            this.modificador = modificador;
            this.descricao = descricao;
        }

        public static EstadoMoral fromValor(int moral) {
            for (EstadoMoral e : values()) {
                if (moral >= e.min && moral <= e.max)
                    return e;
            }
            return NEUTRO;
        }

        public double getModificador() {
            return modificador;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum TipoEvento {
        // Resultados
        VITORIA_CONVINCENTE(12, "Grande vitória"),
        VITORIA_APERTADA(6, "Vitória difícil"),
        EMPATE_POSITIVO(2, "Empate com time maior"),
        EMPATE_NEGATIVO(-3, "Empate decepcionante"),
        DERROTA_ESPERADA(-5, "Derrota para time maior"),
        DERROTA_HUMILHANTE(-15, "Goleada sofrida"),

        // Individual
        GOL_MARCADO(8, "Marcou gol"),
        ASSISTENCIA(5, "Deu assistência"),
        ATUACAO_DESTAQUE(6, "Destaque da partida"),
        ERRO_GRAVE(-10, "Erro causou gol"),
        CARTAO_VERMELHO(-12, "Expulso"),

        // Gestão
        TITULAR(3, "Escalado como titular"),
        RESERVA_ESPERADO(0, "No banco conforme esperado"),
        RESERVA_INJUSTO(-8, "No banco sem justificativa"),
        SUBSTITUIDO_CEDO(-6, "Substituído antes dos 60min"),
        ELOGIO_TECNICO(5, "Elogiado pelo técnico"),
        CRITICA_PUBLICA(-10, "Criticado publicamente"),

        // Contrato
        RENOVACAO_ACEITA(10, "Contrato renovado"),
        RENOVACAO_RECUSADA(-15, "Renovação negada"),
        AUMENTO_SALARIO(8, "Recebeu aumento"),

        // Externo
        RUMOR_SAIDA(-5, "Rumores de transferência"),
        CONVOCACAO_SELECAO(12, "Convocado para seleção"),
        LESAO(-10, "Sofreu lesão");

        private final int impacto;
        private final String descricao;

        TipoEvento(int impacto, String descricao) {
            this.impacto = impacto;
            this.descricao = descricao;
        }

        public int getImpacto() {
            return impacto;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    private Map<Player, Integer> moralJogadores = new HashMap<>();
    private Map<Player, Integer> semanasNoEstado = new HashMap<>(); // Para complacência
    private Random random = new Random();

    /**
     * Aplica evento de moral a um jogador.
     */
    public void aplicarEvento(Player jogador, TipoEvento evento) {
        int moralAtual = moralJogadores.getOrDefault(jogador, 50);
        int impacto = evento.getImpacto();

        // Personalidade afeta impacto
        PlayerPersonality pers = jogador.getPersonality();
        if (pers != null) {
            // Jogadores com alta determinação lidam melhor com negativos
            if (impacto < 0 && pers.getPressao() > 15) {
                impacto = (int) (impacto * 0.6);
            }
            // Jogadores ambiciosos reagem mais a positivos
            if (impacto > 0 && pers.getAmbicao() > 15) {
                impacto = (int) (impacto * 1.3);
            }
        }

        int novaMoral = Math.max(0, Math.min(100, moralAtual + impacto));
        moralJogadores.put(jogador, novaMoral);

        // Reset contador de estado
        semanasNoEstado.put(jogador, 0);
    }

    /**
     * Processa passagem de semana - verifica complacência.
     */
    public List<String> processarSemana(List<Player> elenco) {
        List<String> eventos = new ArrayList<>();

        for (Player p : elenco) {
            int moral = moralJogadores.getOrDefault(p, 50);
            int semanas = semanasNoEstado.getOrDefault(p, 0) + 1;
            semanasNoEstado.put(p, semanas);

            EstadoMoral estado = EstadoMoral.fromValor(moral);

            // Complacência após 4+ semanas em euforia
            if (estado == EstadoMoral.EUFORICO && semanas >= 4) {
                // Risco de complacência
                if (random.nextDouble() < 0.3) {
                    moral -= 10;
                    moralJogadores.put(p, moral);
                    eventos.add("⚠️ " + p.getNome() + " mostra sinais de complacência");
                }
            }

            // Recuperação natural de estados extremos
            if (estado == EstadoMoral.DEPRIMIDO && semanas >= 2) {
                moral += 5;
                moralJogadores.put(p, moral);
            }

            // Decaimento natural da euforia
            if (moral > 80) {
                moral -= 2;
                moralJogadores.put(p, moral);
            }
        }

        return eventos;
    }

    /**
     * Calcula modificador de performance baseado no moral.
     */
    public double getModificadorPerformance(Player jogador, boolean jogoGrande) {
        int moral = moralJogadores.getOrDefault(jogador, 50);
        EstadoMoral estado = EstadoMoral.fromValor(moral);
        double mod = estado.getModificador();

        // Eufóricos podem falhar em jogos grandes (excesso de confiança)
        if (estado == EstadoMoral.EUFORICO && jogoGrande) {
            if (random.nextDouble() < 0.25) {
                mod = -0.08;
            }
        }

        // Jogadores nervosos sofrem mais em jogos grandes
        PlayerPersonality pers = jogador.getPersonality();
        if (jogoGrande && pers != null && pers.getJogosImportantes() < 10) {
            if (estado.ordinal() < EstadoMoral.MOTIVADO.ordinal()) {
                mod -= 0.10; // Ansiedade
            }
        }

        return mod;
    }

    /**
     * Calcula moral médio do elenco.
     */
    public int getMoralMedio(List<Player> elenco) {
        if (elenco.isEmpty())
            return 50;

        int soma = 0;
        for (Player p : elenco) {
            soma += moralJogadores.getOrDefault(p, 50);
        }
        return soma / elenco.size();
    }

    /**
     * Verifica se equipe está no sweet spot.
     */
    public boolean noSweetSpot(List<Player> titulares) {
        int count = 0;
        for (Player p : titulares) {
            EstadoMoral estado = EstadoMoral.fromValor(moralJogadores.getOrDefault(p, 50));
            if (estado == EstadoMoral.FOCADO || estado == EstadoMoral.MOTIVADO) {
                count++;
            }
        }
        return count >= titulares.size() * 0.7; // 70% no ideal
    }

    // Getters
    public int getMoral(Player p) {
        return moralJogadores.getOrDefault(p, 50);
    }

    public void setMoral(Player p, int v) {
        moralJogadores.put(p, Math.max(0, Math.min(100, v)));
    }

    public EstadoMoral getEstado(Player p) {
        return EstadoMoral.fromValor(getMoral(p));
    }
}
