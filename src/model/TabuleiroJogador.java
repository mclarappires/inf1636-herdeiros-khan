package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class TabuleiroJogador {

	static final int LINHAS = 4;

	private final Heroi heroi;

	//melhorias de cada coluna, preenchidas de baixo para cima
	private final Map<Coluna, List<Melhoria>> melhorias = new HashMap<Coluna, List<Melhoria>>();

	private int gratuitasPendentes = 0;

	TabuleiroJogador(Heroi heroi){
		if (heroi == null) {
			throw new IllegalArgumentException("Tabuleiro do jogador precisa de um herói");
		}
		this.heroi = heroi;
		for (Coluna c : Coluna.values()) {
			melhorias.put(c, new LinkedList<Melhoria>());
		}
	}

	Heroi getHeroi() {
		return heroi;
	}

	int getCapacidade(Coluna coluna) {
		if (coluna == null) {
			throw new IllegalArgumentException("Coluna nula");
		}
		return LINHAS;
	}

	/**
	 * Melhoria escolhida na preparação para o primeiro espaço da coluna branca (só o Jochi).
	 * Não gasta melhoria gratuita.
	 */
	void colocarMelhoriaInicial(Melhoria m) {
		if (m == null) {
			throw new IllegalArgumentException("Melhoria nula");
		}
		List<Melhoria> branca = melhorias.get(Coluna.BRANCA);
		if (branca.size() >= heroi.getMelhoriasIniciaisNaBranca()) {
			throw new IllegalStateException("O herói " + heroi + " não tem melhoria inicial a colocar");
		}
		branca.add(m);
	}

	/**
	 * Coloca a melhoria no primeiro espaço livre da coluna. Colunas coloridas só aceitam
	 * melhorias da sua cor; a branca só aceita as gratuitas, ganhas ao completar uma linha.
	 */
	void colocarMelhoria(Melhoria m, Coluna c) {
		if (m == null) {
			throw new IllegalArgumentException("Melhoria nula");
		}
		List<Melhoria> daColuna = melhorias.get(c);
		if (daColuna == null) {
			throw new IllegalArgumentException("Coluna nula");
		}
		if (daColuna.size() >= getCapacidade(c)) {
			throw new IllegalStateException("Coluna " + c + " está cheia");
		}

		if (c.isBranca()) {
			if (gratuitasPendentes == 0) {
				throw new IllegalStateException("Não há melhoria gratuita a receber");
			}
			gratuitasPendentes--;
			daColuna.add(m);
			return;
		}

		if (m.getColuna() != c) {
			throw new IllegalArgumentException("Melhoria não tem a cor da coluna " + c);
		}
		daColuna.add(m);

		int linha = daColuna.size() - 1;
		//o espaço branco das linhas já ocupadas na preparação (Jochi) não dá melhoria grátis
		if (linhaCompleta(linha) && linha >= heroi.getMelhoriasIniciaisNaBranca()) {
			gratuitasPendentes++;
		}
	}

	/** Uma linha está completa quando as três colunas coloridas têm melhoria nela. */
	boolean linhaCompleta(int linha) {
		if (linha < 0 || linha >= LINHAS) {
			throw new IllegalArgumentException("Linha inexistente: " + linha);
		}
		for (Coluna c : Coluna.values()) {
			if (!c.isBranca() && melhorias.get(c).size() <= linha) {
				return false;
			}
		}
		return true;
	}

	int getMelhoriasGratuitasPendentes() {
		return gratuitasPendentes;
	}

	boolean temEspaco(Coluna coluna) {
		return contarMelhorias(coluna) < getCapacidade(coluna);
	}

	int contarMelhorias(Coluna coluna) {
		return getMelhorias(coluna).size();
	}

	/** Melhorias na coluna da região; definem os votos de influência no fim do jogo. */
	int contarMelhorias(Regiao r) {
		return contarMelhorias(Coluna.daRegiao(r));
	}

	List<Melhoria> getMelhorias(Coluna coluna) {
		List<Melhoria> daColuna = melhorias.get(coluna);
		if (daColuna == null) {
			throw new IllegalArgumentException("Coluna nula");
		}
		return Collections.unmodifiableList(daColuna);
	}

	//RESTAURAÇÃO (só para o LeitorPartida)

	/** Troca as melhorias da coluna pelas dadas, de baixo para cima, sem dar melhoria grátis. */
	void restaurarColuna(Coluna coluna, List<Melhoria> novas) {
		List<Melhoria> daColuna = melhorias.get(coluna);
		if (daColuna == null || novas == null) {
			throw new IllegalArgumentException("Coluna ou melhorias nulas");
		}
		if (novas.size() > getCapacidade(coluna)) {
			throw new IllegalArgumentException("Coluna " + coluna + " tem só " + LINHAS + " espaços");
		}
		for (Melhoria m : novas) {
			if (m == null) {
				throw new IllegalArgumentException("Melhoria nula");
			}
			if (!coluna.isBranca() && m.getColuna() != coluna) {
				throw new IllegalArgumentException("Melhoria não tem a cor da coluna " + coluna);
			}
		}
		daColuna.clear();
		daColuna.addAll(novas);
	}

	void definirGratuitasPendentes(int quantidade) {
		if (quantidade < 0 || quantidade > LINHAS) {
			throw new IllegalArgumentException("Melhorias gratuitas inválidas: " + quantidade);
		}
		gratuitasPendentes = quantidade;
	}

	/** Melhorias das quatro colunas, inclusive da branca. */
	List<Melhoria> getTodasMelhorias() {
		List<Melhoria> todas = new LinkedList<Melhoria>();
		for (Coluna c : Coluna.values()) {
			todas.addAll(melhorias.get(c));
		}
		return todas;
	}

}
