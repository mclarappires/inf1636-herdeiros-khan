package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CartaConselheiroAbertoTest {

	private static final int DEFAULT_TIMEOUT = 2000;

	private tabuleiro mapa;
	private parada naChina;
	private parada naPersia;
	private Jogador azul;
	private Jogador vermelho;

	@Before
	public void montarCenario() {
		karakorum inicio = new karakorum(0);
		naChina = new parada(1, 1, 1, regiao.CHINA);
		naPersia = new parada(2, 1, 1, regiao.PERSIA);
		mapa = new tabuleiro();
		mapa.adicionarParada(inicio);
		mapa.adicionarParada(naChina);
		mapa.adicionarParada(naPersia);
		azul = new Jogador(cor.AZUL, Heroi.OGEDEI, inicio, 0);
		vermelho = new Jogador(cor.VERMELHO, Heroi.OGEDEI, inicio, 0);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void cumprida_tresEspadasEmCartaDeQuatro_naoCumprida() {
		receberEspadas(azul, 3);
		assertFalse("3 espadas não bastam", new CartaTerEspadas(4).cumprida(azul, mapa));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void cumprida_quatroEspadas_cumprida() {
		receberEspadas(azul, 4);
		assertTrue("4 espadas cumprem a carta", new CartaTerEspadas(4).cumprida(azul, mapa));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void reivindicar_objetivoCumprido_somaUmVoto() {
		receberEspadas(azul, 4);
		new CartaTerEspadas(4).reivindicar(azul, mapa);
		assertEquals("A carta aberta vale 1 voto", 1, azul.getVotos());
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void reivindicar_cartaJaReivindicadaPorOutroJogador_lancaExcecao() {
		CartaConselheiroAberto carta = new CartaTerEspadas(4);
		receberEspadas(azul, 4);
		receberEspadas(vermelho, 4);
		carta.reivindicar(azul, mapa);
		carta.reivindicar(vermelho, mapa);
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void reivindicar_objetivoNaoCumprido_lancaExcecao() {
		new CartaTerEspadas(4).reivindicar(azul, mapa);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void yurtsPorRegiao_semYurtNaRussia_naoCumprida() {
		naChina.adicionarYurt(new yurt(cor.AZUL));
		naPersia.adicionarYurt(new yurt(cor.AZUL));
		assertFalse("Falta yurt na Rússia", new CartaYurtsPorRegiao(1).cumprida(azul, mapa));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void colunasComMelhorias_duasColunasComDuasMelhorias_cumprida() {
		colocarMelhorias(Coluna.PRIMEIRA, 2);
		colocarMelhorias(Coluna.SEGUNDA, 2);
		assertTrue("2 colunas com 2 melhorias", new CartaColunasComMelhorias(2, 2).cumprida(azul, mapa));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criarBaralho_temAsQuatorzeCartas() {
		assertEquals("O baralho aberto tem 14 cartas", 14, CartaConselheiroAberto.criarBaralho().size());
	}

	private void receberEspadas(Jogador j, int quantidade) {
		for (int i = 0; i < quantidade; i++) {
			j.receberTributo(tipoTributo.ESPADA);
		}
	}

	private void colocarMelhorias(Coluna coluna, int quantidade) {
		for (int i = 0; i < quantidade; i++) {
			azul.getTabuleiroJogador().colocarMelhoria(new Melhoria(TipoMelhoria.PUNHO, coluna), coluna);
		}
	}

}
