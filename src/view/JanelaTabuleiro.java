package view;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import model.FachadaModelo;

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
        PainelTabuleiro painel = new PainelTabuleiro("resources/tabuleiro.jpg",
                "resources/paradas_coordenadas.txt");
        add(painel);
        painel.setPeoes(peoesDaPartida());
    }

    /** Id da parada Karakorum no mapa; la comecam todos os peoes. */
    private static final int KARAKORUM = 1;

    /**
     * Um peao por jogador, na cor escolhida na selecao. TEMPORARIO: todos em
     * Karakorum (posicao inicial do manual) ate a fachada informar a parada de cada peao.
     * Sem partida iniciada (main desta classe), usa os peoes de teste.
     */
    private static List<PeaoNaTela> peoesDaPartida() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        if (!fachada.temPartida()) {
            return peoesDeTeste();
        }
        List<PeaoNaTela> lista = new ArrayList<PeaoNaTela>();
        for (int i = 0; i < fachada.getQuantidadeJogadores(); i++) {
            lista.add(new PeaoNaTela(KARAKORUM, CoresJogador.de(fachada.getCorDoJogador(i))));
        }
        return lista;
    }

    /**
     * TEMPORARIO: peoes fixos so para ver o desenho funcionando.
     * Depois o Controller monta esta lista a partir do Model.
     */
    private static List<PeaoNaTela> peoesDeTeste() {
        List<PeaoNaTela> lista = new ArrayList<PeaoNaTela>();
        lista.add(new PeaoNaTela(1, new Color(40, 90, 200)));   // azul em Karakorum
        lista.add(new PeaoNaTela(1, new Color(200, 40, 40)));   // vermelho em Karakorum
        lista.add(new PeaoNaTela(1, new Color(40, 150, 60)));   // verde em Karakorum
        lista.add(new PeaoNaTela(1, new Color(235, 195, 40)));  // amarelo em Karakorum
        lista.add(new PeaoNaTela(13, new Color(30, 30, 30)));   // preto numa parada dupla
        lista.add(new PeaoNaTela(13, new Color(200, 40, 40)));  // vermelho na mesma dupla
        lista.add(new PeaoNaTela(17, new Color(40, 90, 200)));  // azul numa parada simples
        return lista;
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