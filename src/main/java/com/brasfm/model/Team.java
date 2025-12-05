package com.brasfm.model;

import com.brasfm.model.enums.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Representa um time de futebol completo.
 */
public class Team {
    private String nome;
    private String sigla;
    private String pais;
    private String estado; // Para times brasileiros
    private int divisao; // 1 = primeira divisão, 2 = segunda, etc.
    private String escudoPath; // Caminho para o arquivo do escudo

    // Elenco
    private List<Player> jogadores;
    private List<Player> titulares; // 11 escalados
    private List<Player> reservas; // até 7 reservas
    private List<Player> juniores; // academia de base

    // Infraestrutura
    private Stadium estadio;
    private Tatica tatica;

    // Finanças
    private long saldo;
    private int patrocinioAnual;

    // Estatísticas da temporada
    private int pontos;
    private int vitorias;
    private int empates;
    private int derrotas;
    private int golsPro;
    private int golsContra;

    // Ranking
    private int rankingPontos;
    private int rankingTitulos;

    // Controle
    private boolean timeHumano;
    private int moralTorcida; // 0-100

    public Team(String nome, String sigla) {
        this.nome = nome;
        this.sigla = sigla;
        this.jogadores = new ArrayList<>();
        this.titulares = new ArrayList<>();
        this.reservas = new ArrayList<>();
        this.juniores = new ArrayList<>();
        this.tatica = new Tatica();
        this.divisao = 1;
        this.moralTorcida = 50;
    }

    /**
     * Adiciona um jogador ao elenco.
     */
    public void addJogador(Player jogador) {
        jogadores.add(jogador);
    }

    /**
     * Remove um jogador do elenco.
     */
    public void removeJogador(Player jogador) {
        jogadores.remove(jogador);
        titulares.remove(jogador);
        reservas.remove(jogador);
    }

    /**
     * Escala um jogador como titular.
     */
    public boolean escalarTitular(Player jogador, Position posicao) {
        if (!jogador.podeJogar()) {
            return false;
        }
        if (titulares.size() >= 11) {
            return false;
        }

        // Remove de onde estava
        titulares.remove(jogador);
        reservas.remove(jogador);

        jogador.setPosicao(posicao);
        titulares.add(jogador);
        return true;
    }

    /**
     * Escala um jogador como reserva.
     */
    public boolean escalarReserva(Player jogador) {
        if (!jogador.podeJogar()) {
            return false;
        }
        if (reservas.size() >= 7) {
            return false;
        }

        titulares.remove(jogador);
        reservas.remove(jogador);

        reservas.add(jogador);
        return true;
    }

    /**
     * Calcula a força média do time titular.
     */
    public int getForcaTime() {
        if (titulares.isEmpty()) {
            return 0;
        }
        return (int) titulares.stream()
                .mapToInt(Player::getForcaAtual)
                .average()
                .orElse(0);
    }

    /**
     * Calcula a força do ataque.
     */
    public int getForcaAtaque() {
        List<Player> atacantes = titulares.stream()
                .filter(p -> p.getPosicao().isOfensiva())
                .collect(Collectors.toList());

        if (atacantes.isEmpty()) {
            return 0;
        }

        return (int) atacantes.stream()
                .mapToInt(p -> (p.getFinalizacaoEfetiva() + p.getForcaAtual()) / 2)
                .average()
                .orElse(0);
    }

    /**
     * Calcula a força da defesa.
     */
    public int getForcaDefesa() {
        List<Player> defensores = titulares.stream()
                .filter(p -> p.getPosicao().isDefensiva() || p.isGoleiro())
                .collect(Collectors.toList());

        if (defensores.isEmpty()) {
            return 0;
        }

        return (int) defensores.stream()
                .mapToInt(p -> (p.getDesarmeEfetivo() + p.getForcaAtual()) / 2)
                .average()
                .orElse(0);
    }

    /**
     * Calcula a força do meio-campo.
     */
    public int getForcaMeioCampo() {
        List<Player> meias = titulares.stream()
                .filter(p -> !p.getPosicao().isDefensiva() && !p.getPosicao().isOfensiva() && !p.isGoleiro())
                .collect(Collectors.toList());

        if (meias.isEmpty()) {
            return 0;
        }

        return (int) meias.stream()
                .mapToInt(p -> (p.getPasseEfetivo() + p.getForcaAtual()) / 2)
                .average()
                .orElse(0);
    }

    /**
     * Retorna o goleiro titular.
     */
    public Player getGoleiro() {
        return titulares.stream()
                .filter(Player::isGoleiro)
                .findFirst()
                .orElse(null);
    }

    /**
     * Verifica se a escalação está completa e válida.
     */
    public boolean escalacaoValida() {
        if (titulares.size() != 11) {
            return false;
        }

        // Deve ter exatamente 1 goleiro
        long goleiros = titulares.stream().filter(Player::isGoleiro).count();
        return goleiros == 1;
    }

    /**
     * Paga salários de todos os jogadores.
     */
    public void pagarSalarios() {
        int totalSalarios = jogadores.stream()
                .mapToInt(Player::getSalario)
                .sum();
        saldo -= totalSalarios;
    }

