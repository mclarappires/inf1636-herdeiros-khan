package model;

class paradaConselheiro extends parada{
	
	private final Conselheiro conselheiro;
	
	paradaConselheiro(int id, Conselheiro conselheiro){
		super(id, 2, 2);
		if (conselheiro == null) {
			throw new IllegalArgumentException("Conselheiro Nulo");
			
		}
		this.conselheiro = conselheiro;
		
	}
	
	Conselheiro getConselheiro() {
		return conselheiro;
	}
	
	
}
