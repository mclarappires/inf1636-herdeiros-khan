package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import model.FachadaModelo;

/**
 * Janela propria com as cartas do jogador da vez: a carta secreta dele e as cartas abertas em
 * jogo. E aberta e fechada por botoes; enquanto esta aberta, o jogo espera (modal).
 */
class JanelaCartas extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final int LARGURA = 1000;
    private static final int ALTURA = 680;

    private final int jogador;

    JanelaCartas(Frame dono) {
        super(dono, "Cartas", true);
        FachadaModelo fachada = FachadaModelo.getInstancia();
        jogador = fachada.getJogadorDaVez();
        String heroi = fachada.getHeroiDoJogador(jogador);
        setTitle("Cartas de " + heroi.charAt(0) + heroi.substring(1).toLowerCase());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(LARGURA, ALTURA);
        setLocationRelativeTo(dono);

        JButton fechar = new JButton("Fechar");
        fechar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        fechar.addActionListener(new OuvinteFechar());
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 8));
        rodape.add(fechar);

        add(new AreaCartas(), BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);
    }

    /** Carta secreta a esquerda e abertas a direita, em duas linhas, desenhadas com drawImage. */
    private class AreaCartas extends JPanel {

        private static final long serialVersionUID = 1L;

        private static final int MARGEM = 20;
        private static final int ALTURA_TEXTO = 34;

        AreaCartas() {
            setBackground(new Color(40, 30, 25));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));

            FachadaModelo fachada = FachadaModelo.getInstancia();
            int larguraSecreta = (getWidth() - 3 * MARGEM) * 2 / 5;
            int alturaCartas = getHeight() - 2 * MARGEM - ALTURA_TEXTO;
            desenharSecreta(g2, fachada, larguraSecreta, alturaCartas);
            desenharAbertas(g2, fachada, 2 * MARGEM + larguraSecreta,
                    getWidth() - 3 * MARGEM - larguraSecreta, alturaCartas);
        }

        private void desenharSecreta(Graphics2D g2, FachadaModelo fachada, int largura, int altura) {
            int votos = fachada.getVotosDaCartaSecreta(jogador);
            escrever(g2, "Carta secreta: valeria " + votos + (votos == 1 ? " voto" : " votos") + " agora",
                    MARGEM);
            SpriteCarta.desenhar(g2, SpriteCarta.secreta(fachada.getCartaSecretaDoJogador(jogador)),
                    MARGEM, MARGEM + ALTURA_TEXTO, largura, altura);
        }

        private void desenharAbertas(Graphics2D g2, FachadaModelo fachada, int x, int largura, int altura) {
            String[] abertas = fachada.getCartasAbertas();
            escrever(g2, abertas.length == 0 ? "Nenhuma carta aberta em jogo"
                    : "Cartas abertas: 1 voto para quem cumprir primeiro", x);
            int l = (largura - MARGEM) / 2;
            int a = (altura - MARGEM) / 2;
            for (int i = 0; i < abertas.length; i++) {
                SpriteCarta.desenhar(g2, SpriteCarta.aberta(abertas[i]), x + (i % 2) * (l + MARGEM),
                        MARGEM + ALTURA_TEXTO + (i / 2) * (a + MARGEM), l, a);
            }
        }

        private void escrever(Graphics2D g2, String texto, int x) {
            g2.setColor(Color.WHITE);
            g2.drawString(texto, x, MARGEM + g2.getFontMetrics().getAscent());
        }
    }

    private class OuvinteFechar implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }
}
