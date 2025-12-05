package com.brasfm.economy;

import com.brasfm.model.*;
import java.util.*;

/**
 * Representa um agente de jogadores.
 * Agentes influenciam transferências e podem criar problemas.
 */
public class Agent {

    private String nome;
    private int reputacao; // 1-20
    private int ganancia; // 1-20, afeta comissões exigidas
    private int influencia; // 1-20, capacidade de convencer jogadores
    private List<Player> clientes;

    // Comissão padrão: 5-15% do valor da transferência
    private double taxaComissao;

    public Agent(String nome, int reputacao) {
        this.nome = nome;
        this.reputacao = Math.max(1, Math.min(20, reputacao));
        this.ganancia = 5 + new Random().nextInt(12);
        this.influencia = 5 + new Random().nextInt(12);
        this.clientes = new ArrayList<>();
        this.taxaComissao = 0.05 + (ganancia / 100.0);
    }

    /**
     * Calcula comissão exigida para uma transferência.
     */
    public long calcularComissao(long valorTransferencia) {
        return (long) (valorTransferencia * taxaComissao);
    }

    /**
     * Tenta instigar jogador a pedir transferência.
     * Retorna true se conseguir.
     */
    public boolean instigarTransferencia(Player jogador, Team timeAtual, Team timePretendente) {
        Random r = new Random();

        // Fatores que favorecem a saída
        int chanceBase = 10;

        // Jogador infeliz?
        PlayerPersonality pers = jogador.getPersonality();
        if (pers != null && pers.getFelicidade() < 50) {
            chanceBase += 20;
        }

        // Time pretendente maior?
        if (timePretendente.getForcaMedia() > timeAtual.getForcaMedia() + 10) {
            chanceBase += 15;
        }

        // Jogador ambicioso?
        if (pers != null && pers.getAmbicao() > 15) {
            chanceBase += 15;
        }

        // Jogador leal?
        if (pers != null && pers.getLealdade() > 15) {
            chanceBase -= 20;
        }

        // Influência do agente
        chanceBase += influencia;

        return r.nextInt(100) < chanceBase;
    }

    /**
     * Negocia renovação de contrato em nome do jogador.
     */
    public ContractDemand negociarRenovacao(Player jogador, long valorMercado) {
        ContractDemand demanda = new ContractDemand();

        // Salário exigido baseado no valor de mercado e ganância
        int salarioAtual = jogador.getSalario();
        int salarioBase = (int) (valorMercado * 0.0025); // ~0.25% do valor por semana

        // Ganância aumenta exigência
        double fatorGanancia = 1.0 + (ganancia / 40.0);
        demanda.salarioSemanal = (int) (salarioBase * fatorGanancia);

        // Aumento mínimo sobre salário atual
        demanda.salarioSemanal = Math.max(demanda.salarioSemanal, (int) (salarioAtual * 1.2));

        // Duração do contrato
        if (jogador.getIdade() < 25) {
            demanda.semanasContrato = 52 * (3 + new Random().nextInt(3)); // 3-5 anos
        } else if (jogador.getIdade() < 30) {
            demanda.semanasContrato = 52 * (2 + new Random().nextInt(2)); // 2-3 anos
        } else {
            demanda.semanasContrato = 52 * (1 + new Random().nextInt(2)); // 1-2 anos
        }

        // Bônus de assinatura
        demanda.bonusAssinatura = (long) (demanda.salarioSemanal * 4 * (1 + ganancia / 20.0));

        // Cláusula de rescisão
        demanda.clausulaRescisao = (long) (valorMercado * (1.5 + ganancia / 20.0));

        return demanda;
    }

    /**
     * Verifica se agente vai bloquear transferência por comissão insuficiente.
     */
    public boolean bloquearPorComissao(long comissaoOferecida, long valorTransferencia) {
        long comissaoDesejada = calcularComissao(valorTransferencia);

        // Se oferta for menor que 70% do desejado, bloqueia
        return comissaoOferecida < comissaoDesejada * 0.7;
    }

    /**
     * Adiciona cliente.
     */
    public void addCliente(Player jogador) {
        if (!clientes.contains(jogador)) {
            clientes.add(jogador);
        }
    }

    /**
     * Remove cliente.
     */
    public void removeCliente(Player jogador) {
        clientes.remove(jogador);
    }

    // Getters
    public String getNome() {
        return nome;
    }

    public int getReputacao() {
        return reputacao;
    }

    public int getGanancia() {
        return ganancia;
    }

    public int getInfluencia() {
        return influencia;
    }

    public List<Player> getClientes() {
        return clientes;
    }

    public double getTaxaComissao() {
        return taxaComissao;
    }

    @Override
    public String toString() {
        return nome + " (Rep: " + reputacao + ", Clientes: " + clientes.size() + ")";
    }

    /**
     * Demandas contratuais de um jogador.
     */
    public static class ContractDemand {
        public int salarioSemanal;
        public int semanasContrato;
        public long bonusAssinatura;
        public long clausulaRescisao;

        @Override
        public String toString() {
            return String.format(
                    "Salário: R$ %,d/sem | %d anos | Bônus: R$ %,d | Cláusula: R$ %,d",
                    salarioSemanal, semanasContrato / 52, bonusAssinatura, clausulaRescisao);
        }
    }
}
