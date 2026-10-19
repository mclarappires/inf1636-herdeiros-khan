package model;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class Partida {

	static final int MIN_JOGADORES = 2;
	static final int MAX_JOGADORES = 5;

	private final Tabuleiro tabuleiro;
	private final List<Jogador> jogadores = new LinkedList<Jogador>(); //na ordem de jogada
	private int indiceDaVez = 0;

	Partida(Tabuleiro tabuleiro, List<Heroi> herois, List<Cor> cores) {
		if (tabuleiro == null) {
			throw new IllegalArgumentException("Tabuleiro nulo");
		}
		exigirEscolhasValidas(herois, cores);
		this.tabuleiro = tabuleiro;
		tabuleiro.preparar();

		Karakorum inicial = acharKarakorum(tabuleiro);
		for (int i = 0; i < herois.size(); i++) {
			jogadores.add(new Jogador(cores.get(i), herois.get(i), inicial, moedasIniciais(i)));
		}
	}

	/** O primeiro e o segundo jogadores começam com 1 moeda; os demais com 2. */
	static int moedasIniciais(int posicao) {
		if (posicao < 0) {
			throw new IllegalArgumentException("Posição negativa: " + posicao);
		}
		return posicao < 2 ? 1 : 2;
	}

	private static void exigirEscolhasValidas(List<Heroi> herois, List<Cor> cores) {
		if (herois == null || cores == null) {
			throw new IllegalArgumentException("Escolhas de heróis e cores são obrigatórias");
		}
		if (herois.size() != cores.size()) {
			throw new IllegalArgumentException("Cada jogador precisa de um herói e uma cor");
		}
		if (herois.size() < MIN_JOGADORES || herois.size() > MAX_JOGADORES) {
			throw new IllegalArgumentException("A partida precisa de " + MIN_JOGADORES
					+ " a " + MAX_JOGADORES + " jogadores");
		}
		Set<Heroi> heroisVistos = new HashSet<Heroi>();
		Set<Cor> coresVistas = new HashSet<Cor>();
		for (int i = 0; i < herois.size(); i++) {
			if (herois.get(i) == null || cores.get(i) == null) {
				throw new IllegalArgumentException("Jogador " + (i + 1) + " sem herói ou cor");
			}
			if (!heroisVistos.add(herois.get(i))) {
				throw new IllegalArgumentException("Herói " + herois.get(i) + " escolhido mais de uma vez");
			}
			if (!coresVistas.add(cores.get(i))) {
				throw new IllegalArgumentException("Cor " + cores.get(i) + " escolhida mais de uma vez");
			}
		}
	}

	private static Karakorum acharKarakorum(Tabuleiro tabuleiro) {
		for (Parada p : tabuleiro.getParadas()) {
			if (p instanceof Karakorum) {
				return (Karakorum) p;
			}
		}
		throw new IllegalStateException("O mapa não tem Karakorum");
	}

	//VEZ DE JOGAR

	/**
	 * Encerra o turno do jogador da vez e passa a vez ao próximo (sentido horário).
	 * Se o jogador não puder encerrar (por exemplo, peão em parada cheia), a exceção
	 * sobe e a vez continua com ele.
	 */
	void encerrarTurnoDaVez() {
		getJogadorDaVez().encerrarTurno();
		indiceDaVez = (indiceDaVez + 1) % jogadores.size();
	}

	//CONSULTAS

	Tabuleiro getTabuleiro() {
		return tabuleiro;
	}

	List<Jogador> getJogadores() {
		return Collections.unmodifiableList(jogadores);
	}

	Jogador getJogador(int indice) {
		if (indice < 0 || indice >= jogadores.size()) {
			throw new IllegalArgumentException("Jogador inexistente: " + indice);
		}
		return jogadores.get(indice);
	}

	int getIndiceDaVez() {
		return indiceDaVez;
	}

	Jogador getJogadorDaVez() {
		return jogadores.get(indiceDaVez);
	}
}