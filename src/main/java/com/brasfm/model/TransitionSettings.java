package com.brasfm.model;

/**
 * Configurações táticas para as TRANSIÇÕES (momento de ganhar/perder a bola).
 */
public class TransitionSettings {

    public enum TransicaoOfensiva {
        SEGURAR_POSSE("Segurar Posse", "Reorganiza antes de atacar", 0.3),
        NORMAL("Normal", "Avalia a situação", 0.5),
        CONTRA_ATAQUE("Contra-Ataque", "Busca profundidade imediata", 0.8),
        CONTRA_ATAQUE_RAPIDO("Contra-Ataque Rápido", "Jogada vertical imediata", 1.0);

        private final String nome;
        private final String descricao;
        private final double velocidade;

        TransicaoOfensiva(String nome, String descricao, double velocidade) {
            this.nome = nome;
            this.descricao = descricao;
            this.velocidade = velocidade;
        }

        public String getNome() {
            return nome;
        }

        public String getDescricao() {
            return descricao;
        }

        public double getVelocidade() {
            return velocidade;
        }
    }

    public enum TransicaoDefensiva {
        REAGRUPAR("Reagrupar", "Volta para posição defensiva", 0.3),
        NORMAL("Normal", "Equilibra reagrupar e pressionar", 0.5),
        PRESSAO_IMEDIATA("Pressão Imediata", "Tenta recuperar bola na hora", 0.8),
        CONTRA_PRESSAO("Contra-Pressão Total", "Todos pressionam imediatamente", 1.0);

        private final String nome;
        private final String descricao;
        private final double intensidade;

        TransicaoDefensiva(String nome, String descricao, double intensidade) {
            this.nome = nome;
            this.descricao = descricao;
            this.intensidade = intensidade;
        }

        public String getNome() {
            return nome;
        }

        public String getDescricao() {
            return descricao;
        }

        public double getIntensidade() {
            return intensidade;
        }
    }

    private TransicaoOfensiva transicaoOfensiva = TransicaoOfensiva.NORMAL;
    private TransicaoDefensiva transicaoDefensiva = TransicaoDefensiva.NORMAL;
    private int jogadoresNoContraAtaque = 3; // Quantos avançam no contra-ataque
    private boolean distribucaoRapida = false; // Goleiro distribui rápido

    public TransitionSettings() {
    }

    // Presets
    public static TransitionSettings estiloCauteloso() {
        TransitionSettings s = new TransitionSettings();
        s.transicaoOfensiva = TransicaoOfensiva.SEGURAR_POSSE;
        s.transicaoDefensiva = TransicaoDefensiva.REAGRUPAR;
        s.jogadoresNoContraAtaque = 2;
        return s;
    }

    public static TransitionSettings estiloVertiginoso() {
        TransitionSettings s = new TransitionSettings();
        s.transicaoOfensiva = TransicaoOfensiva.CONTRA_ATAQUE_RAPIDO;
        s.transicaoDefensiva = TransicaoDefensiva.CONTRA_PRESSAO;
        s.jogadoresNoContraAtaque = 5;
        s.distribucaoRapida = true;
        return s;
    }

    // Getters e Setters
    public TransicaoOfensiva getTransicaoOfensiva() {
        return transicaoOfensiva;
    }

    public void setTransicaoOfensiva(TransicaoOfensiva v) {
        this.transicaoOfensiva = v;
    }

    public TransicaoDefensiva getTransicaoDefensiva() {
        return transicaoDefensiva;
    }

    public void setTransicaoDefensiva(TransicaoDefensiva v) {
        this.transicaoDefensiva = v;
    }

    public int getJogadoresNoContraAtaque() {
        return jogadoresNoContraAtaque;
    }

    public void setJogadoresNoContraAtaque(int v) {
        this.jogadoresNoContraAtaque = v;
    }

    public boolean isDistribucaoRapida() {
        return distribucaoRapida;
    }

    public void setDistribucaoRapida(boolean v) {
        this.distribucaoRapida = v;
    }
}
