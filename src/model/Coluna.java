package model;

enum Coluna {
	PRIMEIRA(Regiao.CHINA),
	SEGUNDA(Regiao.PERSIA),
	TERCEIRA(Regiao.RUSSIA),
	BRANCA(null); //coluna de recuperação, não pertence a nenhuma região

	private final Regiao regiaoDaColuna;

	private Coluna(Regiao regiaoDaColuna) {
		this.regiaoDaColuna = regiaoDaColuna;
	}

	public Regiao getRegiao() {
		return regiaoDaColuna;
	}

	public boolean isBranca() {
		return regiaoDaColuna == null;
	}

	/** Coluna colorida cujas melhorias valem votos na região. */
	public static Coluna daRegiao(Regiao r) {
		for (Coluna c : values()) {
			if (!c.isBranca() && c.regiaoDaColuna == r) {
				return c;
			}
		}
		throw new IllegalArgumentException("Região sem coluna: " + r);
	}
}
