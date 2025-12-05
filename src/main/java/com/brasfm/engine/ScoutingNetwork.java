package com.brasfm.engine;

import com.brasfm.model.*;
import java.util.*;

/**
 * Gerencia a rede de olheiros e o conhecimento do clube sobre jogadores
 * externos.
 */
public class ScoutingNetwork {

    private List<Scout> olheiros = new ArrayList<>();
    private Map<Player, PlayerKnowledge> conhecimento = new HashMap<>();
    private Map<Player, PlayerStats> estatisticas = new HashMap<>();

    // Orçamento de scouting
    private long orcamentoAnual;
    private long gastoAtual;

    // Regiões cobertas
    private Set<String> regioesAtivas = new HashSet<>();

    // Shortlist (jogadores de interesse)
    private List<Player> shortlist = new ArrayList<>();

    public ScoutingNetwork(long orcamentoAnual) {
        this.orcamentoAnual = orcamentoAnual;
        this.gastoAtual = 0;
    }

    /**
     * Contrata um novo olheiro.
     */
    public boolean contratarOlheiro(Scout olheiro) {
        int custoAnual = olheiro.getSalarioSemanal() * 52;
        if (gastoAtual + custoAnual > orcamentoAnual) {
            return false;
        }

        olheiros.add(olheiro);
        return true;
    }

    /**
     * Demite um olheiro.
     */
    public void demitirOlheiro(Scout olheiro) {
        olheiros.remove(olheiro);
    }

    /**
     * Envia olheiro para observar uma região.
     */
    public boolean enviarParaRegiao(Scout olheiro, String regiao) {
        int custViagem = Scout.calcularCustoViagem(olheiro.getRegiaoAtual(), regiao);
        if (gastoAtual + custViagem > orcamentoAnual) {
            return false;
        }

        gastoAtual += custViagem;
        olheiro.enviarParaRegiao(regiao);
        regioesAtivas.add(regiao.toLowerCase());
        return true;
    }

    /**
     * Inicia observação de um jogador específico.
     */
    public boolean observarJogador(Scout olheiro, Player jogador, int semanas) {
        int custo = semanas * 2000; // Custo por semana de observação
        if (gastoAtual + custo > orcamentoAnual) {
            return false;
        }

        if (!olheiro.iniciarMissao(jogador, semanas)) {
            return false;
        }

        gastoAtual += custo;
        return true;
    }

    /**
     * Processa passagem de semana para todos os olheiros.
     */
    public List<PlayerKnowledge> passarSemana() {
        List<PlayerKnowledge> relatoriosCompletos = new ArrayList<>();

        for (Scout s : olheiros) {
            PlayerKnowledge pk = s.passarSemana(conhecimento);
            if (pk != null) {
                relatoriosCompletos.add(pk);
            }
        }

        // Remove olheiros com contrato expirado
        olheiros.removeIf(s -> s.getSemanasContrato() <= 0);

        return relatoriosCompletos;
    }

    /**
     * Retorna nível de conhecimento sobre um jogador.
     */
    public PlayerKnowledge getConhecimento(Player jogador) {
        return conhecimento.get(jogador);
    }

    /**
     * Retorna estatísticas de um jogador (se disponíveis).
     */
    public PlayerStats getEstatisticas(Player jogador) {
        return estatisticas.get(jogador);
    }

    /**
     * Registra conhecimento total (jogador do próprio elenco).
     */
    public void registrarConhecimentoTotal(Player jogador) {
        PlayerKnowledge pk = conhecimento.computeIfAbsent(jogador, PlayerKnowledge::new);
        pk.setNivel(PlayerKnowledge.NivelConhecimento.TOTAL);
    }

    /**
     * Adiciona jogador à shortlist.
     */
    public void addShortlist(Player jogador) {
        if (!shortlist.contains(jogador)) {
            shortlist.add(jogador);
        }
    }

    /**
     * Remove jogador da shortlist.
     */
    public void removeShortlist(Player jogador) {
        shortlist.remove(jogador);
    }

    /**
     * Busca jogadores por critérios (limitado pelo conhecimento).
     */
    public List<Player> buscarJogadores(
            String regiao,
            com.brasfm.model.enums.Position posicao,
            int idadeMin,
            int idadeMax,
            int forcaMinEstimada) {
        List<Player> resultados = new ArrayList<>();

        // Só encontra jogadores em regiões com olheiros
        if (!regioesAtivas.contains(regiao.toLowerCase())) {
            return resultados; // Nenhum resultado - sem cobertura
        }

        // Filtra baseado no que sabemos
        for (Map.Entry<Player, PlayerKnowledge> entry : conhecimento.entrySet()) {
            Player p = entry.getKey();
            PlayerKnowledge pk = entry.getValue();

            // Filtros básicos (sempre conhecidos)
            if (posicao != null && p.getPosicaoOriginal() != posicao)
                continue;
            if (p.getIdade() < idadeMin || p.getIdade() > idadeMax)
                continue;

            // Força só pode ser filtrada se temos conhecimento
            if (pk.getNivel().getNivel() >= 1) {
                int[] forca = pk.getAtributosEstimados().get("forca");
                if (forca != null && forca[1] < forcaMinEstimada)
                    continue;
            }

            resultados.add(p);
        }

        // Ordena por nível de conhecimento
        resultados.sort((a, b) -> {
            PlayerKnowledge pkA = conhecimento.get(a);
            PlayerKnowledge pkB = conhecimento.get(b);
            return Integer.compare(
                    pkB != null ? pkB.getNivel().getNivel() : 0,
                    pkA != null ? pkA.getNivel().getNivel() : 0);
        });

        return resultados;
    }

