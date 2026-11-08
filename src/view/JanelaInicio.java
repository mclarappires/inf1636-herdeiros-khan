package view;

import javax.swing.JFrame;

/**
 * Primeira janela: novo jogo, carregar jogo ou sair. Ao escolher "Novo jogo", o Controller
 * fecha esta janela e abre a JanelaSelecao (escolha dos personagens).
 */
public class JanelaInicio extends JFrame {

    private static final long serialVersionUID = 1L;

    public JanelaInicio() {
        super("Herdeiros do Khan");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(JanelaTabuleiro.LARGURA, JanelaTabuleiro.ALTURA);
        setLocationRelativeTo(null);
        setContentPane(new PainelInicio());
    }
}
