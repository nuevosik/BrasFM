package com.brasfm.model;

import java.util.*;

/**
 * Representa o conhecimento que o clube tem sobre um jogador.
 * Implementa o sistema de "Fog of War" para atributos.
 */
public class PlayerKnowledge {

    public enum NivelConhecimento {
        DESCONHECIDO(0, "Desconhecido", "Apenas nome e posição visíveis", 15),
        SUPERFICIAL(1, "Observação Superficial", "Físico visível, resto estimado", 8),
        APROFUNDADO(2, "Observação Aprofundada", "Técnicos mais precisos", 4),
        TOTAL(3, "Conhecimento Total", "Todos atributos conhecidos", 0);

        private final int nivel;
        private final String nome;
        private final String descricao;
        private final int margemErro; // +/- no valor real

        NivelConhecimento(int nivel, String nome, String descricao, int margemErro) {
            this.nivel = nivel;
            this.nome = nome;
            this.descricao = descricao;
            this.margemErro = margemErro;
        }

        public int getNivel() {
            return nivel;
        }

        public String getNome() {
            return nome;
        }

        public String getDescricao() {
            return descricao;
        }

        public int getMargemErro() {
            return margemErro;
        }
    }

    private Player jogador;
    private NivelConhecimento nivel = NivelConhecimento.DESCONHECIDO;
    private int observacoes = 0;
    private Date ultimaObservacao;

    // Valores estimados (com margem de erro)
    private Map<String, int[]> atributosEstimados = new HashMap<>();

    // Relatório qualitativo do olheiro
    private List<String> relatorioOlheiro = new ArrayList<>();

    // Dados estatísticos (se disponíveis)
    private PlayerStats estatisticas;

    // Potencial estimado (1-5 estrelas) com margem de erro
    private double potencialEstimado = 0;
    private double margemPotencial = 2.5; // Inicialmente totalmente incerto

    public PlayerKnowledge(Player jogador) {
        this.jogador = jogador;
    }

    /**
     * Processa uma observação do jogador (jogo assistido ou olheiro).
     */
    public void processarObservacao(Scout olheiro, boolean jogoCompleto) {
        observacoes++;
        ultimaObservacao = new Date();

        // Avança nível de conhecimento
        if (observacoes >= 10 && nivel.getNivel() < 3) {
            nivel = NivelConhecimento.TOTAL;
        } else if (observacoes >= 5 && nivel.getNivel() < 2) {
            nivel = NivelConhecimento.APROFUNDADO;
        } else if (observacoes >= 1 && nivel.getNivel() < 1) {
            nivel = NivelConhecimento.SUPERFICIAL;
        }

        // Atualiza estimativas
        atualizarEstimativas(olheiro);

        // Gera relatório qualitativo
        if (olheiro != null && jogoCompleto) {
            gerarRelatorioOlheiro(olheiro);
        }
    }

    /**
     * Atualiza as estimativas de atributos baseado no nível de conhecimento.
     */
    private void atualizarEstimativas(Scout olheiro) {
        Random r = new Random();
        int margem = nivel.getMargemErro();

        // Viés do olheiro afeta a estimativa
        double viesTolheiro = olheiro != null ? olheiro.getVies() : 0;

        // Atributos físicos são mais fáceis de ver
        int margemFisico = (int) (margem * 0.7);
        int margemTecnico = margem;
        int margemMental = (int) (margem * 1.3); // Mais difícil de avaliar

        // Gera estimativas com erro
        // Formato: [min, max] estimado

        // Físicos
        atributosEstimados.put("velocidade", gerarEstimativa(70, margemFisico, viesTolheiro, r));
        atributosEstimados.put("forca", gerarEstimativa(70, margemFisico, viesTolheiro, r));
        atributosEstimados.put("resistencia", gerarEstimativa(70, margemFisico, viesTolheiro, r));

        // Técnicos
        atributosEstimados.put("finalizacao",
                gerarEstimativa(jogador.getFinalizacao(), margemTecnico, viesTolheiro, r));
        atributosEstimados.put("passe", gerarEstimativa(jogador.getPasse(), margemTecnico, viesTolheiro, r));
        atributosEstimados.put("tecnica", gerarEstimativa(jogador.getTecnica(), margemTecnico, viesTolheiro, r));

        // Mentais (mais difíceis de ver)
        atributosEstimados.put("decisoes", gerarEstimativa(60, margemMental, viesTolheiro, r));
        atributosEstimados.put("compostura", gerarEstimativa(60, margemMental, viesTolheiro, r));

        // Potencial
        double potencialReal = jogador.getCpe() / 20.0; // 1-5 estrelas
        int variacaoPotencial = r.nextInt(margem / 3 + 1);
        potencialEstimado = potencialReal + (r.nextDouble() - 0.5) * variacaoPotencial * 0.5;
        potencialEstimado = Math.max(1, Math.min(5, potencialEstimado));
        margemPotencial = margem / 6.0;
    }

