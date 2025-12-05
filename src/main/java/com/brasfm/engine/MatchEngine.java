package com.brasfm.engine;

import com.brasfm.model.*;
import com.brasfm.model.enums.*;
import com.brasfm.social.MoraleSystem;
import com.brasfm.audio.SoundSystem;
import java.util.*;

/**
 * Motor de Simulação v3.0 - Baseado em Atributos Individuais
 * 
 * Não usa "Força" agregada. Cada disputa usa os atributos relevantes:
 * - Duelo: Força, Equilíbrio, Determinação
 * - Passe: Passe, Visão, Decisões
 * - Finalização: Finalização, Compostura, Técnica
 * - Defesa: Desarme, Antecipação, Posicionamento
 * - Goleiro: Reflexos, Um contra Um, Posicionamento
 */
public class MatchEngine {
    private Random random;
    private XGCalculator xgCalculator;
    private FatigueSystem fatigueSystem;
    private SoundSystem soundSystem;

    // Estado da partida
    private Match match;
    private Map<Player, Double> energiaJogadores;
    private Map<Player, Double> notasJogadores;
    private Map<Player, PlayerAttributes> atributosEfetivos;
    private MoraleSystem moraleSystem;
    private Team timeHumano; // Para saber quando tocar som de gol vs gol adversário

    // Estatísticas avançadas
    private double xgMandante = 0;
    private double xgVisitante = 0;
    private int posseMandanteTotal = 0;
    private int amostrasPosse = 0;

    public MatchEngine() {
        this.random = new Random();
        this.xgCalculator = new XGCalculator();
        this.fatigueSystem = new FatigueSystem();
        this.energiaJogadores = new HashMap<>();
        this.notasJogadores = new HashMap<>();
        this.atributosEfetivos = new HashMap<>();
    }

    public void setMoraleSystem(MoraleSystem ms) {
        this.moraleSystem = ms;
    }

    public void setSoundSystem(SoundSystem ss) {
        this.soundSystem = ss;
    }

    public void setTimeHumano(Team time) {
        this.timeHumano = time;
    }

    /**
     * Simula uma partida completa.
     */
    public Match simular(Team mandante, Team visitante, boolean jogoImportante) {
        this.match = new Match(mandante, visitante);
        this.xgMandante = 0;
        this.xgVisitante = 0;
        this.posseMandanteTotal = 0;
        this.amostrasPosse = 0;

        // Inicializa estado dos jogadores
        inicializarJogadores(mandante, jogoImportante);
        inicializarJogadores(visitante, jogoImportante);

        // Calcula público
        calcularPublico(mandante, visitante, jogoImportante);

        // Inicia partida
        match.iniciar();

        // Simula primeiro tempo
        simularTempo(1, 45, mandante, visitante, jogoImportante);

        // Intervalo
        if (soundSystem != null) {
            soundSystem.tocarIntervalo();
        }
        processarIntervalo(mandante);
        processarIntervalo(visitante);

        // Simula segundo tempo
        simularTempo(46, 90, mandante, visitante, jogoImportante);

        // Acréscimos se necessário
        int acrescimos = calcularAcrescimos();
        if (acrescimos > 0) {
            simularTempo(91, 90 + acrescimos, mandante, visitante, jogoImportante);
        }

        // Finaliza
        match.finalizar();

        // Som de fim de jogo
        if (soundSystem != null) {
            soundSystem.tocarFimJogo();
        }

        // Calcula notas finais
        calcularNotasFinais();

        // Calcula posse média
        if (amostrasPosse > 0) {
            match.setPosseMandante(posseMandanteTotal / amostrasPosse);
        }

        return match;
    }

