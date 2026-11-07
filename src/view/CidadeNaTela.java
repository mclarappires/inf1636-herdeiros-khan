package view;

import java.util.List;

/**
 * Uma cidade revelada: seu id no mapa, os tesouros que ainda estao sobre ela e se o jogador
 * da vez pode ataca-la agora.
 */
class CidadeNaTela {

    private final int id;
    private final List<String> tesouros; // um item por peca, com o nome do tipo (ex.: "PELES")
    private final boolean atacavel;

    CidadeNaTela(int id, List<String> tesouros, boolean atacavel) {
        this.id = id;
        this.tesouros = tesouros;
        this.atacavel = atacavel;
    }

    int getId() {
        return id;
    }

    List<String> getTesouros() {
        return tesouros;
    }

    boolean isAtacavel() {
        return atacavel;
    }
}
