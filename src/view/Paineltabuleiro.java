package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * Painel que desenha o tabuleiro com Graphics2D.drawImage (nada de JLabel).
 * Passo 1: so desenha a imagem e imprime no console a posicao clicada
 * em fracao (0 a 1) da imagem, para calibrar as paradas depois.
 */
class PainelTabuleiro extends JPanel {

    private static final long serialVersionUID = 1L;

    private Image imagemTabuleiro;

    // area onde a imagem foi desenhada (muda quando a janela e redimensionada)
    private int imgX, imgY, imgLargura, imgAltura;

    PainelTabuleiro(String caminhoImagem) {
        carregarImagem(caminhoImagem);
        addMouseListener(new OuvinteMouse());
    }

    private void carregarImagem(String caminho) {
        try {
            imagemTabuleiro = ImageIO.read(new File(caminho));
        } catch (IOException e) {
            imagemTabuleiro = null;
            System.err.println("Nao foi possivel ler a imagem: " + caminho);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (imagemTabuleiro == null) {
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Imagem do tabuleiro nao encontrada", 20, 30);
            return;
        }

        // mantem a proporcao da imagem dentro do painel
        int larguraOriginal = imagemTabuleiro.getWidth(null);
        int alturaOriginal = imagemTabuleiro.getHeight(null);
        double escala = Math.min((double) getWidth() / larguraOriginal,
                (double) getHeight() / alturaOriginal);
        imgLargura = (int) (larguraOriginal * escala);
        imgAltura = (int) (alturaOriginal * escala);
        imgX = (getWidth() - imgLargura) / 2;
        imgY = (getHeight() - imgAltura) / 2;

        g2.drawImage(imagemTabuleiro, imgX, imgY, imgLargura, imgAltura, null);
    }

    /** Eventos tratados como no Cap. 12: classe ouvinte, sem lambda. */
    private class OuvinteMouse extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (imgLargura == 0 || imgAltura == 0) {
                return;
            }
            double fx = (double) (e.getX() - imgX) / imgLargura;
            double fy = (double) (e.getY() - imgY) / imgAltura;
            if (fx < 0 || fx > 1 || fy < 0 || fy > 1) {
                return; // clique fora da imagem
            }
            System.out.printf("clique: x=%.4f y=%.4f%n", fx, fy);
        }
    }
}