    private void inicializarJogadores(Team team, boolean jogoImportante) {
        for (Player p : team.getTitulares()) {
            energiaJogadores.put(p, (double) p.getEnergia());
            notasJogadores.put(p, 6.0);

            // Cria atributos baseados nos valores REAIS, não na força agregada
            PlayerAttributes attrs = criarAtributosReais(p);

            // Aplica modificador de moral
            if (moraleSystem != null) {
                double modMoral = moraleSystem.getModificadorPerformance(p, jogoImportante);
                if (modMoral != 0) {
                    attrs.setDecisoes((int) (attrs.getDecisoes() * (1 + modMoral)));
                    attrs.setCompostura((int) (attrs.getCompostura() * (1 + modMoral)));
                    attrs.setConcentracao((int) (attrs.getConcentracao() * (1 + modMoral)));
                }
            }

            atributosEfetivos.put(p, attrs);
        }
        for (Player p : team.getReservas()) {
            energiaJogadores.put(p, (double) p.getEnergia());
        }
    }

    /**
     * Cria atributos baseados nos valores REAIS do jogador.
     */
    private PlayerAttributes criarAtributosReais(Player p) {
        PlayerAttributes attrs = new PlayerAttributes(50);

        // Técnicos - usa valores reais do jogador
        attrs.setFinalizacao(p.getFinalizacao());
        attrs.setPasse(p.getPasse());
        attrs.setTecnica(p.getTecnica());
        attrs.setDrible(p.getAgilidade());
        attrs.setDesarme(p.getDesarme());

        // Físicos - estima baseado em posição e força
        attrs.setVelocidade(calcularAtributoBase(p, "velocidade"));
        attrs.setResistencia(calcularAtributoBase(p, "resistencia"));
        attrs.setForca(calcularAtributoBase(p, "forca"));

        // Mentais - usa personalidade se disponível
        PlayerPersonality pers = p.getPersonality();
        if (pers != null) {
            attrs.setDecisoes(50 + pers.getConsistencia() * 2);
            attrs.setCompostura(40 + pers.getJogosImportantes() * 3);
            attrs.setConcentracao(50 + pers.getProfissionalismo() * 2);
            attrs.setAntecipacao(calcularAtributoBase(p, "antecipacao"));
            attrs.setVisao(50 + pers.getConsistencia() + p.getArmacao() / 5);
            attrs.setSemBola(50 + pers.getAmbicao());
        } else {
            attrs.setDecisoes(55 + p.getForca() / 10);
            attrs.setCompostura(55);
            attrs.setConcentracao(55);
            attrs.setAntecipacao(55);
            attrs.setVisao(50 + p.getArmacao() / 3);
            attrs.setSemBola(55);
        }

        // Defensivos
        attrs.setMarcacao(p.getDesarme());
        attrs.setPosicionamento(calcularAtributoBase(p, "posicionamento"));

        // Goleiro
        if (p.isGoleiro()) {
            attrs.setGoleiro(p.getGoleiro());
            attrs.setReflexos(p.getGoleiro());
            attrs.setUmContraUm(p.getGoleiro());
        }

        return attrs;
    }

    private int calcularAtributoBase(Player p, String tipo) {
        int base = 50;
        Position pos = p.getPosicaoOriginal();

        switch (tipo) {
            case "velocidade":
                if (pos == Position.PONTA_DIREITA || pos == Position.PONTA_ESQUERDA)
                    base = 70;
                else if (pos == Position.LATERAL_DIREITO || pos == Position.LATERAL_ESQUERDO)
                    base = 65;
                else if (pos == Position.ZAGUEIRO)
                    base = 55;
                break;
            case "forca":
                if (pos == Position.ZAGUEIRO || pos == Position.CENTROAVANTE)
                    base = 70;
                else if (pos == Position.VOLANTE)
                    base = 65;
                break;
            case "resistencia":
                if (pos == Position.VOLANTE || pos == Position.MEIA)
                    base = 70;
                break;
            case "posicionamento":
                if (pos.isDefensiva())
                    base = 70;
                else if (pos == Position.CENTROAVANTE)
                    base = 65;
                break;
            case "antecipacao":
                if (pos.isDefensiva())
                    base = 65;
                break;
        }

        base += (p.getForca() - 60) / 4;
        return Math.max(20, Math.min(95, base + random.nextInt(6) - 3));
    }

