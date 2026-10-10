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
	
	private final regiao regiao;
	
	private final Map<Integer, parada> adjacentes = new HashMap<Integer, parada>();
	
	private final List<yurt> yurts = new LinkedList<yurt>();

	private final List<Peao> peoes = new LinkedList<Peao>(); //peões que estão na parada

	parada(int id, int capacidadePeoes, int capacidadeYurts, regiao regiao){
		this.id = id;
		this.capacidadePeoes = capacidadePeoes;
		this.capacidadeYurts = capacidadeYurts;
		this.regiao = regiao;
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
	
	regiao getRegiao() {
		return regiao;
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
		guardarYurt(y);
	}

	/** Guarda o yurt sem conferir a capacidade; só para exceções como o yurt inicial em Karakorum. */
	void guardarYurt(yurt y) {
		yurts.add(y);
	}

	boolean temYurt() {
		return !yurts.isEmpty();
	}

	boolean temYurtDe(cor dono) {
		for (yurt y : yurts) {
			if (y.getDono() == dono) {
				return true;
			}
		}
		return false;
	}

	void entrarPeao(Peao peao) {
		if (peao == null) {
			throw new IllegalArgumentException("Peão nulo");
		}
		if (!peoes.contains(peao)) {
			peoes.add(peao);
		}
	}

	void sairPeao(Peao peao) {
		peoes.remove(peao);
	}

	/** Indica se o peão pode encerrar o movimento aqui, sem contar ele mesmo na lotação. */
	boolean temVagaPara(Peao peao) {
		int outros = peoes.contains(peao) ? peoes.size() - 1 : peoes.size();
		return outros < capacidadePeoes;
	}

}
