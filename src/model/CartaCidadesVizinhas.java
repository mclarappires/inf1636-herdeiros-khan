package model;

/**
 * Cidades com um mínimo de yurts do jogador em paradas adjacentes. O yurt de conquista,
 * dentro da cidade, não conta como adjacente. Prefeito e Cidadão só mudam os números.
 */
class CartaCidadesVizinhas extends CartaConselheiroSecreto {

	private final int minimoYurts;

	CartaCidadesVizinhas(String nome, int minimoYurts, int umVoto, int doisVotos, int tresVotos) {
		super(nome, umVoto, doisVotos, tresVotos);
		if (minimoYurts <= 0) {
			throw new IllegalArgumentException("Mínimo de yurts deve ser positivo");
		}
		this.minimoYurts = minimoYurts;
	}

	static CartaCidadesVizinhas prefeito() {
		return new CartaCidadesVizinhas("Prefeito", 2, 2, 3, 4);
	}

	static CartaCidadesVizinhas cidadao() {
		return new CartaCidadesVizinhas("Cidadão", 1, 3, 6, 8);
	}

	@Override
	int medir(Jogador j, tabuleiro t) {
		int cidades = 0;
		for (cidade c : t.getCidades()) {
			if (ContagemDeYurts.nasParadas(j.getCor(), c.getParadasAdjacentes()) >= minimoYurts) {
				cidades++;
			}
		}
		return cidades;
	}

}
