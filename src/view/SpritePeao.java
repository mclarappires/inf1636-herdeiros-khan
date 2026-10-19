package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * Gera a imagem (BufferedImage) de um peao na cor pedida, para ser desenhada
 * com drawImage como o enunciado exige. Quando houver uma imagem de peao
 * oficial, e so trocar este metodo por uma leitura de arquivo.
 */
class SpritePeao {

    static final int LARGURA = 80;
    static final int ALTURA = 112;

    private SpritePeao() {
    }

    static BufferedImage criar(Color cor) {
        BufferedImage imagem = new BufferedImage(LARGURA, ALTURA, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagem.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // sombra no chao
        g.setColor(new Color(0, 0, 0, 90));
        g.fill(new Ellipse2D.Double(6, ALTURA - 18, LARGURA - 12, 14));

        // corpo em forma de pino
        Path2D corpo = new Path2D.Double();
        corpo.moveTo(LARGURA / 2.0 - 12, 44);
        corpo.curveTo(LARGURA / 2.0 - 16, 70, 10, 82, 8, ALTURA - 12);
        corpo.lineTo(LARGURA - 8, ALTURA - 12);
        corpo.curveTo(LARGURA - 10, 82, LARGURA / 2.0 + 16, 70, LARGURA / 2.0 + 12, 44);
        corpo.closePath();
        Ellipse2D cabeca = new Ellipse2D.Double(LARGURA / 2.0 - 20, 4, 40, 40);

        g.setColor(cor);
        g.fill(corpo);
        g.fill(cabeca);

        // brilho para dar volume
        g.setColor(new Color(255, 255, 255, 70));
        g.fill(new Ellipse2D.Double(LARGURA / 2.0 - 12, 10, 12, 12));

        // contorno
        g.setColor(new Color(20, 20, 20, 230));
        g.setStroke(new BasicStroke(3f));
        g.draw(corpo);
        g.draw(cabeca);
        g.dispose();
        return imagem;
    }
}