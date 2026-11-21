package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Relê o texto gravado pelo GravadorPartida e monta uma Partida nova sobre um mapa recém
 * carregado (ainda não preparado). Cada linha é "PALAVRA token token..."; '#' inicia um
 * comentário. Um arquivo inválido gera IllegalArgumentException com o número da linha.
 */
class LeitorPartida {

	private LeitorPartida() {
	}

	/** Uma linha útil do arquivo, já sem comentário. */
	private static class Linha {
		final int numero;
		final String texto;
		final String[] p;

		Linha(int numero, String texto) {
			this.numero = numero;
			this.texto = texto;
			this.p = separar(texto);
		}
	}

	/** Separa a linha em tokens com um Scanner (o separador padrão são os espaços em branco). */
	private static String[] separar(String texto) {
		List<String> tokens = new LinkedList<String>();
		Scanner s = null;
		try {
			s = new Scanner(texto);
			while (s.hasNext()) {
				tokens.add(s.next());
			}
		} finally {
			if (s != null) {
				s.close();
			}
		}
		return tokens.toArray(new String[tokens.size()]);
	}

	/** Lê a partida salva. O mapa precisa ter sido carregado agora e não pode estar preparado. */
	static Partida ler(Reader fonte, Tabuleiro mapa) throws IOException {
		if (fonte == null || mapa == null) {
			throw new IllegalArgumentException("Arquivo e mapa são obrigatórios");
		}
		if (mapa.isPreparado()) {
			throw new IllegalArgumentException("O mapa já foi preparado para outra partida");
		}
		Map<String, List<Linha>> grupos = agrupar(fonte);

		Linha versao = unica(grupos, "VERSAO");
		executar(versao, new Acao() {
			@Override
			void fazer(Linha l) {
				exigirTamanho(l, 2);
				if (inteiro(l.p[1]) != GravadorPartida.VERSAO) {
					throw new IllegalArgumentException("versão não suportada: " + l.p[1]);
				}
			}
		});

		List<Jogador> jogadores = criarJogadores(grupos, mapa);
		restaurarTabuleiro(grupos, mapa);
		restaurarJogadores(grupos, mapa, jogadores);
		return criarPartida(grupos, mapa, jogadores);
	}

	//LEITURA DAS LINHAS

	private static Map<String, List<Linha>> agrupar(Reader fonte) throws IOException {
		Map<String, List<Linha>> grupos = new HashMap<String, List<Linha>>();
		BufferedReader leitor = new BufferedReader(fonte);
		String texto;
		int numero = 0;
		while ((texto = leitor.readLine()) != null) {
			numero++;
			int comentario = texto.indexOf('#');
			if (comentario >= 0) {
				texto = texto.substring(0, comentario);
			}
			texto = texto.trim();
			if (texto.isEmpty()) {
				continue;
			}
			Linha linha = new Linha(numero, texto);
			String chave = linha.p[0].toUpperCase();
			List<Linha> doGrupo = grupos.get(chave);
			if (doGrupo == null) {
				doGrupo = new LinkedList<Linha>();
				grupos.put(chave, doGrupo);
			}
			doGrupo.add(linha);
		}
		return grupos;
	}

	private static List<Linha> todas(Map<String, List<Linha>> grupos, String chave) {
		List<Linha> linhas = grupos.get(chave);
		return linhas == null ? new LinkedList<Linha>() : linhas;
	}

	private static Linha unica(Map<String, List<Linha>> grupos, String chave) {
		List<Linha> linhas = todas(grupos, chave);
		if (linhas.isEmpty()) {
			throw new IllegalArgumentException("Falta a linha " + chave);
		}
		if (linhas.size() > 1) {
			throw new IllegalArgumentException("Linha " + linhas.get(1).numero + ": " + chave + " repetida");
		}
		return linhas.get(0);
	}

	/** Trabalho feito sobre uma linha; os erros ganham o número da linha. */
	private abstract static class Acao {
		abstract void fazer(Linha l);
	}

	private static void executar(Linha l, Acao acao) {
		try {
			acao.fazer(l);
		} catch (RuntimeException e) {
			throw new IllegalArgumentException("Linha " + l.numero + " (\"" + l.texto + "\"): " + e.getMessage(), e);
		}
	}

	//JOGADORES

