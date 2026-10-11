package model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class MelhoriaTest {

	private static final int DEFAULT_TIMEOUT = 2000;

	private Jogador jogador;

	@Before
	public void criarJogador() {
		jogador = new Jogador(cor.AZUL, Heroi.OGEDEI, new karakorum(0), 0);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void aplicar_espada_daUmaEspadaVirtualAoJogador() {
		new Melhoria(TipoMelhoria.ESPADA, Coluna.PRIMEIRA).aplicar(jogador);
		assertEquals("Jogador deveria poder gastar 1 espada", 1, jogador.getDisponivel(tipoTributo.ESPADA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void aplicar_doisCavalos_somaDoisMovimentos() {
		new Melhoria(TipoMelhoria.DOIS_CAVALOS, Coluna.PRIMEIRA).aplicar(jogador);
		assertEquals("A melhoria vale 2 movimentos", 2, jogador.getMovimentosRestantes());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void novaMelhoria_deTesouro_temTipoTesouro() {
		Melhoria deFerro = new Melhoria(tipoTesouro.FERRO, Coluna.PRIMEIRA);
		assertEquals("Melhoria criada com um tesouro é do tipo TESOURO",
				TipoMelhoria.TESOURO, deFerro.getTipo());
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void novaMelhoria_tipoTesouroSemInformarOTesouro_lancaExcecao() {
		new Melhoria(TipoMelhoria.TESOURO, Coluna.PRIMEIRA);
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void novaMelhoria_comCorDaColunaBranca_lancaExcecao() {
		new Melhoria(TipoMelhoria.PUNHO, Coluna.BRANCA);
	}

}
