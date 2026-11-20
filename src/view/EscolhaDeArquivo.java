package view;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Escolha do arquivo .txt da partida salva com JFileChooser, para salvar e para carregar. */
class EscolhaDeArquivo {

    private static final String EXTENSAO = "txt";

    // a pasta escolhida da ultima vez, para o proximo dialogo abrir nela
    private static File ultimaPasta = null;

    private EscolhaDeArquivo() {
    }

    /** Caminho do arquivo escolhido para carregar, ou null se o jogador cancelou. */
    static String paraAbrir(Component pai) {
        JFileChooser escolha = criar("Carregar partida");
        if (escolha.showOpenDialog(pai) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return lembrar(escolha.getSelectedFile()).getPath();
    }

    /** Caminho do arquivo escolhido para salvar (com .txt no fim), ou null se o jogador cancelou. */
    static String paraSalvar(Component pai) {
        JFileChooser escolha = criar("Salvar partida");
        if (escolha.showSaveDialog(pai) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File arquivo = lembrar(escolha.getSelectedFile());
        String caminho = arquivo.getPath();
        if (!caminho.toLowerCase().endsWith("." + EXTENSAO)) {
            caminho += "." + EXTENSAO;
        }
        return caminho;
    }

    private static JFileChooser criar(String titulo) {
        JFileChooser escolha = new JFileChooser(ultimaPasta);
        escolha.setDialogTitle(titulo);
        escolha.setFileFilter(new FileNameExtensionFilter("Partida salva (*.txt)", EXTENSAO));
        escolha.setAcceptAllFileFilterUsed(false);
        return escolha;
    }

    private static File lembrar(File arquivo) {
        ultimaPasta = arquivo.getParentFile();
        return arquivo;
    }
}
