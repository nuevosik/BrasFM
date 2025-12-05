package com.brasfm.engine;

import com.brasfm.model.Match;
import com.brasfm.model.Team;
import com.brasfm.model.Player;
import javax.swing.Timer;
import java.util.function.Consumer;
import java.util.Random;
import java.util.List;

/**
 * Engine de partida ao vivo com callbacks para UI.
 * Separa a lógica do jogo da interface gráfica.
 * Inclui sistema de Momentum, Bolas Paradas, Clima, Lesões e Táticas.
 */
public class LiveMatchEngine {

    private Match partida;
    private Timer timer;
    private int minuto = 0;
    private Random random = new Random();

    // Estado do jogo
    private int golsCasa = 0;
    private int golsFora = 0;
    private int chutesCasa = 0;
    private int chutesFora = 0;
    private int posseCasa = 50;
    private int posseFora = 50;

    // Bolas paradas
    private int escanteiosCasa = 0;
    private int escanteiosFora = 0;
    private int faltasCasa = 0;
    private int faltasFora = 0;
    private int penaltisCasa = 0;
    private int penaltisFora = 0;
    private int impedimentosCasa = 0;
    private int impedimentosFora = 0;

    // Sistema de Momentum (Pressão)
    private int momentum = 0;
    private int strikeCasa = 0;
    private int strikeFora = 0;
    private int ultimosMomentos = 0;

    // Clima
    public enum Clima {
        NORMAL, CHUVA, CALOR
    }

    private Clima clima = Clima.NORMAL;

    // Lesões durante o jogo
    private int lesoesCasa = 0;
    private int lesoesFora = 0;

    // Constantes de momentum
    private static final int MOMENTUM_ALTO = 40;
    private static final int MOMENTUM_MUITO_ALTO = 70;
    private static final int MOMENTUM_DECAY = 3;
    private static final int MOMENTUM_GAIN_GOL = 30;
    private static final int MOMENTUM_GAIN_CHUTE = 10;
    private static final int MOMENTUM_GAIN_ATAQUE = 5;

    // Constantes de bolas paradas
    private static final double CHANCE_ESCANTEIO_GOL = 0.08; // 8% de gol por escanteio
    private static final double CHANCE_FALTA_GOL = 0.05; // 5% de gol por falta perigosa
    private static final double CHANCE_PENALTI_GOL = 0.75; // 75% de conversão de pênalti
    private static final double CHANCE_PENALTI = 0.003; // 0.3% por minuto de pênalti

    // Constantes táticas e bônus
    private static final int BONUS_MANDANTE = 3; // Bônus casa nos atributos mentais
    private static final double CHANCE_LESAO = 0.0005; // 0.05% por minuto
    private static final double CHANCE_IMPEDIMENTO = 0.015; // 1.5% por ataque

    // Times
    private Team mandante;
    private Team visitante;
    private double chanceBaseGol;
    private double bonusTaticaCasa = 1.0; // Multiplicador tático
    private double bonusTaticaFora = 1.0;

    // Callbacks para atualizar a UI
    private Consumer<String> onNarracao;
    private Consumer<Integer> onMinutoChange;
    private Consumer<MatchEvent> onEvento;
    private Consumer<MomentumInfo> onMomentumChange;
    private Runnable onIntervalo;
    private Runnable onFimJogo;

    // Enum para tipos de evento
    public enum TipoEvento {
        GOL_CASA, GOL_FORA, CHUTE_CASA, CHUTE_FORA, DEFESA, FALTA,
        ATAQUE_CASA, ATAQUE_FORA, MEIO, MOMENTUM_CASA, MOMENTUM_FORA,
        ESCANTEIO, PENALTI, IMPEDIMENTO, LESAO, GOL_PENALTI, GOL_FALTA, GOL_ESCANTEIO
    }

    // Classe para eventos de partida
    public static class MatchEvent {
        public TipoEvento tipo;
        public String descricao;
        public boolean casaAtaca;
        public int intensidade;