	/** JOGADOR i HEROI nome: os jogadores na ordem de jogada, ainda com o estado inicial. */
	private static List<Jogador> criarJogadores(Map<String, List<Linha>> grupos, Tabuleiro mapa) {
		List<Linha> linhas = todas(grupos, "JOGADOR");
		final List<Heroi> herois = new LinkedList<Heroi>();
		final List<String> nomes = new LinkedList<String>();
		for (Linha l : linhas) {
			executar(l, new Acao() {
				@Override
				void fazer(Linha l) {
					//as 3 primeiras palavras são tokens; o resto da linha é o nome, que pode ter espaços
					String[] partes = new String[4];
					int lidas = 0;
					Scanner s = null;
					try {
						s = new Scanner(l.texto);
						while (lidas < 3 && s.hasNext()) {
							partes[lidas++] = s.next();
						}
						if (lidas == 3 && s.hasNextLine()) {
							String nome = s.nextLine().trim();
							if (!nome.isEmpty()) {
								partes[lidas++] = nome;
							}
						}
					} finally {
						if (s != null) {
							s.close();
						}
					}
					if (lidas != 4) {
						throw new IllegalArgumentException("esperado JOGADOR índice HEROI nome");
					}
					if (inteiro(partes[1]) != herois.size()) {
						throw new IllegalArgumentException("os jogadores devem vir na ordem 0, 1, 2...");
					}
					herois.add(enumerado(Heroi.class, partes[2]));
					nomes.add(desescapar(partes[3]));
				}
			});
		}
		Partida.exigirEscolhasValidas(herois);
		Partida.exigirNomesValidos(nomes, herois.size());

		Karakorum inicial = acharKarakorum(mapa);
		List<Jogador> jogadores = new LinkedList<Jogador>();
		for (int i = 0; i < herois.size(); i++) {
			Heroi heroi = herois.get(i);
			Jogador j = new Jogador(heroi.getCor(), heroi, inicial, 0); //moedas vêm da linha TRIBUTOS
			j.setNome(nomes.get(i));
			jogadores.add(j);
		}
		return jogadores;
	}

	/** J i CAMPO valores...: o estado de cada jogador entre turnos. */
	private static void restaurarJogadores(Map<String, List<Linha>> grupos, final Tabuleiro mapa,
			final List<Jogador> jogadores) {
		for (Linha l : todas(grupos, "J")) {
			executar(l, new Acao() {
				@Override
				void fazer(Linha l) {
					if (l.p.length < 3) {
						throw new IllegalArgumentException("esperado J índice CAMPO valores");
					}
					int indice = inteiro(l.p[1]);
					if (indice < 0 || indice >= jogadores.size()) {
						throw new IllegalArgumentException("jogador inexistente: " + indice);
					}
					restaurarCampo(jogadores.get(indice), l.p[2].toUpperCase(), l.p, mapa);
				}
			});
		}
	}

	private static void restaurarCampo(Jogador j, String campo, String[] p, Tabuleiro mapa) {
		final int inicio = 3; //primeiro valor depois de "J i CAMPO"
		switch (campo) {
		case "VOTOS":
			j.definirVotos(valorUnico(p));
			break;
		case "YURTS":
			j.definirYurtsDisponiveis(valorUnico(p));
			break;
		case "TRIBUTOS":
			j.definirTributos(quantidades(TipoTributo.class, p, inicio));
			break;
		case "ESPADAS_GUARDADAS":
			j.guardarEspadasVirtuais(valorUnico(p));
			break;
		case "TESOUROS":
			j.definirTesouros(quantidades(TipoTesouro.class, p, inicio));
			break;
		case "BONUS":
			Map<PecaBonus, Boolean> bonus = new HashMap<PecaBonus, Boolean>();
			for (Map.Entry<PecaBonus, Integer> e : quantidades(PecaBonus.class, p, inicio).entrySet()) {
				bonus.put(e.getKey(), e.getValue() != 0);
			}
			j.definirBonus(bonus);
			break;
		case "CARTA_SECRETA":
			exigirTamanho(p, inicio + 1);
			j.definirCartaSecreta(CartaConselheiroSecreto.porId(p[inicio]));
			break;
		case "COLUNAS_USADAS":
			for (int i = inicio; i < p.length; i++) {
				j.getPecaAtivacao(enumerado(Coluna.class, p[i])).usar();
			}
			break;
		case "PEOES":
			List<Peao> peoes = j.getPeoes();
			if (p.length - inicio != peoes.size()) {
				throw new IllegalArgumentException(j.getHeroi() + " tem " + peoes.size() + " peão(ões)");
			}
			for (int i = 0; i < peoes.size(); i++) {
				peoes.get(i).moverPara(mapa.getParada(inteiro(p[inicio + i])));
			}
			break;
		case "MELHORIAS":
			if (p.length <= inicio) {
				throw new IllegalArgumentException("falta a coluna");
			}
			List<Melhoria> daColuna = new LinkedList<Melhoria>();
			for (int i = inicio + 1; i < p.length; i++) {
				daColuna.add(decodificarMelhoria(p[i]));
			}
			j.getTabuleiroJogador().restaurarColuna(enumerado(Coluna.class, p[inicio]), daColuna);
			break;
		case "GRATUITAS":
			j.getTabuleiroJogador().definirGratuitasPendentes(valorUnico(p));
			break;
		default:
			throw new IllegalArgumentException("campo de jogador desconhecido: " + campo);
		}
	}

