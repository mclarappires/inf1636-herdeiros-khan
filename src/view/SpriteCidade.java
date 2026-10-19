package view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Imagem da peça de uma cidade revelada, lida de resources/cidades/cidade_<id>.png.
 * Se o arquivo não puder ser lido, avisa no console e devolve null (a peça não é desenhada).
 */
class SpriteCidade {

    private SpriteCidade() {
    }

    static BufferedImage criar(int idCidade) {
        File arquivo = new File("resources/cidades/cidade_" + idCidade + ".jpg");
        try {
            BufferedImage imagem = ImageIO.read(arquivo);
            if (imagem == null) {
                System.err.println("Arquivo nao e uma imagem valida: " + arquivo.getAbsolutePath());
            }
            return imagem;
        } catch (IOException e) {
            System.err.println("Nao foi possivel ler a imagem da cidade: " + arquivo.getAbsolutePath()
                    + " (existe? " + arquivo.exists() + ")");
            return null;
        }
    }
}