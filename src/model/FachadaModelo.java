package model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/** Único ponto de acesso ao model para view e controller; troca dados só por String e int. */
public class FachadaModelo {

	public static final int MIN_JOGADORES = Partida.MIN_JOGADORES;
	public static final int MAX_JOGADORES = Partida.MAX_JOGADORES;
	public static final int LINHAS_TABULEIRO_JOGADOR = TabuleiroJogador.LINHAS;

	/** Local do Khan antes do primeiro uso. */
	public static final int KHAN_FORA = -1;
	/** Local do Khan que indica a área de melhorias; os demais são ids de província. */
	public static final int KHAN_AREA_MELHORIAS = 0;

	/** Preço, em moedas, de uma melhoria da área. */
	public static final int CUSTO_MELHORIA = Jogador.CUSTO_MELHORIA;
	/** Quantos espaços tem a área de melhorias (numerados de 0, à esquerda, a ESPACOS-1). */
	public static final int ESPACOS_AREA_MELHORIAS = AreaMelhorias.ESPACOS;

	private static FachadaModelo instancia = null;

	private Partida partida = null;

	private FachadaModelo() {
	}

	public static FachadaModelo getInstancia() {
		if (instancia == null) {
			instancia = new FachadaModelo();
		}
		return instancia;
	}

	public String[] getHerois() {
		Heroi[] herois = Heroi.values();
		String[] nomes = new String[herois.length];
		for (int i = 0; i < herois.length; i++) {
			nomes[i] = herois[i].name();
		}
		return nomes;
	}

	/** Cor fixa do herói; a tela de seleção mostra antes de a partida existir. */
	public String getCorDoHeroi(String heroi) {
		return converterHeroi(heroi).getCor().name();
	}

	public String[] getColunas() {
		Coluna[] colunas = Coluna.values();
		String[] nomes = new String[colunas.length];
		for (int i = 0; i < colunas.length; i++) {
			nomes[i] = colunas[i].name();
		}
		return nomes;
	}

	/** O herói do jogador i fica na posição i da lista, na ordem de jogada; a cor vem do herói. */
	public void novaPartida(String caminhoMapa, List<String> herois) throws IOException {
		if (caminhoMapa == null) {
			throw new IllegalArgumentException("Caminho do mapa nulo");
		}
		List<Heroi> heroisEscolhidos = converterHerois(herois);

		Reader leitor = new InputStreamReader(new FileInputStream(caminhoMapa), "UTF-8");
		Tabuleiro tabuleiro;
		try {
			tabuleiro = CarregadorMapa.carregar(leitor, new Random());
		} finally {
			leitor.close();
		}
		//só troca a partida atual se a nova for válida
		partida = new Partida(tabuleiro, heroisEscolhidos);
	}

	public boolean temPartida() {
		return partida != null;
	}

	public int getQuantidadeJogadores() {
		return exigirPartida().getJogadores().size();
	}

	public String getHeroiDoJogador(int indice) {
		return exigirPartida().getJogador(indice).getHeroi().name();
	}

	public String getCorDoJogador(int indice) {
		return exigirPartida().getJogador(indice).getCor().name();
	}

	public int getMoedasDoJogador(int indice) {
		return exigirPartida().getJogador(indice).getMoedas();
	}

	/**
	 * Quanto do tributo ("MOEDA", "ESPADA" ou "YURT") o jogador pode gastar agora:
	 * peças mais os recursos virtuais do turno.
	 */
	public int getTributoDisponivel(int jogador, String tipo) {
		return exigirPartida().getJogador(jogador).getDisponivel(converterTributo(tipo));
	}

	//PEÕES

	public int getQuantidadePeoes(int jogador) {
		return exigirPartida().getJogador(jogador).getPeoes().size();
	}

	/** Id da parada onde está o peão (Altani tem dois; os demais, um). */
	public int getParadaDoPeao(int jogador, int indicePeao) {
		List<Peao> peoes = exigirPartida().getJogador(jogador).getPeoes();
		if (indicePeao < 0 || indicePeao >= peoes.size()) {
			throw new IllegalArgumentException("Peão inexistente: " + indicePeao);
		}
		return peoes.get(indicePeao).getPosicao().getId();
	}

	//TABULEIRO DO JOGADOR

