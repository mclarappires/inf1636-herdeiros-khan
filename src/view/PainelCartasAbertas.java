package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import model.FachadaModelo;

/**
 * Cartas de conselheiro aberto ainda em jogo, ao lado do tabuleiro, a vista de todos. A carta
 * cumprida sai do jogo e nao e reposta, entao a coluna vai diminuindo.
 */
class PainelCartasAbertas extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Font FONTE = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    private static final int ALTURA_TITULO = 24;
    private static final int ESPACO = 6;
    private static final int MAX_CARTAS = 4;

    private String[] ids = new String[0];

    /** Relê na fachada as cartas em jogo. */
    void atualizar() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        ids = fachada.temPartida() ? fachada.getCartasAbertas() : new String[0];
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!FachadaModelo.getInstancia().temPartida()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String titulo = ids.length == 0 ? "Sem cartas abertas" : "Cartas abertas";
        g2.setFont(FONTE);
        g2.setColor(Color.BLACK);
        FontMetrics medidas = g2.getFontMetrics();
        g2.drawString(titulo, (getWidth() - medidas.stringWidth(titulo)) / 2, medidas.getAscent());

        // o tamanho nao muda quando uma carta sai, para as demais nao pularem de tamanho
        int altura = (getHeight() - ALTURA_TITULO - (MAX_CARTAS - 1) * ESPACO) / MAX_CARTAS;
        for (int i = 0; i < ids.length; i++) {
            SpriteCarta.desenhar(g2, SpriteCarta.aberta(ids[i]), 0, ALTURA_TITULO + i * (altura + ESPACO),
                    getWidth(), altura);
        }
    }
}
