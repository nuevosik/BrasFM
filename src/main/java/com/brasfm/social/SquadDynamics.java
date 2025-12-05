package com.brasfm.social;

import com.brasfm.model.*;
import java.util.*;

/**
 * Sistema de grupos sociais e hierarquias no vestiário.
 */
public class SquadDynamics {

    public enum TipoGrupo {
        NACIONALIDADE("Nacionalidade", "Jogadores do mesmo país"),
        VETERANOS("Veteranos", "Jogadores com 3+ anos no clube"),
        JOVENS("Jovens", "Jogadores sub-23"),
        CONTRATACOES("Contratações", "Jogadores recém-chegados"),
        ESTRELAS("Estrelas", "Jogadores de destaque");

        private final String nome;
        private final String descricao;

        TipoGrupo(String nome, String descricao) {
            this.nome = nome;
            this.descricao = descricao;
        }

        public String getNome() {
            return nome;
        }
    }

    public enum PapelVestiario {
        CAPITAO(20, "Braçadeira e voz principal"),
        LIDER_EQUIPA(15, "Influência significativa"),
        VETERANO(10, "Voz experiente"),
        INFLUENTE(8, "Respeitado pelo grupo"),
        REGULAR(5, "Membro comum"),
        NOVATO(2, "Ainda se adaptando"),
        ISOLADO(0, "Fora dos grupos");

        private final int influencia;
        private final String descricao;

        PapelVestiario(int influencia, String descricao) {
            this.influencia = influencia;
            this.descricao = descricao;
        }

        public int getInfluencia() {
            return influencia;
        }
    }

    // Grupos sociais
    private Map<String, List<Player>> gruposPorNacionalidade = new HashMap<>();
    private List<Player> veteranos = new ArrayList<>();
    private List<Player> jovens = new ArrayList<>();
    private List<Player> contratacoes = new ArrayList<>();

    // Hierarquia
    private Map<Player, PapelVestiario> papeis = new HashMap<>();
    private Player capitao;
    private List<Player> lideresEquipa = new ArrayList<>();

    // Coesão tática (0-100)
    private int coesaoTatica = 50;

    // Conflitos ativos
    private Map<Player, List<Player>> conflitos = new HashMap<>();

    // Semanas no clube
    private Map<Player, Integer> semanasNoClube = new HashMap<>();

    /**
     * Atualiza os grupos sociais baseado no elenco.
     */
    public void atualizarGrupos(List<Player> elenco) {
        // Limpa
        gruposPorNacionalidade.clear();
        veteranos.clear();
        jovens.clear();

        for (Player p : elenco) {
            int semanas = semanasNoClube.getOrDefault(p, 0);

            // Veteranos: 3+ anos (156 semanas)
            if (semanas >= 156) {
                veteranos.add(p);
            }

            // Jovens: sub-23
            if (p.getIdade() < 23) {
                jovens.add(p);
            }

            // Contratações recentes: < 12 semanas
            if (semanas < 12) {
                if (!contratacoes.contains(p)) {
                    contratacoes.add(p);
                }
            } else {
                contratacoes.remove(p);
            }

            // Atribui papel se não tiver
            if (!papeis.containsKey(p)) {
                atribuirPapelInicial(p);
            }
        }
    }

    private void atribuirPapelInicial(Player p) {
        int semanas = semanasNoClube.getOrDefault(p, 0);
        PlayerPersonality pers = p.getPersonality();

        if (semanas < 12) {
            papeis.put(p, PapelVestiario.NOVATO);
        } else if (semanas >= 156 && pers != null && pers.getLideranca() > 12) {
            papeis.put(p, PapelVestiario.VETERANO);
        } else if (pers != null && pers.getLideranca() > 15) {
            papeis.put(p, PapelVestiario.INFLUENTE);
        } else {
            papeis.put(p, PapelVestiario.REGULAR);
        }
    }

    /**
     * Define o capitão do time.
     */
    public void definirCapitao(Player novoCapitao) {
        Player antigoCapitao = this.capitao;

        // Remove papel antigo
        if (antigoCapitao != null) {
            papeis.put(antigoCapitao, PapelVestiario.VETERANO);

            // Alienar capitão pode causar problema
            PlayerPersonality pers = antigoCapitao.getPersonality();
            if (pers != null && pers.getLideranca() > 15) {
                // Espalha descontentamento
                for (Player seguidor : getSeguidores(antigoCapitao)) {
                    MoraleSystem.TipoEvento.valueOf("CRITICA_PUBLICA");
                }
            }
        }

        this.capitao = novoCapitao;
        papeis.put(novoCapitao, PapelVestiario.CAPITAO);

        if (!lideresEquipa.contains(novoCapitao)) {
            lideresEquipa.add(novoCapitao);
        }
    }

