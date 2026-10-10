package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class JogadorTest {

	private static final int DEFAULT_TIMEOUT = 2000;
	private static final int MOEDAS_INICIAIS = 2;
	private static final int ID_CIDADE = 20;
	private static final cor COR_DO_JOGADOR = cor.AZUL;
	private static final cor COR_DO_ADVERSARIO = cor.VERMELHO;

	// Karakorum - vizinha - distante - paradaDoConselheiro
	private karakorum inicio;
	private parada vizinha;
	private parada distante;
	private paradaConselheiro paradaDoConselheiro;
	private parada paradaDupla; //ligada a Karakorum, aceita 2 peões e 2 yurts

	private Provincia provinciaDaVizinha;  //tributo de espada
	private Provincia provinciaDaDistante; //tributo de yurt

	// Ogedei: 1ª coluna = 2 cavalos + 1 punho; 2ª = 1 cavalo + 1 punho + 1 moeda;
	// 3ª = 1 cavalo + 2 punhos + Khan; branca = 2 cavalos + Khan + recuperar
	private Jogador jogador;

	@Before
	public void montarCenario() {
		inicio = new karakorum(0);
		vizinha = new parada(1, 1, 1, regiao.CHINA);
		distante = new parada(2, 1, 1, regiao.CHINA);

		Map<tipoTesouro, Integer> pedido = new HashMap<tipoTesouro, Integer>();
		pedido.put(tipoTesouro.FERRO, 2);
		paradaDoConselheiro = new paradaConselheiro(3, regiao.CHINA, new Conselheiro(pedido, 1, pecaBonus.MOEDAS));

		inicio.ligar(vizinha);
		vizinha.ligar(distante);
		distante.ligar(paradaDoConselheiro);

		paradaDupla = new parada(4, 2, 2, regiao.CHINA);
		inicio.ligar(paradaDupla);

		provinciaDaVizinha = new Provincia(10, tipoTributo.ESPADA);
		provinciaDaVizinha.ligarParada(vizinha);
		provinciaDaVizinha.adicionarTributo();

		provinciaDaDistante = new Provincia(11, tipoTributo.YURT);
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
				MOEDAS_INICIAIS + 1, jogador.getDisponivel(tipoTributo.MOEDA));
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
		vizinha.adicionarYurt(new yurt(COR_DO_JOGADOR));
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(distante);
		assertEquals("Pular a parada com yurt próprio não gasta movimento",
				1, jogador.getMovimentosRestantes());
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void mover_paradaComYurtDeOutroJogador_naoPodeSerPulada() {
		vizinha.adicionarYurt(new yurt(COR_DO_ADVERSARIO));
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(distante);
	}

	// TRIBUTOS

	@Test(timeout = DEFAULT_TIMEOUT)
	public void pegarTributo_provinciaAdjacenteAParadaVisitada_entraNoInventario() {
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(vizinha);
		jogador.pegarTributo(provinciaDaVizinha);
		assertEquals("Jogador deveria ter 1 espada", 1, jogador.getQuantidade(tipoTributo.ESPADA));
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void pegarTributo_provinciaLongeDasParadasVisitadas_lancaExcecao() {
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(vizinha);
		jogador.pegarTributo(provinciaDaDistante);
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void pegarTributo_provinciaDeParadaPuladaPorYurt_lancaExcecao() {
		vizinha.adicionarYurt(new yurt(COR_DO_JOGADOR));
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
		paradaDupla.adicionarYurt(new yurt(COR_DO_ADVERSARIO));
		irAteAParadaDuplaComTributosDeYurt(1);
		jogador.construirYurt(paradaDupla);
		assertTrue("A parada dupla deveria ter o yurt do jogador", paradaDupla.temYurtDe(COR_DO_JOGADOR));
	}

	// MELHORIAS

	@Test(timeout = DEFAULT_TIMEOUT)
	public void comprarMelhoria_consomeDuasMoedas() {
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.comprarMelhoria(new Melhoria(TipoMelhoria.PUNHO, Coluna.TERCEIRA));
		assertEquals("As 2 moedas deveriam ter sido gastas", 0, jogador.getMoedas());
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void comprarMelhoria_comUmaMoeda_lancaExcecao() {
		Jogador pobre = new Jogador(cor.VERDE, Heroi.OGEDEI, inicio, 1);
		pobre.ativarColuna(Coluna.PRIMEIRA);
		pobre.comprarMelhoria(new Melhoria(TipoMelhoria.PUNHO, Coluna.TERCEIRA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void comprarMelhoria_naoValeNoTurnoDaCompra() {
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.comprarMelhoria(new Melhoria(TipoMelhoria.DOIS_CAVALOS, Coluna.PRIMEIRA));
		assertEquals("Só os 2 cavalos da coluna valem neste turno", 2, jogador.getMovimentosRestantes());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void colocarMelhoria_completaLinhaDeTres_geraMelhoriaGratuita() {
		TabuleiroJogador tabuleiroDoOgedei = completarPrimeiraLinha(Heroi.OGEDEI);
		assertEquals("Linha completa rende 1 melhoria para a coluna branca",
				1, tabuleiroDoOgedei.getMelhoriasGratuitasPendentes());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void colocarMelhoria_jochiCompletaPrimeiraLinha_naoGeraMelhoriaGratuita() {
		TabuleiroJogador tabuleiroDoJochi = completarPrimeiraLinha(Heroi.JOCHI);
		assertEquals("O 1º espaço branco do Jochi já vem ocupado",
				0, tabuleiroDoJochi.getMelhoriasGratuitasPendentes());
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void colocarMelhoria_colunaDeOutraCor_lancaExcecao() {
		new TabuleiroJogador(Heroi.OGEDEI).colocarMelhoria(
				new Melhoria(TipoMelhoria.PUNHO, Coluna.PRIMEIRA), Coluna.SEGUNDA);
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
		pecaBonus recebida = jogador.entregarTesouros(paradaDoConselheiro, ofertaDeFerro(2));
		assertEquals("Bônus do conselheiro", pecaBonus.MOEDAS, recebida);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void entregarTesouros_retiraOsTesourosDoJogador() {
		levarAoConselheiroComFerro(3);
		jogador.entregarTesouros(paradaDoConselheiro, ofertaDeFerro(2));
		assertEquals("Deveria sobrar 1 ferro", 1, jogador.getQuantidade(tipoTesouro.FERRO));
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void entregarTesouros_semOsTesourosOfertados_lancaExcecao() {
		levarAoConselheiroComFerro(1);
		jogador.entregarTesouros(paradaDoConselheiro, ofertaDeFerro(2));
	}

	// CIDADES

	@Test(timeout = DEFAULT_TIMEOUT)
	public void atacarCidade_primeiroTesouro_custaUmaEspada() {
		tabuleiro mapa = montarTabuleiroComCidadeJuntoDaVizinha();
		irAteAVizinhaComEspadas(3);
		jogador.atacarCidade(mapa, ID_CIDADE, tesouroPresente(mapa));
		assertEquals("3 espadas - 1 do primeiro tesouro", 2, jogador.getQuantidade(tipoTributo.ESPADA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void atacarCidade_segundoTesouroNoMesmoTurno_custaDuasEspadas() {
		tabuleiro mapa = montarTabuleiroComCidadeJuntoDaVizinha();
		irAteAVizinhaComEspadas(3);
		jogador.atacarCidade(mapa, ID_CIDADE, tesouroPresente(mapa));
		jogador.atacarCidade(mapa, ID_CIDADE, tesouroPresente(mapa));
		assertEquals("3 espadas - 1 do primeiro - 2 do segundo", 0, jogador.getQuantidade(tipoTributo.ESPADA));
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalStateException.class)
	public void atacarCidade_semEspadasParaOSegundoTesouro_lancaExcecao() {
		tabuleiro mapa = montarTabuleiroComCidadeJuntoDaVizinha();
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
				MOEDAS_INICIAIS, jogador.getDisponivel(tipoTributo.MOEDA));
	}

	// HERÓIS

	@Test(timeout = DEFAULT_TIMEOUT)
	public void novoJogador_tolui_comecaComYurtEmKarakorum() {
		new Jogador(cor.PRETO, Heroi.TOLUI, inicio, 1);
		assertTrue("Karakorum deveria ter o yurt inicial do Tolui", inicio.temYurt());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void novoJogador_altani_temDoisPeoes() {
		Jogador altani = new Jogador(cor.AMARELO, Heroi.ALTANI, inicio, 1);
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
			jogador.receberTesouro(tipoTesouro.FERRO);
		}
		vizinha.adicionarYurt(new yurt(COR_DO_JOGADOR));
		distante.adicionarYurt(new yurt(COR_DO_JOGADOR));
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(paradaDoConselheiro);
	}

	private void irAteAParadaDuplaComTributosDeYurt(int tributos) {
		for (int i = 0; i < tributos; i++) {
			jogador.receberTributo(tipoTributo.YURT);
		}
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(paradaDupla);
	}

	private Map<tipoTesouro, Integer> ofertaDeFerro(int quantidade) {
		Map<tipoTesouro, Integer> oferta = new HashMap<tipoTesouro, Integer>();
		oferta.put(tipoTesouro.FERRO, quantidade);
		return oferta;
	}

	private TabuleiroJogador completarPrimeiraLinha(Heroi heroi) {
		TabuleiroJogador doHeroi = new TabuleiroJogador(heroi);
		doHeroi.colocarMelhoria(new Melhoria(TipoMelhoria.PUNHO, Coluna.PRIMEIRA), Coluna.PRIMEIRA);
		doHeroi.colocarMelhoria(new Melhoria(TipoMelhoria.PUNHO, Coluna.SEGUNDA), Coluna.SEGUNDA);
		doHeroi.colocarMelhoria(new Melhoria(TipoMelhoria.PUNHO, Coluna.TERCEIRA), Coluna.TERCEIRA);
		return doHeroi;
	}

	/** Tabuleiro com 3 cidades (todas visíveis, 4 tesouros cada); a de id ID_CIDADE fica junto da vizinha. */
	private tabuleiro montarTabuleiroComCidadeJuntoDaVizinha() {
		tabuleiro mapa = new tabuleiro(new Random(1));
		mapa.adicionarParada(vizinha);
		mapa.adicionarCidade(new cidade(ID_CIDADE, regiao.CHINA));
		mapa.adicionarCidade(new cidade(ID_CIDADE + 1, regiao.RUSSIA));
		mapa.adicionarCidade(new cidade(ID_CIDADE + 2, regiao.PERSIA));
		mapa.ligarCidadeParada(ID_CIDADE, vizinha.getId());
		mapa.preparar();
		return mapa;
	}

	private void irAteAVizinhaComEspadas(int espadas) {
		for (int i = 0; i < espadas; i++) {
			jogador.receberTributo(tipoTributo.ESPADA);
		}
		jogador.ativarColuna(Coluna.PRIMEIRA);
		jogador.mover(vizinha);
	}

	/** Um tipo de tesouro que a cidade de teste ainda tem. */
	private tipoTesouro tesouroPresente(tabuleiro mapa) {
		cidade alvo = mapa.getCidade(ID_CIDADE);
		for (tipoTesouro tipo : tipoTesouro.values()) {
			if (alvo.getQuantidade(tipo) > 0) {
				return tipo;
			}
		}
		throw new IllegalStateException("Cidade de teste sem tesouros");
	}

}