	//TABULEIRO

	private static void restaurarTabuleiro(Map<String, List<Linha>> grupos, final Tabuleiro mapa) {
		final List<Cidade> ocultas = new LinkedList<Cidade>();
		final List<Cidade> visiveis = new LinkedList<Cidade>();
		final List<Cidade> conquistadas = new LinkedList<Cidade>();
		final List<TipoTesouro> reserva = new LinkedList<TipoTesouro>();

		executar(unica(grupos, "CIDADES_OCULTAS"), new Acao() {
			@Override
			void fazer(Linha l) {
				for (int i = 1; i < l.p.length; i++) {
					ocultas.add(mapa.getCidade(inteiro(l.p[i])));
				}
			}
		});
		for (Linha linha : todas(grupos, "CIDADE_VISIVEL")) {
			executar(linha, new Acao() {
				@Override
				void fazer(Linha l) {
					exigirTamanhoMinimo(l, 2);
					Cidade c = mapa.getCidade(inteiro(l.p[1]));
					for (Map.Entry<TipoTesouro, Integer> e : quantidades(TipoTesouro.class, l.p, 2).entrySet()) {
						for (int i = 0; i < e.getValue(); i++) {
							c.colocarTesouro(e.getKey());
						}
					}
					if (!c.temTesouro()) {
						throw new IllegalArgumentException("cidade visível sem tesouros");
					}
					visiveis.add(c);
				}
			});
		}
		for (Linha linha : todas(grupos, "CIDADE_CONQUISTADA")) {
			executar(linha, new Acao() {
				@Override
				void fazer(Linha l) {
					exigirTamanho(l, 3);
					Cidade c = mapa.getCidade(inteiro(l.p[1]));
					c.restaurarConquista(l.p[2].equals("-") ? null : new Yurt(enumerado(Cor.class, l.p[2])));
					conquistadas.add(c);
				}
			});
		}
		executar(unica(grupos, "RESERVA_TESOUROS"), new Acao() {
			@Override
			void fazer(Linha l) {
				for (int i = 1; i < l.p.length; i++) {
					reserva.add(enumerado(TipoTesouro.class, l.p[i]));
				}
			}
		});
		mapa.restaurar(ocultas, visiveis, conquistadas, reserva); //confere se cada cidade apareceu uma vez

		for (Linha linha : todas(grupos, "PROVINCIA")) {
			executar(linha, new Acao() {
				@Override
				void fazer(Linha l) {
					exigirTamanho(l, 3);
					mapa.getProvincia(inteiro(l.p[1])).definirQuantidade(inteiro(l.p[2]));
				}
			});
		}
		for (Linha linha : todas(grupos, "YURTS")) {
			executar(linha, new Acao() {
				@Override
				void fazer(Linha l) {
					exigirTamanhoMinimo(l, 3);
					Parada parada = mapa.getParada(inteiro(l.p[1]));
					if (parada.temYurt()) {
						throw new IllegalArgumentException("parada repetida");
					}
					for (int i = 2; i < l.p.length; i++) {
						parada.adicionarYurt(new Yurt(enumerado(Cor.class, l.p[i]))); //confere a capacidade
					}
				}
			});
		}
		for (Linha linha : todas(grupos, "BONUS")) {
			executar(linha, new Acao() {
				@Override
				void fazer(Linha l) {
					exigirTamanho(l, 4);
					restaurarBonus(mapa.getParada(inteiro(l.p[1])), enumerado(PecaBonus.class, l.p[2]),
							inteiro(l.p[3]) != 0);
				}
			});
		}
		executar(unica(grupos, "KHAN"), new Acao() {
			@Override
			void fazer(Linha l) {
				exigirTamanho(l, 2);
				mapa.getPeaoKhan().restaurar(localDoKhan(mapa, inteiro(l.p[1])));
			}
		});
		restaurarAreaMelhorias(grupos, mapa.getAreaMelhorias());
	}

