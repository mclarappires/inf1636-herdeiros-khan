package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Preparação do jogo e conquista de cidades sobre o mapa real: a mesma
 * lógica do TabuleiroTest, mas com as 9 cidades, 12 províncias e os 40
 * tesouros do jogo de verdade (manual págs. 4, 5 e 10).
 */
public class PreparacaoConquistaTest {

	private static final int DEFAULT_TIMEOUT = 2000;
	private static final String ARQUIVO_MAPA = "resources/mapa_herdeiros.txt";
	private static final int TOTAL_DE_TESOUROS = 40;
	private static final int TESOUROS_POR_CIDADE = 4;
	private static final int NUMERO_DE_CIDADES = 9;
	private static final int NUMERO_DE_PROVINCIAS = 12;

	private Tabuleiro mapa;

	@Before
	public void carregarEPreparar() throws IOException {
		FileReader leitor = new FileReader(ARQUIVO_MAPA);
		try {
			mapa = CarregadorMapa.carregar(leitor, new Random(1L));
		} finally {
			leitor.close();
		}
		mapa.preparar();
	}

	// PREPARAÇÃO

	@Test(timeout = DEFAULT_TIMEOUT)
	public void preparar_reveleTresCidades() {
		assertEquals("Devem ficar 3 cidades visiveis", 3, mapa.getCidadesVisiveis().size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void preparar_guardaSeisCidadesNaPilha() {
		assertEquals("9 cidades - 3 reveladas = 6 na pilha", 6, mapa.getQuantidadeCidadesNaoReveladas());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void preparar_cadaCidadeReveladaTemQuatroTesouros() {
		int cidadesSemQuatro = 0;
		for (Cidade c : mapa.getCidadesVisiveis()) {
			if (c.getQuantidadeTesouros() != TESOUROS_POR_CIDADE) {
				cidadesSemQuatro++;
			}
		}
		assertEquals("Nenhuma cidade revelada deveria ter outra quantidade de tesouros", 0, cidadesSemQuatro);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void preparar_colocaUmTributoEmCadaProvincia() {
		assertEquals("12 provincias com 1 peca cada", NUMERO_DE_PROVINCIAS, tributosNasProvincias());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void preparar_reservaFicaComVinteEOitoTesouros() {
		assertEquals("40 tesouros - 3 cidades x 4 = 28", 28, mapa.getTesourosNaReserva());
	}

	// CONQUISTA

	@Test(timeout = DEFAULT_TIMEOUT)
	public void conquistar_tresPrimeirosTesourosNaoConquistamACidade() {
		Cidade alvo = mapa.getCidadesVisiveis().get(0);
		assertFalse("Tomar 3 de 4 tesouros nao conquista", tomarAlgunsTesouros(alvo, 3));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void conquistar_quartoTesouroConquistaACidade() {
		Cidade alvo = mapa.getCidadesVisiveis().get(0);
		assertTrue("O 4o tesouro conquista a cidade", tomarAlgunsTesouros(alvo, 4));
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void conquistar_cidadeAindaNaoReveladaLancaExcecao() {
		mapa.tomarTesouroDeCidade(idDeCidadeEscondida(), TipoTesouro.values()[0]);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void conquistarTodas_nuncaHaMaisDeTresCidadesVisiveis() {
		assertEquals("O maximo de cidades visiveis ao mesmo tempo", 3, maiorNumeroDeVisiveisAoConquistarTudo());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void conquistarTodas_nenhumaCidadeEConquistadaDuasVezes() {
		conquistarTodas();
		assertEquals("As 9 cidades conquistadas deveriam ser distintas",
				NUMERO_DE_CIDADES, new HashSet<Cidade>(mapa.getCidadesConquistadas()).size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void conquistarTodas_terminaOJogoPorFaltaDeCidades() {
		conquistarTodas();
		assertTrue("Sem cidades, o jogo termina", mapa.todasCidadesConquistadas());
	}

	/** 40 tesouros - 9 cidades x 4 tesouros = 4 que nunca saem da reserva. */
	@Test(timeout = DEFAULT_TIMEOUT)
	public void conquistarTodas_sobramQuatroTesourosNaReserva() {
		conquistarTodas();
		assertEquals("Sobram 40 - 36 tesouros", TOTAL_DE_TESOUROS - NUMERO_DE_CIDADES * TESOUROS_POR_CIDADE,
				mapa.getTesourosNaReserva());
	}

	// AUXILIARES

	private int tributosNasProvincias() {
		int total = 0;
		for (int id = 1; id <= NUMERO_DE_PROVINCIAS; id++) {
			total += mapa.getProvincia(id).getQuantidade();
		}
		return total;
	}

	/** Toma até N tesouros da cidade; devolve true se algum deles a conquistou. */
	private boolean tomarAlgunsTesouros(Cidade alvo, int quantidade) {
		boolean conquistou = false;
		int tomados = 0;
		for (TipoTesouro tipo : TipoTesouro.values()) {
			while (tomados < quantidade && alvo.getQuantidade(tipo) > 0) {
				conquistou = mapa.tomarTesouroDeCidade(alvo.getId(), tipo) || conquistou;
				tomados++;
			}
		}
		return conquistou;
	}

	private int idDeCidadeEscondida() {
		for (int id = 1; id <= NUMERO_DE_CIDADES; id++) {
			if (!mapa.getCidadesVisiveis().contains(mapa.getCidade(id))) {
				return id;
			}
		}
		throw new IllegalStateException("Nao ha cidade escondida");
	}

	private void conquistarTodas() {
		while (!mapa.getCidadesVisiveis().isEmpty()) {
			tomarAlgunsTesouros(mapa.getCidadesVisiveis().get(0), TESOUROS_POR_CIDADE);
		}
	}

	private int maiorNumeroDeVisiveisAoConquistarTudo() {
		int maior = mapa.getCidadesVisiveis().size();
		while (!mapa.getCidadesVisiveis().isEmpty()) {
			tomarAlgunsTesouros(mapa.getCidadesVisiveis().get(0), TESOUROS_POR_CIDADE);
			maior = Math.max(maior, mapa.getCidadesVisiveis().size());
		}
		return maior;
	}
}