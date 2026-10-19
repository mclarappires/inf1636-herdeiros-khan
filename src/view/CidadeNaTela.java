package view;

import java.util.List;

/** Uma cidade revelada: seu id no mapa e os tesouros que ainda estao sobre ela. */
class CidadeNaTela {

    private final int id;
    private final List<String> tesouros; // um item por peca, com o nome do tipo (ex.: "PELES")

    CidadeNaTela(int id, List<String> tesouros) {
        this.id = id;
        this.tesouros = tesouros;
    }

    int getId() {
        return id;
    }

    List<String> getTesouros() {
        return tesouros;
    }
}