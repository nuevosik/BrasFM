package com.brasfm.economy;

import com.brasfm.model.*;
import com.brasfm.model.enums.*;
import java.util.*;

/**
 * Lógica de IA para comportamento de clubes no mercado.
 */
public class ClubTransferAI {

    public enum VisaoClube {
        FORMADOR("Formador", "Desenvolve e vende jovens com lucro"),
        COMPETIDOR("Competidor", "Busca títulos, gasta em estrelas"),
        EQUILIBRADO("Equilibrado", "Mistura formação e compras"),
        ECONOMICO("Econômico", "Foca em contratações baratas e empréstimos");

        private final String nome;
        private final String descricao;

        VisaoClube(String nome, String descricao) {
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

    private Team time;
    private VisaoClube visao;
    private PlayerValuation valoracao;
    private Random random;

    // Configurações de comportamento
    private double toleranciaPreco; // Quanto aceita pagar acima do mercado (0.8-1.5)
    private double agressividade; // Chance de fazer propostas (0.3-1.0)
    private boolean panicBuyMode; // Modo deadline day

    public ClubTransferAI(Team time, VisaoClube visao, PlayerValuation valoracao) {
        this.time = time;
        this.visao = visao;
        this.valoracao = valoracao;
        this.random = new Random();

        // Configura comportamento baseado na visão
        switch (visao) {
            case FORMADOR:
                this.toleranciaPreco = 0.9;
                this.agressividade = 0.4;
                break;
            case COMPETIDOR:
                this.toleranciaPreco = 1.3;
                this.agressividade = 0.8;
                break;
            case EQUILIBRADO:
                this.toleranciaPreco = 1.1;
                this.agressividade = 0.6;
                break;
            case ECONOMICO:
                this.toleranciaPreco = 0.8;
                this.agressividade = 0.5;
                break;
        }
    }

    /**
     * Identifica necessidades do elenco.
     */
    public List<Position> identificarNecessidades() {
        List<Position> necessidades = new ArrayList<>();

        // Conta jogadores por posição
        Map<Position, Integer> contagem = new EnumMap<>(Position.class);
        for (Position p : Position.values()) {
            contagem.put(p, 0);
        }

        for (Player p : time.getJogadores()) {
            Position pos = p.getPosicaoOriginal();
            contagem.put(pos, contagem.getOrDefault(pos, 0) + 1);
        }

        // Define mínimos ideais
        Map<Position, Integer> minimos = new EnumMap<>(Position.class);
        minimos.put(Position.GOLEIRO, 2);
        minimos.put(Position.ZAGUEIRO, 4);
        minimos.put(Position.LATERAL_DIREITO, 2);
        minimos.put(Position.LATERAL_ESQUERDO, 2);
        minimos.put(Position.VOLANTE, 2);
        minimos.put(Position.MEIA, 2);
        minimos.put(Position.MEIA_ATACANTE, 2);
        minimos.put(Position.PONTA_DIREITA, 2);
        minimos.put(Position.PONTA_ESQUERDA, 2);
        minimos.put(Position.CENTROAVANTE, 2);

        for (Map.Entry<Position, Integer> entry : minimos.entrySet()) {
            int atual = contagem.getOrDefault(entry.getKey(), 0);
            if (atual < entry.getValue()) {
                necessidades.add(entry.getKey());
            }
        }

        return necessidades;
    }

    /**
     * Avalia se deve aceitar proposta por um jogador.
     */
    public boolean avaliarPropostaRecebida(TransferOffer proposta) {
        Player jogador = proposta.getJogador();
        long valorMercado = valoracao.calcularValor(jogador, time, 70);
        long valorOferecido = proposta.getValorGarantido();

        // Razão valor/mercado
        double razao = (double) valorOferecido / valorMercado;

        // Clube formador aceita boas ofertas
        if (visao == VisaoClube.FORMADOR) {
            if (razao >= 1.0)
                return true;
            if (razao >= 0.85 && jogador.getIdade() >= 27)
                return true;
        }

        // Clube competidor é mais difícil
        if (visao == VisaoClube.COMPETIDOR) {
            if (razao < 1.3)
                return false;
            // Não vende titulares importantes
            if (time.getTitulares().contains(jogador) && jogador.getForca() > time.getForcaMedia()) {
                return razao >= 1.5;
            }
        }

        // Aceita se for razoável
        return razao >= 1.0;
    }

    /**
     * Cria contraproposta se não aceitar.
     */
    public TransferOffer criarContraproposta(TransferOffer propostaOriginal) {
        Player jogador = propostaOriginal.getJogador();
        long valorMercado = valoracao.calcularValor(jogador, time, 70);

        // Pede mais
        double fatorAumento;
        switch (visao) {
            case COMPETIDOR:
                fatorAumento = 1.4;
                break;
            case FORMADOR:
                fatorAumento = 1.15;
                break;
            default:
                fatorAumento = 1.25;
        }

        long novoValor = (long) (valorMercado * fatorAumento);

        return propostaOriginal.criarContraproposta(
                (long) (novoValor * 0.7), // 70% à vista
                (long) (novoValor * 0.3) // 30% parcelado
        );
    }

    /**
     * Decide se deve fazer proposta por um jogador.
     */
    public TransferOffer considerarContratacao(Player alvo, Team clubeVendedor, List<Position> necessidades) {
        // Verifica se é posição necessária
        if (!necessidades.contains(alvo.getPosicaoOriginal())) {
            if (random.nextDouble() > 0.2)
                return null; // 80% ignora
        }

        // Verifica se pode pagar
        long valorMercado = valoracao.calcularValor(alvo, clubeVendedor, 70);
        long orcamentoDisponivel = time.getSaldo();

        if (valorMercado > orcamentoDisponivel * 0.7) {
            // Não pode pagar cash - tenta estrutura criativa
            if (visao != VisaoClube.COMPETIDOR)
                return null;
        }

        // Verifica força
        if (alvo.getForca() < time.getForcaMedia() - 10) {
            return null; // Muito fraco
        }

        // Chance de fazer proposta
        if (random.nextDouble() > agressividade)
            return null;

        // Cria proposta
        TransferOffer proposta = new TransferOffer(
                clubeVendedor, time, alvo,
                TransferOffer.TipoTransferencia.DEFINITIVA);

        long valorOferta = (long) (valorMercado * toleranciaPreco);

        // Panic buy mode aumenta oferta
        if (panicBuyMode) {
            valorOferta = (long) (valorOferta * (1.1 + random.nextDouble() * 0.3));
        }

        // Decide estrutura de pagamento
        if (valorOferta <= orcamentoDisponivel) {
            proposta.setValorInicial(valorOferta);
        } else {
            proposta.setValorInicial((long) (orcamentoDisponivel * 0.6));
            proposta.setValorParcelado(valorOferta - proposta.getValorInicial());
            proposta.setNumeroParcelas(12); // 12 meses
        }

        // Adiciona bônus para clubes econômicos
        if (visao == VisaoClube.ECONOMICO || visao == VisaoClube.FORMADOR) {
            proposta.setBonusPorJogos((long) (valorOferta * 0.1));
            proposta.setJogosParaBonus(20);
            proposta.setPercentualRevendaFutura(0.15); // 15%
        }

        return proposta;
    }

    /**
     * Ativa modo pânico de deadline day.
     */
    public void ativarPanicBuy() {
        this.panicBuyMode = true;
        this.toleranciaPreco *= 1.3;
        this.agressividade = Math.min(1.0, agressividade + 0.3);
    }

    /**
     * Decide quais jogadores colocar à venda.
     */
    public List<Player> identificarVendaveis() {
        List<Player> vendaveis = new ArrayList<>();

        for (Player p : time.getJogadores()) {
            // Jogadores velhos demais
            if (p.getIdade() >= 32 && p.getForca() < time.getForcaMedia()) {
                vendaveis.add(p);
                continue;
            }

            // Jogadores com salário alto demais para força
            long valorMercado = valoracao.calcularValor(p, time, 70);
            int salarioEsperado = valoracao.calcularSalarioEsperado(valorMercado);
            if (p.getSalario() > salarioEsperado * 1.5) {
                vendaveis.add(p);
                continue;
            }

            // Clubes formadores vendem jovens valiosos
            if (visao == VisaoClube.FORMADOR) {
                if (p.getIdade() >= 23 && p.getIdade() <= 26 && p.getForca() >= 70) {
                    vendaveis.add(p);
                }
            }

            // Contrato expirando
            if (p.getSemanasContrato() < 26) {
                vendaveis.add(p);
            }
        }

        return vendaveis;
    }

    // Getters
    public Team getTime() {
        return time;
    }

    public VisaoClube getVisao() {
        return visao;
    }

    public boolean isPanicBuyMode() {
        return panicBuyMode;
    }
}
