package com.brasfm.model;

import com.brasfm.model.enums.Caracteristica;
import com.brasfm.model.enums.Position;
import java.util.EnumSet;
import java.util.Set;

/**
 * Representa um jogador de futebol com todas as suas habilidades e
 * características.
 */
public class Player {
    private String nome;
    private int idade;
    private Position posicao;
    private Position posicaoOriginal;

    // Força geral (1-100)
    private int forca;

    // Habilidades individuais (1-100)
    private int goleiro;
    private int agilidade;
    private int passe;
    private int armacao;
    private int desarme;
    private int finalizacao;
    private int tecnica;

    // Características inatas (máximo 2)
    private Set<Caracteristica> caracteristicas;

    // Estado atual
    private int energia; // 0-100
    private int moral; // 0-100
    private boolean contundido;
    private boolean suspenso;
    private int cartaoAmarelo;
    private int cartoesAcumulados;

    // Contrato
    private int salario;
    private int semanasContrato;

    // Estatísticas da temporada
    private int jogos;
    private int gols;
    private int assistencias;
    private double mediaNota;

    // Lado preferido
    private boolean prefereDireita;
    private boolean prefereEsquerda;

    // Potencial para juniores
    private int potencial; // CPE - Capacidade Potencial Estimada
    private boolean estrela;

    // Personalidade e atributos ocultos
    private PlayerPersonality personality;

    public Player(String nome, int idade, Position posicao) {
        this.personality = new PlayerPersonality();
        this.nome = nome;
        this.idade = idade;
        this.posicao = posicao;
        this.posicaoOriginal = posicao;
        this.caracteristicas = EnumSet.noneOf(Caracteristica.class);
        this.energia = 100;
        this.moral = 75;
        this.semanasContrato = 52; // 1 ano
        this.prefereDireita = true;
        this.prefereEsquerda = true;
    }

    /**
     * Calcula a força considerando improvisação de posição.
     */
    public int getForcaEfetiva() {
        if (posicao == posicaoOriginal) {
            return forca;
        }
        // Penalidade por improvisação de posição
        return (int) (forca * 0.85);
    }

    /**
     * Calcula a força considerando a condição física e moral.
     */
    public int getForcaAtual() {
        double fatorEnergia = energia / 100.0;
        double fatorMoral = 0.8 + (moral / 500.0); // moral afeta entre 0.8 e 1.0
        return (int) (getForcaEfetiva() * fatorEnergia * fatorMoral);
    }

    /**
     * Evolui o jogador após treino/jogo.
     */
    public void evoluir(double fator) {
        if (idade < 20) {
            // Jogadores muito novos evoluem mais devagar
            fator *= 0.7;
        } else if (idade <= 31) {
            // Idade ideal para evolução
            fator *= 1.0;
        } else {
            // Após 32 anos, começa a decair
            fator *= -0.5;
        }

        // Quanto maior a força, mais difícil subir
        double dificuldade = 1.0 - (forca / 150.0);
        int ganho = (int) (fator * dificuldade);

        this.forca = Math.max(1, Math.min(100, this.forca + ganho));
    }

    /**
     * Recupera energia após descanso.
     */
    public void recuperarEnergia(int quantidade) {
        this.energia = Math.min(100, this.energia + quantidade);
    }

    /**
     * Gasta energia durante o jogo.
     */
    public void gastarEnergia(int quantidade) {
        // Jogadores com resistência gastam menos energia
        if (caracteristicas.contains(Caracteristica.RESISTENCIA)) {
            quantidade = (int) (quantidade * 0.8);
        }
        this.energia = Math.max(0, this.energia - quantidade);
    }

    /**
     * Verifica se pode ser escalado.
     */
    public boolean podeJogar() {
        return !contundido && !suspenso && semanasContrato > 0;
    }

    /**
     * Adiciona uma característica ao jogador (máximo 2).
     */
    public void addCaracteristica(Caracteristica c) {
        if (caracteristicas.size() < 2) {
            caracteristicas.add(c);
        }
    }

    /**
     * Verifica se tem determinada característica.
     */
    public boolean temCaracteristica(Caracteristica c) {
        return caracteristicas.contains(c);
    }

    /**
     * Calcula habilidade de finalização efetiva.
     */
    public int getFinalizacaoEfetiva() {
        int base = finalizacao;
        if (temCaracteristica(Caracteristica.FINALIZACAO)) {
            base = (int) (base * 1.15);
        }
        return Math.min(100, base);
    }

    /**
     * Calcula habilidade de passe efetiva.
     */
    public int getPasseEfetivo() {
        int base = passe;
        if (temCaracteristica(Caracteristica.PASSE)) {
            base = (int) (base * 1.15);
        }
        if (temCaracteristica(Caracteristica.ARMACAO)) {
            base = (int) (base * 1.10);
        }
        return Math.min(100, base);
    }

