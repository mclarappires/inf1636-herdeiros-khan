package model;

/** Ter uma quantidade de colunas com um mínimo de melhorias cada (ex.: 2 colunas com 2, 1 coluna com 4). */
class CartaColunasComMelhorias extends CartaConselheiroAberto {

	private final int melhoriasPorColuna;

	CartaColunasComMelhorias(int colunas, int melhoriasPorColuna) {
		super(colunas);
		if (melhoriasPorColuna <= 0) {
			throw new IllegalArgumentException("Melhorias por coluna deve ser positivo");
		}
		this.melhoriasPorColuna = melhoriasPorColuna;
	}

	@Override
	int medir(Jogador j, tabuleiro t) {
		int colunas = 0;
		for (Coluna c : Coluna.values()) {
			if (j.getTabuleiroJogador().contarMelhorias(c) >= melhoriasPorColuna) {
				colunas++;
			}
		}
		return colunas;
	}

}
