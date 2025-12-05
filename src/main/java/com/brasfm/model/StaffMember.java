package com.brasfm.model;

/**
 * Membro do staff técnico/administrativo do clube.
 */
public class StaffMember {

    public enum TipoStaff {
        TREINADOR_GOLEIROS("Treinador de Goleiros", "Melhora atributos de goleiros"),
        TREINADOR_DEFESA("Treinador de Defesa", "Melhora atributos defensivos"),
        TREINADOR_ATAQUE("Treinador de Ataque", "Melhora finalização e criação"),
        PREPARADOR_FISICO("Preparador Físico", "Melhora condição física e recuperação"),
        FISIOTERAPEUTA("Fisioterapeuta", "Reduz tempo de recuperação de lesões"),
        CIENTISTA_DESPORTO("Cientista do Desporto", "Otimiza treinos e previne lesões"),
        ANALISTA("Analista", "Melhora conhecimento sobre adversários"),
        OLHEIRO("Olheiro", "Descobre novos talentos"),
        DIRETOR_BASE("Diretor da Base", "Melhora qualidade dos juniores");

        private final String nome;
        private final String descricao;

        TipoStaff(String nome, String descricao) {
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

    private String nome;
    private TipoStaff tipo;
    private int habilidade; // 1-20
    private int experiencia; // Anos de experiência
    private int salarioSemanal;
    private int semanasContrato;

    public StaffMember(String nome, TipoStaff tipo, int habilidade) {
        this.nome = nome;
        this.tipo = tipo;
        this.habilidade = Math.max(1, Math.min(20, habilidade));
        this.experiencia = habilidade / 2;
        this.salarioSemanal = habilidade * 500;
        this.semanasContrato = 52;
    }

    /**
     * Calcula o bônus de desenvolvimento que este membro aplica.
     * 
     * @return Multiplicador entre 0.8 e 1.5
     */
    public double getBonusDesenvolvimento() {
        return 0.8 + (habilidade / 20.0) * 0.7;
    }

    /**
     * Calcula redução de tempo de lesão (fisioterapeuta).
     */
    public double getReducaoLesao() {
        if (tipo != TipoStaff.FISIOTERAPEUTA)
            return 1.0;
        return 1.0 - (habilidade / 40.0); // Até 50% de redução
    }

    /**
     * Calcula redução de chance de lesão (cientista).
     */
    public double getReducaoChanceLesao() {
        if (tipo != TipoStaff.CIENTISTA_DESPORTO)
            return 1.0;
        return 1.0 - (habilidade / 50.0); // Até 40% de redução
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoStaff getTipo() {
        return tipo;
    }

    public int getHabilidade() {
        return habilidade;
    }

    public void setHabilidade(int v) {
        this.habilidade = Math.max(1, Math.min(20, v));
    }

    public int getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(int v) {
        this.experiencia = v;
    }

    public int getSalarioSemanal() {
        return salarioSemanal;
    }

    public void setSalarioSemanal(int v) {
        this.salarioSemanal = v;
    }

    public int getSemanasContrato() {
        return semanasContrato;
    }

    public void setSemanasContrato(int v) {
        this.semanasContrato = v;
    }

    public void passarSemana() {
        if (semanasContrato > 0)
            semanasContrato--;
    }

    @Override
    public String toString() {
        return nome + " (" + tipo.getNome() + " - " + habilidade + ")";
    }
}