	private static void restaurarBonus(Parada parada, PecaBonus peca, boolean coletado) {
		Conselheiro c;
		if (parada instanceof ParadaConselheiro) {
			c = ((ParadaConselheiro) parada).getConselheiro();
			c.colocarBonus(peca);
		} else if (parada instanceof Karakorum) {
			((Karakorum) parada).colocarBonus(peca);
			c = ((Karakorum) parada).getConselheiro();
		} else {
			throw new IllegalArgumentException("a parada " + parada.getId() + " não tem conselheiro");
		}
		if (coletado) {
			c.marcarBonusColetado();
		}
	}

	private static LocalDoKhan localDoKhan(Tabuleiro mapa, int local) {
		if (local == FachadaModelo.KHAN_FORA) {
			return null;
		}
		if (local == FachadaModelo.KHAN_AREA_MELHORIAS) {
			return mapa.getAreaMelhorias();
		}
		Provincia p = mapa.getProvincia(local);
		if (!p.isDoKhan()) {
			throw new IllegalArgumentException("a província " + local + " não é do Khan");
		}
		return p;
	}

	private static void restaurarAreaMelhorias(Map<String, List<Linha>> grupos, final AreaMelhorias area) {
		final Map<Integer, Melhoria> espacos = new HashMap<Integer, Melhoria>();
		for (Linha linha : todas(grupos, "AREA_MELHORIA")) {
			executar(linha, new Acao() {
				@Override
				void fazer(Linha l) {
					exigirTamanho(l, 3);
					int espaco = inteiro(l.p[1]);
					if (espacos.containsKey(espaco)) {
						throw new IllegalArgumentException("espaço repetido");
					}
					espacos.put(espaco, decodificarMelhoria(l.p[2]));
				}
			});
		}
		executar(unica(grupos, "PILHA_MELHORIAS"), new Acao() {
			@Override
			void fazer(Linha l) {
				List<Melhoria> pilha = new LinkedList<Melhoria>();
				for (int i = 1; i < l.p.length; i++) {
					pilha.add(decodificarMelhoria(l.p[i]));
				}
				area.restaurar(pilha, espacos);
			}
		});
	}

	//PARTIDA

	private static Partida criarPartida(Map<String, List<Linha>> grupos, final Tabuleiro mapa,
			final List<Jogador> jogadores) {
		final int[] vez = new int[1];
		executar(unica(grupos, "VEZ"), new Acao() {
			@Override
			void fazer(Linha l) {
				exigirTamanho(l, 2);
				vez[0] = inteiro(l.p[1]);
			}
		});
		final int[] fim = new int[3];
		executar(unica(grupos, "FIM"), new Acao() {
			@Override
			void fazer(Linha l) {
				exigirTamanho(l, 4);
				for (int i = 0; i < fim.length; i++) {
					fim[i] = inteiro(l.p[i + 1]);
				}
			}
		});
		final List<CartaConselheiroAberto> abertas = new LinkedList<CartaConselheiroAberto>();
		for (Linha linha : todas(grupos, "CARTA_ABERTA")) {
			executar(linha, new Acao() {
				@Override
				void fazer(Linha l) {
					exigirTamanho(l, 3);
					CartaConselheiroAberto carta = CartaConselheiroAberto.porId(l.p[1]);
					for (CartaConselheiroAberto outra : abertas) {
						if (outra.getId().equals(carta.getId())) {
							throw new IllegalArgumentException("carta repetida");
						}
					}
					if (inteiro(l.p[2]) != 0) {
						carta.marcarReivindicada();
					}
					abertas.add(carta);
				}
			});
		}
		if (abertas.size() != Partida.CARTAS_ABERTAS) {
			throw new IllegalArgumentException("A partida tem " + Partida.CARTAS_ABERTAS + " cartas abertas");
		}
		return new Partida(mapa, jogadores, abertas, vez[0], fim[0] != 0, fim[1], fim[2] != 0);
	}

