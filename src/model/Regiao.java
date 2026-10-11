package model;

enum Regiao {
	RUSSIA("Rússia"),
	CHINA("China"),
	PERSIA("Pérsia");
	
	private final String nome;
	
	private Regiao(String nome) {
		this.nome = nome;
	}
	
	public String getNome() {
		return nome;
	}
	
}
