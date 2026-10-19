package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * Painel que desenha o tabuleiro com Graphics2D.drawImage (nada de JLabel).
 * Passo 1: so desenha a imagem e imprime no console a posicao clicada
 * em fracao (0 a 1) da imagem, para calibrar as paradas depois.
 */
class PainelTabuleiro extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Ponto vermelho e area de clique de cada parada; so para calibrar (tecla F2 liga/desliga). */
    private boolean mostrarMarcadores = false;

    /** Peao ocupa esta fracao da largura da imagem do tabuleiro. */
    private static final double LARGURA_PEAO = 0.030;

    private Image imagemTabuleiro;
    private PosicoesParadas posicoes;

    private final List<PeaoNaTela> peoes = new ArrayList<PeaoNaTela>();
    private final Map<Color, BufferedImage> sprites = new HashMap<Color, BufferedImage>();

    // area onde a imagem foi desenhada (muda quando a janela e redimensionada)
    private int imgX, imgY, imgLargura, imgAltura;

    PainelTabuleiro(String caminhoImagem, String caminhoPosicoes) {
        carregarImagem(caminhoImagem);
        try {
            posicoes = new PosicoesParadas(caminhoPosicoes);
        } catch (IOException e) {
            posicoes = null;
            System.err.println("Nao foi possivel ler as posicoes: " + caminhoPosicoes);
        }
        addMouseListener(new OuvinteMouse());
        setFocusable(true);
        addKeyListener(new OuvinteTeclado());
    }

    /** Troca os peoes exibidos (o Controller chama isto com o estado do Model). */
    void setPeoes(List<PeaoNaTela> novos) {
        peoes.clear();
        peoes.addAll(novos);
        repaint();
    }

    private void carregarImagem(String caminho) {
        File arquivo = new File(caminho);
        try {
            imagemTabuleiro = ImageIO.read(arquivo);
            if (imagemTabuleiro == null) {
                System.err.println("Arquivo encontrado, mas nao e uma imagem valida: "
                        + arquivo.getAbsolutePath());
            }
        } catch (IOException e) {
            imagemTabuleiro = null;
            System.err.println("Nao foi possivel ler a imagem: " + arquivo.getAbsolutePath()
                    + " (existe? " + arquivo.exists() + ")");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (imagemTabuleiro == null) {
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Imagem do tabuleiro nao encontrada", 20, 30);
            return;
        }

        // mantem a proporcao da imagem dentro do painel
        int larguraOriginal = imagemTabuleiro.getWidth(null);
        int alturaOriginal = imagemTabuleiro.getHeight(null);
        double escala = Math.min((double) getWidth() / larguraOriginal,
                (double) getHeight() / alturaOriginal);
        imgLargura = (int) (larguraOriginal * escala);
        imgAltura = (int) (alturaOriginal * escala);
        imgX = (getWidth() - imgLargura) / 2;
        imgY = (getHeight() - imgAltura) / 2;

        g2.drawImage(imagemTabuleiro, imgX, imgY, imgLargura, imgAltura, null);

        desenharPeoes(g2);

        if (mostrarMarcadores && posicoes != null) {
            g2.setColor(Color.RED);
            for (Map.Entry<Integer, double[]> e : posicoes.todas().entrySet()) {
                int px = imgX + (int) (e.getValue()[0] * imgLargura);
                int py = imgY + (int) (e.getValue()[1] * imgAltura);
                g2.fillOval(px - 4, py - 4, 8, 8);
                int r = (int) (e.getValue()[2] * imgLargura);
                g2.drawOval(px - r, py - r, 2 * r, 2 * r);
            }
        }
    }

    /** Desenha cada peao na sua parada; peoes na mesma parada ficam lado a lado. */
    private void desenharPeoes(Graphics2D g2) {
        if (posicoes == null) {
            return;
        }
        Map<Integer, List<PeaoNaTela>> porParada = new HashMap<Integer, List<PeaoNaTela>>();
        for (PeaoNaTela peao : peoes) {
            List<PeaoNaTela> grupo = porParada.get(peao.getParada());
            if (grupo == null) {
                grupo = new ArrayList<PeaoNaTela>();
                porParada.put(peao.getParada(), grupo);
            }
            grupo.add(peao);
        }

        int largura = (int) (LARGURA_PEAO * imgLargura);
        int altura = largura * SpritePeao.ALTURA / SpritePeao.LARGURA;
        for (Map.Entry<Integer, List<PeaoNaTela>> e : porParada.entrySet()) {
            double[] p = posicoes.get(e.getKey());
            if (p == null) {
                continue;
            }
            int cx = imgX + (int) (p[0] * imgLargura);
            int cy = imgY + (int) (p[1] * imgAltura);
            List<PeaoNaTela> grupo = e.getValue();
            // espalha em fileira horizontal, centrada na parada
            double passo = largura * 0.75;
            for (int i = 0; i < grupo.size(); i++) {
                int x = (int) (cx + (i - (grupo.size() - 1) / 2.0) * passo) - largura / 2;
                int y = cy - altura + altura / 6; // pe do peao um pouco abaixo do centro
                g2.drawImage(spriteDe(grupo.get(i).getCor()), x, y, largura, altura, null);
            }
        }
    }

    private BufferedImage spriteDe(Color cor) {
        BufferedImage sprite = sprites.get(cor);
        if (sprite == null) {
            sprite = SpritePeao.criar(cor);
            sprites.put(cor, sprite);
        }
        return sprite;
    }

    /**
     * Chamado quando o clique cai numa parada. Por enquanto so imprime;
     * depois chama o Controller (que pergunta ao Model se o movimento vale).
     */
    private void aoClicarParada(int idParada) {
        System.out.println("parada clicada: " + idParada);
    }

    /** Eventos tratados como no Cap. 12: classe ouvinte, sem lambda. */
    private class OuvinteMouse extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            requestFocusInWindow();
            if (imgLargura == 0 || imgAltura == 0) {
                return;
            }
            double fx = (double) (e.getX() - imgX) / imgLargura;
            double fy = (double) (e.getY() - imgY) / imgAltura;
            if (fx < 0 || fx > 1 || fy < 0 || fy > 1) {
                return; // clique fora da imagem
            }
            System.out.printf("clique: x=%.4f y=%.4f%n", fx, fy);
            if (posicoes == null) {
                return;
            }
            Integer id = posicoes.paradaNoClique(fx, fy, imgLargura, imgAltura);
            if (id != null) {
                aoClicarParada(id);
            }
        }
    }

    /** F2 mostra ou esconde os marcadores de calibracao. */
    private class OuvinteTeclado extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_F2) {
                mostrarMarcadores = !mostrarMarcadores;
                repaint();
            }
        }
    }
}