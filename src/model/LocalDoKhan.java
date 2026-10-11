package model;

import java.util.List;

/** Lugar para onde o peão do Khan pode ser movido: uma província do Khan ou a área de melhorias. */
interface LocalDoKhan {

	/**
	 * Aplica o efeito de o Khan chegar aqui e devolve os tributos que vão para quem o moveu.
	 * Deve validar antes de mudar qualquer coisa, para um movimento recusado não ter efeito.
	 */
	List<TipoTributo> receberKhan();

}
