package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

public class FachadaModeloTest {

	private static final int DEFAULT_TIMEOUT = 2000;
	private static final String ARQUIVO_MAPA = "resources/mapa_herdeiros.txt";

	private final FachadaModelo fachada = FachadaModelo.getInstancia();

	@Test(timeout = DEFAULT_TIMEOUT)
	public void getHerois_listaOsCincoHerois() {
		assertEquals(Heroi.values().length, fachada.getHerois().length);
		assertEquals("ALTANI", fachada.getHerois()[0]);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void getCorDoHeroi_devolveACorFixa() {
		assertEquals("VERDE", fachada.getCorDoHeroi("ALTANI"));
		assertEquals("VERMELHO", fachada.getCorDoHeroi("CHAGATAI"));
		assertEquals("PRETO", fachada.getCorDoHeroi("JOCHI"));
		assertEquals("AZUL", fachada.getCorDoHeroi("OGEDEI"));
		assertEquals("AMARELO", fachada.getCorDoHeroi("TOLUI"));
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void getCorDoHeroi_heroiDesconhecido_lancaExcecao() {
		fachada.getCorDoHeroi("GENGIS");
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void novaPartida_criaJogadoresNaOrdem() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI", "ALTANI"));
		assertTrue(fachada.temPartida());
		assertEquals(3, fachada.getQuantidadeJogadores());
		assertEquals("TOLUI", fachada.getHeroiDoJogador(1));
		assertEquals(1, fachada.getMoedasDoJogador(1));
		assertEquals(2, fachada.getMoedasDoJogador(2));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void novaPartida_corDoJogadorVemDoHeroi() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI", "ALTANI"));
		assertEquals("PRETO", fachada.getCorDoJogador(0));
		assertEquals("AMARELO", fachada.getCorDoJogador(1));
		assertEquals("VERDE", fachada.getCorDoJogador(2));
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void novaPartida_heroiDesconhecido_lancaExcecao() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "GENGIS"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void isColunaUsada_ficaVerdadeiroDepoisDeAtivar() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI"));
		assertFalse(fachada.isColunaUsada(0, "SEGUNDA"));
		assertTrue(fachada.ativarColuna("SEGUNDA", 0));
		assertTrue(fachada.isColunaUsada(0, "SEGUNDA"));
		assertFalse(fachada.isColunaUsada(0, "PRIMEIRA"));
		assertFalse(fachada.isColunaUsada(1, "SEGUNDA"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void getMelhorias_comecamVazias() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI"));
		for (String coluna : fachada.getColunas()) {
			assertEquals(0, fachada.getSimbolosDasMelhorias(0, coluna).length);
			assertEquals(0, fachada.getCoresDasMelhorias(0, coluna).length);
		}
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void getCapacidadeDaColuna_brancaDoJochiTemUmEspacoAMenos() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI"));
		int linhas = FachadaModelo.LINHAS_TABULEIRO_JOGADOR;
		assertEquals(linhas - 1, fachada.getCapacidadeDaColuna(0, "BRANCA"));
		assertEquals(linhas, fachada.getCapacidadeDaColuna(0, "PRIMEIRA"));
		assertEquals(linhas, fachada.getCapacidadeDaColuna(1, "BRANCA"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ativarColuna_altaniPodeEscolherOSegundoPeao() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("ALTANI", "TOLUI"));
		assertTrue(fachada.ativarColuna("PRIMEIRA", 1));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ativarColuna_peaoInexistente_devolveFalso() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI"));
		assertFalse(fachada.ativarColuna("PRIMEIRA", 1));
		assertFalse(fachada.isColunaUsada(0, "PRIMEIRA"));
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void isColunaUsada_colunaDesconhecida_lancaExcecao() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI"));
		fachada.isColunaUsada(0, "ROXA");
	}

	@Test(expected = IOException.class, timeout = DEFAULT_TIMEOUT)
	public void novaPartida_mapaInexistente_lancaExcecao() throws IOException {
		fachada.novaPartida("resources/nao-existe.txt", Arrays.asList("JOCHI", "TOLUI"));
	}
}