    /**
     * Calcula habilidade de desarme efetiva.
     */
    public int getDesarmeEfetivo() {
        int base = desarme;
        if (temCaracteristica(Caracteristica.DESARME)) {
            base = (int) (base * 1.15);
        }
        if (temCaracteristica(Caracteristica.MARCACAO)) {
            base = (int) (base * 1.10);
        }
        return Math.min(100, base);
    }

    /**
     * Verifica se é goleiro.
     */
    public boolean isGoleiro() {
        return posicaoOriginal == Position.GOLEIRO;
    }

    /**
     * Passa uma semana (reduz contrato, envelhece no aniversário).
     */
    public void passarSemana() {
        if (semanasContrato > 0) {
            semanasContrato--;
        }
    }

    /**
     * Renova contrato.
     */
    public void renovarContrato(int semanas, int novoSalario) {
        this.semanasContrato = semanas;
        this.salario = novoSalario;
        if (moral < 70) {
            this.moral = 70;
        }
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    public Position getPosicao() {
        return posicao;
    }

    public void setPosicao(Position posicao) {
        this.posicao = posicao;
    }

    public Position getPosicaoOriginal() {
        return posicaoOriginal;
    }

    public int getForca() {
        return forca;
    }

    public void setForca(int forca) {
        this.forca = Math.max(1, Math.min(100, forca));
    }

    public int getGoleiro() {
        return goleiro;
    }

    public void setGoleiro(int goleiro) {
        this.goleiro = goleiro;
    }

    public int getAgilidade() {
        return agilidade;
    }

    public void setAgilidade(int agilidade) {
        this.agilidade = agilidade;
    }

    public int getPasse() {
        return passe;
    }

    public void setPasse(int passe) {
        this.passe = passe;
    }

    public int getArmacao() {
        return armacao;
    }

    public void setArmacao(int armacao) {
        this.armacao = armacao;
    }

    public int getDesarme() {
        return desarme;
    }

    public void setDesarme(int desarme) {
        this.desarme = desarme;
    }

    public int getFinalizacao() {
        return finalizacao;
    }

    public void setFinalizacao(int finalizacao) {
        this.finalizacao = finalizacao;
    }

    public int getTecnica() {
        return tecnica;
    }

    public void setTecnica(int tecnica) {
        this.tecnica = tecnica;
    }

    public Set<Caracteristica> getCaracteristicas() {
        return caracteristicas;
    }

    public int getEnergia() {
        return energia;
    }

    public void setEnergia(int energia) {
        this.energia = Math.max(0, Math.min(100, energia));
    }

    public int getMoral() {
        return moral;
    }

    public void setMoral(int moral) {
        this.moral = Math.max(0, Math.min(100, moral));
    }

    public boolean isContundido() {
        return contundido;
    }

    public void setContundido(boolean contundido) {
        this.contundido = contundido;
    }

    public boolean isSuspenso() {
        return suspenso;
    }

    public void setSuspenso(boolean suspenso) {
        this.suspenso = suspenso;
    }

    public int getCartaoAmarelo() {
        return cartaoAmarelo;
    }

    public void addCartaoAmarelo() {
        this.cartaoAmarelo++;
        this.cartoesAcumulados++;
        if (cartoesAcumulados >= 3) {
            this.suspenso = true;
            this.cartoesAcumulados = 0;
        }
    }

    public int getSalario() {
        return salario;
    }

    public void setSalario(int salario) {
        this.salario = salario;
    }

    public int getSemanasContrato() {
        return semanasContrato;
    }

    public int getJogos() {
        return jogos;
    }

    public void addJogo() {
        this.jogos++;
    }

    public int getGols() {
        return gols;
    }

    public void addGol() {
        this.gols++;
    }

    public int getAssistencias() {
        return assistencias;
    }

    public void addAssistencia() {
        this.assistencias++;
    }

    public double getMediaNota() {
        return mediaNota;
    }

    public void atualizarNota(double nota) {
        if (jogos == 0) {
            this.mediaNota = nota;
        } else {
            this.mediaNota = ((mediaNota * (jogos - 1)) + nota) / jogos;
        }
    }

    public boolean isPrefereDireita() {
        return prefereDireita;
    }

    public void setPrefereDireita(boolean prefereDireita) {
        this.prefereDireita = prefereDireita;
    }

    public boolean isPrefereEsquerda() {
        return prefereEsquerda;
    }

    public void setPrefereEsquerda(boolean prefereEsquerda) {
        this.prefereEsquerda = prefereEsquerda;
    }

    public int getPotencial() {
        return potencial;
    }

    public void setPotencial(int potencial) {
        this.potencial = potencial;
    }

    public boolean isEstrela() {
        return estrela;
    }

    public void setEstrela(boolean estrela) {
        this.estrela = estrela;
    }

    public PlayerPersonality getPersonality() {
        return personality;
    }

    public void setPersonality(PlayerPersonality personality) {
        this.personality = personality;
    }

    public int getCpe() {
        return potencial;
    }

    public double getMediaNotas() {
        return mediaNota;
    }

    @Override
    public String toString() {
        return nome + " (" + posicaoOriginal.getSigla() + ") - " + forca;
    }
}
