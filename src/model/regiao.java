package model;

enum regiao {
	RUSSIA("Rússia"),
	CHINA("China"),
	PERSIA("Pérsia");
	
	private final String nome;
	
	private regiao(String nome) {
		this.nome = nome;
	}
	
	public String getNome() {
		return nome;
	}
	
}
