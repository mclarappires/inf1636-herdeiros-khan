package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class CarregadorMapa {

	private CarregadorMapa() {
	}

	static Tabuleiro carregar(Reader fonte, Random random) throws IOException {
		Tabuleiro tabuleiro = new Tabuleiro(random);
		BufferedReader leitor = new BufferedReader(fonte);
		String linha;
		int numero = 0;
		while ((linha = leitor.readLine()) != null) {
			numero++;
			int comentario = linha.indexOf('#');
			if (comentario >= 0) {
				linha = linha.substring(0, comentario);
			}
			linha = linha.trim();
			if (linha.isEmpty()) {
				continue;
			}
			try {
				interpretar(tabuleiro, linha.split("\\s+"));
			} catch (RuntimeException e) {
				throw new IllegalArgumentException(
						"Linha " + numero + " (\"" + linha + "\"): " + e.getMessage(), e);
			}
		}
		return tabuleiro;
	}

	private static void interpretar(Tabuleiro t, String[] p) {
		String comando = p[0].toUpperCase();
		switch (comando) {
		case "PARADA":
			exigir(p, 2, 5);
			int proximo = 2;
			Regiao regiaoLida = null;
			if (p.length > proximo && !ehNumero(p[proximo])) {
				regiaoLida = Regiao.valueOf(p[proximo].toUpperCase());
				proximo++;
			}
			int peoes = 1;
			int yurts = 1;
			if (p.length - proximo == 2) {
				peoes = inteiro(p[proximo]);
				yurts = inteiro(p[proximo + 1]);
			} else if (p.length != proximo) {
				throw new IllegalArgumentException("argumentos inválidos para PARADA");
			}
			t.adicionarParada(new parada(inteiro(p[1]), peoes, yurts, regiaoLida));
			break;
		case "KARAKORUM":
			exigir(p, 2, 5);
			if (p.length == 2) {
				t.adicionarParada(new Karakorum(inteiro(p[1])));
				break;
			}

			// Se houver mais argumentos, deve ser o número de tesouros e os tipos de tesouro (ex: KARAKORUM 1 PRATA PELE BOI)
			if (p.length != 2 + Karakorum.TESOUROS_POR_ENTREGA) {
				throw new IllegalArgumentException("KARAKORUM pede " + Karakorum.TESOUROS_POR_ENTREGA + " tesouros");
			}
			Map<TipoTesouro, Integer> pedido = new HashMap<TipoTesouro, Integer>();
			for (int i = 2; i < p.length; i++) {
				TipoTesouro tesouro = TipoTesouro.valueOf(p[i].toUpperCase());
				Integer atual = pedido.get(tesouro);
				pedido.put(tesouro, atual == null ? 1 : atual + 1);
			}
			t.adicionarParada(new Karakorum(inteiro(p[1]), pedido));
			break;
		case "PROVINCIA":
			exigir(p, 3, 3);
			t.adicionarProvincia(new Provincia(inteiro(p[1]), TipoTributo.valueOf(p[2].toUpperCase())));
			break;
		case "CIDADE":
			exigir(p, 3, 3);
			t.adicionarCidade(new Cidade(inteiro(p[1]), Regiao.valueOf(p[2].toUpperCase())));
			break;
		case "ROTA":
			exigir(p, 3, 3);
			t.ligarParadas(inteiro(p[1]), inteiro(p[2]));
			break;
		case "PROVINCIA_PARADA":
			exigir(p, 3, 3);
			t.ligarProvinciaParada(inteiro(p[1]), inteiro(p[2]));
			break;
		case "CIDADE_PARADA":
			exigir(p, 3, 3);
			t.ligarCidadeParada(inteiro(p[1]), inteiro(p[2]));
			break;
		default:
			throw new IllegalArgumentException("comando desconhecido: " + p[0]);
		}
	}

	private static void exigir(String[] p, int min, int max) {
		if (p.length < min || p.length > max) {
			throw new IllegalArgumentException("número de argumentos inválido para " + p[0]);
		}
	}

	private static boolean ehNumero(String s) {
		return s.matches("-?\\d+");
	}

	private static int inteiro(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("número inválido: " + s);
		}
	}
}