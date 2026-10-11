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
		return coluna.isBranca() ? LINHAS - heroi.getEspacosBrancosOcupados() : LINHAS;
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
		if (linhaCompleta(linha) && linha >= heroi.getEspacosBrancosOcupados()) {
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

	/** Melhorias das quatro colunas, inclusive da branca. */
	List<Melhoria> getTodasMelhorias() {
		List<Melhoria> todas = new LinkedList<Melhoria>();
		for (Coluna c : Coluna.values()) {
			todas.addAll(melhorias.get(c));
		}
		return todas;
	}

}
