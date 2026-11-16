package model;

import java.util.HashMap;
import java.util.Map;

enum Heroi {
	ALTANI(Cor.VERDE, IconesBase.CAVALO_DOIS_PUNHOS_KHAN, IconesBase.DOIS_CAVALOS_PUNHO, IconesBase.CAVALO_PUNHO_MOEDA),
	CHAGATAI(Cor.VERMELHO, IconesBase.CAVALO_DOIS_PUNHOS_KHAN, IconesBase.CAVALO_PUNHO_MOEDA, IconesBase.DOIS_CAVALOS_PUNHO),
	JOCHI(Cor.PRETO, IconesBase.CAVALO_PUNHO_MOEDA, IconesBase.CAVALO_DOIS_PUNHOS_KHAN, IconesBase.DOIS_CAVALOS_PUNHO),
	OGEDEI(Cor.AZUL, IconesBase.DOIS_CAVALOS_PUNHO, IconesBase.CAVALO_PUNHO_MOEDA, IconesBase.CAVALO_DOIS_PUNHOS_KHAN),
	TOLUI(Cor.AMARELO, IconesBase.CAVALO_PUNHO_MOEDA, IconesBase.DOIS_CAVALOS_PUNHO, IconesBase.CAVALO_DOIS_PUNHOS_KHAN);

	private final Cor cor; //cor das peças de quem joga com o herói
	private final Map<Coluna, IconesBase> iconesBase = new HashMap<Coluna, IconesBase>();

	private Heroi(Cor cor, IconesBase primeira, IconesBase segunda, IconesBase terceira) {
		this.cor = cor;
		iconesBase.put(Coluna.PRIMEIRA, primeira);
		iconesBase.put(Coluna.SEGUNDA, segunda);
		iconesBase.put(Coluna.TERCEIRA, terceira);
		iconesBase.put(Coluna.BRANCA, IconesBase.BRANCA); //igual para todos os heróis
	}

	public Cor getCor() {
		return cor;
	}

	public IconesBase getIconesBase(Coluna coluna) {
		if (coluna == null) {
			throw new IllegalArgumentException("Coluna nula");
		}
		return iconesBase.get(coluna);
	}

	//Altani joga com dois peões
	public int getQuantidadePeoes() {
		return this == ALTANI ? 2 : 1;
	}

	/**
	 * Melhorias que o herói põe na coluna branca na preparação: o Jochi revela 2 da pilha e
	 * escolhe 1 para o primeiro espaço (por isso a 1ª linha completa não lhe dá melhoria grátis).
	 */
	public int getMelhoriasIniciaisNaBranca() {
		return this == JOCHI ? 1 : 0;
	}
}
