package com.brasfm.model.enums;

/**
 * Tipos de marcação disponíveis.
 */
public enum TipoMarcacao {
    LEVE("Marcação Leve", 0.0, 0.05),
    PESADA("Marcação Pesada", 0.1, 0.15),
    MUITO_PESADA("Marcação Muito Pesada", 0.2, 0.30);

    private final String nome;
    private final double reducaoAtaquesAdversarios;
    private final double chanceFalta;

    TipoMarcacao(String nome, double reducaoAtaquesAdversarios, double chanceFalta) {
        this.nome = nome;
        this.reducaoAtaquesAdversarios = reducaoAtaquesAdversarios;
        this.chanceFalta = chanceFalta;
    }

    public String getNome() {
        return nome;
    }

    public double getReducaoAtaquesAdversarios() {
        return reducaoAtaquesAdversarios;
    }

    public double getChanceFalta() {
        return chanceFalta;
    }
}
