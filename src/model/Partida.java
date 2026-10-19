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

	/** Cada jogador joga com a cor do seu herói. */
	Partida(Tabuleiro tabuleiro, List<Heroi> herois) {
		if (tabuleiro == null) {
			throw new IllegalArgumentException("Tabuleiro nulo");
		}
		exigirEscolhasValidas(herois);
		this.tabuleiro = tabuleiro;
		tabuleiro.preparar();

		Karakorum inicial = acharKarakorum(tabuleiro);
		for (int i = 0; i < herois.size(); i++) {
			Heroi heroi = herois.get(i);
			jogadores.add(new Jogador(heroi.getCor(), heroi, inicial, moedasIniciais(i)));
		}
	}

	/** O primeiro e o segundo jogadores começam com 1 moeda; os demais com 2. */
	static int moedasIniciais(int posicao) {
		if (posicao < 0) {
			throw new IllegalArgumentException("Posição negativa: " + posicao);
		}
		return posicao < 2 ? 1 : 2;
	}

	//herói sem repetição já garante que as cores também não se repetem
	private static void exigirEscolhasValidas(List<Heroi> herois) {
		if (herois == null) {
			throw new IllegalArgumentException("Escolha dos heróis é obrigatória");
		}
		if (herois.size() < MIN_JOGADORES || herois.size() > MAX_JOGADORES) {
			throw new IllegalArgumentException("A partida precisa de " + MIN_JOGADORES
					+ " a " + MAX_JOGADORES + " jogadores");
		}
		Set<Heroi> heroisVistos = new HashSet<Heroi>();
		for (int i = 0; i < herois.size(); i++) {
			if (herois.get(i) == null) {
				throw new IllegalArgumentException("Jogador " + (i + 1) + " sem herói");
			}
			if (!heroisVistos.add(herois.get(i))) {
				throw new IllegalArgumentException("Herói " + herois.get(i) + " escolhido mais de uma vez");
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