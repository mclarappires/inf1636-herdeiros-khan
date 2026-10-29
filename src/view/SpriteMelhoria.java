package view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Imagem de uma peca de melhoria: resources/melhorias/&lt;simbolo&gt;_&lt;cor&gt;.png
 * (ex.: moeda_verde.png, cavalos_vermelho.png). Devolve null se a imagem nao existir;
 * quem chama desenha a peca de outro jeito.
 */
class SpriteMelhoria {

    private static final String PASTA = "resources/melhorias/";

    private SpriteMelhoria() {
    }

    /** simbolo: "MOEDA", "DOIS_CAVALOS", "SEDA"...; coluna: "PRIMEIRA", "SEGUNDA" ou "TERCEIRA". */
    static BufferedImage criar(String simbolo, String coluna) {
        String cor = corDoArquivo(coluna);
        if (simbolo == null || cor == null) {
            return null;
        }
        String nome = simbolo.equals("DOIS_CAVALOS") ? "cavalos" : simbolo.toLowerCase();
        File arquivo = new File(PASTA + nome + "_" + cor + ".png");
        try {
            return ImageIO.read(arquivo);
        } catch (IOException e) {
            System.err.println("Nao foi possivel ler a imagem da melhoria: " + arquivo.getAbsolutePath());
            return null;
        }
    }

    /** Mesma correspondencia do tabuleiro do jogador: PRIMEIRA vermelha, SEGUNDA verde, TERCEIRA amarela. */
    private static String corDoArquivo(String coluna) {
        if ("PRIMEIRA".equals(coluna)) {
            return "vermelho";
        }
        if ("SEGUNDA".equals(coluna)) {
            return "verde";
        }
        if ("TERCEIRA".equals(coluna)) {
            return "amarelo";
        }
        return null;
    }
}