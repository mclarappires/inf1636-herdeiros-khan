package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class Karakorum extends Parada{

	static final int TESOUROS_POR_ENTREGA = 3;
	static final int VOTOS_POR_ENTREGA = 2;

	private final Map<TipoTesouro, Integer> pedido;
	private Conselheiro conselheiro = null; //criado quando a peça de bônus é colocada

	//usa o pedido padrão, até ser conferido no tabuleiro físico
	Karakorum(int id){
		this(id, pedidoPadrao());
	}

	Karakorum(int id, Map<TipoTesouro, Integer> pedido){
		super(id, Integer.MAX_VALUE, 0, null);
		if (pedido == null) {
			throw new IllegalArgumentException("Pedido de tesouros nulo");
		}
		int total = 0;
		for (Map.Entry<TipoTesouro, Integer> e : pedido.entrySet()) {
			if (e.getKey() == null || e.getValue() == null || e.getValue() <= 0) {
				throw new IllegalArgumentException("Pedido inválido: " + e);
			}
			total += e.getValue();
		}
		if (total != TESOUROS_POR_ENTREGA) {
			throw new IllegalArgumentException(
					"Karakorum pede exatamente " + TESOUROS_POR_ENTREGA + " tesouros");
		}
		this.pedido = new HashMap<TipoTesouro, Integer>(pedido);
	}

	private static Map<TipoTesouro, Integer> pedidoPadrao() {
		Map<TipoTesouro, Integer> padrao = new HashMap<TipoTesouro, Integer>();
		padrao.put(TipoTesouro.PELES, 1);
		padrao.put(TipoTesouro.FERRO, 1);
		padrao.put(TipoTesouro.GADO, 1);
		return padrao;
	}

	/** Coloca a peça de bônus sorteada na preparação; ela vai para o primeiro a entregar aqui. */
	void colocarBonus(PecaBonus bonus) {
		if (conselheiro != null) {
			throw new IllegalStateException("Karakorum já tem peça de bônus");
		}
		conselheiro = new Conselheiro(pedido, VOTOS_POR_ENTREGA, bonus);
	}

	boolean temBonus() {
		return conselheiro != null;
	}

	Conselheiro getConselheiro() {
		if (conselheiro == null) {
			throw new IllegalStateException("Karakorum ainda não recebeu a peça de bônus");
		}
		return conselheiro;
	}

	Map<TipoTesouro, Integer> getPedido() {
		return Collections.unmodifiableMap(pedido);
	}

	/** Yurt inicial do Tolui: única exceção à regra de que Karakorum não aceita yurts. */
	void colocarYurtInicial(Yurt y) {
		if (y == null) {
			throw new IllegalArgumentException("Yurt nulo");
		}
		guardarYurt(y);
	}
}