    private void simularTempo(int inicio, int fim, Team mandante, Team visitante, boolean jogoImportante) {
        for (int minuto = inicio; minuto <= fim; minuto++) {
            match.setMinutoAtual(minuto);

            // Atualiza fadiga
            atualizarFadiga(mandante);
            atualizarFadiga(visitante);

            // Simula ações do minuto usando ATRIBUTOS INDIVIDUAIS
            simularMinuto(mandante, visitante, minuto, jogoImportante);
        }
    }

    private void simularMinuto(Team mandante, Team visitante, int minuto, boolean jogoImportante) {
        // Usa ATRIBUTOS de passe e visão para determinar posse (não força!)
        int qualidadeMeioM = calcularQualidadeMeioCampo(mandante);
        int qualidadeMeioV = calcularQualidadeMeioCampo(visitante);

        int diferenca = qualidadeMeioM - qualidadeMeioV;
        int posseMandante = 50 + (int) (diferenca * 0.3);
        posseMandante = Math.max(30, Math.min(70, posseMandante));

        posseMandanteTotal += posseMandante;
        amostrasPosse++;

        // Chance de ação significativa
        double chanceAcao = 0.10 + random.nextDouble() * 0.10;

        if (random.nextDouble() < chanceAcao) {
            boolean mandanteAtaca = random.nextInt(100) < posseMandante;

            if (mandanteAtaca) {
                processarAtaque(mandante, visitante, minuto, jogoImportante);
            } else {
                processarAtaque(visitante, mandante, minuto, jogoImportante);
            }
        }

        // Processar faltas e cartões
        processarFaltas(mandante, visitante, minuto);
    }

    /**
     * Calcula qualidade do meio usando ATRIBUTOS INDIVIDUAIS.
     */
    private int calcularQualidadeMeioCampo(Team team) {
        int soma = 0;
        int count = 0;

        for (Player p : team.getTitulares()) {
            if (p.isGoleiro())
                continue;

            PlayerAttributes attrs = getAtributosComFadiga(p);

            if (p.getPosicao() == Position.VOLANTE || p.getPosicao() == Position.MEIA ||
                    p.getPosicao() == Position.MEIA_ATACANTE) {
                // Meias: Passe + Visão + Decisões
                int contribuicao = (attrs.getPasse() + attrs.getVisao() + attrs.getDecisoes()) / 3;
                soma += contribuicao * 2;
                count += 2;
            } else {
                // Outros: Passe + Técnica
                soma += (attrs.getPasse() + attrs.getTecnica()) / 2;
                count++;
            }
        }

        return count > 0 ? soma / count : 50;
    }