        public MatchEvent(TipoEvento tipo, String descricao, boolean casaAtaca, int intensidade) {
            this.tipo = tipo;
            this.descricao = descricao;
            this.casaAtaca = casaAtaca;
            this.intensidade = intensidade;
        }
    }

    // Classe para info de momentum
    public static class MomentumInfo {
        public int valor; // -100 a +100
        public boolean casaPressionando;
        public String descricao;

        public MomentumInfo(int valor, boolean casaPressionando, String descricao) {
            this.valor = valor;
            this.casaPressionando = casaPressionando;
            this.descricao = descricao;
        }
    }

    public LiveMatchEngine(Match partida) {
        this.partida = partida;
        this.mandante = partida.getMandante();
        this.visitante = partida.getVisitante();

        // Calcula chance base (será modificada pelo momentum)
        double forcaCasa = mandante.getForcaTime();
        double forcaFora = visitante.getForcaTime();
        double totalForca = forcaCasa + forcaFora;
        this.chanceBaseGol = 0.025; // 2.5% base por minuto
    }

    /**
     * Calcula a chance de gol usando duelos individuais por setor.
     * Simula mini-duelos entre criação (atacante) vs defesa (defensor).
     */
    private double getChanceGol(boolean casaAtaca) {
        Team atacante = casaAtaca ? mandante : visitante;
        Team defensor = casaAtaca ? visitante : mandante;

        // 1. Qualidade do Meio-Campo (Criação de jogadas)
        int criacao = calcularQualidadeSetor(atacante, "CRIACAO");

        // 2. Qualidade Defensiva (Contenção)
        int defesa = calcularQualidadeSetor(defensor, "DEFESA");

        // 3. Qualidade de Finalização
        int finalizacao = calcularQualidadeSetor(atacante, "FINALIZACAO");

        // 4. Qualidade do Goleiro
        int goleiro = calcularQualidadeSetor(defensor, "GOLEIRO");

        // Chance de criar jogada: criação vs defesa
        double chanceCriar = (double) criacao / (criacao + defesa);

        // Chance de finalizar: finalização vs goleiro
        double chanceFinalizar = (double) finalizacao / (finalizacao + goleiro);

        // Bônus de momentum (até +20% se time está dominando)
        double bonusMomentum = 0;
        if (casaAtaca && momentum > 0) {
            bonusMomentum = (momentum / 100.0) * 0.2;
        } else if (!casaAtaca && momentum < 0) {
            bonusMomentum = (-momentum / 100.0) * 0.2;
        } else if (casaAtaca && momentum < 0) {
            bonusMomentum = (momentum / 100.0) * 0.15; // Penalidade
        } else if (!casaAtaca && momentum > 0) {
            bonusMomentum = (-momentum / 100.0) * 0.15;
        }

        // Bônus de mandante (fator casa) - atributos mentais
        double bonusCasa = casaAtaca ? 1.03 : 1.0; // +3% para mandante

        // Chance final: probabilidade de criar * probabilidade de finalizar * base
        // Base de 5% por minuto, modificada pelos duelos
        double chanceBase = 0.05;
        double chanceFinal = chanceBase * chanceCriar * chanceFinalizar * (1 + bonusMomentum) * bonusCasa;

        // Garante um limite razoável (0.5% a 5%)
        return Math.max(0.005, Math.min(0.05, chanceFinal));
    }

