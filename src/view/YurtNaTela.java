package view;

import java.awt.Color;

/** Um yurt construido: em qual parada esta e a cor do dono. So dados para desenhar. */
class YurtNaTela {

    private final int parada;
    private final Color cor;

    YurtNaTela(int parada, Color cor) {
        this.parada = parada;
        this.cor = cor;
    }

    int getParada() {
        return parada;
    }

    Color getCor() {
        return cor;
    }
}