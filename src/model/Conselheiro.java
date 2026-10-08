package model;

import java.util.HashMap;
import java.util.Map;

class Conselheiro {
	
	private final Map<tipoTesouro, Integer> exigidos; //vazio quando o exigido for qualquer tesouro
	private final int quantQualquerTipo; //zero quando for pedido um tipo de tesouro específico
	
	private final int votosPorEntrega;
	private final pecaBonus bonus;
	private boolean bonusColetado = false;
	
	//construtor de pedidos de tipos especificos de tesouros
	Conselheiro(Map<tipoTesouro, Integer> exigidos, int votosPorEntrega, pecaBonus bonus){
		if (exigidos == null || exigidos.isEmpty()) {
			throw new IllegalArgumentException("Pedido de tesouros vazio");
		}
		
		for (Map.Entry<tipoTesouro, Integer> e : exigidos.entrySet()) {
			if(e.getKey() == null || e.getValue() == null || e.getValue() <=0) {
				throw new IllegalArgumentException("Pedido inválido: " + e);
			}
		}
		
		validar(votosPorEntrega, bonus);
		
		this.exigidos = new HashMap<tipoTesouro, Integer>(exigidos);
		this.quantQualquerTipo = 0;
		this.votosPorEntrega = votosPorEntrega;
		this.bonus = bonus;
	}
	
	//construtor de um pedido sem tipo de tesouro definido
	Conselheiro(int quantQualquerTipo, int votosPorEntrega, pecaBonus bonus){
		if (quantQualquerTipo <=0) {
			throw new IllegalArgumentException("Quantidade deve ser positiva");
		}
		
		validar(votosPorEntrega, bonus);
		this.exigidos = new HashMap<tipoTesouro, Integer>();
		this.quantQualquerTipo = quantQualquerTipo;
		this.votosPorEntrega = votosPorEntrega;
		this.bonus = bonus;
	}
	
	private static void validar(int votosPorEntrega, pecaBonus bonus) {
		if (votosPorEntrega <= 0) {
			throw new IllegalArgumentException("Votos por entrega deve ser positivo");
		}
		if (bonus == null) {
			throw new IllegalArgumentException("Conselheiro precisa de uma peça de bônus");
		}
	}
	
	boolean aceita(Map<tipoTesouro, Integer> oferta) {
		if (oferta == null) {
			return false;
		}
		
		//verificacao para qualquer tipo de tesouro
		if (exigidos.isEmpty()) {
			int total = 0;
			for (Integer q : oferta.values()) {
				total += q;
			}
			
			return total >= quantQualquerTipo;
		}
		
		//verificacao para conselheiros que necessitam tipos especificos
		for (Map.Entry<tipoTesouro, Integer> e : exigidos.entrySet()) {
			Integer ofertada = oferta.get(e.getKey());
			if (ofertada == null || ofertada < e.getValue()) {
				return false;
			}
		}
		return true;
	}
	
	int getVotosPorEntrega() {
		return votosPorEntrega;
	}
 
	boolean bonusJaColetado() {
		return bonusColetado;
	}
	
	
	//retorna a pecaBonus pro primeiro jogador que concluir o conselheiro
	pecaBonus coletarBonus() {
		if (bonusColetado) {
			return null;
		}
		bonusColetado = true;
		return bonus;
	}

}