    /**
     * Calcula a qualidade de um setor baseada nos atributos dos jogadores
     * titulares.
     */
    private int calcularQualidadeSetor(Team time, String setor) {
        int soma = 0;
        List<Player> titulares = time.getTitulares();

        if (titulares == null || titulares.isEmpty()) {
            return 50; // Valor padrão se não conseguir acessar
        }

        for (Player p : titulares) {
            if (p == null || p.getPosicaoOriginal() == null)
                continue;

            switch (setor) {
                case "CRIACAO":
                    // Meio-campistas contribuem com passe e armação
                    if (p.getPosicaoOriginal().name().contains("MEIA") ||
                            p.getPosicaoOriginal().name().contains("VOLANTE")) {
                        soma += p.getPasse() + p.getArmacao();
                    }
                    // Pontas também criam jogadas
                    if (p.getPosicaoOriginal().name().contains("PONTA")) {
                        soma += (p.getPasse() + p.getArmacao()) / 2;
                    }
                    break;

                case "DEFESA":
                    // Defensores contribuem com desarme e força
                    if (p.getPosicaoOriginal().isDefensiva()) {
                        soma += p.getDesarme() + p.getForca();
                    }
                    // Volante também ajuda na defesa
                    if (p.getPosicaoOriginal().name().contains("VOLANTE")) {
                        soma += p.getDesarme();
                    }
                    break;

                case "FINALIZACAO":
                    // Atacantes finalizam
                    if (p.getPosicaoOriginal().isOfensiva()) {
                        soma += p.getFinalizacaoEfetiva();
                    }
                    // Meias atacantes também
                    if (p.getPosicaoOriginal().name().contains("MEIA_ATACANTE")) {
                        soma += p.getFinalizacaoEfetiva() / 2;
                    }
                    break;

                case "GOLEIRO":
                    if (p.getPosicaoOriginal().isGoleiro()) {
                        soma += p.getGoleiro() * 3; // Goleiro tem peso triplo
                    }
                    break;
            }
        }

        return Math.max(1, soma); // Nunca zero
    }

    /**
     * Define os callbacks para comunicação com a UI.
     */
    public void setCallbacks(Consumer<String> onNarracao,
            Consumer<Integer> onMinutoChange,
            Consumer<MatchEvent> onEvento,
            Runnable onIntervalo,
            Runnable onFimJogo) {
        this.onNarracao = onNarracao;
        this.onMinutoChange = onMinutoChange;
        this.onEvento = onEvento;
        this.onIntervalo = onIntervalo;
        this.onFimJogo = onFimJogo;
    }

    /**
     * Inicia a simulação da partida.
     */
    public void iniciar() {
        narrar("🏟️ Estádio lotado! O juiz apita e a bola rola!");
        notificarEvento(new MatchEvent(TipoEvento.MEIO, "Início", true, 0));

        timer = new Timer(150, e -> processarMinuto());
        timer.start();
    }

    /**
     * Processa cada minuto da partida.
     */
    private void processarMinuto() {
        minuto++;

        if (onMinutoChange != null) {
            onMinutoChange.accept(minuto);
        }

        // Intervalo
        if (minuto == 45) {
            narrar("\n⏱️ 45' - FIM DO PRIMEIRO TEMPO!");
            narrar("   " + mandante.getSigla() + " " + golsCasa + " x " + golsFora + " " + visitante.getSigla() + "\n");
            if (onIntervalo != null) {
                onIntervalo.run();
            }
        }

        if (minuto == 46) {
            narrar("🔔 Começa o segundo tempo!\n");
        }

        // Simulação de eventos
        processarEventos();

        // Fim de jogo
        if (minuto >= 90) {
            finalizarPartida();
        }
    }

