package model;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Grava a partida, entre dois turnos, num texto ASCII que o LeitorPartida relê. Só o estado
 * vai para o arquivo; o mapa (paradas, rotas, províncias, cidades) é relido do arquivo do mapa.
 * A ordem é sempre a mesma (enums na ordem do enum, ids crescentes), então gravar a partida
 * relida produz o mesmo texto.
 */
class GravadorPartida {

	static final int VERSAO = 1;

	private GravadorPartida() {
	}

	/** Grava a partida no destino (não fecha o destino). Recusa se a partida não puder ser salva agora. */
	static void gravar(Partida p, Writer destino) {
		if (p == null || destino == null) {
			throw new IllegalArgumentException("Partida e destino são obrigatórios");
		}
		if (!p.podeSalvar()) {
			throw new IllegalStateException("Só é possível salvar antes de o jogador da vez ativar uma coluna");
		}
		PrintWriter saida = new PrintWriter(destino);
		saida.println("# Herdeiros do Khan - partida salva");
		saida.println("VERSAO " + VERSAO);
		gravarJogadores(p, saida);
		gravarPartida(p, saida);
		gravarTabuleiro(p.getTabuleiro(), saida);
		saida.flush();
	}

	//JOGADORES

	private static void gravarJogadores(Partida p, PrintWriter saida) {
		List<Jogador> jogadores = p.getJogadores();
		for (int i = 0; i < jogadores.size(); i++) {
			Jogador j = jogadores.get(i);
			saida.println("JOGADOR " + i + " " + j.getHeroi().name() + " " + LeitorPartida.escapar(j.getNome()));
		}
		for (int i = 0; i < jogadores.size(); i++) {
			Jogador j = jogadores.get(i);
			String prefixo = "J " + i + " ";
			saida.println(prefixo + "VOTOS " + j.getVotos());
			saida.println(prefixo + "YURTS " + j.getYurtsDisponiveis());

			StringBuilder tributos = new StringBuilder(prefixo + "TRIBUTOS");
			for (TipoTributo t : TipoTributo.values()) {
				acrescentarQuantidade(tributos, t, j.getQuantidade(t));
			}
			saida.println(tributos);
			saida.println(prefixo + "ESPADAS_GUARDADAS " + j.getEspadasGuardadas());

			StringBuilder tesouros = new StringBuilder(prefixo + "TESOUROS");
			for (TipoTesouro t : TipoTesouro.values()) {
				acrescentarQuantidade(tesouros, t, j.getQuantidade(t));
			}
			saida.println(tesouros);

			StringBuilder bonus = new StringBuilder(prefixo + "BONUS");
			for (PecaBonus peca : PecaBonus.values()) {
				if (j.getBonus().contains(peca)) {
					bonus.append(' ').append(peca.name()).append(':').append(j.isBonusUsado(peca) ? 1 : 0);
				}
			}
			saida.println(bonus);

			saida.println(prefixo + "CARTA_SECRETA " + j.getCartaSecreta().getId());

			StringBuilder usadas = new StringBuilder(prefixo + "COLUNAS_USADAS");
			for (Coluna c : Coluna.values()) {
				if (j.getPecaAtivacao(c).isUsada()) {
					usadas.append(' ').append(c.name());
				}
			}
			saida.println(usadas);

			StringBuilder peoes = new StringBuilder(prefixo + "PEOES");
			for (Peao peao : j.getPeoes()) {
				peoes.append(' ').append(peao.getPosicao().getId());
			}
			saida.println(peoes);

			TabuleiroJogador tj = j.getTabuleiroJogador();
			for (Coluna c : Coluna.values()) {
				StringBuilder linha = new StringBuilder(prefixo + "MELHORIAS " + c.name());
				for (Melhoria m : tj.getMelhorias(c)) {
					linha.append(' ').append(codificar(m));
				}
				saida.println(linha);
			}
			saida.println(prefixo + "GRATUITAS " + tj.getMelhoriasGratuitasPendentes());
		}
	}

	//VEZ, FIM E CARTAS ABERTAS

	private static void gravarPartida(Partida p, PrintWriter saida) {
		saida.println("VEZ " + p.getIndiceDaVez());
		saida.println("FIM " + (p.isFimDisparado() ? 1 : 0) + " " + p.getTurnosAteOFim()
				+ " " + (p.isTerminada() ? 1 : 0));
		for (CartaConselheiroAberto carta : p.getCartasAbertas()) {
			saida.println("CARTA_ABERTA " + carta.getId() + " " + (carta.isReivindicada() ? 1 : 0));
		}
	}

	//TABULEIRO

