package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;


public class mapaTest {

	private static final int DEFAULT_TIMEOUT = 2000;
	private static final String ARQUIVO_MAPA = "resources/mapa_herdeiros.txt";
	private static final int ID_MAXIMO = 60;
	private static final int ID_KARAKORUM = 1;

	private Tabuleiro mapa;

	@Before
	public void carregarMapa() throws IOException {
		FileReader leitor = new FileReader(ARQUIVO_MAPA);
		try {
			mapa = CarregadorMapa.carregar(leitor, new Random(1L));
		} finally {
			leitor.close();
		}
	}

	// ESTRUTURA

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapa_temTrintaEQuatroParadas() {
		assertEquals("O mapa tem Karakorum + 33 paradas", 34, paradasDoMapa().size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapa_temNoveCidades() {
		assertEquals("O mapa tem 9 cidades", 9, cidadesDoMapa().size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapa_temDozeProvincias() {
		assertEquals("O mapa tem 12 provincias", 12, provinciasDoMapa().size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapa_karakorumEhIdentificadoComoKarakorum() {
		assertTrue("A parada 1 deveria ser o Karakorum", mapa.getParada(ID_KARAKORUM) instanceof Karakorum);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapa_karakorumNaoPertenceANenhumaRegiao() {
		assertNull("Karakorum nao tem regiao", mapa.getParada(ID_KARAKORUM).getRegiao());
	}

	// REGIOES

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapa_cadaRegiaoTemOnzeParadas() {
		Map<Regiao, Integer> contagem = new HashMap<Regiao, Integer>();
		for (Parada p : paradasDoMapa()) {
			if (p.getRegiao() != null) {
				incrementar(contagem, p.getRegiao());
			}
		}
		assertEquals("Cada regiao deveria ter 11 paradas", todasAsRegioesCom(11), contagem);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapa_cadaRegiaoTemTresCidades() {
		Map<Regiao, Integer> contagem = new HashMap<Regiao, Integer>();
		for (Cidade c : cidadesDoMapa()) {
			incrementar(contagem, c.getRegiao());
		}
		assertEquals("Cada regiao deveria ter 3 cidades", todasAsRegioesCom(3), contagem);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapa_cadaCidadeEstaCercadaPorTresParadas() {
		List<Integer> tamanhos = new ArrayList<Integer>();
		for (Cidade c : cidadesDoMapa()) {
			tamanhos.add(c.getParadasAdjacentes().size());
		}
		assertEquals("Toda cidade deveria tocar exatamente 3 paradas",
				repetido(3, 9), tamanhos);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapa_paradasDeUmaCidadeSaoDaRegiaoDaCidade() {
		List<Integer> cidadesComParadaDeOutraRegiao = new ArrayList<Integer>();
		for (Cidade c : cidadesDoMapa()) {
			for (Parada p : c.getParadasAdjacentes()) {
				if (p.getRegiao() != c.getRegiao()) {
					cidadesComParadaDeOutraRegiao.add(c.getId());
				}
			}
		}
		assertEquals("Cidades cercadas por parada de outra regiao",
				new ArrayList<Integer>(), cidadesComParadaDeOutraRegiao);
	}

	// CONSELHEIROS E BONUS

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapa_temSeisParadasDuplasDeConselheiro() {
		assertEquals("Sao 6 paradas duplas ao redor do mapa", 6, paradasDuplas().size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapa_cadaRegiaoTemDuasParadasDuplas() {
		Map<Regiao, Integer> contagem = new HashMap<Regiao, Integer>();
		for (Parada p : paradasDuplas()) {
			incrementar(contagem, p.getRegiao());
		}
		assertEquals("Cada regiao deveria ter 2 paradas duplas", todasAsRegioesCom(2), contagem);
	}

	/** Manual: uma peça de bônus para cada conselheiro (6) e uma para o Karakorum. */
	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapa_paradasEspeciaisIgualamAsPecasDeBonus() {
		int paradasEspeciais = paradasDuplas().size() + 1;
		assertEquals("Conselheiros + Karakorum deveriam igualar as pecas de bonus",
				PecaBonus.values().length, paradasEspeciais);
	}

	// CONEXAO E PROVINCIAS

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapa_todasAsParadasSaoAlcancaveisAPartirDoKarakorum() {
		assertEquals("O grafo de rotas deveria ser conexo", 34, alcancaveisDesde(ID_KARAKORUM).size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapa_cadaTipoDeTributoTemQuatroProvincias() {
		Map<TipoTributo, Integer> contagem = new HashMap<TipoTributo, Integer>();
		for (Provincia p : provinciasDoMapa()) {
			incrementar(contagem, p.getTipo());
		}
		Map<TipoTributo, Integer> esperado = new HashMap<TipoTributo, Integer>();
		for (TipoTributo tipo : TipoTributo.values()) {
			esperado.put(tipo, 4);
		}
		assertEquals("Cada tipo de tributo deveria ter 4 provincias", esperado, contagem);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void mapa_todaProvinciaTocaAoMenosUmaParada() {
		List<Integer> semParada = new ArrayList<Integer>();
		for (Provincia p : provinciasDoMapa()) {
			if (p.getParadasAdjacentes().isEmpty()) {
				semParada.add(p.getId());
			}
		}
		assertEquals("Provincias sem nenhuma parada adjacente", new ArrayList<Integer>(), semParada);
	}

	// AUXILIARES

	private List<Parada> paradasDoMapa() {
		List<Parada> lista = new ArrayList<Parada>();
		for (int id = 1; id <= ID_MAXIMO; id++) {
			try {
				lista.add(mapa.getParada(id));
			} catch (IllegalArgumentException inexistente) {
				// id sem parada: segue procurando
			}
		}
		return lista;
	}

	private List<Cidade> cidadesDoMapa() {
		List<Cidade> lista = new ArrayList<Cidade>();
		for (int id = 1; id <= ID_MAXIMO; id++) {
			try {
				lista.add(mapa.getCidade(id));
			} catch (IllegalArgumentException inexistente) {
				// id sem cidade: segue procurando
			}
		}
		return lista;
	}

	private List<Provincia> provinciasDoMapa() {
		List<Provincia> lista = new ArrayList<Provincia>();
		for (int id = 1; id <= ID_MAXIMO; id++) {
			try {
				lista.add(mapa.getProvincia(id));
			} catch (IllegalArgumentException inexistente) {
				// id sem provincia: segue procurando
			}
		}
		return lista;
	}

	/** Paradas de conselheiro: as que comportam 2 peões (o Karakorum comporta todos, por isso fica fora). */
	private List<Parada> paradasDuplas() {
		List<Parada> duplas = new ArrayList<Parada>();
		for (Parada p : paradasDoMapa()) {
			if (p.getCapacidadePeoes() == 2) {
				duplas.add(p);
			}
		}
		return duplas;
	}

	private Set<Integer> alcancaveisDesde(int idInicial) {
		Set<Integer> visitadas = new HashSet<Integer>();
		LinkedList<Parada> fila = new LinkedList<Parada>();
		fila.add(mapa.getParada(idInicial));
		visitadas.add(idInicial);
		while (!fila.isEmpty()) {
			Parada atual = fila.removeFirst();
			for (Parada vizinha : atual.getAdjacentes()) {
				if (visitadas.add(vizinha.getId())) {
					fila.add(vizinha);
				}
			}
		}
		return visitadas;
	}

	private <K> void incrementar(Map<K, Integer> contagem, K chave) {
		Integer atual = contagem.get(chave);
		contagem.put(chave, atual == null ? 1 : atual + 1);
	}

	private Map<Regiao, Integer> todasAsRegioesCom(int quantidade) {
		Map<Regiao, Integer> esperado = new HashMap<Regiao, Integer>();
		for (Regiao r : Regiao.values()) {
			esperado.put(r, quantidade);
		}
		return esperado;
	}

	private List<Integer> repetido(int valor, int vezes) {
		List<Integer> lista = new ArrayList<Integer>();
		for (int i = 0; i < vezes; i++) {
			lista.add(valor);
		}
		return lista;
	}
}