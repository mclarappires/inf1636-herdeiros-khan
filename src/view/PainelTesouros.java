package view;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controller.ControladorJogo;
import model.FachadaModelo;

/**
 * Inventario do jogador da vez, ao lado do mapa: quantos tesouros de cada tipo ele pode usar
 * (pecas mais os tesouros virtuais do turno) e as pecas de bonus que ja ganhou. As pecas que
 * podem ser usadas agora ficam com contorno dourado e um clique as usa; as usadas ficam
 * acinzentadas. Tudo desenhado com drawImage.
 */
class PainelTesouros extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Font FONTE_TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    private static final Font FONTE_NUMERO = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    private static final int LADO_TESOURO = 28;
    private static final int LADO_BONUS = 30;
    private static final int BONUS_POR_LINHA = 5;
    private static final int ESPACO = 4;
    private static final Color DOURADO = new Color(255, 200, 0);

    /** Efeito de cada peca, como no manual; aparece na confirmacao do uso. */
    private static final Map<String, String> EFEITOS = new HashMap<String, String>();

    static {
        EFEITOS.put("VOTO", "Trono: 1 voto extra na trilha do Kurultai.");
        EFEITOS.put("ESPADAS", "Espadas: receba 2 peças de tributo de espada.");
        EFEITOS.put("MOEDAS", "Moedas: receba 2 peças de tributo de moeda.");
        EFEITOS.put("YURTS", "Yurts: receba 2 peças de tributo de yurt.");
        EFEITOS.put("MOVIMENTOS", "Cavalos: 3 movimentos extras e 1 ação extra de pegar tributo.");
        EFEITOS.put("ACOES", "Mãos: 2 ações extras de pegar tributo.");
        EFEITOS.put("TESOUROS", "Baú: sorteie 3 tesouros da pilha, fique com 1 e descarte os outros.");
    }

    private final JanelaTabuleiro janela;

    private String[] tipos = new String[0];
    private int[] quantidades = new int[0];
    private String[] bonus = new String[0];
    private boolean[] usados = new boolean[0];
    private boolean[] disponiveis = new boolean[0];

    /** Onde cada peca de bonus foi desenhada {x, y}, para achar a peca clicada. */
    private int[][] posicoesBonus = new int[0][];

    PainelTesouros(JanelaTabuleiro janela) {
        this.janela = janela;
        setPreferredSize(new Dimension(170, 150));
        setOpaque(false);
        OuvinteMouse ouvinte = new OuvinteMouse();
        addMouseListener(ouvinte);
        addMouseMotionListener(ouvinte);
    }

    /** Relê na fachada o inventario do jogador da vez. */
    void atualizar() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        if (!fachada.temPartida() || fachada.isEscolhaDeCartasPendente()) {
            tipos = new String[0];
            bonus = new String[0];
            posicoesBonus = new int[0][];
            repaint();
            return;
        }
        int vez = fachada.getJogadorDaVez();
        tipos = fachada.getTiposDeTesouro();
        quantidades = new int[tipos.length];
        for (int i = 0; i < tipos.length; i++) {
            quantidades[i] = fachada.getTesouroDisponivel(vez, tipos[i]);
        }
        bonus = fachada.getBonusDoJogador(vez);
        usados = new boolean[bonus.length];
        disponiveis = new boolean[bonus.length];
        for (int i = 0; i < bonus.length; i++) {
            usados[i] = fachada.isBonusUsado(vez, bonus[i]);
            disponiveis[i] = fachada.podeUsarBonus(bonus[i]);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (tipos.length == 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int y = escreverTitulo(g2, "Tesouros", 0);
        int passo = getWidth() / tipos.length;
        g2.setFont(FONTE_NUMERO);
        FontMetrics medidas = g2.getFontMetrics();
        for (int i = 0; i < tipos.length; i++) {
            int cx = i * passo + passo / 2;
            BufferedImage imagem = SpriteTesouro.de(tipos[i]);
            if (quantidades[i] == 0) { // tipo que o jogador nao tem fica apagado
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            }
            if (imagem != null) {
                g2.drawImage(imagem, cx - LADO_TESOURO / 2, y, LADO_TESOURO, LADO_TESOURO, null);
            }
            g2.setComposite(AlphaComposite.SrcOver);
            String n = String.valueOf(quantidades[i]);
            g2.setColor(Color.BLACK);
            g2.drawString(n, cx - medidas.stringWidth(n) / 2, y + LADO_TESOURO + medidas.getAscent());
        }
        y += LADO_TESOURO + medidas.getHeight() + 8;

        y = escreverTitulo(g2, bonus.length == 0 ? "Bônus: nenhum" : "Bônus (clique para usar)", y);
        int x0 = (getWidth() - (BONUS_POR_LINHA * (LADO_BONUS + ESPACO) - ESPACO)) / 2;
        posicoesBonus = new int[bonus.length][];
        for (int i = 0; i < bonus.length; i++) {
            int x = x0 + (i % BONUS_POR_LINHA) * (LADO_BONUS + ESPACO);
            int yi = y + (i / BONUS_POR_LINHA) * (LADO_BONUS + ESPACO);
            posicoesBonus[i] = new int[] { x, yi };
            BufferedImage imagem = SpriteBonus.de(bonus[i]);
            if (imagem != null) {
                g2.drawImage(imagem, x, yi, LADO_BONUS, LADO_BONUS, null);
            }
            if (usados[i]) { // peca virada: ja foi usada
                g2.setColor(new Color(60, 60, 60, 170));
                g2.fillRect(x, yi, LADO_BONUS, LADO_BONUS);
            } else if (disponiveis[i]) {
                g2.setColor(DOURADO);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRect(x - 1, yi - 1, LADO_BONUS + 1, LADO_BONUS + 1);
            }
        }
    }

    /** Escreve o titulo centralizado a partir de y e devolve o y logo abaixo dele. */
    private int escreverTitulo(Graphics2D g2, String titulo, int y) {
        g2.setFont(FONTE_TITULO);
        g2.setColor(Color.BLACK);
        FontMetrics medidas = g2.getFontMetrics();
        g2.drawString(titulo, (getWidth() - medidas.stringWidth(titulo)) / 2, y + medidas.getAscent());
        return y + medidas.getHeight() + 4;
    }

    /** Indice da peca de bonus sob o ponto, ou -1. */
    private int bonusEm(int x, int y) {
        for (int i = 0; i < posicoesBonus.length && i < bonus.length; i++) {
            int[] p = posicoesBonus[i];
            if (p != null && x >= p[0] && x <= p[0] + LADO_BONUS && y >= p[1] && y <= p[1] + LADO_BONUS) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Usa a peca depois de confirmar (o uso e unico). No Bau, abre em seguida a escolha de um
     * dos tesouros sorteados. Recusas aparecem na faixa de mensagens do mapa.
     */
    private void usar(String peca) {
        String efeito = EFEITOS.get(peca);
        int resposta = JOptionPane.showConfirmDialog(janela,
                "Usar esta peça de bônus? Ela só pode ser usada uma vez.\n" + (efeito == null ? peca : efeito),
                "Usar peça de bônus", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }
        ControladorJogo controlador = ControladorJogo.getInstancia();
        String motivo = controlador.usarBonus(peca);
        if (motivo != null) {
            janela.mostrarMensagem("Não foi possível usar o bônus: " + motivo);
            return;
        }
        String[] opcoes = FachadaModelo.getInstancia().getOpcoesDoBau();
        if (opcoes.length > 0) {
            motivo = controlador.escolherTesouroDoBau(JanelaBau.escolher(janela, opcoes));
            if (motivo != null) {
                janela.mostrarMensagem(motivo);
            }
        }
    }

    /** Eventos tratados como no Cap. 12: classe ouvinte, sem lambda. */
    private class OuvinteMouse extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int i = bonusEm(e.getX(), e.getY());
            if (i >= 0 && disponiveis[i]) {
                usar(bonus[i]);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int i = bonusEm(e.getX(), e.getY());
            setCursor(i >= 0 && disponiveis[i]
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        }
    }
}
