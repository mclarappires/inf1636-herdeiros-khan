package model;

//ícones impressos em uma coluna do tabuleiro do jogador
enum IconesBase {
	CAVALO_DOIS_PUNHOS_KHAN(1, 2, 0, true, false),
	DOIS_CAVALOS_PUNHO(2, 1, 0, false, false),
	CAVALO_PUNHO_MOEDA(1, 1, 1, false, false),
	BRANCA(2, 0, 0, true, true);

	private final int cavalos;  //movimentos
	private final int punhos;   //ações de pegar tributo
	private final int moedas;   //moedas virtuais, valem só no turno
	private final boolean khan;
	private final boolean recuperar;

	private IconesBase(int cavalos, int punhos, int moedas, boolean khan, boolean recuperar) {
		this.cavalos = cavalos;
		this.punhos = punhos;
		this.moedas = moedas;
		this.khan = khan;
		this.recuperar = recuperar;
	}

	public int getCavalos() {
		return cavalos;
	}

	public int getPunhos() {
		return punhos;
	}

	public int getMoedas() {
		return moedas;
	}

	public boolean temKhan() {
		return khan;
	}

	public boolean temRecuperar() {
		return recuperar;
	}
}
