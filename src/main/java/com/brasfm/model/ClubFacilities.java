package com.brasfm.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Instalações do clube que afetam desenvolvimento e recuperação.
 */
public class ClubFacilities {

    /**
     * Centro de Treino (1-20): Qualidade das instalações de treino.
     * Afeta taxa de evolução dos jogadores.
     */
    private int centroTreino = 10;

    /**
     * Academia de Base (1-20): Qualidade da formação de jovens.
     * Afeta potencial e quantidade de regenerados.
     */
    private int academiaBase = 10;

    /**
     * Departamento Médico (1-20): Qualidade do tratamento médico.
     * Reduz tempo de recuperação de lesões.
     */
    private int departamentoMedico = 10;

    /**
     * Centro de Recuperação (1-20): Instalações de fisioterapia.
     * Melhora recuperação de fadiga entre jogos.
     */
    private int centroRecuperacao = 10;

    /**
     * Ciência do Desporto (1-20): Tecnologia e análise de dados.
     * Reduz incidência de lesões e otimiza treinos.
     */
    private int cienciaDesporto = 10;

    /**
     * Alojamento (1-20): Qualidade das instalações para jogadores.
     * Afeta felicidade e adaptação de contratações.
     */
    private int alojamento = 10;

    // Staff do clube
    private List<StaffMember> staff = new ArrayList<>();

    // Upgrades em andamento
    private String upgradeEmAndamento = null;
    private int semanasParaConclusao = 0;
    private long custoUpgrade = 0;

    public ClubFacilities() {
    }

    /**
     * Calcula o bônus de treino baseado nas instalações.
     * 
     * @return Multiplicador entre 0.5 e 1.5
     */
    public double getBonusTreino() {
        double base = 0.5 + (centroTreino / 20.0);

        // Bônus do staff
        for (StaffMember s : staff) {
            if (s.getTipo() == StaffMember.TipoStaff.CIENTISTA_DESPORTO) {
                base *= s.getBonusDesenvolvimento();
            }
        }

        return Math.min(1.5, base);
    }

    /**
     * Calcula modificador de recuperação de fadiga.
     */
    public double getModificadorRecuperacao() {
        double base = 0.7 + (centroRecuperacao / 20.0) * 0.6;

        for (StaffMember s : staff) {
            if (s.getTipo() == StaffMember.TipoStaff.PREPARADOR_FISICO) {
                base *= s.getBonusDesenvolvimento();
            }
        }

        return Math.min(1.5, base);
    }

    /**
     * Calcula redução de tempo de lesão.
     */
    public double getReducaoTempoLesao() {
        double reducao = departamentoMedico / 40.0; // Até 50%

        for (StaffMember s : staff) {
            if (s.getTipo() == StaffMember.TipoStaff.FISIOTERAPEUTA) {
                reducao += (1 - s.getReducaoLesao()) / 2;
            }
        }

        return Math.min(0.6, reducao);
    }

    /**
     * Calcula redução de chance de lesão.
     */
    public double getReducaoChanceLesao() {
        double reducao = cienciaDesporto / 50.0; // Até 40%

        for (StaffMember s : staff) {
            if (s.getTipo() == StaffMember.TipoStaff.CIENTISTA_DESPORTO) {
                reducao += (1 - s.getReducaoChanceLesao()) / 2;
            }
        }

        return Math.min(0.5, reducao);
    }

    /**
     * Calcula qualidade média dos regenerados da base.
     */
    public int getPotencialMedioBase() {
        int base = 50 + academiaBase * 2; // 52 a 90

        for (StaffMember s : staff) {
            if (s.getTipo() == StaffMember.TipoStaff.DIRETOR_BASE) {
                base += s.getHabilidade();
            }
        }

        return Math.min(95, base);
    }

    /**
     * Calcula bônus de adaptação para novos jogadores.
     */
    public double getBonusAdaptacao() {
        return 0.7 + (alojamento / 20.0) * 0.6;
    }

