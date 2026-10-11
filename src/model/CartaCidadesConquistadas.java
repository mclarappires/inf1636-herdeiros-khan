package model;

/** Ter conquistado uma quantidade de cidades, contadas pelo yurt de conquista do jogador. */
class CartaCidadesConquistadas extends CartaConselheiroAberto {

	CartaCidadesConquistadas(int quantidade) {
		super(quantidade);
	}

	@Override
	int medir(Jogador j, tabuleiro t) {
		int total = 0;
		for (cidade c : t.getCidadesConquistadas()) {
			yurt y = c.getYurtDeConquista();
			if (y != null && y.getDono() == j.getCor()) {
				total++;
			}
		}
		return total;
	}

}
