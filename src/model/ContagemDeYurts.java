package model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/** Consultas sobre os yurts de um jogador no mapa, usadas pelas cartas de conselheiro. */
final class ContagemDeYurts {

	private ContagemDeYurts() {
	}

	/** Yurts do jogador em paradas (Karakorum incluída) e em cidades conquistadas. */
	static int noMapa(Cor dono, Tabuleiro t) {
		int total = 0;
		for (Parada p : t.getParadas()) {
			if (p.temYurtDe(dono)) {
				total++;
			}
		}
		return total + emCidades(dono, t, null);
	}

	/** Yurts do jogador nas paradas e cidades da região; Karakorum não tem região. */
	static int naRegiao(Cor dono, Tabuleiro t, Regiao r) {
		if (r == null) {
			throw new IllegalArgumentException("Região nula");
		}
		int total = 0;
		for (Parada p : t.getParadas()) {
			if (p.getRegiao() == r && p.temYurtDe(dono)) {
				total++;
			}
		}
		return total + emCidades(dono, t, r);
	}

	/** Paradas da coleção com yurt do jogador; serve para contar yurts vizinhos de cidades e províncias. */
	static int nasParadas(Cor dono, Collection<Parada> paradas) {
		int total = 0;
		for (Parada p : paradas) {
			if (p.temYurtDe(dono)) {
				total++;
			}
		}
		return total;
	}

	/** Tamanho do maior grupo de paradas adjacentes entre si que têm yurt do jogador. */
	static int maiorGrupo(Cor dono, Tabuleiro t) {
		Set<Parada> vistas = new HashSet<Parada>();
		int maior = 0;
		for (Parada inicio : t.getParadas()) {
			if (!inicio.temYurtDe(dono) || !vistas.add(inicio)) {
				continue;
			}
			int tamanho = 0;
			LinkedList<Parada> fila = new LinkedList<Parada>();
			fila.add(inicio);
			while (!fila.isEmpty()) {
				Parada atual = fila.removeFirst();
				tamanho++;
				for (Parada vizinha : atual.getAdjacentes()) {
					if (vizinha.temYurtDe(dono) && vistas.add(vizinha)) {
						fila.add(vizinha);
					}
				}
			}
			maior = Math.max(maior, tamanho);
		}
		return maior;
	}

	//cidades conquistadas com yurt do jogador; região nula conta todas
	private static int emCidades(Cor dono, Tabuleiro t, Regiao r) {
		int total = 0;
		for (Cidade c : t.getCidadesConquistadas()) {
			Yurt y = c.getYurtDeConquista();
			if (y != null && y.getDono() == dono && (r == null || c.getRegiao() == r)) {
				total++;
			}
		}
		return total;
	}

}
