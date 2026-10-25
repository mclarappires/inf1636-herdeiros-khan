package view;

import java.awt.Color;

/**
 * O que a View precisa saber de um peao para desenha-lo: em que parada esta,
 * de que cor e e, se o jogador tem mais de um (Altani), qual o numero dele.
 * O Controller monta esta lista a partir do Model.
 */
class PeaoNaTela {

    private int parada;
    private final Color cor;
    private final int numero; // 0 quando o jogador tem um peao so

    PeaoNaTela(int parada, Color cor) {
        this(parada, cor, 0);
    }

    PeaoNaTela(int parada, Color cor, int numero) {
        this.parada = parada;
        this.cor = cor;
        this.numero = numero;
    }

    int getNumero() {
        return numero;
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