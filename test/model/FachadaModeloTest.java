package model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class FachadaModeloTest {

	private static final int DEFAULT_TIMEOUT = 2000;
	private static final String ARQUIVO_MAPA = "resources/mapa_herdeiros.txt";

	private final FachadaModelo fachada = FachadaModelo.getInstancia();

	/** Cria a partida e faz cada jogador guardar a primeira carta secreta, liberando as jogadas. */
	private void comecarPartida(String... herois) throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList(herois));
		for (int i = 0; i < herois.length; i++) {
			assertNull(fachada.escolherCartaSecreta(i, fachada.getOpcoesDeCartaSecreta(i)[0]));
		}
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void getHerois_listaOsCincoHerois() {
		assertEquals(Heroi.values().length, fachada.getHerois().length);
		assertEquals("ALTANI", fachada.getHerois()[0]);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void getCorDoHeroi_devolveACorFixa() {
		assertEquals("VERDE", fachada.getCorDoHeroi("ALTANI"));
		assertEquals("VERMELHO", fachada.getCorDoHeroi("CHAGATAI"));
		assertEquals("PRETO", fachada.getCorDoHeroi("JOCHI"));
		assertEquals("AZUL", fachada.getCorDoHeroi("OGEDEI"));
		assertEquals("AMARELO", fachada.getCorDoHeroi("TOLUI"));
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void getCorDoHeroi_heroiDesconhecido_lancaExcecao() {
		fachada.getCorDoHeroi("GENGIS");
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void novaPartida_criaJogadoresNaOrdem() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI", "ALTANI"));
		assertTrue(fachada.temPartida());
		assertEquals(3, fachada.getQuantidadeJogadores());
		assertEquals("TOLUI", fachada.getHeroiDoJogador(1));
		assertEquals(1, fachada.getMoedasDoJogador(1));
		assertEquals(2, fachada.getMoedasDoJogador(2));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void novaPartida_corDoJogadorVemDoHeroi() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI", "ALTANI"));
		assertEquals("PRETO", fachada.getCorDoJogador(0));
		assertEquals("AMARELO", fachada.getCorDoJogador(1));
		assertEquals("VERDE", fachada.getCorDoJogador(2));
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void novaPartida_heroiDesconhecido_lancaExcecao() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "GENGIS"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void isColunaUsada_ficaVerdadeiroDepoisDeAtivar() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertFalse(fachada.isColunaUsada(0, "SEGUNDA"));
		assertTrue(fachada.ativarColuna("SEGUNDA", 0));
		assertTrue(fachada.isColunaUsada(0, "SEGUNDA"));
		assertFalse(fachada.isColunaUsada(0, "PRIMEIRA"));
		assertFalse(fachada.isColunaUsada(1, "SEGUNDA"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void getMelhorias_comecamVazias() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		for (String coluna : fachada.getColunas()) {
			assertEquals(0, fachada.getSimbolosDasMelhorias(0, coluna).length);
			assertEquals(0, fachada.getCoresDasMelhorias(0, coluna).length);
		}
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void getCapacidadeDaColuna_brancaDoJochiTemUmEspacoAMenos() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		int linhas = FachadaModelo.LINHAS_TABULEIRO_JOGADOR;
		assertEquals(linhas - 1, fachada.getCapacidadeDaColuna(0, "BRANCA"));
		assertEquals(linhas, fachada.getCapacidadeDaColuna(0, "PRIMEIRA"));
		assertEquals(linhas, fachada.getCapacidadeDaColuna(1, "BRANCA"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ativarColuna_altaniPodeEscolherOSegundoPeao() throws IOException {
		comecarPartida("ALTANI", "TOLUI");
		assertTrue(fachada.ativarColuna("PRIMEIRA", 1));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ativarColuna_peaoInexistente_devolveFalso() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertFalse(fachada.ativarColuna("PRIMEIRA", 1));
		assertFalse(fachada.isColunaUsada(0, "PRIMEIRA"));
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void isColunaUsada_colunaDesconhecida_lancaExcecao() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		fachada.isColunaUsada(0, "ROXA");
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void getTributoDisponivel_comecaComAsMoedasIniciais() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI", "ALTANI"));
		assertEquals(1, fachada.getTributoDisponivel(0, "MOEDA"));
		assertEquals(1, fachada.getTributoDisponivel(1, "MOEDA"));
		assertEquals(2, fachada.getTributoDisponivel(2, "MOEDA"));
		assertEquals(0, fachada.getTributoDisponivel(0, "ESPADA"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void getTributoDisponivel_contaAMoedaDaColuna() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertTrue(fachada.ativarColuna("PRIMEIRA", 0)); //Jochi: cavalo, punho e moeda
		assertEquals(2, fachada.getTributoDisponivel(0, "MOEDA"));
		assertEquals(1, fachada.getMovimentosRestantes());
	}

	@Test(expected = IllegalArgumentException.class, timeout = DEFAULT_TIMEOUT)
	public void getTributoDisponivel_tipoDesconhecido_lancaExcecao() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		fachada.getTributoDisponivel(0, "OURO");
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void encerrarTurno_semColunaAtivada_devolveMotivoEAVezFica() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertNotNull(fachada.encerrarTurno());
		assertEquals(0, fachada.getJogadorDaVez());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void encerrarTurno_comKhanPendente_devolveMotivoEAVezFica() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertTrue(fachada.ativarColuna("SEGUNDA", 0)); //Jochi: coluna com Khan
		assertTrue(fachada.isKhanPendente());
		assertNotNull(fachada.encerrarTurno());
		assertEquals(0, fachada.getJogadorDaVez());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void encerrarTurno_colunaSemKhan_passaAVez() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertTrue(fachada.ativarColuna("TERCEIRA", 0)); //Jochi: dois cavalos e punho
		assertNull(fachada.encerrarTurno());
		assertEquals(1, fachada.getJogadorDaVez());
		assertNull(fachada.getColunaAtiva());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void pegarTributo_provinciaJuntoDeKarakorum_gastaUmPunho() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertTrue(fachada.ativarColuna("TERCEIRA", 0)); //Jochi: dois cavalos e punho
		assertEquals(1, fachada.getPunhosRestantes());
		assertTrue(fachada.podePegarTributo(2)); //província de yurt ao lado de Karakorum

		assertNull(fachada.pegarTributo(2));
		assertEquals(1, fachada.getTributoDisponivel(0, "YURT"));
		assertEquals(0, fachada.getPunhosRestantes());
		assertEquals(0, fachada.getTributosDaProvincia(2));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void pegarTributo_semPunhos_devolveMotivo() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertTrue(fachada.ativarColuna("TERCEIRA", 0));
		assertNull(fachada.pegarTributo(2));
		assertFalse(fachada.podePegarTributo(3));
		assertNotNull(fachada.pegarTributo(3));
		assertEquals(1, fachada.getTributosDaProvincia(3));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void pegarTributo_provinciaLonge_devolveMotivoENadaMuda() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertTrue(fachada.ativarColuna("TERCEIRA", 0));
		assertFalse(fachada.podePegarTributo(5));
		assertNotNull(fachada.pegarTributo(5));
		assertEquals(1, fachada.getTributosDaProvincia(5));
		assertEquals(1, fachada.getPunhosRestantes());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void pegarTributo_semColunaAtivada_devolveMotivo() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertFalse(fachada.podePegarTributo(2));
		assertNotNull(fachada.pegarTributo(2));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void pegarTributo_pecaFicaGuardadaParaOsProximosTurnos() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertTrue(fachada.ativarColuna("TERCEIRA", 0));
		assertNull(fachada.pegarTributo(2));
		assertNull(fachada.encerrarTurno());
		assertEquals(1, fachada.getTributoDisponivel(0, "YURT"));
	}

	// KHAN

	@Test(timeout = DEFAULT_TIMEOUT)
	public void usarKhan_emProvinciaDoKhan_poeTributosEOJogadorNaoRecebe() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertTrue(fachada.ativarColuna("SEGUNDA", 0)); //Jochi: coluna com Khan
		assertEquals(FachadaModelo.KHAN_FORA, fachada.getLocalDoKhan());
		int naProvincia = fachada.getTributosDaProvincia(1);
		int nasVizinhas = fachada.getTributosDaProvincia(5) + fachada.getTributosDaProvincia(9);
		int moedas = fachada.getTributoDisponivel(0, "MOEDA");
		int yurts = fachada.getTributoDisponivel(0, "YURT");

		assertNull(fachada.usarKhan(1)); //província do Khan vizinha da 5 e da 9
		assertFalse(fachada.isKhanPendente());
		assertEquals(1, fachada.getLocalDoKhan());
		assertEquals(Math.min(naProvincia + 1, 3), fachada.getTributosDaProvincia(1));
		assertEquals(nasVizinhas + 2, fachada.getTributosDaProvincia(5) + fachada.getTributosDaProvincia(9));
		assertEquals(moedas, fachada.getTributoDisponivel(0, "MOEDA"));
		assertEquals(yurts, fachada.getTributoDisponivel(0, "YURT"));
		assertNull(fachada.encerrarTurno());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void usarKhan_provinciaQueNaoEDoKhan_devolveMotivoEKhanContinuaPendente() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertTrue(fachada.ativarColuna("SEGUNDA", 0));
		assertFalse(fachada.podeUsarKhanEm(5));
		assertNotNull(fachada.usarKhan(5));
		assertTrue(fachada.isKhanPendente());
		assertEquals(FachadaModelo.KHAN_FORA, fachada.getLocalDoKhan());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void usarKhan_mesmoLugarNoTurnoSeguinte_devolveMotivo() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertTrue(fachada.ativarColuna("SEGUNDA", 0));
		assertNull(fachada.usarKhan(4));
		assertNull(fachada.encerrarTurno());

		assertTrue(fachada.ativarColuna("TERCEIRA", 0)); //Tolui: coluna com Khan
		assertFalse(fachada.podeUsarKhanEm(4));
		assertNotNull(fachada.usarKhan(4));
		assertTrue(fachada.podeUsarKhanEm(FachadaModelo.KHAN_AREA_MELHORIAS));
		assertNull(fachada.usarKhan(FachadaModelo.KHAN_AREA_MELHORIAS));
		assertEquals(FachadaModelo.KHAN_AREA_MELHORIAS, fachada.getLocalDoKhan());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void usarKhan_colunaSemKhan_devolveMotivo() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertTrue(fachada.ativarColuna("TERCEIRA", 0)); //Jochi: dois cavalos e punho
		assertFalse(fachada.podeUsarKhanEm(1));
		assertNotNull(fachada.usarKhan(1));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void getIdsProvinciasDoKhan_listaAsQuatro() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		int[] ids = fachada.getIdsProvinciasDoKhan();
		Arrays.sort(ids);
		assertTrue(Arrays.equals(new int[] { 1, 4, 10, 11 }, ids));
	}

	// CARTAS DE CONSELHEIRO

	@Test(timeout = DEFAULT_TIMEOUT)
	public void novaPartida_revelaQuatroCartasAbertasEDuasSecretasPorJogador() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI"));
		assertEquals(4, fachada.getCartasAbertas().length);
		assertEquals(2, fachada.getOpcoesDeCartaSecreta(0).length);
		assertEquals(2, fachada.getOpcoesDeCartaSecreta(1).length);
		assertTrue(fachada.isEscolhaDeCartasPendente());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ativarColuna_antesDeTodosEscolheremACarta_devolveFalsoENadaMuda() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI"));
		assertNull(fachada.escolherCartaSecreta(0, fachada.getOpcoesDeCartaSecreta(0)[0]));
		assertEquals(1, fachada.getProximoJogadorSemCartaSecreta());

		assertFalse(fachada.ativarColuna("TERCEIRA", 0));
		assertFalse(fachada.isColunaUsada(0, "TERCEIRA"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void escolherCartaSecreta_todosEscolhem_liberaAPartidaEGuardaACarta() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI"));
		String deJochi = fachada.getOpcoesDeCartaSecreta(0)[1];
		String deTolui = fachada.getOpcoesDeCartaSecreta(1)[0];

		assertNull(fachada.escolherCartaSecreta(0, deJochi));
		assertNull(fachada.escolherCartaSecreta(1, deTolui));

		assertFalse(fachada.isEscolhaDeCartasPendente());
		assertEquals(-1, fachada.getProximoJogadorSemCartaSecreta());
		assertEquals(deJochi, fachada.getCartaSecretaDoJogador(0));
		assertEquals(deTolui, fachada.getCartaSecretaDoJogador(1));
		assertTrue(fachada.ativarColuna("TERCEIRA", 0));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void escolherCartaSecreta_cartaForaDasOpcoes_devolveMotivoEContinuaPendente() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI"));
		assertNotNull(fachada.escolherCartaSecreta(0, "nao_existe"));
		assertNull(fachada.getCartaSecretaDoJogador(0));
		assertEquals(0, fachada.getProximoJogadorSemCartaSecreta());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void getVotosDaCartaSecreta_semProgresso_valeZero() throws IOException {
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "TOLUI"));
		assertEquals(0, fachada.getVotosDaCartaSecreta(0)); //ainda sem carta
		assertNull(fachada.escolherCartaSecreta(0, fachada.getOpcoesDeCartaSecreta(0)[0]));
		assertEquals(0, fachada.getVotosDaCartaSecreta(0));
	}

	// CONSELHEIROS

	/** Manual: uma peça de bônus diferente sobre cada um dos 7 conselheiros (Karakorum incluída). */
	@Test(timeout = DEFAULT_TIMEOUT)
	public void getBonusNoConselheiro_inicioDaPartida_seteBonusDiferentes() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		int[] ids = fachada.getIdsConselheiros();
		assertArrayEquals(new int[] { 1, 8, 12, 13, 20, 30, 32 }, ids);
		Set<String> bonus = new HashSet<String>();
		for (int id : ids) {
			assertNotNull("Conselheiro " + id + " sem peça de bônus", fachada.getBonusNoConselheiro(id));
			bonus.add(fachada.getBonusNoConselheiro(id));
		}
		assertEquals(7, bonus.size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void getPedidoDoConselheiro_comunsKarakorumEQualquer() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertArrayEquals(new String[] { "SEDA", "SEDA" }, fachada.getPedidoDoConselheiro(8));
		assertEquals(3, fachada.getPedidoDoConselheiro(1).length);
		assertEquals(2, fachada.getVotosDoConselheiro(1));
		String q = FachadaModelo.TESOURO_QUALQUER;
		assertArrayEquals(new String[] { q, q, q }, fachada.getPedidoDoConselheiro(32));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void podeEntregarEm_semTesouros_eFalsoEEntregaDevolveMotivo() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertTrue(fachada.ativarColuna("PRIMEIRA", 0)); //o peão está em Karakorum
		assertFalse(fachada.podeEntregarEm(1));
		assertNotNull(fachada.entregarTesouros(1, fachada.getPedidoDoConselheiro(1)));
		assertEquals(0, fachada.getVotosDoJogador(0));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void entregarTesouros_paradaSemConselheiro_devolveMotivo() throws IOException {
		comecarPartida("JOCHI", "TOLUI");
		assertTrue(fachada.ativarColuna("PRIMEIRA", 0));
		assertNotNull(fachada.entregarTesouros(2, new String[] { "SEDA", "SEDA" }));
		assertFalse(fachada.podeEntregarEm(2));
	}

	@Test(expected = IOException.class, timeout = DEFAULT_TIMEOUT)
	public void novaPartida_mapaInexistente_lancaExcecao() throws IOException {
		fachada.novaPartida("resources/nao-existe.txt", Arrays.asList("JOCHI", "TOLUI"));
	}
}
