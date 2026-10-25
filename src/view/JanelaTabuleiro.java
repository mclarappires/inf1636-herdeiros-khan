package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import model.FachadaModelo;

/**
 * Janela principal do tabuleiro (maximo 1400 x 1050). No centro fica o mapa ou, no lugar
 * dele, o tabuleiro do jogador da vez, aberto pelo botao da direita.
 */
public class JanelaTabuleiro extends JFrame {

    private static final long serialVersionUID = 1L;

    public static final int LARGURA = 1200;
    public static final int ALTURA = 900;

    private static final String CARTAO_MAPA = "mapa";
    private static final String CARTAO_JOGADOR = "jogador";

    private final PainelTabuleiro painel;
    private final PainelTurno painelTurno;
    private final PainelTabuleiroJogador painelJogador;

    private final CardLayout cartoes = new CardLayout();
    private final JPanel centro = new JPanel(cartoes);
    private final JButton botaoTabuleiroJogador = new JButton();

    public JanelaTabuleiro() {
        super("Herdeiros do Khan");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(LARGURA, ALTURA);
        setLocationRelativeTo(null);

        painel = new PainelTabuleiro("resources/tabuleiro.jpg",
                "resources/paradas_coordenadas.txt", "resources/provincias_coordenadas.txt",
                "resources/cidades_coordenadas.txt");
        painelTurno = new PainelTurno();
        painelJogador = new PainelTabuleiroJogador(this);
        centro.add(painel, CARTAO_MAPA);
        centro.add(painelJogador, CARTAO_JOGADOR);

        add(painelTurno, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(criarLateral(), BorderLayout.EAST);
        atualizar();
    }

    private JPanel criarLateral() {
        JPanel lateral = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 16));
        lateral.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        lateral.setPreferredSize(new Dimension(190, 0));
        botaoTabuleiroJogador.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        botaoTabuleiroJogador.setPreferredSize(new Dimension(170, 60));
        botaoTabuleiroJogador.addActionListener(new OuvinteTabuleiroJogador());
        lateral.add(botaoTabuleiroJogador);
        return lateral;
    }

    /** Relê o estado do jogo na fachada e redesenha peoes, faixa do turno e tabuleiro do jogador. */
    public void atualizar() {
        painel.setPeoes(peoesDaPartida());
        painel.setTributos(tributosDaPartida());
        painel.setCidadesVisiveis(cidadesVisiveisDaPartida());
        painelTurno.atualizar();
        painelJogador.atualizar();
        atualizarBotao();
    }

    private void atualizarBotao() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        if (!fachada.temPartida()) {
            botaoTabuleiroJogador.setText("<html><center>Tabuleiro<br>do jogador</center></html>");
            botaoTabuleiroJogador.setEnabled(false);
            return;
        }
        String heroi = fachada.getHeroiDoJogador(fachada.getJogadorDaVez());
        botaoTabuleiroJogador.setText("<html><center>Tabuleiro de<br>"
                + heroi.charAt(0) + heroi.substring(1).toLowerCase() + "</center></html>");
        botaoTabuleiroJogador.setEnabled(true);
    }

    /** Volta a mostrar o mapa no centro da janela. */
    public void mostrarMapa() {
        cartoes.show(centro, CARTAO_MAPA);
    }

    /** Mostra o tabuleiro do jogador da vez no lugar do mapa. */
    public void mostrarTabuleiroJogador() {
        painelJogador.atualizar();
        cartoes.show(centro, CARTAO_JOGADOR);
    }

    private class OuvinteTabuleiroJogador implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            mostrarTabuleiroJogador();
        }
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
            int quantidade = fachada.getQuantidadePeoes(i);
            for (int k = 0; k < quantidade; k++) {
                // numerados so quando o jogador tem mais de um peao (Altani)
                int numero = quantidade > 1 ? k + 1 : 0;
                lista.add(new PeaoNaTela(fachada.getParadaDoPeao(i, k), cor, numero));
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