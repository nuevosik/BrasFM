package com.brasfm.model;

import com.brasfm.model.enums.CondicaoGramado;

/**
 * Representa um estádio de futebol.
 */
public class Stadium {
    private String nome;
    private int capacidade;
    private CondicaoGramado condicaoGramado;
    private int precoIngresso;

    // Expansão
    private boolean emObra;
    private int lugaresEmConstrucao;
    private int semanasParaConclusao;

    public Stadium(String nome, int capacidade) {
        this.nome = nome;
        this.capacidade = capacidade;
        this.condicaoGramado = CondicaoGramado.MUITO_BOM;
        this.precoIngresso = 50; // preço padrão
    }

    /**
     * Calcula a renda de um jogo baseado no público.
     */
    public int calcularRenda(int publico) {
        return publico * precoIngresso;
    }

    /**
     * Inicia uma expansão do estádio.
     */
    public void iniciarExpansao(int novosLugares, int custo) {
        if (!emObra) {
            this.emObra = true;
            this.lugaresEmConstrucao = novosLugares;
            this.semanasParaConclusao = Math.max(4, novosLugares / 2500);
        }
    }

    /**
     * Atualiza o progresso da obra.
     */
    public void passarSemana() {
        if (emObra) {
            semanasParaConclusao--;
            if (semanasParaConclusao <= 0) {
                capacidade += lugaresEmConstrucao;
                emObra = false;
                lugaresEmConstrucao = 0;
            }
        }
    }

    /**
     * Estima o público para um jogo baseado na importância e no time visitante.
     */
    public int estimarPublico(double fatorImportancia, int forcaVisitante) {
        double fatorVisitante = forcaVisitante / 100.0;
        double ocupacao = 0.3 + (0.4 * fatorImportancia) + (0.2 * fatorVisitante);
        ocupacao = Math.min(1.0, ocupacao);
        return (int) (capacidade * ocupacao);
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public CondicaoGramado getCondicaoGramado() {
        return condicaoGramado;
    }

    public void setCondicaoGramado(CondicaoGramado condicaoGramado) {
        this.condicaoGramado = condicaoGramado;
    }

    public int getPrecoIngresso() {
        return precoIngresso;
    }

    public void setPrecoIngresso(int precoIngresso) {
        this.precoIngresso = precoIngresso;
    }

    public boolean isEmObra() {
        return emObra;
    }

    @Override
    public String toString() {
        return nome + " (Cap: " + capacidade + ")";
    }
}
