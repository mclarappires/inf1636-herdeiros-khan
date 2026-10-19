package view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Onde cada parada fica na imagem do tabuleiro, em fracao (0 a 1) da largura
 * e da altura. Pertence a View: o Model nao sabe nada de pixels.
 */
class PosicoesParadas {

    private static final double RAIO_PADRAO = 0.025;

    /** id -> {x, y, raio}, tudo em fracao da imagem (raio: fracao da largura). */
    private final Map<Integer, double[]> posicoes = new HashMap<Integer, double[]>();

    /** Le linhas "id x y [raio]"; # inicia comentario. */
    PosicoesParadas(String caminho) throws IOException {
        BufferedReader leitor = new BufferedReader(new FileReader(caminho));
        try {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                int comentario = linha.indexOf('#');
                if (comentario >= 0) {
                    linha = linha.substring(0, comentario);
                }
                linha = linha.trim();
                if (linha.length() == 0) {
                    continue;
                }
                String[] partes = linha.split("\\s+");
                int id = Integer.parseInt(partes[0]);
                double x = Double.parseDouble(partes[1]);
                double y = Double.parseDouble(partes[2]);
                double raio = partes.length > 3 ? Double.parseDouble(partes[3]) : RAIO_PADRAO;
                posicoes.put(id, new double[] { x, y, raio });
            }
        } finally {
            leitor.close();
        }
    }

    /** Fracoes {x, y} da parada, ou null se nao existir. */
    double[] get(int id) {
        return posicoes.get(id);
    }

    Map<Integer, double[]> todas() {
        return posicoes;
    }

    /**
     * Id da parada cujo raio contem o clique (fx, fy); se mais de uma servir,
     * a mais proxima. Retorna null se o clique nao caiu em nenhuma parada.
     */
    Integer paradaNoClique(double fx, double fy, int imgLargura, int imgAltura) {
        Integer melhor = null;
        double melhorDistancia = Double.MAX_VALUE;
        for (Map.Entry<Integer, double[]> e : posicoes.entrySet()) {
            double[] p = e.getValue();
            double dx = (p[0] - fx) * imgLargura;
            double dy = (p[1] - fy) * imgAltura;
            double distancia = Math.sqrt(dx * dx + dy * dy);
            if (distancia <= p[2] * imgLargura && distancia < melhorDistancia) {
                melhorDistancia = distancia;
                melhor = e.getKey();
            }
        }
        return melhor;
    }
}