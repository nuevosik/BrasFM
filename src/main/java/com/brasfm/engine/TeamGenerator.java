package com.brasfm.engine;

import com.brasfm.model.*;
import com.brasfm.model.enums.*;
import java.util.*;

/**
 * Gerador de times e jogadores para teste e início do jogo.
 */
public class TeamGenerator {
    private Random random;
    private String[] nomesBrasileiros = {
            "Pedro", "Lucas", "Gabriel", "Matheus", "Rafael", "Bruno", "Felipe", "Gustavo",
            "André", "Carlos", "Diego", "Eduardo", "Fernando", "Henrique", "Igor", "João",
            "Leonardo", "Marcos", "Nicolas", "Paulo", "Ricardo", "Thiago", "Victor", "William",
            "Alex", "Daniel", "Enzo", "Fabio", "Guilherme", "Hugo", "Júlio", "Kaio",
            "Luan", "Miguel", "Nathan", "Otávio", "Patrick", "Renan", "Samuel", "Tales"
    };

    private String[] sobrenomes = {
            "Silva", "Santos", "Oliveira", "Souza", "Lima", "Pereira", "Costa", "Ferreira",
            "Rodrigues", "Almeida", "Nascimento", "Araújo", "Melo", "Barbosa", "Ribeiro", "Martins",
            "Carvalho", "Rocha", "Gomes", "Nunes", "Moura", "Dias", "Mendes", "Freitas"
    };

    public TeamGenerator() {
        this.random = new Random();
    }

    /**
     * Gera um time completo com jogadores.
     */
    public Team gerarTime(String nome, String sigla, int forcaMedia, String pais, String estado) {
        Team time = new Team(nome, sigla);
        time.setPais(pais);
        time.setEstado(estado);

        // Gera estádio
        Stadium estadio = new Stadium("Estádio " + nome, 20000 + random.nextInt(40000));
        time.setEstadio(estadio);

        // Define finanças
        time.setSaldo(5000000 + random.nextInt(15000000));
        time.setPatrocinioAnual(2000000 + random.nextInt(8000000));

        // Gera jogadores por posição
        // 3 goleiros
        for (int i = 0; i < 3; i++) {
            Player p = gerarJogador(Position.GOLEIRO, forcaMedia);
            time.addJogador(p);
        }

        // 6 zagueiros
        for (int i = 0; i < 6; i++) {
            Player p = gerarJogador(Position.ZAGUEIRO, forcaMedia);
            time.addJogador(p);
        }

        // 3 laterais direitos
        for (int i = 0; i < 3; i++) {
            Player p = gerarJogador(Position.LATERAL_DIREITO, forcaMedia);
            time.addJogador(p);
        }

        // 3 laterais esquerdos
        for (int i = 0; i < 3; i++) {
            Player p = gerarJogador(Position.LATERAL_ESQUERDO, forcaMedia);
            time.addJogador(p);
        }

        // 4 volantes
        for (int i = 0; i < 4; i++) {
            Player p = gerarJogador(Position.VOLANTE, forcaMedia);
            time.addJogador(p);
        }

        // 4 meias
        for (int i = 0; i < 4; i++) {
            Player p = gerarJogador(Position.MEIA, forcaMedia);
            time.addJogador(p);
        }

        // 2 pontas direita
        for (int i = 0; i < 2; i++) {
            Player p = gerarJogador(Position.PONTA_DIREITA, forcaMedia);
            time.addJogador(p);
        }

        // 2 pontas esquerda
        for (int i = 0; i < 2; i++) {
            Player p = gerarJogador(Position.PONTA_ESQUERDA, forcaMedia);
            time.addJogador(p);
        }

        // 4 centroavantes
        for (int i = 0; i < 4; i++) {
            Player p = gerarJogador(Position.CENTROAVANTE, forcaMedia);
            time.addJogador(p);
        }

        // Escala automaticamente 4-4-2
        escalarAutomatico(time);

        return time;
    }

