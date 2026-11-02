package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import controller.ControladorJogo;
import model.FachadaModelo;

/**
 * Antes da primeira rodada: um jogador por vez ve as suas 2 cartas de conselheiro secreto e
 * clica na que vai manter. As cartas so aparecem depois do botao, para o jogador anterior
 * nao ver as do proximo. Aparece no lugar do mapa (ver JanelaTabuleiro).
 */
class PainelEscolhaCarta extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Font FONTE = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
    private static final Font FONTE_TITULO = new Font(Font.SERIF, Font.BOLD, 26);
    private static final Color DOURADO = new Color(255, 200, 40);

    private final JLabel titulo = new JLabel();
    private final JLabel instrucao = new JLabel();
    private final JButton botaoVer = new JButton("Ver minhas cartas");
    private final AreaCartas area = new AreaCartas();

    private int jogadorMostrado = -1;
    private boolean revelada = false;

    PainelEscolhaCarta() {
        setLayout(new BorderLayout());
        add(criarTopo(), BorderLayout.NORTH);
        add(area, BorderLayout.CENTER);
        atualizar();
    }

    private JPanel criarTopo() {
        JPanel topo = new JPanel(new BorderLayout(12, 0));
        topo.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JPanel textos = new JPanel(new BorderLayout(0, 4));
        titulo.setFont(FONTE_TITULO);
        instrucao.setFont(FONTE);
        textos.add(titulo, BorderLayout.NORTH);
        textos.add(instrucao, BorderLayout.SOUTH);
        topo.add(textos, BorderLayout.WEST);

        botaoVer.setFont(FONTE.deriveFont(Font.BOLD));
        botaoVer.addActionListener(new OuvinteVer());
        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        direita.add(botaoVer);
        topo.add(direita, BorderLayout.EAST);
        return topo;
    }

    /** Relê na fachada quem ainda precisa escolher; ao trocar de jogador, esconde as cartas. */
    void atualizar() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        int jogador = fachada.temPartida() ? fachada.getProximoJogadorSemCartaSecreta() : -1;
        if (jogador != jogadorMostrado) {
            jogadorMostrado = jogador;
            revelada = false;
            area.limparCartas();
        }
        if (jogador < 0) {
            titulo.setText(" ");
            instrucao.setText(" ");
            botaoVer.setVisible(false);
            repaint();
            return;
        }
        String heroi = fachada.getHeroiDoJogador(jogador);
        titulo.setText("Carta secreta de " + heroi.charAt(0) + heroi.substring(1).toLowerCase());
        instrucao.setText(revelada
                ? "Clique na carta que deseja manter. A outra será descartada."
                : "Passe o computador para este jogador e clique em \"Ver minhas cartas\".");
        botaoVer.setVisible(!revelada);
        repaint();
    }

    private void escolher(String idCarta) {
        String motivo = ControladorJogo.getInstancia().escolherCartaSecreta(idCarta);
        if (motivo != null) {
            instrucao.setText(motivo);
        }
    }

    /** As 2 opcoes do jogador, desenhadas com drawImage; um clique numa delas e a escolha. */
    private class AreaCartas extends JPanel {

        private static final long serialVersionUID = 1L;

        private static final int MARGEM = 30;

        private String[] ids = new String[0];
        private Rectangle[] onde = new Rectangle[0];
        private int sobMouse = -1;

        AreaCartas() {
            setBackground(new Color(40, 30, 25));
            OuvinteMouse ouvinte = new OuvinteMouse();
            addMouseListener(ouvinte);
            addMouseMotionListener(ouvinte);
        }

        void limparCartas() {
            ids = new String[0];
            onde = new Rectangle[0];
            sobMouse = -1;
            setCursor(Cursor.getDefaultCursor());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!revelada || jogadorMostrado < 0) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            ids = FachadaModelo.getInstancia().getOpcoesDeCartaSecreta(jogadorMostrado);
            onde = new Rectangle[ids.length];
            if (ids.length == 0) {
                return;
            }
            int largura = (getWidth() - (ids.length + 1) * MARGEM) / ids.length;
            int altura = getHeight() - 2 * MARGEM;
            for (int i = 0; i < ids.length; i++) {
                int x = MARGEM + i * (largura + MARGEM);
                onde[i] = SpriteCarta.desenhar(g2, SpriteCarta.secreta(ids[i]), x, MARGEM, largura, altura);
                if (i == sobMouse) {
                    g2.setColor(DOURADO);
                    g2.setStroke(new BasicStroke(5f));
                    g2.drawRect(onde[i].x, onde[i].y, onde[i].width, onde[i].height);
                }
            }
        }

        private int cartaEm(int x, int y) {
            for (int i = 0; i < onde.length; i++) {
                if (onde[i] != null && onde[i].contains(x, y)) {
                    return i;
                }
            }
            return -1;
        }

        /** Eventos tratados como no Cap. 12: classe ouvinte, sem lambda. */
        private class OuvinteMouse extends MouseAdapter {
            @Override
            public void mouseClicked(MouseEvent e) {
                int i = cartaEm(e.getX(), e.getY());
                if (i >= 0) {
                    escolher(ids[i]);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int i = cartaEm(e.getX(), e.getY());
                if (i != sobMouse) {
                    sobMouse = i;
                    setCursor(i >= 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                            : Cursor.getDefaultCursor());
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                sobMouse = -1;
                setCursor(Cursor.getDefaultCursor());
                repaint();
            }
        }
    }

    private class OuvinteVer implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            revelada = true;
            atualizar();
        }
    }
}
