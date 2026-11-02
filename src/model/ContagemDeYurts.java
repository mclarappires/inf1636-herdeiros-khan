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
			total += p.contarYurtsDe(dono);
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
			if (p.getRegiao() == r) {
				total += p.contarYurtsDe(dono);
			}
		}
		return total + emCidades(dono, t, r);
	}

	/**
	 * Yurts do jogador nas paradas da coleção; serve para contar yurts vizinhos de cidades e
	 * províncias. O yurt do Tolui em Karakorum não conta.
	 */
	static int nasParadas(Cor dono, Collection<Parada> paradas) {
		int total = 0;
		for (Parada p : paradas) {
			if (!isKarakorum(p)) {
				total += p.contarYurtsDe(dono);
			}
		}
		return total;
	}

	/**
	 * Quantidade de yurts do maior grupo de paradas adjacentes entre si que têm yurt do
	 * jogador. O yurt do Tolui em Karakorum não faz parte da rede.
	 */
	static int maiorGrupo(Cor dono, Tabuleiro t) {
		Set<Parada> vistas = new HashSet<Parada>();
		int maior = 0;
		for (Parada inicio : t.getParadas()) {
			if (!entraNoGrupo(inicio, dono) || !vistas.add(inicio)) {
				continue;
			}
			int tamanho = 0;
			LinkedList<Parada> fila = new LinkedList<Parada>();
			fila.add(inicio);
			while (!fila.isEmpty()) {
				Parada atual = fila.removeFirst();
				tamanho += atual.contarYurtsDe(dono);
				for (Parada vizinha : atual.getAdjacentes()) {
					if (entraNoGrupo(vizinha, dono) && vistas.add(vizinha)) {
						fila.add(vizinha);
					}
				}
			}
			maior = Math.max(maior, tamanho);
		}
		return maior;
	}

	private static boolean entraNoGrupo(Parada p, Cor dono) {
		return !isKarakorum(p) && p.temYurtDe(dono);
	}

	private static boolean isKarakorum(Parada p) {
		return p instanceof Karakorum;
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
