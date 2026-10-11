package model;

/** Ter tesouros de tipos diferentes; tesouros virtuais do turno valem e nada é gasto. */
class CartaTesourosDiferentes extends CartaConselheiroAberto {

	CartaTesourosDiferentes(int tipos) {
		super(tipos);
	}

	@Override
	int medir(Jogador j, tabuleiro t) {
		int tipos = 0;
		for (tipoTesouro tipo : tipoTesouro.values()) {
			if (j.getDisponivel(tipo) > 0) {
				tipos++;
			}
		}
		return tipos;
	}

}