    private void processarAtaque(Team atacante, Team defensor, int minuto, boolean jogoImportante) {
        // Fase 1: Criação - usa Visão + Passe vs Antecipação + Marcação
        Player criador = selecionarCriador(atacante);
        Player marcador = selecionarMarcador(defensor);

        if (criador == null)
            return;

        PlayerAttributes attrsCriador = getAtributosComFadiga(criador);
        PlayerAttributes attrsMarcador = marcador != null ? getAtributosComFadiga(marcador) : null;

        int ataque = (attrsCriador.getVisao() + attrsCriador.getPasse() + attrsCriador.getDecisoes()) / 3;
        int defesa = attrsMarcador != null ? (attrsMarcador.getAntecipacao() + attrsMarcador.getMarcacao()) / 2 : 40;

        boolean passeBemSucedido = resolverDuelo(ataque, defesa, 0.55);
        if (!passeBemSucedido)
            return;

        // Fase 2: Finalização
        Player finalizador = selecionarFinalizador(atacante);
        Player goleiro = defensor.getGoleiro();
        Player defensorProximo = selecionarMarcador(defensor);

        if (finalizador == null || goleiro == null)
            return;

        PlayerAttributes attrsFinalizador = getAtributosComFadiga(finalizador);
        PlayerAttributes attrsDefensor = defensorProximo != null ? getAtributosComFadiga(defensorProximo) : null;
        PlayerAttributes attrsGoleiro = getAtributosComFadiga(goleiro);

        // Tipo de chance
        double distanciaGol = 8 + random.nextDouble() * 25;
        double angulo = 20 + random.nextDouble() * 50;
        boolean dentroArea = distanciaGol <= 16.5;
        boolean cabecada = random.nextDouble() < 0.15;
        boolean grandeChance = random.nextDouble() < 0.10;

        // Pressão defensiva baseada em ATRIBUTOS
        double pressao = 0.5;
        if (attrsDefensor != null) {
            pressao = (attrsDefensor.getPosicionamento() + attrsDefensor.getMarcacao()) / 200.0;
        }

        // xG base
        double xgBase = xgCalculator.calcularXGBase(distanciaGol, angulo, dentroArea, cabecada);

        // Modificadores baseados em ATRIBUTOS ESPECÍFICOS (não força!)
        double modFinalizacao = attrsFinalizador.getFinalizacao() / 70.0;
        double modCompostura = jogoImportante ? attrsFinalizador.getCompostura() / 80.0 : 1.0;
        double modDecisao = attrsFinalizador.getDecisoes() / 75.0;
        double modTecnica = attrsFinalizador.getTecnica() / 75.0;

        // Goleiro: Reflexos + Posicionamento + Um contra Um
        double modGoleiro = (attrsGoleiro.getReflexos() + attrsGoleiro.getPosicionamento() +
                attrsGoleiro.getUmContraUm()) / 240.0;

        double xg = xgBase * modFinalizacao * modCompostura * modDecisao *
                modTecnica * (1 - pressao * 0.3) * (2 - modGoleiro);

        xg = Math.max(0.01, Math.min(0.95, xg));

        // Registra xG
        if (atacante == match.getMandante()) {
            xgMandante += xg;
        } else {
            xgVisitante += xg;
        }

        match.registrarChute(atacante, dentroArea);

        // Resolve chute
        if (xgCalculator.resolverChute(xg, random)) {
            // GOL!
            Player assistente = criador != finalizador ? criador : selecionarAssistente(atacante, finalizador);
            match.registrarGol(atacante, finalizador, assistente);

            // Toca som de gol
            if (soundSystem != null) {
                if (atacante == timeHumano || (timeHumano == null && atacante == match.getMandante())) {
                    soundSystem.tocarGol();
                } else {
                    soundSystem.tocarGolAdversario();
                }
            }

            notasJogadores.merge(finalizador, 1.5, Double::sum);
            if (assistente != null) {
                notasJogadores.merge(assistente, 0.8, Double::sum);
            }
            notasJogadores.merge(goleiro, -0.5, Double::sum);

            gastarEnergia(finalizador, FatigueSystem.TipoAcao.SPRINT);

        } else {
            // Defesa ou fora
            if (random.nextDouble() < attrsGoleiro.getReflexos() / 250.0) {
                match.addEvento(new MatchEvent(minuto, MatchEvent.TipoEvento.DEFESA_DIFICIL, defensor, goleiro));
                notasJogadores.merge(goleiro, 0.3, Double::sum);
            }
        }

        gastarEnergia(finalizador, FatigueSystem.TipoAcao.CHUTE);
    }

    /**
     * Resolve duelo usando atributos.
     */
    private boolean resolverDuelo(int ataque, int defesa, double baseChance) {
        int diferenca = ataque - defesa;
        double chance = baseChance + (diferenca / 100.0);
        chance = Math.max(0.15, Math.min(0.85, chance));
        return random.nextDouble() < chance;
    }

