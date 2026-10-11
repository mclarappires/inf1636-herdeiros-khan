package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

public class AreaMelhoriasTest {

	private static final int DEFAULT_TIMEOUT = 2000;
	private static final int MOEDAS_INICIAIS = 4;

	// 6 peças: as 5 primeiras vão para os espaços 0 a 4 e a última fica na pilha
	private List<Melhoria> pecas;
	private AreaMelhorias area;

	// Ogedei: a 1ª coluna não tem Khan; a 3ª tem
	private Jogador jogador;

	@Before
	public void montarCenario() {
		pecas = new LinkedList<Melhoria>();
		for (int i = 0; i < 6; i++) {
			pecas.add(new Melhoria(TipoMelhoria.PUNHO, Coluna.PRIMEIRA));
		}
		Map<Integer, TipoTributo> icones = new HashMap<Integer, TipoTributo>();
		icones.put(0, TipoTributo.MOEDA);
		area = new AreaMelhorias(pecas, icones);
		area.preparar();

		jogador = new Jogador(Cor.AZUL, Heroi.OGEDEI, new Karakorum(0), MOEDAS_INICIAIS);
	}

	// ÁREA

	@Test(timeout = DEFAULT_TIMEOUT)
	public void preparar_preencheOsCincoEspacos() {
		assertEquals("5 das 6 peças saem da pilha", 1, area.getTamanhoPilha());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void retirar_deslizaAsDaEsquerdaParaADireita() {
		area.retirar(2);
		assertSame("A peça do espaço 1 desliza para o 2", pecas.get(1), area.getMelhoria(2));
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void retirar_espacoVazio_lancaExcecao() {
		area.retirar(4); //esvazia o espaço 0
		area.retirar(0);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void reabastecer_preencheSoOsEspacosVazios() {
		area.retirar(4);
		area.reabastecer();
		assertSame("O espaço 0, vazio, recebe a peça da pilha", pecas.get(5), area.getMelhoria(0));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void reabastecer_espacoVazioComIconeDeMoeda_devolveMoeda() {
		area.retirar(4);
		area.retirar(4);
		assertEquals("O espaço 1 tem ícone de moeda", Arrays.asList(TipoTributo.MOEDA), area.reabastecer());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criarPilha_temQuarentaEQuatroPecas() {
		assertEquals("O jogo tem 44 melhorias", 44, AreaMelhorias.criarPilha().size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criarPilha_divideAsMoedasEntreAsTresCores() {
		int moedasNaTerceira = 0;
		for (Melhoria m : AreaMelhorias.criarPilha()) {
			if (m.getTipo() == TipoMelhoria.MOEDA && m.getColuna() == Coluna.TERCEIRA) {
				moedasNaTerceira++;
			}
		}
		assertEquals("8 moedas divididas 3/3/2", 2, moedasNaTerceira);
	}

	// COMPRA E KHAN

	@Test(timeout = DEFAULT_TIMEOUT)
	public void comprarMelhoria_daArea_vaiParaOTabuleiroDoJogador() {
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.comprarMelhoria(area, 0);
		assertEquals("A melhoria comprada entra na 1ª coluna",
				1, jogador.getTabuleiroJogador().contarMelhorias(Coluna.PRIMEIRA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void comprarMelhoria_colunaCheia_naoTiraAPecaDaArea() {
		for (int i = 0; i < TabuleiroJogador.LINHAS; i++) {
			jogador.getTabuleiroJogador().colocarMelhoria(
					new Melhoria(TipoMelhoria.MOEDA, Coluna.PRIMEIRA), Coluna.PRIMEIRA);
		}
		jogador.ativarColuna(Coluna.SEGUNDA);
		assertThrows(IllegalStateException.class, new ThrowingRunnable() {
			public void run() {
				jogador.comprarMelhoria(area, 0);
			}
		});
		assertSame("A peça recusada continua no espaço 0", pecas.get(0), area.getMelhoria(0));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void usarKhanNaArea_espacoComMoeda_jogadorRecebeMoeda() {
		area.retirar(4);
		jogador.ativarColuna(Coluna.TERCEIRA);
		jogador.usarKhanNaAreaDeMelhorias(area);
		assertEquals("Ganha a moeda do ícone do espaço 0", MOEDAS_INICIAIS + 1, jogador.getMoedas());
	}

	// MELHORIA GRATUITA

	@Test(timeout = DEFAULT_TIMEOUT)
	public void pegarMelhoriaGratuita_doEspaco_vaiParaAColunaBranca() {
		completarLinha();
		jogador.pegarMelhoriaGratuita(area, 0);
		assertEquals("A gratuita entra na coluna branca",
				1, jogador.getTabuleiroJogador().contarMelhorias(Coluna.BRANCA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void pegarMelhoriaGratuitaDoTopo_semLinhaCompleta_naoTiraAPecaDaPilha() {
		assertThrows(IllegalStateException.class, new ThrowingRunnable() {
			public void run() {
				jogador.pegarMelhoriaGratuitaDoTopo(area);
			}
		});
		assertEquals("A pilha continua com 1 peça", 1, area.getTamanhoPilha());
	}

	private void completarLinha() {
		TabuleiroJogador doJogador = jogador.getTabuleiroJogador();
		doJogador.colocarMelhoria(new Melhoria(TipoMelhoria.PUNHO, Coluna.PRIMEIRA), Coluna.PRIMEIRA);
		doJogador.colocarMelhoria(new Melhoria(TipoMelhoria.PUNHO, Coluna.SEGUNDA), Coluna.SEGUNDA);
		doJogador.colocarMelhoria(new Melhoria(TipoMelhoria.PUNHO, Coluna.TERCEIRA), Coluna.TERCEIRA);
	}

}