	//CONVERSÕES

	/** "MOEDA/PRIMEIRA" ou "SEDA/SEGUNDA" (símbolo de tesouro = melhoria de tesouro virtual). */
	static Melhoria decodificarMelhoria(String texto) {
		String[] partes = texto.split("/");
		if (partes.length != 2) {
			throw new IllegalArgumentException("melhoria inválida: " + texto);
		}
		Coluna cor = enumerado(Coluna.class, partes[1]);
		for (TipoTesouro tesouro : TipoTesouro.values()) {
			if (tesouro.name().equals(partes[0])) {
				return new Melhoria(tesouro, cor);
			}
		}
		return new Melhoria(enumerado(TipoMelhoria.class, partes[0]), cor);
	}

	/** Lê "NOME:quantidade" de p[inicio] em diante. */
	private static <E extends Enum<E>> Map<E, Integer> quantidades(Class<E> tipo, String[] p, int inicio) {
		Map<E, Integer> mapa = new HashMap<E, Integer>();
		for (int i = inicio; i < p.length; i++) {
			String[] partes = p[i].split(":");
			if (partes.length != 2) {
				throw new IllegalArgumentException("esperado NOME:quantidade em " + p[i]);
			}
			E chave = enumerado(tipo, partes[0]);
			if (mapa.containsKey(chave)) {
				throw new IllegalArgumentException(chave + " repetido");
			}
			int q = inteiro(partes[1]);
			if (q < 0) {
				throw new IllegalArgumentException("quantidade negativa em " + p[i]);
			}
			mapa.put(chave, q);
		}
		return mapa;
	}

	private static <E extends Enum<E>> E enumerado(Class<E> tipo, String nome) {
		try {
			return Enum.valueOf(tipo, nome.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(tipo.getSimpleName() + " desconhecido: " + nome);
		}
	}

	private static int inteiro(String texto) {
		try {
			return Integer.parseInt(texto);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("número esperado em vez de " + texto);
		}
	}

	//o único valor numérico de "J i CAMPO valor"
	private static int valorUnico(String[] p) {
		exigirTamanho(p, 4);
		return inteiro(p[3]);
	}

	private static void exigirTamanho(Linha l, int tamanho) {
		exigirTamanho(l.p, tamanho);
	}

	private static void exigirTamanho(String[] p, int tamanho) {
		if (p.length != tamanho) {
			throw new IllegalArgumentException("esperados " + (tamanho - 1) + " valores");
		}
	}

	private static void exigirTamanhoMinimo(Linha l, int tamanho) {
		if (l.p.length < tamanho) {
			throw new IllegalArgumentException("esperados pelo menos " + (tamanho - 1) + " valores");
		}
	}

	private static Karakorum acharKarakorum(Tabuleiro mapa) {
		for (Parada p : mapa.getParadas()) {
			if (p instanceof Karakorum) {
				return (Karakorum) p;
			}
		}
		throw new IllegalArgumentException("O mapa não tem Karakorum");
	}

	//TEXTO ASCII

	/**
	 * Deixa o texto só com ASCII imprimível: acentos, controles, '\' e '#' (que abre comentário)
	 * viram \\uXXXX. Os espaços ficam como estão.
	 */
	static String escapar(String texto) {
		StringBuilder saida = new StringBuilder();
		for (int i = 0; i < texto.length(); i++) {
			char c = texto.charAt(i);
			if (c < ' ' || c > '~' || c == '\\' || c == '#') {
				saida.append(String.format("\\u%04x", (int) c));
			} else {
				saida.append(c);
			}
		}
		return saida.toString();
	}

	/** Desfaz escapar. */
	static String desescapar(String texto) {
		StringBuilder saida = new StringBuilder();
		int i = 0;
		while (i < texto.length()) {
			char c = texto.charAt(i);
			if (c != '\\') {
				saida.append(c);
				i++;
				continue;
			}
			if (i + 6 > texto.length() || texto.charAt(i + 1) != 'u') {
				throw new IllegalArgumentException("escape inválido em " + texto);
			}
			try {
				saida.append((char) Integer.parseInt(texto.substring(i + 2, i + 6), 16));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("escape inválido em " + texto);
			}
			i += 6;
		}
		return saida.toString();
	}
}
