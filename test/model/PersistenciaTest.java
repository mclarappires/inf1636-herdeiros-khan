package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Salvar e carregar a partida (GravadorPartida / LeitorPartida): o texto gravado só tem ASCII,
 * a partida relida tem o mesmo estado (gravá-la de novo produz o mesmo texto) e só se salva
 * entre jogadas.
 */
public class PersistenciaTest {

	private static final int DEFAULT_TIMEOUT = 2000;
	private static final String ARQUIVO_MAPA = "resources/mapa_herdeiros.txt";
	private static final int ID_KARAKORUM = 1;

	private Partida partida;

	/** Tolui, Chagatai, Jochi e Altani já fizeram as escolhas iniciais; ninguém jogou ainda. */
	@Before
	public void criarPartida() throws IOException {
		List<Heroi> herois = Arrays.asList(Heroi.TOLUI, Heroi.CHAGATAI, Heroi.JOCHI, Heroi.ALTANI);
		List<String> nomes = Arrays.asList("João", "Ana Luísa", "Bia#1", "Caio\\B");
		partida = new Partida(carregarMapa(), herois, nomes, new Random(7L));
		fazerEscolhasIniciais(partida);
	}

	private static Tabuleiro carregarMapa() throws IOException {
		FileReader leitor = new FileReader(ARQUIVO_MAPA);
		try {
			return CarregadorMapa.carregar(leitor, new Random(1L));
		} finally {
			leitor.close();
		}
	}

	private static void fazerEscolhasIniciais(Partida p) {
		for (Jogador j : p.getJogadores()) {
			p.escolherCartaSecreta(j, p.getOpcoesDeCartaSecreta(j).get(0).getId());
			if (!p.getOpcoesDeMelhoriaInicial(j).isEmpty()) {
				p.escolherMelhoriaInicial(j, 0);
			}
		}
	}

	private static String salvar(Partida p) throws IOException {
		StringWriter texto = new StringWriter();
		GravadorPartida.gravar(p, texto);
		return texto.toString();
	}

	private static Partida carregar(String texto) throws IOException {
		return LeitorPartida.ler(new StringReader(texto), carregarMapa());
	}

	/**
	 * Uma rodada quase completa: o Tolui anda, pega tributo, constrói yurt e põe o Khan numa
	 * província; o Chagatai sobra com 1 espada virtual e leva o Khan à área de melhorias; o
	 * Jochi compra uma melhoria. A vez fica com a Altani, que ainda não ativou coluna.
	 */
	private void jogarTresTurnos() {
		Tabuleiro t = partida.getTabuleiro();

		Jogador tolui = partida.getJogadorDaVez();
		tolui.ativarColuna(Coluna.TERCEIRA); //cavalo, 2 punhos, Khan
		tolui.mover(t.getParada(16));
		tolui.pegarTributo(t.getProvincia(2)); //província de yurt
		tolui.construirYurt(t.getParada(16));
		tolui.usarKhan(t.getPeaoKhan(), t.getProvincia(11));
		partida.encerrarTurnoDaVez();

		Jogador chagatai = partida.getJogadorDaVez();
		chagatai.ativarColuna(Coluna.PRIMEIRA); //cavalo, 2 punhos, Khan
		chagatai.receberTributoVirtual(TipoTributo.ESPADA); //como uma melhoria de espada
		chagatai.mover(t.getParada(10));
		chagatai.usarKhan(t.getPeaoKhan(), t.getAreaMelhorias());
		partida.encerrarTurnoDaVez();

		Jogador jochi = partida.getJogadorDaVez();
		jochi.ativarColuna(Coluna.PRIMEIRA); //cavalo, punho, moeda
		jochi.mover(t.getParada(12));
		jochi.comprarMelhoria(t.getAreaMelhorias(), 0);
		partida.encerrarTurnoDaVez();
	}

	// IDA E VOLTA

