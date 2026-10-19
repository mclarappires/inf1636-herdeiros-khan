package view;

import java.awt.BorderLayout;
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

    private final PainelTabuleiro painel;
    private final PainelTurno painelTurno;

    public JanelaTabuleiro() {
        super("Herdeiros do Khan");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(LARGURA, ALTURA);
        setLocationRelativeTo(null);

        painel = new PainelTabuleiro("resources/tabuleiro.jpg",
                "resources/paradas_coordenadas.txt", "resources/provincias_coordenadas.txt",
                "resources/cidades_coordenadas.txt");
        painelTurno = new PainelTurno();
        add(painelTurno, BorderLayout.NORTH);
        add(painel, BorderLayout.CENTER);
        atualizar();
    }

    /** Relê o estado do jogo na fachada e redesenha peoes e faixa do turno. */
    public void atualizar() {
        painel.setPeoes(peoesDaPartida());
        painel.setTributos(tributosDaPartida());
        painel.setCidadesVisiveis(cidadesVisiveisDaPartida());
        painelTurno.atualizar();
    }

    /**
     * Um PeaoNaTela por peao de cada jogador, na parada onde o Model diz que ele esta.
     * Sem partida iniciada (main desta classe), usa peoes de teste.
     */
    private static List<PeaoNaTela> peoesDaPartida() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        if (!fachada.temPartida()) {
            return peoesDeTeste();
        }
        List<PeaoNaTela> lista = new ArrayList<PeaoNaTela>();
        for (int i = 0; i < fachada.getQuantidadeJogadores(); i++) {
            Color cor = CoresJogador.de(fachada.getCorDoJogador(i));
            for (int k = 0; k < fachada.getQuantidadePeoes(i); k++) {
                lista.add(new PeaoNaTela(fachada.getParadaDoPeao(i, k), cor));
            }
        }
        return lista;
    }

    /** Cidades reveladas, cada uma com os tesouros que o Model diz que ainda estao nela. */
    private static List<CidadeNaTela> cidadesVisiveisDaPartida() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        List<CidadeNaTela> lista = new ArrayList<CidadeNaTela>();
        if (!fachada.temPartida()) {
            return lista;
        }
        for (int id : fachada.getIdsCidadesVisiveis()) {
            List<String> tesouros = new ArrayList<String>();
            for (String tipo : fachada.getTiposDeTesouro()) {
                for (int i = 0; i < fachada.getTesourosDaCidade(id, tipo); i++) {
                    tesouros.add(tipo);
                }
            }
            lista.add(new CidadeNaTela(id, tesouros));
        }
        return lista;
    }

    /** Uma pilha por provincia, com o tipo e a quantidade de pecas que o Model informa. */
    private static List<PilhaDeTributos> tributosDaPartida() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        List<PilhaDeTributos> lista = new ArrayList<PilhaDeTributos>();
        if (!fachada.temPartida()) {
            return lista;
        }
        for (int id : fachada.getIdsProvincias()) {
            lista.add(new PilhaDeTributos(id, fachada.getTipoDaProvincia(id),
                    fachada.getTributosDaProvincia(id)));
        }
        return lista;
    }

    /** TEMPORARIO: peoes fixos so para ver o desenho funcionando sem partida. */
    private static List<PeaoNaTela> peoesDeTeste() {
        List<PeaoNaTela> lista = new ArrayList<PeaoNaTela>();
        lista.add(new PeaoNaTela(1, new Color(40, 90, 200)));
        lista.add(new PeaoNaTela(1, new Color(200, 40, 40)));
        lista.add(new PeaoNaTela(1, new Color(40, 150, 60)));
        lista.add(new PeaoNaTela(1, new Color(235, 195, 40)));
        lista.add(new PeaoNaTela(13, new Color(30, 30, 30)));
        lista.add(new PeaoNaTela(13, new Color(200, 40, 40)));
        lista.add(new PeaoNaTela(17, new Color(40, 90, 200)));
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