package com.brasfm.economy;

import com.brasfm.model.*;
import java.util.*;

/**
 * Representa uma proposta de transferência com estrutura complexa.
 */
public class TransferOffer {

    public enum Status {
        PENDENTE, ACEITA, RECUSADA, NEGOCIANDO, EXPIRADA, BLOQUEADA_AGENTE
    }

    public enum TipoTransferencia {
        DEFINITIVA,
        EMPRESTIMO,
        EMPRESTIMO_COM_OPCAO,
        TROCA
    }

    private String id;
    private Team clubeOrigem;
    private Team clubeDestino;
    private Player jogador;
    private TipoTransferencia tipo;
    private Status status;
    private Date dataCriacao;
    private Date dataExpiracao;

    // Valores
    private long valorInicial; // Parcela à vista
    private long valorParcelado; // Valor em parcelas
    private int numeroParcelas; // Quantas parcelas

    // Cláusulas adicionais
    private long bonusPorJogos; // Bônus após X jogos
    private int jogosParaBonus;
    private long bonusPorGols; // Bônus por gols
    private int golsParaBonus;
    private long bonusPorTitulos; // Bônus se ganhar título
    private double percentualRevendaFutura; // % de venda futura

    // Empréstimo
    private int semanasEmprestimo;
    private long valorOpcaoCompra;
    private boolean opcaoObrigatoria; // Se é obrigatória

    // Troca
    private Player jogadorTroca;
    private long diferencaValor; // Se houver diferença em $

    // Agente
    private Agent agente;
    private long comissaoAgente;

    // Contraproposta
    private TransferOffer contraproposta;

    public TransferOffer(Team origem, Team destino, Player jogador, TipoTransferencia tipo) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.clubeOrigem = origem;
        this.clubeDestino = destino;
        this.jogador = jogador;
        this.tipo = tipo;
        this.status = Status.PENDENTE;
        this.dataCriacao = new Date();

