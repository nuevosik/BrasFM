package com.brasfm.model;

/**
 * Atributos expandidos de um jogador, divididos em Físicos, Técnicos e Mentais.
 */
public class PlayerAttributes {

    // ==================== ATRIBUTOS FÍSICOS ====================
    private int velocidade; // Velocidade máxima de corrida
    private int aceleracao; // Tempo para atingir velocidade máxima
    private int resistencia; // Capacidade de manter performance por 90 min
    private int forca; // Capacidade de disputas físicas
    private int equilibrio; // Resistência a ser derrubado
    private int salto; // Capacidade de saltar
    private int agilidade; // Mudança de direção rápida
    private int reflexos; // Velocidade de reação (goleiro)

    // ==================== ATRIBUTOS TÉCNICOS ====================
    private int passe; // Precisão de passes curtos/médios
    private int passeLongo; // Precisão de lançamentos
    private int cruzamento; // Qualidade de cruzamentos
    private int finalizacao; // Precisão de chutes
    private int cabeceio; // Habilidade no jogo aéreo
    private int drible; // Capacidade de superar adversários
    private int primeiroToque; // Controle de bola inicial
    private int tecnica; // Habilidade geral com a bola
    private int chuteLonga; // Força e precisão de chutes de longe
    private int cobrancaFalta; // Habilidade em bolas paradas
    private int penalti; // Habilidade em cobrar pênaltis
    private int goleiro; // Habilidade de defesa (goleiro)
    private int umContraUm; // Capacidade em 1v1 (goleiro)
    private int saida; // Saída do gol (goleiro)

    // ==================== ATRIBUTOS MENTAIS ====================
    private int decisoes; // Escolha da melhor ação
    private int visao; // Identificar oportunidades de passe
    private int antecipacao; // Prever movimentos adversários
    private int compostura; // Performance sob pressão
    private int concentracao; // Manter foco durante 90 min
    private int bravura; // Disposição para disputas arriscadas
    private int trabalhoEquipa; // Seguir instruções táticas
    private int semBola; // Movimentação ofensiva inteligente
    private int agressividade; // Intensidade nas disputas
    private int determinacao; // Vontade de vencer
    private int lideranca; // Influência sobre companheiros
    private int posicionamento; // Leitura de jogo defensiva
    private int marcacao; // Capacidade de marcar adversários
    private int desarme; // Capacidade de roubar a bola
    private int flair; // Criatividade e improviso

    public PlayerAttributes() {
        // Inicializa todos com valor médio
        inicializarComValor(50);
    }

    public PlayerAttributes(int valorBase) {
        inicializarComValor(valorBase);
    }

    private void inicializarComValor(int valor) {
        // Físicos
        this.velocidade = valor;
        this.aceleracao = valor;
        this.resistencia = valor;
        this.forca = valor;
        this.equilibrio = valor;
        this.salto = valor;
        this.agilidade = valor;
        this.reflexos = valor;

        // Técnicos
        this.passe = valor;
        this.passeLongo = valor;
        this.cruzamento = valor;
        this.finalizacao = valor;
        this.cabeceio = valor;
        this.drible = valor;
        this.primeiroToque = valor;
        this.tecnica = valor;
        this.chuteLonga = valor;
        this.cobrancaFalta = valor;
        this.penalti = valor;
        this.goleiro = valor;
        this.umContraUm = valor;
        this.saida = valor;

        // Mentais
        this.decisoes = valor;
        this.visao = valor;
        this.antecipacao = valor;
        this.compostura = valor;
        this.concentracao = valor;
        this.bravura = valor;
        this.trabalhoEquipa = valor;
        this.semBola = valor;
        this.agressividade = valor;
        this.determinacao = valor;
        this.lideranca = valor;
        this.posicionamento = valor;
        this.marcacao = valor;
        this.desarme = valor;
        this.flair = valor;
    }

