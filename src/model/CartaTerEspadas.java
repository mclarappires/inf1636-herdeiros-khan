package model;

/** Ter uma quantidade de espadas (peças e espadas virtuais do turno); nada é gasto. */
class CartaTerEspadas extends CartaConselheiroAberto {

	CartaTerEspadas(int quantidade) {
		super(quantidade);
	}

	@Override
	int medir(Jogador j, Tabuleiro t) {
		return j.getDisponivel(TipoTributo.ESPADA);
	}

}
