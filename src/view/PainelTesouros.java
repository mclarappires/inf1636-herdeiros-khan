package view;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import model.FachadaModelo;

/**
 * Inventario do jogador da vez, ao lado do mapa: quantos tesouros de cada tipo ele pode usar
 * (pecas mais os tesouros virtuais do turno) e as pecas de bonus que ja ganhou; as usadas
 * ficam acinzentadas. Tudo desenhado com drawImage.
 */
class PainelTesouros extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Font FONTE_TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    private static final Font FONTE_NUMERO = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    private static final int LADO_TESOURO = 28;
    private static final int LADO_BONUS = 30;
    private static final int BONUS_POR_LINHA = 5;
    private static final int ESPACO = 4;

    private String[] tipos = new String[0];
    private int[] quantidades = new int[0];
    private String[] bonus = new String[0];
    private boolean[] usados = new boolean[0];

    PainelTesouros() {
        setPreferredSize(new Dimension(170, 150));
        setOpaque(false);
    }

    /** Relê na fachada o inventario do jogador da vez. */
    void atualizar() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        if (!fachada.temPartida() || fachada.isEscolhaDeCartasPendente()) {
            tipos = new String[0];
            bonus = new String[0];
            repaint();
            return;
        }
        int vez = fachada.getJogadorDaVez();
        tipos = fachada.getTiposDeTesouro();
        quantidades = new int[tipos.length];
        for (int i = 0; i < tipos.length; i++) {
            quantidades[i] = fachada.getTesouroDisponivel(vez, tipos[i]);
        }
        bonus = fachada.getBonusDoJogador(vez);
        usados = new boolean[bonus.length];
        for (int i = 0; i < bonus.length; i++) {
            usados[i] = fachada.isBonusUsado(vez, bonus[i]);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (tipos.length == 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int y = escreverTitulo(g2, "Tesouros", 0);
        int passo = getWidth() / tipos.length;
        g2.setFont(FONTE_NUMERO);
        FontMetrics medidas = g2.getFontMetrics();
        for (int i = 0; i < tipos.length; i++) {
            int cx = i * passo + passo / 2;
            BufferedImage imagem = SpriteTesouro.de(tipos[i]);
            if (quantidades[i] == 0) { // tipo que o jogador nao tem fica apagado
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            }
            if (imagem != null) {
                g2.drawImage(imagem, cx - LADO_TESOURO / 2, y, LADO_TESOURO, LADO_TESOURO, null);
            }
            g2.setComposite(AlphaComposite.SrcOver);
            String n = String.valueOf(quantidades[i]);
            g2.setColor(Color.BLACK);
            g2.drawString(n, cx - medidas.stringWidth(n) / 2, y + LADO_TESOURO + medidas.getAscent());
        }
        y += LADO_TESOURO + medidas.getHeight() + 8;

        y = escreverTitulo(g2, bonus.length == 0 ? "Bônus: nenhum" : "Bônus", y);
        int x0 = (getWidth() - (BONUS_POR_LINHA * (LADO_BONUS + ESPACO) - ESPACO)) / 2;
        for (int i = 0; i < bonus.length; i++) {
            int x = x0 + (i % BONUS_POR_LINHA) * (LADO_BONUS + ESPACO);
            int yi = y + (i / BONUS_POR_LINHA) * (LADO_BONUS + ESPACO);
            BufferedImage imagem = SpriteBonus.de(bonus[i]);
            if (imagem != null) {
                g2.drawImage(imagem, x, yi, LADO_BONUS, LADO_BONUS, null);
            }
            if (usados[i]) { // peca virada: ja foi usada
                g2.setColor(new Color(60, 60, 60, 170));
                g2.fillRect(x, yi, LADO_BONUS, LADO_BONUS);
            }
        }
    }

    /** Escreve o titulo centralizado a partir de y e devolve o y logo abaixo dele. */
    private int escreverTitulo(Graphics2D g2, String titulo, int y) {
        g2.setFont(FONTE_TITULO);
        g2.setColor(Color.BLACK);
        FontMetrics medidas = g2.getFontMetrics();
        g2.drawString(titulo, (getWidth() - medidas.stringWidth(titulo)) / 2, y + medidas.getAscent());
        return y + medidas.getHeight() + 4;
    }
}