	/** Gravar a partida relida produz exatamente o mesmo texto: nenhum dado se perde na volta. */
	@Test(timeout = DEFAULT_TIMEOUT)
	public void salvarCarregarSalvar_textoIgual() throws IOException {
		jogarTresTurnos();
		String primeiro = salvar(partida);
		assertEquals(primeiro, salvar(carregar(primeiro)));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_estadoDoTabuleiroEDosJogadoresVolta() throws IOException {
		jogarTresTurnos();
		Partida relida = carregar(salvar(partida));
		Tabuleiro t = relida.getTabuleiro();

		assertEquals(3, relida.getIndiceDaVez());
		assertEquals(16, relida.getJogador(0).getPeoes().get(0).getPosicao().getId());
		assertEquals(10, relida.getJogador(1).getPeoes().get(0).getPosicao().getId());
		assertEquals(2, relida.getJogador(3).getPeoes().size()); //Altani
		assertEquals(Arrays.asList(Cor.AMARELO), coresDosYurts(t.getParada(16)));
		assertSame(t.getAreaMelhorias(), t.getPeaoKhan().getLocal());
		for (Provincia original : partida.getTabuleiro().getProvincias()) {
			assertEquals(original.getQuantidade(), t.getProvincia(original.getId()).getQuantidade());
		}
		assertEquals(partida.getTabuleiro().getAreaMelhorias().getTamanhoPilha(),
				t.getAreaMelhorias().getTamanhoPilha());
		assertNull(t.getAreaMelhorias().getMelhoria(0)); //comprada e não reposta
		assertEquals(1, relida.getJogador(2).getTabuleiroJogador().contarMelhorias(Coluna.PRIMEIRA)
				+ relida.getJogador(2).getTabuleiroJogador().contarMelhorias(Coluna.SEGUNDA)
				+ relida.getJogador(2).getTabuleiroJogador().contarMelhorias(Coluna.TERCEIRA));
		assertTrue(relida.getJogador(0).getPecaAtivacao(Coluna.TERCEIRA).isUsada());
		assertEquals(partida.getJogador(2).getCartaSecreta().getId(),
				relida.getJogador(2).getCartaSecreta().getId());
	}

	/** A partida relida continua jogável: a Altani joga e a vez passa ao Tolui. */
	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_partidaContinuaJogavel() throws IOException {
		jogarTresTurnos();
		Partida relida = carregar(salvar(partida));
		Jogador altani = relida.getJogadorDaVez();
		altani.ativarColuna(Coluna.SEGUNDA, 1); //2 cavalos, punho; segundo peão
		altani.mover(relida.getTabuleiro().getParada(27));
		relida.encerrarTurnoDaVez();
		assertEquals(0, relida.getIndiceDaVez());
	}

	// SÓ ENTRE JOGADAS

	@Test(timeout = DEFAULT_TIMEOUT)
	public void salvar_colunaAtivada_recusado() throws IOException {
		partida.getJogadorDaVez().ativarColuna(Coluna.BRANCA);
		assertFalse(partida.podeSalvar());
		try {
			salvar(partida);
			fail("Não deveria salvar no meio do turno");
		} catch (IllegalStateException e) {
			//esperado
		}
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void salvar_escolhaInicialPendente_recusado() throws IOException {
		Partida nova = new Partida(carregarMapa(), Arrays.asList(Heroi.OGEDEI, Heroi.TOLUI));
		assertFalse(nova.podeSalvar());
		fazerEscolhasIniciais(nova);
		assertTrue(nova.podeSalvar());
	}

	// ASCII E NOMES

	/** Acentos, '#' (abre comentário) e '\' viram \\uXXXX; os nomes voltam iguais. */
	@Test(timeout = DEFAULT_TIMEOUT)
	public void salvar_soAscii_nomesVoltamIguais() throws IOException {
		String texto = salvar(partida);
		for (int i = 0; i < texto.length(); i++) {
			assertTrue("Caractere não ASCII: " + texto.charAt(i), texto.charAt(i) < 128);
		}
		assertTrue(texto.contains("Jo\\u00e3o"));
		Partida relida = carregar(texto);
		assertEquals("João", relida.getJogador(0).getNome());
		assertEquals("Ana Luísa", relida.getJogador(1).getNome());
		assertEquals("Bia#1", relida.getJogador(2).getNome());
		assertEquals("Caio\\B", relida.getJogador(3).getNome());
	}

	// HABILIDADES DOS HERÓIS

	/** O yurt inicial do Tolui é recolocado pelo próprio Jogador: Karakorum não fica com 2. */
	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_tolui_umSoYurtEmKarakorum() throws IOException {
		jogarTresTurnos();
		Partida relida = carregar(salvar(partida));
		assertEquals(1, relida.getTabuleiro().getParada(ID_KARAKORUM).getYurts().size());
		assertEquals(partida.getJogador(0).getYurtsDisponiveis(), relida.getJogador(0).getYurtsDisponiveis());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_chagataiGuardaEspadas_jochiTemMelhoriaNaBranca() throws IOException {
		jogarTresTurnos();
		Partida relida = carregar(salvar(partida));
		assertEquals(1, relida.getJogador(1).getEspadasGuardadas());
		List<Melhoria> branca = relida.getJogador(2).getTabuleiroJogador().getMelhorias(Coluna.BRANCA);
		assertEquals(1, branca.size());
		assertEquals(GravadorPartida.codificar(
				partida.getJogador(2).getTabuleiroJogador().getMelhorias(Coluna.BRANCA).get(0)),
				GravadorPartida.codificar(branca.get(0)));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_khanAindaFora() throws IOException {
		Partida relida = carregar(salvar(partida));
		assertNull(relida.getTabuleiro().getPeaoKhan().getLocal());
	}

	// CIDADES, BÔNUS E FIM DE JOGO

	/** Cidade conquistada mantém o yurt do conquistador, e a próxima revelada continua a mesma. */
	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_cidadeConquistadaComYurt() throws IOException {
		Tabuleiro t = partida.getTabuleiro();
		Cidade alvo = t.getCidadesVisiveis().get(0);
		for (TipoTesouro tipo : TipoTesouro.values()) {
			while (alvo.getQuantidade(tipo) > 0) {
				t.tomarTesouroDeCidade(alvo.getId(), tipo);
			}
		}
		alvo.colocarYurtDeConquista(new Yurt(Cor.VERMELHO));

		Partida relida = carregar(salvar(partida));
		Tabuleiro r = relida.getTabuleiro();
		assertEquals(1, r.getCidadesConquistadas().size());
		Cidade conquistada = r.getCidadesConquistadas().get(0);
		assertEquals(alvo.getId(), conquistada.getId());
		assertTrue(conquistada.isConquistada());
		assertEquals(Cor.VERMELHO, conquistada.getYurtDeConquista().getDono());
		assertEquals(t.getCidadesVisiveis().size(), r.getCidadesVisiveis().size());
		assertEquals(t.getQuantidadeCidadesNaoReveladas(), r.getQuantidadeCidadesNaoReveladas());
		assertEquals(t.getTesourosNaReserva(), r.getTesourosNaReserva());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_bonusColetadoNaoVoltaAoMapa() throws IOException {
		Parada p = partida.getTabuleiro().getParada(8); //conselheiro da seda
		((ParadaConselheiro) p).getConselheiro().coletarBonus();
		Partida relida = carregar(salvar(partida));
		Conselheiro c = ((ParadaConselheiro) relida.getTabuleiro().getParada(8)).getConselheiro();
		assertTrue(c.bonusJaColetado());
		assertNull(c.coletarBonus());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_fimDisparado_mantemTurnosAteOFim() throws IOException {
		String texto = salvar(partida).replace("FIM 0 0 0", "FIM 1 5 0");
		Partida relida = carregar(texto);
		assertTrue(relida.isFimDisparado());
		assertEquals(5, relida.getTurnosAteOFim());
		assertFalse(relida.isTerminada());
	}

	// ARQUIVO INVÁLIDO

	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_heroiDesconhecido_erroComNumeroDaLinha() throws IOException {
		String texto = salvar(partida).replace("JOGADOR 0 TOLUI", "JOGADOR 0 GENGIS");
		try {
			carregar(texto);
			fail("Herói desconhecido deveria ser recusado");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage(), e.getMessage().startsWith("Linha "));
		}
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_linhaTruncada_recusada() throws IOException {
		String texto = salvar(partida).replace("VEZ 0", "VEZ");
		try {
			carregar(texto);
			fail("Linha VEZ sem número deveria ser recusada");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("VEZ"));
		}
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_cidadeFaltando_recusada() throws IOException {
		String texto = salvar(partida);
		int inicio = texto.indexOf("CIDADE_VISIVEL");
		int fim = texto.indexOf('\n', inicio) + 1;
		try {
			carregar(texto.substring(0, inicio) + texto.substring(fim));
			fail("Cidade sem linha deveria ser recusada");
		} catch (IllegalArgumentException e) {
			//esperado
		}
	}

	// PELA FACHADA (arquivo de verdade)

	/** Arquivo inválido não troca a partida atual; o válido troca e o arquivo gravado é ASCII. */
	@Test(timeout = DEFAULT_TIMEOUT)
	public void fachada_salvarECarregarArquivo() throws IOException {
		FachadaModelo fachada = FachadaModelo.getInstancia();
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("OGEDEI", "TOLUI"), Arrays.asList("Zé", "Lia"));
		File arquivo = File.createTempFile("partida", ".txt");
		arquivo.deleteOnExit();
		assertNotNull(fachada.salvarPartida(arquivo.getPath())); //escolhas iniciais pendentes

		while (fachada.getProximoJogadorComEscolhaPendente() >= 0) {
			int j = fachada.getProximoJogadorComEscolhaPendente();
			assertNull(fachada.escolherCartaSecreta(j, fachada.getOpcoesDeCartaSecreta(j)[0]));
		}
		assertTrue(fachada.podeSalvar());
		assertNull(fachada.salvarPartida(arquivo.getPath()));
		InputStream bytes = new FileInputStream(arquivo);
		try {
			int b;
			while ((b = bytes.read()) != -1) {
				assertTrue("Byte não ASCII: " + b, b < 128);
			}
		} finally {
			bytes.close();
		}

		File invalido = File.createTempFile("invalido", ".txt");
		invalido.deleteOnExit();
		Writer escritor = new OutputStreamWriter(new FileOutputStream(invalido), "US-ASCII");
		try {
			escritor.write("VERSAO 1\nJOGADOR 0 GENGIS Temujin\n");
		} finally {
			escritor.close();
		}
		fachada.novaPartida(ARQUIVO_MAPA, Arrays.asList("JOCHI", "ALTANI"));
		assertNotNull(fachada.carregarPartida(ARQUIVO_MAPA, invalido.getPath()));
		assertEquals("JOCHI", fachada.getHeroiDoJogador(0)); //a partida atual ficou

		assertNull(fachada.carregarPartida(ARQUIVO_MAPA, arquivo.getPath()));
		assertEquals("OGEDEI", fachada.getHeroiDoJogador(0));
		assertEquals("Zé", fachada.getNomeDoJogador(0));
		assertTrue(fachada.podeSalvar());
	}

	private static List<Cor> coresDosYurts(Parada p) {
		List<Cor> cores = new java.util.LinkedList<Cor>();
		for (Yurt y : p.getYurts()) {
			cores.add(y.getDono());
		}
		return cores;
	}
}
