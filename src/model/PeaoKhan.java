package model;

import java.util.List;

/** Peão do Khan: fica fora do tabuleiro até o primeiro uso e nunca volta para onde está. */
class PeaoKhan {

	private LocalDoKhan local = null;

	/** Move o Khan e devolve os tributos que vão para quem o moveu. */
	List<TipoTributo> mover(LocalDoKhan novo) {
		if (novo == null) {
			throw new IllegalArgumentException("Destino do Khan nulo");
		}
		if (novo == local) {
			throw new IllegalStateException("O Khan precisa ir para um lugar diferente do atual");
		}
		List<TipoTributo> ganhos = novo.receberKhan();
		local = novo;
		return ganhos;
	}

	/** Lugar atual do Khan, ou null se ele ainda não entrou no tabuleiro. */
	LocalDoKhan getLocal() {
		return local;
	}

}
