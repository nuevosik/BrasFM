package com.brasfm.social;

import com.brasfm.model.*;
import java.util.*;

/**
 * Sistema de mídia, conferências de imprensa e rumores.
 */
public class MediaSystem {

    public enum TipoManchete {
        // Resultados
        VITORIA_DESTAQUE("Grande vitória!", 10),
        DERROTA_CRITICA("Derrota preocupante", -8),
        SERIE_INVICTA("Time segue invicto!", 12),
        CRISE_RESULTADOS("Crise de resultados", -15),

        // Transferências
        RUMOR_SAIDA("especulado em clube maior", -5),
        RUMOR_CHEGADA("Clube negocia com", 0),
        TRANSFERENCIA_CONFIRMADA("é o novo reforço!", 8),
        RENOVACAO_DIFICIL("não renova contrato", -10),

        // Pressão
        TORCIDA_COBRA("Torcida pressiona", -8),
        DIRETORIA_APOIA("Diretoria banca técnico", 5),
        TECNICO_AMEACADO("Cargo em risco?", -12);

        private final String template;
        private final int impactoMoral;

        TipoManchete(String template, int impacto) {
            this.template = template;
            this.impactoMoral = impacto;
        }

        public String getTemplate() {
            return template;
        }

        public int getImpactoMoral() {
            return impactoMoral;
        }
    }

    public enum RespostaImprensa {
        ELOGIAR("Elogiar", "Reforça confiança", 5),
        PROTEGER("Proteger Jogador", "Tira pressão do jogador", 3),
        CRITICAR("Criticar Publicamente", "Pode motivar ou destruir", -8),
        PRESSIONAR_ARBITRO("Pressionar Árbitros", "Jogo mental", 0),
        PROVOCAR_RIVAL("Provocar Rival", "Esquenta o jogo", 2),
        SEM_COMENTARIOS("Sem Comentários", "Neutro", 0),
        CONFIRMAR("Confirmar", "Assume responsabilidade", 0),
        NEGAR("Negar", "Pode gerar desconfiança se falso", -3);

        private final String nome;
        private final String descricao;
        private final int impactoBase;

        RespostaImprensa(String nome, String descricao, int impacto) {
            this.nome = nome;
            this.descricao = descricao;
            this.impactoBase = impacto;
        }

        public String getNome() {
            return nome;
        }

        public String getDescricao() {
            return descricao;
        }

        public int getImpactoBase() {
            return impactoBase;
        }
    }

    // Manchetes ativas e rumores
    private List<Manchete> manchetesAtivas = new ArrayList<>();
    private List<Rumor> rumoresAtivos = new ArrayList<>();
    private Random random = new Random();

    private MoraleSystem moraleSystem;
    private SquadDynamics dynamics;

    public MediaSystem(MoraleSystem moraleSystem, SquadDynamics dynamics) {
        this.moraleSystem = moraleSystem;
        this.dynamics = dynamics;
    }

    /**
     * Gera manchetes após uma partida.
     */
    public List<Manchete> gerarManchetesPosJogo(Team time, int golsFeitos, int golsSofridos,
            Player destaque, boolean jogoImportante) {
        List<Manchete> novas = new ArrayList<>();
        int saldo = golsFeitos - golsSofridos;

        if (saldo >= 3) {
            novas.add(new Manchete(TipoManchete.VITORIA_DESTAQUE,
                    time.getNome() + " goleia e impressiona!"));
        } else if (saldo <= -3) {
            novas.add(new Manchete(TipoManchete.DERROTA_CRITICA,
                    time.getNome() + " sofre goleada humilhante"));
        }

        if (destaque != null && saldo > 0) {
            novas.add(new Manchete(TipoManchete.VITORIA_DESTAQUE,
                    destaque.getNome() + " brilha em vitória"));
        }

        // Verifica sequências
        if (time.getVitorias() >= 5 && time.getDerrotas() == 0) {
            novas.add(new Manchete(TipoManchete.SERIE_INVICTA,
                    time.getNome() + " mantém invencibilidade!"));
        }

        if (time.getDerrotas() >= 3) {
            novas.add(new Manchete(TipoManchete.CRISE_RESULTADOS,
                    "Crise no " + time.getNome() + ": " + time.getDerrotas() + " derrotas seguidas"));
        }

        manchetesAtivas.addAll(novas);
        return novas;
    }

    /**
     * Gera rumor de transferência.
     */
    public Rumor gerarRumorTransferencia(Player jogador, Team clubeInteressado) {
        Rumor rumor = new Rumor(jogador, clubeInteressado);
        rumoresAtivos.add(rumor);

        // Afeta moral do jogador
        PlayerPersonality pers = jogador.getPersonality();
        if (pers != null && pers.getAmbicao() > 12) {
            // Ambicioso fica animado
            moraleSystem.setMoral(jogador, moraleSystem.getMoral(jogador) + 5);
        } else {
            // Outros ficam inquietos
            moraleSystem.setMoral(jogador, moraleSystem.getMoral(jogador) - 5);
        }

        return rumor;
    }

