package model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class PecaAtivacaoTest {

	private static final int DEFAULT_TIMEOUT = 2000;

	private PecaAtivacao peca;

	@Before
	public void criarPeca() {
		peca = new PecaAtivacao(Coluna.PRIMEIRA);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void usar_pecaLivre_ficaUsada() {
		peca.usar();
		assertTrue("A peça deveria estar na coluna", peca.isUsada());
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void usar_pecaJaUsada_lancaExcecao() {
		peca.usar();
		peca.usar();
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void recuperar_pecaUsada_voltaAFicarLivre() {
		peca.usar();
		peca.recuperar();
		assertFalse("A peça deveria ter voltado para o jogador", peca.isUsada());
	}

}
