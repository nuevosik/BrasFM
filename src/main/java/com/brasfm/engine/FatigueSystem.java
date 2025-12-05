package com.brasfm.engine;

import com.brasfm.model.*;

/**
 * Sistema de fadiga que gerencia a energia dos jogadores durante a partida.
 */
public class FatigueSystem {

    // Constantes de gasto de energia
    private static final double ENERGIA_POR_MINUTO_BASE = 0.8; // Gasto base por minuto
    private static final double ENERGIA_POR_SPRINT = 2.0; // Gasto extra por sprint
    private static final double ENERGIA_POR_DISPUTA = 1.5; // Gasto em disputas físicas
    private static final double ENERGIA_GEGENPRESS = 2.5; // Multiplicador para Gegenpress
    private static final double RECUPERACAO_INTERVALO = 15.0; // Recuperação no intervalo
    private static final double RECUPERACAO_PAUSA = 3.0; // Recuperação em pausas

    /**
     * Calcula o gasto de energia base por minuto considerando a tática.
     */
    public double calcularGastoMinuto(
            PlayerAttributes attrs,
            OutOfPossessionSettings.IntensidadePressao intensidade,
            InPossessionSettings.Ritmo ritmo,
            boolean temPosse) {
        double gasto = ENERGIA_POR_MINUTO_BASE;

        // Resistência reduz o gasto
        double resistencia = attrs.getResistencia() / 100.0;
        gasto *= (1.5 - resistencia * 0.5); // Jogador com 100 gasta 50% menos

        // Intensidade da pressão afeta quando não tem posse
        if (!temPosse) {
            gasto *= intensidade.getCustoEnergia();
        }

        // Ritmo afeta quando tem posse
        if (temPosse) {
            gasto *= ritmo.getFator();
        }

        return gasto;
    }

    /**
     * Calcula o gasto de energia para uma ação específica.
     */
    public double calcularGastoAcao(
            PlayerAttributes attrs,
            TipoAcao acao) {
        double resistencia = attrs.getResistencia() / 100.0;
        double gasto;

        switch (acao) {
            case SPRINT:
                gasto = ENERGIA_POR_SPRINT;
                break;
            case DISPUTA_FISICA:
                gasto = ENERGIA_POR_DISPUTA;
                // Força ajuda a gastar menos em disputas
                double forca = attrs.getForca() / 100.0;
                gasto *= (1.3 - forca * 0.3);
                break;
            case DRIBLE:
                gasto = 1.2;
                break;
            case CHUTE:
                gasto = 0.8;
                break;
            case PASSE_LONGO:
                gasto = 0.5;
                break;
            case CABECEIO:
                gasto = 1.0;
                break;
            case DESARME:
                gasto = 1.3;
                break;
            default:
                gasto = 0.3;
        }

        // Resistência reduz gasto
        gasto *= (1.3 - resistencia * 0.3);

        return gasto;
    }

    /**
     * Calcula a recuperação de energia em uma pausa.
     */
    public double calcularRecuperacao(PlayerAttributes attrs, TipoPausa pausa) {
        double baseRecuperacao;

        switch (pausa) {
            case INTERVALO:
                baseRecuperacao = RECUPERACAO_INTERVALO;
                break;
            case FALTA:
            case ESCANTEIO:
            case LATERAL:
                baseRecuperacao = RECUPERACAO_PAUSA;
                break;
            case SUBSTITUICAO:
                baseRecuperacao = 2.0;
                break;
            case GOL:
                baseRecuperacao = 4.0;
                break;
            default:
                baseRecuperacao = 1.0;
        }

        // Resistência ajuda a recuperar mais rápido
        double resistencia = attrs.getResistencia() / 100.0;
        return baseRecuperacao * (0.8 + resistencia * 0.4);
    }

    /**
     * Converte energia (0-100) para fator de fadiga (0-1).
     * A fadiga afeta os atributos de forma não-linear.
     */
    public double energiaParaFatorFadiga(double energia) {
        // Função sigmoide suavizada
        // Com 100% de energia = 1.0 (sem penalidade)
        // Com 50% de energia = ~0.85
        // Com 20% de energia = ~0.5
        // Com 0% de energia = ~0.3

        if (energia >= 80) {
            // Quase sem efeito acima de 80%
            return 0.95 + (energia - 80) * 0.0025;
        } else if (energia >= 50) {
            // Queda gradual entre 50-80%
            return 0.75 + (energia - 50) * 0.0067;
        } else if (energia >= 20) {
            // Queda mais acentuada entre 20-50%
            return 0.45 + (energia - 20) * 0.01;
        } else {
            // Queda severa abaixo de 20%
            return 0.30 + energia * 0.0075;
        }
    }

    /**
     * Determina se um jogador está em risco de lesão por fadiga.
     */
    public double calcularRiscoLesao(double energia, int idade) {
        double riscoBase = 0.001; // 0.1% por minuto base

        // Baixa energia aumenta risco
        if (energia < 30) {
            riscoBase *= 3;
        } else if (energia < 50) {
            riscoBase *= 1.5;
        }

        // Idade aumenta risco
        if (idade >= 32) {
            riscoBase *= 1.0 + (idade - 32) * 0.15;
        }

        return Math.min(0.05, riscoBase); // Máximo 5% por minuto
    }

    /**
     * Determina se uma substituição é recomendada.
     */
    public boolean substituicaoRecomendada(double energia, int minuto, boolean perdendo) {
        // Energia muito baixa
        if (energia < 30)
            return true;

        // Energia baixa nos últimos 20 minutos
        if (energia < 50 && minuto > 70)
            return true;

        // Se estiver ganhando e jogador cansado
        if (!perdendo && energia < 40 && minuto > 60)
            return true;

        return false;
    }

    public enum TipoAcao {
        SPRINT,
        DISPUTA_FISICA,
        DRIBLE,
        CHUTE,
        PASSE_LONGO,
        CABECEIO,
        DESARME,
        MOVIMENTO
    }

    public enum TipoPausa {
        INTERVALO,
        FALTA,
        ESCANTEIO,
        LATERAL,
        SUBSTITUICAO,
        GOL
    }
}
