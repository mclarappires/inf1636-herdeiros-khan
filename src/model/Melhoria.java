package model;

class Melhoria {

	private final TipoMelhoria tipo;
	private final Coluna coluna; //coluna colorida com a mesma cor de fundo da melhoria
	private final tipoTesouro tesouro; //nulo quando a melhoria não for de tesouro

	//construtor das melhorias que não são de tesouro
	Melhoria(TipoMelhoria tipo, Coluna coluna){
		if (tipo == null) {
			throw new IllegalArgumentException("Melhoria precisa de um tipo");
		}
		if (tipo == TipoMelhoria.TESOURO) {
			throw new IllegalArgumentException("Melhoria de tesouro precisa do tipo de tesouro");
		}
		validar(coluna);
		this.tipo = tipo;
		this.coluna = coluna;
		this.tesouro = null;
	}

	//construtor das melhorias de tesouro virtual
	Melhoria(tipoTesouro tesouro, Coluna coluna){
		if (tesouro == null) {
			throw new IllegalArgumentException("Tesouro nulo");
		}
		validar(coluna);
		this.tipo = TipoMelhoria.TESOURO;
		this.coluna = coluna;
		this.tesouro = tesouro;
	}

	private static void validar(Coluna coluna) {
		if (coluna == null || coluna.isBranca()) {
			throw new IllegalArgumentException("Melhoria precisa da cor de uma coluna colorida");
		}
	}

	TipoMelhoria getTipo() {
		return tipo;
	}

	Coluna getColuna() {
		return coluna;
	}

	tipoTesouro getTesouro() {
		return tesouro;
	}

	/** Dá ao jogador o recurso da melhoria; chamado quando a coluna dela é ativada. */
	void aplicar(Jogador j) {
		if (j == null) {
			throw new IllegalArgumentException("Jogador nulo");
		}
		switch (tipo) {
		case MOEDA:
			j.receberTributoVirtual(tipoTributo.MOEDA);
			break;
		case ESPADA:
			j.receberTributoVirtual(tipoTributo.ESPADA);
			break;
		case YURT:
			j.receberTributoVirtual(tipoTributo.YURT);
			break;
		case TESOURO:
			j.receberTesouroVirtual(tesouro);
			break;
		case PUNHO:
			j.receberPunhos(1);
			break;
		case DOIS_CAVALOS:
			j.receberMovimentos(2);
			break;
		}
	}

}