    private int[] gerarEstimativa(int valorReal, int margem, double vies, Random r) {
        // Aplica viés do olheiro (-0.3 a 0.3)
        int ajusteVies = (int) (vies * margem);

        // Gera erro na estimativa
        int erro = r.nextInt(margem + 1) - margem / 2;
        int estimativa = valorReal + erro + ajusteVies;

        int min = Math.max(1, estimativa - margem);
        int max = Math.min(100, estimativa + margem);

        return new int[] { min, max };
    }

    /**
     * Gera relatório qualitativo do olheiro.
     */
    private void gerarRelatorioOlheiro(Scout olheiro) {
        relatorioOlheiro.clear();
        Random r = new Random();
        PlayerPersonality pers = jogador.getPersonality();

        // Comentários sobre pontos fortes
        if (jogador.getForca() > 75) {
            relatorioOlheiro.add("Jogador muito forte fisicamente.");
        }
        if (jogador.getFinalizacao() > 75) {
            relatorioOlheiro.add("Excelente finalizador.");
        }
        if (jogador.getPasse() > 75) {
            relatorioOlheiro.add("Ótima qualidade de passe.");
        }

        // Comentários sobre personalidade (parcialmente visíveis)
        if (pers != null) {
            if (pers.getConsistencia() < 10 && r.nextBoolean()) {
                relatorioOlheiro.add("⚠️ Parece inconsistente - oscila muito.");
            }
            if (pers.getJogosImportantes() < 8 && r.nextBoolean()) {
                relatorioOlheiro.add("⚠️ Desaparece em jogos grandes.");
            }
            if (pers.getJogosImportantes() > 15 && r.nextBoolean()) {
                relatorioOlheiro.add("✅ Cresce em jogos importantes.");
            }
            if (pers.getProfissionalismo() < 10 && r.nextBoolean()) {
                relatorioOlheiro.add("⚠️ Ouvir rumores de problemas extra-campo.");
            }
            if (pers.getLideranca() > 15) {
                relatorioOlheiro.add("✅ Líder nato no vestiário.");
            }
            if (pers.getTemperamento() < 8) {
                relatorioOlheiro.add("⚠️ Temperamento explosivo - muitos cartões.");
            }
        }

        // Viés do olheiro afeta relatório
        if (olheiro.getPreferenciaTatica() == Scout.PreferenciaTatica.FISICO) {
            if (jogador.getForca() > 60) {
                relatorioOlheiro.add("(Olheiro valoriza: bom porte físico)");
            }
        } else if (olheiro.getPreferenciaTatica() == Scout.PreferenciaTatica.TECNICO) {
            if (jogador.getTecnica() > 60) {
                relatorioOlheiro.add("(Olheiro valoriza: refinado tecnicamente)");
            }
        }
    }

    /**
     * Retorna string formatada para display de atributo estimado.
     */
    public String getAtributoDisplay(String atributo) {
        if (nivel == NivelConhecimento.DESCONHECIDO) {
            return "???";
        }

        int[] faixa = atributosEstimados.get(atributo);
        if (faixa == null) {
            return "---";
        }

        if (nivel == NivelConhecimento.TOTAL) {
            // Mostra valor real
            return String.valueOf((faixa[0] + faixa[1]) / 2);
        }

        if (faixa[1] - faixa[0] <= 3) {
            return String.valueOf((faixa[0] + faixa[1]) / 2);
        }

        return faixa[0] + "-" + faixa[1];
    }

    /**
     * Retorna potencial estimado como estrelas.
     */
    public String getPotencialDisplay() {
        if (nivel == NivelConhecimento.DESCONHECIDO) {
            return "☆☆☆☆☆";
        }

        StringBuilder sb = new StringBuilder();
        int cheias = (int) potencialEstimado;
        boolean meia = (potencialEstimado - cheias) >= 0.5;

        for (int i = 0; i < cheias; i++)
            sb.append("★");
        if (meia)
            sb.append("½");
        while (sb.length() < 5)
            sb.append("☆");

        if (margemPotencial > 0.5) {
            sb.append(" ±").append(String.format("%.1f", margemPotencial));
        }

        return sb.toString();
    }

    // Getters
    public Player getJogador() {
        return jogador;
    }

    public NivelConhecimento getNivel() {
        return nivel;
    }

    public int getObservacoes() {
        return observacoes;
    }

    public Date getUltimaObservacao() {
        return ultimaObservacao;
    }

    public List<String> getRelatorioOlheiro() {
        return relatorioOlheiro;
    }

    public Map<String, int[]> getAtributosEstimados() {
        return atributosEstimados;
    }

    public void setNivel(NivelConhecimento nivel) {
        this.nivel = nivel;
    }
}
