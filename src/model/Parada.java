package model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


class Parada {
	
	private final int id;
	private final int capacidadePeoes;
	private final int capacidadeYurts;
	
	private final Regiao regiao;
	
	private final Map<Integer, Parada> adjacentes = new HashMap<Integer, Parada>();
	
	private final List<Yurt> yurts = new LinkedList<Yurt>();

	private final List<Peao> peoes = new LinkedList<Peao>(); //peões que estão na parada

	Parada(int id, int capacidadePeoes, int capacidadeYurts, Regiao regiao){
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
	
	Regiao getRegiao() {
		return regiao;
	}
	
	/** Cria a rota nos dois sentidos entre esta parada e outra. */
	void ligar(Parada outra) {
		if (outra == null || outra == this) {
			throw new IllegalArgumentException("Ligação inválida na parada " + id);
		}
		adjacentes.put(outra.id, outra);
		outra.adjacentes.put(this.id, this);
	}
	
	Collection<Parada> getAdjacentes() {
		return Collections.unmodifiableCollection(adjacentes.values());
	}
	
	/** Paradas duplas aceitam 2 yurts; Karakorum não aceita nenhum e não é dupla. */
	boolean isDupla() {
		return capacidadeYurts == 2;
	}

	boolean podeConstruirYurt() {
		return yurts.size() < capacidadeYurts;
	}
 
	void adicionarYurt(Yurt y) {
		if (y == null) {
			throw new IllegalArgumentException("Yurt nulo");
		}
		if (!podeConstruirYurt()) {
			throw new IllegalStateException("Parada " + id + " não aceita mais yurts");
		}
		guardarYurt(y);
	}

	/** Guarda o yurt sem conferir a capacidade; só para exceções como o yurt inicial em Karakorum. */
	void guardarYurt(Yurt y) {
		yurts.add(y);
	}

	boolean temYurt() {
		return !yurts.isEmpty();
	}

	boolean temYurtDe(Cor dono) {
		for (Yurt y : yurts) {
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
	
	List<Yurt> getYurts() {
	    return Collections.unmodifiableList(yurts);
	}

}
