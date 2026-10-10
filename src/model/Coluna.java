package model;

enum Coluna {
	PRIMEIRA(regiao.CHINA),
	SEGUNDA(regiao.PERSIA),
	TERCEIRA(regiao.RUSSIA),
	BRANCA(null); //coluna de recuperação, não pertence a nenhuma região

	private final regiao regiaoDaColuna;

	private Coluna(regiao regiaoDaColuna) {
		this.regiaoDaColuna = regiaoDaColuna;
	}

	public regiao getRegiao() {
		return regiaoDaColuna;
	}

	public boolean isBranca() {
		return regiaoDaColuna == null;
	}

	/** Coluna colorida cujas melhorias valem votos na região. */
	public static Coluna daRegiao(regiao r) {
		for (Coluna c : values()) {
			if (!c.isBranca() && c.regiaoDaColuna == r) {
				return c;
			}
		}
		throw new IllegalArgumentException("Região sem coluna: " + r);
	}
}