	/** Se a peça de ativação já está na coluna (só volta quando a coluna branca é ativada). */
	public boolean isColunaUsada(int jogador, String coluna) {
		return exigirPartida().getJogador(jogador).getPecaAtivacao(converterColuna(coluna)).isUsada();
	}

	/** Espaços de melhoria da coluna; a branca do Jochi tem um a menos. */
	public int getCapacidadeDaColuna(int jogador, String coluna) {
		return exigirPartida().getJogador(jogador).getTabuleiroJogador().getCapacidade(converterColuna(coluna));
	}

	/** Símbolos das melhorias da coluna, de baixo para cima ("MOEDA", "PUNHO", "SEDA"...). */
	public String[] getSimbolosDasMelhorias(int jogador, String coluna) {
		List<Melhoria> daColuna = getMelhorias(jogador, coluna);
		String[] simbolos = new String[daColuna.size()];
		for (int i = 0; i < simbolos.length; i++) {
			simbolos[i] = daColuna.get(i).getSimbolo().name();
		}
		return simbolos;
	}

	/** Cor de cada melhoria (nome da coluna colorida de origem), na mesma ordem dos símbolos. */
	public String[] getCoresDasMelhorias(int jogador, String coluna) {
		List<Melhoria> daColuna = getMelhorias(jogador, coluna);
		String[] cores = new String[daColuna.size()];
		for (int i = 0; i < cores.length; i++) {
			cores[i] = daColuna.get(i).getColuna().name();
		}
		return cores;
	}

	private List<Melhoria> getMelhorias(int jogador, String coluna) {
		return exigirPartida().getJogador(jogador).getTabuleiroJogador().getMelhorias(converterColuna(coluna));
	}

	//YURTS

	/** Ids de todas as paradas do tabuleiro. */
	public int[] getIdsParadas() {
		Collection<Parada> paradas = exigirPartida().getTabuleiro().getParadas();
		int[] ids = new int[paradas.size()];
		int i = 0;
		for (Parada p : paradas) {
			ids[i++] = p.getId();
		}
		return ids;
	}

	/** Cor do dono de cada yurt construido na parada (vazio se nao ha yurt). */
	public String[] getCoresDosYurts(int idParada) {
		List<Yurt> yurts = exigirPartida().getTabuleiro().getParada(idParada).getYurts();
		String[] cores = new String[yurts.size()];
		for (int i = 0; i < cores.length; i++) {
			cores[i] = yurts.get(i).getDono().name();
		}
		return cores;
	}

	/**
	 * O jogador da vez gasta 1 peça de yurt para construir um yurt na parada. Devolve null se
	 * construiu, ou o motivo de não poder (parada não visitada, cheia, sem peças de yurt...).
	 */
	public String construirYurt(int idParada) {
		Partida p = exigirPartida();
		try {
			p.getJogadorDaVez().construirYurt(p.getTabuleiro().getParada(idParada));
			return null;
		} catch (IllegalStateException e) {
			return e.getMessage();
		} catch (IllegalArgumentException e) { //parada inexistente ou não visitada
			return e.getMessage();
		}
	}

	/** Peças de yurt que o jogador da vez pode gastar agora (peças mais yurts virtuais do turno). */
	public int getYurtsParaConstruir() {
		return exigirPartida().getJogadorDaVez().getDisponivel(TipoTributo.YURT);
	}

	//MELHORIAS À VENDA

	/** Símbolo ("MOEDA", "PUNHO", "SEDA"...) da melhoria no espaço da área, ou null se vazio. */
	public String getSimboloDaMelhoriaNaArea(int espaco) {
		Melhoria m = exigirPartida().getTabuleiro().getAreaMelhorias().getMelhoria(espaco);
		return m == null ? null : m.getSimbolo().name();
	}

	/** Nome da coluna colorida de origem da melhoria do espaço, ou null se vazio. */
	public String getCorDaMelhoriaNaArea(int espaco) {
		Melhoria m = exigirPartida().getTabuleiro().getAreaMelhorias().getMelhoria(espaco);
		return m == null ? null : m.getColuna().name();
	}

	public int getTamanhoDaPilhaDeMelhorias() {
		return exigirPartida().getTabuleiro().getAreaMelhorias().getTamanhoPilha();
	}

	/** Melhorias gratuitas que o jogador da vez ainda não escolheu (ganha uma por linha completa). */
	public int getMelhoriasGratuitasPendentes() {
		return exigirPartida().getJogadorDaVez().getTabuleiroJogador().getMelhoriasGratuitasPendentes();
	}

