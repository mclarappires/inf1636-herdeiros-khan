package model;

/** Ter uma quantidade de yurts no mapa, contando os de conquista nas cidades. */
class CartaYurtsNoMapa extends CartaConselheiroAberto {

	CartaYurtsNoMapa(int quantidade) {
		super(quantidade);
	}

	@Override
	int medir(Jogador j, Tabuleiro t) {
		return ContagemDeYurts.noMapa(j.getCor(), t);
	}

}
