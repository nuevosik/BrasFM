package com.brasfm.model;

/**
 * Traços de personalidade e atributos ocultos do jogador.
 * Estes valores não são visíveis diretamente ao jogador, mas afetam
 * comportamento.
 */
public class PlayerPersonality {

    // ==================== ATRIBUTOS OCULTOS ====================

    /**
     * Consistência (1-20): Define a variância da performance.
     * Baixo = grandes oscilações, Alto = performance estável.
     */
    private int consistencia;

    /**
     * Jogos Importantes (1-20): Performance em finais e clássicos.
     * Baixo = desaparece, Alto = vira herói.
     */
    private int jogosImportantes;

    /**
     * Propensão a Lesões (1-20): Probabilidade de se lesionar.
     * Baixo = propenso a lesões, Alto = raramente se lesiona.
     */
    private int resistenciaLesao;

    /**
     * Versatilidade (1-20): Capacidade de jogar em múltiplas posições.
     */
    private int versatilidade;

    /**
     * Condição Natural (1-20): Velocidade de recuperação entre jogos.
     */
    private int condicaoNatural;

    // ==================== TRAÇOS DE PERSONALIDADE ====================

    /**
     * Profissionalismo (1-20): Disciplina, dedicação ao treino.
     * Baixo = problemas extra-campo, Alto = exemplo de profissional.
     */
    private int profissionalismo;

    /**
     * Ambição (1-20): Desejo de crescer e vencer.
     * Baixo = acomodado, Alto = quer grandes coisas.
     */
    private int ambicao;

    /**
     * Lealdade (1-20): Apego ao clube atual.
     * Baixo = sempre busca transferência, Alto = sangue do clube.
     */
    private int lealdade;

    /**
     * Temperamento (1-20): Controle emocional.
     * Baixo = explosivo/cartões, Alto = frio.
     */
    private int temperamento;

    /**
     * Pressão (1-20): Como lida com pressão da torcida/mídia.
     * Baixo = afetado negativamente, Alto = usa como combustível.
     */
    private int pressao;

    /**
     * Liderança (1-20): Capacidade de influenciar companheiros.
     */
    private int lideranca;

    /**
     * Controverso (1-20): Tendência a criar polêmicas.
     * Baixo = discreto, Alto = sempre na mídia.
     */
    private int controverso;

    /**
     * Adaptabilidade (1-20): Facilidade de se adaptar a novo clube/tática.
     */
    private int adaptabilidade;

    // ==================== ESTADO MENTAL ====================

    /**
     * Felicidade (0-100): Estado atual de satisfação no clube.
     */
    private int felicidade = 70;

    /**
     * Confiança (0-100): Autoconfiança baseada em performances recentes.
     */
    private int confianca = 60;

    // ==================== HISTÓRICO ====================

    private int cartoesPorTemperamento = 0; // Cartões causados por perder a cabeça
    private int problemasExtraCampo = 0; // Incidentes de baixo profissionalismo
    private boolean emConflito = false; // Em conflito com técnico/diretoria
    private String motivoConflito = "";

    public PlayerPersonality() {
        // Gera valores aleatórios razoáveis
        java.util.Random r = new java.util.Random();

        this.consistencia = 10 + r.nextInt(8); // 10-17
        this.jogosImportantes = 8 + r.nextInt(10); // 8-17
        this.resistenciaLesao = 10 + r.nextInt(8);
        this.versatilidade = 5 + r.nextInt(12);
        this.condicaoNatural = 10 + r.nextInt(8);

        this.profissionalismo = 10 + r.nextInt(8);
        this.ambicao = 8 + r.nextInt(10);
        this.lealdade = 8 + r.nextInt(10);
        this.temperamento = 10 + r.nextInt(8);
        this.pressao = 8 + r.nextInt(10);
        this.lideranca = 5 + r.nextInt(12);
        this.controverso = 3 + r.nextInt(8);
        this.adaptabilidade = 10 + r.nextInt(8);
    }