    /**
     * Gera relatório de cobertura de scouting.
     */
    public String gerarRelatorioCobertura() {
        StringBuilder sb = new StringBuilder();

        sb.append("══════ REDE DE OLHEIROS ══════\n\n");
        sb.append("Orçamento: R$ ").append(String.format("%,d", orcamentoAnual)).append("\n");
        sb.append("Gasto: R$ ").append(String.format("%,d", gastoAtual)).append("\n");
        sb.append("Disponível: R$ ").append(String.format("%,d", orcamentoAnual - gastoAtual)).append("\n\n");

        sb.append("─── Olheiros (").append(olheiros.size()).append(") ───\n");
        for (Scout s : olheiros) {
            sb.append("• ").append(s.getNome()).append(" (").append(s.getHabilidade()).append("/20)\n");
            sb.append("  Região: ").append(s.getRegiaoAtual() != null ? s.getRegiaoAtual() : "Não alocado");
            sb.append(" | Eficiência: ").append(String.format("%.0f%%", s.getEficienciaNaRegiao() * 100));
            if (s.isEmMissao()) {
                sb.append(" | EM MISSÃO (").append(s.getSemanasRestantesMissao()).append(" sem)");
            }
            sb.append("\n");
        }

        sb.append("\n─── Regiões Cobertas ───\n");
        for (String regiao : regioesAtivas) {
            long olheirosNaRegiao = olheiros.stream()
                    .filter(s -> regiao.equals(s.getRegiaoAtual()))
                    .count();
            sb.append("• ").append(regiao).append(": ").append(olheirosNaRegiao).append(" olheiro(s)\n");
        }

        sb.append("\n─── Conhecimento ───\n");
        int total = conhecimento.size();
        int nivel0 = (int) conhecimento.values().stream()
                .filter(pk -> pk.getNivel() == PlayerKnowledge.NivelConhecimento.DESCONHECIDO).count();
        int nivel3 = (int) conhecimento.values().stream()
                .filter(pk -> pk.getNivel() == PlayerKnowledge.NivelConhecimento.TOTAL).count();

        sb.append("Jogadores rastreados: ").append(total).append("\n");
        sb.append("Conhecimento total: ").append(nivel3).append("\n");
        sb.append("Ainda desconhecidos: ").append(nivel0).append("\n");

        sb.append("\n─── Shortlist (").append(shortlist.size()).append(") ───\n");
        for (Player p : shortlist) {
            PlayerKnowledge pk = conhecimento.get(p);
            sb.append("• ").append(p.getNome()).append(" (").append(p.getIdade()).append(") - ");
            sb.append(pk != null ? pk.getNivel().getNome() : "Desconhecido").append("\n");
        }

        return sb.toString();
    }

    /**
     * Compara dois jogadores (Moneyball analysis).
     */
    public String compararJogadores(Player a, Player b) {
        PlayerStats statsA = estatisticas.get(a);
        PlayerStats statsB = estatisticas.get(b);

        StringBuilder sb = new StringBuilder();
        sb.append("══════ COMPARAÇÃO ANALÍTICA ══════\n\n");

        sb.append(String.format("%-20s | %-20s\n", a.getNome(), b.getNome()));
        sb.append("─".repeat(43)).append("\n");

        if (statsA != null && statsB != null) {
            sb.append(String.format("Gols/90: %-11.2f | %-11.2f\n",
                    statsA.getGolsPor90(), statsB.getGolsPor90()));
            sb.append(String.format("xG/90: %-13.2f | %-11.2f\n",
                    statsA.getxGPor90(), statsB.getxGPor90()));
            sb.append(String.format("G-xG: %-14+.2f | %-11+.2f\n",
                    statsA.getGolsMenosXG(), statsB.getGolsMenosXG()));
            sb.append(String.format("Conversão: %-9.1f%% | %-9.1f%%\n",
                    statsA.getTaxaConversao(), statsB.getTaxaConversao()));
            sb.append("\n");
            sb.append(String.format("Índice: %-13d | %-11d\n",
                    statsA.calcularIndiceOverperformance(), statsB.calcularIndiceOverperformance()));
        } else {
            sb.append("⚠️ Dados estatísticos insuficientes para comparação\n");
        }

        return sb.toString();
    }

    // Getters
    public List<Scout> getOlheiros() {
        return olheiros;
    }

    public List<Player> getShortlist() {
        return shortlist;
    }

    public long getOrcamentoAnual() {
        return orcamentoAnual;
    }

    public long getOrcamentoDisponivel() {
        return orcamentoAnual - gastoAtual;
    }

    public Set<String> getRegioesAtivas() {
        return regioesAtivas;
    }

    public void setOrcamentoAnual(long v) {
        this.orcamentoAnual = v;
    }
}