    /**
     * Gera um jogador com posição e força específicas.
     */
    public Player gerarJogador(Position posicao, int forcaMedia) {
        String nome = gerarNome();
        int idade = gerarIdade(posicao);

        Player jogador = new Player(nome, idade, posicao);

        // Gera força baseada na média com variação
        int variacao = random.nextInt(21) - 10; // -10 a +10
        int forca = Math.max(40, Math.min(95, forcaMedia + variacao));
        jogador.setForca(forca);

        // Define habilidades baseadas na posição
        definirHabilidades(jogador, posicao, forca);

        // Define características inatas (1-2)
        definirCaracteristicas(jogador, posicao);

        // Define contrato
        int salario = calcularSalario(forca);
        jogador.setSalario(salario);
        jogador.renovarContrato(52 + random.nextInt(52), salario); // 1-2 anos

        // Define lado preferido
        if (posicao == Position.LATERAL_DIREITO || posicao == Position.PONTA_DIREITA) {
            jogador.setPrefereDireita(true);
            jogador.setPrefereEsquerda(random.nextDouble() < 0.3);
        } else if (posicao == Position.LATERAL_ESQUERDO || posicao == Position.PONTA_ESQUERDA) {
            jogador.setPrefereEsquerda(true);
            jogador.setPrefereDireita(random.nextDouble() < 0.3);
        }

        // 5% de chance de ser estrela
        if (random.nextDouble() < 0.05 && forca > 80) {
            jogador.setEstrela(true);
        }

        return jogador;
    }

    /**
     * Define as habilidades do jogador baseado na posição.
     */
    private void definirHabilidades(Player jogador, Position posicao, int forca) {
        int variacao = 15;

        switch (posicao) {
            case GOLEIRO:
                jogador.setGoleiro(forca + random.nextInt(variacao) - 5);
                jogador.setAgilidade(forca - 20 + random.nextInt(variacao));
                jogador.setPasse(forca - 30 + random.nextInt(variacao));
                break;

            case ZAGUEIRO:
                jogador.setDesarme(forca + random.nextInt(variacao));
                jogador.setAgilidade(forca - 10 + random.nextInt(variacao));
                jogador.setTecnica(forca - 15 + random.nextInt(variacao));
                jogador.setPasse(forca - 10 + random.nextInt(variacao));
                break;

            case LATERAL_DIREITO:
            case LATERAL_ESQUERDO:
                jogador.setAgilidade(forca + random.nextInt(variacao));
                jogador.setDesarme(forca - 5 + random.nextInt(variacao));
                jogador.setPasse(forca + random.nextInt(variacao));
                jogador.setTecnica(forca - 5 + random.nextInt(variacao));
                break;

            case VOLANTE:
                jogador.setDesarme(forca + random.nextInt(variacao));
                jogador.setPasse(forca + random.nextInt(variacao));
                jogador.setArmacao(forca - 10 + random.nextInt(variacao));
                jogador.setTecnica(forca - 5 + random.nextInt(variacao));
                break;

            case MEIA:
            case MEIA_ATACANTE:
                jogador.setArmacao(forca + random.nextInt(variacao));
                jogador.setPasse(forca + random.nextInt(variacao));
                jogador.setTecnica(forca + random.nextInt(variacao));
                jogador.setFinalizacao(forca - 5 + random.nextInt(variacao));
                break;

            case PONTA_DIREITA:
            case PONTA_ESQUERDA:
                jogador.setAgilidade(forca + random.nextInt(variacao));
                jogador.setTecnica(forca + random.nextInt(variacao));
                jogador.setFinalizacao(forca - 5 + random.nextInt(variacao));
                jogador.setPasse(forca - 5 + random.nextInt(variacao));
                break;

            case CENTROAVANTE:
            case ATACANTE:
                jogador.setFinalizacao(forca + random.nextInt(variacao));
                jogador.setTecnica(forca - 5 + random.nextInt(variacao));
                jogador.setAgilidade(forca - 10 + random.nextInt(variacao));
                break;
        }
    }

