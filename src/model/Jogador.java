package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Jogador {

	static final int YURTS_INICIAIS = 12;
	static final int CUSTO_MELHORIA = 2; //em moedas

	private final cor cor;
	private final Heroi heroi;
	private int votos = 0;
	private int yurtsDisponiveis = YURTS_INICIAIS;

	private final Map<tipoTesouro, Integer> tesouros = new HashMap<tipoTesouro, Integer>();
	private final Map<tipoTributo, Integer> tributos = new HashMap<tipoTributo, Integer>();

	private final Map<Coluna, PecaAtivacao> pecasAtivacao = new HashMap<Coluna, PecaAtivacao>();
	private final TabuleiroJogador tabuleiroJogador;
	private final List<Peao> peoes = new LinkedList<Peao>();

	private final Map<pecaBonus, Boolean> bonus = new HashMap<pecaBonus, Boolean>(); //peça -> já foi usada
	private CartaConselheiroSecreto cartaSecreta = null;

	//ESTADO DO TURNO (vale da ativação da coluna até encerrarTurno)

	private Coluna colunaAtiva = null;
	private Peao peaoAtivo = null;
	private int movimentosRestantes = 0;
	private int punhosRestantes = 0;
	private boolean khanPendente = false;

	//recursos dados por ícones e melhorias: não acumulam de um turno para o outro
	private final Map<tipoTributo, Integer> tributosVirtuais = new HashMap<tipoTributo, Integer>();
	private final Map<tipoTesouro, Integer> tesourosVirtuais = new HashMap<tipoTesouro, Integer>();

	private final Set<parada> paradasVisitadas = new HashSet<parada>();
	private final Map<Integer, Integer> tesourosTomadosNoTurno = new HashMap<Integer, Integer>(); //por id de cidade

	Jogador(cor cor, Heroi heroi, karakorum inicial, int moedasIniciais){
		if (cor == null) {
			throw new IllegalArgumentException("Jogador precisa de uma cor");
		}
		if (heroi == null) {
			throw new IllegalArgumentException("Jogador precisa de um herói");
		}
		if (inicial == null) {
			throw new IllegalArgumentException("Jogador precisa começar em Karakorum");
		}
		if (moedasIniciais < 0) {
			throw new IllegalArgumentException("Moedas iniciais negativas");
		}
		this.cor = cor;
		this.heroi = heroi;
		this.tabuleiroJogador = new TabuleiroJogador(heroi);

		for (Coluna c : Coluna.values()) {
			pecasAtivacao.put(c, new PecaAtivacao(c));
		}
		for (int i = 0; i < heroi.getQuantidadePeoes(); i++) {
			peoes.add(new Peao(cor, inicial));
		}
		somar(tributos, tipoTributo.MOEDA, moedasIniciais);

		//Tolui começa com um yurt em Karakorum
		if (heroi == Heroi.TOLUI) {
			inicial.colocarYurtInicial(new yurt(cor));
			yurtsDisponiveis--;
		}
	}

	//AÇÕES DO TURNO

	void ativarColuna(Coluna coluna) {
		ativarColuna(coluna, 0);
	}

	/** Coloca a peça de ativação na coluna e recebe as ações dela. Altani escolhe qual peão age. */
	void ativarColuna(Coluna coluna, int indicePeao) {
		if (coluna == null) {
			throw new IllegalArgumentException("Coluna nula");
		}
		if (indicePeao < 0 || indicePeao >= peoes.size()) {
			throw new IllegalArgumentException("Peão inexistente: " + indicePeao);
		}
		if (colunaAtiva != null) {
			throw new IllegalStateException("Já existe uma coluna ativada neste turno");
		}
		pecasAtivacao.get(coluna).usar();

		colunaAtiva = coluna;
		peaoAtivo = peoes.get(indicePeao);
		paradasVisitadas.add(peaoAtivo.getPosicao());

		IconesBase icones = heroi.getIconesBase(coluna);
		movimentosRestantes = icones.getCavalos();
		punhosRestantes = icones.getPunhos();
		somar(tributosVirtuais, tipoTributo.MOEDA, icones.getMoedas());
		khanPendente = icones.temKhan();

		for (Melhoria m : tabuleiroJogador.getMelhorias(coluna)) {
			m.aplicar(this);
		}

		if (icones.temRecuperar()) {
			for (PecaAtivacao peca : pecasAtivacao.values()) {
				peca.recuperar();
			}
		}
	}

	/** Gasta um movimento para levar o peão até a parada. */
	void mover(parada destino) {
		exigirTurnoAtivo();
		if (destino == null) {
			throw new IllegalArgumentException("Parada nula");
		}
		if (movimentosRestantes <= 0) {
			throw new IllegalStateException("Sem movimentos restantes");
		}
		if (!alcancavel(peaoAtivo.getPosicao(), destino)) {
			throw new IllegalArgumentException(
					"Parada " + destino.getId() + " não é alcançável com um movimento");
		}
		peaoAtivo.moverPara(destino);
		movimentosRestantes--;
		paradasVisitadas.add(destino);
	}

	/** Um movimento leva a uma parada adjacente; paradas com yurt do próprio jogador são puladas sem custo. */
	private boolean alcancavel(parada origem, parada destino) {
		if (destino == origem) {
			return false;
		}
		Set<parada> vistas = new HashSet<parada>();
		LinkedList<parada> fila = new LinkedList<parada>();
		vistas.add(origem);
		fila.add(origem);
		while (!fila.isEmpty()) {
			for (parada vizinha : fila.removeFirst().getAdjacentes()) {
				if (vizinha == destino) {
					return true;
				}
				if (vizinha.temYurtDe(cor) && vistas.add(vizinha)) {
					fila.add(vizinha);
				}
			}
		}
		return false;
	}

	/** Gasta um punho para pegar um tributo de uma província adjacente a uma parada visitada. */
	void pegarTributo(Provincia provincia) {
		exigirTurnoAtivo();
		if (provincia == null) {
			throw new IllegalArgumentException("Província nula");
		}
		if (punhosRestantes <= 0) {
			throw new IllegalStateException("Sem ações de pegar tributo restantes");
		}
		if (!visitouParadaJuntoDe(provincia)) {
			throw new IllegalArgumentException("Província " + provincia.getId()
					+ " não é adjacente a nenhuma parada visitada no turno");
		}
		somar(tributos, provincia.retirarTributo(), 1);
		punhosRestantes--;
	}

	/**
	 * Toma um tesouro de uma cidade adjacente a uma parada visitada. O primeiro tesouro de
	 * cada cidade no turno custa 1 espada e os seguintes 2. Devolve true se a cidade foi conquistada.
	 */
	boolean atacarCidade(tabuleiro tabuleiro, int idCidade, tipoTesouro tipo) {
		exigirTurnoAtivo();
		if (tabuleiro == null) {
			throw new IllegalArgumentException("Tabuleiro nulo");
		}
		cidade alvo = tabuleiro.getCidade(idCidade);
		if (!visitouParadaJuntoDe(alvo)) {
			throw new IllegalArgumentException("Cidade " + idCidade
					+ " não é adjacente a nenhuma parada visitada no turno");
		}

		int jaTomados = contar(tesourosTomadosNoTurno, idCidade);
		int custo = cidade.custoEmEspadas(jaTomados + 1) - cidade.custoEmEspadas(jaTomados);
		tipoTributo pagamento = tipoTributo.ESPADA;
		//Ogedei pode pagar com uma moeda o primeiro tesouro de uma cidade
		if (heroi == Heroi.OGEDEI && jaTomados == 0 && getDisponivel(tipoTributo.ESPADA) < custo) {
			pagamento = tipoTributo.MOEDA;
		}
		if (getDisponivel(pagamento) < custo) {
			throw new IllegalStateException("Faltam espadas para atacar a cidade " + idCidade);
		}

		boolean conquistou = tabuleiro.tomarTesouroDeCidade(idCidade, tipo);
		pagar(pagamento, custo);
		somar(tesouros, tipo, 1);
		somar(tesourosTomadosNoTurno, idCidade, 1);

		if (conquistou && yurtsDisponiveis > 0) {
			alvo.colocarYurtDeConquista(new yurt(cor));
			yurtsDisponiveis--;
		}
		return conquistou;
	}

	/** Gasta um tributo de yurt para construir em uma parada visitada no turno. */
	void construirYurt(parada local) {
		exigirTurnoAtivo();
		if (local == null) {
			throw new IllegalArgumentException("Parada nula");
		}
		if (yurtsDisponiveis == 0) {
			throw new IllegalStateException("Jogador não tem mais yurts");
		}
		if (!podeConstruirEm(local)) {
			throw new IllegalArgumentException(
					"Parada " + local.getId() + " não foi visitada neste turno");
		}
		if (!local.podeConstruirYurt()) {
			throw new IllegalStateException("Parada " + local.getId() + " não aceita mais yurts");
		}
		pagar(tipoTributo.YURT, 1);
		local.adicionarYurt(new yurt(cor));
		yurtsDisponiveis--;
	}

	private boolean podeConstruirEm(parada local) {
		if (paradasVisitadas.contains(local)) {
			return true;
		}
		//Tolui também constrói em paradas adjacentes ao seu peão
		return heroi == Heroi.TOLUI && peaoAtivo.getPosicao().getAdjacentes().contains(local);
	}

	/**
	 * Paga 2 moedas e coloca a melhoria na coluna da cor dela. Como a coluna do turno já foi
	 * ativada, a melhoria só passa a valer nos turnos seguintes.
	 */
	void comprarMelhoria(Melhoria m) {
		exigirTurnoAtivo();
		if (m == null) {
			throw new IllegalArgumentException("Melhoria nula");
		}
		if (getDisponivel(tipoTributo.MOEDA) < CUSTO_MELHORIA) {
			throw new IllegalStateException(
					"São necessárias " + CUSTO_MELHORIA + " moedas para comprar uma melhoria");
		}
		tabuleiroJogador.colocarMelhoria(m, m.getColuna());
		pagar(tipoTributo.MOEDA, CUSTO_MELHORIA);
	}

	/** Coloca na coluna branca a melhoria ganha ao completar uma linha do tabuleiro do jogador. */
	void colocarMelhoriaGratuita(Melhoria m) {
		tabuleiroJogador.colocarMelhoria(m, Coluna.BRANCA);
	}

	/**
	 * Entrega tesouros ao conselheiro da parada em que o peão está e recebe os votos.
	 * Devolve a peça de bônus do conselheiro, ou null se outro jogador já a pegou.
	 */
	pecaBonus entregarTesouros(paradaConselheiro local, Map<tipoTesouro, Integer> oferta) {
		exigirTurnoAtivo();
		if (local == null) {
			throw new IllegalArgumentException("Parada nula");
		}
		if (oferta == null) {
			throw new IllegalArgumentException("Oferta nula");
		}
		if (peaoAtivo.getPosicao() != local) {
			throw new IllegalStateException("O peão não está na parada " + local.getId());
		}
		Map<tipoTesouro, Integer> reais = separarTesourosReais(oferta);
		Conselheiro conselheiro = local.getConselheiro();
		if (!conselheiro.aceita(oferta)) {
			throw new IllegalArgumentException("O conselheiro não aceita essa oferta");
		}

		for (Map.Entry<tipoTesouro, Integer> e : oferta.entrySet()) {
			int doJogador = contar(reais, e.getKey());
			somar(tesouros, e.getKey(), -doJogador);
			somar(tesourosVirtuais, e.getKey(), doJogador - e.getValue());
		}

		votos += conselheiro.getVotosPorEntrega();
		pecaBonus peca = conselheiro.coletarBonus();
		if (peca != null) {
			bonus.put(peca, false);
			if (peca == pecaBonus.VOTO) { //o voto extra é imediato
				usarBonus(peca);
			}
		}
		return peca;
	}

	/**
	 * Calcula quanto de cada tipo da oferta sai dos tesouros do jogador. Os tesouros virtuais
	 * do turno cobrem o resto, mas toda entrega precisa de pelo menos 1 tesouro real.
	 */
	private Map<tipoTesouro, Integer> separarTesourosReais(Map<tipoTesouro, Integer> oferta) {
		Map<tipoTesouro, Integer> reais = new HashMap<tipoTesouro, Integer>();
		tipoTesouro trocavel = null; //tipo coberto só por virtual, mas que o jogador também tem de verdade

		for (Map.Entry<tipoTesouro, Integer> e : oferta.entrySet()) {
			tipoTesouro tipo = e.getKey();
			if (tipo == null || e.getValue() == null || e.getValue() <= 0) {
				throw new IllegalArgumentException("Oferta inválida: " + e);
			}
			int doJogador = e.getValue() - Math.min(contar(tesourosVirtuais, tipo), e.getValue());
			if (contar(tesouros, tipo) < doJogador) {
				throw new IllegalStateException("Faltam tesouros de " + tipo);
			}
			if (doJogador > 0) {
				reais.put(tipo, doJogador);
			} else if (contar(tesouros, tipo) > 0) {
				trocavel = tipo;
			}
		}

		if (reais.isEmpty()) {
			if (trocavel == null) {
				throw new IllegalStateException("A entrega precisa de pelo menos 1 tesouro real");
			}
			reais.put(trocavel, 1);
		}
		return reais;
	}

	/** Usa uma peça de bônus, que só vale uma vez. */
	void usarBonus(pecaBonus peca) {
		exigirTurnoAtivo();
		Boolean usada = bonus.get(peca);
		if (usada == null) {
			throw new IllegalStateException("Jogador não tem a peça de bônus " + peca);
		}
		if (usada) {
			throw new IllegalStateException("Peça de bônus " + peca + " já foi usada");
		}
		bonus.put(peca, true);

		switch (peca) {
		case VOTO:
			votos++;
			break;
		case ESPADAS:
			somar(tributos, tipoTributo.ESPADA, 2);
			break;
		case MOEDAS:
			somar(tributos, tipoTributo.MOEDA, 2);
			break;
		case YURTS:
			somar(tributos, tipoTributo.YURT, 2);
			break;
		case MOVIMENTOS:
			movimentosRestantes += 3;
			punhosRestantes += 1;
			break;
		case ACOES:
			punhosRestantes += 2;
			break;
		case TESOUROS:
			//quem controla a reserva sorteia os 3 tesouros e entrega o escolhido por receberTesouro
			break;
		}
	}

	/** O Khan é obrigatório quando a coluna ativada tem o ícone dele. */
	void registrarUsoDoKhan() {
		exigirTurnoAtivo();
		if (!khanPendente) {
			throw new IllegalStateException("Não há ação do Khan a realizar neste turno");
		}
		khanPendente = false;
	}

	void encerrarTurno() {
		exigirTurnoAtivo();
		if (khanPendente) {
			throw new IllegalStateException("O Khan precisa ser usado antes do fim do turno");
		}
		parada posicao = peaoAtivo.getPosicao();
		if (!peaoAtivo.podeTerminarEm(posicao)) {
			throw new IllegalStateException(
					"O peão não pode terminar o turno na parada " + posicao.getId());
		}

		//Chagatai guarda para os turnos seguintes as espadas que recebeu e não usou
		int espadasGuardadas = heroi == Heroi.CHAGATAI ? contar(tributosVirtuais, tipoTributo.ESPADA) : 0;
		tributosVirtuais.clear();
		somar(tributosVirtuais, tipoTributo.ESPADA, espadasGuardadas);

		tesourosVirtuais.clear();
		paradasVisitadas.clear();
		tesourosTomadosNoTurno.clear();
		movimentosRestantes = 0;
		punhosRestantes = 0;
		colunaAtiva = null;
		peaoAtivo = null;
	}

	//RECEBIMENTOS

	void adicionarVotos(int quantidade) {
		if (quantidade <= 0) {
			throw new IllegalArgumentException("Quantidade de votos deve ser positiva");
		}
		votos += quantidade;
	}

	void receberTesouro(tipoTesouro tipo) {
		if (tipo == null) {
			throw new IllegalArgumentException("Tesouro nulo");
		}
		somar(tesouros, tipo, 1);
	}

	void receberTributo(tipoTributo tipo) {
		if (tipo == null) {
			throw new IllegalArgumentException("Tributo nulo");
		}
		somar(tributos, tipo, 1);
	}

	void receberTributoVirtual(tipoTributo tipo) {
		somar(tributosVirtuais, tipo, 1);
	}

	void receberTesouroVirtual(tipoTesouro tipo) {
		somar(tesourosVirtuais, tipo, 1);
	}

	void receberMovimentos(int quantidade) {
		movimentosRestantes += quantidade;
	}

	void receberPunhos(int quantidade) {
		punhosRestantes += quantidade;
	}

	void definirCartaSecreta(CartaConselheiroSecreto carta) {
		if (carta == null) {
			throw new IllegalArgumentException("Carta nula");
		}
		cartaSecreta = carta;
	}

	//CONSULTAS

	cor getCor() {
		return cor;
	}

	Heroi getHeroi() {
		return heroi;
	}

	int getVotos() {
		return votos;
	}

	int getYurtsDisponiveis() {
		return yurtsDisponiveis;
	}

	int getMoedas() {
		return getQuantidade(tipoTributo.MOEDA);
	}

	/** Peças de tributo que o jogador tem, sem os recursos virtuais do turno. */
	int getQuantidade(tipoTributo tipo) {
		return contar(tributos, tipo);
	}

	/** Quanto o jogador pode gastar agora: peças de tributo mais recursos virtuais do turno. */
	int getDisponivel(tipoTributo tipo) {
		return contar(tributos, tipo) + contar(tributosVirtuais, tipo);
	}

	int getQuantidade(tipoTesouro tipo) {
		return contar(tesouros, tipo);
	}

	/** Tesouros do tipo mais os tesouros virtuais do turno; usado pelas cartas abertas. */
	int getDisponivel(tipoTesouro tipo) {
		return contar(tesouros, tipo) + contar(tesourosVirtuais, tipo);
	}

	/** Tesouros não entregues; usado no desempate. */
	int getQuantidadeTesouros() {
		int total = 0;
		for (Integer q : tesouros.values()) {
			total += q;
		}
		return total;
	}

	int getMovimentosRestantes() {
		return movimentosRestantes;
	}

	int getPunhosRestantes() {
		return punhosRestantes;
	}

	boolean isKhanPendente() {
		return khanPendente;
	}

	Coluna getColunaAtiva() {
		return colunaAtiva;
	}

	PecaAtivacao getPecaAtivacao(Coluna coluna) {
		return pecasAtivacao.get(coluna);
	}

	TabuleiroJogador getTabuleiroJogador() {
		return tabuleiroJogador;
	}

	List<Peao> getPeoes() {
		return Collections.unmodifiableList(peoes);
	}

	Set<pecaBonus> getBonus() {
		return Collections.unmodifiableSet(bonus.keySet());
	}

	CartaConselheiroSecreto getCartaSecreta() {
		return cartaSecreta;
	}

	//AUXILIARES

	private void exigirTurnoAtivo() {
		if (colunaAtiva == null) {
			throw new IllegalStateException("Nenhuma coluna foi ativada neste turno");
		}
	}

	private boolean visitouParadaJuntoDe(Provincia provincia) {
		for (parada p : paradasVisitadas) {
			if (provincia.adjacenteA(p)) {
				return true;
			}
		}
		return false;
	}

	private boolean visitouParadaJuntoDe(cidade alvo) {
		for (parada p : paradasVisitadas) {
			if (alvo.adjacenteA(p)) {
				return true;
			}
		}
		return false;
	}

	/** Gasta primeiro os recursos virtuais do turno e depois as peças de tributo. */
	private void pagar(tipoTributo tipo, int custo) {
		if (getDisponivel(tipo) < custo) {
			throw new IllegalStateException("Faltam tributos de " + tipo);
		}
		int virtuais = Math.min(contar(tributosVirtuais, tipo), custo);
		somar(tributosVirtuais, tipo, -virtuais);
		somar(tributos, tipo, virtuais - custo);
	}

	private static <K> int contar(Map<K, Integer> mapa, K chave) {
		Integer q = mapa.get(chave);
		return q == null ? 0 : q;
	}

	private static <K> void somar(Map<K, Integer> mapa, K chave, int delta) {
		int novo = contar(mapa, chave) + delta;
		if (novo == 0) {
			mapa.remove(chave);
		} else {
			mapa.put(chave, novo);
		}
	}

}
