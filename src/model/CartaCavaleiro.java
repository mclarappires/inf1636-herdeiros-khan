package model;

/** Cavaleiro: melhorias com símbolo de cavalo ou de punho. */
class CartaCavaleiro extends CartaConselheiroSecreto {

	CartaCavaleiro() {
		super("Cavaleiro", 2, 4, 5);
	}

	@Override
	int medir(Jogador j, Tabuleiro t) {
		int total = 0;
		for (Melhoria m : j.getTabuleiroJogador().getTodasMelhorias()) {
			if (m.getTipo() == TipoMelhoria.DOIS_CAVALOS || m.getTipo() == TipoMelhoria.PUNHO) {
				total++;
			}
		}
		return total;
	}

}