    /**
     * Processa eventos aleatórios do jogo com sistema de momentum.
     */
    private void processarEventos() {
        double rand = random.nextDouble();

        // Decaimento natural do momentum
        if (momentum > 0) {
            momentum = Math.max(0, momentum - MOMENTUM_DECAY);
        } else if (momentum < 0) {
            momentum = Math.min(0, momentum + MOMENTUM_DECAY);
        }

        // Determina quem está atacando (influenciado pelo momentum)
        double chanceAtaqueCasa = 0.5 + (momentum / 200.0); // 25% a 75%
        boolean casaAtaca = random.nextDouble() < chanceAtaqueCasa;

        // Atualiza posse baseada no momentum
        if (casaAtaca) {
            posseCasa = Math.min(75, posseCasa + 2);
            posseFora = 100 - posseCasa;
            strikeCasa++;
            strikeFora = 0;
        } else {
            posseFora = Math.min(75, posseFora + 2);
            posseCasa = 100 - posseFora;
            strikeFora++;
            strikeCasa = 0;
        }

        // Calcula chances dinâmicas baseadas no momentum
        double chanceCasaGol = getChanceGol(true);
        double chanceForaGol = getChanceGol(false);

        // Verifica se há momento de pressão
        if (strikeCasa >= 3 && momentum < MOMENTUM_MUITO_ALTO) {
            aumentarMomentum(true, MOMENTUM_GAIN_ATAQUE);
            if (momentum >= MOMENTUM_ALTO && ultimosMomentos == 0) {
                narrar("🔥 " + minuto + "' - " + mandante.getSigla() + " está PRESSIONANDO!");
                notificarEvento(new MatchEvent(TipoEvento.MOMENTUM_CASA, "Pressão!", true, 60));
                ultimosMomentos = 5;
            }
        } else if (strikeFora >= 3 && momentum > -MOMENTUM_MUITO_ALTO) {
            aumentarMomentum(false, MOMENTUM_GAIN_ATAQUE);
            if (momentum <= -MOMENTUM_ALTO && ultimosMomentos == 0) {
                narrar("🔥 " + minuto + "' - " + visitante.getSigla() + " está PRESSIONANDO!");
                notificarEvento(new MatchEvent(TipoEvento.MOMENTUM_FORA, "Pressão!", false, 60));
                ultimosMomentos = 5;
            }
        }

        if (ultimosMomentos > 0)
            ultimosMomentos--;

        // Gol do mandante
        if (rand < chanceCasaGol) {
            golsCasa++;
            chutesCasa++;
            aumentarMomentum(true, MOMENTUM_GAIN_GOL);
            narrar("\n" + minuto + "' - ⚽ GOOOOL DO " + mandante.getNome().toUpperCase() + "!!!");
            narrar("   Placar: " + golsCasa + " x " + golsFora);
            if (momentum > MOMENTUM_ALTO) {
                narrar("   💪 " + mandante.getSigla() + " está dominando a partida!\n");
            } else {
                narrar("");
            }
            notificarEvento(new MatchEvent(TipoEvento.GOL_CASA,
                    "Gol de " + mandante.getNome(), true, 100));
        }
        // Gol do visitante
        else if (rand < chanceCasaGol + chanceForaGol) {
            golsFora++;
            chutesFora++;
            aumentarMomentum(false, MOMENTUM_GAIN_GOL);
            narrar("\n" + minuto + "' - ⚽ GOOOOL DO " + visitante.getNome().toUpperCase() + "!!!");
            narrar("   Placar: " + golsCasa + " x " + golsFora);
            if (momentum < -MOMENTUM_ALTO) {
                narrar("   💪 " + visitante.getSigla() + " está dominando a partida!\n");
            } else {
                narrar("");
            }
            notificarEvento(new MatchEvent(TipoEvento.GOL_FORA,
                    "Gol de " + visitante.getNome(), false, 100));
        }
        // Chute com momentum
        else if (rand < 0.04 + Math.abs(momentum) * 0.0005) {
            if (casaAtaca) {
                chutesCasa++;
                aumentarMomentum(true, MOMENTUM_GAIN_CHUTE);
                narrar(minuto + "' - 🎯 Chute de " + mandante.getSigla() + "!");
                notificarEvento(new MatchEvent(TipoEvento.CHUTE_CASA, "Chute", true, 70));
            } else {
                chutesFora++;
                aumentarMomentum(false, MOMENTUM_GAIN_CHUTE);
                narrar(minuto + "' - 🎯 Chute de " + visitante.getSigla() + "!");
                notificarEvento(new MatchEvent(TipoEvento.CHUTE_FORA, "Chute", false, 70));
            }
        }
        // Escanteio (novo evento)
        else if (rand < 0.12) {
            processarEscanteio(casaAtaca);
        }
        // Falta perigosa (novo evento)
        else if (rand < 0.14) {
            processarFaltaPerigosa(casaAtaca);
        }
        // Pênalti (raro)
        else if (rand < 0.14 + CHANCE_PENALTI * getModificadorClima()) {
            processarPenalti(casaAtaca);
        }
        // Eventos variados
        else if (rand < 0.18) {
            String[] eventos = {
                    "🔄 Troca de passes no meio-campo",
                    "🏃 Contra-ataque perigoso!",
                    "🧤 Grande defesa do goleiro!",
                    "⚠️ Falta cometida"
            };
            String evento = eventos[random.nextInt(eventos.length)];
            narrar(minuto + "' - " + evento);

            if (evento.contains("defesa")) {
                // Defesa quebra momentum adversário
                if (casaAtaca) {
                    momentum = Math.max(-50, momentum - 15);
                } else {
                    momentum = Math.min(50, momentum + 15);
                }
                notificarEvento(new MatchEvent(TipoEvento.DEFESA, evento, casaAtaca, 50));
            } else if (evento.contains("Contra-ataque")) {
                aumentarMomentum(casaAtaca, MOMENTUM_GAIN_ATAQUE);
                notificarEvento(new MatchEvent(
                        casaAtaca ? TipoEvento.ATAQUE_CASA : TipoEvento.ATAQUE_FORA,
                        evento, casaAtaca, 40));
            } else {
                notificarEvento(new MatchEvent(TipoEvento.MEIO, evento, casaAtaca, 10));
            }
        }

        // Verificar impedimento em ataques perigosos
        if (rand < 0.08 && !verificarImpedimento(casaAtaca)) {
            // Ataque continuou normalmente
        }

        // Verificar lesões (chance muito baixa)
        verificarLesao(casaAtaca);

        // Notifica mudança de momentum se callback disponível
        if (onMomentumChange != null) {
            String desc = momentum > MOMENTUM_ALTO ? mandante.getSigla() + " pressionando"
                    : momentum < -MOMENTUM_ALTO ? visitante.getSigla() + " pressionando" : "Equilibrado";
            onMomentumChange.accept(new MomentumInfo(momentum, momentum > 0, desc));
        }
    }

