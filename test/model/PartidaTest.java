package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class PartidaTest {

	private static final int DEFAULT_TIMEOUT = 2000;
	private static final String ARQUIVO_MAPA = "resources/mapa_herdeiros.txt";
	private static final int ID_KARAKORUM = 1;

	private Tabuleiro tabuleiro;

	@Before
	public void carregarMapa() throws IOException {
		FileReader leitor = new FileReader(ARQUIVO_MAPA);
		try {
			tabuleiro = CarregadorMapa.carregar(leitor, new Random(1L));
		} finally {
			leitor.close();
		}
	}

	private static List<Heroi> herois(Heroi... h) {
		return Arrays.asList(h);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criar_cincoJogadores_moedasDependemDaOrdem() {
		Partida p = new Partida(tabuleiro,
				herois(Heroi.ALTANI, Heroi.CHAGATAI, Heroi.JOCHI, Heroi.OGEDEI, Heroi.TOLUI));
		int[] esperadas = {1, 1, 2, 2, 2};
		assertEquals(5, p.getJogadores().size());
		for (int i = 0; i < esperadas.length; i++) {
			assertEquals(esperadas[i], p.getJogador(i).getMoedas());
		}
	}

	// KURULTAI

	/** Manual: a ficha neutra dispara o fim em 10 / 14 / 16 / 17 votos para 2 / 3 / 4 / 5 jogadores. */
	@Test(timeout = DEFAULT_TIMEOUT)
	public void getVotosParaReunirConselho_dependeDoNumeroDeJogadores() {
		assertEquals(10, new Partida(tabuleiro, herois(Heroi.OGEDEI, Heroi.JOCHI)).getVotosParaReunirConselho());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void getVotosParaReunirConselho_cincoJogadores_dezessete() {
		Partida p = new Partida(tabuleiro,
				herois(Heroi.ALTANI, Heroi.CHAGATAI, Heroi.JOCHI, Heroi.OGEDEI, Heroi.TOLUI));
		assertEquals(17, p.getVotosParaReunirConselho());
	}

	/** A ficha neutra avança a cada voto de qualquer jogador; ao chegar no limite, o conselho está completo. */
	@Test(timeout = DEFAULT_TIMEOUT)
	public void getVotosDoConselho_somaOsVotosDeTodos() {
		Partida p = new Partida(tabuleiro, herois(Heroi.OGEDEI, Heroi.JOCHI));
		assertEquals(0, p.getVotosDoConselho());
		p.getJogador(0).adicionarVotos(6);
		p.getJogador(1).adicionarVotos(3);
		assertEquals(9, p.getVotosDoConselho());
		assertFalse("9 de 10 votos", p.isConselhoCompleto());
		p.getJogador(1).adicionarVotos(1);
		assertTrue("Chegou a 10 com 2 jogadores", p.isConselhoCompleto());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criar_mantemAOrdemDasEscolhas() {
		Partida p = new Partida(tabuleiro, herois(Heroi.OGEDEI, Heroi.JOCHI));
		assertEquals(Heroi.OGEDEI, p.getJogador(0).getHeroi());
		assertEquals(Heroi.JOCHI, p.getJogador(1).getHeroi());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criar_corDoJogadorEhADoHeroi() {
		Partida p = new Partida(tabuleiro,
				herois(Heroi.ALTANI, Heroi.CHAGATAI, Heroi.JOCHI, Heroi.OGEDEI, Heroi.TOLUI));
		Cor[] esperadas = {Cor.VERDE, Cor.VERMELHO, Cor.PRETO, Cor.AZUL, Cor.AMARELO};
		for (int i = 0; i < esperadas.length; i++) {
			assertEquals(esperadas[i], p.getJogador(i).getCor());
		}
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criar_preparaOTabuleiro() {
		assertFalse(tabuleiro.isPreparado());
		new Partida(tabuleiro, herois(Heroi.ALTANI, Heroi.JOCHI));
		assertTrue(tabuleiro.isPreparado());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criar_peoesComecamEmKarakorum() {
		Partida p = new Partida(tabuleiro, herois(Heroi.ALTANI, Heroi.JOCHI));
		Parada karakorum = tabuleiro.getParada(ID_KARAKORUM);
		assertEquals(2, p.getJogador(0).getPeoes().size()); //Altani
		for (Jogador j : p.getJogadores()) {
			for (Peao peao : j.getPeoes()) {
				assertEquals(karakorum, peao.getPosicao());
			}
		}
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criar_tolui_comecaComYurtEmKarakorum() {
		Partida p = new Partida(tabuleiro, herois(Heroi.ALTANI, Heroi.TOLUI));
		assertTrue(tabuleiro.getParada(ID_KARAKORUM).temYurtDe(Cor.AMARELO));
		assertEquals(Jogador.YURTS_INICIAIS - 1, p.getJogador(1).getYurtsDisponiveis());
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void criar_umJogador_lancaExcecao() {
		new Partida(tabuleiro, herois(Heroi.ALTANI));
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void criar_seisJogadores_lancaExcecao() {
		new Partida(tabuleiro,
				herois(Heroi.ALTANI, Heroi.CHAGATAI, Heroi.JOCHI, Heroi.OGEDEI, Heroi.TOLUI, Heroi.ALTANI));
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void criar_heroiRepetido_lancaExcecao() {
		new Partida(tabuleiro, herois(Heroi.TOLUI, Heroi.TOLUI));
	}



	// CARTAS DE CONSELHEIRO

	/** Sorteios são aleatórios: procura uma semente que revele a carta, sobre um mapa novo a cada vez. */
	private Partida partidaComCartaAberta(String id) throws IOException {
		for (long semente = 0; semente < 1000; semente++) {
			carregarMapa();
			Partida p = new Partida(tabuleiro, herois(Heroi.JOCHI, Heroi.TOLUI), new Random(semente));
			for (CartaConselheiroAberto carta : p.getCartasAbertasEmJogo()) {
				if (carta.getId().equals(id)) {
					return p;
				}
			}
		}
		throw new IllegalStateException("Nenhuma semente revelou a carta " + id);
	}

	private static void receberEspadas(Jogador j, int quantidade) {
		for (int i = 0; i < quantidade; i++) {
			j.receberTributo(TipoTributo.ESPADA);
		}
	}

	private static boolean temCartaAberta(Partida p, String id) {
		for (CartaConselheiroAberto carta : p.getCartasAbertasEmJogo()) {
			if (carta.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criar_cincoJogadores_cartasSecretasNaoSeRepetem() {
		Partida p = new Partida(tabuleiro,
				herois(Heroi.ALTANI, Heroi.CHAGATAI, Heroi.JOCHI, Heroi.OGEDEI, Heroi.TOLUI));
		Set<String> ids = new HashSet<String>();
		for (Jogador j : p.getJogadores()) {
			for (CartaConselheiroSecreto carta : p.getOpcoesDeCartaSecreta(j)) {
				assertTrue("Carta repetida: " + carta.getNome(), ids.add(carta.getId()));
			}
		}
		assertEquals("5 jogadores recebem as 10 cartas do baralho", 10, ids.size());
		assertEquals("Só 4 cartas abertas entram em jogo", Partida.CARTAS_ABERTAS, p.getCartasAbertasEmJogo().size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void escolherCartaSecreta_guardaACartaEDescartaAOutra() {
		Partida p = new Partida(tabuleiro, herois(Heroi.JOCHI, Heroi.TOLUI));
		Jogador jochi = p.getJogador(0);
		CartaConselheiroSecreto escolhida = p.getOpcoesDeCartaSecreta(jochi).get(1);

		p.escolherCartaSecreta(jochi, escolhida.getId());

		assertEquals(escolhida, jochi.getCartaSecreta());
		assertTrue("A outra carta foi descartada", p.getOpcoesDeCartaSecreta(jochi).isEmpty());
		assertTrue("Tolui ainda não escolheu", p.isEscolhaDeCartasPendente());
	}

	@Test(expected = IllegalStateException.class, timeout = DEFAULT_TIMEOUT)
	public void escolherCartaSecreta_segundaVez_lancaExcecao() {
		Partida p = new Partida(tabuleiro, herois(Heroi.JOCHI, Heroi.TOLUI));
		Jogador jochi = p.getJogador(0);
		String id = p.getOpcoesDeCartaSecreta(jochi).get(0).getId();
		p.escolherCartaSecreta(jochi, id);
		p.escolherCartaSecreta(jochi, id);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void conferirCartasAbertas_primeiroACumprir_ganhaUmVotoECartaSai() throws IOException {
		Partida p = partidaComCartaAberta("espadas_4");
		receberEspadas(p.getJogador(0), 4);

		p.conferirCartasAbertas();

		assertEquals(1, p.getJogador(0).getVotos());
		assertFalse(temCartaAberta(p, "espadas_4"));
		assertEquals("Nenhuma carta nova é revelada", Partida.CARTAS_ABERTAS - 1, p.getCartasAbertasEmJogo().size());
		assertEquals("As espadas não são gastas", 4, p.getJogador(0).getQuantidade(TipoTributo.ESPADA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void conferirCartasAbertas_segundoACumprir_naoGanhaVoto() throws IOException {
		Partida p = partidaComCartaAberta("espadas_4");
		receberEspadas(p.getJogador(0), 4);
		p.conferirCartasAbertas();
		p.getJogador(0).ativarColuna(Coluna.TERCEIRA); //Jochi: dois cavalos e punho
		p.encerrarTurnoDaVez();
		receberEspadas(p.getJogador(1), 4);

		p.conferirCartasAbertas();

		assertEquals(1, p.getIndiceDaVez());
		assertEquals(0, p.getJogador(1).getVotos());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void conferirCartasAbertas_outroJogadorCumpriu_soContaNoTurnoDele() throws IOException {
		Partida p = partidaComCartaAberta("espadas_4");
		receberEspadas(p.getJogador(1), 4);

		p.conferirCartasAbertas(); //vez do Jochi

		assertEquals(0, p.getJogador(1).getVotos());
		assertTrue(temCartaAberta(p, "espadas_4"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criar_escolhaInvalida_naoPreparaOTabuleiro() {
		try {
			new Partida(tabuleiro, herois(Heroi.TOLUI, Heroi.TOLUI));
		} catch (IllegalArgumentException e) {
			//esperado
		}
		assertFalse(tabuleiro.isPreparado());
	}
}
