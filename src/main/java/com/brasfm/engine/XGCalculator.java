package com.brasfm.engine;

import com.brasfm.model.*;

/**
 * Calculador de Expected Goals (xG).
 * Calcula a probabilidade de um chute resultar em gol baseado em múltiplos
 * fatores.
 */
public class XGCalculator {

    // Constantes para cálculo base de xG por distância
    private static final double XG_AREA_PEQUENA = 0.45; // Dentro da pequena área
    private static final double XG_AREA_GRANDE = 0.12; // Dentro da área
    private static final double XG_MEIA_LUA = 0.06; // Meia-lua
    private static final double XG_MEDIA = 0.03; // 20-30 metros
    private static final double XG_LONGA = 0.01; // 30+ metros

    /**
     * Calcula o xG base de um chute baseado na posição.
     * 
     * @param distanciaGol Distância do gol em metros
     * @param angulo       Ângulo em relação ao centro do gol (0-90 graus)
     * @param dentroArea   Se o chute é de dentro da área
     * @param cabecada     Se é uma cabeçada
     * @return xG base entre 0 e 1
     */
    public double calcularXGBase(double distanciaGol, double angulo, boolean dentroArea, boolean cabecada) {
        double xg;

        // Cálculo base por distância
        if (distanciaGol <= 6) {
            xg = XG_AREA_PEQUENA;
        } else if (distanciaGol <= 16.5) {
            xg = XG_AREA_GRANDE;
        } else if (distanciaGol <= 22) {
            xg = XG_MEIA_LUA;
        } else if (distanciaGol <= 30) {
            xg = XG_MEDIA;
        } else {
            xg = XG_LONGA;
        }

        // Modificador de ângulo (ângulo menor = mais difícil)
        double fatorAngulo = Math.sin(Math.toRadians(angulo));
        xg *= fatorAngulo;

        // Cabeçadas são mais difíceis
        if (cabecada) {
            xg *= 0.7;
        }

        return Math.min(0.95, Math.max(0.01, xg));
    }

    /**
     * Calcula o xG ajustado considerando todos os fatores contextuais.
     */
    public double calcularXGCompleto(
            double xgBase,
            PlayerAttributes finalizador,
            PlayerAttributes goleiro,
            double fadigaFinalizador, // 0-1, onde 1 = totalmente descansado
            double fadigaGoleiro,
            double pressaoDefensiva, // 0-1, onde 1 = muita pressão
            boolean grandeChance, // 1v1 com goleiro, etc.
            boolean jogoImportante, // Final, decisão
            int minuto // Minuto do jogo
    ) {
        double xg = xgBase;

        // 1. Fator Finalização do Jogador
        double fatorFinalizacao = finalizador.getFinalizacao() / 100.0;
        double fatorTecnica = finalizador.getTecnica() / 100.0;
        fatorFinalizacao = (fatorFinalizacao * 0.7) + (fatorTecnica * 0.3);
        xg *= (0.5 + fatorFinalizacao); // Multiplica entre 0.5 e 1.5

        // 2. Fator Goleiro
        double fatorGoleiro = goleiro.getGoleiro() / 100.0;
        double fatorReflexos = goleiro.getReflexos() / 100.0;
        double habilidadeGoleiro = (fatorGoleiro * 0.6) + (fatorReflexos * 0.4);
        xg *= (1.5 - habilidadeGoleiro); // Multiplica entre 0.5 e 1.5

        // 3. Fator Fadiga
        // Jogadores cansados são menos precisos
        double fatorFadiga = 0.7 + (fadigaFinalizador * 0.3);
        xg *= fatorFadiga;

        // Goleiros cansados defendem pior
        double fatorFadigaGoleiro = 0.7 + (fadigaGoleiro * 0.3);
        xg *= (2 - fatorFadigaGoleiro); // Inverte: goleiro cansado = mais xG

        // 4. Fator Pressão Defensiva
        // Maior pressão = menor xG
        xg *= (1 - pressaoDefensiva * 0.4);

        // 5. Grande Chance (1v1, rebote fácil)
        if (grandeChance) {
            xg *= 1.5;
        }

        // 6. Fator Psicológico (compostura em jogos importantes)
        if (jogoImportante) {
            double compostura = finalizador.getCompostura() / 100.0;
            // Jogadores com baixa compostura perdem até 30% em jogos grandes
            double fatorPsicologico = 0.7 + (compostura * 0.3);
            xg *= fatorPsicologico;
        }

        // 7. Fator Concentração nos minutos finais
        if (minuto > 80) {
            double concentracao = finalizador.getConcentracao() / 100.0;
            double fatorConcentracao = 0.8 + (concentracao * 0.2);
            xg *= fatorConcentracao;
        }

        // 8. Fator Decisão (jogador inteligente escolhe melhor quando chutar)
        double decisoes = finalizador.getDecisoes() / 100.0;
        // Isso é aplicado externamente na decisão de chutar, não no xG em si

        return Math.min(0.95, Math.max(0.01, xg));
    }