        // Expira em 7 dias
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 7);
        this.dataExpiracao = cal.getTime();
    }

    /**
     * Calcula o valor total da transferência.
     */
    public long getValorTotal() {
        long total = valorInicial + valorParcelado;

        // Estima bônus (50% de chance de ativar cada)
        total += bonusPorJogos * 0.5;
        total += bonusPorGols * 0.3;
        total += bonusPorTitulos * 0.2;

        return total;
    }

    /**
     * Calcula valor garantido (sem bônus).
     */
    public long getValorGarantido() {
        return valorInicial + valorParcelado;
    }

    /**
     * Cria uma contraproposta.
     */
    public TransferOffer criarContraproposta(long novoValorInicial, long novoValorParcelado) {
        TransferOffer contra = new TransferOffer(clubeOrigem, clubeDestino, jogador, tipo);
        contra.valorInicial = novoValorInicial;
        contra.valorParcelado = novoValorParcelado;
        contra.numeroParcelas = this.numeroParcelas;
        contra.bonusPorJogos = this.bonusPorJogos;
        contra.jogosParaBonus = this.jogosParaBonus;
        contra.percentualRevendaFutura = this.percentualRevendaFutura;

        this.contraproposta = contra;
        this.status = Status.NEGOCIANDO;

        return contra;
    }

    /**
     * Aceita a proposta.
     */
    public void aceitar() {
        // Verifica se agente bloqueia
        if (agente != null && agente.bloquearPorComissao(comissaoAgente, getValorTotal())) {
            this.status = Status.BLOQUEADA_AGENTE;
            return;
        }

        this.status = Status.ACEITA;
    }

    /**
     * Recusa a proposta.
     */
    public void recusar() {
        this.status = Status.RECUSADA;
    }

    /**
     * Verifica se expirou.
     */
    public boolean expirou() {
        if (new Date().after(dataExpiracao)) {
            this.status = Status.EXPIRADA;
            return true;
        }
        return false;
    }

    /**
     * Gera descrição da proposta.
     */
    public String getDescricao() {
        StringBuilder sb = new StringBuilder();

        sb.append("═══ PROPOSTA DE TRANSFERÊNCIA ═══\n\n");
        sb.append("Jogador: ").append(jogador.getNome()).append("\n");
        sb.append("De: ").append(clubeOrigem.getNome()).append("\n");
        sb.append("Para: ").append(clubeDestino.getNome()).append("\n");
        sb.append("Tipo: ").append(tipo).append("\n\n");

        sb.append("──── VALORES ────\n");
        sb.append(String.format("À vista: R$ %,d\n", valorInicial));
        if (valorParcelado > 0) {
            sb.append(String.format("Parcelado: R$ %,d em %d parcelas\n", valorParcelado, numeroParcelas));
        }
        sb.append(String.format("TOTAL GARANTIDO: R$ %,d\n\n", getValorGarantido()));

        if (bonusPorJogos > 0 || bonusPorGols > 0 || bonusPorTitulos > 0) {
            sb.append("──── BÔNUS ────\n");
            if (bonusPorJogos > 0) {
                sb.append(String.format("+ R$ %,d após %d jogos\n", bonusPorJogos, jogosParaBonus));
            }
            if (bonusPorGols > 0) {
                sb.append(String.format("+ R$ %,d após %d gols\n", bonusPorGols, golsParaBonus));
            }
            if (bonusPorTitulos > 0) {
                sb.append(String.format("+ R$ %,d se ganhar título\n", bonusPorTitulos));
            }
            sb.append("\n");
        }

        if (percentualRevendaFutura > 0) {
            sb.append(String.format("📋 %.0f%% de revenda futura\n\n", percentualRevendaFutura * 100));
        }

        if (tipo == TipoTransferencia.EMPRESTIMO || tipo == TipoTransferencia.EMPRESTIMO_COM_OPCAO) {
            sb.append("──── EMPRÉSTIMO ────\n");
            sb.append(String.format("Duração: %d meses\n", semanasEmprestimo / 4));
            if (valorOpcaoCompra > 0) {
                sb.append(String.format("Opção de compra: R$ %,d%s\n",
                        valorOpcaoCompra, opcaoObrigatoria ? " (OBRIGATÓRIA)" : ""));
            }
            sb.append("\n");
        }

        if (agente != null) {
            sb.append(String.format("🤝 Agente: %s (comissão R$ %,d)\n", agente.getNome(), comissaoAgente));
        }

        sb.append("\nStatus: ").append(status).append("\n");

        return sb.toString();
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public Team getClubeOrigem() {
        return clubeOrigem;
    }

    public Team getClubeDestino() {
        return clubeDestino;
    }

    public Player getJogador() {
        return jogador;
    }

    public TipoTransferencia getTipo() {
        return tipo;
    }

    public Status getStatus() {
        return status;
    }

    public long getValorInicial() {
        return valorInicial;
    }

    public void setValorInicial(long v) {
        this.valorInicial = v;
    }

    public long getValorParcelado() {
        return valorParcelado;
    }

    public void setValorParcelado(long v) {
        this.valorParcelado = v;
    }

    public int getNumeroParcelas() {
        return numeroParcelas;
    }

    public void setNumeroParcelas(int v) {
        this.numeroParcelas = v;
    }

    public long getBonusPorJogos() {
        return bonusPorJogos;
    }

    public void setBonusPorJogos(long v) {
        this.bonusPorJogos = v;
    }

    public int getJogosParaBonus() {
        return jogosParaBonus;
    }

    public void setJogosParaBonus(int v) {
        this.jogosParaBonus = v;
    }

    public double getPercentualRevendaFutura() {
        return percentualRevendaFutura;
    }

    public void setPercentualRevendaFutura(double v) {
        this.percentualRevendaFutura = v;
    }

    public int getSemanasEmprestimo() {
        return semanasEmprestimo;
    }

    public void setSemanasEmprestimo(int v) {
        this.semanasEmprestimo = v;
    }

    public long getValorOpcaoCompra() {
        return valorOpcaoCompra;
    }

    public void setValorOpcaoCompra(long v) {
        this.valorOpcaoCompra = v;
    }

    public void setAgente(Agent a) {
        this.agente = a;
    }

    public void setComissaoAgente(long v) {
        this.comissaoAgente = v;
    }
}
