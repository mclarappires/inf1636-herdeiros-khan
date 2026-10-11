package model;

import java.util.HashMap;
import java.util.Map;

class Conselheiro {

	static final int TESOUROS_DO_MESMO_TIPO = 2; //conselheiros comuns
	static final int TESOUROS_DE_QUALQUER_TIPO = 3; //conselheiro único
	static final int VOTOS_POR_ENTREGA = 1;

	private final Map<TipoTesouro, Integer> exigidos; //vazio quando o exigido for qualquer tesouro
	private final int quantQualquerTipo; //zero quando for pedido um tipo de tesouro específico

	private final int votosPorEntrega;
	private PecaBonus bonus; //sorteada na preparação; nula até ser colocada
	private boolean bonusColetado = false;

	//construtor de pedidos de tipos especificos de tesouros
	Conselheiro(Map<TipoTesouro, Integer> exigidos, int votosPorEntrega, PecaBonus bonus){
		this(exigidos, votosPorEntrega);
		colocarBonus(bonus);
	}

	//construtor de pedidos de tipos especificos, com a peça de bônus colocada depois
	Conselheiro(Map<TipoTesouro, Integer> exigidos, int votosPorEntrega){
		if (exigidos == null || exigidos.isEmpty()) {
			throw new IllegalArgumentException("Pedido de tesouros vazio");
		}

		for (Map.Entry<TipoTesouro, Integer> e : exigidos.entrySet()) {
			if(e.getKey() == null || e.getValue() == null || e.getValue() <=0) {
				throw new IllegalArgumentException("Pedido inválido: " + e);
			}
		}

		validar(votosPorEntrega);

		this.exigidos = new HashMap<TipoTesouro, Integer>(exigidos);
		this.quantQualquerTipo = 0;
		this.votosPorEntrega = votosPorEntrega;
	}

	//construtor de um pedido sem tipo de tesouro definido
	Conselheiro(int quantQualquerTipo, int votosPorEntrega, PecaBonus bonus){
		this(quantQualquerTipo, votosPorEntrega);
		colocarBonus(bonus);
	}

	//construtor de um pedido sem tipo definido, com a peça de bônus colocada depois
	Conselheiro(int quantQualquerTipo, int votosPorEntrega){
		if (quantQualquerTipo <=0) {
			throw new IllegalArgumentException("Quantidade deve ser positiva");
		}

		validar(votosPorEntrega);
		this.exigidos = new HashMap<TipoTesouro, Integer>();
		this.quantQualquerTipo = quantQualquerTipo;
		this.votosPorEntrega = votosPorEntrega;
	}

	/** Conselheiro comum: pede 2 tesouros de um tipo específico. */
	static Conselheiro comum(TipoTesouro tipo) {
		if (tipo == null) {
			throw new IllegalArgumentException("Tesouro nulo");
		}
		Map<TipoTesouro, Integer> pedido = new HashMap<TipoTesouro, Integer>();
		pedido.put(tipo, TESOUROS_DO_MESMO_TIPO);
		return new Conselheiro(pedido, VOTOS_POR_ENTREGA);
	}

	/** Conselheiro único: pede 3 tesouros de qualquer tipo. */
	static Conselheiro unico() {
		return new Conselheiro(TESOUROS_DE_QUALQUER_TIPO, VOTOS_POR_ENTREGA);
	}

	private static void validar(int votosPorEntrega) {
		if (votosPorEntrega <= 0) {
			throw new IllegalArgumentException("Votos por entrega deve ser positivo");
		}
	}

	/** Coloca a peça de bônus sorteada na preparação; só pode ser feito uma vez. */
	void colocarBonus(PecaBonus bonus) {
		if (bonus == null) {
			throw new IllegalArgumentException("Conselheiro precisa de uma peça de bônus");
		}
		if (this.bonus != null) {
			throw new IllegalStateException("Conselheiro já tem peça de bônus");
		}
		this.bonus = bonus;
	}

	boolean temBonus() {
		return bonus != null;
	}

	PecaBonus getBonus() {
		return bonus;
	}

	boolean aceita(Map<TipoTesouro, Integer> oferta) {
		if (oferta == null) {
			return false;
		}

		//verificacao para qualquer tipo de tesouro
		if (exigidos.isEmpty()) {
			int total = 0;
			for (Integer q : oferta.values()) {
				total += q;
			}

			return total >= quantQualquerTipo;
		}

		//verificacao para conselheiros que necessitam tipos especificos
		for (Map.Entry<TipoTesouro, Integer> e : exigidos.entrySet()) {
			Integer ofertada = oferta.get(e.getKey());
			if (ofertada == null || ofertada < e.getValue()) {
				return false;
			}
		}
		return true;
	}

	int getVotosPorEntrega() {
		return votosPorEntrega;
	}

	boolean bonusJaColetado() {
		return bonusColetado;
	}


	//retorna a pecaBonus pro primeiro jogador que concluir o conselheiro
	PecaBonus coletarBonus() {
		if (bonusColetado || bonus == null) {
			return null;
		}
		bonusColetado = true;
		return bonus;
	}

}
