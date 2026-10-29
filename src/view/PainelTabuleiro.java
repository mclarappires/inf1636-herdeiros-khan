package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.SwingUtilities;
import javax.swing.Timer;

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
    private PosicoesParadas posicoesMelhorias;

    /** Ids em melhorias_coordenadas.txt: 0 a 4 sao os espacos da area; 5 e a pilha. */
    private static final int ID_PILHA_DE_MELHORIAS = 5;
    /** Largura da peca de melhoria (fracao da imagem) e sua proporcao altura/largura. */
    private static final double LARGURA_MELHORIA = 0.0787;
    private static final double PROPORCAO_MELHORIA = 0.5266;
    private static final Color DOURADO = new Color(255, 200, 0);
    private static final Color VERDE_GRATIS = new Color(70, 200, 90);

    private AreaNaTela areaDeMelhorias = null;
    private final Map<String, BufferedImage> spritesMelhoria = new HashMap<String, BufferedImage>();

    /** Onde o Khan esta (FachadaModelo.KHAN_FORA se ainda nao entrou) e para onde pode ir agora. */
    private int localDoKhan = FachadaModelo.KHAN_FORA;
    private final List<Integer> destinosDoKhan = new ArrayList<Integer>();

    private final List<PeaoNaTela> peoes = new ArrayList<PeaoNaTela>();

    /** Diametro do circulo de um yurt, em fracao da largura da imagem do tabuleiro. */
    private static final double DIAMETRO_YURT = 0.034;
    private final List<YurtNaTela> yurts = new ArrayList<YurtNaTela>();

    /** Aviso temporario no topo do mapa (motivo de uma construcao recusada); some sozinho. */
    private String mensagem = null;
    private final Timer relogioDaMensagem = new Timer(4000, new OuvinteMensagem());
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
            String caminhoCidades, String caminhoKhan, String caminhoMelhorias) {
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
        try {
            posicoesMelhorias = new PosicoesParadas(caminhoMelhorias);
        } catch (IOException e) {
            posicoesMelhorias = null;
            System.err.println("Nao foi possivel ler as posicoes das melhorias: " + caminhoMelhorias);
        }
        relogioDaMensagem.setRepeats(false);
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

    /** Troca a area de melhorias exibida (null sem partida). */
    void setAreaDeMelhorias(AreaNaTela area) {
        areaDeMelhorias = area;
        repaint();
    }

    /** Troca os yurts exibidos (cada um na parada onde foi construido, na cor do dono). */
    void setYurts(List<YurtNaTela> novos) {
        yurts.clear();
        yurts.addAll(novos);
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
        desenharAreaDeMelhorias(g2);
        desenharYurts(g2);
        desenharPeoes(g2);
        desenharKhan(g2);

        desenharDicas(g2);
        desenharMensagem(g2);

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

    /**
     * Dicas fixas no canto de baixo do mapa (sobre a trilha do Kurultai, que ainda nao e usada):
     * como construir yurts e como comprar melhorias.
     */
    private void desenharDicas(Graphics2D g2) {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        List<String> linhas = new ArrayList<String>();
        linhas.add("Botão direito numa parada: construir yurt"
                + (fachada.temPartida() ? " (você tem " + fachada.getYurtsParaConstruir() + ")" : ""));
        linhas.add(areaDeMelhorias != null && areaDeMelhorias.isGratuita()
                ? "Melhoria grátis: clique numa peça da área ou na pilha"
                : "Clique numa peça da área: comprar (" + FachadaModelo.CUSTO_MELHORIA + " moedas)");

        int larguraMaxima = (int) (0.385 * imgLargura);
        int tamanhoFonte = Math.max(imgLargura / 62, 9);
        FontMetrics medidas;
        do {
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, tamanhoFonte));
            medidas = g2.getFontMetrics();
            tamanhoFonte--;
        } while (tamanhoFonte >= 8 && (medidas.stringWidth(linhas.get(0)) > larguraMaxima - 12
                || medidas.stringWidth(linhas.get(1)) > larguraMaxima - 12));

        int altura = linhas.size() * medidas.getHeight() + 8;
        int x = imgX + (int) (0.60 * imgLargura);
        int y = imgY + (int) (0.992 * imgAltura) - altura;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 175));
        g2.fillRoundRect(x, y, larguraMaxima, altura, 12, 12);
        g2.setColor(Color.WHITE);
        for (int i = 0; i < linhas.size(); i++) {
            g2.drawString(linhas.get(i), x + 8, y + 4 + medidas.getAscent() + i * medidas.getHeight());
        }
    }

    /** Desenha as pecas da area de melhorias, o destaque das que podem ser pegas e a pilha. */
    private void desenharAreaDeMelhorias(Graphics2D g2) {
        if (posicoesMelhorias == null || areaDeMelhorias == null) {
            return;
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int l = (int) (LARGURA_MELHORIA * imgLargura);
        int a = (int) (l * PROPORCAO_MELHORIA);
        Color destaque = areaDeMelhorias.isGratuita() ? VERDE_GRATIS : DOURADO;

        for (int i = 0; i < areaDeMelhorias.getEspacos(); i++) {
            String simbolo = areaDeMelhorias.getSimbolo(i);
            double[] p = posicoesMelhorias.get(i);
            if (simbolo == null || p == null) {
                continue;
            }
            int x = imgX + (int) (p[0] * imgLargura) - l / 2;
            int y = imgY + (int) (p[1] * imgAltura) - a / 2;
            desenharPecaDeMelhoria(g2, simbolo, areaDeMelhorias.getCor(i), x, y, l, a);
            if (areaDeMelhorias.isDisponivel(i)) {
                desenharContorno(g2, x, y, l, a, destaque);
            }
        }

        double[] p = posicoesMelhorias.get(ID_PILHA_DE_MELHORIAS);
        if (p != null && areaDeMelhorias.getTamanhoDaPilha() > 0) {
            int x = imgX + (int) (p[0] * imgLargura) - l / 2;
            int y = imgY + (int) (p[1] * imgAltura) - a / 2;
            int arco = a / 5;
            for (int k = 2; k >= 0; k--) { // pecas viradas para baixo, empilhadas
                g2.setColor(new Color(75, 55, 120));
                g2.fillRoundRect(x + k * 2, y - k * 2, l, a, arco, arco);
                g2.setColor(new Color(235, 220, 170));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x + k * 2, y - k * 2, l, a, arco, arco);
            }
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(a / 3, 10)));
            FontMetrics m = g2.getFontMetrics();
            String n = String.valueOf(areaDeMelhorias.getTamanhoDaPilha());
            g2.setColor(Color.WHITE);
            g2.drawString(n, x + (l - m.stringWidth(n)) / 2, y + (a + m.getAscent() - m.getDescent()) / 2);
            if (areaDeMelhorias.isPilhaDisponivel()) {
                desenharContorno(g2, x, y, l, a, destaque);
            }
        }
    }

    /** Imagem da peca; se o arquivo faltar, um retangulo na cor da coluna com o nome do simbolo. */
    private void desenharPecaDeMelhoria(Graphics2D g2, String simbolo, String cor, int x, int y, int l, int a) {
        String chave = simbolo + "|" + cor;
        if (!spritesMelhoria.containsKey(chave)) {
            spritesMelhoria.put(chave, SpriteMelhoria.criar(simbolo, cor));
        }
        BufferedImage sprite = spritesMelhoria.get(chave);
        int arco = a / 5;
        if (sprite != null) {
            g2.drawImage(sprite, x, y, l, a, null);
            g2.setColor(new Color(70, 45, 20));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x, y, l, a, arco, arco);
            return;
        }
        g2.setColor("PRIMEIRA".equals(cor) ? new Color(226, 107, 90)
                : "SEGUNDA".equals(cor) ? new Color(122, 178, 92) : new Color(234, 200, 72));
        g2.fillRoundRect(x, y, l, a, arco, arco);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(a / 4, 9)));
        FontMetrics m = g2.getFontMetrics();
        String texto = simbolo.toLowerCase();
        g2.drawString(texto, x + (l - m.stringWidth(texto)) / 2, y + (a + m.getAscent() - m.getDescent()) / 2);
    }

    private static void desenharContorno(Graphics2D g2, int x, int y, int l, int a, Color cor) {
        g2.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 90));
        g2.fillRoundRect(x - 3, y - 3, l + 6, a + 6, 14, 14);
        g2.setColor(cor);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRoundRect(x - 3, y - 3, l + 6, a + 6, 14, 14);
    }

    /** Espaco da area (0 a 4) ou pilha (5) sob o clique; null se nao ha peca ali. */
    private Integer melhoriaNoClique(double fx, double fy) {
        if (posicoesMelhorias == null || areaDeMelhorias == null) {
            return null;
        }
        double meiaLargura = LARGURA_MELHORIA / 2;
        double meiaAltura = meiaLargura * PROPORCAO_MELHORIA * imgLargura / imgAltura;
        for (Map.Entry<Integer, double[]> e : posicoesMelhorias.todas().entrySet()) {
            int id = e.getKey();
            boolean temPeca = id == ID_PILHA_DE_MELHORIAS ? areaDeMelhorias.getTamanhoDaPilha() > 0
                    : id < areaDeMelhorias.getEspacos() && areaDeMelhorias.getSimbolo(id) != null;
            double[] p = e.getValue();
            if (temPeca && Math.abs(fx - p[0]) <= meiaLargura && Math.abs(fy - p[1]) <= meiaAltura) {
                return id;
            }
        }
        return null;
    }

    /** Clique numa peca da area ou na pilha: compra (ou pega de graca) e avisa se nao puder. */
    private void aoClicarMelhoria(int id) {
        ControladorJogo controlador = ControladorJogo.getInstancia();
        String motivo = id == ID_PILHA_DE_MELHORIAS
                ? controlador.cliqueNaPilhaDeMelhorias() : controlador.cliqueNaMelhoria(id);
        if (motivo != null) {
            mostrarMensagem(motivo);
        } else {
            mensagem = null;
            repaint();
        }
    }

    /** Aviso temporario (por exemplo, por que o yurt nao pode ser construido). */
    private void desenharMensagem(Graphics2D g2) {
        if (mensagem != null) {
            desenharFaixa(g2, mensagem, 36, new Color(150, 20, 20, 225), Color.WHITE);
        }
    }

    /** Texto centralizado sobre um retangulo arredondado; yBase e a linha de base do texto. */
    private void desenharFaixa(Graphics2D g2, String texto, int yBase, Color fundo, Color letra) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        FontMetrics medidas = g2.getFontMetrics();
        int largura = medidas.stringWidth(texto) + 24;
        int altura = medidas.getHeight() + 8;
        int x = (getWidth() - largura) / 2;
        int y = yBase - medidas.getAscent() - 4;
        g2.setColor(fundo);
        g2.fillRoundRect(x, y, largura, altura, 14, 14);
        g2.setColor(letra);
        g2.drawString(texto, x + 12, yBase);
    }

    /** Mostra um aviso por alguns segundos. */
    private void mostrarMensagem(String texto) {
        mensagem = texto;
        relogioDaMensagem.restart();
        repaint();
    }

    /** Apaga o aviso quando o tempo acaba (classe ouvinte, sem lambda). */
    private class OuvinteMensagem implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            mensagem = null;
            repaint();
        }
    }

    /** Botao direito numa parada: tenta construir um yurt do jogador da vez e avisa se nao puder. */
    private void aoClicarDireito(double fx, double fy) {
        if (posicoes == null || !FachadaModelo.getInstancia().temPartida()) {
            return;
        }
        Integer parada = posicoes.paradaNoClique(fx, fy, imgLargura, imgAltura);
        if (parada == null) {
            return;
        }
        String motivo = ControladorJogo.getInstancia().cliqueDireitoNaParada(parada);
        if (motivo != null) {
            mostrarMensagem("Não foi possível construir: " + motivo);
        } else {
            mensagem = null;
            repaint();
        }
    }

    /** Desenha cada yurt como um circulo na cor do dono sobre a parada; dois na mesma parada ficam lado a lado. */
    private void desenharYurts(Graphics2D g2) {
        if (posicoes == null) {
            return;
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Map<Integer, Integer> jaDesenhados = new HashMap<Integer, Integer>();
        int diametro = (int) (DIAMETRO_YURT * imgLargura);
        for (YurtNaTela y : yurts) {
            double[] p = posicoes.get(y.getParada());
            if (p == null) {
                continue;
            }
            Integer antes = jaDesenhados.get(y.getParada());
            int ordem = antes == null ? 0 : antes;
            jaDesenhados.put(y.getParada(), ordem + 1);
            int cx = imgX + (int) (p[0] * imgLargura) + ordem * (int) (diametro * 0.9);
            int cy = imgY + (int) (p[1] * imgAltura);
            g2.setColor(y.getCor());
            g2.fillOval(cx - diametro / 2, cy - diametro / 2, diametro, diametro);
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(Color.WHITE);
            g2.drawOval(cx - diametro / 2, cy - diametro / 2, diametro, diametro);
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
            if (SwingUtilities.isRightMouseButton(e)) {
                aoClicarDireito(fx, fy);
                return;
            }
            if (mostrarMarcadores) { // modo de calibracao (F2): mostra onde clicou
                System.out.printf("clique: x=%.4f y=%.4f%n", fx, fy);
            }
            Integer melhoria = melhoriaNoClique(fx, fy);
            if (melhoria != null) {
                aoClicarMelhoria(melhoria);
                return;
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