package model;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class KarakorumTest {

	private static final int DEFAULT_TIMEOUT = 2000;

	// pedido padrão: PELES, FERRO e BOI
	private Karakorum karakorum;
	private Parada vizinha;
	private Jogador jogador; //Ogedei; a 1ª coluna (2 cavalos + 1 punho) não tem Khan

	@Before
	public void montarCenario() {
		karakorum = new Karakorum(0);
		karakorum.colocarBonus(PecaBonus.ESPADAS);
		vizinha = new Parada(1, 1, 1, Regiao.CHINA);
		karakorum.ligar(vizinha);

		jogador = new Jogador(Cor.AZUL, Heroi.OGEDEI, karakorum, 0);
		jogador.receberTesouro(TipoTesouro.PELES);
		jogador.receberTesouro(TipoTesouro.FERRO);
		jogador.receberTesouro(TipoTesouro.GADO);
		jogador.receberTesouro(TipoTesouro.ESPECIARIAS);
		jogador.ativarColuna(Coluna.PRIMEIRA);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void entregarTesouros_tresTesourosPedidos_somaDoisVotos() {
		jogador.entregarTesouros(karakorum, ofertaPedida());
		assertEquals("A entrega em Karakorum vale 2 votos", 2, jogador.getVotos());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void entregarTesouros_primeiroAEntregar_recebeBonusDeKarakorum() {
		PecaBonus recebida = jogador.entregarTesouros(karakorum, ofertaPedida());
		assertEquals("Peça de bônus colocada em Karakorum", PecaBonus.ESPADAS, recebida);
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void entregarTesouros_tipoNaoPedido_lancaExcecao() {
		Map<TipoTesouro, Integer> oferta = ofertaPedida();
		oferta.remove(TipoTesouro.GADO);
		oferta.put(TipoTesouro.ESPECIARIAS, 1);
		jogador.entregarTesouros(karakorum, oferta);
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void entregarTesouros_peaoForaDeKarakorum_lancaExcecao() {
		jogador.mover(vizinha);
		jogador.entregarTesouros(karakorum, ofertaPedida());
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void getConselheiro_semPecaDeBonus_lancaExcecao() {
		new Karakorum(5).getConselheiro();
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void construtor_pedidoComDoisTesouros_lancaExcecao() {
		Map<TipoTesouro, Integer> pedido = new HashMap<TipoTesouro, Integer>();
		pedido.put(TipoTesouro.SEDA, 2);
		new Karakorum(5, pedido);
	}

	private Map<TipoTesouro, Integer> ofertaPedida() {
		Map<TipoTesouro, Integer> oferta = new HashMap<TipoTesouro, Integer>();
		oferta.put(TipoTesouro.PELES, 1);
		oferta.put(TipoTesouro.FERRO, 1);
		oferta.put(TipoTesouro.GADO, 1);
		return oferta;
	}

}
