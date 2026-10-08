package model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
 
class cidade {
	
	static final int TESOUROS_INICIAIS	 = 4;
	
	private final int id;
	private final regiao regiao;
	private boolean conquistada = false;
	private yurt yurtConquista = null;
	
	private final Map<tipoTesouro, Integer> tesouros = new HashMap<tipoTesouro, Integer>();
	
	private final Map<Integer, parada> paradasAdjacentes = new HashMap<Integer, parada>();
	
	cidade(int id, regiao regiao){
		if(regiao == null) {
			throw new IllegalArgumentException("Cidade precisa de uma região");
		}
		this.id = id;
		this.regiao = regiao;
	}
	
	int getId() {
		return id;
	}
	
	regiao getRegiao() {
		return regiao;
	}
 
	boolean isConquistada() {
		return conquistada;
	}
	
	/** Coloca um tesouro na cidade (usado quando ela é revelada). */
	void colocarTesouro(tipoTesouro tipo) {
		if (tipo == null) {
			throw new IllegalArgumentException("Tesouro nulo");
		}
		if (conquistada) {
			throw new IllegalStateException("Cidade " + id + " já foi conquistada");
		}
		Integer atual = tesouros.get(tipo);
		tesouros.put(tipo, atual == null ? 1 : atual + 1);
	}
	
	int getQuantidadeTesouros() {
		int total = 0;
		for (Integer q : tesouros.values()) {
			total += q;
		}
		return total;
	}
 
	int getQuantidade(tipoTesouro tipo) {
		Integer q = tesouros.get(tipo);
		return q == null ? 0 : q;
	}
 
	/** Só se pode atacar uma cidade que ainda tenha tesouros. */
	boolean temTesouro() {
		return getQuantidadeTesouros() > 0;
	}
	
	boolean tomarTesouro(tipoTesouro tipo) {
		if (conquistada) {
			throw new IllegalStateException("Cidade " + id + " já foi conquistada");
		}
		int quantidade = getQuantidade(tipo);
		if (quantidade == 0) {
			throw new IllegalStateException("Cidade " + id + " não tem tesouro " + tipo);
		}
		if (quantidade == 1) {
			tesouros.remove(tipo);
		} else {
			tesouros.put(tipo, quantidade - 1);
		}
		if (tesouros.isEmpty()) {
			conquistada = true;
		}
		return conquistada;
	}
	
	static int custoEmEspadas(int quantidadeTesouros) {
		if (quantidadeTesouros < 0) {
			throw new IllegalArgumentException("Quantidade negativa");
		}
		if (quantidadeTesouros == 0) {
			return 0;
		}
		return 1 + 2 * (quantidadeTesouros - 1);
	}
	
	void colocarYurtDeConquista(yurt yurt) {
		if (yurt == null) {
			throw new IllegalArgumentException("Yurt nulo");
		}
		if (!conquistada) {
			throw new IllegalStateException("Cidade " + id + " ainda não foi conquistada");
		}
		if (yurtConquista != null) {
			throw new IllegalStateException("Cidade " + id + " já tem yurt de conquista");
		}
		yurtConquista = yurt;
	}
 
	yurt getYurtDeConquista() {
		return yurtConquista;
	}
	
	/** Registra que esta cidade faz fronteira com a parada. */
	void ligarParada(parada parada) {
		if (parada == null) {
			throw new IllegalArgumentException("Parada nula");
		}
		paradasAdjacentes.put(parada.getId(), parada);
	}
 
	boolean adjacenteA(parada parada) {
		return parada != null && paradasAdjacentes.containsKey(parada.getId());
	}
 
	Collection<parada> getParadasAdjacentes() {
		return Collections.unmodifiableCollection(paradasAdjacentes.values());
	}

}
