package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * Yurt (tenda mongol) de jogador, gerada em codigo na cor do dono: base cilindrica
 * clara, teto conico na cor do jogador e porta escura. Pecas iguais ao tributo
 * ficam so no PNG; esta e propria de cada jogador, por isso muda de cor.
 */
class SpriteYurt {

    static final int LARGURA = 100;
    static final int ALTURA = 84;

    private SpriteYurt() {
    }

    static BufferedImage criar(Color cor) {
        BufferedImage img = new BufferedImage(LARGURA, ALTURA, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // sombra no chao
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(new Ellipse2D.Double(4, ALTURA - 14, LARGURA - 8, 12));

        // parede (feltro claro)
        g.setColor(new Color(240, 232, 210));
        g.fillRoundRect(10, 38, LARGURA - 20, 38, 8, 8);
        // teto conico na cor do jogador
        Path2D teto = new Path2D.Double();
        teto.moveTo(LARGURA / 2.0, 4);
        teto.lineTo(LARGURA - 2, 42);
        teto.lineTo(2, 42);
        teto.closePath();
        g.setColor(cor);
        g.fill(teto);
        // porta
        g.setColor(new Color(50, 35, 25));
        g.fillRoundRect(LARGURA / 2 - 9, 50, 18, 26, 8, 8);

        // contorno escuro para destacar sobre qualquer cor do mapa
        g.setStroke(new BasicStroke(3f));
        g.setColor(new Color(25, 20, 15));
        g.draw(teto);
        g.drawRoundRect(10, 38, LARGURA - 20, 38, 8, 8);
        g.dispose();
        return img;
    }
}