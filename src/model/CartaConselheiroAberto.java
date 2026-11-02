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
	private String id = null; //nome do arquivo da imagem da carta (ex.: "yurts_6")

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

	/** Define o id da carta (o mesmo nome do arquivo em resources/cartas/abertas). Devolve a própria carta. */
	CartaConselheiroAberto comId(String id) {
		if (id == null || id.isEmpty()) {
			throw new IllegalArgumentException("Id da carta vazio");
		}
		this.id = id;
		return this;
	}

	/** Id usado pela view para achar a imagem da carta. */
	String getId() {
		if (id == null) {
			throw new IllegalStateException("Carta sem id");
		}
		return id;
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
		baralho.add(new CartaTerEspadas(4).comId("espadas_4"));
		baralho.add(new CartaYurtsNoMapa(6).comId("yurts_6"));
		baralho.add(new CartaYurtsNoMapa(10).comId("yurts_10"));
		baralho.add(new CartaTerTesouros(4).comId("tesouros_4"));
		baralho.add(new CartaTesourosDiferentes(5).comId("tesouros_diferentes_5"));
		baralho.add(new CartaYurtsPorRegiao(1).comId("yurts_regiao_1"));
		baralho.add(new CartaYurtsPorRegiao(2).comId("yurts_regiao_2"));
		baralho.add(new CartaPecasBonus(2).comId("bonus_2"));
		baralho.add(new CartaPecasBonus(3).comId("bonus_3"));
		baralho.add(new CartaCidadesConquistadas(2).comId("cidades_2"));
		baralho.add(new CartaCidadesConquistadas(3).comId("cidades_3"));
		baralho.add(new CartaLinhaDeMelhorias().comId("linha_melhorias"));
		baralho.add(new CartaColunasComMelhorias(2, 2).comId("colunas_2_melhorias_2"));
		baralho.add(new CartaColunasComMelhorias(1, 4).comId("melhorias_coluna_4"));
		return baralho;
	}

}