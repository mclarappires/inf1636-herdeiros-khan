package model;

class Peao {

	private final Cor dono;
	private Parada posicao;

	Peao(Cor dono, Parada inicial){
		if (dono == null) {
			throw new IllegalArgumentException("Peão precisa de um dono");
		}
		if (inicial == null) {
			throw new IllegalArgumentException("Peão precisa de uma parada inicial");
		}
		this.dono = dono;
		this.posicao = inicial;
		inicial.entrarPeao(this);
	}

	Cor getDono() {
		return dono;
	}

	Parada getPosicao() {
		return posicao;
	}

	void moverPara(Parada destino) {
		if (destino == null) {
			throw new IllegalArgumentException("Parada nula");
		}
		posicao.sairPeao(this);
		destino.entrarPeao(this);
		posicao = destino;
	}

	/** O peão pode passar por uma parada cheia, mas só encerra o movimento onde houver vaga. */
	boolean podeTerminarEm(Parada p) {
		return p != null && p.temVagaPara(this);
	}

}