    /**
     * Aumenta o momentum de um time.
     */
    private void aumentarMomentum(boolean casaGanha, int quantidade) {
        if (casaGanha) {
            momentum = Math.min(100, momentum + quantidade);
        } else {
            momentum = Math.max(-100, momentum - quantidade);
        }
    }

    // ==================== BOLAS PARADAS ====================

    /**
     * Processa um escanteio.
     */
    private void processarEscanteio(boolean casaCobra) {
        if (casaCobra) {
            escanteiosCasa++;
        } else {
            escanteiosFora++;
        }

        String time = casaCobra ? mandante.getSigla() : visitante.getSigla();
        narrar(minuto + "' - 🚩 Escanteio para " + time + "!");
        notificarEvento(new MatchEvent(TipoEvento.ESCANTEIO, "Escanteio", casaCobra, 40));

        // Modificador de clima (chuva dificulta cruzamentos)
        double modClima = clima == Clima.CHUVA ? 0.7 : 1.0;

        // Chance de gol de escanteio
        if (random.nextDouble() < CHANCE_ESCANTEIO_GOL * modClima * (casaCobra ? bonusTaticaCasa : bonusTaticaFora)) {
            if (casaCobra) {
                golsCasa++;
                chutesCasa++;
                aumentarMomentum(true, MOMENTUM_GAIN_GOL);
                narrar("   ⚽ GOL DE CABEÇA! " + mandante.getNome().toUpperCase() + " marca de escanteio!");
                narrar("   Placar: " + golsCasa + " x " + golsFora + "\n");
                notificarEvento(new MatchEvent(TipoEvento.GOL_ESCANTEIO, "Gol de escanteio", true, 100));
            } else {
                golsFora++;
                chutesFora++;
                aumentarMomentum(false, MOMENTUM_GAIN_GOL);
                narrar("   ⚽ GOL DE CABEÇA! " + visitante.getNome().toUpperCase() + " marca de escanteio!");
                narrar("   Placar: " + golsCasa + " x " + golsFora + "\n");
                notificarEvento(new MatchEvent(TipoEvento.GOL_ESCANTEIO, "Gol de escanteio", false, 100));
            }
        }
    }

