package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import model.FachadaModelo;

/**
 * Faixa no topo: pintada com a cor do jogador da vez, mostra quem joga, a coluna
 * ativada e os movimentos restantes. A coluna e ativada no tabuleiro do jogador.
 */
class PainelTurno extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Font FONTE = new Font(Font.SANS_SERIF, Font.BOLD, 20);

    private Color corDaVez = Color.GRAY;
    private String texto = "";

    PainelTurno() {
        setPreferredSize(new Dimension(0, 60));
        atualizar();
    }

    /** Relê o estado do jogo e redesenha a faixa. */
    void atualizar() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        if (!fachada.temPartida()) {
            corDaVez = Color.GRAY;
            texto = "Sem partida (modo de teste)";
        } else {
            int vez = fachada.getJogadorDaVez();
            corDaVez = CoresJogador.de(fachada.getCorDoJogador(vez));
            String ativa = fachada.getColunaAtiva();
            texto = "Vez de " + formatar(fachada.getHeroiDoJogador(vez));
            if (ativa == null) {
                texto += "   |   Abra seu tabuleiro para ativar uma coluna";
            } else {
                texto += "   |   Coluna " + PainelTabuleiroJogador.nomeDaColuna(ativa)
                        + "   |   Movimentos: " + fachada.getMovimentosRestantes();
            }
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

        // texto claro em fundo escuro e vice-versa
        double luminosidade = 0.299 * corDaVez.getRed() + 0.587 * corDaVez.getGreen()
                + 0.114 * corDaVez.getBlue();
        g2.setColor(luminosidade < 140 ? Color.WHITE : Color.BLACK);
        g2.setFont(FONTE);
        FontMetrics medidas = g2.getFontMetrics();
        g2.drawString(texto, 16, (getHeight() + medidas.getAscent() - medidas.getDescent()) / 2);
    }

    /** "VERMELHA" vira "Vermelha". */
    private static String formatar(String nome) {
        return nome.charAt(0) + nome.substring(1).toLowerCase();
    }
}
