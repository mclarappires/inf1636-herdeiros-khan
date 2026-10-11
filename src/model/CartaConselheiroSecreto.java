package model;

abstract class CartaConselheiroSecreto {

	/** Votos (de 0 a 3) que a carta rende ao jogador na contagem final. */
	abstract int votosFinais(Jogador j, Tabuleiro t);

}
