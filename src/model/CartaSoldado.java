package model;

/** Soldado: melhorias no tabuleiro do jogador. */
class CartaSoldado extends CartaConselheiroSecreto {

	CartaSoldado() {
		super("Soldado", 4, 7, 10);
	}

	@Override
	int medir(Jogador j, tabuleiro t) {
		return j.getTabuleiroJogador().getTodasMelhorias().size();
	}

}
