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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final String CARTAO_ESCOLHA = "escolha";

    private final PainelTabuleiro painel;
    private final PainelTurno painelTurno;
    private final PainelTabuleiroJogador painelJogador;
    private final PainelEscolhaCarta painelEscolha;
    private final PainelCartasAbertas painelAbertas = new PainelCartasAbertas();
    private final PainelTesouros painelTesouros = new PainelTesouros(this);

    private final CardLayout cartoes = new CardLayout();
    private final JPanel centro = new JPanel(cartoes);
    private final JButton botaoTabuleiroJogador = new JButton();
    private final JButton botaoCartas = new JButton();

    public JanelaTabuleiro() {
        super("Herdeiros do Khan");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(LARGURA, ALTURA);
        setLocationRelativeTo(null);

        painel = new PainelTabuleiro("resources/tabuleiro.jpg",
                "resources/paradas_coordenadas.txt", "resources/provincias_coordenadas.txt",
                "resources/cidades_coordenadas.txt", "resources/khan_coordenadas.txt",
                "resources/melhorias_coordenadas.txt", "resources/bonus_coordenadas.txt");
        painelTurno = new PainelTurno();
        painelJogador = new PainelTabuleiroJogador(this);
        painelEscolha = new PainelEscolhaCarta();
        centro.add(painel, CARTAO_MAPA);
        centro.add(painelJogador, CARTAO_JOGADOR);
        centro.add(painelEscolha, CARTAO_ESCOLHA);

        add(painelTurno, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(criarLateral(), BorderLayout.EAST);
        atualizar();
    }

    private JPanel criarLateral() {
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
        botoes.setPreferredSize(new Dimension(170, 124));
        botaoTabuleiroJogador.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        botaoTabuleiroJogador.setPreferredSize(new Dimension(170, 52));
        botaoTabuleiroJogador.addActionListener(new OuvinteTabuleiroJogador());
        botoes.add(botaoTabuleiroJogador);
        botaoCartas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        botaoCartas.setPreferredSize(new Dimension(170, 52));
        botaoCartas.addActionListener(new OuvinteCartas());
        botoes.add(botaoCartas);

        JPanel topo = new JPanel(new BorderLayout(0, 4));
        topo.add(botoes, BorderLayout.NORTH);
        topo.add(painelTesouros, BorderLayout.CENTER);

        JPanel lateral = new JPanel(new BorderLayout(0, 4));
        lateral.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 10));
        lateral.setPreferredSize(new Dimension(190, 0));
        lateral.add(topo, BorderLayout.NORTH);
        lateral.add(painelAbertas, BorderLayout.CENTER);
        return lateral;
    }

    /** Relê o estado do jogo na fachada e redesenha peoes, faixa do turno e tabuleiro do jogador. */
    public void atualizar() {
        painel.setPeoes(peoesDaPartida());
        painel.setTributos(tributosDaPartida());
        painel.setCidadesVisiveis(cidadesVisiveisDaPartida());
        painel.setConquistas(conquistasDaPartida());
        painel.setYurts(yurtsDaPartida());
        painel.setAreaDeMelhorias(areaDaPartida());
        painel.setConselheiros(conselheirosDaPartida());
        atualizarKhan();
        painelTurno.atualizar();
        painelJogador.atualizar();
        painelEscolha.atualizar();
        painelAbertas.atualizar();
        painelTesouros.atualizar();
        atualizarBotao();
    }

    /** Lugar do Khan e, se o jogador da vez precisa usa-lo, os lugares para onde ele pode ir. */
    private void atualizarKhan() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        List<Integer> destinos = new ArrayList<Integer>();
        if (!fachada.temPartida()) {
            painel.setKhan(FachadaModelo.KHAN_FORA, destinos);
            return;
        }
        if (fachada.podeUsarKhanEm(FachadaModelo.KHAN_AREA_MELHORIAS)) {
            destinos.add(FachadaModelo.KHAN_AREA_MELHORIAS);
        }
        for (int id : fachada.getIdsProvinciasDoKhan()) {
            if (fachada.podeUsarKhanEm(id)) {
                destinos.add(id);
            }
        }
        painel.setKhan(fachada.getLocalDoKhan(), destinos);
    }

    private void atualizarBotao() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        if (!fachada.temPartida()) {
            botaoTabuleiroJogador.setText("<html><center>Tabuleiro<br>do jogador</center></html>");
            botaoTabuleiroJogador.setEnabled(false);
            botaoCartas.setText("<html><center>Cartas<br>do jogador</center></html>");
            botaoCartas.setEnabled(false);
            return;
        }
        String heroi = fachada.getHeroiDoJogador(fachada.getJogadorDaVez());
        String nome = heroi.charAt(0) + heroi.substring(1).toLowerCase();
        botaoTabuleiroJogador.setText("<html><center>Tabuleiro de<br>" + nome + "</center></html>");
        botaoCartas.setText("<html><center>Cartas de<br>" + nome + "</center></html>");
        // ninguem joga nem ve cartas enquanto alguem ainda escolhe a carta secreta
        boolean liberado = !fachada.isEscolhaDeCartasPendente();
        botaoTabuleiroJogador.setEnabled(liberado);
        botaoCartas.setEnabled(liberado);
    }

    /** Mostra, no lugar do mapa, a escolha da carta secreta do proximo jogador. */
    public void mostrarEscolhaDeCarta() {
        painelEscolha.atualizar();
        cartoes.show(centro, CARTAO_ESCOLHA);
    }

    /** Aviso temporario na faixa do mapa (por exemplo, por que uma peca de bonus nao pode ser usada). */
    void mostrarMensagem(String texto) {
        painel.mostrarMensagem(texto);
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

    private class OuvinteCartas implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new JanelaCartas(JanelaTabuleiro.this).setVisible(true);
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

    /**
     * A area de melhorias como o Model informa: peca de cada espaco, quais o jogador da vez
     * pode comprar (ou pegar de graca, se ganhou uma por linha completa) e o tamanho da pilha.
     */
    private static AreaNaTela areaDaPartida() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        if (!fachada.temPartida()) {
            return null;
        }
        int n = FachadaModelo.ESPACOS_AREA_MELHORIAS;
        String[] simbolos = new String[n];
        String[] cores = new String[n];
        boolean[] disponiveis = new boolean[n];
        boolean gratuita = fachada.getMelhoriasGratuitasPendentes() > 0;
        for (int i = 0; i < n; i++) {
            simbolos[i] = fachada.getSimboloDaMelhoriaNaArea(i);
            cores[i] = fachada.getCorDaMelhoriaNaArea(i);
            disponiveis[i] = simbolos[i] != null && (gratuita
                    ? fachada.podePegarMelhoriaGratuita(i) : fachada.podeComprarMelhoria(i));
        }
        int pilha = fachada.getTamanhoDaPilhaDeMelhorias();
        boolean pilhaDisponivel = pilha > 0 && (gratuita
                ? fachada.podePegarMelhoriaGratuitaDoTopo() : fachada.podeComprarMelhoriaDoTopo());
        return new AreaNaTela(simbolos, cores, disponiveis, pilha, pilhaDisponivel, gratuita);
    }

    /**
     * Um ConselheiroNaTela por conselheiro: a peca de bonus que ainda esta nele e se o jogador
     * da vez pode entregar tesouros a ele agora (so depois de todos escolherem a carta secreta).
     */
    private static List<ConselheiroNaTela> conselheirosDaPartida() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        List<ConselheiroNaTela> lista = new ArrayList<ConselheiroNaTela>();
        if (!fachada.temPartida()) {
            return lista;
        }
        boolean jogando = !fachada.isEscolhaDeCartasPendente();
        for (int id : fachada.getIdsConselheiros()) {
            lista.add(new ConselheiroNaTela(id, fachada.getBonusNoConselheiro(id),
                    jogando && fachada.podeEntregarEm(id)));
        }
        return lista;
    }

    /** Um YurtNaTela por yurt construido, na cor do dono (o Model so informa ids e cores). */
    private static List<YurtNaTela> yurtsDaPartida() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        List<YurtNaTela> lista = new ArrayList<YurtNaTela>();
        if (!fachada.temPartida()) {
            return lista;
        }
        for (int idParada : fachada.getIdsParadas()) {
            for (String cor : fachada.getCoresDosYurts(idParada)) {
                lista.add(new YurtNaTela(idParada, CoresJogador.de(cor)));
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
        boolean jogando = !fachada.isEscolhaDeCartasPendente();
        for (int id : fachada.getIdsCidadesVisiveis()) {
            List<String> tesouros = new ArrayList<String>();
            for (String tipo : fachada.getTiposDeTesouro()) {
                for (int i = 0; i < fachada.getTesourosDaCidade(id, tipo); i++) {
                    tesouros.add(tipo);
                }
            }
            lista.add(new CidadeNaTela(id, tesouros, jogando && fachada.podeAtacarCidade(id)));
        }
        return lista;
    }

    /** Cidade conquistada -> cor do yurt do conquistador (as sem yurt ficam de fora). */
    private static Map<Integer, Color> conquistasDaPartida() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        Map<Integer, Color> conquistas = new HashMap<Integer, Color>();
        if (!fachada.temPartida()) {
            return conquistas;
        }
        for (int id : fachada.getIdsCidadesConquistadas()) {
            String cor = fachada.getCorDoYurtDaCidade(id);
            if (cor != null) {
                conquistas.put(id, CoresJogador.de(cor));
            }
        }
        return conquistas;
    }

    /**
     * Uma pilha por provincia, com o tipo e a quantidade de pecas que o Model informa,
     * marcada quando o jogador da vez pode pegar uma peca dela agora.
     */
    private static List<PilhaDeTributos> tributosDaPartida() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        List<PilhaDeTributos> lista = new ArrayList<PilhaDeTributos>();
        if (!fachada.temPartida()) {
            return lista;
        }
        for (int id : fachada.getIdsProvincias()) {
            lista.add(new PilhaDeTributos(id, fachada.getTipoDaProvincia(id),
                    fachada.getTributosDaProvincia(id), fachada.podePegarTributo(id)));
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