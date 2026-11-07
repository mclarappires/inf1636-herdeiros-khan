package view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Imagem da peça de ativação de um herói, lida de resources/ativacao/<heroi>.jpg.
 * Se o arquivo não puder ser lido, avisa no console e devolve null.
 */
class SpriteAtivacao {

    private SpriteAtivacao() {
    }

    static BufferedImage criar(String heroi) {
        File arquivo = new File("resources/ativacao/" + heroi.toLowerCase() + ".jpg");
        try {
            BufferedImage imagem = ImageIO.read(arquivo);
            if (imagem == null) {
                System.err.println("Arquivo nao e uma imagem valida: " + arquivo.getAbsolutePath());
            }
            return imagem;
        } catch (IOException e) {
            System.err.println("Nao foi possivel ler a peca de ativacao: " + arquivo.getAbsolutePath()
                    + " (existe? " + arquivo.exists() + ")");
            return null;
        }
    }
}
