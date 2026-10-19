package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

	private static List<Cor> cores(Cor... c) {
		return Arrays.asList(c);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criar_cincoJogadores_moedasDependemDaOrdem() {
		Partida p = new Partida(tabuleiro,
				herois(Heroi.ALTANI, Heroi.CHAGATAI, Heroi.JOCHI, Heroi.OGEDEI, Heroi.TOLUI),
				cores(Cor.AZUL, Cor.AMARELO, Cor.PRETO, Cor.VERMELHO, Cor.VERDE));
		int[] esperadas = {1, 1, 2, 2, 2};
		assertEquals(5, p.getJogadores().size());
		for (int i = 0; i < esperadas.length; i++) {
			assertEquals(esperadas[i], p.getJogador(i).getMoedas());
		}
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criar_mantemAOrdemDasEscolhas() {
		Partida p = new Partida(tabuleiro, herois(Heroi.OGEDEI, Heroi.JOCHI), cores(Cor.VERDE, Cor.AZUL));
		assertEquals(Heroi.OGEDEI, p.getJogador(0).getHeroi());
		assertEquals(Cor.VERDE, p.getJogador(0).getCor());
		assertEquals(Heroi.JOCHI, p.getJogador(1).getHeroi());
		assertEquals(Cor.AZUL, p.getJogador(1).getCor());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criar_preparaOTabuleiro() {
		assertFalse(tabuleiro.isPreparado());
		new Partida(tabuleiro, herois(Heroi.ALTANI, Heroi.JOCHI), cores(Cor.AZUL, Cor.PRETO));
		assertTrue(tabuleiro.isPreparado());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criar_peoesComecamEmKarakorum() {
		Partida p = new Partida(tabuleiro, herois(Heroi.ALTANI, Heroi.JOCHI), cores(Cor.AZUL, Cor.PRETO));
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
		Partida p = new Partida(tabuleiro, herois(Heroi.ALTANI, Heroi.TOLUI), cores(Cor.AZUL, Cor.VERDE));
		assertTrue(tabuleiro.getParada(ID_KARAKORUM).temYurtDe(Cor.VERDE));
		assertEquals(Jogador.YURTS_INICIAIS - 1, p.getJogador(1).getYurtsDisponiveis());
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void criar_umJogador_lancaExcecao() {
		new Partida(tabuleiro, herois(Heroi.ALTANI), cores(Cor.AZUL));
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void criar_seisJogadores_lancaExcecao() {
		new Partida(tabuleiro,
				herois(Heroi.ALTANI, Heroi.CHAGATAI, Heroi.JOCHI, Heroi.OGEDEI, Heroi.TOLUI, Heroi.ALTANI),
				cores(Cor.AZUL, Cor.AMARELO, Cor.PRETO, Cor.VERMELHO, Cor.VERDE, Cor.AZUL));
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void criar_heroiRepetido_lancaExcecao() {
		new Partida(tabuleiro, herois(Heroi.TOLUI, Heroi.TOLUI), cores(Cor.AZUL, Cor.VERDE));
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void criar_corRepetida_lancaExcecao() {
		new Partida(tabuleiro, herois(Heroi.TOLUI, Heroi.JOCHI), cores(Cor.AZUL, Cor.AZUL));
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void criar_quantidadesDiferentes_lancaExcecao() {
		new Partida(tabuleiro, herois(Heroi.TOLUI, Heroi.JOCHI), cores(Cor.AZUL, Cor.VERDE, Cor.PRETO));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criar_escolhaInvalida_naoPreparaOTabuleiro() {
		try {
			new Partida(tabuleiro, herois(Heroi.TOLUI, Heroi.TOLUI), cores(Cor.AZUL, Cor.VERDE));
		} catch (IllegalArgumentException e) {
			//esperado
		}
		assertFalse(tabuleiro.isPreparado());
	}
}
