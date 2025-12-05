package com.brasfm.model;

import java.util.*;

/**
 * Representa um olheiro (scout) do clube.
 */
public class Scout {

    public enum PreferenciaTatica {
        FISICO("Valoriza Físico", "Tende a superestimar jogadores fortes"),
        TECNICO("Valoriza Técnica", "Prefere jogadores refinados"),
        MENTAL("Valoriza Mental", "Foca em inteligência tática"),
        EQUILIBRADO("Equilibrado", "Avaliação balanceada");

        private final String nome;
        private final String descricao;

        PreferenciaTatica(String nome, String descricao) {
            this.nome = nome;
            this.descricao = descricao;
        }

        public String getNome() {
            return nome;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    private String nome;
    private int habilidade; // 1-20, precisão das avaliações
    private int adaptabilidade; // 1-20, velocidade de adaptação a novas regiões
    private int julgamentoJovens; // 1-20, precisão em avaliar potencial
    private int julgamentoMentais; // 1-20, capacidade de avaliar atributos mentais

    private PreferenciaTatica preferencia;

    // Regionalização
    private Set<String> regioesDominio; // Regiões que conhece bem
    private String regiaoAtual; // Onde está baseado
    private int semanasNaRegiao = 0; // Tempo na região atual
    private static final int SEMANAS_ADAPTAR = 12; // 3 meses para adaptar

    // Estado
    private int salarioSemanal;
    private int semanasContrato;
    private boolean emMissao = false;
    private Player alvoAtual;
    private int semanasRestantesMissao = 0;

    public Scout(String nome, int habilidade) {
        this.nome = nome;
        this.habilidade = Math.max(1, Math.min(20, habilidade));
        this.adaptabilidade = 10 + new Random().nextInt(8);
        this.julgamentoJovens = 8 + new Random().nextInt(10);
        this.julgamentoMentais = 8 + new Random().nextInt(10);
        this.preferencia = PreferenciaTatica.values()[new Random().nextInt(4)];
        this.regioesDominio = new HashSet<>();
        this.salarioSemanal = habilidade * 300;
        this.semanasContrato = 52;
    }

    /**
     * Adiciona região de domínio (onde cresceu/trabalhou).
     */
    public void addRegiaoDominio(String regiao) {
        regioesDominio.add(regiao.toLowerCase());
    }

    /**
     * Envia olheiro para uma nova região.
     */
    public void enviarParaRegiao(String regiao) {
        if (!regiao.equalsIgnoreCase(regiaoAtual)) {
            semanasNaRegiao = 0;
        }
        regiaoAtual = regiao;
    }

    /**
     * Calcula a eficiência do olheiro na região atual.
     * 
     * @return 0.3 a 1.0
     */
    public double getEficienciaNaRegiao() {
        if (regiaoAtual == null)
            return 0.5;

        // Se é região de domínio, sempre eficiente
        if (regioesDominio.contains(regiaoAtual.toLowerCase())) {
            return 1.0;
        }

        // Senão, depende do tempo na região e adaptabilidade
        double progresso = (double) semanasNaRegiao / SEMANAS_ADAPTAR;
        progresso = Math.min(1.0, progresso);

        // Adaptabilidade acelera o processo
        double fatorAdaptabilidade = 0.5 + (adaptabilidade / 40.0);
        progresso *= fatorAdaptabilidade;

        return 0.3 + progresso * 0.7;
    }

    /**
     * Calcula viés do olheiro (afeta estimativas).
     * 
     * @return -0.3 a 0.3 (negativo = subestima, positivo = superestima)
     */
    public double getVies() {
        // Viés baseado na preferência
        return (new Random().nextDouble() - 0.5) * 0.3 * (1 - habilidade / 40.0);
    }

    /**
     * Inicia missão de observação de um jogador.
     */
    public boolean iniciarMissao(Player jogador, int semanas) {
        if (emMissao)
            return false;

        emMissao = true;
        alvoAtual = jogador;
        semanasRestantesMissao = semanas;
        return true;
    }

    /**
     * Processa passagem de semana.
     */
    public PlayerKnowledge passarSemana(Map<Player, PlayerKnowledge> conhecimento) {
        semanasContrato--;
        semanasNaRegiao++;

        if (emMissao && semanasRestantesMissao > 0) {
            semanasRestantesMissao--;

            // Cada semana gera uma observação
            if (alvoAtual != null) {
                PlayerKnowledge pk = conhecimento.computeIfAbsent(alvoAtual, PlayerKnowledge::new);
                pk.processarObservacao(this, semanasRestantesMissao % 2 == 0);

                if (semanasRestantesMissao == 0) {
                    emMissao = false;
                    Player temp = alvoAtual;
                    alvoAtual = null;
                    return conhecimento.get(temp);
                }
            }
        }

        return null;
    }

    /**
     * Gera relatório completo sobre um jogador.
     */
    public String gerarRelatorioCompleto(Player jogador, PlayerKnowledge conhecimento) {
        StringBuilder sb = new StringBuilder();

        sb.append("═══════════════════════════════════════\n");
        sb.append("       RELATÓRIO DE OBSERVAÇÃO\n");
        sb.append("═══════════════════════════════════════\n\n");

        sb.append("Jogador: ").append(jogador.getNome()).append("\n");
        sb.append("Idade: ").append(jogador.getIdade()).append(" | ");
        sb.append("Posição: ").append(jogador.getPosicaoOriginal().getNome()).append("\n");
        sb.append("Clube: ").append("---").append("\n\n");

        sb.append("Olheiro: ").append(nome).append("\n");
        sb.append("Habilidade: ").append(habilidade).append("/20 | ");
        sb.append("Preferência: ").append(preferencia.getNome()).append("\n");
        sb.append("Observações: ").append(conhecimento.getObservacoes()).append("\n\n");

        sb.append("───────── ATRIBUTOS ─────────\n");
        sb.append("Finalização: ").append(conhecimento.getAtributoDisplay("finalizacao")).append("\n");
        sb.append("Passe: ").append(conhecimento.getAtributoDisplay("passe")).append("\n");
        sb.append("Técnica: ").append(conhecimento.getAtributoDisplay("tecnica")).append("\n");
        sb.append("Força: ").append(conhecimento.getAtributoDisplay("forca")).append("\n");
        sb.append("Velocidade: ").append(conhecimento.getAtributoDisplay("velocidade")).append("\n\n");

        sb.append("Potencial: ").append(conhecimento.getPotencialDisplay()).append("\n\n");

        sb.append("───────── OBSERVAÇÕES ─────────\n");
        for (String obs : conhecimento.getRelatorioOlheiro()) {
            sb.append("• ").append(obs).append("\n");
        }

        if (preferencia != PreferenciaTatica.EQUILIBRADO) {
            sb.append("\n⚠️ Atenção: Este olheiro ").append(preferencia.getDescricao().toLowerCase()).append(".\n");
        }

        sb.append("\n═══════════════════════════════════════\n");

        return sb.toString();
    }

    /**
     * Calcula custo de enviar olheiro para uma região.
     */
    public static int calcularCustoViagem(String regiaoOrigem, String regiaoDestino) {
        // Custo base por semana
        int custoBase = 5000;

        // Multiplica pela distância (simulada)
        Map<String, Integer> custosPorRegiao = new HashMap<>();
        custosPorRegiao.put("brasil", 0);
        custosPorRegiao.put("argentina", 2000);
        custosPorRegiao.put("europa", 8000);
        custosPorRegiao.put("africa", 6000);
        custosPorRegiao.put("asia", 10000);

        int custoDestino = custosPorRegiao.getOrDefault(regiaoDestino.toLowerCase(), 5000);

        return custoBase + custoDestino;
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public int getHabilidade() {
        return habilidade;
    }

    public int getAdaptabilidade() {
        return adaptabilidade;
    }

    public int getJulgamentoJovens() {
        return julgamentoJovens;
    }

    public int getJulgamentoMentais() {
        return julgamentoMentais;
    }

    public PreferenciaTatica getPreferenciaTatica() {
        return preferencia;
    }

    public Set<String> getRegioesDominio() {
        return regioesDominio;
    }

    public String getRegiaoAtual() {
        return regiaoAtual;
    }

    public boolean isEmMissao() {
        return emMissao;
    }

    public Player getAlvoAtual() {
        return alvoAtual;
    }

    public int getSemanasRestantesMissao() {
        return semanasRestantesMissao;
    }

    public int getSalarioSemanal() {
        return salarioSemanal;
    }

    public int getSemanasContrato() {
        return semanasContrato;
    }

    @Override
    public String toString() {
        return nome + " (" + habilidade + "/20 - " + preferencia.getNome() + ")";
    }
}