    /**
     * Inicia upgrade de uma instalação.
     */
    public boolean iniciarUpgrade(String instalacao, long orcamento) {
        if (upgradeEmAndamento != null) {
            return false; // Já tem upgrade em andamento
        }

        int nivelAtual = getNivelInstalacao(instalacao);
        if (nivelAtual >= 20) {
            return false; // Já no máximo
        }

        long custo = calcularCustoUpgrade(instalacao, nivelAtual);
        if (custo > orcamento) {
            return false;
        }

        upgradeEmAndamento = instalacao;
        semanasParaConclusao = 4 + nivelAtual; // 4-24 semanas
        custoUpgrade = custo;

        return true;
    }

    /**
     * Processa passagem de semana.
     */
    public void passarSemana() {
        // Staff
        for (StaffMember s : staff) {
            s.passarSemana();
        }
        staff.removeIf(s -> s.getSemanasContrato() <= 0);

        // Upgrades
        if (upgradeEmAndamento != null && semanasParaConclusao > 0) {
            semanasParaConclusao--;
            if (semanasParaConclusao == 0) {
                completarUpgrade();
            }
        }
    }

    private void completarUpgrade() {
        if (upgradeEmAndamento == null)
            return;

        switch (upgradeEmAndamento.toLowerCase()) {
            case "centro de treino":
                centroTreino++;
                break;
            case "academia":
                academiaBase++;
                break;
            case "médico":
                departamentoMedico++;
                break;
            case "recuperação":
                centroRecuperacao++;
                break;
            case "ciência":
                cienciaDesporto++;
                break;
            case "alojamento":
                alojamento++;
                break;
        }

        upgradeEmAndamento = null;
        custoUpgrade = 0;
    }

    private int getNivelInstalacao(String instalacao) {
        switch (instalacao.toLowerCase()) {
            case "centro de treino":
                return centroTreino;
            case "academia":
                return academiaBase;
            case "médico":
                return departamentoMedico;
            case "recuperação":
                return centroRecuperacao;
            case "ciência":
                return cienciaDesporto;
            case "alojamento":
                return alojamento;
            default:
                return 0;
        }
    }

    private long calcularCustoUpgrade(String instalacao, int nivelAtual) {
        // Custo exponencial: ~500k no nível 1, ~10M no nível 19
        return (long) (500000 * Math.pow(1.2, nivelAtual));
    }

    /**
     * Adiciona membro ao staff.
     */
    public void addStaff(StaffMember membro) {
        staff.add(membro);
    }

    /**
     * Remove membro do staff.
     */
    public void removeStaff(StaffMember membro) {
        staff.remove(membro);
    }

    /**
     * Retorna staff de um tipo específico.
     */
    public StaffMember getStaffPorTipo(StaffMember.TipoStaff tipo) {
        return staff.stream()
                .filter(s -> s.getTipo() == tipo)
                .findFirst()
                .orElse(null);
    }

    /**
     * Calcula custo semanal total do staff.
     */
    public int getCustoStaffSemanal() {
        return staff.stream().mapToInt(StaffMember::getSalarioSemanal).sum();
    }

    // Getters e Setters
    public int getCentroTreino() {
        return centroTreino;
    }

    public void setCentroTreino(int v) {
        this.centroTreino = clamp(v);
    }

    public int getAcademiaBase() {
        return academiaBase;
    }

    public void setAcademiaBase(int v) {
        this.academiaBase = clamp(v);
    }

    public int getDepartamentoMedico() {
        return departamentoMedico;
    }

    public void setDepartamentoMedico(int v) {
        this.departamentoMedico = clamp(v);
    }

    public int getCentroRecuperacao() {
        return centroRecuperacao;
    }

    public void setCentroRecuperacao(int v) {
        this.centroRecuperacao = clamp(v);
    }

    public int getCienciaDesporto() {
        return cienciaDesporto;
    }

    public void setCienciaDesporto(int v) {
        this.cienciaDesporto = clamp(v);
    }

    public int getAlojamento() {
        return alojamento;
    }

    public void setAlojamento(int v) {
        this.alojamento = clamp(v);
    }

    public List<StaffMember> getStaff() {
        return staff;
    }

    public String getUpgradeEmAndamento() {
        return upgradeEmAndamento;
    }

    public int getSemanasParaConclusao() {
        return semanasParaConclusao;
    }

    public long getCustoUpgrade() {
        return custoUpgrade;
    }

    private int clamp(int v) {
        return Math.max(1, Math.min(20, v));
    }
}
