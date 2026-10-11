package model;

/** Ter obtido uma quantidade de peças de bônus, usadas ou não. */
class CartaPecasBonus extends CartaConselheiroAberto {

	CartaPecasBonus(int quantidade) {
		super(quantidade);
	}

	@Override
	int medir(Jogador j, Tabuleiro t) {
		return j.getBonus().size();
	}

}
