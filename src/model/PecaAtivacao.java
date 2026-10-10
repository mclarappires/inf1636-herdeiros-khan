package model;

class PecaAtivacao {

	private final Coluna coluna;
	private boolean usada = false;

	PecaAtivacao(Coluna coluna){
		if (coluna == null) {
			throw new IllegalArgumentException("Peça de ativação precisa de uma coluna");
		}
		this.coluna = coluna;
	}

	Coluna getColuna() {
		return coluna;
	}

	boolean isUsada() {
		return usada;
	}

	void usar() {
		if (usada) {
			throw new IllegalStateException("Coluna " + coluna + " já está ativada");
		}
		usada = true;
	}

	void recuperar() {
		usada = false;
	}

}