    /**
     * Processa uma falta perigosa (livre direta).
     */
    private void processarFaltaPerigosa(boolean casaCobra) {
        if (casaCobra) {
            faltasCasa++;
        } else {
            faltasFora++;
        }

        String time = casaCobra ? mandante.getSigla() : visitante.getSigla();
        narrar(minuto + "' - ⚠️ FALTA PERIGOSA para " + time + "! Na entrada da área!");
        notificarEvento(new MatchEvent(TipoEvento.FALTA, "Falta perigosa", casaCobra, 60));

        // Chance de gol de falta direta
        if (random.nextDouble() < CHANCE_FALTA_GOL) {
            if (casaCobra) {
                golsCasa++;
                aumentarMomentum(true, MOMENTUM_GAIN_GOL);
                narrar("   ⚽ GOLAÇO DE FALTA! " + mandante.getNome().toUpperCase() + "!");
                narrar("   A bola entrou no ângulo! Placar: " + golsCasa + " x " + golsFora + "\n");
                notificarEvento(new MatchEvent(TipoEvento.GOL_FALTA, "Gol de falta", true, 100));
            } else {
                golsFora++;
                aumentarMomentum(false, MOMENTUM_GAIN_GOL);
                narrar("   ⚽ GOLAÇO DE FALTA! " + visitante.getNome().toUpperCase() + "!");
                narrar("   A bola entrou no ângulo! Placar: " + golsCasa + " x " + golsFora + "\n");
                notificarEvento(new MatchEvent(TipoEvento.GOL_FALTA, "Gol de falta", false, 100));
            }
        } else {
            narrar("   O goleiro fez a defesa!");
        }
    }

    /**
     * Processa um pênalti.
     */
    private void processarPenalti(boolean casaCobra) {
        if (casaCobra) {
            penaltisCasa++;
        } else {
            penaltisFora++;
        }

        String time = casaCobra ? mandante.getNome() : visitante.getNome();
        narrar("\n" + minuto + "' - 🔴 PÊNALTI PARA " + time.toUpperCase() + "!");
        narrar("   O árbitro aponta para a marca da cal!");
        notificarEvento(new MatchEvent(TipoEvento.PENALTI, "Pênalti!", casaCobra, 90));

        // Conversão do pênalti (75% base)
        if (random.nextDouble() < CHANCE_PENALTI_GOL) {
            if (casaCobra) {
                golsCasa++;
                aumentarMomentum(true, MOMENTUM_GAIN_GOL);
                narrar("   ⚽ GOOOOL DE PÊNALTI! " + mandante.getNome().toUpperCase() + "!");
                narrar("   Placar: " + golsCasa + " x " + golsFora + "\n");
                notificarEvento(new MatchEvent(TipoEvento.GOL_PENALTI, "Gol de pênalti", true, 100));
            } else {
                golsFora++;
                aumentarMomentum(false, MOMENTUM_GAIN_GOL);
                narrar("   ⚽ GOOOOL DE PÊNALTI! " + visitante.getNome().toUpperCase() + "!");
                narrar("   Placar: " + golsCasa + " x " + golsFora + "\n");
                notificarEvento(new MatchEvent(TipoEvento.GOL_PENALTI, "Gol de pênalti", false, 100));
            }
        } else {
            narrar("   🧤 DEFENDEUUU! O goleiro pegou o pênalti!");
            // Goleiro ganha momentum
            aumentarMomentum(!casaCobra, MOMENTUM_GAIN_GOL);
        }
    }

