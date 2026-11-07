package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

class Partida {

	static final int MIN_JOGADORES = 2;
	static final int MAX_JOGADORES = 5;

	/** Cartas de conselheiro aberto sorteadas em cada partida. */
	static final int CARTAS_ABERTAS = 4;
	/** Cartas secretas entregues a cada jogador, das quais ele guarda uma. */
	static final int CARTAS_SECRETAS_POR_JOGADOR = 2;

	/** Número de jogadores -> votos da ficha neutra que completam o Kurultai e disparam o fim. */
	private static final Map<Integer, Integer> VOTOS_PARA_REUNIR = new HashMap<Integer, Integer>();

	static {
		VOTOS_PARA_REUNIR.put(2, 10);
		VOTOS_PARA_REUNIR.put(3, 14);
		VOTOS_PARA_REUNIR.put(4, 16);
		VOTOS_PARA_REUNIR.put(5, 17);
	}

	private final Tabuleiro tabuleiro;
	private final List<Jogador> jogadores = new LinkedList<Jogador>(); //na ordem de jogada
	private int indiceDaVez = 0;

	private final List<CartaConselheiroAberto> cartasAbertas = new LinkedList<CartaConselheiroAberto>();
	//as 2 cartas secretas de cada jogador até ele escolher uma; depois a lista fica vazia
	private final Map<Jogador, List<CartaConselheiroSecreto>> opcoesSecretas =
			new HashMap<Jogador, List<CartaConselheiroSecreto>>();

	/** Cada jogador joga com a cor do seu herói. */
	Partida(Tabuleiro tabuleiro, List<Heroi> herois) {
		this(tabuleiro, herois, new Random());
	}

	/** O random sorteia as cartas de conselheiro (abertas e secretas). */
	Partida(Tabuleiro tabuleiro, List<Heroi> herois, Random random) {
		if (tabuleiro == null) {
			throw new IllegalArgumentException("Tabuleiro nulo");
		}
		if (random == null) {
			throw new IllegalArgumentException("Random nulo");
		}
		exigirEscolhasValidas(herois);
		this.tabuleiro = tabuleiro;
		tabuleiro.preparar();

		Karakorum inicial = acharKarakorum(tabuleiro);
		for (int i = 0; i < herois.size(); i++) {
			Heroi heroi = herois.get(i);
			jogadores.add(new Jogador(heroi.getCor(), heroi, inicial, moedasIniciais(i)));
		}
		sortearCartas(random);
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

	//CARTAS DE CONSELHEIRO

	/** Revela 4 cartas abertas e entrega 2 cartas secretas a cada jogador (o baralho secreto tem 10). */
	private void sortearCartas(Random random) {
		List<CartaConselheiroAberto> abertas = CartaConselheiroAberto.criarBaralho();
		Collections.shuffle(abertas, random);
		cartasAbertas.addAll(abertas.subList(0, CARTAS_ABERTAS));

		List<CartaConselheiroSecreto> secretas = CartaConselheiroSecreto.criarBaralho();
		Collections.shuffle(secretas, random);
		int proxima = 0;
		for (Jogador j : jogadores) {
			opcoesSecretas.put(j, new LinkedList<CartaConselheiroSecreto>(
					secretas.subList(proxima, proxima + CARTAS_SECRETAS_POR_JOGADOR)));
			proxima += CARTAS_SECRETAS_POR_JOGADOR;
		}
	}

	/** O jogador guarda a carta secreta de id dado (a outra é descartada). Só vale uma vez por jogador. */
	void escolherCartaSecreta(Jogador jogador, String id) {
		List<CartaConselheiroSecreto> opcoes = opcoesSecretas.get(jogador);
		if (opcoes == null) {
			throw new IllegalArgumentException("Jogador não pertence a esta partida");
		}
		if (opcoes.isEmpty()) {
			throw new IllegalStateException("O jogador já escolheu a carta secreta");
		}
		for (CartaConselheiroSecreto carta : opcoes) {
			if (carta.getId().equals(id)) {
				jogador.definirCartaSecreta(carta);
				opcoes.clear();
				return;
			}
		}
		throw new IllegalArgumentException("A carta " + id + " não está entre as opções do jogador");
	}

	/** As cartas secretas entre as quais o jogador ainda precisa escolher (vazio depois de escolher). */
	List<CartaConselheiroSecreto> getOpcoesDeCartaSecreta(Jogador jogador) {
		List<CartaConselheiroSecreto> opcoes = opcoesSecretas.get(jogador);
		if (opcoes == null) {
			throw new IllegalArgumentException("Jogador não pertence a esta partida");
		}
		return Collections.unmodifiableList(opcoes);
	}

	/** Verdadeiro enquanto algum jogador ainda não escolheu a sua carta secreta. */
	boolean isEscolhaDeCartasPendente() {
		for (List<CartaConselheiroSecreto> opcoes : opcoesSecretas.values()) {
			if (!opcoes.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Confere se o jogador da vez cumpriu alguma carta aberta que ainda esteja em jogo; se
	 * cumpriu, ele ganha o voto e a carta sai. Deve rodar depois de cada ação do jogador.
	 */
	void conferirCartasAbertas() {
		Jogador jogador = getJogadorDaVez();
		for (CartaConselheiroAberto carta : cartasAbertas) {
			if (!carta.isReivindicada() && carta.cumprida(jogador, tabuleiro)) {
				carta.reivindicar(jogador, tabuleiro);
			}
		}
	}

	/** Cartas abertas que ainda não foram cumpridas por ninguém. */
	List<CartaConselheiroAberto> getCartasAbertasEmJogo() {
		List<CartaConselheiroAberto> emJogo = new LinkedList<CartaConselheiroAberto>();
		for (CartaConselheiroAberto carta : cartasAbertas) {
			if (!carta.isReivindicada()) {
				emJogo.add(carta);
			}
		}
		return emJogo;
	}

	//VEZ DE JOGAR

	/**
	 * Encerra o turno do jogador da vez e passa a vez ao próximo (sentido horário).
	 * Se o jogador não puder encerrar (por exemplo, peão em parada cheia), a exceção
	 * sobe e a vez continua com ele.
	 */
	void encerrarTurnoDaVez() {
		conferirCartasAbertas();
		getJogadorDaVez().encerrarTurno();
		indiceDaVez = (indiceDaVez + 1) % jogadores.size();
	}

	//KURULTAI

	/**
	 * Posição da ficha neutra na trilha do Kurultai: ela avança a cada voto que qualquer jogador
	 * ganha durante o jogo, então é a soma dos votos de todos (os votos de influência e das
	 * cartas secretas só são contados no fim e não entram aqui).
	 */
	int getVotosDoConselho() {
		int total = 0;
		for (Jogador j : jogadores) {
			total += j.getVotos();
		}
		return total;
	}

	/** Votos da ficha neutra que completam o conselho: 10, 14, 16 ou 17 para 2, 3, 4 ou 5 jogadores. */
	int getVotosParaReunirConselho() {
		return VOTOS_PARA_REUNIR.get(jogadores.size());
	}

	/** Verdadeiro quando a ficha neutra chegou ao limite: o fim da partida foi disparado. */
	boolean isConselhoCompleto() {
		return getVotosDoConselho() >= getVotosParaReunirConselho();
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