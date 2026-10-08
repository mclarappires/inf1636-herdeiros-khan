package model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

class tabuleiro {
	
	static final int CIDADES_VISIVEIS = 3;
	
	private final Random random;
	
	private final Map<Integer, parada> paradas = new HashMap<Integer, parada>();
	private final Map<Integer, Provincia> provincias = new HashMap<Integer, Provincia>();
	private final Map<Integer, cidade> cidades = new HashMap<Integer, cidade>();
	
	
	private final LinkedList<cidade> cidadesNaoReveladas = new LinkedList<cidade>();
	private final List<cidade> cidadesVisiveis = new LinkedList<cidade>();
	private final List<cidade> cidadesConquistadas = new LinkedList<cidade>();
	private final LinkedList<tipoTesouro> pilhaTesouros = new LinkedList<tipoTesouro>();
	
	private boolean preparado = false;
	
	tabuleiro(){
		this(new Random());
	}
	
	tabuleiro(Random random){
		if (random == null) {
			throw new IllegalArgumentException("Random nulo");
		}
		this.random = random;
		for(tipoTesouro tipo : tipoTesouro.values()) {
			for (int i = 0; i < tipoTesouro.QUANTIDADE_POR_TIPO; i++) {
				pilhaTesouros.add(tipo);
			}
		}
		
		Collections.shuffle(pilhaTesouros, random);
	}
	
	void adicionarParada(parada parada) {
		exigirNaoPreparado();
		if (parada == null) {
			throw new IllegalArgumentException("Parada nula");
		}
		if (paradas.containsKey(parada.getId())) {
			throw new IllegalArgumentException("Já existe parada com id " + parada.getId());
		}
		paradas.put(parada.getId(), parada);
	}
	
	void adicionarProvincia(Provincia provincia) {
		exigirNaoPreparado();
		if (provincia == null) {
			throw new IllegalArgumentException("Província nula");
		}
		if (provincias.containsKey(provincia.getId())) {
			throw new IllegalArgumentException("Já existe província com id " + provincia.getId());
		}
		provincias.put(provincia.getId(), provincia);
	}
	
	void adicionarCidade(cidade cidade) {
		exigirNaoPreparado();
		if (cidade == null) {
			throw new IllegalArgumentException("Cidade nula");
		}
		if (cidades.containsKey(cidade.getId())) {
			throw new IllegalArgumentException("Já existe cidade com id " + cidade.getId());
		}
		cidades.put(cidade.getId(), cidade);
		cidadesNaoReveladas.add(cidade);
	}
	
	void ligarParadas(int idA, int idB) {
		getParada(idA).ligar(getParada(idB));
	}
 
	void ligarProvinciaParada(int idProvincia, int idParada) {
		getProvincia(idProvincia).ligarParada(getParada(idParada));
	}
 
	void ligarCidadeParada(int idCidade, int idParada) {
		getCidade(idCidade).ligarParada(getParada(idParada));
	}
	
	parada getParada(int id) {
		parada p = paradas.get(id);
		if (p == null) {
			throw new IllegalArgumentException("Parada inexistente: " + id);
		}
		return p;
	}
 
	Provincia getProvincia(int id) {
		Provincia p = provincias.get(id);
		if (p == null) {
			throw new IllegalArgumentException("Província inexistente: " + id);
		}
		return p;
	}
 
	cidade getCidade(int id) {
		cidade c = cidades.get(id);
		if (c == null) {
			throw new IllegalArgumentException("Cidade inexistente: " + id);
		}
		return c;
	}
	
	void preparar() {
		exigirNaoPreparado();
		if (cidades.size() < CIDADES_VISIVEIS) {
			throw new IllegalStateException(
					"São necessárias pelo menos " + CIDADES_VISIVEIS + " cidades");
		}
		Collections.shuffle(cidadesNaoReveladas, random);
		for (int i = 0; i < CIDADES_VISIVEIS; i++) {
			revelarCidade();
		}
		for (Provincia p : provincias.values()) {
			p.adicionarTributo();
		}
		preparado = true;
	}
	
	List<pecaBonus> embaralharBonus() {
		List<pecaBonus> bonus = new LinkedList<pecaBonus>(Arrays.asList(pecaBonus.values()));
		Collections.shuffle(bonus, random);
		return bonus;
	}
	
	boolean revelarCidade() {
		cidade cidade = cidadesNaoReveladas.poll();
		if (cidade == null) {
			return false;
		}
		for (int i = 0; i < cidade.TESOUROS_INICIAIS && !pilhaTesouros.isEmpty(); i++) {
			cidade.colocarTesouro(pilhaTesouros.removeFirst());
		}
		cidadesVisiveis.add(cidade);
		return true;
	}
	
	//DURANTE O JOGO 
	
	boolean tomarTesouroDeCidade(int idCidade, tipoTesouro tipo) {
		cidade cidade = getCidade(idCidade);
		if (!cidadesVisiveis.contains(cidade)) {
			throw new IllegalStateException("Cidade " + idCidade + " não está disponível para ataque");
		}
		boolean conquistou = cidade.tomarTesouro(tipo);
		if (conquistou) {
			cidadesVisiveis.remove(cidade);
			cidadesConquistadas.add(cidade);
			revelarCidade();
		}
		return conquistou;
	}
	
	boolean todasCidadesConquistadas() {
		return preparado && cidadesNaoReveladas.isEmpty() && cidadesVisiveis.isEmpty();
	}
	
	//CONSULTAS
	
	boolean isPreparado() {
		return preparado;
	}
 
	List<cidade> getCidadesVisiveis() {
		return Collections.unmodifiableList(cidadesVisiveis);
	}
 
	List<cidade> getCidadesConquistadas() {
		return Collections.unmodifiableList(cidadesConquistadas);
	}
 
	int getQuantidadeCidadesNaoReveladas() {
		return cidadesNaoReveladas.size();
	}
 
	int getTesourosNaReserva() {
		return pilhaTesouros.size();
	}
 
	private void exigirNaoPreparado() {
		if (preparado) {
			throw new IllegalStateException("Tabuleiro já foi preparado");
		}
	}
}
	

