package model;

/** Mensageiro: paradas duplas com yurt do jogador; Karakorum não é dupla e não conta. */
class CartaMensageiro extends CartaConselheiroSecreto {

	CartaMensageiro() {
		super("Mensageiro", 2, 4, 6);
	}

	@Override
	int medir(Jogador j, Tabuleiro t) {
		int paradas = 0;
		for (Parada p : t.getParadas()) {
			if (p.isDupla() && p.temYurtDe(j.getCor())) {
				paradas++;
			}
		}
		return paradas;
	}

}
