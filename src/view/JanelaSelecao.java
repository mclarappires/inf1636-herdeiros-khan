package view;

import javax.swing.JFrame;

/**
 * Segunda janela: escolha do numero de jogadores e dos personagens. Aberta pelo Controller
 * depois que a janela inicial fecha; ao comecar a partida, ela fecha e o tabuleiro abre.
 */
public class JanelaSelecao extends JFrame {

    private static final long serialVersionUID = 1L;

    public JanelaSelecao() {
        super("Herdeiros do Khan - Personagens");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(JanelaTabuleiro.LARGURA, JanelaTabuleiro.ALTURA);
        setLocationRelativeTo(null);
        setContentPane(new PainelSelecao());
    }
}