    /**
     * Verifica se houve impedimento no ataque.
     * Retorna true se o ataque foi cancelado por impedimento.
     */
    private boolean verificarImpedimento(boolean casaAtaca) {
        if (random.nextDouble() < CHANCE_IMPEDIMENTO) {
            if (casaAtaca) {
                impedimentosCasa++;
            } else {
                impedimentosFora++;
            }

            String time = casaAtaca ? mandante.getSigla() : visitante.getSigla();
            narrar(minuto + "' - 🏳️ IMPEDIMENTO de " + time + "!");
            notificarEvento(new MatchEvent(TipoEvento.IMPEDIMENTO, "Impedimento", casaAtaca, 20));
            return true;
        }
        return false;
    }

    /**
     * Verifica se houve lesão.
     */
    private void verificarLesao(boolean casaTime) {
        if (random.nextDouble() < CHANCE_LESAO * (clima == Clima.CALOR ? 2.0 : 1.0)) {
            if (casaTime) {
                lesoesCasa++;
            } else {
                lesoesFora++;
            }

            String time = casaTime ? mandante.getSigla() : visitante.getSigla();
            narrar("\n" + minuto + "' - 🏥 LESÃO! Jogador de " + time + " fica no chão!");
            narrar("   Atendimento médico em campo. Será substituído.\n");
            notificarEvento(new MatchEvent(TipoEvento.LESAO, "Lesão!", casaTime, 50));
        }
    }

    /**
     * Aplica modificadores de clima nos eventos.
     */
    private double getModificadorClima() {
        switch (clima) {
            case CHUVA:
                return 0.9; // -10% precisão
            case CALOR:
                return 0.95; // -5% performance geral
            default:
                return 1.0;
        }
    }

    /**
     * Define o clima da partida.
     */
    public void setClima(Clima clima) {
        this.clima = clima;
    }

    /**
     * Calcula bônus tático baseado na formação (meio-campo).
     * Formações com mais jogadores no meio ganham vantagem.
     */
    public void calcularBonusTatico() {
        // Simplificado: usa força geral + bônus mandante
        double forcaCasa = mandante.getForcaTime() + BONUS_MANDANTE;
        double forcaFora = visitante.getForcaTime();
        double total = forcaCasa + forcaFora;

        bonusTaticaCasa = (forcaCasa / total) * 2; // 0.8 a 1.2
        bonusTaticaFora = (forcaFora / total) * 2;
    }

    /**
     * Finaliza a partida.
     */
    private void finalizarPartida() {
        timer.stop();

        narrar("\n🏁 90' - APITA O ÁRBITRO! FIM DE JOGO!");
        narrar("\n" + mandante.getNome() + " " + golsCasa + " x " + golsFora + " " + visitante.getNome());

        // Registra gols na partida
        for (int i = 0; i < golsCasa; i++) {
            partida.registrarGol(mandante, null, null);
        }
        for (int i = 0; i < golsFora; i++) {
            partida.registrarGol(visitante, null, null);
        }
        partida.finalizar();

        if (onFimJogo != null) {
            onFimJogo.run();
        }
    }

    private void narrar(String texto) {
        if (onNarracao != null) {
            onNarracao.accept(texto);
        }
    }

    private void notificarEvento(MatchEvent evento) {
        if (onEvento != null) {
            onEvento.accept(evento);
        }
    }

    // Getters para estado do jogo
    public int getGolsCasa() {
        return golsCasa;
    }

    public int getGolsFora() {
        return golsFora;
    }

    public int getChutesCasa() {
        return chutesCasa;
    }

    public int getChutesFora() {
        return chutesFora;
    }

    public int getPosseCasa() {
        return posseCasa;
    }

    public int getPosseFora() {
        return posseFora;
    }

    public int getMinuto() {
        return minuto;
    }

    public Team getMandante() {
        return mandante;
    }

    public Team getVisitante() {
        return visitante;
    }

    public String getPlacar() {
        return golsCasa + " x " + golsFora;
    }

    public int getMomentum() {
        return momentum;
    }

    /**
     * Para a simulação.
     */
    public void parar() {
        if (timer != null) {
            timer.stop();
        }
    }
}
