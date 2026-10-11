package model;

/** Ter completado uma linha de melhorias no tabuleiro do jogador. */
class CartaLinhaDeMelhorias extends CartaConselheiroAberto {

	CartaLinhaDeMelhorias() {
		super(1);
	}

	@Override
	int medir(Jogador j, Tabuleiro t) {
		TabuleiroJogador doJogador = j.getTabuleiroJogador();
		int linhas = 0;
		for (int linha = 0; linha < TabuleiroJogador.LINHAS; linha++) {
			if (doJogador.linhaCompleta(linha)) {
				linhas++;
			}
		}
		return linhas;
	}

}
