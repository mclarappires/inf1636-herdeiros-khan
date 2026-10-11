package model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

class Tabuleiro {
	
	static final int CIDADES_VISIVEIS = 3;
	
	private final Random random;
	
	private final Map<Integer, parada> paradas = new HashMap<Integer, parada>();
	private final Map<Integer, Provincia> provincias = new HashMap<Integer, Provincia>();
	private final Map<Integer, Cidade> cidades = new HashMap<Integer, Cidade>();
	
	
	private final LinkedList<Cidade> cidadesNaoReveladas = new LinkedList<Cidade>();
	private final List<Cidade> cidadesVisiveis = new LinkedList<Cidade>();
	private final List<Cidade> cidadesConquistadas = new LinkedList<Cidade>();
	private final LinkedList<TipoTesouro> pilhaTesouros = new LinkedList<TipoTesouro>();
	private final AreaMelhorias areaMelhorias;

	private boolean preparado = false;
	
	Tabuleiro(){
		this(new Random());
	}
	
	Tabuleiro(Random random){
		if (random == null) {
			throw new IllegalArgumentException("Random nulo");
		}
		this.random = random;
		for(TipoTesouro tipo : TipoTesouro.values()) {
			for (int i = 0; i < TipoTesouro.QUANTIDADE_POR_TIPO; i++) {
				pilhaTesouros.add(tipo);
			}
		}
		
		Collections.shuffle(pilhaTesouros, random);
		//criada depois dos tesouros para não mudar o sorteio deles
		areaMelhorias = new AreaMelhorias(random);
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
	
	void adicionarCidade(Cidade cidade) {
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
 
	Cidade getCidade(int id) {
		Cidade c = cidades.get(id);
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
		areaMelhorias.preparar();
		preparado = true;
	}
	
	List<PecaBonus> embaralharBonus() {
		List<PecaBonus> bonus = new LinkedList<PecaBonus>(Arrays.asList(PecaBonus.values()));
		Collections.shuffle(bonus, random);
		return bonus;
	}
	
	boolean revelarCidade() {
		Cidade cidade = cidadesNaoReveladas.poll();
		if (cidade == null) {
			return false;
		}
		for (int i = 0; i < Cidade.TESOUROS_INICIAIS && !pilhaTesouros.isEmpty(); i++) {
			cidade.colocarTesouro(pilhaTesouros.removeFirst());
		}
		cidadesVisiveis.add(cidade);
		return true;
	}
	
	//DURANTE O JOGO 
	
	boolean tomarTesouroDeCidade(int idCidade, TipoTesouro tipo) {
		Cidade cidade = getCidade(idCidade);
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
 
	List<Cidade> getCidadesVisiveis() {
		return Collections.unmodifiableList(cidadesVisiveis);
	}
 
	List<Cidade> getCidadesConquistadas() {
		return Collections.unmodifiableList(cidadesConquistadas);
	}

	Collection<parada> getParadas() {
		return Collections.unmodifiableCollection(paradas.values());
	}

	Collection<Provincia> getProvincias() {
		return Collections.unmodifiableCollection(provincias.values());
	}

	Collection<Cidade> getCidades() {
		return Collections.unmodifiableCollection(cidades.values());
	}
 
	int getQuantidadeCidadesNaoReveladas() {
		return cidadesNaoReveladas.size();
	}
 
	AreaMelhorias getAreaMelhorias() {
		return areaMelhorias;
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
	

