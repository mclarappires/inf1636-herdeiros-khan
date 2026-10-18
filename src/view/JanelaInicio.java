package view;

import java.awt.CardLayout;
import java.awt.Container;

import javax.swing.JFrame;

/** Janela de abertura: tela de inicio e selecao de personagens, trocadas por CardLayout. */
public class JanelaInicio extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String CARTAO_INICIO = "inicio";
    private static final String CARTAO_SELECAO = "selecao";

    private final CardLayout cartoes = new CardLayout();

    public JanelaInicio() {
        super("Herdeiros do Khan");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(Janelatabuleiro.LARGURA, Janelatabuleiro.ALTURA);
        setLocationRelativeTo(null);

        Container conteudo = getContentPane();
        conteudo.setLayout(cartoes);
        conteudo.add(new PainelInicio(this), CARTAO_INICIO);
        conteudo.add(new PainelSelecao(this), CARTAO_SELECAO);
    }

    public void mostrarInicio() {
        cartoes.show(getContentPane(), CARTAO_INICIO);
    }

    public void mostrarSelecao() {
        cartoes.show(getContentPane(), CARTAO_SELECAO);
    }
}
