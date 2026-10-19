package model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
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

	public String[] getColunas() {
		Coluna[] colunas = Coluna.values();
		String[] nomes = new String[colunas.length];
		for (int i = 0; i < colunas.length; i++) {
			nomes[i] = colunas[i].name();
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

	//PEÕES

	public int getQuantidadePeoes(int jogador) {
		return exigirPartida().getJogador(jogador).getPeoes().size();
	}

	/** Id da parada onde está o peão (Altani tem dois; os demais, um). */
	public int getParadaDoPeao(int jogador, int indicePeao) {
		List<Peao> peoes = exigirPartida().getJogador(jogador).getPeoes();
		if (indicePeao < 0 || indicePeao >= peoes.size()) {
			throw new IllegalArgumentException("Peão inexistente: " + indicePeao);
		}
		return peoes.get(indicePeao).getPosicao().getId();
	}

	//PROVÍNCIAS E PEÇAS DE TRIBUTO

	public int[] getIdsProvincias() {
		Collection<Provincia> provincias = exigirPartida().getTabuleiro().getProvincias();
		int[] ids = new int[provincias.size()];
		int i = 0;
		for (Provincia p : provincias) {
			ids[i++] = p.getId();
		}
		return ids;
	}

	/** "ESPADA", "YURT" ou "MOEDA": o tipo de peça que a província produz. */
	public String getTipoDaProvincia(int idProvincia) {
		return exigirPartida().getTabuleiro().getProvincia(idProvincia).getTipo().name();
	}

	public int getTributosDaProvincia(int idProvincia) {
		return exigirPartida().getTabuleiro().getProvincia(idProvincia).getQuantidade();
	}

	//CIDADES

	/** Ids das cidades reveladas, isto é, as que podem ser atacadas agora. */
	public int[] getIdsCidadesVisiveis() {
		List<Cidade> visiveis = exigirPartida().getTabuleiro().getCidadesVisiveis();
		int[] ids = new int[visiveis.size()];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = visiveis.get(i).getId();
		}
		return ids;
	}

	//TURNO

	/** Índice (na ordem de jogada) do jogador que está jogando. */
	public int getJogadorDaVez() {
		return exigirPartida().getIndiceDaVez();
	}

	/** Nome da coluna ativada neste turno, ou null se o jogador ainda não ativou nenhuma. */
	public String getColunaAtiva() {
		Coluna ativa = exigirPartida().getJogadorDaVez().getColunaAtiva();
		return ativa == null ? null : ativa.name();
	}

	public int getMovimentosRestantes() {
		return exigirPartida().getJogadorDaVez().getMovimentosRestantes();
	}

	public boolean isKhanPendente() {
		return exigirPartida().getJogadorDaVez().isKhanPendente();
	}

	/** Ativa a coluna para o jogador da vez. Devolve false se não for possível (coluna já usada, turno em andamento...). */
	public boolean ativarColuna(String coluna, int indicePeao) {
		try {
			exigirPartida().getJogadorDaVez().ativarColuna(Coluna.valueOf(coluna), indicePeao);
			return true;
		} catch (IllegalStateException e) {
			return false;
		} catch (IllegalArgumentException e) { //nome de coluna desconhecido ou peão inexistente
			return false;
		}
	}

	/** Move o peão que age no turno para a parada, se o movimento valer. Falso não altera nada. */
	public boolean moverPeaoDaVez(int idParada) {
		Partida p = exigirPartida();
		try {
			Parada destino = p.getTabuleiro().getParada(idParada);
			Jogador jogador = p.getJogadorDaVez();
			if (!jogador.podeMoverPara(destino)) {
				return false;
			}
			jogador.mover(destino);
			return true;
		} catch (IllegalStateException e) {
			return false;
		} catch (IllegalArgumentException e) { //parada inexistente
			return false;
		}
	}

	/** Encerra o turno e passa a vez. Falso se o turno ainda não pode terminar. */
	public boolean encerrarTurno() {
		try {
			exigirPartida().encerrarTurnoDaVez();
			return true;
		} catch (IllegalStateException e) {
			return false;
		}
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