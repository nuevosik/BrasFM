package com.brasfm.economy;

import com.brasfm.model.*;
import java.util.*;

/**
 * Sistema de valoração dinâmica de jogadores.
 * Valor = (CA × IdadeFactor) + (ReputaçãoLiga × Forma) + (ContratoRestante ×
 * Potencial)
 */
public class PlayerValuation {

    // Constantes de valoração
    private static final long VALOR_BASE = 100_000; // R$ 100k por ponto de força
    private static final double INFLACAO_ANUAL = 1.05; // 5% ao ano

    private int anoAtual;

    public PlayerValuation(int anoAtual) {
        this.anoAtual = anoAtual;
    }

    /**
     * Calcula o valor de mercado de um jogador.
     */
    public long calcularValor(Player jogador, Team time, int reputacaoLiga) {
        // Componente base: Força × Valor Base
        long valorBase = jogador.getForca() * VALOR_BASE;

        // Fator Idade (pico 23-27)
        double fatorIdade = calcularFatorIdade(jogador.getIdade());

        // Fator Contrato (Lei Bosman)
        double fatorContrato = calcularFatorContrato(jogador.getSemanasContrato());

        // Fator Potencial
        double fatorPotencial = calcularFatorPotencial(jogador);

        // Fator Reputação da Liga
        double fatorLiga = reputacaoLiga / 100.0;

        // Fator Forma Recente
        double fatorForma = calcularFatorForma(jogador);

        // Fator Home-Grown (formado no país)
        double fatorHomeGrown = 1.0;
        // TODO: Implementar verificação de home-grown

        // Cálculo final
        long valor = (long) (valorBase * fatorIdade * fatorContrato * fatorPotencial);
        valor = (long) (valor * (0.7 + fatorLiga * 0.5)); // Liga afeta 30-120%
        valor = (long) (valor * fatorForma);
        valor = (long) (valor * fatorHomeGrown);

        // Aplica inflação
        int anosDesdeInicio = anoAtual - 2025;
        valor = (long) (valor * Math.pow(INFLACAO_ANUAL, anosDesdeInicio));

        // Arredonda para valores "bonitos"
        valor = arredondarValor(valor);

        return Math.max(50000, valor);
    }

    /**
     * Fator de idade: pico entre 23-27, depois decai.
     */
    private double calcularFatorIdade(int idade) {
        if (idade <= 17)
            return 0.3;
        if (idade <= 19)
            return 0.5;
        if (idade <= 21)
            return 0.8;
        if (idade <= 22)
            return 0.95;
        if (idade <= 27)
            return 1.0; // Pico
        if (idade <= 29)
            return 0.85;
        if (idade <= 31)
            return 0.6;
        if (idade <= 33)
            return 0.35;
        return 0.15;
    }

    /**
     * Fator de contrato: último ano = valor muito reduzido.
     */
    private double calcularFatorContrato(int semanasRestantes) {
        int mesesRestantes = semanasRestantes / 4;

        if (mesesRestantes <= 6) {
            return 0.25; // Quase de graça - Lei Bosman
        } else if (mesesRestantes <= 12) {
            return 0.50;
        } else if (mesesRestantes <= 18) {
            return 0.75;
        } else if (mesesRestantes <= 24) {
            return 0.90;
        }
        return 1.0;
    }

    /**
     * Fator de potencial: jovens com alto potencial valem mais.
     */
    private double calcularFatorPotencial(Player jogador) {
        if (jogador.getIdade() > 25) {
            return 1.0; // Potencial não afeta após pico
        }

        int potencial = jogador.getPotencial();
        int forca = jogador.getForca();
        int diferenca = potencial - forca;

        if (diferenca > 20)
            return 1.5;
        if (diferenca > 15)
            return 1.3;
        if (diferenca > 10)
            return 1.2;
        if (diferenca > 5)
            return 1.1;
        return 1.0;
    }

    /**
     * Fator de forma recente.
     */
    private double calcularFatorForma(Player jogador) {
        double mediaNota = jogador.getMediaNotas();

        if (mediaNota >= 8.0)
            return 1.3; // Forma excepcional
        if (mediaNota >= 7.5)
            return 1.15;
        if (mediaNota >= 7.0)
            return 1.05;
        if (mediaNota >= 6.5)
            return 1.0;
        if (mediaNota >= 6.0)
            return 0.9;
        return 0.75; // Má forma
    }

    /**
     * Arredonda para valores "bonitos" de mercado.
     */
    private long arredondarValor(long valor) {
        if (valor < 500_000) {
            return (valor / 50_000) * 50_000;
        } else if (valor < 5_000_000) {
            return (valor / 100_000) * 100_000;
        } else if (valor < 20_000_000) {
            return (valor / 500_000) * 500_000;
        } else {
            return (valor / 1_000_000) * 1_000_000;
        }
    }

    /**
     * Calcula salário esperado baseado no valor de mercado.
     */
    public int calcularSalarioEsperado(long valorMercado) {
        // Salário anual = ~10-15% do valor de mercado
        double fator = 0.10 + new Random().nextDouble() * 0.05;
        long salarioAnual = (long) (valorMercado * fator);
        return (int) (salarioAnual / 52); // Semanal
    }

    /**
     * Calcula taxa de Home-Grown (prêmio por formação local).
     */
    public double calcularTaxaHomeGrown(Player jogador, String paisLiga, String paisJogador) {
        if (paisJogador.equalsIgnoreCase(paisLiga)) {
            // Jogador do mesmo país = +20-50%
            return 1.2 + new Random().nextDouble() * 0.3;
        }
        return 1.0;
    }

    public void setAnoAtual(int ano) {
        this.anoAtual = ano;
    }
}
