package view;

import java.awt.Color;

/**
 * O que a View precisa saber de um peao para desenha-lo: em que parada esta
 * e de que cor e. O Controller monta esta lista a partir do Model.
 */
class PeaoNaTela {

    private int parada;
    private final Color cor;

    PeaoNaTela(int parada, Color cor) {
        this.parada = parada;
        this.cor = cor;
    }

    int getParada() {
        return parada;
    }

    void setParada(int parada) {
        this.parada = parada;
    }

    Color getCor() {
        return cor;
    }
}