    /**
     * Calcula modificador de performance baseado na consistência.
     * 
     * @return Fator entre 0.7 e 1.3
     */
    public double calcularFatorConsistencia(java.util.Random random) {
        // Quanto maior a consistência, menor a variação
        double variacao = (20 - consistencia) / 20.0 * 0.3; // 0 a 0.3
        double modificador = 1.0 + (random.nextDouble() * 2 - 1) * variacao;
        return Math.max(0.7, Math.min(1.3, modificador));
    }

    /**
     * Calcula modificador para jogos importantes.
     * 
     * @return Fator entre 0.7 e 1.3
     */
    public double calcularFatorJogoImportante(boolean jogoImportante) {
        if (!jogoImportante)
            return 1.0;

        // Jogadores com baixo atributo perdem até 30%
        // Jogadores com alto ganham até 30%
        return 0.7 + (jogosImportantes / 20.0) * 0.6;
    }

    /**
     * Calcula chance de lesão baseado no atributo oculto.
     */
    public double calcularChanceLesao(double fadigaAtual) {
        double baseChance = 0.001; // 0.1% por minuto

        // Baixa resistência = até 3x mais chance
        double multiplicador = 3.0 - (resistenciaLesao / 10.0);

        // Fadiga aumenta risco
        if (fadigaAtual < 30) {
            multiplicador *= 2;
        } else if (fadigaAtual < 50) {
            multiplicador *= 1.5;
        }

        return baseChance * multiplicador;
    }

    /**
     * Calcula recuperação de energia entre jogos.
     */
    public double calcularRecuperacaoEntreJogos(int diasDescanso) {
        // Condição natural afeta velocidade de recuperação
        double taxaBase = 15 + (condicaoNatural * 1.5); // 15-45 por dia
        return Math.min(100, diasDescanso * taxaBase);
    }

    /**
     * Verifica chance de problema extra-campo.
     */
    public boolean verificarProblemaExtraCampo(java.util.Random random) {
        if (profissionalismo >= 15)
            return false;

        // Chance semanal baseada no profissionalismo
        double chance = (20 - profissionalismo) / 1000.0; // 0.5% a 2%

        if (random.nextDouble() < chance) {
            problemasExtraCampo++;
            return true;
        }
        return false;
    }

    /**
     * Verifica se jogador quer sair do clube.
     */
    public boolean querSair(int forcaTime, int forcaJogador, boolean titular) {
        // Alta ambição + baixa lealdade + desvalorizado = quer sair
        if (ambicao < 10)
            return false;

        int insatisfacao = 0;

        if (ambicao > 15 && forcaJogador > forcaTime + 10) {
            insatisfacao += 30; // Muito melhor que o time
        }

        if (!titular && ambicao > 12) {
            insatisfacao += 20;
        }

        if (felicidade < 40) {
            insatisfacao += 30;
        }

        // Lealdade reduz insatisfação
        insatisfacao -= lealdade * 2;

        return insatisfacao > 50;
    }

    /**
     * Verifica chance de cartão por temperamento.
     */
    public double getChanceCartaoTemperamento() {
        // Temperamento baixo = mais chance de perder a cabeça
        return (20 - temperamento) / 400.0; // 0% a 5%
    }

    /**
     * Atualiza felicidade baseado em eventos.
     */
    public void atualizarFelicidade(EventoFelicidade evento) {
        switch (evento) {
            case VITORIA:
                felicidade = Math.min(100, felicidade + 3);
                break;
            case DERROTA:
                felicidade = Math.max(0, felicidade - 5);
                break;
            case GOL_MARCADO:
                felicidade = Math.min(100, felicidade + 5);
                confianca = Math.min(100, confianca + 8);
                break;
            case ERRO_GRAVE:
                felicidade = Math.max(0, felicidade - 10);
                confianca = Math.max(0, confianca - 15);
                break;
            case BANCO:
                felicidade = Math.max(0, felicidade - (ambicao > 12 ? 8 : 3));
                break;
            case TITULAR:
                felicidade = Math.min(100, felicidade + 2);
                break;
            case AUMENTO_SALARIO:
                felicidade = Math.min(100, felicidade + 15);
                break;
            case RECUSA_AUMENTO:
                felicidade = Math.max(0, felicidade - 20);
                break;
            case ELOGIO_TECNICO:
                felicidade = Math.min(100, felicidade + 5);
                confianca = Math.min(100, confianca + 5);
                break;
            case CRITICA_TECNICO:
                felicidade = Math.max(0, felicidade - 8);
                // Alto profissionalismo responde bem a críticas
                if (profissionalismo > 14) {
                    confianca = Math.min(100, confianca + 3);
                } else {
                    confianca = Math.max(0, confianca - 5);
                }
                break;
        }
    }

