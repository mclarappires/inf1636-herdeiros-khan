package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.FachadaModelo;

/**
 * Escolha dos tesouros para o conselheiro que aceita qualquer tipo: um JComboBox por tesouro
 * pedido, com os tipos que o jogador da vez tem. Modal; quem decide se a oferta vale e o Model.
 */
class JanelaEscolhaTesouros extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final Font FONTE = new Font(Font.SANS_SERIF, Font.PLAIN, 15);

    private final List<JComboBox<String>> combos = new ArrayList<JComboBox<String>>();
    private String[] escolhidos = null;

    private JanelaEscolhaTesouros(Window dono, int quantidade) {
        super(dono, "Entregar tesouros", ModalityType.APPLICATION_MODAL);
        FachadaModelo fachada = FachadaModelo.getInstancia();
        int vez = fachada.getJogadorDaVez();
        List<String> tipos = new ArrayList<String>();
        for (String tipo : fachada.getTiposDeTesouro()) {
            if (fachada.getTesouroDisponivel(vez, tipo) > 0) {
                tipos.add(tipo);
            }
        }

        JPanel conteudo = new JPanel(new BorderLayout(0, 10));
        conteudo.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel rotulo = new JLabel("Escolha " + quantidade + " tesouros para o conselheiro:");
        rotulo.setFont(FONTE);
        conteudo.add(rotulo, BorderLayout.NORTH);

        JPanel linhas = new JPanel(new GridLayout(quantidade, 1, 0, 6));
        for (int i = 0; i < quantidade; i++) {
            JComboBox<String> combo = new JComboBox<String>(tipos.toArray(new String[tipos.size()]));
            combo.setFont(FONTE);
            combos.add(combo);
            linhas.add(combo);
        }
        conteudo.add(linhas, BorderLayout.CENTER);

        JButton entregar = new JButton("Entregar");
        entregar.setEnabled(!tipos.isEmpty());
        entregar.addActionListener(new OuvinteEntregar());
        JButton cancelar = new JButton("Cancelar");
        cancelar.addActionListener(new OuvinteCancelar());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.add(cancelar);
        botoes.add(entregar);
        conteudo.add(botoes, BorderLayout.SOUTH);

        setContentPane(conteudo);
        pack();
        setLocationRelativeTo(dono);
    }

    /** Abre a janela e espera; devolve os tipos escolhidos (com repetição) ou null se cancelou. */
    static String[] escolher(Window dono, int quantidade) {
        JanelaEscolhaTesouros janela = new JanelaEscolhaTesouros(dono, quantidade);
        janela.setVisible(true); // modal: só volta quando fechar
        return janela.escolhidos;
    }

    private class OuvinteEntregar implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            escolhidos = new String[combos.size()];
            for (int i = 0; i < escolhidos.length; i++) {
                escolhidos[i] = (String) combos.get(i).getSelectedItem();
            }
            dispose();
        }
    }

    private class OuvinteCancelar implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            escolhidos = null;
            dispose();
        }
    }
}
