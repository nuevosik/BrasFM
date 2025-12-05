package com.brasfm.economy;

import com.brasfm.model.*;
import java.util.*;

/**
 * Gerencia o mercado de transferências.
 */
public class TransferMarket {

    private List<TransferOffer> ofertasAtivas;
    private List<TransferOffer> historicoTransferencias;
    private List<Player> jogadoresDisponiveis;
    private Map<Team, ClubTransferAI> aiClubes;
    private PlayerValuation valoracao;

    private boolean janelaAberta;
    private int diasRestantes;
    private boolean deadlineDay;
    private int anoAtual;

    public TransferMarket(int anoAtual) {
        this.anoAtual = anoAtual;
        this.ofertasAtivas = new ArrayList<>();
        this.historicoTransferencias = new ArrayList<>();
        this.jogadoresDisponiveis = new ArrayList<>();
        this.aiClubes = new HashMap<>();
        this.valoracao = new PlayerValuation(anoAtual);
        this.janelaAberta = false;
    }

    /**
     * Registra um clube na IA de transferências.
     */
    public void registrarClube(Team time, ClubTransferAI.VisaoClube visao) {
        aiClubes.put(time, new ClubTransferAI(time, visao, valoracao));
    }

    /**
     * Abre a janela de transferências.
     */
    public void abrirJanela(int diasDuracao) {
        this.janelaAberta = true;
        this.diasRestantes = diasDuracao;
        this.deadlineDay = false;

        // Atualiza lista de disponíveis
        atualizarJogadoresDisponiveis();
    }

    /**
     * Fecha a janela de transferências.
     */
    public void fecharJanela() {
        this.janelaAberta = false;

        // Cancela ofertas pendentes
        for (TransferOffer o : ofertasAtivas) {
            if (o.getStatus() == TransferOffer.Status.PENDENTE ||
                    o.getStatus() == TransferOffer.Status.NEGOCIANDO) {
                o.recusar();
            }
        }
        ofertasAtivas.clear();
    }

    /**
     * Processa um dia de mercado.
     */
    public List<String> processarDia() {
        List<String> eventos = new ArrayList<>();

        if (!janelaAberta)
            return eventos;

        diasRestantes--;

        // Deadline day
        if (diasRestantes <= 1 && !deadlineDay) {
            deadlineDay = true;
            for (ClubTransferAI ai : aiClubes.values()) {
                ai.ativarPanicBuy();
            }
            eventos.add("⚠️ DEADLINE DAY! Clubes em modo pânico!");
        }

        // IA faz propostas
        for (Map.Entry<Team, ClubTransferAI> entry : aiClubes.entrySet()) {
            List<String> eventosClube = processarAI(entry.getKey(), entry.getValue());
            eventos.addAll(eventosClube);
        }

        // Processa ofertas pendentes
        processarOfertas();

        if (diasRestantes <= 0) {
            fecharJanela();
            eventos.add("🔒 Janela de transferências fechada!");
        }

        return eventos;
    }

    private List<String> processarAI(Team time, ClubTransferAI ai) {
        List<String> eventos = new ArrayList<>();
        Random r = new Random();

        // Identifica necessidades
        List<com.brasfm.model.enums.Position> necessidades = ai.identificarNecessidades();

        // Procura jogadores
        for (Player jogador : jogadoresDisponiveis) {
            if (time.getJogadores().contains(jogador))
                continue;

            TransferOffer proposta = ai.considerarContratacao(
                    jogador,
                    encontrarClubeDono(jogador),
                    necessidades);

            if (proposta != null) {
                ofertasAtivas.add(proposta);
                eventos.add(String.format("📋 %s fez proposta por %s: R$ %,d",
                        time.getNome(), jogador.getNome(), proposta.getValorTotal()));
            }

            // Limita propostas por dia
            if (ofertasAtivas.size() > 20)
                break;
        }

        return eventos;
    }

