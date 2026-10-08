package model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


class parada {
	
	private final int id;
	private final int capacidadePeoes;
	private final int capacidadeYurts;
	
	private final Map<Integer, parada> adjacentes = new HashMap<Integer, parada>();
	
	private final List<yurt> yurts = new LinkedList<yurt>();
	
	parada(int id, int capacidadePeoes, int capacidadeYurts){
		this.id = id;
		this.capacidadePeoes = capacidadePeoes;
		this.capacidadeYurts = capacidadeYurts;
	}
	
	int getId() {
		return id;
	}
	
	int getCapacidadePeoes(){
		return capacidadePeoes;
	}
	
	int getCapacidadeYurts(){
		return capacidadeYurts;
	}
	
	/** Cria a rota nos dois sentidos entre esta parada e outra. */
	void ligar(parada outra) {
		if (outra == null || outra == this) {
			throw new IllegalArgumentException("Ligação inválida na parada " + id);
		}
		adjacentes.put(outra.id, outra);
		outra.adjacentes.put(this.id, this);
	}
	
	Collection<parada> getAdjacentes() {
		return Collections.unmodifiableCollection(adjacentes.values());
	}
	
	boolean podeConstruirYurt() {
		return yurts.size() < capacidadeYurts;
	}
 
	void adicionarYurt(yurt y) {
		if (y == null) {
			throw new IllegalArgumentException("Yurt nulo");
		}
		if (!podeConstruirYurt()) {
			throw new IllegalStateException("Parada " + id + " não aceita mais yurts");
		}
		yurts.add(y);
	}
	
	
	
	
	
}
