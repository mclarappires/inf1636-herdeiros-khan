package model;

/** Ter uma quantidade de tesouros de qualquer tipo; tesouros virtuais do turno valem e nada é gasto. */
class CartaTerTesouros extends CartaConselheiroAberto {

	CartaTerTesouros(int quantidade) {
		super(quantidade);
	}

	@Override
	int medir(Jogador j, tabuleiro t) {
		int total = 0;
		for (tipoTesouro tipo : tipoTesouro.values()) {
			total += j.getDisponivel(tipo);
		}
		return total;
	}

}
