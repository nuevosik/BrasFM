package com.brasfm.model;

import com.brasfm.model.enums.ConcentrarAtaques;
import com.brasfm.model.enums.EstiloJogo;
import com.brasfm.model.enums.TipoMarcacao;

/**
 * Opções táticas para uma partida.
 */
public class Tatica {
    private Formacao formacao;
    private EstiloJogo estiloJogo;
    private TipoMarcacao tipoMarcacao;
    private ConcentrarAtaques concentrarAtaques;

    public Tatica() {
        this.formacao = Formacao.F_4_4_2;
        this.estiloJogo = EstiloJogo.EQUILIBRADO;
        this.tipoMarcacao = TipoMarcacao.LEVE;
        this.concentrarAtaques = ConcentrarAtaques.VARIADO;
    }

    public Tatica(Formacao formacao, EstiloJogo estiloJogo, TipoMarcacao tipoMarcacao,
            ConcentrarAtaques concentrarAtaques) {
        this.formacao = formacao;
        this.estiloJogo = estiloJogo;
        this.tipoMarcacao = tipoMarcacao;
        this.concentrarAtaques = concentrarAtaques;
    }

    /**
     * Calcula o peso ofensivo da tática.
     */
    public double getPesoOfensivo() {
        double base = estiloJogo.getPesoAtaque();
        if (formacao.isOfensiva()) {
            base += 0.1;
        }
        return Math.min(1.0, base);
    }

    /**
     * Calcula o peso defensivo da tática.
     */
    public double getPesoDefensivo() {
        double base = estiloJogo.getPesoDefesa();
        if (formacao.isDefensiva()) {
            base += 0.1;
        }
        base += tipoMarcacao.getReducaoAtaquesAdversarios();
        return Math.min(1.0, base);
    }

    // Getters e Setters
    public Formacao getFormacao() {
        return formacao;
    }

    public void setFormacao(Formacao formacao) {
        this.formacao = formacao;
    }

    public EstiloJogo getEstiloJogo() {
        return estiloJogo;
    }

    public void setEstiloJogo(EstiloJogo estiloJogo) {
        this.estiloJogo = estiloJogo;
    }

    public TipoMarcacao getTipoMarcacao() {
        return tipoMarcacao;
    }

    public void setTipoMarcacao(TipoMarcacao tipoMarcacao) {
        this.tipoMarcacao = tipoMarcacao;
    }

    public ConcentrarAtaques getConcentrarAtaques() {
        return concentrarAtaques;
    }

    public void setConcentrarAtaques(ConcentrarAtaques concentrarAtaques) {
        this.concentrarAtaques = concentrarAtaques;
    }

    @Override
    public String toString() {
        return formacao.getNome() + " - " + estiloJogo.getNome();
    }
}
