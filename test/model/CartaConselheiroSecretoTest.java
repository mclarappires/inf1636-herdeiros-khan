package model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class CartaConselheiroSecretoTest {

	private static final int DEFAULT_TIMEOUT = 2000;

	// a - b - c em linha; d isolada; dupla ligada a Karakorum
	private Tabuleiro mapa;
	private Karakorum inicio;
	private Parada a;
	private Parada b;
	private Parada c;
	private Parada d;
	private Parada dupla;
	private Jogador azul;

	@Before
	public void montarCenario() {
		inicio = new Karakorum(0);
		a = new Parada(1, 1, 1, Regiao.CHINA);
		b = new Parada(2, 1, 1, Regiao.CHINA);
		c = new Parada(3, 1, 1, Regiao.CHINA);
		d = new Parada(4, 1, 1, Regiao.PERSIA);
		dupla = new Parada(5, 2, 2, Regiao.RUSSIA);
		a.ligar(b);
		b.ligar(c);
		inicio.ligar(dupla);

		mapa = new Tabuleiro();
		mapa.adicionarParada(inicio);
		mapa.adicionarParada(a);
		mapa.adicionarParada(b);
		mapa.adicionarParada(c);
		mapa.adicionarParada(d);
		mapa.adicionarParada(dupla);

		azul = new Jogador(Cor.AZUL, Heroi.OGEDEI, inicio, 0);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void votosFinais_soldadoComSeteMelhorias_valeDoisVotos() {
		colocarPunhos(Coluna.PRIMEIRA, 4);
		colocarPunhos(Coluna.SEGUNDA, 3);
		assertEquals("7 melhorias atingem o 2º nível", 2, new CartaSoldado().votosFinais(azul, mapa));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void votosFinais_soldadoAbaixoDoPrimeiroNivel_valeZero() {
		colocarPunhos(Coluna.PRIMEIRA, 3);
		assertEquals("3 melhorias não chegam a 4", 0, new CartaSoldado().votosFinais(azul, mapa));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void medir_sacerdoteComTresMelhoriasIguais_contaUmPar() {
		colocarPunhos(Coluna.PRIMEIRA, 3);
		assertEquals("3 melhorias iguais formam 1 par", 1, new CartaSacerdote().medir(azul, mapa));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void medir_viajante_contaMaiorGrupoENaoOTotal() {
		colocarYurts(a, b, c, d);
		assertEquals("a-b-c formam o maior grupo; d está isolada", 3, new CartaViajante().medir(azul, mapa));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void medir_mensageiro_ignoraYurtDoToluiEmKarakorum() {
		Jogador tolui = new Jogador(Cor.PRETO, Heroi.TOLUI, inicio, 0);
		dupla.adicionarYurt(new Yurt(Cor.PRETO));
		assertEquals("Só a parada dupla conta", 1, new CartaMensageiro().medir(tolui, mapa));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void medir_bajulador_ignoraProvinciaQueNaoEDoKhan() {
		adicionarProvinciaEntreAEB(new Provincia(10, TipoTributo.MOEDA));
		adicionarProvinciaEntreAEB(new Provincia(11, TipoTributo.MOEDA, true));
		colocarYurts(a, b);
		assertEquals("Só a província do Khan conta", 1, new CartaBajulador().medir(azul, mapa));
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void construtor_limiaresForaDeOrdem_lancaExcecao() {
		new CartaCidadesVizinhas("Inválida", 1, 3, 3, 4);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void criarBaralho_temAsDezCartas() {
		assertEquals("O baralho secreto tem 10 cartas", 10, CartaConselheiroSecreto.criarBaralho().size());
	}

	private void colocarPunhos(Coluna coluna, int quantidade) {
		for (int i = 0; i < quantidade; i++) {
			azul.getTabuleiroJogador().colocarMelhoria(new Melhoria(TipoMelhoria.PUNHO, coluna), coluna);
		}
	}

	private void adicionarProvinciaEntreAEB(Provincia p) {
		p.ligarParada(a);
		p.ligarParada(b);
		mapa.adicionarProvincia(p);
	}

	private void colocarYurts(Parada... paradas) {
		for (Parada p : paradas) {
			p.adicionarYurt(new Yurt(Cor.AZUL));
		}
	}

}
