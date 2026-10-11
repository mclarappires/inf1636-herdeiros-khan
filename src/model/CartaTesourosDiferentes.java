package model;

/** Ter tesouros de tipos diferentes; tesouros virtuais do turno valem e nada é gasto. */
class CartaTesourosDiferentes extends CartaConselheiroAberto {

	CartaTesourosDiferentes(int tipos) {
		super(tipos);
	}

	@Override
	int medir(Jogador j, Tabuleiro t) {
		int tipos = 0;
		for (TipoTesouro tipo : TipoTesouro.values()) {
			if (j.getDisponivel(tipo) > 0) {
				tipos++;
			}
		}
		return tipos;
	}

}
