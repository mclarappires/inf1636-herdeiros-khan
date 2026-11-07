package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Contagem final de votos: trilha do Kurultai, influência nas três regiões e carta secreta.
 * Só calcula e guarda os números; quem decide quando o jogo acaba é a Partida.
 */
final class ResultadoFinal {

	/** Votos do 1º colocado em yurts, pelo número de melhorias (0 a 4) na coluna da região. */
	private static final int[] VOTOS_DO_PRIMEIRO = {1, 1, 2, 2, 3};
	/** Votos do 2º colocado, pelo mesmo critério; com 0 melhorias o 2º não recebe nada. */
	private static final int[] VOTOS_DO_SEGUNDO = {0, 1, 1, 2, 2};

	private final List<Jogador> jogadores;
	private final int[] trilha;
	private final int[] carta;
	private final int[] total;
	private final Map<Regiao, int[]> yurtsPorRegiao = new HashMap<Regiao, int[]>();
	private final Map<Regiao, int[]> influenciaPorRegiao = new HashMap<Regiao, int[]>();
	private final List<Integer> vencedores;

	private ResultadoFinal(List<Jogador> jogadores, Tabuleiro tabuleiro) {
		this.jogadores = jogadores;
		int n = jogadores.size();
		trilha = new int[n];
		carta = new int[n];
		total = new int[n];
		for (Regiao r : Regiao.values()) {
			calcularInfluencia(r, tabuleiro);
		}
		for (int i = 0; i < n; i++) {
			Jogador j = jogadores.get(i);
			trilha[i] = j.getVotos();
			CartaConselheiroSecreto secreta = j.getCartaSecreta();
			carta[i] = secreta == null ? 0 : secreta.votosFinais(j, tabuleiro);
			total[i] = trilha[i] + carta[i];
			for (Regiao r : Regiao.values()) {
				total[i] += influenciaPorRegiao.get(r)[i];
			}
		}
		vencedores = Collections.unmodifiableList(desempatar(tabuleiro));
	}

	/** Contagem como seria se a partida acabasse agora. */
	static ResultadoFinal calcular(List<Jogador> jogadores, Tabuleiro tabuleiro) {
		if (jogadores == null || jogadores.isEmpty() || tabuleiro == null) {
			throw new IllegalArgumentException("Jogadores e tabuleiro são obrigatórios");
		}
		return new ResultadoFinal(jogadores, tabuleiro);
	}

	/**
	 * Quem tem mais yurts na região é o 1º; o 2º é quem tem o segundo maior número. Empate no
	 * 1º lugar: todos são 1º e ninguém é 2º. Empate no 2º: todos são 2º. Quem não tem yurt na
	 * região não ganha nada.
	 */
	private void calcularInfluencia(Regiao regiao, Tabuleiro tabuleiro) {
		int n = jogadores.size();
		int[] yurts = new int[n];
		int[] votos = new int[n];
		int maior = 0;
		for (int i = 0; i < n; i++) {
			yurts[i] = ContagemDeYurts.naRegiao(jogadores.get(i).getCor(), tabuleiro, regiao);
			maior = Math.max(maior, yurts[i]);
		}
		int primeiros = 0;
		for (int i = 0; i < n; i++) {
			if (maior > 0 && yurts[i] == maior) {
				votos[i] = VOTOS_DO_PRIMEIRO[melhorias(i, regiao)];
				primeiros++;
			}
		}
		if (primeiros == 1) {
			int segundo = 0;
			for (int i = 0; i < n; i++) {
				if (yurts[i] < maior) {
					segundo = Math.max(segundo, yurts[i]);
				}
			}
			for (int i = 0; i < n; i++) {
				if (segundo > 0 && yurts[i] == segundo) {
					votos[i] = VOTOS_DO_SEGUNDO[melhorias(i, regiao)];
				}
			}
		}
		yurtsPorRegiao.put(regiao, yurts);
		influenciaPorRegiao.put(regiao, votos);
	}

