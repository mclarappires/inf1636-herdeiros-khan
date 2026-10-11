package model;

/** Governador: regiões com pelo menos 2 yurts e 2 melhorias na coluna da região. */
class CartaGovernador extends CartaConselheiroSecreto {

	static final int MINIMO_YURTS = 2;
	static final int MINIMO_MELHORIAS = 2;

	CartaGovernador() {
		super("Governador", 1, 2, 3);
	}

	@Override
	int medir(Jogador j, tabuleiro t) {
		int regioes = 0;
		for (regiao r : regiao.values()) {
			if (ContagemDeYurts.naRegiao(j.getCor(), t, r) >= MINIMO_YURTS
					&& j.getTabuleiroJogador().contarMelhorias(r) >= MINIMO_MELHORIAS) {
				regioes++;
			}
		}
		return regioes;
	}

}
