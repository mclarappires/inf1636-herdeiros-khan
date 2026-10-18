package model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/** Único ponto de acesso ao model para view e controller; troca dados só por String e int. */
public class FachadaModelo {

	public static final int MIN_JOGADORES = Partida.MIN_JOGADORES;
	public static final int MAX_JOGADORES = Partida.MAX_JOGADORES;

	private static FachadaModelo instancia = null;

	private Partida partida = null;

	private FachadaModelo() {
	}

	public static FachadaModelo getInstancia() {
		if (instancia == null) {
			instancia = new FachadaModelo();
		}
		return instancia;
	}

	public String[] getHerois() {
		Heroi[] herois = Heroi.values();
		String[] nomes = new String[herois.length];
		for (int i = 0; i < herois.length; i++) {
			nomes[i] = herois[i].name();
		}
		return nomes;
	}

	public String[] getCores() {
		Cor[] cores = Cor.values();
		String[] nomes = new String[cores.length];
		for (int i = 0; i < cores.length; i++) {
			nomes[i] = cores[i].name();
		}
		return nomes;
	}

	/** Herói e cor do jogador i ficam na posição i das listas, na ordem de jogada. */
	public void novaPartida(String caminhoMapa, List<String> herois, List<String> cores) throws IOException {
		if (caminhoMapa == null) {
			throw new IllegalArgumentException("Caminho do mapa nulo");
		}
		List<Heroi> heroisEscolhidos = converterHerois(herois);
		List<Cor> coresEscolhidas = converterCores(cores);

		Reader leitor = new InputStreamReader(new FileInputStream(caminhoMapa), "UTF-8");
		Tabuleiro tabuleiro;
		try {
			tabuleiro = CarregadorMapa.carregar(leitor, new Random());
		} finally {
			leitor.close();
		}
		//só troca a partida atual se a nova for válida
		partida = new Partida(tabuleiro, heroisEscolhidos, coresEscolhidas);
	}

	public boolean temPartida() {
		return partida != null;
	}

	public int getQuantidadeJogadores() {
		return exigirPartida().getJogadores().size();
	}

	public String getHeroiDoJogador(int indice) {
		return exigirPartida().getJogador(indice).getHeroi().name();
	}

	public String getCorDoJogador(int indice) {
		return exigirPartida().getJogador(indice).getCor().name();
	}

	public int getMoedasDoJogador(int indice) {
		return exigirPartida().getJogador(indice).getMoedas();
	}

	private Partida exigirPartida() {
		if (partida == null) {
			throw new IllegalStateException("Nenhuma partida foi iniciada");
		}
		return partida;
	}

	private static List<Heroi> converterHerois(List<String> nomes) {
		if (nomes == null) {
			throw new IllegalArgumentException("Lista de heróis nula");
		}
		List<Heroi> herois = new LinkedList<Heroi>();
		for (String nome : nomes) {
			try {
				herois.add(Heroi.valueOf(nome));
			} catch (RuntimeException e) { //nome nulo ou desconhecido
				throw new IllegalArgumentException("Herói desconhecido: " + nome);
			}
		}
		return herois;
	}

	private static List<Cor> converterCores(List<String> nomes) {
		if (nomes == null) {
			throw new IllegalArgumentException("Lista de cores nula");
		}
		List<Cor> cores = new LinkedList<Cor>();
		for (String nome : nomes) {
			try {
				cores.add(Cor.valueOf(nome));
			} catch (RuntimeException e) {
				throw new IllegalArgumentException("Cor desconhecida: " + nome);
			}
		}
		return cores;
	}
}
