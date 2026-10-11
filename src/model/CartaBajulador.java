package model;

/** Bajulador: províncias do Khan com pelo menos 2 yurts do jogador em paradas adjacentes. */
class CartaBajulador extends CartaConselheiroSecreto {

	static final int MINIMO_YURTS = 2;

	CartaBajulador() {
		super("Bajulador", 2, 3, 4);
	}

	@Override
	int medir(Jogador j, tabuleiro t) {
		int provincias = 0;
		for (Provincia p : t.getProvincias()) {
			if (p.isDoKhan()
					&& ContagemDeYurts.nasParadas(j.getCor(), p.getParadasAdjacentes()) >= MINIMO_YURTS) {
				provincias++;
			}
		}
		return provincias;
	}

}
