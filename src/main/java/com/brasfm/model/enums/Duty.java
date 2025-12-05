package com.brasfm.model.enums;

/**
 * Deveres táticos que definem a mentalidade ofensiva/defensiva do jogador.
 */
public enum Duty {
    DEFEND("Defender", "Foco em posicionamento defensivo, raramente avança", -0.3),
    SUPPORT("Apoiar", "Equilibrado entre ataque e defesa", 0.0),
    ATTACK("Atacar", "Avança agressivamente, menos foco defensivo", 0.3);

    private final String nome;
    private final String descricao;
    private final double modificadorPosicao; // negativo = recua, positivo = avança

    Duty(String nome, String descricao, double modificadorPosicao) {
        this.nome = nome;
        this.descricao = descricao;
        this.modificadorPosicao = modificadorPosicao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getModificadorPosicao() {
        return modificadorPosicao;
    }
}