    /**
     * Define características inatas.
     */
    private void definirCaracteristicas(Player jogador, Position posicao) {
        List<Caracteristica> possiveis = new ArrayList<>();

        switch (posicao) {
            case GOLEIRO:
                possiveis.add(Caracteristica.COLOCACAO);
                possiveis.add(Caracteristica.SAIDA_GOL);
                possiveis.add(Caracteristica.REFLEXO);
                possiveis.add(Caracteristica.DEFESA_PENALTY);
                break;

            case ZAGUEIRO:
                possiveis.add(Caracteristica.DESARME);
                possiveis.add(Caracteristica.MARCACAO);
                possiveis.add(Caracteristica.CABECEIO);
                possiveis.add(Caracteristica.VELOCIDADE);
                break;

            case LATERAL_DIREITO:
            case LATERAL_ESQUERDO:
                possiveis.add(Caracteristica.VELOCIDADE);
                possiveis.add(Caracteristica.CRUZAMENTO);
                possiveis.add(Caracteristica.MARCACAO);
                possiveis.add(Caracteristica.RESISTENCIA);
                break;

            case VOLANTE:
                possiveis.add(Caracteristica.DESARME);
                possiveis.add(Caracteristica.MARCACAO);
                possiveis.add(Caracteristica.PASSE);
                possiveis.add(Caracteristica.CABECEIO);
                break;

            case MEIA:
            case MEIA_ATACANTE:
                possiveis.add(Caracteristica.ARMACAO);
                possiveis.add(Caracteristica.PASSE);
                possiveis.add(Caracteristica.DRIBLE);
                possiveis.add(Caracteristica.FINALIZACAO);
                break;

            case PONTA_DIREITA:
            case PONTA_ESQUERDA:
                possiveis.add(Caracteristica.VELOCIDADE);
                possiveis.add(Caracteristica.DRIBLE);
                possiveis.add(Caracteristica.CRUZAMENTO);
                possiveis.add(Caracteristica.FINALIZACAO);
                break;

            case CENTROAVANTE:
            case ATACANTE:
                possiveis.add(Caracteristica.FINALIZACAO);
                possiveis.add(Caracteristica.CABECEIO);
                possiveis.add(Caracteristica.DRIBLE);
                possiveis.add(Caracteristica.VELOCIDADE);
                break;
        }

        // Adiciona 1-2 características
        Collections.shuffle(possiveis);
        jogador.addCaracteristica(possiveis.get(0));
        if (random.nextDouble() < 0.7 && possiveis.size() > 1) {
            jogador.addCaracteristica(possiveis.get(1));
        }
    }

    /**
     * Gera nome aleatório.
     */
    private String gerarNome() {
        String nome = nomesBrasileiros[random.nextInt(nomesBrasileiros.length)];
        String sobrenome = sobrenomes[random.nextInt(sobrenomes.length)];
        return nome + " " + sobrenome;
    }

    /**
     * Gera idade apropriada para a posição.
     */
    private int gerarIdade(Position posicao) {
        // Goleiros tendem a ser mais velhos
        int base = posicao == Position.GOLEIRO ? 25 : 23;
        return base + random.nextInt(10) - 3; // -3 a +6 anos
    }

    /**
     * Calcula salário baseado na força.
     */
    private int calcularSalario(int forca) {
        // Fórmula: salário aumenta exponencialmente com a força
        return (int) (10000 * Math.pow(1.05, forca - 50));
    }

    /**
     * Escala automaticamente o time em 4-4-2.
     */
    public void escalarAutomatico(Team time) {
        time.getTitulares().clear();
        time.getReservas().clear();

        List<Player> jogadores = time.getJogadores();

        // Ordena por força
        jogadores.sort((a, b) -> Integer.compare(b.getForca(), a.getForca()));

        // Escala por posição
        escalarPorPosicao(time, Position.GOLEIRO, 1);
        escalarPorPosicao(time, Position.ZAGUEIRO, 2);
        escalarPorPosicao(time, Position.LATERAL_DIREITO, 1);
        escalarPorPosicao(time, Position.LATERAL_ESQUERDO, 1);
        escalarPorPosicao(time, Position.VOLANTE, 2);
        escalarPorPosicao(time, Position.MEIA, 2);
        escalarPorPosicao(time, Position.CENTROAVANTE, 2);

        // Preenche reservas
        for (Player p : jogadores) {
            if (!time.getTitulares().contains(p) && time.getReservas().size() < 7) {
                time.escalarReserva(p);
            }
        }
    }

