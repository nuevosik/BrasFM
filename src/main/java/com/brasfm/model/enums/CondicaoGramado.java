package com.brasfm.model.enums;

/**
 * Condição do gramado do estádio.
 */
public enum CondicaoGramado {
    EXCELENTE("Excelente", 1.0),
    MUITO_BOM("Muito Bom", 0.98),
    RUIM("Ruim", 0.85),
    PESSIMO("Péssimo", 0.70);

    private final String nome;
    private final double fatorTecnica;

    CondicaoGramado(String nome, double fatorTecnica) {
        this.nome = nome;
        this.fatorTecnica = fatorTecnica;
    }

    public String getNome() {
        return nome;
    }

    /**
     * Fator que afeta jogadores técnicos (1.0 = sem penalidade).
     */
    public double getFatorTecnica() {
        return fatorTecnica;
    }
}