    /**
     * Verifica se jogador está em boa mentalidade.
     */
    public boolean emBoaMentalidade() {
        return felicidade >= 50 && confianca >= 40 && !emConflito;
    }

    /**
     * Calcula modificador de treino baseado em profissionalismo.
     */
    public double getModificadorTreino() {
        return 0.7 + (profissionalismo / 20.0) * 0.6; // 0.7 a 1.3
    }

    // ==================== GETTERS E SETTERS ====================

    public int getConsistencia() {
        return consistencia;
    }

    public void setConsistencia(int v) {
        this.consistencia = clamp(v);
    }

    public int getJogosImportantes() {
        return jogosImportantes;
    }

    public void setJogosImportantes(int v) {
        this.jogosImportantes = clamp(v);
    }

    public int getResistenciaLesao() {
        return resistenciaLesao;
    }

    public void setResistenciaLesao(int v) {
        this.resistenciaLesao = clamp(v);
    }

    public int getVersatilidade() {
        return versatilidade;
    }

    public void setVersatilidade(int v) {
        this.versatilidade = clamp(v);
    }

    public int getCondicaoNatural() {
        return condicaoNatural;
    }

    public void setCondicaoNatural(int v) {
        this.condicaoNatural = clamp(v);
    }

    public int getProfissionalismo() {
        return profissionalismo;
    }

    public void setProfissionalismo(int v) {
        this.profissionalismo = clamp(v);
    }

    public int getAmbicao() {
        return ambicao;
    }

    public void setAmbicao(int v) {
        this.ambicao = clamp(v);
    }

    public int getLealdade() {
        return lealdade;
    }

    public void setLealdade(int v) {
        this.lealdade = clamp(v);
    }

    public int getTemperamento() {
        return temperamento;
    }

    public void setTemperamento(int v) {
        this.temperamento = clamp(v);
    }

    public int getPressao() {
        return pressao;
    }

    public void setPressao(int v) {
        this.pressao = clamp(v);
    }

    public int getLideranca() {
        return lideranca;
    }

    public void setLideranca(int v) {
        this.lideranca = clamp(v);
    }

    public int getControverso() {
        return controverso;
    }

    public void setControverso(int v) {
        this.controverso = clamp(v);
    }

    public int getAdaptabilidade() {
        return adaptabilidade;
    }

    public void setAdaptabilidade(int v) {
        this.adaptabilidade = clamp(v);
    }

    public int getFelicidade() {
        return felicidade;
    }

    public void setFelicidade(int v) {
        this.felicidade = Math.max(0, Math.min(100, v));
    }

    public int getConfianca() {
        return confianca;
    }

    public void setConfianca(int v) {
        this.confianca = Math.max(0, Math.min(100, v));
    }

    public boolean isEmConflito() {
        return emConflito;
    }

    public void setEmConflito(boolean v) {
        this.emConflito = v;
    }

    public String getMotivoConflito() {
        return motivoConflito;
    }

    public void setMotivoConflito(String v) {
        this.motivoConflito = v;
    }

    public int getProblemasExtraCampo() {
        return problemasExtraCampo;
    }

    private int clamp(int v) {
        return Math.max(1, Math.min(20, v));
    }

    public enum EventoFelicidade {
        VITORIA, DERROTA, GOL_MARCADO, ERRO_GRAVE,
        BANCO, TITULAR, AUMENTO_SALARIO, RECUSA_AUMENTO,
        ELOGIO_TECNICO, CRITICA_TECNICO
    }
}
