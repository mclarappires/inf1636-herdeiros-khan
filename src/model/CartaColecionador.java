package model;

import java.util.HashSet;
import java.util.Set;

/** Colecionador: melhorias com símbolos diferentes; cada tipo de tesouro é um símbolo diferente. */
class CartaColecionador extends CartaConselheiroSecreto {

	CartaColecionador() {
		super("Colecionador", 4, 6, 7);
	}

	@Override
	int medir(Jogador j, tabuleiro t) {
		Set<Enum<?>> simbolos = new HashSet<Enum<?>>();
		for (Melhoria m : j.getTabuleiroJogador().getTodasMelhorias()) {
			simbolos.add(m.getSimbolo());
		}
		return simbolos.size();
	}

}
