package view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Imagem de uma peça de tributo, lida de resources/tributos/yurt.png, espada.png
 * ou moeda.png. Se o arquivo não puder ser lido, avisa no console e devolve null
 * (a peça simplesmente não é desenhada).
 */
class SpriteTributo {

    private SpriteTributo() {
    }

    /** tipo: "YURT", "ESPADA" ou "MOEDA", como vem da fachada. */
    static BufferedImage criar(String tipo) {
        File arquivo = new File("resources/tributos/" + tipo.toLowerCase() + ".png");
        try {
            BufferedImage imagem = ImageIO.read(arquivo);
            if (imagem == null) {
                System.err.println("Arquivo nao e uma imagem valida: " + arquivo.getAbsolutePath());
            }
            return imagem;
        } catch (IOException e) {
            System.err.println("Nao foi possivel ler a imagem da peca: " + arquivo.getAbsolutePath()
                    + " (existe? " + arquivo.exists() + ")");
            return null;
        }
    }
}