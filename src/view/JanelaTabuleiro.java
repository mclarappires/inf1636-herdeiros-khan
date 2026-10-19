package view;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Janela principal do tabuleiro (maximo 1400 x 1050). */
public class JanelaTabuleiro extends JFrame {

    private static final long serialVersionUID = 1L;

    public static final int LARGURA = 1200;
    public static final int ALTURA = 900;

    public JanelaTabuleiro() {
        super("Herdeiros do Khan");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(LARGURA, ALTURA);
        setLocationRelativeTo(null);
        add(new PainelTabuleiro("resources/tabuleiro.jpg",
                "resources/paradas_coordenadas.txt"));
    }

    public static void main(String[] args) {
        // Runnable de classe anonima (lambda e proibido no enunciado)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JanelaTabuleiro().setVisible(true);
            }
        });
    }
}