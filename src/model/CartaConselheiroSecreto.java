package model;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Objetivo secreto: no fim do jogo rende 1, 2 ou 3 votos conforme o nível atingido.
 * Cada subclasse só sabe medir o progresso do jogador; os níveis ficam aqui.
 */
abstract class CartaConselheiroSecreto {

	private final String nome;
	private final List<Integer> limiares; //o que é preciso medir para 1, 2 e 3 votos

	CartaConselheiroSecreto(String nome, int umVoto, int doisVotos, int tresVotos) {
		if (nome == null || nome.isEmpty()) {
			throw new IllegalArgumentException("Carta precisa de um nome");
		}
		if (umVoto <= 0 || doisVotos <= umVoto || tresVotos <= doisVotos) {
			throw new IllegalArgumentException("Limiares devem ser positivos e crescentes");
		}
		this.nome = nome;
		this.limiares = Collections.unmodifiableList(Arrays.asList(umVoto, doisVotos, tresVotos));
	}

	/** Quanto o jogador já tem daquilo que a carta pede. */
	abstract int medir(Jogador j, Tabuleiro t);

	String getNome() {
		return nome;
	}

	List<Integer> getLimiares() {
		return limiares;
	}

	/** Votos (de 0 a 3) que a carta rende ao jogador na contagem final. */
	int votosFinais(Jogador j, Tabuleiro t) {
		if (j == null || t == null) {
			throw new IllegalArgumentException("Jogador e tabuleiro são obrigatórios");
		}
		int medido = medir(j, t);
		int votos = 0;
		for (Integer limiar : limiares) {
			if (medido >= limiar) {
				votos++;
			}
		}
		return votos;
	}

	/** As 10 cartas secretas do jogo, sem embaralhar. */
	static List<CartaConselheiroSecreto> criarBaralho() {
		List<CartaConselheiroSecreto> baralho = new LinkedList<CartaConselheiroSecreto>();
		baralho.add(new CartaViajante());
		baralho.add(new CartaSoldado());
		baralho.add(new CartaColecionador());
		baralho.add(new CartaSacerdote());
		baralho.add(new CartaGovernador());
		baralho.add(new CartaBajulador());
		baralho.add(CartaCidadesVizinhas.prefeito());
		baralho.add(CartaCidadesVizinhas.cidadao());
		baralho.add(new CartaCavaleiro());
		baralho.add(new CartaMensageiro());
		return baralho;
	}

}
