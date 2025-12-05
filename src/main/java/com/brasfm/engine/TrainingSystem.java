package com.brasfm.engine;

import com.brasfm.model.*;
import java.util.*;

/**
 * Sistema de treino e desenvolvimento de jogadores.
 * Substitui o conceito de "loja de equipamentos" por desenvolvimento orgânico.
 */
public class TrainingSystem {

    public enum TipoTreino {
        TATICO("Tático", "Melhora trabalho de equipa e posicionamento"),
        FISICO("Físico", "Melhora resistência, velocidade e força"),
        TECNICO("Técnico", "Melhora passe, drible e técnica"),
        FINALIZACAO("Finalização", "Melhora chute e cabeceio"),
        DEFENSIVO("Defensivo", "Melhora desarme e marcação"),
        GOLEIRO("Goleiro", "Treino específico para goleiros"),
        RECUPERACAO("Recuperação", "Foco em descanso e recuperação física"),
        TATICO_ESPECIFICO("Tático Específico", "Treina a tática atual da equipa");

        private final String nome;
        private final String descricao;

        TipoTreino(String nome, String descricao) {
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

    public enum IntensidadeTreino {
        LEVE("Leve", 0.5, 0.3), // Pouca evolução, pouca fadiga
        NORMAL("Normal", 1.0, 0.6), // Evolução normal
        INTENSO("Intenso", 1.5, 1.0), // Mais evolução, mais fadiga
        DUPLO("Duplo", 2.0, 1.5); // Muito mais evolução, risco de lesão

        private final String nome;
        private final double fatorEvolucao;
        private final double fatorFadiga;

        IntensidadeTreino(String nome, double fatorEvolucao, double fatorFadiga) {
            this.nome = nome;
            this.fatorEvolucao = fatorEvolucao;
            this.fatorFadiga = fatorFadiga;
        }

        public String getNome() {
            return nome;
        }

        public double getFatorEvolucao() {
            return fatorEvolucao;
        }

        public double getFatorFadiga() {
            return fatorFadiga;
        }
    }

    private Random random = new Random();

    /**
     * Processa uma sessão de treino para um jogador.
     * 
     * @return Pontos de experiência ganhos
     */
    public double processarTreino(
            Player jogador,
            PlayerAttributes attrs,
            PlayerPersonality personality,
            TipoTreino tipo,
            IntensidadeTreino intensidade,
            ClubFacilities facilities,
            StaffMember treinadorRelevante) {
        // XP base por sessão
        double xpBase = 10;

        // Modificador de intensidade
        xpBase *= intensidade.getFatorEvolucao();

        // Modificador de idade (jovens evoluem mais)
        double fatorIdade = calcularFatorIdade(jogador.getIdade());
        xpBase *= fatorIdade;

        // Modificador de personalidade (profissionalismo)
        xpBase *= personality.getModificadorTreino();

        // Modificador de instalações
        xpBase *= facilities.getBonusTreino();

        // Modificador do treinador específico
        if (treinadorRelevante != null) {
            xpBase *= treinadorRelevante.getBonusDesenvolvimento();
        }

        // Aplica evolução nos atributos relevantes
        aplicarEvolucao(attrs, tipo, xpBase);

        // Aplica fadiga
        double fadigaBase = 10 * intensidade.getFatorFadiga();
        double energiaAtual = jogador.getEnergia();
        jogador.gastarEnergia((int) fadigaBase);

        // Verifica risco de lesão em treino intenso
        if (intensidade == IntensidadeTreino.DUPLO) {
            double chanceLesao = 0.02 * (1 - facilities.getReducaoChanceLesao());
            chanceLesao *= (1 - personality.getResistenciaLesao() / 40.0);

            if (random.nextDouble() < chanceLesao) {
                // Lesão no treino!
                int diasLesao = 7 + random.nextInt(14); // 1-3 semanas
                jogador.setContundido(true);
                return -1; // Indica lesão
            }
        }

        return xpBase;
    }

    /**
     * Calcula o fator de evolução por idade.
     * Jogadores jovens evoluem mais, veteranos chegam ao platô.
     */
    private double calcularFatorIdade(int idade) {
        if (idade <= 18) {
            return 1.5; // Potencial máximo
        } else if (idade <= 21) {
            return 1.3;
        } else if (idade <= 24) {
            return 1.1;
        } else if (idade <= 28) {
            return 1.0; // Auge
        } else if (idade <= 32) {
            return 0.7; // Início do declínio
        } else {
            return 0.4; // Declínio acentuado
        }
    }

    /**
     * Aplica evolução nos atributos baseado no tipo de treino.
     */
    private void aplicarEvolucao(PlayerAttributes attrs, TipoTreino tipo, double xp) {
        // Cada ponto de XP tem 10% de chance de aumentar um atributo em 1
        int ganhos = 0;
        for (int i = 0; i < (int) xp; i++) {
            if (random.nextDouble() < 0.10) {
                ganhos++;
            }
        }

        if (ganhos == 0)
            return;

        switch (tipo) {
            case TATICO:
                attrs.setTrabalhoEquipa(attrs.getTrabalhoEquipa() + ganhos);
                attrs.setPosicionamento(attrs.getPosicionamento() + ganhos / 2);
                attrs.setAntecipacao(attrs.getAntecipacao() + ganhos / 2);
                break;

            case FISICO:
                attrs.setResistencia(attrs.getResistencia() + ganhos);
                attrs.setVelocidade(attrs.getVelocidade() + ganhos / 2);
                attrs.setForca(attrs.getForca() + ganhos / 2);
                break;

            case TECNICO:
                attrs.setTecnica(attrs.getTecnica() + ganhos);
                attrs.setPrimeiroToque(attrs.getPrimeiroToque() + ganhos / 2);
                attrs.setDrible(attrs.getDrible() + ganhos / 2);
                break;

            case FINALIZACAO:
                attrs.setFinalizacao(attrs.getFinalizacao() + ganhos);
                attrs.setChuteLonga(attrs.getChuteLonga() + ganhos / 2);
                attrs.setCabeceio(attrs.getCabeceio() + ganhos / 2);
                break;

            case DEFENSIVO:
                attrs.setDesarme(attrs.getDesarme() + ganhos);
                attrs.setMarcacao(attrs.getMarcacao() + ganhos / 2);
                attrs.setPosicionamento(attrs.getPosicionamento() + ganhos / 2);
                break;

            case GOLEIRO:
                attrs.setGoleiro(attrs.getGoleiro() + ganhos);
                attrs.setReflexos(attrs.getReflexos() + ganhos / 2);
                attrs.setUmContraUm(attrs.getUmContraUm() + ganhos / 2);
                break;

            case RECUPERACAO:
                // Não melhora atributos, apenas recupera energia
                break;

            case TATICO_ESPECIFICO:
                attrs.setDecisoes(attrs.getDecisoes() + ganhos);
                attrs.setSemBola(attrs.getSemBola() + ganhos / 2);
                break;
        }
    }

    /**
     * Processa treino de recuperação.
     */
    public void processarRecuperacao(Player jogador, ClubFacilities facilities) {
        double recuperacaoBase = 20;
        recuperacaoBase *= facilities.getModificadorRecuperacao();
        jogador.recuperarEnergia((int) recuperacaoBase);
    }

    /**
     * Calcula quanto tempo um jogador precisa descansar antes do próximo jogo.
     */
    public int calcularDiasRecuperacaoNecessarios(
            Player jogador,
            PlayerPersonality personality,
            ClubFacilities facilities) {
        double energiaAtual = jogador.getEnergia();

        if (energiaAtual >= 80) {
            return 0; // Pronto para jogar
        }

        double recuperacaoPorDia = 15 * facilities.getModificadorRecuperacao();
        recuperacaoPorDia *= (0.7 + personality.getCondicaoNatural() / 30.0);

        double energiaNecessaria = 80 - energiaAtual;
        return (int) Math.ceil(energiaNecessaria / recuperacaoPorDia);
    }

    /**
     * Gera relatório de desenvolvimento do jogador.
     */
    public String gerarRelatorioDesenvolvimento(
            Player jogador,
            PlayerPersonality personality,
            int idadeInicial) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== Relatório de Desenvolvimento ===\n");
        sb.append("Jogador: ").append(jogador.getNome()).append("\n");
        sb.append("Idade: ").append(jogador.getIdade()).append(" anos\n");
        sb.append("\n");

        double fatorIdade = calcularFatorIdade(jogador.getIdade());
        if (fatorIdade >= 1.3) {
            sb.append("📈 Alto potencial de evolução\n");
        } else if (fatorIdade >= 1.0) {
            sb.append("📊 Evolução normal\n");
        } else if (fatorIdade >= 0.7) {
            sb.append("📉 Início do declínio\n");
        } else {
            sb.append("⚠️ Declínio acentuado\n");
        }

        sb.append("\n[Personalidade]\n");
        sb.append("Profissionalismo: ").append(personality.getProfissionalismo()).append("/20\n");
        sb.append("Ambição: ").append(personality.getAmbicao()).append("/20\n");
        sb.append("Condição Natural: ").append(personality.getCondicaoNatural()).append("/20\n");

        return sb.toString();
    }

    /**
     * Retorna treinador adequado para um tipo de treino.
     */
    public StaffMember getTreinadorParaTreino(ClubFacilities facilities, TipoTreino tipo) {
        StaffMember.TipoStaff tipoStaff;

        switch (tipo) {
            case GOLEIRO:
                tipoStaff = StaffMember.TipoStaff.TREINADOR_GOLEIROS;
                break;
            case DEFENSIVO:
                tipoStaff = StaffMember.TipoStaff.TREINADOR_DEFESA;
                break;
            case FINALIZACAO:
                tipoStaff = StaffMember.TipoStaff.TREINADOR_ATAQUE;
                break;
            case FISICO:
            case RECUPERACAO:
                tipoStaff = StaffMember.TipoStaff.PREPARADOR_FISICO;
                break;
            default:
                tipoStaff = StaffMember.TipoStaff.CIENTISTA_DESPORTO;
                break;
        }

        return facilities.getStaffPorTipo(tipoStaff);
    }
}