    /**
     * Calcula o xG de um pênalti.
     */
    public double calcularXGPenalti(PlayerAttributes cobrador, PlayerAttributes goleiro, boolean jogoImportante) {
        // xG base de pênalti é ~0.76
        double xg = 0.76;

        // Habilidade do cobrador
        double fatorPenalti = cobrador.getPenalti() / 100.0;
        xg *= (0.8 + fatorPenalti * 0.4); // 0.8 a 1.2

        // Habilidade do goleiro
        double fatorGoleiroPenalti = goleiro.getUmContraUm() / 100.0;
        xg *= (1.2 - fatorGoleiroPenalti * 0.3); // 0.9 a 1.2

        // Pressão psicológica
        if (jogoImportante) {
            double compostura = cobrador.getCompostura() / 100.0;
            xg *= (0.7 + compostura * 0.3);
        }

        return Math.min(0.95, Math.max(0.50, xg));
    }

    /**
     * Calcula o xG de uma cabeçada após cruzamento.
     */
    public double calcularXGCabeceio(
            PlayerAttributes cabeceeador,
            PlayerAttributes cruzador,
            PlayerAttributes goleiro,
            double distanciaGol,
            boolean marcado) {
        double xgBase = calcularXGBase(distanciaGol, 45, true, true);

        // Qualidade do cruzamento
        double qualidadeCruzamento = cruzador.getCruzamento() / 100.0;
        xgBase *= (0.6 + qualidadeCruzamento * 0.6);

        // Habilidade de cabeceio
        double habilidadeCabeceio = cabeceeador.getCabeceio() / 100.0;
        double salto = cabeceeador.getSalto() / 100.0;
        xgBase *= (0.5 + (habilidadeCabeceio * 0.3 + salto * 0.2));

        // Goleiro
        double fatorGoleiro = goleiro.getSaida() / 100.0;
        xgBase *= (1.3 - fatorGoleiro * 0.3);

        // Se marcado de perto
        if (marcado) {
            xgBase *= 0.6;
        }

        return Math.min(0.60, Math.max(0.02, xgBase));
    }

    /**
     * Determina se um chute resulta em gol baseado no xG.
     * 
     * @return true se gol, false caso contrário
     */
    public boolean resolverChute(double xg, java.util.Random random) {
        return random.nextDouble() < xg;
    }

    /**
     * Gera uma descrição do tipo de chance baseado no xG.
     */
    public String descreverChance(double xg) {
        if (xg >= 0.50) {
            return "Chance Claríssima";
        } else if (xg >= 0.30) {
            return "Grande Chance";
        } else if (xg >= 0.15) {
            return "Boa Chance";
        } else if (xg >= 0.08) {
            return "Meia Chance";
        } else if (xg >= 0.04) {
            return "Chance Difícil";
        } else {
            return "Tentativa de Longe";
        }
    }
}
