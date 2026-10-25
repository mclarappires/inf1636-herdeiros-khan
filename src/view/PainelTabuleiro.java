package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controller.ControladorJogo;
import model.FachadaModelo;

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
    private PosicoesParadas posicoesProvincias;
    private PosicoesParadas posicoesCidades;
    private PosicoesParadas posicoesKhan;

    /** Onde o Khan esta (FachadaModelo.KHAN_FORA se ainda nao entrou) e para onde pode ir agora. */
    private int localDoKhan = FachadaModelo.KHAN_FORA;
    private final List<Integer> destinosDoKhan = new ArrayList<Integer>();

    private final List<PeaoNaTela> peoes = new ArrayList<PeaoNaTela>();
    private final Map<Color, BufferedImage> sprites = new HashMap<Color, BufferedImage>();

    /** Uma peca de tributo ocupa esta fracao da largura da imagem do tabuleiro. */
    private static final double LARGURA_TRIBUTO = 0.034;

    /** A peca de uma cidade revelada ocupa esta fracao da largura da imagem do tabuleiro. */
    private static final double LARGURA_CIDADE = 0.095;

    /** Diametro de uma peca de tesouro, em fracao do lado da peca da cidade. */
    private static final double LARGURA_TESOURO = 0.23;

    private final List<CidadeNaTela> cidadesVisiveis = new ArrayList<CidadeNaTela>();
    private final Map<Integer, BufferedImage> spritesCidade = new HashMap<Integer, BufferedImage>();

    private final List<PilhaDeTributos> tributos = new ArrayList<PilhaDeTributos>();
    private final Map<String, BufferedImage> spritesTributo = new HashMap<String, BufferedImage>();

    // area onde a imagem foi desenhada (muda quando a janela e redimensionada)
    private int imgX, imgY, imgLargura, imgAltura;

    PainelTabuleiro(String caminhoImagem, String caminhoPosicoes, String caminhoProvincias,
            String caminhoCidades, String caminhoKhan) {
        carregarImagem(caminhoImagem);
        try {
            posicoes = new PosicoesParadas(caminhoPosicoes);
        } catch (IOException e) {
            posicoes = null;
            System.err.println("Nao foi possivel ler as posicoes: " + caminhoPosicoes);
        }
        try {
            posicoesProvincias = new PosicoesParadas(caminhoProvincias);
        } catch (IOException e) {
            posicoesProvincias = null;
            System.err.println("Nao foi possivel ler as posicoes das provincias: " + caminhoProvincias);
        }
        try {
            posicoesCidades = new PosicoesParadas(caminhoCidades);
        } catch (IOException e) {
            posicoesCidades = null;
            System.err.println("Nao foi possivel ler as posicoes das cidades: " + caminhoCidades);
        }
        try {
            posicoesKhan = new PosicoesParadas(caminhoKhan);
        } catch (IOException e) {
            posicoesKhan = null;
            System.err.println("Nao foi possivel ler as posicoes do Khan: " + caminhoKhan);
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

    /** Troca as cidades reveladas exibidas, cada uma com os tesouros que tem sobre ela. */
    void setCidadesVisiveis(List<CidadeNaTela> cidades) {
        cidadesVisiveis.clear();
        cidadesVisiveis.addAll(cidades);
        repaint();
    }

    /** Troca as pecas de tributo exibidas (uma pilha por provincia). */
    void setTributos(List<PilhaDeTributos> novas) {
        tributos.clear();
        tributos.addAll(novas);
        repaint();
    }

    /** Troca o lugar do Khan e os lugares para onde o jogador da vez pode move-lo agora. */
    void setKhan(int local, List<Integer> destinos) {
        localDoKhan = local;
        destinosDoKhan.clear();
        destinosDoKhan.addAll(destinos);
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

        desenharCidades(g2);
        desenharTributos(g2);
        desenharPeoes(g2);
        desenharKhan(g2);

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

    /** Desenha a peca de cada cidade revelada e, em fileira na parte de baixo dela, os tesouros que restam. */
    private void desenharCidades(Graphics2D g2) {
        if (posicoesCidades == null) {
            return;
        }
        int lado = (int) (LARGURA_CIDADE * imgLargura);
        int diametro = (int) (lado * LARGURA_TESOURO);
        for (CidadeNaTela cidade : cidadesVisiveis) {
            double[] p = posicoesCidades.get(cidade.getId());
            if (p == null) {
                continue;
            }
            if (!spritesCidade.containsKey(cidade.getId())) {
                spritesCidade.put(cidade.getId(), SpriteCidade.criar(cidade.getId()));
            }
            BufferedImage sprite = spritesCidade.get(cidade.getId());
            int cx = imgX + (int) (p[0] * imgLargura);
            int cy = imgY + (int) (p[1] * imgAltura);
            if (sprite != null) {
                g2.drawImage(sprite, cx - lado / 2, cy - lado / 2, lado, lado, null);
            }

            List<String> tesouros = cidade.getTesouros();
            int passo = diametro + 2;
            int x0 = cx - (tesouros.size() * passo - 2) / 2;
            int y0 = cy + (int) (lado * 0.5) - diametro - 3;
            for (int i = 0; i < tesouros.size(); i++) {
                BufferedImage tesouro = SpriteTesouro.de(tesouros.get(i));
                if (tesouro != null) {
                    g2.drawImage(tesouro, x0 + i * passo, y0, diametro, diametro, null);
                }
            }
        }
    }

    /** Desenha as pecas de cada provincia sobre o icone impresso; as extras empilham para cima. */
    private void desenharTributos(Graphics2D g2) {
        if (posicoesProvincias == null) {
            return;
        }
        int tamanho = (int) (LARGURA_TRIBUTO * imgLargura);
        for (PilhaDeTributos pilha : tributos) {
            double[] p = posicoesProvincias.get(pilha.getProvincia());
            if (p == null) {
                continue;
            }
            int cx = imgX + (int) (p[0] * imgLargura);
            int cy = imgY + (int) (p[1] * imgAltura);
            BufferedImage sprite = spriteTributoDe(pilha.getTipo());
            if (sprite == null) {
                continue;
            }
            if (pilha.isDisponivel()) {
                desenharDestaqueDaPilha(g2, cx, cy, tamanho, pilha.getQuantidade());
            }
            for (int i = 0; i < pilha.getQuantidade(); i++) {
                int y = cy - tamanho / 2 - (int) (i * tamanho * 0.30);
                g2.drawImage(sprite, cx - tamanho / 2, y, tamanho, tamanho, null);
            }
        }
    }

    /** Anel dourado em volta da pilha da qual o jogador da vez pode pegar uma peca agora. */
    private static void desenharDestaqueDaPilha(Graphics2D g2, int cx, int cy, int tamanho, int quantidade) {
        int extra = (int) ((quantidade - 1) * tamanho * 0.30); // a pilha cresce para cima
        int diametro = tamanho + tamanho / 2;
        int x = cx - diametro / 2;
        int y = cy - diametro / 2 - extra;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(255, 215, 0, 110));
        g2.fillRoundRect(x, y, diametro, diametro + extra, diametro, diametro);
        g2.setColor(new Color(255, 200, 0));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(x, y, diametro, diametro + extra, diametro, diametro);
    }

    /** Imagem da peca do tipo; lida uma vez so (se faltar o arquivo, fica null e a peca nao aparece). */
    private BufferedImage spriteTributoDe(String tipo) {
        if (!spritesTributo.containsKey(tipo)) {
            spritesTributo.put(tipo, SpriteTributo.criar(tipo));
        }
        return spritesTributo.get(tipo);
    }

    /** Anel dourado nos lugares para onde o Khan pode ir agora e o peao do Khan onde ele esta. */
    private void desenharKhan(Graphics2D g2) {
        if (posicoesKhan == null) {
            return;
        }
        for (int destino : destinosDoKhan) {
            double[] p = posicoesKhan.get(destino);
            if (p == null) {
                continue;
            }
            int cx = imgX + (int) (p[0] * imgLargura);
            int cy = imgY + (int) (p[1] * imgAltura);
            int diametro = 2 * (int) (p[2] * imgLargura);
            desenharDestaqueDaPilha(g2, cx, cy, diametro * 2 / 3, 1);
        }
        double[] p = posicoesKhan.get(localDoKhan);
        if (p == null) { // KHAN_FORA: ainda nao entrou no tabuleiro
            return;
        }
        int largura = (int) (LARGURA_PEAO * imgLargura);
        int altura = largura * SpritePeao.ALTURA / SpritePeao.LARGURA;
        int cx = imgX + (int) (p[0] * imgLargura);
        int cy = imgY + (int) (p[1] * imgAltura);
        g2.drawImage(spriteDe(Color.WHITE), cx - largura / 2, cy - altura + altura / 6, largura, altura, null);
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
                if (grupo.get(i).getNumero() > 0) {
                    desenharNumeroDoPeao(g2, grupo.get(i).getNumero(), x + largura / 2,
                            y + (int) (altura * 0.55), (int) (largura * 0.6));
                }
            }
        }
    }

    /** Circulo branco com o numero do peao (Altani tem dois), no corpo do peao. */
    private static void desenharNumeroDoPeao(Graphics2D g2, int numero, int cx, int cy, int diametro) {
        diametro = Math.max(diametro, 12);
        int x = cx - diametro / 2;
        int y = cy - diametro / 2;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillOval(x, y, diametro, diametro);
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(x, y, diametro, diametro);

        String texto = String.valueOf(numero);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(diametro * 3 / 4, 9)));
        FontMetrics medidas = g2.getFontMetrics();
        g2.setColor(Color.BLACK);
        g2.drawString(texto, cx - medidas.stringWidth(texto) / 2,
                cy + (medidas.getAscent() - medidas.getDescent()) / 2);
    }

    private BufferedImage spriteDe(Color cor) {
        BufferedImage sprite = sprites.get(cor);
        if (sprite == null) {
            sprite = SpritePeao.criar(cor);
            sprites.put(cor, sprite);
        }
        return sprite;
    }

    /** Clique numa parada: quem decide se o movimento vale e o Model, via Controller. */
    private void aoClicarParada(int idParada) {
        ControladorJogo.getInstancia().cliqueNaParada(idParada);
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
            if (mostrarMarcadores) { // modo de calibracao (F2): mostra onde clicou
                System.out.printf("clique: x=%.4f y=%.4f%n", fx, fy);
            }
            // os lugares do Khan so recebem clique enquanto ele precisa ser usado
            Integer khan = posicoesKhan == null || destinosDoKhan.isEmpty() ? null
                    : posicoesKhan.paradaNoClique(fx, fy, imgLargura, imgAltura);
            if (khan != null) {
                aoClicarKhan(khan);
                return;
            }
            Integer parada = posicoes == null ? null
                    : posicoes.paradaNoClique(fx, fy, imgLargura, imgAltura);
            Integer provincia = posicoesProvincias == null ? null
                    : posicoesProvincias.paradaNoClique(fx, fy, imgLargura, imgAltura);
            // algumas pilhas encostam numa parada: fica com o centro mais proximo do clique
            if (provincia != null && (parada == null
                    || distancia(posicoesProvincias, provincia, fx, fy) <= distancia(posicoes, parada, fx, fy))) {
                aoClicarProvincia(provincia);
            } else if (parada != null) {
                aoClicarParada(parada);
            }
        }
    }

    /** Distancia em pixels do clique ate o centro do ponto calibrado. */
    private double distancia(PosicoesParadas tabela, int id, double fx, double fy) {
        double[] p = tabela.get(id);
        double dx = (p[0] - fx) * imgLargura;
        double dy = (p[1] - fy) * imgAltura;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Clique na pilha de tributo de uma provincia: se o jogador da vez pode pegar uma peca,
     * pede confirmacao antes; se nao pode, mostra o motivo que o Model deu.
     */
    private void aoClicarProvincia(int idProvincia) {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        if (!fachada.temPartida()) {
            return;
        }
        if (fachada.podePegarTributo(idProvincia)) {
            String tipo = fachada.getTipoDaProvincia(idProvincia).toLowerCase();
            int resposta = JOptionPane.showConfirmDialog(this,
                    "Pegar 1 peça de " + tipo + " desta província?\nUsa 1 punho (você tem "
                            + fachada.getPunhosRestantes() + ").",
                    "Pegar tributo", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (resposta != JOptionPane.YES_OPTION) {
                return;
            }
        }
        String motivo = ControladorJogo.getInstancia().cliqueNaProvincia(idProvincia);
        if (motivo != null) {
            JOptionPane.showMessageDialog(this, motivo, "Pegar tributo", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Clique num lugar do Khan: se ele pode ir para la, pede confirmacao antes; se nao pode,
     * mostra o motivo que o Model deu.
     */
    private void aoClicarKhan(int local) {
        if (destinosDoKhan.contains(local)) {
            String onde = local == FachadaModelo.KHAN_AREA_MELHORIAS ? "a área de melhorias"
                    : "esta província";
            int resposta = JOptionPane.showConfirmDialog(this, "Mover o Khan para " + onde + "?",
                    "Usar o Khan", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (resposta != JOptionPane.YES_OPTION) {
                return;
            }
        }
        String motivo = ControladorJogo.getInstancia().cliqueNoKhan(local);
        if (motivo != null) {
            JOptionPane.showMessageDialog(this, motivo, "Usar o Khan", JOptionPane.WARNING_MESSAGE);
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