	//melhorias na coluna da região (as ações impressas e a coluna branca não entram), limitadas à tabela
	private int melhorias(int jogador, Regiao regiao) {
		int quantidade = jogadores.get(jogador).getTabuleiroJogador().contarMelhorias(regiao);
		return Math.min(quantidade, VOTOS_DO_PRIMEIRO.length - 1);
	}

	/** Mais votos vence; depois, mais tesouros não entregues; depois, peão mais perto de Karakorum. */
	private List<Integer> desempatar(Tabuleiro tabuleiro) {
		List<Integer> candidatos = new LinkedList<Integer>();
		int maiorTotal = Integer.MIN_VALUE;
		for (int t : total) {
			maiorTotal = Math.max(maiorTotal, t);
		}
		for (int i = 0; i < total.length; i++) {
			if (total[i] == maiorTotal) {
				candidatos.add(i);
			}
		}
		int maisTesouros = -1;
		for (int i : candidatos) {
			maisTesouros = Math.max(maisTesouros, jogadores.get(i).getQuantidadeTesouros());
		}
		List<Integer> empatados = new LinkedList<Integer>();
		for (int i : candidatos) {
			if (jogadores.get(i).getQuantidadeTesouros() == maisTesouros) {
				empatados.add(i);
			}
		}
		if (empatados.size() == 1) {
			return empatados;
		}
		Map<Parada, Integer> distancias = distanciasAteKarakorum(tabuleiro);
		int maisPerto = Integer.MAX_VALUE;
		for (int i : empatados) {
			maisPerto = Math.min(maisPerto, distancia(jogadores.get(i), distancias));
		}
		List<Integer> resultado = new LinkedList<Integer>();
		for (int i : empatados) {
			if (distancia(jogadores.get(i), distancias) == maisPerto) {
				resultado.add(i);
			}
		}
		return resultado; //mais de um: vitória compartilhada
	}

	//distância em paradas pelas rotas, a partir de Karakorum; com 2 peões (Altani) vale o mais perto
	private static int distancia(Jogador j, Map<Parada, Integer> distancias) {
		int menor = Integer.MAX_VALUE;
		for (Peao peao : j.getPeoes()) {
			Integer d = distancias.get(peao.getPosicao());
			if (d != null) {
				menor = Math.min(menor, d);
			}
		}
		return menor;
	}

	private static Map<Parada, Integer> distanciasAteKarakorum(Tabuleiro tabuleiro) {
		Map<Parada, Integer> distancias = new HashMap<Parada, Integer>();
		LinkedList<Parada> fila = new LinkedList<Parada>();
		for (Parada p : tabuleiro.getParadas()) {
			if (p instanceof Karakorum) {
				distancias.put(p, 0);
				fila.add(p);
			}
		}
		while (!fila.isEmpty()) {
			Parada atual = fila.removeFirst();
			for (Parada vizinha : atual.getAdjacentes()) {
				if (!distancias.containsKey(vizinha)) {
					distancias.put(vizinha, distancias.get(atual) + 1);
					fila.add(vizinha);
				}
			}
		}
		return distancias;
	}

	//CONSULTAS

	int getVotosDaTrilha(int jogador) {
		return trilha[jogador];
	}

	int getVotosDaCarta(int jogador) {
		return carta[jogador];
	}

	int getVotosDeInfluencia(int jogador, Regiao regiao) {
		return influenciaPorRegiao.get(regiao)[jogador];
	}

	int getYurtsNaRegiao(int jogador, Regiao regiao) {
		return yurtsPorRegiao.get(regiao)[jogador];
	}

	int getTotal(int jogador) {
		return total[jogador];
	}

	/** Índices dos vencedores: um, ou vários se o empate persistiu até o fim (vitória compartilhada). */
	List<Integer> getVencedores() {
		return vencedores;
	}

}