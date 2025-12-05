package com.brasfm.ui.model;

import javax.swing.table.AbstractTableModel;
import com.brasfm.model.Player;
import java.util.List;
import java.util.ArrayList;

/**
 * TableModel para exibição do elenco.
 * Segue padrão MVC - atualiza automaticamente quando dados mudam.
 */
public class ElencoTableModel extends AbstractTableModel {

    private List<Player> jogadores;
    private static final String[] COLUNAS = {
            "Pos", "Nome", "Idade", "Força", "Energia", "Moral", "Passe", "Fin", "Def", "Salário"
    };

    public ElencoTableModel() {
        this.jogadores = new ArrayList<>();
    }

    public ElencoTableModel(List<Player> jogadores) {
        this.jogadores = jogadores != null ? jogadores : new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return jogadores.size();
    }

    @Override
    public int getColumnCount() {
        return COLUNAS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUNAS[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 2: // Idade
            case 3: // Força
            case 4: // Energia
            case 5: // Moral
            case 6: // Passe
            case 7: // Finalização
            case 8: // Defesa
                return Integer.class;
            default:
                return String.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= jogadores.size()) {
            return null;
        }

        Player p = jogadores.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return p.getPosicaoOriginal().getSigla();
            case 1:
                return p.getNome();
            case 2:
                return p.getIdade();
            case 3:
                return p.getForca();
            case 4:
                return p.getEnergia();
            case 5:
                return p.getMoral();
            case 6:
                return p.getPasse();
            case 7:
                return p.getFinalizacao();
            case 8:
                return p.getDesarme();
            case 9:
                return formatarSalario(p.getSalario());
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    private String formatarSalario(int salario) {
        if (salario >= 1000000) {
            return String.format("%.1fM", salario / 1000000.0);
        } else if (salario >= 1000) {
            return String.format("%dk", salario / 1000);
        }
        return String.valueOf(salario);
    }

    // ========== Métodos de manipulação ==========

    /**
     * Retorna o jogador na linha especificada.
     */
    public Player getJogadorAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < jogadores.size()) {
            return jogadores.get(rowIndex);
        }
        return null;
    }

    /**
     * Atualiza a lista de jogadores e notifica a tabela.
     */
    public void setJogadores(List<Player> novaLista) {
        this.jogadores = novaLista != null ? novaLista : new ArrayList<>();
        fireTableDataChanged();
    }

    /**
     * Adiciona um jogador e notifica a tabela.
     */
    public void addJogador(Player jogador) {
        jogadores.add(jogador);
        fireTableRowsInserted(jogadores.size() - 1, jogadores.size() - 1);
    }

    /**
     * Remove um jogador e notifica a tabela.
     */
    public void removeJogador(Player jogador) {
        int index = jogadores.indexOf(jogador);
        if (index >= 0) {
            jogadores.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }

    /**
     * Remove jogador pelo índice.
     */
    public void removeJogadorAt(int index) {
        if (index >= 0 && index < jogadores.size()) {
            jogadores.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }

    /**
     * Atualiza um jogador específico e notifica a tabela.
     */
    public void atualizarJogador(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < jogadores.size()) {
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    /**
     * Atualiza um jogador específico.
     */
    public void atualizarJogador(Player jogador) {
        int index = jogadores.indexOf(jogador);
        if (index >= 0) {
            fireTableRowsUpdated(index, index);
        }
    }

    /**
     * Notifica que todos os dados foram atualizados.
     */
    public void refresh() {
        fireTableDataChanged();
    }

    /**
     * Retorna a lista de jogadores.
     */
    public List<Player> getJogadores() {
        return jogadores;
    }

    /**
     * Ordena por força (maior para menor).
     */
    public void ordenarPorForca() {
        jogadores.sort((a, b) -> Integer.compare(b.getForca(), a.getForca()));
        fireTableDataChanged();
    }

    /**
     * Ordena por posição.
     */
    public void ordenarPorPosicao() {
        jogadores.sort((a, b) -> {
            int ordemA = getOrdemPosicao(a.getPosicaoOriginal().getSigla());
            int ordemB = getOrdemPosicao(b.getPosicaoOriginal().getSigla());
            return Integer.compare(ordemA, ordemB);
        });
        fireTableDataChanged();
    }

    private int getOrdemPosicao(String sigla) {
        if (sigla.contains("GOL"))
            return 0;
        if (sigla.contains("Z"))
            return 1;
        if (sigla.contains("L"))
            return 2;
        if (sigla.contains("V"))
            return 3;
        if (sigla.contains("M"))
            return 4;
        if (sigla.contains("A") || sigla.contains("P") || sigla.contains("C"))
            return 5;
        return 6;
    }
}
