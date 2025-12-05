package com.brasfm.model;

/**
 * Representa a formação tática de um time.
 * Exemplo: 4-4-2, 4-3-3, 3-5-2, etc.
 */
public class Formacao {
    private String nome;
    private int defensores;
    private int meias;
    private int atacantes;

    // Posições específicas no campo (coordenadas x, y de 0 a 100)
    private int[][] posicoesCampo;

    public static final Formacao F_4_4_2 = new Formacao("4-4-2", 4, 4, 2);
    public static final Formacao F_4_3_3 = new Formacao("4-3-3", 4, 3, 3);
    public static final Formacao F_3_5_2 = new Formacao("3-5-2", 3, 5, 2);
    public static final Formacao F_4_5_1 = new Formacao("4-5-1", 4, 5, 1);
    public static final Formacao F_5_3_2 = new Formacao("5-3-2", 5, 3, 2);
    public static final Formacao F_5_4_1 = new Formacao("5-4-1", 5, 4, 1);
    public static final Formacao F_4_2_3_1 = new Formacao("4-2-3-1", 4, 5, 1);
    public static final Formacao F_4_1_4_1 = new Formacao("4-1-4-1", 4, 5, 1);

    public Formacao(String nome, int defensores, int meias, int atacantes) {
        this.nome = nome;
        this.defensores = defensores;
        this.meias = meias;
        this.atacantes = atacantes;
        inicializarPosicoes();
    }

    private void inicializarPosicoes() {
        posicoesCampo = new int[11][2]; // 11 jogadores, x e y

        // Goleiro
        posicoesCampo[0] = new int[] { 50, 5 };

        // Defensores
        int posIndex = 1;
        for (int i = 0; i < defensores; i++) {
            int x = (i + 1) * 100 / (defensores + 1);
            posicoesCampo[posIndex++] = new int[] { x, 25 };
        }

        // Meias
        for (int i = 0; i < meias; i++) {
            int x = (i + 1) * 100 / (meias + 1);
            posicoesCampo[posIndex++] = new int[] { x, 55 };
        }

        // Atacantes
        for (int i = 0; i < atacantes; i++) {
            int x = (i + 1) * 100 / (atacantes + 1);
            posicoesCampo[posIndex++] = new int[] { x, 85 };
        }
    }

    public boolean isOfensiva() {
        return atacantes >= 3;
    }

    public boolean isDefensiva() {
        return defensores >= 5;
    }

    public static Formacao[] getFormacoesDisponiveis() {
        return new Formacao[] { F_4_4_2, F_4_3_3, F_3_5_2, F_4_5_1, F_5_3_2, F_5_4_1, F_4_2_3_1, F_4_1_4_1 };
    }

    // Getters
    public String getNome() {
        return nome;
    }

    public int getDefensores() {
        return defensores;
    }

    public int getMeias() {
        return meias;
    }

    public int getAtacantes() {
        return atacantes;
    }

    public int[][] getPosicoesCampo() {
        return posicoesCampo;
    }

    @Override
    public String toString() {
        return nome;
    }
}
