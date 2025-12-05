package com.brasfm.model.enums;

/**
 * Funções táticas específicas que definem o comportamento do jogador em campo.
 * Cada função requer diferentes atributos e afeta o posicionamento/decisões.
 */
public enum Role {
    // Goleiro
    GOALKEEPER("Goleiro", Position.GOLEIRO, "Goleiro tradicional"),
    SWEEPER_KEEPER("Goleiro Líbero", Position.GOLEIRO, "Sai do gol para varrer bolas longas"),

    // Zagueiros
    CENTRAL_DEFENDER("Zagueiro Central", Position.ZAGUEIRO, "Defensor tradicional focado em marcação"),
    BALL_PLAYING_DEFENDER("Zagueiro Construtor", Position.ZAGUEIRO, "Inicia jogadas com passes precisos"),
    STOPPER("Zagueiro Agressivo", Position.ZAGUEIRO, "Avança para cortar jogadas antes da área"),
    LIBERO("Líbero", Position.ZAGUEIRO, "Zagueiro livre que também articula o jogo"),

    // Laterais
    FULL_BACK("Lateral Defensivo", Position.LATERAL_DIREITO, "Foco em marcação, pouco avança"),
    WING_BACK("Ala", Position.LATERAL_DIREITO, "Sobe constantemente pela ponta"),
    INVERTED_WING_BACK("Lateral Invertido", Position.LATERAL_DIREITO, "Corta para o meio quando ataca"),
    COMPLETE_WING_BACK("Lateral Completo", Position.LATERAL_DIREITO, "Faz todo o corredor, ataque e defesa"),

    // Volantes
    ANCHOR_MAN("Volante Fixo", Position.VOLANTE, "Fica à frente da zaga, não avança"),
    DEFENSIVE_MIDFIELDER("Volante", Position.VOLANTE, "Protege a defesa e recupera bolas"),
    DEEP_LYING_PLAYMAKER("Meia Recuado", Position.VOLANTE, "Dita o ritmo recuando para receber"),
    REGISTA("Regista", Position.VOLANTE, "Maestro que orquestra de trás"),
    BOX_TO_BOX("Box-to-Box", Position.VOLANTE, "Percorre todo o campo, ataca e defende"),
    CARRILERO("Carrilero", Position.VOLANTE, "Corre pelos corredores laterais"),
    HALF_BACK("Meia Zagueiro", Position.VOLANTE, "Recua para formar linha de 3 na defesa"),

    // Meias
    CENTRAL_MIDFIELDER("Meia Central", Position.MEIA, "Meia equilibrado no centro"),
    ADVANCED_PLAYMAKER("Meia Armador", Position.MEIA_ATACANTE, "Criador de jogadas avançado"),
    MEZZALA("Mezzala", Position.MEIA, "Meia que ataca pelo corredor interno"),
    TREQUARTISTA("Trequartista", Position.MEIA_ATACANTE, "Gênio criativo com liberdade total"),
    ATTACKING_MIDFIELDER("Meia Ofensivo", Position.MEIA_ATACANTE, "Meia focado em assistências e gols"),
    ROAMING_PLAYMAKER("Armador Móvel", Position.MEIA, "Busca espaços por todo o campo"),
    ENGANCHE("Enganche", Position.MEIA_ATACANTE, "Clássico 10 sul-americano, pouco móvel"),

    // Pontas
    WINGER("Ponta", Position.PONTA_DIREITA, "Ponta tradicional que cruza"),
    INVERTED_WINGER("Ponta Invertido", Position.PONTA_DIREITA, "Corta para dentro para finalizar"),
    INSIDE_FORWARD("Atacante de Lado", Position.PONTA_DIREITA, "Joga aberto mas busca o gol"),
    WIDE_PLAYMAKER("Armador Aberto", Position.PONTA_DIREITA, "Cria jogadas pelas pontas"),
    RAUMDEUTER("Raumdeuter", Position.PONTA_DIREITA, "Interpretador de espaços, se move sem bola"),

    // Atacantes
    STRIKER("Atacante", Position.CENTROAVANTE, "Atacante tradicional focado em gols"),
    ADVANCED_FORWARD("Atacante Avançado", Position.CENTROAVANTE, "Joga no limite da linha"),
    COMPLETE_FORWARD("Atacante Completo", Position.CENTROAVANTE, "Faz tudo: marca, cria e finaliza"),
    TARGET_MAN("Pivô", Position.CENTROAVANTE, "Usa força física para segurar e pivotar"),
    PRESSING_FORWARD("Atacante Pressionador", Position.CENTROAVANTE, "Inicia a marcação desde a frente"),
    POACHER("Matador", Position.CENTROAVANTE, "Vive na área esperando rebotes"),
    FALSE_NINE("Falso 9", Position.CENTROAVANTE, "Recua para criar espaço para outros"),
    DEEP_LYING_FORWARD("Segundo Atacante", Position.ATACANTE, "Joga entre as linhas");

    private final String nome;
    private final Position posicaoBase;
    private final String descricao;

    Role(String nome, Position posicaoBase, String descricao) {
        this.nome = nome;
        this.posicaoBase = posicaoBase;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public Position getPosicaoBase() {
        return posicaoBase;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Retorna as funções disponíveis para uma posição.
     */
    public static Role[] getRolesParaPosicao(Position posicao) {
        return java.util.Arrays.stream(values())
                .filter(r -> r.posicaoBase == posicao ||
                        (posicao == Position.LATERAL_ESQUERDO && r.posicaoBase == Position.LATERAL_DIREITO) ||
                        (posicao == Position.PONTA_ESQUERDA && r.posicaoBase == Position.PONTA_DIREITA))
                .toArray(Role[]::new);
    }

    /**
     * Verifica se esta função é compatível com uma posição.
     */
    public boolean isCompativelCom(Position posicao) {
        if (posicaoBase == posicao)
            return true;
        // Laterais e pontas são intercambiáveis entre lados
        if (posicao == Position.LATERAL_ESQUERDO && posicaoBase == Position.LATERAL_DIREITO)
            return true;
        if (posicao == Position.PONTA_ESQUERDA && posicaoBase == Position.PONTA_DIREITA)
            return true;
        return false;
    }
}
