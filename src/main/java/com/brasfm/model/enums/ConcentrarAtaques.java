package com.brasfm.model.enums;

/**
 * Onde concentrar os ataques do time.
 */
public enum ConcentrarAtaques {
    VARIADO("Ataques Variados", 0.33, 0.34, 0.33),
    PELO_MEIO("Concentrar pelo Meio", 0.15, 0.70, 0.15),
    PELAS_LATERAIS("Concentrar pelas Laterais", 0.35, 0.30, 0.35);

    private final String nome;
    private final double pesoEsquerda;
    private final double pesoMeio;
    private final double pesoDireita;

    ConcentrarAtaques(String nome, double pesoEsquerda, double pesoMeio, double pesoDireita) {
        this.nome = nome;
        this.pesoEsquerda = pesoEsquerda;
        this.pesoMeio = pesoMeio;
        this.pesoDireita = pesoDireita;
    }

    public String getNome() {
        return nome;
    }

    public double getPesoEsquerda() {
        return pesoEsquerda;
    }

    public double getPesoMeio() {
        return pesoMeio;
    }

    public double getPesoDireita() {
        return pesoDireita;
    }
}
