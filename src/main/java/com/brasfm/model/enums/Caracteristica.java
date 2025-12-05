package com.brasfm.model.enums;

/**
 * Características inatas dos jogadores.
 * Estas não mudam ao longo do jogo e influenciam o desempenho em determinadas
 * situações.
 */
public enum Caracteristica {
    // Características de goleiro
    COLOCACAO("Colocação", "Posicionamento do goleiro"),
    SAIDA_GOL("Saída do Gol", "Capacidade de sair do gol para interceptar"),
    REFLEXO("Reflexo", "Velocidade de reação do goleiro"),
    DEFESA_PENALTY("Defesa de Pênalty", "Habilidade em defender pênaltis"),

    // Características de jogadores de linha
    ARMACAO("Armação", "Criação de jogadas e lançamentos"),
    CABECEIO("Cabeceio", "Habilidade no jogo aéreo"),
    CRUZAMENTO("Cruzamento", "Precisão de cruzamentos"),
    DESARME("Desarme", "Capacidade de roubar a bola"),
    DRIBLE("Drible", "Habilidade de driblar adversários"),
    FINALIZACAO("Finalização", "Precisão e força de chutes"),
    MARCACAO("Marcação", "Posicionamento defensivo"),
    PASSE("Passe", "Precisão de passes"),
    RESISTENCIA("Resistência", "Vigor físico durante o jogo"),
    VELOCIDADE("Velocidade", "Rapidez de corrida");

    private final String nome;
    private final String descricao;

    Caracteristica(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isParaGoleiro() {
        return this == COLOCACAO || this == SAIDA_GOL || this == REFLEXO || this == DEFESA_PENALTY;
    }
}
