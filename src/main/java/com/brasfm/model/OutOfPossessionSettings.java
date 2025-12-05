package com.brasfm.model;

/**
 * Configurações táticas para a fase SEM POSSE de bola.
 */
public class OutOfPossessionSettings {

    public enum LinhaEngajamento {
        MUITO_BAIXA("Muito Baixa", 25, "Espera adversário na própria área"),
        BAIXA("Baixa", 35, "Bloco baixo, pressiona só no meio defensivo"),
        NORMAL("Normal", 50, "Linha média, pressão equilibrada"),
        ALTA("Alta", 65, "Pressão no meio ofensivo"),
        MUITO_ALTA("Muito Alta", 80, "Pressiona desde o ataque adversário");

        private final String nome;
        private final int posicaoInicial; // 0-100, onde 100 é o gol adversário
        private final String descricao;

        LinhaEngajamento(String nome, int posicaoInicial, String descricao) {
            this.nome = nome;
            this.posicaoInicial = posicaoInicial;
            this.descricao = descricao;
        }

        public String getNome() {
            return nome;
        }

        public int getPosicaoInicial() {
            return posicaoInicial;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum LinhaDefensiva {
        MUITO_PROFUNDA("Muito Profunda", 15, "Zagueiros na pequena área"),
        PROFUNDA("Profunda", 25, "Defesa recuada"),
        NORMAL("Normal", 35, "Linha média"),
        ALTA("Alta", 50, "Linha avançada, usa impedimento"),
        MUITO_ALTA("Muito Alta", 60, "Linha altíssima, armadilha de impedimento");

        private final String nome;
        private final int posicao; // 0-100
        private final String descricao;

        LinhaDefensiva(String nome, int posicao, String descricao) {
            this.nome = nome;
            this.posicao = posicao;
            this.descricao = descricao;
        }

        public String getNome() {
            return nome;
        }

        public int getPosicao() {
            return posicao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum IntensidadePressao {
        PASSIVA("Passiva", 0.2, 0.5, "Deixa adversário jogar, poupa energia"),
        LEVE("Leve", 0.4, 0.7, "Pressão ocasional"),
        NORMAL("Normal", 0.6, 1.0, "Pressão equilibrada"),
        INTENSA("Intensa", 0.8, 1.4, "Marca sob pressão constante"),
        GEGENPRESS("Gegenpress", 1.0, 2.0, "Pressão imediata após perda"); // Consome muito mais energia

        private final String nome;
        private final double intensidade;
        private final double custoEnergia; // Multiplicador do gasto de energia
        private final String descricao;

        IntensidadePressao(String nome, double intensidade, double custoEnergia, String descricao) {
            this.nome = nome;
            this.intensidade = intensidade;
            this.custoEnergia = custoEnergia;
            this.descricao = descricao;
        }

        public String getNome() {
            return nome;
        }

        public double getIntensidade() {
            return intensidade;
        }

        public double getCustoEnergia() {
            return custoEnergia;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum LarguraDefensiva {
        MUITO_ESTREITA("Muito Estreita", 0.4, "Todos no meio, expõe laterais"),
        ESTREITA("Estreita", 0.6, "Compacto pelo meio"),
        NORMAL("Normal", 0.75, "Cobertura equilibrada"),
        LARGA("Larga", 0.9, "Cobre laterais, expõe centro"),
        MUITO_LARGA("Muito Larga", 1.0, "Cobertura total, time espaçado");

        private final String nome;
        private final double fator;
        private final String descricao;

        LarguraDefensiva(String nome, double fator, String descricao) {
            this.nome = nome;
            this.fator = fator;
            this.descricao = descricao;
        }

        public String getNome() {
            return nome;
        }

        public double getFator() {
            return fator;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    private LinhaEngajamento linhaEngajamento = LinhaEngajamento.NORMAL;
    private LinhaDefensiva linhaDefensiva = LinhaDefensiva.NORMAL;
    private IntensidadePressao intensidadePressao = IntensidadePressao.NORMAL;
    private LarguraDefensiva larguraDefensiva = LarguraDefensiva.NORMAL;
    private boolean marcacaoHomem = false; // vs zona
    private boolean armadilhaImpedimento = false;
    private boolean restDefense = false; // Mantém jogadores atrás durante ataque

    public OutOfPossessionSettings() {
    }

    // Presets
    public static OutOfPossessionSettings blocoAlto() {
        OutOfPossessionSettings s = new OutOfPossessionSettings();
        s.linhaEngajamento = LinhaEngajamento.MUITO_ALTA;
        s.linhaDefensiva = LinhaDefensiva.ALTA;
        s.intensidadePressao = IntensidadePressao.INTENSA;
        s.armadilhaImpedimento = true;
        return s;
    }

    public static OutOfPossessionSettings blocoBaixo() {
        OutOfPossessionSettings s = new OutOfPossessionSettings();
        s.linhaEngajamento = LinhaEngajamento.BAIXA;
        s.linhaDefensiva = LinhaDefensiva.PROFUNDA;
        s.intensidadePressao = IntensidadePressao.PASSIVA;
        s.larguraDefensiva = LarguraDefensiva.ESTREITA;
        return s;
    }

    public static OutOfPossessionSettings gegenpress() {
        OutOfPossessionSettings s = new OutOfPossessionSettings();
        s.linhaEngajamento = LinhaEngajamento.MUITO_ALTA;
        s.linhaDefensiva = LinhaDefensiva.ALTA;
        s.intensidadePressao = IntensidadePressao.GEGENPRESS;
        return s;
    }

    // Getters e Setters
    public LinhaEngajamento getLinhaEngajamento() {
        return linhaEngajamento;
    }

    public void setLinhaEngajamento(LinhaEngajamento v) {
        this.linhaEngajamento = v;
    }

    public LinhaDefensiva getLinhaDefensiva() {
        return linhaDefensiva;
    }

    public void setLinhaDefensiva(LinhaDefensiva v) {
        this.linhaDefensiva = v;
    }

    public IntensidadePressao getIntensidadePressao() {
        return intensidadePressao;
    }

    public void setIntensidadePressao(IntensidadePressao v) {
        this.intensidadePressao = v;
    }

    public LarguraDefensiva getLarguraDefensiva() {
        return larguraDefensiva;
    }

    public void setLarguraDefensiva(LarguraDefensiva v) {
        this.larguraDefensiva = v;
    }

    public boolean isMarcacaoHomem() {
        return marcacaoHomem;
    }

    public void setMarcacaoHomem(boolean v) {
        this.marcacaoHomem = v;
    }

    public boolean isArmadilhaImpedimento() {
        return armadilhaImpedimento;
    }

    public void setArmadilhaImpedimento(boolean v) {
        this.armadilhaImpedimento = v;
    }

    public boolean isRestDefense() {
        return restDefense;
    }

    public void setRestDefense(boolean v) {
        this.restDefense = v;
    }
}