	/** Se o jogador da vez pode comprar agora a melhoria do espaço (para destacar no mapa). */
	public boolean podeComprarMelhoria(int espaco) {
		Partida p = exigirPartida();
		try {
			Melhoria m = p.getTabuleiro().getAreaMelhorias().getMelhoria(espaco);
			return m != null && podeComprar(p.getJogadorDaVez(), m);
		} catch (IllegalArgumentException e) { //espaço inexistente
			return false;
		}
	}

	/** Se o jogador da vez pode comprar agora a melhoria do topo da pilha. */
	public boolean podeComprarMelhoriaDoTopo() {
		Partida p = exigirPartida();
		AreaMelhorias area = p.getTabuleiro().getAreaMelhorias();
		return area.getTamanhoPilha() > 0 && podeComprar(p.getJogadorDaVez(), area.verTopo());
	}

	private static boolean podeComprar(Jogador j, Melhoria m) {
		return j.getColunaAtiva() != null
				&& j.getDisponivel(TipoTributo.MOEDA) >= Jogador.CUSTO_MELHORIA
				&& j.getTabuleiroJogador().temEspaco(m.getColuna());
	}

	/** Se o jogador da vez pode pegar de graça a melhoria do espaço. */
	public boolean podePegarMelhoriaGratuita(int espaco) {
		Partida p = exigirPartida();
		try {
			return p.getTabuleiro().getAreaMelhorias().getMelhoria(espaco) != null
					&& podePegarGratuita(p.getJogadorDaVez());
		} catch (IllegalArgumentException e) { //espaço inexistente
			return false;
		}
	}

	public boolean podePegarMelhoriaGratuitaDoTopo() {
		Partida p = exigirPartida();
		return p.getTabuleiro().getAreaMelhorias().getTamanhoPilha() > 0 && podePegarGratuita(p.getJogadorDaVez());
	}

	private static boolean podePegarGratuita(Jogador j) {
		TabuleiroJogador t = j.getTabuleiroJogador();
		return t.getMelhoriasGratuitasPendentes() > 0 && t.temEspaco(Coluna.BRANCA);
	}

	/** Compra por CUSTO_MELHORIA moedas a melhoria do espaço. Devolve null se comprou, ou o motivo. */
	public String comprarMelhoria(int espaco) {
		Partida p = exigirPartida();
		try {
			p.getJogadorDaVez().comprarMelhoria(p.getTabuleiro().getAreaMelhorias(), espaco);
			return null;
		} catch (IllegalStateException e) {
			return e.getMessage();
		} catch (IllegalArgumentException e) { //espaço inexistente
			return e.getMessage();
		}
	}

	/** Compra, sem ver antes, a melhoria do topo da pilha. Devolve null se comprou, ou o motivo. */
	public String comprarMelhoriaDoTopo() {
		Partida p = exigirPartida();
		try {
			p.getJogadorDaVez().comprarMelhoriaDoTopo(p.getTabuleiro().getAreaMelhorias());
			return null;
		} catch (IllegalStateException e) {
			return e.getMessage();
		} catch (IllegalArgumentException e) {
			return e.getMessage();
		}
	}

	/** Pega de graça a melhoria do espaço (vai para a coluna branca). Devolve null se pegou, ou o motivo. */
	public String pegarMelhoriaGratuita(int espaco) {
		Partida p = exigirPartida();
		try {
			p.getJogadorDaVez().pegarMelhoriaGratuita(p.getTabuleiro().getAreaMelhorias(), espaco);
			return null;
		} catch (IllegalStateException e) {
			return e.getMessage();
		} catch (IllegalArgumentException e) {
			return e.getMessage();
		}
	}

	/** Pega de graça a melhoria do topo da pilha. Devolve null se pegou, ou o motivo. */
	public String pegarMelhoriaGratuitaDoTopo() {
		Partida p = exigirPartida();
		try {
			p.getJogadorDaVez().pegarMelhoriaGratuitaDoTopo(p.getTabuleiro().getAreaMelhorias());
			return null;
		} catch (IllegalStateException e) {
			return e.getMessage();
		} catch (IllegalArgumentException e) {
			return e.getMessage();
		}
	}

	//PROVÍNCIAS E PEÇAS DE TRIBUTO

