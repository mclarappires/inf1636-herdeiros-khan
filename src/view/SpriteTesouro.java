package view;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Imagens das pecas de tesouro. A tabela "tipo -> arquivo" fica em
 * resources/tesouros/tesouros.txt (uma linha "NOME arquivo.png" por tipo), assim
 * mudar o nome de um tipo no Model so exige editar essa linha.
 * Se algo faltar, avisa no console e devolve null (a peca nao e desenhada).
 */
class SpriteTesouro {

    private static final String PASTA = "resources/tesouros/";

    private static Map<String, String> arquivos = null;
    private static final Map<String, BufferedImage> imagens = new HashMap<String, BufferedImage>();

    private SpriteTesouro() {
    }

    /** tipo: nome do tipo de tesouro, como vem da fachada. */
    static BufferedImage de(String tipo) {
        if (arquivos == null) {
            arquivos = lerTabela();
        }
        if (!imagens.containsKey(tipo)) {
            imagens.put(tipo, carregar(tipo));
        }
        return imagens.get(tipo);
    }

    private static BufferedImage carregar(String tipo) {
        String nome = arquivos.get(tipo);
        if (nome == null) {
            System.err.println("Tipo de tesouro sem imagem em " + PASTA + "tesouros.txt: " + tipo);
            return null;
        }
        File arquivo = new File(PASTA + nome);
        try {
            BufferedImage imagem = ImageIO.read(arquivo);
            if (imagem == null) {
                System.err.println("Arquivo nao e uma imagem valida: " + arquivo.getAbsolutePath());
            }
            return imagem;
        } catch (IOException e) {
            System.err.println("Nao foi possivel ler a imagem do tesouro: " + arquivo.getAbsolutePath()
                    + " (existe? " + arquivo.exists() + ")");
            return null;
        }
    }

    private static Map<String, String> lerTabela() {
        Map<String, String> tabela = new HashMap<String, String>();
        File arquivo = new File(PASTA + "tesouros.txt");
        try {
            BufferedReader leitor = new BufferedReader(new FileReader(arquivo));
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
                    if (partes.length == 2) {
                        tabela.put(partes[0], partes[1]);
                    }
                }
            } finally {
                leitor.close();
            }
        } catch (IOException e) {
            System.err.println("Nao foi possivel ler a tabela de tesouros: " + arquivo.getAbsolutePath());
        }
        return tabela;
    }
}