package model;

class Karakorum extends parada{

	Karakorum(int id){
		super(id, Integer.MAX_VALUE, 0, null);
	}

	/** Yurt inicial do Tolui: única exceção à regra de que Karakorum não aceita yurts. */
	void colocarYurtInicial(Yurt y) {
		if (y == null) {
			throw new IllegalArgumentException("Yurt nulo");
		}
		guardarYurt(y);
	}
}