    /**
     * Recebe patrocínio (normalmente no início da temporada).
     */
    public void receberPatrocinio() {
        saldo += patrocinioAnual;
    }

    /**
     * Recebe renda de um jogo.
     */
    public void receberRenda(int renda) {
        saldo += renda;
    }

    /**
     * Recebe valor específico (transferência, prêmio, etc.)
     */
    public void receberPatrocinio(int valor) {
        saldo += valor;
    }

    /**
     * Paga uma despesa.
     */
    public void pagarDespesa(int valor) {
        saldo -= valor;
    }

    /**
     * Retorna a força média do elenco completo.
     */
    public int getForcaMedia() {
        if (jogadores.isEmpty())
            return 0;
        return (int) jogadores.stream()
                .mapToInt(Player::getForca)
                .average()
                .orElse(0);
    }

    /**
     * Registra resultado de uma partida.
     */
    public void registrarResultado(int golsFeitos, int golsSofridos) {
        this.golsPro += golsFeitos;
        this.golsContra += golsSofridos;

        if (golsFeitos > golsSofridos) {
            vitorias++;
            pontos += 3;
            moralTorcida = Math.min(100, moralTorcida + 5);
        } else if (golsFeitos == golsSofridos) {
            empates++;
            pontos += 1;
        } else {
            derrotas++;
            moralTorcida = Math.max(0, moralTorcida - 5);
        }
    }

    /**
     * Retorna o saldo de gols.
     */
    public int getSaldoGols() {
        return golsPro - golsContra;
    }

    /**
     * Retorna jogadores disponíveis para escalação.
     */
    public List<Player> getJogadoresDisponiveis() {
        return jogadores.stream()
                .filter(Player::podeJogar)
                .collect(Collectors.toList());
    }

    /**
     * Retorna jogadores por posição.
     */
    public List<Player> getJogadoresPorPosicao(Position posicao) {
        return jogadores.stream()
                .filter(p -> p.getPosicaoOriginal() == posicao)
                .collect(Collectors.toList());
    }

    /**
     * Recupera energia de todos os jogadores após descanso.
     */
    public void recuperarJogadores() {
        for (Player p : jogadores) {
            p.recuperarEnergia(30);
        }
    }

    /**
     * Passa uma semana.
     */
    public void passarSemana() {
        for (Player p : jogadores) {
            p.passarSemana();
        }
        if (estadio != null) {
            estadio.passarSemana();
        }
    }

    /**
     * Reseta estatísticas para nova temporada.
     */
    public void novaTemporada() {
        pontos = 0;
        vitorias = 0;
        empates = 0;
        derrotas = 0;
        golsPro = 0;
        golsContra = 0;
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getDivisao() {
        return divisao;
    }

    public void setDivisao(int divisao) {
        this.divisao = divisao;
    }

    public List<Player> getJogadores() {
        return jogadores;
    }

    public List<Player> getTitulares() {
        return titulares;
    }

    public List<Player> getReservas() {
        return reservas;
    }

    public List<Player> getJuniores() {
        return juniores;
    }

    public Stadium getEstadio() {
        return estadio;
    }

    public void setEstadio(Stadium estadio) {
        this.estadio = estadio;
    }

    public Tatica getTatica() {
        return tatica;
    }

    public void setTatica(Tatica tatica) {
        this.tatica = tatica;
    }

    public long getSaldo() {
        return saldo;
    }

    public void setSaldo(long saldo) {
        this.saldo = saldo;
    }

    public int getPatrocinioAnual() {
        return patrocinioAnual;
    }

    public void setPatrocinioAnual(int patrocinioAnual) {
        this.patrocinioAnual = patrocinioAnual;
    }

    public int getPontos() {
        return pontos;
    }

    public int getVitorias() {
        return vitorias;
    }

    public int getEmpates() {
        return empates;
    }

    public int getDerrotas() {
        return derrotas;
    }

    public int getGolsPro() {
        return golsPro;
    }

    public int getGolsContra() {
        return golsContra;
    }

    public int getRankingPontos() {
        return rankingPontos;
    }

    public void setRankingPontos(int rankingPontos) {
        this.rankingPontos = rankingPontos;
    }

    public int getRankingTitulos() {
        return rankingTitulos;
    }

    public void setRankingTitulos(int rankingTitulos) {
        this.rankingTitulos = rankingTitulos;
    }

    public boolean isTimeHumano() {
        return timeHumano;
    }

    public void setTimeHumano(boolean timeHumano) {
        this.timeHumano = timeHumano;
    }

    public int getMoralTorcida() {
        return moralTorcida;
    }

    public void setMoralTorcida(int moralTorcida) {
        this.moralTorcida = moralTorcida;
    }

    public String getEscudoPath() {
        return escudoPath;
    }

    public void setEscudoPath(String escudoPath) {
        this.escudoPath = escudoPath;
    }

    @Override
    public String toString() {
        return nome + " (" + sigla + ")";
    }
}