	public int[] getIdsProvincias() {
		Collection<Provincia> provincias = exigirPartida().getTabuleiro().getProvincias();
		int[] ids = new int[provincias.size()];
		int i = 0;
		for (Provincia p : provincias) {
			ids[i++] = p.getId();
		}
		return ids;
	}

	/** "ESPADA", "YURT" ou "MOEDA": o tipo de peça que a província produz. */
	public String getTipoDaProvincia(int idProvincia) {
		return exigirPartida().getTabuleiro().getProvincia(idProvincia).getTipo().name();
	}

	public int getTributosDaProvincia(int idProvincia) {
		return exigirPartida().getTabuleiro().getProvincia(idProvincia).getQuantidade();
	}

	//KHAN

	/** Ids das províncias onde o peão do Khan pode ser colocado. */
	public int[] getIdsProvinciasDoKhan() {
		List<Integer> doKhan = new LinkedList<Integer>();
		for (Provincia p : exigirPartida().getTabuleiro().getProvincias()) {
			if (p.isDoKhan()) {
				doKhan.add(p.getId());
			}
		}
		int[] ids = new int[doKhan.size()];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = doKhan.get(i);
		}
		return ids;
	}

	/** KHAN_FORA, KHAN_AREA_MELHORIAS ou o id da província onde o Khan está. */
	public int getLocalDoKhan() {
		Tabuleiro t = exigirPartida().getTabuleiro();
		LocalDoKhan local = t.getPeaoKhan().getLocal();
		if (local == null) {
			return KHAN_FORA;
		}
		if (local == t.getAreaMelhorias()) {
			return KHAN_AREA_MELHORIAS;
		}
		return ((Provincia) local).getId();
	}

	/** Se o jogador da vez pode mover o Khan para o local agora (para destacar no mapa). */
	public boolean podeUsarKhanEm(int local) {
		Partida p = exigirPartida();
		if (!p.getJogadorDaVez().isKhanPendente() || local == getLocalDoKhan()) {
			return false;
		}
		try {
			return local == KHAN_AREA_MELHORIAS || p.getTabuleiro().getProvincia(local).isDoKhan();
		} catch (IllegalArgumentException e) { //província inexistente
			return false;
		}
	}

	/**
	 * O jogador da vez move o Khan para o local (KHAN_AREA_MELHORIAS ou id de província do Khan).
	 * Devolve null se moveu, ou o motivo de não poder (sem Khan na coluna, mesmo lugar...).
	 */
	public String usarKhan(int local) {
		Partida p = exigirPartida();
		try {
			Tabuleiro t = p.getTabuleiro();
			LocalDoKhan destino = local == KHAN_AREA_MELHORIAS ? t.getAreaMelhorias() : t.getProvincia(local);
			p.getJogadorDaVez().usarKhan(t.getPeaoKhan(), destino);
			return null;
		} catch (IllegalStateException e) {
			return e.getMessage();
		} catch (IllegalArgumentException e) { //província inexistente
			return e.getMessage();
		}
	}

	//CIDADES

	/** Ids das cidades reveladas, isto é, as que podem ser atacadas agora. */
	public int[] getIdsCidadesVisiveis() {
		List<Cidade> visiveis = exigirPartida().getTabuleiro().getCidadesVisiveis();
		int[] ids = new int[visiveis.size()];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = visiveis.get(i).getId();
		}
		return ids;
	}

	/** Nomes dos tipos de tesouro, como no enum TipoTesouro. */
	public String[] getTiposDeTesouro() {
		TipoTesouro[] tipos = TipoTesouro.values();
		String[] nomes = new String[tipos.length];
		for (int i = 0; i < tipos.length; i++) {
			nomes[i] = tipos[i].name();
		}
		return nomes;
	}

	/** Quantos tesouros do tipo ainda estão sobre a cidade. */
	public int getTesourosDaCidade(int idCidade, String tipo) {
		return exigirPartida().getTabuleiro().getCidade(idCidade).getQuantidade(TipoTesouro.valueOf(tipo));
	}

	//TURNO

	/** Índice (na ordem de jogada) do jogador que está jogando. */
	public int getJogadorDaVez() {
		return exigirPartida().getIndiceDaVez();
	}

	/** Nome da coluna ativada neste turno, ou null se o jogador ainda não ativou nenhuma. */
	public String getColunaAtiva() {
		Coluna ativa = exigirPartida().getJogadorDaVez().getColunaAtiva();
		return ativa == null ? null : ativa.name();
	}

	public int getMovimentosRestantes() {
		return exigirPartida().getJogadorDaVez().getMovimentosRestantes();
	}

	public boolean isKhanPendente() {
		return exigirPartida().getJogadorDaVez().isKhanPendente();
	}

	/** Punhos que o jogador da vez ainda tem: cada um pega 1 peça de tributo. */
	public int getPunhosRestantes() {
		return exigirPartida().getJogadorDaVez().getPunhosRestantes();
	}

	/** Se o jogador da vez pode pegar agora uma peça da província (para destacar no mapa). */
	public boolean podePegarTributo(int idProvincia) {
		Partida p = exigirPartida();
		try {
			return p.getJogadorDaVez().podePegarTributo(p.getTabuleiro().getProvincia(idProvincia));
		} catch (IllegalArgumentException e) { //província inexistente
			return false;
		}
	}

	/**
	 * O jogador da vez gasta 1 punho para pegar 1 peça da província. Devolve null se pegou,
	 * ou o motivo de não poder (sem punhos, longe das paradas visitadas, província vazia...).
	 */
	public String pegarTributo(int idProvincia) {
		Partida p = exigirPartida();
		try {
			p.getJogadorDaVez().pegarTributo(p.getTabuleiro().getProvincia(idProvincia));
			return null;
		} catch (IllegalStateException e) {
			return e.getMessage();
		} catch (IllegalArgumentException e) { //província inexistente ou longe das paradas visitadas
			return e.getMessage();
		}
	}

	/** Ativa a coluna para o jogador da vez. Devolve false se não for possível (coluna já usada, turno em andamento...). */
	public boolean ativarColuna(String coluna, int indicePeao) {
		try {
			exigirPartida().getJogadorDaVez().ativarColuna(Coluna.valueOf(coluna), indicePeao);
			return true;
		} catch (IllegalStateException e) {
			return false;
		} catch (IllegalArgumentException e) { //nome de coluna desconhecido ou peão inexistente
			return false;
		}
	}

	/** Move o peão que age no turno para a parada, se o movimento valer. Falso não altera nada. */
	public boolean moverPeaoDaVez(int idParada) {
		Partida p = exigirPartida();
		try {
			Parada destino = p.getTabuleiro().getParada(idParada);
			Jogador jogador = p.getJogadorDaVez();
			if (!jogador.podeMoverPara(destino)) {
				return false;
			}
			jogador.mover(destino);
			return true;
		} catch (IllegalStateException e) {
			return false;
		} catch (IllegalArgumentException e) { //parada inexistente
			return false;
		}
	}

	/**
	 * Encerra o turno e passa a vez. Devolve null se passou, ou o motivo de o turno
	 * ainda não poder terminar (coluna não ativada, Khan pendente, parada cheia...).
	 */
	public String encerrarTurno() {
		try {
			exigirPartida().encerrarTurnoDaVez();
			return null;
		} catch (IllegalStateException e) {
			return e.getMessage();
		}
	}

	private Partida exigirPartida() {
		if (partida == null) {
			throw new IllegalStateException("Nenhuma partida foi iniciada");
		}
		return partida;
	}

	private static List<Heroi> converterHerois(List<String> nomes) {
		if (nomes == null) {
			throw new IllegalArgumentException("Lista de heróis nula");
		}
		List<Heroi> herois = new LinkedList<Heroi>();
		for (String nome : nomes) {
			herois.add(converterHeroi(nome));
		}
		return herois;
	}

	private static Heroi converterHeroi(String nome) {
		try {
			return Heroi.valueOf(nome);
		} catch (RuntimeException e) { //nome nulo ou desconhecido
			throw new IllegalArgumentException("Herói desconhecido: " + nome);
		}
	}

	private static Coluna converterColuna(String nome) {
		try {
			return Coluna.valueOf(nome);
		} catch (RuntimeException e) { //nome nulo ou desconhecido
			throw new IllegalArgumentException("Coluna desconhecida: " + nome);
		}
	}

	private static TipoTributo converterTributo(String nome) {
		try {
			return TipoTributo.valueOf(nome);
		} catch (RuntimeException e) { //nome nulo ou desconhecido
			throw new IllegalArgumentException("Tributo desconhecido: " + nome);
		}
	}
}