package view;

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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import controller.ControladorJogo;
import model.FachadaModelo;

/**
 * Faixa no topo: pintada com a cor do jogador da vez, mostra quem joga, a coluna
 * ativada e os movimentos restantes, e traz o controle para ativar uma coluna.
 */
class PainelTurno extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Font FONTE = new Font(Font.SANS_SERIF, Font.BOLD, 20);

    private final String[] colunas;
    private final JComboBox<String> comboColuna = new JComboBox<String>();
    private final JButton botaoAtivar = new JButton("Ativar coluna");

    private Color corDaVez = Color.GRAY;
    private String texto = "";

    PainelTurno() {
        colunas = FachadaModelo.getInstancia().getColunas();
        setPreferredSize(new Dimension(0, 60));
        setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 14));

        for (String c : colunas) {
            comboColuna.addItem(formatar(c));
        }
        botaoAtivar.addActionListener(new OuvinteAtivar());
        add(comboColuna);
        add(botaoAtivar);
        atualizar();
    }

    /** Relê o estado do jogo e redesenha a faixa. */
    void atualizar() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        if (!fachada.temPartida()) {
            corDaVez = Color.GRAY;
            texto = "Sem partida (modo de teste)";
            botaoAtivar.setEnabled(false);
            comboColuna.setEnabled(false);
        } else {
            int vez = fachada.getJogadorDaVez();
            corDaVez = CoresJogador.de(fachada.getCorDoJogador(vez));
            String ativa = fachada.getColunaAtiva();
            texto = "Vez de " + formatar(fachada.getHeroiDoJogador(vez))
                    + "   |   Coluna: " + (ativa == null ? "nenhuma" : formatar(ativa))
                    + "   |   Movimentos: " + fachada.getMovimentosRestantes();
            botaoAtivar.setEnabled(ativa == null);
            comboColuna.setEnabled(ativa == null);
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

    private class OuvinteAtivar implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ControladorJogo.getInstancia().ativarColuna(colunas[comboColuna.getSelectedIndex()]);
        }
    }
}