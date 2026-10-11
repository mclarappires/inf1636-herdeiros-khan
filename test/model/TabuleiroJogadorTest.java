package model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TabuleiroJogadorTest {

	private static final int DEFAULT_TIMEOUT = 2000;

	private TabuleiroJogador doOgedei;

	@Before
	public void criarTabuleiro() {
		doOgedei = new TabuleiroJogador(Heroi.OGEDEI);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void colocarMelhoria_completaLinhaDeTres_geraMelhoriaGratuita() {
		completarLinha(doOgedei);
		assertEquals("Linha completa rende 1 melhoria para a coluna branca",
				1, doOgedei.getMelhoriasGratuitasPendentes());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void colocarMelhoria_jochiCompletaPrimeiraLinha_naoGeraMelhoriaGratuita() {
		TabuleiroJogador doJochi = new TabuleiroJogador(Heroi.JOCHI);
		completarLinha(doJochi);
		assertEquals("O 1º espaço branco do Jochi já vem ocupado",
				0, doJochi.getMelhoriasGratuitasPendentes());
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void colocarMelhoria_colunaDeOutraCor_lancaExcecao() {
		doOgedei.colocarMelhoria(new Melhoria(TipoMelhoria.PUNHO, Coluna.PRIMEIRA), Coluna.SEGUNDA);
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void colocarMelhoria_colunaBrancaSemLinhaCompleta_lancaExcecao() {
		doOgedei.colocarMelhoria(new Melhoria(TipoMelhoria.PUNHO, Coluna.PRIMEIRA), Coluna.BRANCA);
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void colocarMelhoria_colunaCheia_lancaExcecao() {
		for (int i = 0; i < TabuleiroJogador.LINHAS; i++) {
			colocar(doOgedei, Coluna.PRIMEIRA);
		}
		colocar(doOgedei, Coluna.PRIMEIRA);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void contarMelhorias_porRegiao_contaAColunaDaRegiao() {
		colocar(doOgedei, Coluna.PRIMEIRA);
		colocar(doOgedei, Coluna.PRIMEIRA);
		colocar(doOgedei, Coluna.SEGUNDA);
		assertEquals("A 1ª coluna é a da China", 2, doOgedei.contarMelhorias(Regiao.CHINA));
	}

	// AUXILIARES

	private void colocar(TabuleiroJogador destino, Coluna coluna) {
		destino.colocarMelhoria(new Melhoria(TipoMelhoria.PUNHO, coluna), coluna);
	}

	/** Põe uma melhoria em cada coluna colorida, completando a linha de baixo. */
	private void completarLinha(TabuleiroJogador destino) {
		colocar(destino, Coluna.PRIMEIRA);
		colocar(destino, Coluna.SEGUNDA);
		colocar(destino, Coluna.TERCEIRA);
	}

}