    /**
     * Gera pergunta de conferência de imprensa.
     */
    public PerguntaImprensa gerarPergunta(Team time, Match ultimaPartida, List<Rumor> rumores) {
        List<PerguntaImprensa> possiveis = new ArrayList<>();

        // Sobre resultados
        if (ultimaPartida != null) {
            boolean venceu = ultimaPartida.getGolsMandante() > ultimaPartida.getGolsVisitante();
            if (ultimaPartida.getMandante() == time && venceu ||
                    ultimaPartida.getVisitante() == time && !venceu) {
                possiveis.add(new PerguntaImprensa(
                        "Como avalia a vitória de hoje?",
                        TipoManchete.VITORIA_DESTAQUE));
            } else {
                possiveis.add(new PerguntaImprensa(
                        "O que explica o resultado negativo?",
                        TipoManchete.DERROTA_CRITICA));
            }
        }

        // Sobre rumores
        for (Rumor r : rumores) {
            possiveis.add(new PerguntaImprensa(
                    "Há interesse do " + r.getClubeInteressado().getNome() +
                            " em " + r.getJogador().getNome() + "?",
                    TipoManchete.RUMOR_SAIDA));
        }

        // Sobre pressão
        if (time.getDerrotas() >= 2) {
            possiveis.add(new PerguntaImprensa(
                    "Sente o cargo ameaçado após os resultados?",
                    TipoManchete.TECNICO_AMEACADO));
        }

        return possiveis.isEmpty() ? null : possiveis.get(random.nextInt(possiveis.size()));
    }

    /**
     * Processa resposta do técnico na conferência.
     */
    public String processarResposta(PerguntaImprensa pergunta, RespostaImprensa resposta,
            Team time, Player jogadorMencionado) {

        String resultado = "📰 " + resposta.getNome() + " - ";

        // Impacto no time
        int impacto = resposta.getImpactoBase();

        switch (resposta) {
            case PROTEGER:
                if (jogadorMencionado != null) {
                    moraleSystem.setMoral(jogadorMencionado,
                            moraleSystem.getMoral(jogadorMencionado) + 8);
                    resultado += jogadorMencionado.getNome() + " agradece o apoio.";
                }
                break;

            case CRITICAR:
                if (jogadorMencionado != null) {
                    PlayerPersonality pers = jogadorMencionado.getPersonality();
                    if (pers != null && pers.getProfissionalismo() > 15) {
                        moraleSystem.setMoral(jogadorMencionado,
                                moraleSystem.getMoral(jogadorMencionado) + 5);
                        resultado += jogadorMencionado.getNome() + " responde bem à cobrança.";
                    } else {
                        moraleSystem.setMoral(jogadorMencionado,
                                moraleSystem.getMoral(jogadorMencionado) - 15);
                        resultado += jogadorMencionado.getNome() + " não gostou da crítica pública.";
                    }
                }
                break;

            case PROVOCAR_RIVAL:
                resultado += "Adversário responde e clima esquenta!";
                // Jogo vira mais tenso, moral sobe
                for (Player p : time.getTitulares()) {
                    moraleSystem.setMoral(p, moraleSystem.getMoral(p) + 3);
                }
                break;

            case NEGAR:
                // Se rumor era verdade, perde credibilidade
                resultado += "Mídia questiona veracidade...";
                break;

            default:
                resultado += "Imprensa registra a declaração.";
        }

        return resultado;
    }

    /**
     * Processa passagem de semana - remove notícias antigas.
     */
    public void processarSemana() {
        // Manchetes duram 2 semanas
        manchetesAtivas.removeIf(m -> m.getSemanas() > 2);
        for (Manchete m : manchetesAtivas) {
            m.passarSemana();
        }

        // Rumores podem se concretizar ou morrer
        Iterator<Rumor> it = rumoresAtivos.iterator();
        while (it.hasNext()) {
            Rumor r = it.next();
            r.passarSemana();
            if (r.getSemanas() > 4) {
                it.remove(); // Rumor morreu
            }
        }
    }

    // Classes internas
    public static class Manchete {
        private TipoManchete tipo;
        private String texto;
        private int semanas = 0;

        public Manchete(TipoManchete tipo, String texto) {
            this.tipo = tipo;
            this.texto = texto;
        }

        public void passarSemana() {
            semanas++;
        }

        public int getSemanas() {
            return semanas;
        }

        public String getTexto() {
            return texto;
        }

        public TipoManchete getTipo() {
            return tipo;
        }
    }

    public static class Rumor {
        private Player jogador;
        private Team clubeInteressado;
        private int semanas = 0;
        private boolean confirmado = false;

        public Rumor(Player jogador, Team clube) {
            this.jogador = jogador;
            this.clubeInteressado = clube;
        }

        public void passarSemana() {
            semanas++;
        }

        public int getSemanas() {
            return semanas;
        }

        public Player getJogador() {
            return jogador;
        }

        public Team getClubeInteressado() {
            return clubeInteressado;
        }
    }

    public static class PerguntaImprensa {
        private String texto;
        private TipoManchete contexto;

        public PerguntaImprensa(String texto, TipoManchete contexto) {
            this.texto = texto;
            this.contexto = contexto;
        }

        public String getTexto() {
            return texto;
        }

        public TipoManchete getContexto() {
            return contexto;
        }
    }

    // Getters
    public List<Manchete> getManchetesAtivas() {
        return manchetesAtivas;
    }

    public List<Rumor> getRumoresAtivos() {
        return rumoresAtivos;
    }
}
