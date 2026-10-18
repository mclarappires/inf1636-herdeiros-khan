package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

public class FachadaModeloTest {

	private static final int DEFAULT_TIMEOUT = 2000;
	private static final String ARQUIVO_MAPA = "resources/mapa.txt";

	private final FachadaModelo fachada = FachadaModelo.getInstancia();

	@Test(timeout = DEFAULT_TIMEOUT)
	public void getHerois_listaOsCincoHerois() {
		assertEquals(Heroi.values().length, fachada.getHerois().length);
		assertEquals("ALTANI", fachada.getHerois()[0]);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void novaPartida_criaJogadoresNaOrdem() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI", "ALTANI"),
				Arrays.asList("VERDE", "PRETO", "AZUL"));
		assertTrue(fachada.temPartida());
		assertEquals(3, fachada.getQuantidadeJogadores());
		assertEquals("TOLUI", fachada.getHeroiDoJogador(1));
		assertEquals("AZUL", fachada.getCorDoJogador(2));
		assertEquals(1, fachada.getMoedasDoJogador(1));
		assertEquals(2, fachada.getMoedasDoJogador(2));
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void novaPartida_heroiDesconhecido_lancaExcecao() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "GENGIS"), Arrays.asList("VERDE", "PRETO"));
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void novaPartida_corDesconhecida_lancaExcecao() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI"), Arrays.asList("VERDE", "ROXO"));
	}

	@Test(expected = IOException.class, timeout = DEFAULT_TIMEOUT)
	public void novaPartida_mapaInexistente_lancaExcecao() throws IOException {
		fachada.novaPartida("resources/nao-existe.txt", Arrays.asList("JOCHI", "TOLUI"),
				Arrays.asList("VERDE", "PRETO"));
	}
}
