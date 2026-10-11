package model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class PeaoTest {

	private static final int DEFAULT_TIMEOUT = 2000;

	private Parada simples; //aceita 1 peão
	private Parada dupla;   //aceita 2 peões
	private Peao peao;      //começa na parada simples

	@Before
	public void montarCenario() {
		simples = new Parada(1, 1, 1, Regiao.CHINA);
		dupla = new Parada(2, 2, 2, Regiao.CHINA);
		simples.ligar(dupla);
		peao = new Peao(Cor.AZUL, simples);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void moverPara_atualizaAPosicaoDoPeao() {
		peao.moverPara(dupla);
		assertSame("O peão deveria estar na parada dupla", dupla, peao.getPosicao());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void moverPara_liberaAVagaDaParadaAnterior() {
		Peao outro = new Peao(Cor.VERMELHO, dupla);
		peao.moverPara(dupla);
		assertTrue("A parada simples ficou vazia", outro.podeTerminarEm(simples));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void podeTerminarEm_paradaSimplesOcupadaPorOutroPeao_retornaFalso() {
		Peao outro = new Peao(Cor.VERMELHO, dupla);
		assertFalse("Parada simples só aceita 1 peão", outro.podeTerminarEm(simples));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void podeTerminarEm_paradaOndeJaEsta_naoContaOProprioPeao() {
		assertTrue("O peão pode ficar onde está", peao.podeTerminarEm(simples));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void podeTerminarEm_paradaDuplaComUmPeao_retornaVerdadeiro() {
		new Peao(Cor.VERMELHO, dupla);
		assertTrue("Parada dupla aceita 2 peões", peao.podeTerminarEm(dupla));
	}

}
