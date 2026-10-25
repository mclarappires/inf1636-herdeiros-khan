package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controller.ControladorJogo;
import model.FachadaModelo;

/**
 * Faixa no topo: pintada com a cor do jogador da vez, mostra quem joga e a coluna
 * ativada, o inventario dele (moedas, espadas e cavalos) e o botao de passar a vez.
 * A coluna e ativada no tabuleiro do jogador.
 */
class PainelTurno extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Font FONTE = new Font(Font.SANS_SERIF, Font.BOLD, 20);

    private final Inventario inventario = new Inventario();
    private final JButton botaoPassar = new JButton("Passar a vez");

    private Color corDaVez = Color.GRAY;
    private String texto = "";

    PainelTurno() {
        setPreferredSize(new Dimension(0, 60));
        setLayout(new BorderLayout());

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        direita.setOpaque(false);
        botaoPassar.setFont(FONTE.deriveFont(16f));
        botaoPassar.setPreferredSize(new Dimension(150, 40));
        botaoPassar.addActionListener(new OuvintePassar());
        direita.add(inventario);
        direita.add(botaoPassar);
        add(direita, BorderLayout.EAST);
        atualizar();
    }

    /** Relê o estado do jogo e redesenha a faixa. */
    void atualizar() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        if (!fachada.temPartida()) {
            corDaVez = Color.GRAY;
            texto = "Sem partida (modo de teste)";
            inventario.setVisible(false);
            botaoPassar.setEnabled(false);
        } else {
            int vez = fachada.getJogadorDaVez();
            corDaVez = CoresJogador.de(fachada.getCorDoJogador(vez));
            String ativa = fachada.getColunaAtiva();
            texto = "Vez de " + formatar(fachada.getHeroiDoJogador(vez)) + "   |   "
                    + (ativa == null ? "Ative uma coluna"
                            : "Coluna " + PainelTabuleiroJogador.nomeDaColuna(ativa));
            inventario.mostrar(fachada.getTributoDisponivel(vez, "MOEDA"),
                    fachada.getTributoDisponivel(vez, "ESPADA"), fachada.getTributoDisponivel(vez, "YURT"),
                    fachada.getMovimentosRestantes(), fachada.getPunhosRestantes());
            inventario.setVisible(true);
            botaoPassar.setEnabled(true);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(corDaVez);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(corDoTexto(corDaVez));
        g2.setFont(FONTE);
        FontMetrics medidas = g2.getFontMetrics();
        g2.drawString(texto, 16, (getHeight() + medidas.getAscent() - medidas.getDescent()) / 2);
    }

    /** Texto claro em fundo escuro e vice-versa. */
    private static Color corDoTexto(Color fundo) {
        double luminosidade = 0.299 * fundo.getRed() + 0.587 * fundo.getGreen()
                + 0.114 * fundo.getBlue();
        return luminosidade < 140 ? Color.WHITE : Color.BLACK;
    }

    /** "VERMELHA" vira "Vermelha". */
    private static String formatar(String nome) {
        return nome.charAt(0) + nome.substring(1).toLowerCase();
    }

    /** Moedas, espadas, yurts, cavalos e punhos do jogador da vez, cada um numa pilula clara. */
    private static class Inventario extends JPanel {

        private static final long serialVersionUID = 1L;

        private static final int ALTURA = 40;
        private static final int LARGURA_ITEM = 70;
        private static final int LARGURA_CAVALOS = 110;
        private static final int LARGURA_PUNHOS = 105;
        private static final int ESPACO = 6;
        private static final Font FONTE_NUMERO = new Font(Font.SANS_SERIF, Font.BOLD, 18);

        private final BufferedImage moeda = SpriteTributo.criar("MOEDA");
        private final BufferedImage espada = SpriteTributo.criar("ESPADA");
        private final BufferedImage yurt = SpriteTributo.criar("YURT");
        private int moedas, espadas, yurts, cavalos, punhos;

        Inventario() {
            setOpaque(false);
            setPreferredSize(new Dimension(3 * LARGURA_ITEM + LARGURA_CAVALOS + LARGURA_PUNHOS + 4 * ESPACO,
                    ALTURA));
        }

        void mostrar(int moedas, int espadas, int yurts, int cavalos, int punhos) {
            this.moedas = moedas;
            this.espadas = espadas;
            this.yurts = yurts;
            this.cavalos = cavalos;
            this.punhos = punhos;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int x = 0;
            desenharItem(g2, x, LARGURA_ITEM, moeda, null, moedas);
            x += LARGURA_ITEM + ESPACO;
            desenharItem(g2, x, LARGURA_ITEM, espada, null, espadas);
            x += LARGURA_ITEM + ESPACO;
            desenharItem(g2, x, LARGURA_ITEM, yurt, null, yurts);
            x += LARGURA_ITEM + ESPACO;
            // sem imagem de cavalo e de punho: rotulo em texto
            desenharItem(g2, x, LARGURA_CAVALOS, null, "Cavalos", cavalos);
            x += LARGURA_CAVALOS + ESPACO;
            desenharItem(g2, x, LARGURA_PUNHOS, null, "Punhos", punhos);
        }

        /** Pilula com o icone (ou um rotulo, se nao houver icone) e a quantidade. */
        private void desenharItem(Graphics2D g2, int x, int largura, BufferedImage icone,
                String rotulo, int quantidade) {
            g2.setColor(new Color(255, 255, 255, 210));
            g2.fillRoundRect(x, 0, largura, ALTURA, ALTURA, ALTURA);
            g2.setColor(new Color(60, 40, 20));
            g2.drawRoundRect(x, 0, largura - 1, ALTURA - 1, ALTURA, ALTURA);

            int fim = x + 10;
            if (icone != null) {
                int lado = ALTURA - 8;
                g2.drawImage(icone, x + 6, 4, lado, lado, null);
                fim = x + 6 + lado + 8;
            } else if (rotulo != null) {
                g2.setFont(FONTE_NUMERO.deriveFont(Font.PLAIN, 15f));
                FontMetrics m = g2.getFontMetrics();
                g2.drawString(rotulo, fim, (ALTURA + m.getAscent() - m.getDescent()) / 2);
                fim += m.stringWidth(rotulo) + 8;
            }
            g2.setFont(FONTE_NUMERO);
            FontMetrics m = g2.getFontMetrics();
            g2.drawString(String.valueOf(quantidade), fim,
                    (ALTURA + m.getAscent() - m.getDescent()) / 2);
        }
    }

    private class OuvintePassar implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String motivo = ControladorJogo.getInstancia().passarTurno();
            if (motivo != null) {
                JOptionPane.showMessageDialog(PainelTurno.this, motivo, "Passar a vez",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