    private Player selecionarCriador(Team team) {
        List<Player> candidatos = new ArrayList<>();
        for (Player p : team.getTitulares()) {
            if (!p.isGoleiro() && !p.getPosicao().isDefensiva()) {
                candidatos.add(p);
            }
        }
        if (candidatos.isEmpty()) {
            candidatos.addAll(team.getTitulares());
            candidatos.removeIf(Player::isGoleiro);
        }
        if (candidatos.isEmpty())
            return null;

        // Pondera por Visão + Passe (ATRIBUTOS!)
        int total = 0;
        for (Player p : candidatos) {
            PlayerAttributes attrs = atributosEfetivos.get(p);
            total += attrs.getVisao() + attrs.getPasse();
        }

        int sorteio = random.nextInt(Math.max(1, total));
        int acumulado = 0;

        for (Player p : candidatos) {
            PlayerAttributes attrs = atributosEfetivos.get(p);
            acumulado += attrs.getVisao() + attrs.getPasse();
            if (sorteio < acumulado)
                return p;
        }

        return candidatos.get(0);
    }

    private Player selecionarFinalizador(Team team) {
        List<Player> candidatos = new ArrayList<>();
        for (Player p : team.getTitulares()) {
            if (!p.isGoleiro())
                candidatos.add(p);
        }
        if (candidatos.isEmpty())
            return null;

        // Pondera por Finalização + Sem Bola (ATRIBUTOS!)
        int total = 0;
        for (Player p : candidatos) {
            PlayerAttributes attrs = atributosEfetivos.get(p);
            int peso = attrs.getFinalizacao() + attrs.getSemBola();
            if (p.getPosicao().isOfensiva())
                peso += 40;
            total += peso;
        }

        int sorteio = random.nextInt(Math.max(1, total));
        int acumulado = 0;

        for (Player p : candidatos) {
            PlayerAttributes attrs = atributosEfetivos.get(p);
            int peso = attrs.getFinalizacao() + attrs.getSemBola();
            if (p.getPosicao().isOfensiva())
                peso += 40;
            acumulado += peso;
            if (sorteio < acumulado)
                return p;
        }

        return candidatos.get(0);
    }

    private Player selecionarMarcador(Team team) {
        List<Player> defensores = new ArrayList<>();
        for (Player p : team.getTitulares()) {
            if (p.getPosicao().isDefensiva() || p.getPosicao() == Position.VOLANTE) {
                defensores.add(p);
            }
        }
        if (defensores.isEmpty())
            return null;
        return defensores.get(random.nextInt(defensores.size()));
    }

    private Player selecionarAssistente(Team team, Player finalizador) {
        if (random.nextDouble() > 0.65)
            return null;

        List<Player> candidatos = new ArrayList<>();
        for (Player p : team.getTitulares()) {
            if (p != finalizador && !p.isGoleiro()) {
                candidatos.add(p);
            }
        }

        if (candidatos.isEmpty())
            return null;
        return candidatos.get(random.nextInt(candidatos.size()));
    }

    private void processarFaltas(Team mandante, Team visitante, int minuto) {
        double chanceFaltaM = mandante.getTatica().getTipoMarcacao().getChanceFalta() * 0.06;
        double chanceFaltaV = visitante.getTatica().getTipoMarcacao().getChanceFalta() * 0.06;

        if (random.nextDouble() < chanceFaltaM) {
            match.registrarFalta(mandante);
            processarCartao(mandante, minuto);
        }

        if (random.nextDouble() < chanceFaltaV) {
            match.registrarFalta(visitante);
            processarCartao(visitante, minuto);
        }
    }

    private void processarCartao(Team time, int minuto) {
        if (random.nextDouble() < 0.12) {
            List<Player> jogadores = time.getTitulares();
            if (jogadores.isEmpty())
                return;

            // Jogadores com baixo temperamento levam mais cartões
            List<Player> candidatos = new ArrayList<>();
            for (Player p : jogadores) {
                int peso = 10;
                PlayerPersonality pers = p.getPersonality();
                if (pers != null) {
                    peso += (20 - pers.getTemperamento());
                }
                for (int i = 0; i < peso; i++)
                    candidatos.add(p);
            }

            Player faltoso = candidatos.get(random.nextInt(candidatos.size()));

            if (random.nextDouble() < 0.92) {
                match.registrarCartaoAmarelo(time, faltoso);
                notasJogadores.merge(faltoso, -0.5, Double::sum);
            } else {
                match.registrarCartaoVermelho(time, faltoso);
                notasJogadores.put(faltoso, 2.0);
                // Toca som de expulsão
                if (soundSystem != null) {
                    soundSystem.tocarExpulsao();
                }
            }
        }
    }

