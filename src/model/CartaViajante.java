package model;

/** Viajante: maior grupo de yurts em paradas ligadas entre si (o grupo pode se ramificar). */
class CartaViajante extends CartaConselheiroSecreto {

	CartaViajante() {
		super("Viajante", 3, 6, 8);
	}

	@Override
	int medir(Jogador j, tabuleiro t) {
		return ContagemDeYurts.maiorGrupo(j.getCor(), t);
	}

}