    /**
     * Aplica modificador de fadiga a todos os atributos físicos e técnicos.
     * Fadiga de 0.0 a 1.0, onde 1.0 = totalmente descansado.
     */
    public PlayerAttributes comFadiga(double fatorFadiga) {
        PlayerAttributes copia = new PlayerAttributes();

        // Atributos mentais são menos afetados
        double fatorMental = 0.7 + (fatorFadiga * 0.3); // 70% a 100%

        // Físicos (mais afetados)
        copia.velocidade = (int) (this.velocidade * fatorFadiga);
        copia.aceleracao = (int) (this.aceleracao * fatorFadiga);
        copia.resistencia = this.resistencia; // Não muda
        copia.forca = (int) (this.forca * Math.sqrt(fatorFadiga));
        copia.equilibrio = (int) (this.equilibrio * fatorFadiga);
        copia.salto = (int) (this.salto * fatorFadiga);
        copia.agilidade = (int) (this.agilidade * fatorFadiga);
        copia.reflexos = (int) (this.reflexos * fatorFadiga);

        // Técnicos (moderadamente afetados)
        double fatorTecnico = Math.sqrt(fatorFadiga);
        copia.passe = (int) (this.passe * fatorTecnico);
        copia.passeLongo = (int) (this.passeLongo * fatorTecnico);
        copia.cruzamento = (int) (this.cruzamento * fatorTecnico);
        copia.finalizacao = (int) (this.finalizacao * fatorTecnico);
        copia.cabeceio = (int) (this.cabeceio * fatorTecnico);
        copia.drible = (int) (this.drible * fatorTecnico);
        copia.primeiroToque = (int) (this.primeiroToque * fatorTecnico);
        copia.tecnica = (int) (this.tecnica * fatorTecnico);
        copia.chuteLonga = (int) (this.chuteLonga * fatorFadiga);
        copia.cobrancaFalta = (int) (this.cobrancaFalta * fatorTecnico);
        copia.penalti = (int) (this.penalti * fatorMental);
        copia.goleiro = (int) (this.goleiro * fatorTecnico);
        copia.umContraUm = (int) (this.umContraUm * fatorTecnico);
        copia.saida = (int) (this.saida * fatorFadiga);

        // Mentais (menos afetados, exceto concentração)
        copia.decisoes = (int) (this.decisoes * fatorMental);
        copia.visao = (int) (this.visao * fatorMental);
        copia.antecipacao = (int) (this.antecipacao * fatorMental);
        copia.compostura = this.compostura;
        copia.concentracao = (int) (this.concentracao * fatorFadiga); // Muito afetado
        copia.bravura = this.bravura;
        copia.trabalhoEquipa = (int) (this.trabalhoEquipa * fatorMental);
        copia.semBola = (int) (this.semBola * fatorFadiga);
        copia.agressividade = this.agressividade;
        copia.determinacao = this.determinacao;
        copia.lideranca = this.lideranca;
        copia.posicionamento = (int) (this.posicionamento * fatorMental);
        copia.marcacao = (int) (this.marcacao * fatorFadiga);
        copia.desarme = (int) (this.desarme * fatorFadiga);
        copia.flair = (int) (this.flair * fatorMental);

        return copia;
    }

    /**
     * Calcula a força geral do jogador (média ponderada).
     */
    public int calcularForcaGeral() {
        int somaFisico = velocidade + aceleracao + resistencia + forca + equilibrio;
        int somaTecnico = passe + finalizacao + drible + tecnica + primeiroToque;
        int somaMental = decisoes + visao + compostura + concentracao + trabalhoEquipa;

        return (somaFisico + somaTecnico + somaMental) / 15;
    }

    // ==================== GETTERS E SETTERS ====================

    // Físicos
    public int getVelocidade() {
        return velocidade;
    }

    public void setVelocidade(int v) {
        this.velocidade = clamp(v);
    }

    public int getAceleracao() {
        return aceleracao;
    }

    public void setAceleracao(int v) {
        this.aceleracao = clamp(v);
    }

    public int getResistencia() {
        return resistencia;
    }

    public void setResistencia(int v) {
        this.resistencia = clamp(v);
    }

    public int getForca() {
        return forca;
    }

    public void setForca(int v) {
        this.forca = clamp(v);
    }

    public int getEquilibrio() {
        return equilibrio;
    }

    public void setEquilibrio(int v) {
        this.equilibrio = clamp(v);
    }

    public int getSalto() {
        return salto;
    }

    public void setSalto(int v) {
        this.salto = clamp(v);
    }

    public int getAgilidade() {
        return agilidade;
    }

    public void setAgilidade(int v) {
        this.agilidade = clamp(v);
    }

    public int getReflexos() {
        return reflexos;
    }

