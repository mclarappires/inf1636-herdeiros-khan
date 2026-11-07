package view;

import java.awt.Color;

/** Uma ficha na trilha do Kurultai: quantos votos marca, a cor e se é a ficha neutra (branca). */
class FichaNaTela {

    private final int votos;
    private final Color cor;
    private final boolean neutra;

    FichaNaTela(int votos, Color cor, boolean neutra) {
        this.votos = votos;
        this.cor = cor;
        this.neutra = neutra;
    }

    int getVotos() {
        return votos;
    }

    Color getCor() {
        return cor;
    }

    boolean isNeutra() {
        return neutra;
    }
}
