package model;

class ParadaConselheiro extends Parada{
	
	private final Conselheiro conselheiro;
	
	ParadaConselheiro(int id, Regiao regiao, Conselheiro conselheiro){
		super(id, 2, 2, regiao);
		if (conselheiro == null) {
			throw new IllegalArgumentException("Conselheiro Nulo");
			
		}
		this.conselheiro = conselheiro;
		
	}
	
	Conselheiro getConselheiro() {
		return conselheiro;
	}
	
	
}