    public void setReflexos(int v) {
        this.reflexos = clamp(v);
    }

    // Técnicos
    public int getPasse() {
        return passe;
    }

    public void setPasse(int v) {
        this.passe = clamp(v);
    }

    public int getPasseLongo() {
        return passeLongo;
    }

    public void setPasseLongo(int v) {
        this.passeLongo = clamp(v);
    }

    public int getCruzamento() {
        return cruzamento;
    }

    public void setCruzamento(int v) {
        this.cruzamento = clamp(v);
    }

    public int getFinalizacao() {
        return finalizacao;
    }

    public void setFinalizacao(int v) {
        this.finalizacao = clamp(v);
    }

    public int getCabeceio() {
        return cabeceio;
    }

    public void setCabeceio(int v) {
        this.cabeceio = clamp(v);
    }

    public int getDrible() {
        return drible;
    }

    public void setDrible(int v) {
        this.drible = clamp(v);
    }

    public int getPrimeiroToque() {
        return primeiroToque;
    }

    public void setPrimeiroToque(int v) {
        this.primeiroToque = clamp(v);
    }

    public int getTecnica() {
        return tecnica;
    }

    public void setTecnica(int v) {
        this.tecnica = clamp(v);
    }

    public int getChuteLonga() {
        return chuteLonga;
    }

    public void setChuteLonga(int v) {
        this.chuteLonga = clamp(v);
    }

    public int getCobrancaFalta() {
        return cobrancaFalta;
    }

    public void setCobrancaFalta(int v) {
        this.cobrancaFalta = clamp(v);
    }

    public int getPenalti() {
        return penalti;
    }

    public void setPenalti(int v) {
        this.penalti = clamp(v);
    }

    public int getGoleiro() {
        return goleiro;
    }

    public void setGoleiro(int v) {
        this.goleiro = clamp(v);
    }

    public int getUmContraUm() {
        return umContraUm;
    }

    public void setUmContraUm(int v) {
        this.umContraUm = clamp(v);
    }

    public int getSaida() {
        return saida;
    }

    public void setSaida(int v) {
        this.saida = clamp(v);
    }

    // Mentais
    public int getDecisoes() {
        return decisoes;
    }

    public void setDecisoes(int v) {
        this.decisoes = clamp(v);
    }

    public int getVisao() {
        return visao;
    }

    public void setVisao(int v) {
        this.visao = clamp(v);
    }

    public int getAntecipacao() {
        return antecipacao;
    }

    public void setAntecipacao(int v) {
        this.antecipacao = clamp(v);
    }

    public int getCompostura() {
        return compostura;
    }

    public void setCompostura(int v) {
        this.compostura = clamp(v);
    }

    public int getConcentracao() {
        return concentracao;
    }

    public void setConcentracao(int v) {
        this.concentracao = clamp(v);
    }

    public int getBravura() {
        return bravura;
    }

    public void setBravura(int v) {
        this.bravura = clamp(v);
    }

    public int getTrabalhoEquipa() {
        return trabalhoEquipa;
    }

    public void setTrabalhoEquipa(int v) {
        this.trabalhoEquipa = clamp(v);
    }

    public int getSemBola() {
        return semBola;
    }

    public void setSemBola(int v) {
        this.semBola = clamp(v);
    }

    public int getAgressividade() {
        return agressividade;
    }

    public void setAgressividade(int v) {
        this.agressividade = clamp(v);
    }

    public int getDeterminacao() {
        return determinacao;
    }

    public void setDeterminacao(int v) {
        this.determinacao = clamp(v);
    }

    public int getLideranca() {
        return lideranca;
    }

    public void setLideranca(int v) {
        this.lideranca = clamp(v);
    }

    public int getPosicionamento() {
        return posicionamento;
    }

    public void setPosicionamento(int v) {
        this.posicionamento = clamp(v);
    }

    public int getMarcacao() {
        return marcacao;
    }

    public void setMarcacao(int v) {
        this.marcacao = clamp(v);
    }

    public int getDesarme() {
        return desarme;
    }

    public void setDesarme(int v) {
        this.desarme = clamp(v);
    }

    public int getFlair() {
        return flair;
    }

    public void setFlair(int v) {
        this.flair = clamp(v);
    }

    private int clamp(int valor) {
        return Math.max(1, Math.min(100, valor));
    }
}
