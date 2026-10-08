package model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class Provincia {
	
	static final int MAXIMO_TRIBUTOS = 3;
	
	private final int id;
	private final tipoTributo tipo;
	private int quantidade;
	
	private final Map<Integer, parada> adjacentes = new HashMap<Integer, parada>();
	
	Provincia(int id, tipoTributo tipo){
		if(tipo == null) {
			throw new IllegalArgumentException("Província precisa de um tipo de tributo");
		}
		this.id = id;
		this.tipo = tipo;
	}
	
	int getId() {
		return id;
	}
	
	tipoTributo getTipo(){
		return tipo;
	}
	
	int getQuantidade() {
		return quantidade;
	}
	
	boolean temTributo() {
		return quantidade > 0;
	}
	
	boolean adicionarTributo() {
		if (quantidade >= MAXIMO_TRIBUTOS) {
			return false;
		}
		quantidade++;
		return true;
	}
	
	tipoTributo retirarTributo() {
		if(quantidade == 0) {
			throw new IllegalStateException("Província " + id + " está vazia");
		}
		quantidade--;
		return tipo;
	}

	void ligarParada(parada parada) {
		if (parada == null) {
			throw new IllegalArgumentException("Parada nula");
		}
		adjacentes.put(parada.getId(), parada);
	}
	
	boolean adjacenteA(parada parada) {
		return parada != null && adjacentes.containsKey(parada.getId());
	}
 
	Collection<parada> getParadasAdjacentes() {
		return Collections.unmodifiableCollection(adjacentes.values());
	}
	
}
