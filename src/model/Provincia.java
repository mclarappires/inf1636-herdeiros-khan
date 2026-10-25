package model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Provincia implements LocalDoKhan {
	
	static final int MAXIMO_TRIBUTOS = 3;
	static final int VIZINHAS_DO_KHAN = 2;
	
	private final int id;
	private final TipoTributo tipo;
	private int quantidade;
	private final boolean doKhan; //províncias em que o peão do Khan pode ser colocado

	private final Map<Integer, Parada> adjacentes = new HashMap<Integer, Parada>();
	private final Map<Integer, Provincia> vizinhas = new HashMap<Integer, Provincia>(); //recebem tributo junto quando o Khan vem para cá

	Provincia(int id, TipoTributo tipo){
		this(id, tipo, false);
	}

	Provincia(int id, TipoTributo tipo, boolean doKhan){
		if(tipo == null) {
			throw new IllegalArgumentException("Província precisa de um tipo de tributo");
		}
		this.id = id;
		this.tipo = tipo;
		this.doKhan = doKhan;
	}

	int getId() {
		return id;
	}

	boolean isDoKhan() {
		return doKhan;
	}
	
	TipoTributo getTipo(){
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
	
	TipoTributo retirarTributo() {
		if(quantidade == 0) {
			throw new IllegalStateException("Província " + id + " está vazia");
		}
		quantidade--;
		return tipo;
	}

	void ligarParada(Parada parada) {
		if (parada == null) {
			throw new IllegalArgumentException("Parada nula");
		}
		adjacentes.put(parada.getId(), parada);
	}
	
	boolean adjacenteA(Parada parada) {
		return parada != null && adjacentes.containsKey(parada.getId());
	}
 
	Collection<Parada> getParadasAdjacentes() {
		return Collections.unmodifiableCollection(adjacentes.values());
	}

	/** Cria a vizinhança nos dois sentidos entre esta província e outra. */
	void ligarProvincia(Provincia outra) {
		if (outra == null || outra == this) {
			throw new IllegalArgumentException("Vizinhança inválida na província " + id);
		}
		this.exigirVagaParaVizinha(outra);
		outra.exigirVagaParaVizinha(this);
		vizinhas.put(outra.id, outra);
		outra.vizinhas.put(this.id, this);
	}

	//uma província do Khan entrega tributos junto com exatamente 2 vizinhas fixas
	private void exigirVagaParaVizinha(Provincia nova) {
		if (doKhan && !vizinhas.containsKey(nova.id) && vizinhas.size() >= VIZINHAS_DO_KHAN) {
			throw new IllegalStateException(
					"Província do Khan " + id + " já tem " + VIZINHAS_DO_KHAN + " vizinhas");
		}
	}

	Collection<Provincia> getProvinciasVizinhas() {
		return Collections.unmodifiableCollection(vizinhas.values());
	}

	/**
	 * O Khan põe 1 tributo nesta província e em cada uma das 2 vizinhas (respeitando o máximo
	 * de cada uma). Quem o moveu não recebe nada: os tributos ficam no mapa.
	 */
	@Override
	public List<TipoTributo> receberKhan() {
		if (!doKhan) {
			throw new IllegalStateException("Província " + id + " não é do Khan");
		}
		adicionarTributo();
		for (Provincia vizinha : vizinhas.values()) {
			vizinha.adicionarTributo();
		}
		return Collections.emptyList();
	}

}