    private void processarOfertas() {
        Iterator<TransferOffer> it = ofertasAtivas.iterator();

        while (it.hasNext()) {
            TransferOffer oferta = it.next();

            if (oferta.expirou()) {
                it.remove();
                continue;
            }

            if (oferta.getStatus() != TransferOffer.Status.PENDENTE)
                continue;

            // IA do clube vendedor avalia
            ClubTransferAI aiVendedor = aiClubes.get(oferta.getClubeOrigem());
            if (aiVendedor == null)
                continue;

            if (aiVendedor.avaliarPropostaRecebida(oferta)) {
                oferta.aceitar();
                if (oferta.getStatus() == TransferOffer.Status.ACEITA) {
                    executarTransferencia(oferta);
                    historicoTransferencias.add(oferta);
                }
            } else {
                oferta.recusar();
            }
        }

        // Remove finalizadas
        ofertasAtivas.removeIf(o -> o.getStatus() != TransferOffer.Status.PENDENTE &&
                o.getStatus() != TransferOffer.Status.NEGOCIANDO);
    }

    private void executarTransferencia(TransferOffer oferta) {
        Player jogador = oferta.getJogador();
        Team origem = oferta.getClubeOrigem();
        Team destino = oferta.getClubeDestino();

        // Move jogador
        origem.removeJogador(jogador);
        destino.addJogador(jogador);

        // Transfere dinheiro
        long valorTotal = oferta.getValorGarantido();
        destino.pagarDespesa((int) valorTotal);
        origem.receberPatrocinio((int) valorTotal);

        // Remove da lista de disponíveis
        jogadoresDisponiveis.remove(jogador);
    }

    private void atualizarJogadoresDisponiveis() {
        jogadoresDisponiveis.clear();

        for (ClubTransferAI ai : aiClubes.values()) {
            jogadoresDisponiveis.addAll(ai.identificarVendaveis());
        }
    }

    private Team encontrarClubeDono(Player jogador) {
        for (Team time : aiClubes.keySet()) {
            if (time.getJogadores().contains(jogador)) {
                return time;
            }
        }
        return null;
    }

    /**
     * Usuário faz proposta por um jogador.
     */
    public TransferOffer fazerProposta(Team clubeComprador, Team clubeVendedor, Player jogador,
            long valorInicial, long valorParcelado, int parcelas) {

        if (!janelaAberta)
            return null;

        TransferOffer proposta = new TransferOffer(
                clubeVendedor, clubeComprador, jogador,
                TransferOffer.TipoTransferencia.DEFINITIVA);

        proposta.setValorInicial(valorInicial);
        proposta.setValorParcelado(valorParcelado);
        proposta.setNumeroParcelas(parcelas);

        ofertasAtivas.add(proposta);
        return proposta;
    }

    /**
     * Responde a uma proposta recebida pelo usuário.
     */
    public void responderProposta(TransferOffer proposta, boolean aceitar) {
        if (aceitar) {
            proposta.aceitar();
            if (proposta.getStatus() == TransferOffer.Status.ACEITA) {
                executarTransferencia(proposta);
                historicoTransferencias.add(proposta);
            }
        } else {
            proposta.recusar();
        }
    }

    /**
     * Calcula valor de mercado de um jogador.
     */
    public long getValorMercado(Player jogador, Team time) {
        return valoracao.calcularValor(jogador, time, 70);
    }

    // Getters
    public List<TransferOffer> getOfertasAtivas() {
        return ofertasAtivas;
    }

    public List<TransferOffer> getHistoricoTransferencias() {
        return historicoTransferencias;
    }

    public List<Player> getJogadoresDisponiveis() {
        return jogadoresDisponiveis;
    }

    public boolean isJanelaAberta() {
        return janelaAberta;
    }

    public int getDiasRestantes() {
        return diasRestantes;
    }

    public boolean isDeadlineDay() {
        return deadlineDay;
    }
}
