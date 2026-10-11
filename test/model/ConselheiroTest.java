package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

public class ConselheiroTest {

	private static final int DEFAULT_TIMEOUT = 2000;
	private static final String ARQUIVO_MAPA = "resources/mapa.txt";
	private static final int ID_KARAKORUM = 1;
	private static final int ID_CONSELHEIRO_DO_FERRO = 12; //ligado direto a Karakorum
	private static final int[] IDS_CONSELHEIROS = { 8, 12, 13, 20, 30, 32 };

	// CARREGADOR

	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_conselheiroComTesouro_criaParadaDeConselheiro() throws IOException {
		Tabuleiro mapa = carregar("CONSELHEIRO 5 CHINA FERRO\n");
		assertTrue("A parada 5 deveria ser de conselheiro", mapa.getParada(5) instanceof ParadaConselheiro);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void carregar_conselheiroQualquer_aceitaTresTesourosDeTiposDiferentes() throws IOException {
		Tabuleiro mapa = carregar("CONSELHEIRO 5 CHINA QUALQUER\n");
		Map<TipoTesouro, Integer> oferta = new HashMap<TipoTesouro, Integer>();
		oferta.put(TipoTesouro.BOI, 1);
		oferta.put(TipoTesouro.FERRO, 1);
		oferta.put(TipoTesouro.TAPETE, 1);
		assertTrue("O conselheiro único aceita 3 tesouros quaisquer",
				((ParadaConselheiro) mapa.getParada(5)).getConselheiro().aceita(oferta));
	}

	// CONSELHEIRO

	@Test(timeout = DEFAULT_TIMEOUT)
	public void conselheiroComum_umSoTesouroDoTipo_naoAceita() {
		Map<TipoTesouro, Integer> oferta = new HashMap<TipoTesouro, Integer>();
		oferta.put(TipoTesouro.PELES, 1);
		assertFalse("O conselheiro comum pede 2 do tipo", Conselheiro.comum(TipoTesouro.PELES).aceita(oferta));
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void colocarBonus_duasVezes_lancaExcecao() {
		Conselheiro conselheiro = Conselheiro.unico();
		conselheiro.colocarBonus(PecaBonus.VOTO);
		conselheiro.colocarBonus(PecaBonus.MOEDAS);
	}

	// PREPARAÇÃO DO MAPA REAL

	@Test(timeout = DEFAULT_TIMEOUT)
	public void preparar_mapaReal_karakorumRecebePeca() throws IOException {
		Tabuleiro mapa = mapaRealPreparado();
		assertTrue("Karakorum deveria ter peça de bônus", ((Karakorum) mapa.getParada(ID_KARAKORUM)).temBonus());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void preparar_mapaReal_asSetePecasSaoDiferentes() throws IOException {
		Tabuleiro mapa = mapaRealPreparado();
		Set<PecaBonus> distribuidas = new HashSet<PecaBonus>();
		for (int id : IDS_CONSELHEIROS) {
			distribuidas.add(((ParadaConselheiro) mapa.getParada(id)).getConselheiro().getBonus());
		}
		distribuidas.add(((Karakorum) mapa.getParada(ID_KARAKORUM)).getConselheiro().getBonus());
		assertEquals("As 7 peças de bônus vão uma para cada lugar", PecaBonus.values().length, distribuidas.size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void entregarTesouros_noMapaReal_somaUmVoto() throws IOException {
		Tabuleiro mapa = mapaRealPreparado();
		Jogador ogedei = new Jogador(Cor.AZUL, Heroi.OGEDEI, (Karakorum) mapa.getParada(ID_KARAKORUM), 0);
		ogedei.receberTesouro(TipoTesouro.FERRO);
		ogedei.receberTesouro(TipoTesouro.FERRO);
		ogedei.ativarColuna(Coluna.PRIMEIRA); //2 cavalos
		ogedei.mover(mapa.getParada(ID_CONSELHEIRO_DO_FERRO));

		Map<TipoTesouro, Integer> oferta = new HashMap<TipoTesouro, Integer>();
		oferta.put(TipoTesouro.FERRO, 2);
		ogedei.entregarTesouros((ParadaConselheiro) mapa.getParada(ID_CONSELHEIRO_DO_FERRO), oferta);
		assertEquals("A entrega ao conselheiro do ferro vale 1 voto", 1, ogedei.getVotos());
	}

	// AUXILIARES

	private static Tabuleiro carregar(String texto) throws IOException {
		return CarregadorMapa.carregar(new StringReader(texto), new Random(1));
	}

	private static Tabuleiro mapaRealPreparado() throws IOException {
		FileReader leitor = new FileReader(ARQUIVO_MAPA);
		try {
			Tabuleiro mapa = CarregadorMapa.carregar(leitor, new Random(1L));
			mapa.preparar();
			return mapa;
		} finally {
			leitor.close();
		}
	}

}
