package model;

class Peao {

	private final cor dono;
	private parada posicao;

	Peao(cor dono, parada inicial){
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

	cor getDono() {
		return dono;
	}

	parada getPosicao() {
		return posicao;
	}

	void moverPara(parada destino) {
		if (destino == null) {
			throw new IllegalArgumentException("Parada nula");
		}
		posicao.sairPeao(this);
		destino.entrarPeao(this);
		posicao = destino;
	}

	/** O peão pode passar por uma parada cheia, mas só encerra o movimento onde houver vaga. */
	boolean podeTerminarEm(parada p) {
		return p != null && p.temVagaPara(this);
	}

}
