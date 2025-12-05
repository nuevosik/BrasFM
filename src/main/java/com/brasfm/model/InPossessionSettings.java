package com.brasfm.model;

/**
 * Configurações táticas para a fase COM POSSE de bola.
 */
public class InPossessionSettings {

    public enum Ritmo {
        MUITO_LENTO("Muito Lento", 0.3),
        LENTO("Lento", 0.5),
        NORMAL("Normal", 0.7),
        RAPIDO("Rápido", 0.85),
        MUITO_RAPIDO("Muito Rápido", 1.0);

        private final String nome;
        private final double fator;

        Ritmo(String nome, double fator) {
            this.nome = nome;
            this.fator = fator;
        }

        public String getNome() {
            return nome;
        }

        public double getFator() {
            return fator;
        }
    }

    public enum Largura {
        MUITO_ESTREITO("Muito Estreito", 0.4),
        ESTREITO("Estreito", 0.6),
        NORMAL("Normal", 0.75),
        LARGO("Largo", 0.9),
        MUITO_LARGO("Muito Largo", 1.0);

        private final String nome;
        private final double fator;

        Largura(String nome, double fator) {
            this.nome = nome;
            this.fator = fator;
        }

        public String getNome() {
            return nome;
        }

        public double getFator() {
            return fator;
        }
    }

    public enum TipoPasse {
        CURTO("Passes Curtos", "Prioriza posse e construção lenta"),
        MISTO("Misto", "Varia entre curto e direto"),
        DIRETO("Passes Diretos", "Busca profundidade rapidamente");

        private final String nome;
        private final String descricao;

        TipoPasse(String nome, String descricao) {
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

    public enum Criatividade {
        MUITO_RESTRITA("Muito Restrita", 0.3),
        RESTRITA("Restrita", 0.5),
        NORMAL("Normal", 0.7),
        LIVRE("Livre", 0.85),
        LIBERDADE_TOTAL("Liberdade Total", 1.0);

        private final String nome;
        private final double fator;

        Criatividade(String nome, double fator) {
            this.nome = nome;
            this.fator = fator;
        }

        public String getNome() {
            return nome;
        }

        public double getFator() {
            return fator;
        }
    }

    private Ritmo ritmo = Ritmo.NORMAL;
    private Largura largura = Largura.NORMAL;
    private TipoPasse tipoPasse = TipoPasse.MISTO;
    private Criatividade criatividade = Criatividade.NORMAL;
    private boolean jogarPelasLaterais = false;
    private boolean jogarPeloMeio = false;
    private boolean cruzamentosRasteiros = false;
    private boolean lancamentosLongos = false;

    public InPossessionSettings() {
    }

    // Presets
    public static InPossessionSettings tikitaka() {
        InPossessionSettings s = new InPossessionSettings();
        s.ritmo = Ritmo.LENTO;
        s.largura = Largura.ESTREITO;
        s.tipoPasse = TipoPasse.CURTO;
        s.criatividade = Criatividade.RESTRITA;
        return s;
    }

    public static InPossessionSettings contraAtaque() {
        InPossessionSettings s = new InPossessionSettings();
        s.ritmo = Ritmo.MUITO_RAPIDO;
        s.tipoPasse = TipoPasse.DIRETO;
        s.lancamentosLongos = true;
        return s;
    }

    public static InPossessionSettings possessao() {
        InPossessionSettings s = new InPossessionSettings();
        s.ritmo = Ritmo.LENTO;
        s.tipoPasse = TipoPasse.CURTO;
        s.largura = Largura.LARGO;
        return s;
    }

    // Getters e Setters
    public Ritmo getRitmo() {
        return ritmo;
    }

    public void setRitmo(Ritmo ritmo) {
        this.ritmo = ritmo;
    }

    public Largura getLargura() {
        return largura;
    }

    public void setLargura(Largura largura) {
        this.largura = largura;
    }

    public TipoPasse getTipoPasse() {
        return tipoPasse;
    }

    public void setTipoPasse(TipoPasse tipoPasse) {
        this.tipoPasse = tipoPasse;
    }

    public Criatividade getCriatividade() {
        return criatividade;
    }

    public void setCriatividade(Criatividade criatividade) {
        this.criatividade = criatividade;
    }

    public boolean isJogarPelasLaterais() {
        return jogarPelasLaterais;
    }

    public void setJogarPelasLaterais(boolean v) {
        this.jogarPelasLaterais = v;
    }

    public boolean isJogarPeloMeio() {
        return jogarPeloMeio;
    }

    public void setJogarPeloMeio(boolean v) {
        this.jogarPeloMeio = v;
    }

    public boolean isCruzamentosRasteiros() {
        return cruzamentosRasteiros;
    }

    public void setCruzamentosRasteiros(boolean v) {
        this.cruzamentosRasteiros = v;
    }

    public boolean isLancamentosLongos() {
        return lancamentosLongos;
    }

    public void setLancamentosLongos(boolean v) {
        this.lancamentosLongos = v;
    }
}