    private void escalarPorPosicao(Team time, Position posicao, int quantidade) {
        List<Player> candidatos = time.getJogadoresPorPosicao(posicao);
        candidatos.sort((a, b) -> Integer.compare(b.getForca(), a.getForca()));

        for (int i = 0; i < Math.min(quantidade, candidatos.size()); i++) {
            Player p = candidatos.get(i);
            if (p.podeJogar() && !time.getTitulares().contains(p)) {
                time.escalarTitular(p, posicao);
            }
        }
    }

    /**
     * Gera times brasileiros de exemplo.
     */
    public List<Team> gerarTimesBrasileiros() {
        List<Team> times = new ArrayList<>();

        // Times da Série A com seus escudos
        times.add(gerarTimeComEscudo("Flamengo", "FLA", 82, "Brasil", "RJ", "flarj.png"));
        times.add(gerarTimeComEscudo("Palmeiras", "PAL", 81, "Brasil", "SP", "palmeiras.png"));
        times.add(gerarTimeComEscudo("São Paulo", "SAO", 78, "Brasil", "SP", "saopaulo_bra.png"));
        times.add(gerarTimeComEscudo("Corinthians", "COR", 77, "Brasil", "SP", "corinthians_bra.png"));
        times.add(gerarTimeComEscudo("Santos", "SAN", 75, "Brasil", "SP", "santos.png"));
        times.add(gerarTimeComEscudo("Grêmio", "GRE", 76, "Brasil", "RS", "gremio.png"));
        times.add(gerarTimeComEscudo("Internacional", "INT", 76, "Brasil", "RS", "internacional_bra.png"));
        times.add(gerarTimeComEscudo("Atlético-MG", "CAM", 79, "Brasil", "MG", "atleticomg_bra.png"));
        times.add(gerarTimeComEscudo("Cruzeiro", "CRU", 74, "Brasil", "MG", "cruzeiro_bra.png"));
        times.add(gerarTimeComEscudo("Fluminense", "FLU", 75, "Brasil", "RJ", "flurj.png"));
        times.add(gerarTimeComEscudo("Vasco", "VAS", 72, "Brasil", "RJ", "vasco.png"));
        times.add(gerarTimeComEscudo("Botafogo", "BOT", 74, "Brasil", "RJ", "botafogorj_bra.png"));
        times.add(gerarTimeComEscudo("Athletico-PR", "CAP", 76, "Brasil", "PR", "atleticopr_bra.png"));
        times.add(gerarTimeComEscudo("Bahia", "BAH", 72, "Brasil", "BA", "bahia.png"));
        times.add(gerarTimeComEscudo("Fortaleza", "FOR", 73, "Brasil", "CE", "fortaleza.png"));
        times.add(gerarTimeComEscudo("Ceará", "CEA", 70, "Brasil", "CE", "ceara_bra.png"));
        times.add(gerarTimeComEscudo("Sport", "SPT", 68, "Brasil", "PE", "sport.png"));
        times.add(gerarTimeComEscudo("Goiás", "GOI", 68, "Brasil", "GO", "goias.png"));
        times.add(gerarTimeComEscudo("Coritiba", "CFC", 67, "Brasil", "PR", "coritiba_bra.png"));
        times.add(gerarTimeComEscudo("América-MG", "AME", 68, "Brasil", "MG", "americamg_bra.png"));

        return times;
    }

    /**
     * Gera um time com escudo definido.
     */
    private Team gerarTimeComEscudo(String nome, String sigla, int forcaMedia, String pais, String estado,
            String escudo) {
        Team time = gerarTime(nome, sigla, forcaMedia, pais, estado);
        time.setEscudoPath("teams/escudos/" + escudo);
        return time;
    }
}