    /**
     * Retorna seguidores de um líder (mesmo grupo social).
     */
    public List<Player> getSeguidores(Player lider) {
        List<Player> seguidores = new ArrayList<>();

        // Adiciona do mesmo grupo de nacionalidade
        for (List<Player> grupo : gruposPorNacionalidade.values()) {
            if (grupo.contains(lider)) {
                seguidores.addAll(grupo);
            }
        }

        // Veteranos seguem veteranos
        if (veteranos.contains(lider)) {
            seguidores.addAll(veteranos);
        }

        seguidores.remove(lider);
        return seguidores;
    }

    /**
     * Processa contratação de novo jogador.
     */
    public String processarContratacao(Player novoJogador) {
        semanasNoClube.put(novoJogador, 0);
        contratacoes.add(novoJogador);
        papeis.put(novoJogador, PapelVestiario.NOVATO);

        // Impacto na coesão
        int impacto = -3;

        // Muitas contratações = problema maior
        if (contratacoes.size() > 5) {
            impacto = -8;
        }

        coesaoTatica = Math.max(20, coesaoTatica + impacto);

        return String.format("🆕 %s chegou ao clube. Coesão: %d (-%.0f)",
                novoJogador.getNome(), coesaoTatica, Math.abs(impacto * 1.0));
    }

    /**
     * Processa saída de jogador.
     */
    public List<String> processarSaida(Player jogador, boolean vendaForcada) {
        List<String> eventos = new ArrayList<>();

        // Se era líder, pode causar revolta
        PapelVestiario papel = papeis.getOrDefault(jogador, PapelVestiario.REGULAR);

        if (vendaForcada && papel.getInfluencia() >= 10) {
            List<Player> seguidores = getSeguidores(jogador);
            eventos.add("⚠️ " + jogador.getNome() + " era um líder! " +
                    seguidores.size() + " jogadores afetados.");

            for (Player s : seguidores) {
                // Seguidores ficam chateados
                conflitos.computeIfAbsent(s, k -> new ArrayList<>());
            }

            coesaoTatica = Math.max(10, coesaoTatica - 15);
        }

        // Limpa
        papeis.remove(jogador);
        semanasNoClube.remove(jogador);
        contratacoes.remove(jogador);
        lideresEquipa.remove(jogador);
        if (capitao == jogador)
            capitao = null;

        return eventos;
    }

    /**
     * Processa passagem de semana.
     */
    public void processarSemana() {
        // Incrementa tempo no clube
        for (Player p : semanasNoClube.keySet()) {
            semanasNoClube.merge(p, 1, Integer::sum);
        }

        // Coesão melhora naturalmente
        if (contratacoes.size() <= 2) {
            coesaoTatica = Math.min(100, coesaoTatica + 1);
        }

        // Novatos se integram após 12 semanas
        Iterator<Player> it = contratacoes.iterator();
        while (it.hasNext()) {
            Player p = it.next();
            if (semanasNoClube.getOrDefault(p, 0) >= 12) {
                it.remove();
                papeis.put(p, PapelVestiario.REGULAR);
            }
        }
    }

    /**
     * Calcula modificador de coesão para partidas.
     */
    public double getModificadorCoesao() {
        // Coesão afeta entrosamento em campo
        if (coesaoTatica >= 80)
            return 0.08;
        if (coesaoTatica >= 60)
            return 0.03;
        if (coesaoTatica >= 40)
            return 0.00;
        if (coesaoTatica >= 20)
            return -0.05;
        return -0.12; // Vestiário em crise
    }

    /**
     * Verifica estabilidade do vestiário.
     */
    public boolean vestiarioEstavel() {
        return conflitos.isEmpty() && coesaoTatica >= 50 && capitao != null;
    }

    // Getters
    public int getCoesaoTatica() {
        return coesaoTatica;
    }

    public Player getCapitao() {
        return capitao;
    }

    public List<Player> getLideresEquipa() {
        return lideresEquipa;
    }

    public List<Player> getVeteranos() {
        return veteranos;
    }

    public List<Player> getContratacoes() {
        return contratacoes;
    }

    public PapelVestiario getPapel(Player p) {
        return papeis.getOrDefault(p, PapelVestiario.REGULAR);
    }

    public int getSemanasNoClube(Player p) {
        return semanasNoClube.getOrDefault(p, 0);
    }

    public void registrarJogador(Player p) {
        semanasNoClube.putIfAbsent(p, 0);
    }
}
