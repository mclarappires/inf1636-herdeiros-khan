package view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Imagens das peças de bônus (resources/bonus), achadas pelo nome que a fachada informa
 * ("VOTO", "MOEDAS"...). Se o arquivo faltar, avisa no console e devolve null.
 */
class SpriteBonus {

    private static final String PASTA = "resources/bonus/";

    private static final Map<String, String> ARQUIVOS = new HashMap<String, String>();
    private static final Map<String, BufferedImage> imagens = new HashMap<String, BufferedImage>();

    static {
        ARQUIVOS.put("VOTO", "voto.jpg");
        ARQUIVOS.put("ESPADAS", "espadas.jpg");
        ARQUIVOS.put("MOEDAS", "moedas.jpg");
        ARQUIVOS.put("YURTS", "yurts.jpg");
        ARQUIVOS.put("MOVIMENTOS", "cavalos-mao.jpg");
        ARQUIVOS.put("ACOES", "maos.jpg");
        ARQUIVOS.put("TESOUROS", "tesouros.jpg");
    }

    private SpriteBonus() {
    }

    static BufferedImage de(String bonus) {
        if (!imagens.containsKey(bonus)) {
            imagens.put(bonus, carregar(bonus));
        }
        return imagens.get(bonus);
    }

    private static BufferedImage carregar(String bonus) {
        String nome = ARQUIVOS.get(bonus);
        if (nome == null) {
            System.err.println("Peca de bonus sem imagem: " + bonus);
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
            System.err.println("Nao foi possivel ler a peca de bonus: " + arquivo.getAbsolutePath()
                    + " (existe? " + arquivo.exists() + ")");
            return null;
        }
    }
}
