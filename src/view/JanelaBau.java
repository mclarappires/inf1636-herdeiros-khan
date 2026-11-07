package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Bau: mostra os tesouros sorteados (desenhados com drawImage) e o jogador clica no que vai
 * guardar. Modal e sem fechar pelo X, porque a peca ja foi usada e a escolha e obrigatoria.
 */
class JanelaBau extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final int LADO = 80;
    private static final int ESPACO = 24;

    private final String[] opcoes;
    private String escolhido = null;

    private JanelaBau(Window dono, String[] opcoes) {
        super(dono, "Baú: escolha 1 tesouro", ModalityType.APPLICATION_MODAL);
        this.opcoes = opcoes;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        JPanel conteudo = new JPanel(new BorderLayout(0, 10));
        conteudo.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));
        JLabel rotulo = new JLabel("Clique no tesouro que vai guardar; os outros são descartados.");
        rotulo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        conteudo.add(rotulo, BorderLayout.NORTH);
        conteudo.add(new AreaTesouros(), BorderLayout.CENTER);

        setContentPane(conteudo);
        pack();
        setResizable(false);
        setLocationRelativeTo(dono);
    }

    /** Abre a janela e espera a escolha; devolve o tipo do tesouro escolhido. */
    static String escolher(Window dono, String[] opcoes) {
        JanelaBau janela = new JanelaBau(dono, opcoes);
        janela.setVisible(true); // modal: so volta quando o jogador escolher
        return janela.escolhido;
    }

    /** Fileira com os tesouros sorteados; o clique em um deles fecha a janela. */
    private class AreaTesouros extends JPanel {

        private static final long serialVersionUID = 1L;

        private int sobMouse = -1;

        AreaTesouros() {
            setPreferredSize(new Dimension(opcoes.length * (LADO + ESPACO) + ESPACO, LADO + 2 * ESPACO));
            OuvinteMouse ouvinte = new OuvinteMouse();
            addMouseListener(ouvinte);
            addMouseMotionListener(ouvinte);
        }

        private int xDe(int i) {
            int largura = opcoes.length * (LADO + ESPACO) - ESPACO;
            return (getWidth() - largura) / 2 + i * (LADO + ESPACO);
        }

        private int yTopo() {
            return (getHeight() - LADO) / 2;
        }

        private int indiceEm(int x, int y) {
            for (int i = 0; i < opcoes.length; i++) {
                if (x >= xDe(i) && x <= xDe(i) + LADO && y >= yTopo() && y <= yTopo() + LADO) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < opcoes.length; i++) {
                if (i == sobMouse) {
                    g2.setColor(new Color(255, 200, 0, 120));
                    g2.fillOval(xDe(i) - 8, yTopo() - 8, LADO + 16, LADO + 16);
                }
                BufferedImage imagem = SpriteTesouro.de(opcoes[i]);
                if (imagem != null) {
                    g2.drawImage(imagem, xDe(i), yTopo(), LADO, LADO, null);
                }
            }
        }

        /** Eventos tratados como no Cap. 12: classe ouvinte, sem lambda. */
        private class OuvinteMouse extends MouseAdapter {
            @Override
            public void mouseClicked(MouseEvent e) {
                int i = indiceEm(e.getX(), e.getY());
                if (i >= 0) {
                    escolhido = opcoes[i];
                    dispose();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int i = indiceEm(e.getX(), e.getY());
                if (i != sobMouse) {
                    sobMouse = i;
                    setCursor(i >= 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                    repaint();
                }
            }
        }
    }
}
