package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
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
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;

import controller.ControladorJogo;
import model.FachadaModelo;

/**
 * Contagem final desenhada na janela: votos da trilha, influencia em cada regiao, carta secreta
 * revelada e total de cada jogador, com o vencedor destacado. Aparece quando a ultima rodada acaba.
 */
class PainelResultado extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Font FONTE_TITULO = new Font(Font.SERIF, Font.BOLD, 34);
    private static final Font FONTE = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
    private static final Font FONTE_NEGRITO = FONTE.deriveFont(Font.BOLD);
    private static final Color FUNDO = new Color(40, 30, 25);
    private static final Color DOURADO = new Color(255, 200, 40);

    private static final int MARGEM = 40;
    private static final int ALTURA_LINHA = 46;
    // largura de cada coluna da tabela, em fracao da largura util (soma 1)
    private static final double[] COLUNAS = {0.20, 0.10, 0.15, 0.15, 0.15, 0.15, 0.10};
    private static final String[] TITULOS = {"Jogador", "Trilha", null, null, null, "Carta secreta", "Total"};

    private static final Map<String, String> NOMES_REGIAO = new HashMap<String, String>();

    static {
        NOMES_REGIAO.put("RUSSIA", "Rússia");
        NOMES_REGIAO.put("CHINA", "China");
        NOMES_REGIAO.put("PERSIA", "Pérsia");
    }

    // copia do resultado, lida da fachada em atualizar() para o desenho nao consultar o Model a cada repaint
    private String[] herois = new String[0];
    private Color[] cores = new Color[0];
    private int[] trilha = new int[0];
    private int[][] yurts = new int[0][];
    private int[][] influencia = new int[0][];
    private String[] cartas = new String[0];
    private String[] idsCartas = new String[0];
    private int[] votosCartas = new int[0];
    private int[] totais = new int[0];
    private boolean[] vencedor = new boolean[0];
    private String[] regioes = new String[0];

    PainelResultado() {
        setBackground(FUNDO);
        setLayout(new BorderLayout());
        JButton sair = new JButton("Sair do jogo");
        sair.setFont(FONTE_NEGRITO.deriveFont(16f));
        sair.addActionListener(new OuvinteSair());
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        rodape.setOpaque(false);
        rodape.add(sair);
        add(rodape, BorderLayout.SOUTH);
    }

    /** Relê a contagem final na fachada; so tem o que mostrar depois que a partida acaba. */
    void atualizar() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        if (!fachada.temPartida() || !fachada.isPartidaEncerrada()) {
            return;
        }
        int n = fachada.getQuantidadeJogadores();
        regioes = fachada.getRegioes();
        herois = new String[n];
        cores = new Color[n];
        trilha = new int[n];
        yurts = new int[n][regioes.length];
        influencia = new int[n][regioes.length];
        cartas = new String[n];
        idsCartas = new String[n];
        votosCartas = new int[n];
        totais = new int[n];
        vencedor = new boolean[n];
        for (int i = 0; i < n; i++) {
            herois[i] = formatar(fachada.getHeroiDoJogador(i));
            cores[i] = CoresJogador.de(fachada.getCorDoJogador(i));
            trilha[i] = fachada.getVotosDoJogador(i);
            for (int r = 0; r < regioes.length; r++) {
                yurts[i][r] = fachada.getYurtsNaRegiao(i, regioes[r]);
                influencia[i][r] = fachada.getVotosDeInfluencia(i, regioes[r]);
            }
            cartas[i] = fachada.getNomeDaCartaSecreta(i);
            idsCartas[i] = fachada.getCartaSecretaDoJogador(i);
            votosCartas[i] = fachada.getVotosDaCartaSecreta(i);
            totais[i] = fachada.getVotosFinais(i);
        }
        for (int v : fachada.getVencedores()) {
            vencedor[v] = true;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (herois.length == 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int largura = getWidth() - 2 * MARGEM;
        g2.setColor(Color.WHITE);
        g2.setFont(FONTE_TITULO);
        g2.drawString("Fim de jogo", MARGEM, 56);
        g2.setFont(FONTE_NEGRITO.deriveFont(18f));
        g2.setColor(DOURADO);
        g2.drawString(textoDoVencedor(), MARGEM, 88);

        int y = 110;
        desenharCabecalho(g2, y, largura);
        y += 40;
        for (int i = 0; i < herois.length; i++) {
            desenharLinha(g2, i, y, largura);
            y += ALTURA_LINHA;
        }
        desenharCartas(g2, y + 20, largura);
    }

    private String textoDoVencedor() {
        StringBuilder nomes = new StringBuilder();
        int quantos = 0;
        for (int i = 0; i < vencedor.length; i++) {
            if (vencedor[i]) {
                nomes.append(quantos++ == 0 ? "" : " e ").append(herois[i]);
            }
        }
        return quantos == 1 ? "Vencedor: " + nomes + " (próximo Khan)"
                : "Vitória compartilhada: " + nomes + " governam juntos o Império Mongol";
    }

    private void desenharCabecalho(Graphics2D g2, int y, int largura) {
        g2.setFont(FONTE_NEGRITO);
        g2.setColor(new Color(220, 205, 180));
        int x = MARGEM;
        for (int c = 0; c < COLUNAS.length; c++) {
            boolean regiao = TITULOS[c] == null;
            g2.setFont(FONTE_NEGRITO);
            g2.drawString(regiao ? NOMES_REGIAO.get(regioes[c - 2]) : TITULOS[c], x + 8, y + 16);
            if (regiao) { // legenda das colunas de regiao: cada uma mostra yurts do jogador e votos que rendem
                g2.setFont(FONTE.deriveFont(12f));
                g2.drawString("yurts → votos", x + 8, y + 32);
            }
            x += (int) (COLUNAS[c] * largura);
        }
    }

    private void desenharLinha(Graphics2D g2, int i, int y, int largura) {
        g2.setColor(vencedor[i] ? new Color(255, 200, 40, 70) : new Color(255, 255, 255, 25));
        g2.fillRoundRect(MARGEM, y, largura, ALTURA_LINHA - 6, 12, 12);
        if (vencedor[i]) {
            g2.setColor(DOURADO);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(MARGEM, y, largura, ALTURA_LINHA - 6, 12, 12);
        }
        int base = y + (ALTURA_LINHA - 6) / 2 + 6;
        int x = MARGEM;

        g2.setColor(cores[i]);
        g2.fillOval(x + 10, y + 9, 22, 22);
        g2.setColor(Color.WHITE);
        g2.drawOval(x + 10, y + 9, 22, 22);
        g2.setFont(FONTE_NEGRITO);
        g2.drawString(herois[i], x + 42, base);
        x += (int) (COLUNAS[0] * largura);

        g2.setFont(FONTE);
        g2.drawString(String.valueOf(trilha[i]), x + 8, base);
        x += (int) (COLUNAS[1] * largura);

        for (int r = 0; r < regioes.length; r++) {
            g2.drawString(yurts[i][r] + " → " + influencia[i][r], x + 8, base);
            x += (int) (COLUNAS[2 + r] * largura);
        }

        g2.drawString((cartas[i] == null ? "-" : cartas[i]) + " → " + votosCartas[i], x + 8, base);
        x += (int) (COLUNAS[5] * largura);

        g2.setFont(FONTE_NEGRITO.deriveFont(20f));
        g2.setColor(vencedor[i] ? DOURADO : Color.WHITE);
        g2.drawString(String.valueOf(totais[i]), x + 8, base + 1);
    }

    /** As cartas secretas reveladas, uma por jogador, desenhadas com drawImage. */
    private void desenharCartas(Graphics2D g2, int y, int largura) {
        int altura = getHeight() - y - 70;
        if (altura < 80) {
            return;
        }
        g2.setFont(FONTE_NEGRITO);
        g2.setColor(new Color(220, 205, 180));
        g2.drawString("Cartas de conselheiro secreto", MARGEM, y);
        int n = herois.length;
        int coluna = largura / n;
        for (int i = 0; i < n; i++) {
            int x = MARGEM + i * coluna;
            SpriteCarta.desenhar(g2, SpriteCarta.secreta(idsCartas[i]), x + 6, y + 10, coluna - 12, altura - 24);
            g2.setColor(cores[i]);
            g2.setFont(FONTE_NEGRITO);
            FontMetrics m = g2.getFontMetrics();
            g2.drawString(herois[i], x + (coluna - m.stringWidth(herois[i])) / 2, y + altura);
        }
    }

    private static String formatar(String nome) {
        return nome.charAt(0) + nome.substring(1).toLowerCase();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(JanelaTabuleiro.LARGURA, JanelaTabuleiro.ALTURA);
    }

    private class OuvinteSair implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ControladorJogo.getInstancia().sair();
        }
    }
}