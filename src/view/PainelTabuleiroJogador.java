package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import controller.ControladorJogo;
import model.FachadaModelo;

/**
 * Tabuleiro do heroi do jogador da vez: mostra as 4 colunas, as melhorias e as pecas de
 * ativacao, e um clique numa coluna a ativa. Aparece no lugar do mapa (ver JanelaTabuleiro).
 */
class PainelTabuleiroJogador extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Font FONTE = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
    private static final Font FONTE_TITULO = new Font(Font.SERIF, Font.BOLD, 26);

    // posicoes em fracao da imagem do tabuleiro do heroi (todas tem ~1600 x 871)
    private static final double[] X_COLUNAS = {0.262, 0.439, 0.616, 0.811};
    private static final double LARGURA_COLUNA = 0.151;
    private static final double[] Y_LINHAS = {0.092, 0.255, 0.419, 0.583}; // de cima para baixo
    private static final double ALTURA_ESPACO = 0.149;
    private static final double Y_BASE = 0.752;
    private static final double ALTURA_BASE = 0.149;

    private static final Color DOURADO = new Color(255, 200, 40);
    private static final Map<String, Color> CORES_MELHORIA = new HashMap<String, Color>();
    private static final Map<String, String> NOMES_COLUNA = new HashMap<String, String>();

    static {
        CORES_MELHORIA.put("PRIMEIRA", new Color(226, 107, 90));
        CORES_MELHORIA.put("SEGUNDA", new Color(122, 178, 92));
        CORES_MELHORIA.put("TERCEIRA", new Color(234, 200, 72));

        NOMES_COLUNA.put("PRIMEIRA", "vermelha");
        NOMES_COLUNA.put("SEGUNDA", "verde");
        NOMES_COLUNA.put("TERCEIRA", "amarela");
        NOMES_COLUNA.put("BRANCA", "branca");
    }

    private final JanelaTabuleiro janela;
    private final String[] colunas; // na ordem da esquerda para a direita do tabuleiro

    private final JLabel titulo = new JLabel();
    private final JLabel instrucao = new JLabel();
    private final JPanel escolhaPeao = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final JRadioButton[] botoesPeao = {new JRadioButton(), new JRadioButton()};
    private final AreaTabuleiro area = new AreaTabuleiro();

    private final Map<String, Image> imagensHerois = new HashMap<String, Image>();
    private final Map<String, BufferedImage> spritesTributo = new HashMap<String, BufferedImage>();

    private int jogadorMostrado = -1;

    PainelTabuleiroJogador(JanelaTabuleiro janela) {
        this.janela = janela;
        colunas = FachadaModelo.getInstancia().getColunas();
        setLayout(new BorderLayout());
        add(criarTopo(), BorderLayout.NORTH);
        add(area, BorderLayout.CENTER);
        atualizar();
    }

    private JPanel criarTopo() {
        JPanel topo = new JPanel(new BorderLayout(12, 0));
        topo.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JPanel textos = new JPanel(new BorderLayout(0, 4));
        titulo.setFont(FONTE_TITULO);
        instrucao.setFont(FONTE);
        textos.add(titulo, BorderLayout.NORTH);
        textos.add(instrucao, BorderLayout.SOUTH);
        topo.add(textos, BorderLayout.WEST);

        JLabel rotulo = new JLabel("Peão que age:");
        rotulo.setFont(FONTE);
        escolhaPeao.add(rotulo);
        ButtonGroup grupo = new ButtonGroup();
        for (JRadioButton b : botoesPeao) {
            b.setFont(FONTE);
            grupo.add(b);
            escolhaPeao.add(b);
        }
        botoesPeao[0].setSelected(true);
        topo.add(escolhaPeao, BorderLayout.CENTER);

        JButton fechar = new JButton("Fechar");
        fechar.setFont(FONTE.deriveFont(Font.BOLD));
        fechar.addActionListener(new OuvinteFechar());
        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        direita.add(fechar);
        topo.add(direita, BorderLayout.EAST);
        return topo;
    }

    /** Relê na fachada o tabuleiro do jogador da vez e redesenha. */
    void atualizar() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        if (!fachada.temPartida()) {
            titulo.setText("Sem partida");
            instrucao.setText(" ");
            escolhaPeao.setVisible(false);
            repaint();
            return;
        }
        int vez = fachada.getJogadorDaVez();
        if (vez != jogadorMostrado) { // novo turno: escolha de peao volta para o primeiro
            jogadorMostrado = vez;
            botoesPeao[0].setSelected(true);
        }
        titulo.setText("Tabuleiro de " + formatar(fachada.getHeroiDoJogador(vez)));

        String ativa = fachada.getColunaAtiva();
        instrucao.setText(ativa == null
                ? "Clique em uma coluna para ativá-la."
                : "Coluna " + nomeDaColuna(ativa) + " já ativada neste turno.");

        int peoes = fachada.getQuantidadePeoes(vez);
        escolhaPeao.setVisible(peoes > 1);
        for (int k = 0; k < botoesPeao.length; k++) {
            if (k < peoes) {
                botoesPeao[k].setText("Peão " + (k + 1) + " (parada " + fachada.getParadaDoPeao(vez, k) + ")");
            }
            botoesPeao[k].setEnabled(ativa == null);
        }
        repaint();
    }

    /** Nome que o jogador ve: a cor da coluna ("PRIMEIRA" vira "vermelha"). */
    static String nomeDaColuna(String coluna) {
        String nome = NOMES_COLUNA.get(coluna);
        return nome == null ? coluna.toLowerCase() : nome;
    }

    private int peaoEscolhido() {
        return botoesPeao[1].isSelected() && escolhaPeao.isVisible() ? 1 : 0;
    }

    private boolean podeAtivar(int indiceColuna) {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        return fachada.temPartida() && fachada.getColunaAtiva() == null
                && !fachada.isColunaUsada(fachada.getJogadorDaVez(), colunas[indiceColuna]);
    }

    private void cliqueNaColuna(int indiceColuna) {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        if (!fachada.temPartida()) {
            return;
        }
        String coluna = colunas[indiceColuna];
        String ativa = fachada.getColunaAtiva();
        String aviso = null;
        if (ativa != null) {
            aviso = "Você já ativou a coluna " + nomeDaColuna(ativa) + " neste turno.";
        } else if (fachada.isColunaUsada(fachada.getJogadorDaVez(), coluna)) {
            aviso = "A coluna " + nomeDaColuna(coluna)
                    + " já foi usada. Ative a coluna branca para recuperar as peças.";
        } else if (!ControladorJogo.getInstancia().ativarColuna(coluna, peaoEscolhido())) {
            aviso = "Não foi possível ativar a coluna " + nomeDaColuna(coluna) + ".";
        }
        if (aviso != null) {
            JOptionPane.showMessageDialog(this, aviso, "Ativar coluna", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Image imagemDoHeroi(String heroi) {
        if (!imagensHerois.containsKey(heroi)) {
            String caminho = "resources/herois/" + heroi.toLowerCase() + ".jpg";
            Image imagem = null;
            try {
                imagem = ImageIO.read(new File(caminho));
            } catch (IOException e) {
                System.err.println("Nao foi possivel ler a imagem: " + caminho);
            }
            imagensHerois.put(heroi, imagem);
        }
        return imagensHerois.get(heroi);
    }

    private BufferedImage iconeDaMelhoria(String simbolo) {
        if (simbolo.equals("MOEDA") || simbolo.equals("ESPADA") || simbolo.equals("YURT")) {
            if (!spritesTributo.containsKey(simbolo)) {
                spritesTributo.put(simbolo, SpriteTributo.criar(simbolo));
            }
            return spritesTributo.get(simbolo);
        }
        if (simbolo.equals("PUNHO") || simbolo.equals("DOIS_CAVALOS")) {
            return null; // sem imagem: desenhada como texto
        }
        return SpriteTesouro.de(simbolo);
    }

    /** "TOLUI" vira "Tolui". */
    private static String formatar(String nome) {
        return nome.charAt(0) + nome.substring(1).toLowerCase();
    }

    /** Area desenhada com Graphics2D: imagem do heroi e, por cima, melhorias e pecas de ativacao. */
    private class AreaTabuleiro extends JPanel {

        private static final long serialVersionUID = 1L;

        // area onde a imagem foi desenhada (muda quando a janela e redimensionada)
        private int imgX, imgY, imgLargura, imgAltura;
        private int colunaSobMouse = -1;

        AreaTabuleiro() {
            setBackground(new Color(40, 30, 25));
            OuvinteMouse ouvinte = new OuvinteMouse();
            addMouseListener(ouvinte);
            addMouseMotionListener(ouvinte);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            FachadaModelo fachada = FachadaModelo.getInstancia();
            if (!fachada.temPartida()) {
                imgLargura = 0;
                return;
            }
            int vez = fachada.getJogadorDaVez();
            Image imagem = imagemDoHeroi(fachada.getHeroiDoJogador(vez));
            if (imagem == null) {
                imgLargura = 0;
                g2.setColor(Color.WHITE);
                g2.drawString("Imagem do tabuleiro do herói não encontrada", 20, 30);
                return;
            }

            // mantem a proporcao da imagem dentro do painel
            int larguraOriginal = imagem.getWidth(null);
            int alturaOriginal = imagem.getHeight(null);
            double escala = Math.min((double) getWidth() / larguraOriginal,
                    (double) getHeight() / alturaOriginal);
            imgLargura = (int) (larguraOriginal * escala);
            imgAltura = (int) (alturaOriginal * escala);
            imgX = (getWidth() - imgLargura) / 2;
            imgY = (getHeight() - imgAltura) / 2;
            g2.drawImage(imagem, imgX, imgY, imgLargura, imgAltura, null);

            String ativa = fachada.getColunaAtiva();
            for (int k = 0; k < colunas.length; k++) {
                desenharMelhorias(g2, fachada, vez, k);
                if (fachada.isColunaUsada(vez, colunas[k])) {
                    desenharUsada(g2, k);
                }
                if (colunas[k].equals(ativa)) {
                    contornarColuna(g2, k, DOURADO, 6f);
                } else if (k == colunaSobMouse && podeAtivar(k)) {
                    contornarColuna(g2, k, Color.WHITE, 3f);
                }
            }
        }

        private void desenharMelhorias(Graphics2D g2, FachadaModelo fachada, int vez, int k) {
            int linhas = FachadaModelo.LINHAS_TABULEIRO_JOGADOR;
            int bloqueados = linhas - fachada.getCapacidadeDaColuna(vez, colunas[k]);
            for (int b = 0; b < bloqueados; b++) {
                desenharBloqueado(g2, k, linhas - 1 - b);
            }
            String[] simbolos = fachada.getSimbolosDasMelhorias(vez, colunas[k]);
            String[] cores = fachada.getCoresDasMelhorias(vez, colunas[k]);
            for (int i = 0; i < simbolos.length; i++) {
                desenharMelhoria(g2, k, linhas - 1 - (i + bloqueados), simbolos[i], cores[i]);
            }
        }

        private void desenharMelhoria(Graphics2D g2, int k, int linha, String simbolo, String cor) {
            int x = px(X_COLUNAS[k] + LARGURA_COLUNA * 0.05);
            int y = py(Y_LINHAS[linha] + ALTURA_ESPACO * 0.07);
            int l = tamanho(LARGURA_COLUNA * 0.90, imgLargura);
            int a = tamanho(ALTURA_ESPACO * 0.86, imgAltura);
            int arco = a / 5;
            Color fundo = CORES_MELHORIA.get(cor);
            g2.setColor(fundo == null ? Color.LIGHT_GRAY : fundo);
            g2.fillRoundRect(x, y, l, a, arco, arco);
            g2.setColor(new Color(70, 45, 20));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(x, y, l, a, arco, arco);

            BufferedImage icone = iconeDaMelhoria(simbolo);
            if (icone != null) {
                // icone com 75% da altura da peca, mantendo a proporcao
                int ia = (int) (a * 0.75);
                int il = icone.getWidth() * ia / icone.getHeight();
                if (il > l * 0.9) {
                    il = (int) (l * 0.9);
                    ia = icone.getHeight() * il / icone.getWidth();
                }
                g2.drawImage(icone, x + (l - il) / 2, y + (a - ia) / 2, il, ia, null);
            } else {
                String texto = simbolo.equals("PUNHO") ? "+1 punho"
                        : simbolo.equals("DOIS_CAVALOS") ? "+2 cavalos" : formatar(simbolo);
                escreverCentralizado(g2, texto, x, y, l, a, Color.BLACK, a / 4);
            }
        }

        /** Espaco da coluna branca que ja vem ocupado (Jochi). */
        private void desenharBloqueado(Graphics2D g2, int k, int linha) {
            int x = px(X_COLUNAS[k]);
            int y = py(Y_LINHAS[linha]);
            int l = tamanho(LARGURA_COLUNA, imgLargura);
            int a = tamanho(ALTURA_ESPACO, imgAltura);
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(x, y, l, a);
            escreverCentralizado(g2, "ocupado", x, y, l, a, Color.WHITE, a / 5);
        }

        /** Peca de ativacao ja esta na coluna: escurece os icones da base. */
        private void desenharUsada(Graphics2D g2, int k) {
            int x = px(X_COLUNAS[k]);
            int y = py(Y_BASE);
            int l = tamanho(LARGURA_COLUNA, imgLargura);
            int a = tamanho(ALTURA_BASE, imgAltura);
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRect(x, y, l, a);
            escreverCentralizado(g2, "USADA", x, y, l, a, Color.WHITE, a / 3);
        }

        private void contornarColuna(Graphics2D g2, int k, Color cor, float espessura) {
            int x = px(X_COLUNAS[k]);
            int y = py(Y_LINHAS[0]);
            int l = tamanho(LARGURA_COLUNA, imgLargura);
            int a = py(Y_BASE + ALTURA_BASE) - y;
            g2.setColor(cor);
            g2.setStroke(new BasicStroke(espessura));
            g2.drawRoundRect(x - 4, y - 4, l + 8, a + 8, 16, 16);
        }

        private void escreverCentralizado(Graphics2D g2, String texto, int x, int y, int l, int a,
                Color cor, int tamanhoFonte) {
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(tamanhoFonte, 10)));
            FontMetrics medidas = g2.getFontMetrics();
            g2.setColor(cor);
            g2.drawString(texto, x + (l - medidas.stringWidth(texto)) / 2,
                    y + (a + medidas.getAscent() - medidas.getDescent()) / 2);
        }

        private int px(double fracao) {
            return imgX + (int) (fracao * imgLargura);
        }

        private int py(double fracao) {
            return imgY + (int) (fracao * imgAltura);
        }

        private int tamanho(double fracao, int total) {
            return (int) (fracao * total);
        }

        /** Coluna sob o ponto (das melhorias ate a base), ou -1. */
        private int colunaEm(int x, int y) {
            if (imgLargura == 0 || imgAltura == 0) {
                return -1;
            }
            double fx = (double) (x - imgX) / imgLargura;
            double fy = (double) (y - imgY) / imgAltura;
            if (fy < Y_LINHAS[0] || fy > Y_BASE + ALTURA_BASE) {
                return -1;
            }
            for (int k = 0; k < X_COLUNAS.length; k++) {
                if (fx >= X_COLUNAS[k] && fx <= X_COLUNAS[k] + LARGURA_COLUNA) {
                    return k;
                }
            }
            return -1;
        }

        /** Eventos tratados como no Cap. 12: classe ouvinte, sem lambda. */
        private class OuvinteMouse extends MouseAdapter {
            @Override
            public void mouseClicked(MouseEvent e) {
                int k = colunaEm(e.getX(), e.getY());
                if (k >= 0) {
                    cliqueNaColuna(k);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int k = colunaEm(e.getX(), e.getY());
                if (k != colunaSobMouse) {
                    colunaSobMouse = k;
                    setCursor(k >= 0 && podeAtivar(k)
                            ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                            : Cursor.getDefaultCursor());
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                colunaSobMouse = -1;
                setCursor(Cursor.getDefaultCursor());
                repaint();
            }
        }
    }

    private class OuvinteFechar implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            janela.mostrarMapa();
        }
    }
}
