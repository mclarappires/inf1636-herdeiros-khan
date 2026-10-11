package model;

enum TipoTributo {
	ESPADA(10), //ataca cidades
	YURT(10), // coloca yurts no tabuleiro
	MOEDA(15); //moedas compram melhorias
	
	private final int quantidadeNoJogo;

	private TipoTributo(int quantidadeNoJogo) {
		this.quantidadeNoJogo = quantidadeNoJogo;
	}

	public int getQuantidadeNoJogo() {
		return quantidadeNoJogo;
	}
}
