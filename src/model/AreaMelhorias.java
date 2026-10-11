package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Melhorias à venda: 5 espaços abertos, numerados da esquerda (0) para a direita (4),
 * e uma pilha com as demais peças.
 */
class AreaMelhorias implements LocalDoKhan {

	static final int ESPACOS = 5;

	private static final Coluna[] CORES = { Coluna.PRIMEIRA, Coluna.SEGUNDA, Coluna.TERCEIRA };

	private final LinkedList<Melhoria> pilha;
	private final Map<Integer, Melhoria> espacos = new HashMap<Integer, Melhoria>(); //espaço vazio não tem chave
	private final Map<Integer, TipoTributo> icones; //tributo ganho ao reabastecer um espaço vazio

	AreaMelhorias(Random random) {
		this(embaralhada(random), iconesPadrao());
	}

	//usa a pilha na ordem dada, sem embaralhar
	AreaMelhorias(List<Melhoria> pilha, Map<Integer, TipoTributo> icones) {
		if (pilha == null || icones == null) {
			throw new IllegalArgumentException("Pilha e ícones são obrigatórios");
		}
		for (Melhoria m : pilha) {
			if (m == null) {
				throw new IllegalArgumentException("Pilha com melhoria nula");
			}
		}
		for (Map.Entry<Integer, TipoTributo> e : icones.entrySet()) {
			exigirEspacoValido(e.getKey());
			if (e.getValue() != TipoTributo.MOEDA && e.getValue() != TipoTributo.ESPADA) {
				throw new IllegalArgumentException("Ícone de espaço deve ser moeda ou espada: " + e);
			}
		}
		this.pilha = new LinkedList<Melhoria>(pilha);
		this.icones = new HashMap<Integer, TipoTributo>(icones);
	}

	private static List<Melhoria> embaralhada(Random random) {
		if (random == null) {
			throw new IllegalArgumentException("Random nulo");
		}
		List<Melhoria> pilha = criarPilha();
		Collections.shuffle(pilha, random);
		return pilha;
	}

	//padrão provisório, até ser conferido no tabuleiro físico
	private static Map<Integer, TipoTributo> iconesPadrao() {
		Map<Integer, TipoTributo> padrao = new HashMap<Integer, TipoTributo>();
		padrao.put(1, TipoTributo.MOEDA);
		padrao.put(3, TipoTributo.ESPADA);
		return padrao;
	}

	/** As 44 melhorias do jogo; cada tipo é repartido em rodízio entre as 3 cores. */
	static List<Melhoria> criarPilha() {
		List<Melhoria> todas = new LinkedList<Melhoria>();
		for (TipoMelhoria tipo : TipoMelhoria.values()) {
			if (tipo == TipoMelhoria.TESOURO) {
				continue;
			}
			for (int i = 0; i < tipo.getQuantidadeNoJogo(); i++) {
				todas.add(new Melhoria(tipo, CORES[i % CORES.length]));
			}
		}
		TipoTesouro[] tesouros = TipoTesouro.values();
		for (int i = 0; i < tesouros.length; i++) {
			todas.add(new Melhoria(tesouros[i], CORES[i % CORES.length]));
		}
		return todas;
	}

	/** Revela os espaços iniciais; na preparação ninguém ganha os tributos dos ícones. */
	void preparar() {
		if (!espacos.isEmpty()) {
			throw new IllegalStateException("Área de melhorias já foi preparada");
		}
		reabastecer();
	}

	/** Tira a peça do espaço; as peças à esquerda deslizam uma casa para a direita. */
	Melhoria retirar(int espaco) {
		exigirEspacoValido(espaco);
		Melhoria retirada = espacos.remove(espaco);
		if (retirada == null) {
			throw new IllegalStateException("Espaço " + espaco + " da área de melhorias está vazio");
		}
		for (int i = espaco; i > 0; i--) {
			Melhoria daEsquerda = espacos.remove(i - 1);
			if (daEsquerda != null) {
				espacos.put(i, daEsquerda);
			}
		}
		return retirada;
	}

	Melhoria verTopo() {
		if (pilha.isEmpty()) {
			throw new IllegalStateException("Pilha de melhorias vazia");
		}
		return pilha.getFirst();
	}

	Melhoria retirarDoTopo() {
		verTopo();
		return pilha.removeFirst();
	}

	/**
	 * Uso do Khan na área: revela peças da pilha nos espaços vazios. Devolve os tributos dos
	 * ícones dos espaços que estavam vazios, que vão para quem usou o Khan.
	 */
	List<TipoTributo> reabastecer() {
		List<TipoTributo> ganhos = new LinkedList<TipoTributo>();
		for (int i = 0; i < ESPACOS; i++) {
			if (espacos.containsKey(i)) {
				continue;
			}
			TipoTributo icone = icones.get(i);
			if (icone != null) {
				ganhos.add(icone);
			}
			if (!pilha.isEmpty()) {
				espacos.put(i, pilha.removeFirst());
			}
		}
		return ganhos;
	}

	@Override
	public List<TipoTributo> receberKhan() {
		return reabastecer();
	}

	//CONSULTAS

	/** Melhoria no espaço, ou null se ele estiver vazio. */
	Melhoria getMelhoria(int espaco) {
		exigirEspacoValido(espaco);
		return espacos.get(espaco);
	}

	TipoTributo getIcone(int espaco) {
		exigirEspacoValido(espaco);
		return icones.get(espaco);
	}

	int getTamanhoPilha() {
		return pilha.size();
	}

	private static void exigirEspacoValido(Integer espaco) {
		if (espaco == null || espaco < 0 || espaco >= ESPACOS) {
			throw new IllegalArgumentException("Espaço inexistente na área de melhorias: " + espaco);
		}
	}

}
