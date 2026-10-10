package model;

enum TipoMelhoria {
	MOEDA(8),        // conta como 1 moeda no turno
	ESPADA(8),       // conta como 1 espada no turno
	YURT(8),         // conta como 1 tributo de yurt no turno
	TESOURO(5),      // conta como 1 tesouro do tipo no turno (1 peça por tipo de tesouro)
	PUNHO(6),        // 1 ação extra de pegar tributo
	DOIS_CAVALOS(9); // 2 movimentos extras

	private final int quantidadeNoJogo;

	private TipoMelhoria(int quantidadeNoJogo) {
		this.quantidadeNoJogo = quantidadeNoJogo;
	}

	public int getQuantidadeNoJogo() {
		return quantidadeNoJogo;
	}
}
