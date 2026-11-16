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
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import controller.ControladorJogo;
import model.FachadaModelo;

/**
 * Antes da primeira rodada: um jogador por vez ve as suas 2 cartas de conselheiro secreto e
 * clica na que vai manter. As cartas so aparecem depois do botao, para o jogador anterior
 * nao ver as do proximo. Em seguida o Jochi ve as 2 melhorias reveladas e clica na que vai
 * para o 1o espaco da coluna branca (nao sao secretas, aparecem direto). Aparece no lugar do
 * mapa (ver JanelaTabuleiro).
 */
class PainelEscolhaCarta extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Font FONTE = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
    private static final Font FONTE_TITULO = new Font(Font.SERIF, Font.BOLD, 26);
    private static final Color DOURADO = new Color(255, 200, 40);

    private final JLabel titulo = new JLabel();
    private final JLabel instrucao = new JLabel();
    private final JButton botaoVer = new JButton("Ver minhas cartas");
    private final AreaOpcoes area = new AreaOpcoes();

    private int jogadorMostrado = -1;
    private boolean escolhendoMelhoria = false; // false: carta secreta; true: melhoria inicial do Jochi
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

    /** Rele na fachada quem ainda precisa escolher e o que; ao trocar, esconde as opcoes. */
    void atualizar() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        int jogador = fachada.temPartida() ? fachada.getProximoJogadorComEscolhaPendente() : -1;
        boolean melhoria = jogador >= 0 && fachada.getCartaSecretaDoJogador(jogador) != null;
        if (jogador != jogadorMostrado || melhoria != escolhendoMelhoria) {
            jogadorMostrado = jogador;
            escolhendoMelhoria = melhoria;
            revelada = melhoria; // as melhorias nao sao secretas
            area.limparOpcoes();
        }
        if (jogador < 0) {
            titulo.setText(" ");
            instrucao.setText(" ");
            botaoVer.setVisible(false);
            repaint();
            return;
        }
        String heroi = fachada.getHeroiDoJogador(jogador);
        String nome = heroi.charAt(0) + heroi.substring(1).toLowerCase();
        if (escolhendoMelhoria) {
            titulo.setText("Melhoria inicial de " + nome);
            instrucao.setText("Clique na melhoria que vai para o 1º espaço da coluna branca."
                    + " A outra volta para o fundo da pilha.");
        } else {
            titulo.setText("Carta secreta de " + nome);
            instrucao.setText(revelada
                    ? "Clique na carta que deseja manter. A outra será descartada."
                    : "Passe o computador para este jogador e clique em \"Ver minhas cartas\".");
        }
        botaoVer.setVisible(!revelada);
        repaint();
    }

    private void escolher(int indice, String idCarta) {
        ControladorJogo controlador = ControladorJogo.getInstancia();
        String motivo = escolhendoMelhoria ? controlador.escolherMelhoriaInicial(indice)
                : controlador.escolherCartaSecreta(idCarta);
        if (motivo != null) {
            instrucao.setText(motivo);
        }
    }

    /**
     * As opcoes do jogador (2 cartas ou 2 melhorias), desenhadas com drawImage; um clique numa
     * delas e a escolha.
     */
    private class AreaOpcoes extends JPanel {

        private static final long serialVersionUID = 1L;

        private static final int MARGEM = 30;
        private static final int ALTURA_MELHORIA = 200;

        private String[] ids = new String[0]; // ids das cartas (vazio na escolha da melhoria)
        private Rectangle[] onde = new Rectangle[0];
        private int sobMouse = -1;

        AreaOpcoes() {
            setBackground(new Color(40, 30, 25));
            OuvinteMouse ouvinte = new OuvinteMouse();
            addMouseListener(ouvinte);
            addMouseMotionListener(ouvinte);
        }

        void limparOpcoes() {
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
            if (escolhendoMelhoria) {
                desenharMelhorias(g2);
            } else {
                desenharCartas(g2);
            }
            if (sobMouse >= 0 && sobMouse < onde.length && onde[sobMouse] != null) {
                Rectangle r = onde[sobMouse];
                g2.setColor(DOURADO);
                g2.setStroke(new BasicStroke(5f));
                g2.drawRect(r.x, r.y, r.width, r.height);
            }
        }

        private void desenharCartas(Graphics2D g2) {
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
            }
        }

        /** As melhorias lado a lado, no centro, mantendo a proporcao de cada imagem. */
        private void desenharMelhorias(Graphics2D g2) {
            FachadaModelo fachada = FachadaModelo.getInstancia();
            String[] simbolos = fachada.getSimbolosDasOpcoesIniciais(jogadorMostrado);
            String[] cores = fachada.getCoresDasOpcoesIniciais(jogadorMostrado);
            ids = new String[0];
            onde = new Rectangle[simbolos.length];
            if (simbolos.length == 0) {
                return;
            }
            int altura = Math.min(ALTURA_MELHORIA, getHeight() - 2 * MARGEM);
            int espaco = getWidth() / (simbolos.length + 1);
            for (int i = 0; i < simbolos.length; i++) {
                BufferedImage peca = SpriteMelhoria.criar(simbolos[i], cores[i]);
                int largura = peca == null ? altura
                        : (int) ((double) altura * peca.getWidth() / peca.getHeight());
                int x = espaco * (i + 1) - largura / 2;
                int y = (getHeight() - altura) / 2;
                onde[i] = new Rectangle(x, y, largura, altura);
                if (peca != null) {
                    g2.drawImage(peca, x, y, largura, altura, null);
                } else { // sem imagem: o nome da melhoria num quadro
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.fillRect(x, y, largura, altura);
                    g2.setColor(Color.BLACK);
                    g2.setFont(FONTE);
                    g2.drawString(simbolos[i], x + 10, y + altura / 2);
                }
            }
        }

        private int opcaoEm(int x, int y) {
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
                int i = opcaoEm(e.getX(), e.getY());
                if (i >= 0) {
                    escolher(i, i < ids.length ? ids[i] : null);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int i = opcaoEm(e.getX(), e.getY());
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
