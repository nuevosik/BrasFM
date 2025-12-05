package com.brasfm.model.enums;

/**
 * Estilos de jogo disponíveis para o time.
 */
public enum EstiloJogo {
    EQUILIBRADO("Jogo Equilibrado", "Balanceado entre defesa e ataque", 0.5, 0.5),
    ATAQUE_TOTAL("Ataque Total", "Foco no ataque, defesa vulnerável", 0.7, 0.3),
    CONTRA_ATAQUE("Contra-ataque", "Jogo recuado com contra-ataques rápidos", 0.3, 0.7);

    private final String nome;
    private final String descricao;
    private final double pesoAtaque;
    private final double pesoDefesa;

    EstiloJogo(String nome, String descricao, double pesoAtaque, double pesoDefesa) {
        this.nome = nome;
        this.descricao = descricao;
        this.pesoAtaque = pesoAtaque;
        this.pesoDefesa = pesoDefesa;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getPesoAtaque() {
        return pesoAtaque;
    }

    public double getPesoDefesa() {
        return pesoDefesa;
    }
}
