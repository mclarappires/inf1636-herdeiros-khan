package model;

import java.util.HashMap;
import java.util.Map;

/** Sacerdote: pares de melhorias com o mesmo símbolo; um símbolo pode formar mais de um par. */
class CartaSacerdote extends CartaConselheiroSecreto {

	CartaSacerdote() {
		super("Sacerdote", 1, 2, 4);
	}

	@Override
	int medir(Jogador j, tabuleiro t) {
		Map<Enum<?>, Integer> porSimbolo = new HashMap<Enum<?>, Integer>();
		for (Melhoria m : j.getTabuleiroJogador().getTodasMelhorias()) {
			Integer atual = porSimbolo.get(m.getSimbolo());
			porSimbolo.put(m.getSimbolo(), atual == null ? 1 : atual + 1);
		}
		int pares = 0;
		for (Integer quantidade : porSimbolo.values()) {
			pares += quantidade / 2;
		}
		return pares;
	}

}
