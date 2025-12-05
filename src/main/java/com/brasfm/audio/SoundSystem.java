package com.brasfm.audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Sistema de áudio para efeitos sonoros do jogo.
 * Sons disponíveis: gol, gol adversário, contusão, expulsão, pênalti,
 * intervalo, fim de jogo.
 */
public class SoundSystem {

    public enum Som {
        GOL("sons/gol1.wav"),
        GOL_ADVERSARIO("sons/goladv.wav"),
        CONTUSAO("sons/contusao.wav"),
        EXPULSAO("sons/expulsao.wav"),
        PENALTI("sons/penalty.wav"),
        INTERVALO("sons/intervalo.wav"),
        FIM_JOGO("sons/fimjogo.wav");

        private final String arquivo;

        Som(String arquivo) {
            this.arquivo = arquivo;
        }

        public String getArquivo() {
            return arquivo;
        }
    }

    private Map<Som, Clip> clips;
    private boolean somAtivado;
    private float volume; // 0.0 a 1.0
    private String caminhoBase;

    public SoundSystem() {
        this.clips = new HashMap<>();
        this.somAtivado = true;
        this.volume = 0.8f;
        this.caminhoBase = "";
    }

    public SoundSystem(String caminhoBase) {
        this();
        this.caminhoBase = caminhoBase;
    }

    /**
     * Carrega todos os sons na memória.
     */
    public void carregarSons() {
        for (Som som : Som.values()) {
            try {
                String caminho = caminhoBase + som.getArquivo();
                File arquivo = new File(caminho);

                if (!arquivo.exists()) {
                    System.err.println("Arquivo de som não encontrado: " + caminho);
                    continue;
                }

                AudioInputStream audioStream = AudioSystem.getAudioInputStream(arquivo);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clips.put(som, clip);

            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                System.err.println("Erro ao carregar som " + som.name() + ": " + e.getMessage());
            }
        }

        System.out.println(
                "Sistema de áudio inicializado: " + clips.size() + "/" + Som.values().length + " sons carregados.");
    }

    /**
     * Reproduz um som.
     */
    public void tocar(Som som) {
        if (!somAtivado)
            return;

        Clip clip = clips.get(som);
        if (clip == null)
            return;

        // Para e reinicia o clip
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);

        // Ajusta volume
        ajustarVolume(clip);

        clip.start();
    }

    /**
     * Reproduz som de forma assíncrona (não bloqueia).
     */
    public void tocarAsync(Som som) {
        new Thread(() -> tocar(som)).start();
    }

    /**
     * Para todos os sons.
     */
    public void pararTodos() {
        for (Clip clip : clips.values()) {
            if (clip.isRunning()) {
                clip.stop();
            }
        }
    }

    /**
     * Ajusta o volume de um clip.
     */
    private void ajustarVolume(Clip clip) {
        try {
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            // Converte de 0-1 para dB
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();
            float range = max - min;

            float gain = (range * volume) + min;
            volumeControl.setValue(gain);

        } catch (IllegalArgumentException e) {
            // Controle de volume não disponível
        }
    }

    /**
     * Liga/desliga o som.
     */
    public void setSomAtivado(boolean ativado) {
        this.somAtivado = ativado;
        if (!ativado) {
            pararTodos();
        }
    }

    /**
     * Alterna entre ligado/desligado.
     */
    public void toggleSom() {
        setSomAtivado(!somAtivado);
    }

    /**
     * Define o volume (0.0 a 1.0).
     */
    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    /**
     * Libera recursos de áudio.
     */
    public void fechar() {
        for (Clip clip : clips.values()) {
            clip.close();
        }
        clips.clear();
    }

    // Métodos de conveniência para cada tipo de som
    public void tocarGol() {
        tocarAsync(Som.GOL);
    }

    public void tocarGolAdversario() {
        tocarAsync(Som.GOL_ADVERSARIO);
    }

    public void tocarContusao() {
        tocarAsync(Som.CONTUSAO);
    }

    public void tocarExpulsao() {
        tocarAsync(Som.EXPULSAO);
    }

    public void tocarPenalti() {
        tocarAsync(Som.PENALTI);
    }

    public void tocarIntervalo() {
        tocarAsync(Som.INTERVALO);
    }

    public void tocarFimJogo() {
        tocarAsync(Som.FIM_JOGO);
    }

    // Getters
    public boolean isSomAtivado() {
        return somAtivado;
    }

    public float getVolume() {
        return volume;
    }
}
