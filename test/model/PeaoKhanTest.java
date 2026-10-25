package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

public class PeaoKhanTest {

	private static final int DEFAULT_TIMEOUT = 2000;

	// doKhan é vizinha de vizinhaA e vizinhaB; comum não é do Khan
	private Provincia doKhan;
	private Provincia vizinhaA;
	private Provincia vizinhaB;
	private Provincia comum;
	private PeaoKhan khan;

	@Before
	public void montarCenario() {
		doKhan = new Provincia(1, TipoTributo.ESPADA, true);
		vizinhaA = new Provincia(2, TipoTributo.MOEDA);
		vizinhaB = new Provincia(3, TipoTributo.YURT);
		comum = new Provincia(4, TipoTributo.MOEDA);
		doKhan.ligarProvincia(vizinhaA);
		doKhan.ligarProvincia(vizinhaB);
		khan = new PeaoKhan();
	}

	// PROVÍNCIAS

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mover_paraProvinciaDoKhan_poeTributoNaPropriaProvincia() {
		khan.mover(doKhan);
		assertEquals("A província do Khan ganha 1 tributo", 1, doKhan.getQuantidade());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mover_paraProvinciaDoKhan_poeTributoNasVizinhas() {
		khan.mover(doKhan);
		assertEquals("Cada vizinha ganha 1 tributo", 2, vizinhaA.getQuantidade() + vizinhaB.getQuantidade());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mover_paraProvinciaDoKhan_naoEntregaTributos() {
		List<TipoTributo> entregues = khan.mover(doKhan);
		assertTrue("Os tributos ficam no mapa", entregues.isEmpty());
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void ligarProvincia_terceiraVizinhaDaProvinciaDoKhan_lancaExcecao() {
		doKhan.ligarProvincia(comum);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void usarKhan_emProvinciaDoKhan_jogadorNaoRecebeNada() {
		Jogador ogedei = new Jogador(Cor.AZUL, Heroi.OGEDEI, new Karakorum(0), 0);
		ogedei.ativarColuna(Coluna.TERCEIRA); //1 cavalo + 2 punhos + Khan
		ogedei.usarKhan(khan, doKhan);
		assertEquals("Não recebe a moeda da vizinha A", 0, ogedei.getQuantidade(TipoTributo.MOEDA));
		assertFalse("O Khan já foi usado", ogedei.isKhanPendente());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mover_provinciaComTresTributos_naoPassaDoMaximo() {
		encherAteOMaximo(doKhan);
		khan.mover(doKhan);
		assertEquals("Província não passa de 3 tributos", Provincia.MAXIMO_TRIBUTOS, doKhan.getQuantidade());
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void mover_paraProvinciaQueNaoEDoKhan_lancaExcecao() {
		khan.mover(comum);
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void mover_paraOMesmoLugar_lancaExcecao() {
		khan.mover(doKhan);
		khan.mover(doKhan);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mover_daProvinciaParaAArea_reabasteceAArea() {
		AreaMelhorias area = areaPreparadaComEspacoVazio();
		khan.mover(doKhan);
		khan.mover(area);
		assertTrue("O espaço 0 deveria ter sido reabastecido", area.getMelhoria(0) != null);
	}

	// USO PELO JOGADOR

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void usarKhan_semIconeDoKhanNaColuna_lancaExcecao() {
		Jogador ogedei = new Jogador(Cor.AZUL, Heroi.OGEDEI, new Karakorum(0), 0);
		ogedei.ativarColuna(Coluna.PRIMEIRA); //2 cavalos + 1 punho, sem Khan
		ogedei.usarKhan(khan, doKhan);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void usarKhan_movimentoRecusado_khanContinuaPendente() {
		final Jogador ogedei = new Jogador(Cor.AZUL, Heroi.OGEDEI, new Karakorum(0), 0);
		ogedei.ativarColuna(Coluna.TERCEIRA); //1 cavalo + 2 punhos + Khan
		assertThrows(IllegalStateException.class, new ThrowingRunnable() {
			@Override
			public void run() {
				ogedei.usarKhan(khan, comum);
			}
		});
		assertTrue("O Khan ainda precisa ser usado", ogedei.isKhanPendente());
	}

	// MAPA

	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_provinciaKhanEVizinha_ligaAsProvincias() throws IOException {
		Tabuleiro mapa = CarregadorMapa.carregar(new StringReader(
				"PROVINCIA 1 ESPADA KHAN\nPROVINCIA 2 MOEDA\nPROVINCIA_VIZINHA 1 2\n"), new Random(1));
		assertTrue("A província 2 deveria ser vizinha da 1",
				mapa.getProvincia(1).getProvinciasVizinhas().contains(mapa.getProvincia(2)));
	}

	// AUXILIARES

	private static void encherAteOMaximo(Provincia p) {
		for (int i = 0; i < Provincia.MAXIMO_TRIBUTOS; i++) {
			p.adicionarTributo();
		}
	}

	/** Área com 5 peças nos espaços, 1 na pilha e o espaço 0 já comprado. */
	private static AreaMelhorias areaPreparadaComEspacoVazio() {
		List<Melhoria> pecas = new LinkedList<Melhoria>();
		for (int i = 0; i < AreaMelhorias.ESPACOS + 1; i++) {
			pecas.add(new Melhoria(TipoMelhoria.PUNHO, Coluna.PRIMEIRA));
		}
		AreaMelhorias area = new AreaMelhorias(pecas, new HashMap<Integer, TipoTributo>());
		area.preparar();
		area.retirar(0);
		return area;
	}

}
