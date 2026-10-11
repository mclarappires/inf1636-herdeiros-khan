package model;

import java.util.LinkedList;
import java.util.List;

/**
 * Objetivo aberto: o primeiro jogador que o cumprir no seu turno ganha 1 voto e a carta sai do jogo.
 * Cada subclasse só sabe medir o progresso do jogador; a comparação com o limite fica aqui.
 */
abstract class CartaConselheiroAberto {

	static final int VOTOS = 1;

	private final int limite;
	private boolean reivindicada = false;

	CartaConselheiroAberto(int limite) {
		if (limite <= 0) {
			throw new IllegalArgumentException("Limite deve ser positivo");
		}
		this.limite = limite;
	}

	/** Quanto o jogador já tem daquilo que a carta pede. */
	abstract int medir(Jogador j, Tabuleiro t);

	int getLimite() {
		return limite;
	}

	boolean cumprida(Jogador j, Tabuleiro t) {
		if (j == null || t == null) {
			throw new IllegalArgumentException("Jogador e tabuleiro são obrigatórios");
		}
		return medir(j, t) >= limite;
	}

	boolean isReivindicada() {
		return reivindicada;
	}

	/** Dá o voto da carta ao jogador que cumpriu o objetivo primeiro. */
	void reivindicar(Jogador j, Tabuleiro t) {
		if (reivindicada) {
			throw new IllegalStateException("Carta já foi reivindicada");
		}
		if (!cumprida(j, t)) {
			throw new IllegalStateException("O jogador não cumpriu o objetivo da carta");
		}
		reivindicada = true;
		j.adicionarVotos(VOTOS);
	}

	/** As 14 cartas abertas do jogo, sem embaralhar. */
	static List<CartaConselheiroAberto> criarBaralho() {
		List<CartaConselheiroAberto> baralho = new LinkedList<CartaConselheiroAberto>();
		baralho.add(new CartaTerEspadas(4));
		baralho.add(new CartaYurtsNoMapa(6));
		baralho.add(new CartaYurtsNoMapa(10));
		baralho.add(new CartaTerTesouros(4));
		baralho.add(new CartaTesourosDiferentes(5));
		baralho.add(new CartaYurtsPorRegiao(1));
		baralho.add(new CartaYurtsPorRegiao(2));
		baralho.add(new CartaPecasBonus(2));
		baralho.add(new CartaPecasBonus(3));
		baralho.add(new CartaCidadesConquistadas(2));
		baralho.add(new CartaCidadesConquistadas(3));
		baralho.add(new CartaLinhaDeMelhorias());
		baralho.add(new CartaColunasComMelhorias(2, 2));
		baralho.add(new CartaColunasComMelhorias(1, 4));
		return baralho;
	}

}
