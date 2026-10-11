package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class JogadorTest {

	private static final int DEFAULT_TIMEOUT = 2000;
	private static final int MOEDAS_INICIAIS = 2;
	private static final int ID_CIDADE = 20;
	private static final Cor COR_DO_JOGADOR = Cor.AZUL;
	private static final Cor COR_DO_ADVERSARIO = Cor.VERMELHO;

	// Karakorum - vizinha - distante - paradaDoConselheiro
	private Karakorum inicio;
	private Parada vizinha;
	private Parada distante;
	private ParadaConselheiro paradaDoConselheiro;
	private Parada paradaDupla; //ligada a Karakorum, aceita 2 peões e 2 yurts

	private Provincia provinciaDaVizinha;  //tributo de espada
	private Provincia provinciaDaDistante; //tributo de yurt

	// Ogedei: 1ª coluna = 2 cavalos + 1 punho; 2ª = 1 cavalo + 1 punho + 1 moeda;
	// 3ª = 1 cavalo + 2 punhos + Khan; branca = 2 cavalos + Khan + recuperar
	private Jogador jogador;

	@Before
	public void montarCenario() {
		inicio = new Karakorum(0);
		vizinha = new Parada(1, 1, 1, Regiao.CHINA);
		distante = new Parada(2, 1, 1, Regiao.CHINA);

		Map<TipoTesouro, Integer> pedido = new HashMap<TipoTesouro, Integer>();
		pedido.put(TipoTesouro.FERRO, 2);
		paradaDoConselheiro = new ParadaConselheiro(3, Regiao.CHINA, new Conselheiro(pedido, 1, PecaBonus.MOEDAS));

		inicio.ligar(vizinha);
		vizinha.ligar(distante);
		distante.ligar(paradaDoConselheiro);

		paradaDupla = new Parada(4, 2, 2, Regiao.CHINA);
		inicio.ligar(paradaDupla);

		provinciaDaVizinha = new Provincia(10, TipoTributo.ESPADA);
		provinciaDaVizinha.ligarParada(vizinha);
		provinciaDaVizinha.adicionarTributo();

		provinciaDaDistante = new Provincia(11, TipoTributo.YURT);
		provinciaDaDistante.ligarParada(distante);
		provinciaDaDistante.adicionarTributo();

		jogador = new Jogador(COR_DO_JOGADOR, Heroi.OGEDEI, inicio, MOEDAS_INICIAIS);
	}

	// ATIVAÇÃO DE COLUNA

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ativarColuna_concedeMovimentosDosIconesBaseDoHeroi() {
		jogador.ativarColuna(Coluna.PRIMEIRA);
		assertEquals("Ogedei tem 2 cavalos na 1ª coluna", 2, jogador.getMovimentosRestantes());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ativarColuna_melhoriaDoisCavalos_somaDoisMovimentos() {
		jogador.getTabuleiroJogador().colocarMelhoria(
				new Melhoria(TipoMelhoria.DOIS_CAVALOS, Coluna.PRIMEIRA), Coluna.PRIMEIRA);
		jogador.ativarColuna(Coluna.PRIMEIRA);
		assertEquals("2 cavalos da coluna + 2 da melhoria", 4, jogador.getMovimentosRestantes());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ativarColuna_moedaVirtualContaComoMoedaNoTurno() {
		jogador.ativarColuna(Coluna.SEGUNDA);
		assertEquals("Moedas do jogador + 1 moeda virtual da coluna",
				MOEDAS_INICIAIS + 1, jogador.getDisponivel(TipoTributo.MOEDA));
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void ativarColuna_colunaJaUsadaEmTurnoAnterior_lancaExcecao() {
		jogarTurnoSemAgir(Coluna.PRIMEIRA);
		jogador.ativarColuna(Coluna.PRIMEIRA);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ativarColunaBranca_recuperaPecaDeColunaUsadaAntes() {
		jogarTurnoSemAgir(Coluna.PRIMEIRA);
		jogador.ativarColuna(Coluna.BRANCA);
		assertFalse("A peça da 1ª coluna deveria ter sido recuperada",
				jogador.getPecaAtivacao(Coluna.PRIMEIRA).isUsada());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ativarColunaBranca_recuperaAPropriaPeca() {
		jogador.ativarColuna(Coluna.BRANCA);
		assertFalse("A peça recém-usada na coluna branca também volta",
				jogador.getPecaAtivacao(Coluna.BRANCA).isUsada());
	}

	// MOVIMENTO

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void mover_semMovimentosRestantes_lancaExcecao() {
		jogador.ativarColuna(Coluna.SEGUNDA); //1 cavalo
		jogador.mover(vizinha);
		jogador.mover(distante);
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void mover_paradaNaoAdjacente_lancaExcecao() {
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(distante);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mover_atravessaParadaComYurtProprio_gastaUmMovimento() {
		vizinha.adicionarYurt(new Yurt(COR_DO_JOGADOR));
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(distante);
		assertEquals("Pular a parada com yurt próprio não gasta movimento",
				1, jogador.getMovimentosRestantes());
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void mover_paradaComYurtDeOutroJogador_naoPodeSerPulada() {
		vizinha.adicionarYurt(new Yurt(COR_DO_ADVERSARIO));
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(distante);
	}

	// TRIBUTOS

	@Test(timeout = DEFAULT_TIMEOUT)
	public void pegarTributo_provinciaAdjacenteAParadaVisitada_entraNoInventario() {
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(vizinha);
		jogador.pegarTributo(provinciaDaVizinha);
		assertEquals("Jogador deveria ter 1 espada", 1, jogador.getQuantidade(TipoTributo.ESPADA));
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void pegarTributo_provinciaLongeDasParadasVisitadas_lancaExcecao() {
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(vizinha);
		jogador.pegarTributo(provinciaDaDistante);
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void pegarTributo_provinciaDeParadaPuladaPorYurt_lancaExcecao() {
		vizinha.adicionarYurt(new Yurt(COR_DO_JOGADOR));
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(distante);
		jogador.pegarTributo(provinciaDaVizinha);
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void pegarTributo_semPunhosRestantes_lancaExcecao() {
		provinciaDaVizinha.adicionarTributo();
		jogador.ativarColuna(Coluna.PRIMEIRA); //1 punho
		jogador.mover(vizinha);
		jogador.pegarTributo(provinciaDaVizinha);
		jogador.pegarTributo(provinciaDaVizinha);
	}

	// YURTS
	@Test(timeout = DEFAULT_TIMEOUT)
	public void construirYurt_paradaDuplaComYurtDeOutroJogador_constroi() {
		paradaDupla.adicionarYurt(new Yurt(COR_DO_ADVERSARIO));
		irAteAParadaDuplaComTributosDeYurt(1);
		jogador.construirYurt(paradaDupla);
		assertTrue("A parada dupla deveria ter o yurt do jogador", paradaDupla.temYurtDe(COR_DO_JOGADOR));
	}

	// MELHORIAS

	@Test(timeout = DEFAULT_TIMEOUT)
	public void comprarMelhoria_consomeDuasMoedas() {
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.comprarMelhoria(areaCom(new Melhoria(TipoMelhoria.PUNHO, Coluna.TERCEIRA)), 0);
		assertEquals("As 2 moedas deveriam ter sido gastas", 0, jogador.getMoedas());
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void comprarMelhoria_comUmaMoeda_lancaExcecao() {
		Jogador pobre = new Jogador(Cor.VERDE, Heroi.OGEDEI, inicio, 1);
		pobre.ativarColuna(Coluna.PRIMEIRA);
		pobre.comprarMelhoria(areaCom(new Melhoria(TipoMelhoria.PUNHO, Coluna.TERCEIRA)), 0);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void comprarMelhoria_naoValeNoTurnoDaCompra() {
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.comprarMelhoria(areaCom(new Melhoria(TipoMelhoria.DOIS_CAVALOS, Coluna.PRIMEIRA)), 0);
		assertEquals("Só os 2 cavalos da coluna valem neste turno", 2, jogador.getMovimentosRestantes());
	}

	// CONSELHEIROS

	@Test(timeout = DEFAULT_TIMEOUT)
	public void entregarTesouros_conselheiroAceita_somaVotosDaEntrega() {
		levarAoConselheiroComFerro(2);
		jogador.entregarTesouros(paradaDoConselheiro, ofertaDeFerro(2));
		assertEquals("A entrega vale 1 voto", 1, jogador.getVotos());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void entregarTesouros_primeiroAEntregar_recebeBonusDoConselheiro() {
		levarAoConselheiroComFerro(2);
		PecaBonus recebida = jogador.entregarTesouros(paradaDoConselheiro, ofertaDeFerro(2));
		assertEquals("Bônus do conselheiro", PecaBonus.MOEDAS, recebida);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void entregarTesouros_retiraOsTesourosDoJogador() {
		levarAoConselheiroComFerro(3);
		jogador.entregarTesouros(paradaDoConselheiro, ofertaDeFerro(2));
		assertEquals("Deveria sobrar 1 ferro", 1, jogador.getQuantidade(TipoTesouro.FERRO));
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void entregarTesouros_semOsTesourosOfertados_lancaExcecao() {
		levarAoConselheiroComFerro(1);
		jogador.entregarTesouros(paradaDoConselheiro, ofertaDeFerro(2));
	}

	// CIDADES

	@Test(timeout = DEFAULT_TIMEOUT)
	public void atacarCidade_primeiroTesouro_custaUmaEspada() {
		Tabuleiro mapa = montarTabuleiroComCidadeJuntoDaVizinha();
		irAteAVizinhaComEspadas(3);
		jogador.atacarCidade(mapa, ID_CIDADE, tesouroPresente(mapa));
		assertEquals("3 espadas - 1 do primeiro tesouro", 2, jogador.getQuantidade(TipoTributo.ESPADA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void atacarCidade_segundoTesouroNoMesmoTurno_custaDuasEspadas() {
		Tabuleiro mapa = montarTabuleiroComCidadeJuntoDaVizinha();
		irAteAVizinhaComEspadas(3);
		jogador.atacarCidade(mapa, ID_CIDADE, tesouroPresente(mapa));
		jogador.atacarCidade(mapa, ID_CIDADE, tesouroPresente(mapa));
		assertEquals("3 espadas - 1 do primeiro - 2 do segundo", 0, jogador.getQuantidade(TipoTributo.ESPADA));
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void atacarCidade_semEspadasParaOSegundoTesouro_lancaExcecao() {
		Tabuleiro mapa = montarTabuleiroComCidadeJuntoDaVizinha();
		irAteAVizinhaComEspadas(2);
		jogador.atacarCidade(mapa, ID_CIDADE, tesouroPresente(mapa));
		jogador.atacarCidade(mapa, ID_CIDADE, tesouroPresente(mapa));
	}

	// FIM DE TURNO

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void encerrarTurno_paradaSimplesOcupadaPorOutroPeao_lancaExcecao() {
		new Peao(COR_DO_ADVERSARIO, vizinha);
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(vizinha);
		jogador.encerrarTurno();
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void encerrarTurno_khanNaoUsado_lancaExcecao() {
		jogador.ativarColuna(Coluna.TERCEIRA);
		jogador.encerrarTurno();
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void encerrarTurno_moedaVirtualNaoAcumula() {
		jogarTurnoSemAgir(Coluna.SEGUNDA);
		assertEquals("A moeda virtual some no fim do turno",
				MOEDAS_INICIAIS, jogador.getDisponivel(TipoTributo.MOEDA));
	}

	// HERÓIS

	@Test(timeout = DEFAULT_TIMEOUT)
	public void novoJogador_tolui_comecaComYurtEmKarakorum() {
		new Jogador(Cor.PRETO, Heroi.TOLUI, inicio, 1);
		assertTrue("Karakorum deveria ter o yurt inicial do Tolui", inicio.temYurt());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void novoJogador_altani_temDoisPeoes() {
		Jogador altani = new Jogador(Cor.AMARELO, Heroi.ALTANI, inicio, 1);
		assertEquals("Altani joga com 2 peões", 2, altani.getPeoes().size());
	}

	// AUXILIARES

	/** Ativa uma coluna sem ícone do Khan e encerra o turno em seguida. */
	private void jogarTurnoSemAgir(Coluna coluna) {
		jogador.ativarColuna(coluna);
		jogador.encerrarTurno();
	}

	/** Põe yurts no caminho para o peão chegar ao conselheiro com um único movimento. */
	private void levarAoConselheiroComFerro(int quantidade) {
		for (int i = 0; i < quantidade; i++) {
			jogador.receberTesouro(TipoTesouro.FERRO);
		}
		vizinha.adicionarYurt(new Yurt(COR_DO_JOGADOR));
		distante.adicionarYurt(new Yurt(COR_DO_JOGADOR));
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(paradaDoConselheiro);
	}

	private void irAteAParadaDuplaComTributosDeYurt(int tributos) {
		for (int i = 0; i < tributos; i++) {
			jogador.receberTributo(TipoTributo.YURT);
		}
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(paradaDupla);
	}

	private Map<TipoTesouro, Integer> ofertaDeFerro(int quantidade) {
		Map<TipoTesouro, Integer> oferta = new HashMap<TipoTesouro, Integer>();
		oferta.put(TipoTesouro.FERRO, quantidade);
		return oferta;
	}

	/** Área de melhorias com a peça dada no espaço 0 e nenhum ícone. */
	private AreaMelhorias areaCom(Melhoria peca) {
		AreaMelhorias area = new AreaMelhorias(Arrays.asList(peca), new HashMap<Integer, TipoTributo>());
		area.preparar();
		return area;
	}

	/** Tabuleiro com 3 cidades (todas visíveis, 4 tesouros cada); a de id ID_CIDADE fica junto da vizinha. */
	private Tabuleiro montarTabuleiroComCidadeJuntoDaVizinha() {
		Tabuleiro mapa = new Tabuleiro(new Random(1));
		mapa.adicionarParada(vizinha);
		mapa.adicionarCidade(new Cidade(ID_CIDADE, Regiao.CHINA));
		mapa.adicionarCidade(new Cidade(ID_CIDADE + 1, Regiao.RUSSIA));
		mapa.adicionarCidade(new Cidade(ID_CIDADE + 2, Regiao.PERSIA));
		mapa.ligarCidadeParada(ID_CIDADE, vizinha.getId());
		mapa.preparar();
		return mapa;
	}

	private void irAteAVizinhaComEspadas(int espadas) {
		for (int i = 0; i < espadas; i++) {
			jogador.receberTributo(TipoTributo.ESPADA);
		}
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(vizinha);
	}

	/** Um tipo de tesouro que a cidade de teste ainda tem. */
	private TipoTesouro tesouroPresente(Tabuleiro mapa) {
		Cidade alvo = mapa.getCidade(ID_CIDADE);
		for (TipoTesouro tipo : TipoTesouro.values()) {
			if (alvo.getQuantidade(tipo) > 0) {
				return tipo;
			}
		}
		throw new IllegalStateException("Cidade de teste sem tesouros");
	}

}
