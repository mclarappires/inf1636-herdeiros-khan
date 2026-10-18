package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;

import controller.ControladorJogo;

/** Tela de inicio: titulo desenhado sobre o tabuleiro escurecido e os botoes do menu. */
class PainelInicio extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final String TITULO = "Herdeiros do Khan";
    private static final Color FUNDO = new Color(60, 40, 25);
    private static final Color DOURADO = new Color(235, 195, 90);

    private final JanelaInicio janela;
    private Image imagemFundo;

    PainelInicio(JanelaInicio janela) {
        this.janela = janela;
        carregarImagem("resources/tabuleiro.png");
        setBackground(FUNDO);
        setLayout(new GridBagLayout());

        JPanel botoes = new JPanel(new GridLayout(3, 1, 0, 14));
        botoes.setOpaque(false);
        botoes.add(criarBotao("Novo jogo", new OuvinteNovoJogo()));
        JButton carregar = criarBotao("Carregar jogo", null);
        carregar.setEnabled(false);
        carregar.setToolTipText("Em breve");
        botoes.add(carregar);
        botoes.add(criarBotao("Sair", new OuvinteSair()));

        // botoes um pouco abaixo do centro, deixando o titulo em cima
        GridBagConstraints posicao = new GridBagConstraints();
        posicao.insets = new Insets(160, 0, 0, 0);
        add(botoes, posicao);
    }

    private static JButton criarBotao(String texto, ActionListener ouvinte) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font(Font.SERIF, Font.BOLD, 22));
        botao.setPreferredSize(new Dimension(260, 52));
        botao.setFocusPainted(false);
        if (ouvinte != null) {
            botao.addActionListener(ouvinte);
        }
        return botao;
    }

    private void carregarImagem(String caminho) {
        try {
            imagemFundo = ImageIO.read(new File(caminho));
        } catch (IOException e) {
            imagemFundo = null; // sem imagem fica so a cor de fundo
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (imagemFundo != null) {
            // cobre o painel inteiro mantendo a proporcao
            int largura = imagemFundo.getWidth(null);
            int altura = imagemFundo.getHeight(null);
            double escala = Math.max((double) getWidth() / largura,
                    (double) getHeight() / altura);
            int l = (int) (largura * escala);
            int a = (int) (altura * escala);
            g2.drawImage(imagemFundo, (getWidth() - l) / 2, (getHeight() - a) / 2, l, a, null);
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        g2.setFont(new Font(Font.SERIF, Font.BOLD, 72));
        FontMetrics medidas = g2.getFontMetrics();
        int x = (getWidth() - medidas.stringWidth(TITULO)) / 2;
        int y = getHeight() / 2 - 120;
        g2.setColor(Color.BLACK);
        g2.drawString(TITULO, x + 3, y + 3);
        g2.setColor(DOURADO);
        g2.drawString(TITULO, x, y);
    }

    private class OuvinteNovoJogo implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            janela.mostrarSelecao();
        }
    }

    private static class OuvinteSair implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ControladorJogo.getInstancia().sair();
        }
    }
}