	private static void gravarTabuleiro(Tabuleiro t, PrintWriter saida) {
		StringBuilder ocultas = new StringBuilder("CIDADES_OCULTAS");
		for (Cidade c : t.getCidadesNaoReveladas()) {
			ocultas.append(' ').append(c.getId());
		}
		saida.println(ocultas);

		for (Cidade c : t.getCidadesVisiveis()) {
			StringBuilder linha = new StringBuilder("CIDADE_VISIVEL " + c.getId());
			for (TipoTesouro tipo : TipoTesouro.values()) {
				acrescentarQuantidade(linha, tipo, c.getQuantidade(tipo));
			}
			saida.println(linha);
		}
		for (Cidade c : t.getCidadesConquistadas()) {
			Yurt y = c.getYurtDeConquista();
			saida.println("CIDADE_CONQUISTADA " + c.getId() + " " + (y == null ? "-" : y.getDono().name()));
		}

		StringBuilder reserva = new StringBuilder("RESERVA_TESOUROS");
		for (TipoTesouro tipo : t.getReservaDeTesouros()) {
			reserva.append(' ').append(tipo.name());
		}
		saida.println(reserva);

		for (Provincia prov : ordenarProvincias(t)) {
			saida.println("PROVINCIA " + prov.getId() + " " + prov.getQuantidade());
		}

		for (Parada parada : ordenarParadas(t)) {
			//o único yurt de Karakorum é o inicial do Tolui, que o próprio Jogador recoloca
			if (parada instanceof Karakorum || !parada.temYurt()) {
				continue;
			}
			StringBuilder linha = new StringBuilder("YURTS " + parada.getId());
			for (Yurt y : parada.getYurts()) {
				linha.append(' ').append(y.getDono().name());
			}
			saida.println(linha);
		}

		for (Parada parada : ordenarParadas(t)) {
			Conselheiro c = conselheiroComBonus(parada);
			if (c != null) {
				saida.println("BONUS " + parada.getId() + " " + c.getBonus().name()
						+ " " + (c.bonusJaColetado() ? 1 : 0));
			}
		}

		saida.println("KHAN " + localDoKhan(t));

		AreaMelhorias area = t.getAreaMelhorias();
		for (int espaco = 0; espaco < AreaMelhorias.ESPACOS; espaco++) {
			Melhoria m = area.getMelhoria(espaco);
			if (m != null) {
				saida.println("AREA_MELHORIA " + espaco + " " + codificar(m));
			}
		}
		StringBuilder pilha = new StringBuilder("PILHA_MELHORIAS");
		for (Melhoria m : area.getPilha()) {
			pilha.append(' ').append(codificar(m));
		}
		saida.println(pilha);
	}

	/** -1 fora do tabuleiro, 0 na área de melhorias, ou o id da província (como na Fachada). */
	private static int localDoKhan(Tabuleiro t) {
		LocalDoKhan local = t.getPeaoKhan().getLocal();
		if (local == null) {
			return FachadaModelo.KHAN_FORA;
		}
		if (local == t.getAreaMelhorias()) {
			return FachadaModelo.KHAN_AREA_MELHORIAS;
		}
		return ((Provincia) local).getId();
	}

	//conselheiro da parada que recebeu peça de bônus, ou null
	private static Conselheiro conselheiroComBonus(Parada p) {
		if (p instanceof ParadaConselheiro) {
			Conselheiro c = ((ParadaConselheiro) p).getConselheiro();
			return c.temBonus() ? c : null;
		}
		if (p instanceof Karakorum && ((Karakorum) p).temBonus()) {
			return ((Karakorum) p).getConselheiro();
		}
		return null;
	}

	//AUXILIARES

	/** Melhoria como "SIMBOLO/COR": "MOEDA/PRIMEIRA", "SEDA/SEGUNDA"... */
	static String codificar(Melhoria m) {
		return m.getSimbolo().name() + "/" + m.getColuna().name();
	}

	private static void acrescentarQuantidade(StringBuilder linha, Enum<?> tipo, int quantidade) {
		if (quantidade > 0) {
			linha.append(' ').append(tipo.name()).append(':').append(quantidade);
		}
	}

	private static List<Parada> ordenarParadas(Tabuleiro t) {
		List<Parada> paradas = new LinkedList<Parada>(t.getParadas());
		Collections.sort(paradas, new PorIdDeParada());
		return paradas;
	}

	private static List<Provincia> ordenarProvincias(Tabuleiro t) {
		List<Provincia> provincias = new LinkedList<Provincia>(t.getProvincias());
		Collections.sort(provincias, new PorIdDeProvincia());
		return provincias;
	}

	private static class PorIdDeParada implements Comparator<Parada> {
		@Override
		public int compare(Parada a, Parada b) {
			return Integer.compare(a.getId(), b.getId());
		}
	}

	private static class PorIdDeProvincia implements Comparator<Provincia> {
		@Override
		public int compare(Provincia a, Provincia b) {
			return Integer.compare(a.getId(), b.getId());
		}
	}
}
