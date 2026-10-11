package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Random;

import org.junit.Test;

/**
 * Erros e casos limite do CarregadorMapa: um arquivo mal escrito deve falhar
 * cedo, apontando a linha, em vez de montar um tabuleiro quebrado.
 */
public class CarregadorMapaTest {

	private static final int DEFAULT_TIMEOUT = 2000;

	// IDS E ARGUMENTOS INVÁLIDOS

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void carregar_idDeParadaRepetido_lancaExcecao() throws IOException {
		carregar("PARADA 1\nPARADA 1\n");
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void carregar_idDeCidadeRepetido_lancaExcecao() throws IOException {
		carregar("CIDADE 1 CHINA\nCIDADE 1 RUSSIA\n");
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void carregar_idNaoNumerico_lancaExcecao() throws IOException {
		carregar("PARADA abc\n");
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void carregar_rotaComArgumentoDeMais_lancaExcecao() throws IOException {
		carregar("PARADA 1\nPARADA 2\nROTA 1 2 3\n");
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void carregar_capacidadeSoComPeoesSemYurts_lancaExcecao() throws IOException {
		carregar("PARADA 1 2\n");
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void carregar_provinciaComTipoDeTributoInexistente_lancaExcecao() throws IOException {
		carregar("PROVINCIA 1 OURO\n");
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void carregar_cidadeComRegiaoInexistente_lancaExcecao() throws IOException {
		carregar("CIDADE 1 JAPAO\n");
	}

	// LIGAÇÕES INVÁLIDAS

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void carregar_paradaLigadaASiMesma_lancaExcecao() throws IOException {
		carregar("PARADA 1\nROTA 1 1\n");
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void carregar_cidadeLigadaAParadaInexistente_lancaExcecao() throws IOException {
		carregar("CIDADE 1 CHINA\nCIDADE_PARADA 1 99\n");
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void carregar_provinciaInexistenteLigadaAParada_lancaExcecao() throws IOException {
		carregar("PARADA 1\nPROVINCIA_PARADA 7 1\n");
	}

	// DIAGNÓSTICO

	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_erroNaTerceiraLinha_mensagemCitaALinha3() {
		String mensagem = mensagemDoErro("PARADA 1\nPARADA 2\nCASTELO 3\n");
		assertTrue("A mensagem deveria citar a linha 3: " + mensagem, mensagem.contains("Linha 3"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_comentariosNaoDeslocamONumeroDaLinha() {
		String mensagem = mensagemDoErro("# cabecalho\n\n# outro\nCASTELO 1\n");
		assertTrue("A linha com erro e a 4a: " + mensagem, mensagem.contains("Linha 4"));
	}

	// CASOS LIMITE ACEITOS

	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_comandoEmMinusculas_eAceitoELeARegiao() throws IOException {
		Tabuleiro t = carregar("parada 1 china\n");
		assertEquals("Comando e regiao nao diferenciam maiusculas", Regiao.CHINA, t.getParada(1).getRegiao());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_quebraDeLinhaDoWindows_funcionaIgual() throws IOException {
		Tabuleiro t = carregar("PARADA 1\r\nPARADA 2\r\nROTA 1 2\r\n");
		assertEquals("A rota deveria ser criada apesar do \\r\\n", 1, t.getParada(1).getAdjacentes().size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_capacidadeSemRegiao_guardaAsCapacidades() throws IOException {
		Tabuleiro t = carregar("PARADA 1 3 4\n");
		assertEquals("Capacidade de yurts lida sem regiao", 4, t.getParada(1).getCapacidadeYurts());
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void carregar_arquivoVazio_naoPermitePreparar() throws IOException {
		carregar("").preparar();
	}

	// AUXILIARES

	private Tabuleiro carregar(String texto) throws IOException {
		return CarregadorMapa.carregar(new StringReader(texto), new Random(1L));
	}

	/** Carrega um texto que deve falhar e devolve a mensagem da exceção. */
	private String mensagemDoErro(String texto) {
		try {
			carregar(texto);
		} catch (IllegalArgumentException esperada) {
			return esperada.getMessage();
		} catch (IOException inesperada) {
			throw new IllegalStateException(inesperada);
		}
		throw new IllegalStateException("O texto deveria ter falhado");
	}
}