package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controller.ControladorJogo;
import model.FachadaModelo;

/** Escolha do numero de jogadores e do heroi e da cor de cada um, na ordem de jogada. */
class PainelSelecao extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Font FONTE_TITULO = new Font(Font.SERIF, Font.BOLD, 36);
    private static final Font FONTE = new Font(Font.SANS_SERIF, Font.PLAIN, 16);

    private static final Map<String, String> HABILIDADES = new HashMap<String, String>();
    private static final Map<String, Color> CORES = new HashMap<String, Color>();

    static {
        HABILIDADES.put("ALTANI", "Joga com dois peões e escolhe qual deles age no turno.");
        HABILIDADES.put("CHAGATAI", "Guarda para os próximos turnos as espadas que não usou.");
        HABILIDADES.put("JOCHI", "Começa com o primeiro espaço da coluna branca ocupado.");
        HABILIDADES.put("OGEDEI", "Pode pagar com moeda o primeiro tesouro de cada cidade.");
        HABILIDADES.put("TOLUI", "Começa com um yurt em Karakorum e constrói ao lado do peão.");

        CORES.put("AZUL", new Color(40, 90, 200));
        CORES.put("AMARELO", new Color(235, 195, 40));
        CORES.put("PRETO", new Color(30, 30, 30));
        CORES.put("VERMELHO", new Color(200, 40, 40));
        CORES.put("VERDE", new Color(40, 150, 60));
    }

    private final JanelaInicio janela;
    private final String[] herois;
    private final String[] cores;

    private final JComboBox<String> quantidade = new JComboBox<String>();
    private final List<LinhaJogador> linhas = new LinkedList<LinhaJogador>();

    PainelSelecao(JanelaInicio janela) {
        this.janela = janela;
        FachadaModelo fachada = FachadaModelo.getInstancia();
        herois = fachada.getHerois();
        cores = fachada.getCores();

        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));
        add(criarTopo(), BorderLayout.NORTH);
        add(criarLinhas(), BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);
        atualizarLinhasVisiveis();
    }

    private JPanel criarTopo() {
        JPanel topo = new JPanel(new BorderLayout(0, 12));
        JLabel titulo = new JLabel("Escolha dos personagens");
        titulo.setFont(FONTE_TITULO);
        topo.add(titulo, BorderLayout.NORTH);

        JPanel numero = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel rotulo = new JLabel("Número de jogadores:");
        rotulo.setFont(FONTE);
        numero.add(rotulo);
        for (int n = FachadaModelo.MIN_JOGADORES; n <= FachadaModelo.MAX_JOGADORES; n++) {
            quantidade.addItem(String.valueOf(n));
        }
        quantidade.setFont(FONTE);
        quantidade.addActionListener(new OuvinteQuantidade());
        numero.add(quantidade);
        topo.add(numero, BorderLayout.SOUTH);
        return topo;
    }

    private JPanel criarLinhas() {
        // BoxLayout respeita setVisible, ao contrario do GridLayout
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        for (int i = 0; i < FachadaModelo.MAX_JOGADORES; i++) {
            LinhaJogador linha = new LinhaJogador(i);
            linhas.add(linha);
            painel.add(linha);
            painel.add(Box.createVerticalStrut(10));
        }
        painel.add(Box.createVerticalGlue());
        return painel;
    }

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout());
        JLabel aviso = new JLabel("Moedas iniciais: 1º e 2º jogadores recebem 1 moeda; os demais, 2.");
        aviso.setFont(FONTE);
        rodape.add(aviso, BorderLayout.WEST);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton voltar = new JButton("Voltar");
        voltar.setFont(FONTE);
        voltar.addActionListener(new OuvinteVoltar());
        JButton comecar = new JButton("Começar");
        comecar.setFont(FONTE.deriveFont(Font.BOLD));
        comecar.addActionListener(new OuvinteComecar());
        botoes.add(voltar);
        botoes.add(comecar);
        rodape.add(botoes, BorderLayout.EAST);
        return rodape;
    }

    private int getQuantidadeJogadores() {
        return FachadaModelo.MIN_JOGADORES + quantidade.getSelectedIndex();
    }

    private void atualizarLinhasVisiveis() {
        int n = getQuantidadeJogadores();
        for (int i = 0; i < linhas.size(); i++) {
            linhas.get(i).setVisible(i < n);
        }
        revalidate();
        repaint();
    }

    private void comecar() {
        List<String> heroisEscolhidos = new LinkedList<String>();
        List<String> coresEscolhidas = new LinkedList<String>();
        for (int i = 0; i < getQuantidadeJogadores(); i++) {
            heroisEscolhidos.add(linhas.get(i).getHeroi());
            coresEscolhidas.add(linhas.get(i).getCor());
        }
        String erro = ControladorJogo.getInstancia().iniciarPartida(heroisEscolhidos, coresEscolhidas);
        if (erro != null) {
            JOptionPane.showMessageDialog(this, erro, "Seleção inválida", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static JLabel criarRotulo(String texto) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setFont(FONTE);
        return rotulo;
    }

    /** "TOLUI" vira "Tolui". */
    private static String formatar(String nome) {
        return nome.charAt(0) + nome.substring(1).toLowerCase();
    }

    /** Uma linha por jogador; os combos guardam os nomes formatados na mesma ordem da fachada. */
    private class LinhaJogador extends JPanel {

        private static final long serialVersionUID = 1L;

        private final JComboBox<String> comboHeroi = new JComboBox<String>();
        private final JComboBox<String> comboCor = new JComboBox<String>();
        private final AmostraCor amostra = new AmostraCor();
        private final JLabel habilidade = new JLabel();

        LinhaJogador(int indice) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));
            setBorder(BorderFactory.createEtchedBorder());
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

            JLabel rotulo = new JLabel("Jogador " + (indice + 1));
            rotulo.setFont(FONTE.deriveFont(Font.BOLD));
            rotulo.setPreferredSize(new Dimension(90, 30));
            add(rotulo);

            for (String h : herois) {
                comboHeroi.addItem(formatar(h));
            }
            for (String c : cores) {
                comboCor.addItem(formatar(c));
            }
            // cada linha comeca com um heroi e uma cor diferentes
            comboHeroi.setSelectedIndex(indice % herois.length);
            comboCor.setSelectedIndex(indice % cores.length);
            comboHeroi.setFont(FONTE);
            comboCor.setFont(FONTE);
            comboHeroi.setPreferredSize(new Dimension(140, 30));
            comboCor.setPreferredSize(new Dimension(130, 30));
            comboHeroi.addActionListener(new OuvinteEscolha());
            comboCor.addActionListener(new OuvinteEscolha());

            add(criarRotulo("Herói:"));
            add(comboHeroi);
            add(criarRotulo("Cor:"));
            add(comboCor);
            add(amostra);
            habilidade.setFont(FONTE.deriveFont(Font.ITALIC, 14f));
            add(habilidade);
            atualizar();
        }

        String getHeroi() {
            return herois[comboHeroi.getSelectedIndex()];
        }

        String getCor() {
            return cores[comboCor.getSelectedIndex()];
        }

        private void atualizar() {
            habilidade.setText(HABILIDADES.get(getHeroi()));
            amostra.repaint();
        }

        private class OuvinteEscolha implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                atualizar();
            }
        }

        /** Quadrado com a cor escolhida, desenhado com Graphics. */
        private class AmostraCor extends JPanel {

            private static final long serialVersionUID = 1L;

            AmostraCor() {
                setPreferredSize(new Dimension(28, 28));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Color cor = CORES.get(getCor());
                g.setColor(cor == null ? Color.GRAY : cor);
                g.fillRect(2, 2, getWidth() - 4, getHeight() - 4);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(2, 2, getWidth() - 5, getHeight() - 5);
            }
        }
    }

    private class OuvinteQuantidade implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            atualizarLinhasVisiveis();
        }
    }

    private class OuvinteVoltar implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            janela.mostrarInicio();
        }
    }

    private class OuvinteComecar implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            comecar();
        }
    }
}