    private void atualizarFadiga(Team team) {
        for (Player p : team.getTitulares()) {
            PlayerAttributes attrs = atributosEfetivos.get(p);
            if (attrs == null)
                continue;

            double gasto = fatigueSystem.calcularGastoMinuto(
                    attrs,
                    OutOfPossessionSettings.IntensidadePressao.NORMAL,
                    InPossessionSettings.Ritmo.NORMAL,
                    random.nextBoolean());

            double energiaAtual = energiaJogadores.getOrDefault(p, 100.0);
            energiaJogadores.put(p, Math.max(0, energiaAtual - gasto));
        }
    }

    private void gastarEnergia(Player p, FatigueSystem.TipoAcao acao) {
        PlayerAttributes attrs = atributosEfetivos.get(p);
        if (attrs == null)
            return;

        double gasto = fatigueSystem.calcularGastoAcao(attrs, acao);
        double energiaAtual = energiaJogadores.getOrDefault(p, 100.0);
        energiaJogadores.put(p, Math.max(0, energiaAtual - gasto));
    }

    private void processarIntervalo(Team team) {
        for (Player p : team.getTitulares()) {
            PlayerAttributes attrs = atributosEfetivos.get(p);
            if (attrs == null)
                continue;

            double recuperacao = fatigueSystem.calcularRecuperacao(attrs, FatigueSystem.TipoPausa.INTERVALO);
            double energiaAtual = energiaJogadores.getOrDefault(p, 50.0);
            energiaJogadores.put(p, Math.min(100, energiaAtual + recuperacao));
        }
    }

    private PlayerAttributes getAtributosComFadiga(Player p) {
        PlayerAttributes base = atributosEfetivos.get(p);
        if (base == null)
            return new PlayerAttributes(50);

        double energia = energiaJogadores.getOrDefault(p, 100.0);
        double fator = fatigueSystem.energiaParaFatorFadiga(energia);

        return base.comFadiga(fator);
    }

    private void calcularPublico(Team mandante, Team visitante, boolean importante) {
        int capacidade = mandante.getEstadio() != null ? mandante.getEstadio().getCapacidade() : 30000;
        double ocupacao = 0.5 + (importante ? 0.3 : 0) + random.nextDouble() * 0.2;
        match.setPublico((int) (capacidade * ocupacao));
    }

    private int calcularAcrescimos() {
        int base = 2;
        base += match.getEventos().stream()
                .filter(e -> e.getTipo() == MatchEvent.TipoEvento.GOL ||
                        e.getTipo() == MatchEvent.TipoEvento.CARTAO_VERMELHO ||
                        e.getTipo() == MatchEvent.TipoEvento.SUBSTITUICAO)
                .count() / 2;
        return Math.min(7, base + random.nextInt(2));
    }

    private void calcularNotasFinais() {
        for (Map.Entry<Player, Double> entry : notasJogadores.entrySet()) {
            double nota = Math.max(1, Math.min(10, entry.getValue()));
            entry.getKey().addJogo();
            entry.getKey().atualizarNota(nota);
        }
    }

    // Getters para estatísticas
    public double getXgMandante() {
        return xgMandante;
    }

    public double getXgVisitante() {
        return xgVisitante;
    }

    public Map<Player, Double> getNotasJogadores() {
        return notasJogadores;
    }

    public Map<Player, Double> getEnergiaJogadores() {
        return energiaJogadores;
    }
}
