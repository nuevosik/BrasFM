package com.brasfm.persistence;

import com.brasfm.model.Team;
import com.brasfm.championship.League;
import java.time.LocalDateTime;

/**
 * Container que armazena todo o estado do jogo para serialização.
 */
public class SaveGame {

    // Metadados do save
    private String nomeArquivo;
    private LocalDateTime dataSave;
    private String versaoJogo;

    // Estado do jogo
    private Team timeJogador;
    private League liga;
    private int rodadaAtual;
    private int semanaAtual;

    // Preview para lista de saves
    private String previewTexto;

    public SaveGame() {
        this.versaoJogo = "1.0.0";
        this.dataSave = LocalDateTime.now();
    }

    public SaveGame(Team timeJogador, League liga) {
        this();
        this.timeJogador = timeJogador;
        this.liga = liga;
        this.rodadaAtual = liga != null ? liga.getRodadaAtual() : 0;
        atualizarPreview();
    }

    /**
     * Atualiza o texto de preview para exibição na lista de saves.
     */
    public void atualizarPreview() {
        if (timeJogador != null) {
            String nomeTime = timeJogador.getNome();
            int posicao = calcularPosicao();
            this.previewTexto = String.format("%s - %dª Rodada (Pos: %dº)",
                    nomeTime, rodadaAtual, posicao);
        } else {
            this.previewTexto = "Novo Jogo";
        }
    }

    /**
     * Calcula a posição do time na tabela.
     */
    private int calcularPosicao() {
        if (liga == null || timeJogador == null)
            return 0;

        var classificacao = liga.getClassificacao();
        for (int i = 0; i < classificacao.size(); i++) {
            if (classificacao.get(i).getNome().equals(timeJogador.getNome())) {
                return i + 1;
            }
        }
        return 0;
    }

    // Getters e Setters

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public LocalDateTime getDataSave() {
        return dataSave;
    }

    public void setDataSave(LocalDateTime dataSave) {
        this.dataSave = dataSave;
    }

    public String getVersaoJogo() {
        return versaoJogo;
    }

    public void setVersaoJogo(String versaoJogo) {
        this.versaoJogo = versaoJogo;
    }

    public Team getTimeJogador() {
        return timeJogador;
    }

    public void setTimeJogador(Team timeJogador) {
        this.timeJogador = timeJogador;
    }

    public League getLiga() {
        return liga;
    }

    public void setLiga(League liga) {
        this.liga = liga;
    }

    public int getRodadaAtual() {
        return rodadaAtual;
    }

    public void setRodadaAtual(int rodadaAtual) {
        this.rodadaAtual = rodadaAtual;
    }

    public int getSemanaAtual() {
        return semanaAtual;
    }

    public void setSemanaAtual(int semanaAtual) {
        this.semanaAtual = semanaAtual;
    }

    public String getPreviewTexto() {
        return previewTexto;
    }

    public void setPreviewTexto(String previewTexto) {
        this.previewTexto = previewTexto;
    }

    @Override
    public String toString() {
        return previewTexto != null ? previewTexto : "SaveGame";
    }
}
