package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/** Imagens das cartas de conselheiro, achadas pelo id que o Model informa. */
class SpriteCarta {

    private static final String PASTA = "resources/cartas/";

    // cada imagem e lida do disco uma vez so, e depois achada em tempo constante pelo caminho
    private static final Map<String, BufferedImage> IMAGENS = new HashMap<String, BufferedImage>();

    private SpriteCarta() {
    }

    static BufferedImage secreta(String id) {
        return ler("secretas/", id);
    }

    static BufferedImage aberta(String id) {
        return ler("abertas/", id);
    }

    /**
     * Desenha a carta centralizada dentro da caixa, mantendo a proporcao (sem imagem, um
     * retangulo cinza). Devolve onde ela ficou, para saber se um clique caiu nela.
     */
    static Rectangle desenhar(Graphics2D g2, BufferedImage carta, int x, int y, int largura, int altura) {
        if (carta == null) {
            g2.setColor(Color.GRAY);
            g2.fillRect(x, y, largura, altura);
            return new Rectangle(x, y, largura, altura);
        }
        double escala = Math.min((double) largura / carta.getWidth(), (double) altura / carta.getHeight());
        int l = (int) (carta.getWidth() * escala);
        int a = (int) (carta.getHeight() * escala);
        int cx = x + (largura - l) / 2;
        int cy = y + (altura - a) / 2;
        g2.drawImage(carta, cx, cy, l, a, null);
        return new Rectangle(cx, cy, l, a);
    }

    private static BufferedImage ler(String subpasta, String id) {
        if (id == null) {
            return null;
        }
        String caminho = PASTA + subpasta + id.toLowerCase() + ".png";
        if (!IMAGENS.containsKey(caminho)) {
            IMAGENS.put(caminho, lerDoDisco(new File(caminho)));
        }
        return IMAGENS.get(caminho);
    }

    private static BufferedImage lerDoDisco(File arquivo) {
        try {
            return ImageIO.read(arquivo);
        } catch (IOException e) {
            System.err.println("Nao foi possivel ler a imagem da carta: " + arquivo.getAbsolutePath());
            return null;
        }
    }
}
