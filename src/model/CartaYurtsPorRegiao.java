package model;

/** Ter pelo menos uma quantidade de yurts em cada uma das três regiões. */
class CartaYurtsPorRegiao extends CartaConselheiroAberto {

	CartaYurtsPorRegiao(int porRegiao) {
		super(porRegiao);
	}

	//a região com menos yurts decide se a carta foi cumprida
	@Override
	int medir(Jogador j, tabuleiro t) {
		int menor = Integer.MAX_VALUE;
		for (regiao r : regiao.values()) {
			menor = Math.min(menor, ContagemDeYurts.naRegiao(j.getCor(), t, r));
		}
		return menor;
	}

}
