package com.brasfm.model.enums;

/**
 * Posições dos jogadores no campo.
 */
public enum Position {
    GOLEIRO("Goleiro", "GOL", true),
    ZAGUEIRO("Zagueiro", "ZAG", false),
    LATERAL_DIREITO("Lateral Direito", "LD", false),
    LATERAL_ESQUERDO("Lateral Esquerdo", "LE", false),
    VOLANTE("Volante", "VOL", false),
    MEIA("Meia", "MEI", false),
    MEIA_ATACANTE("Meia Atacante", "MA", false),
    PONTA_DIREITA("Ponta Direita", "PD", false),
    PONTA_ESQUERDA("Ponta Esquerda", "PE", false),
    CENTROAVANTE("Centroavante", "CA", false),
    ATACANTE("Atacante", "ATA", false);

    private final String nome;
    private final String sigla;
    private final boolean isGoleiro;

    Position(String nome, String sigla, boolean isGoleiro) {
        this.nome = nome;
        this.sigla = sigla;
        this.isGoleiro = isGoleiro;
    }

    public String getNome() {
        return nome;
    }

    public String getSigla() {
        return sigla;
    }

    public boolean isGoleiro() {
        return isGoleiro;
    }

    public boolean isDefensiva() {
        return this == ZAGUEIRO || this == LATERAL_DIREITO || this == LATERAL_ESQUERDO || this == VOLANTE;
    }

    public boolean isOfensiva() {
        return this == MEIA_ATACANTE || this == PONTA_DIREITA || this == PONTA_ESQUERDA ||
                this == CENTROAVANTE || this == ATACANTE;
    }
}
