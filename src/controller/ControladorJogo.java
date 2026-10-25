package controller;

import java.io.IOException;
import java.util.List;

import javax.swing.SwingUtilities;

import model.FachadaModelo;
import view.JanelaInicio;
import view.JanelaTabuleiro;

/** Liga as telas ao model e decide qual janela aparece. */
public class ControladorJogo {

    private static final String ARQUIVO_MAPA = "resources/mapa_herdeiros.txt";

    private static ControladorJogo instancia = null;

    private JanelaInicio janelaInicio = null;
    private JanelaTabuleiro janelaTabuleiro = null;

    private ControladorJogo() {
    }

    public static ControladorJogo getInstancia() {
        if (instancia == null) {
            instancia = new ControladorJogo();
        }
        return instancia;
    }

    public void mostrarInicio() {
        if (janelaInicio == null) {
            janelaInicio = new JanelaInicio();
        }
        janelaInicio.mostrarInicio();
        janelaInicio.setVisible(true);
    }

    /**
     * Cria a partida com as escolhas da tela de selecao e abre o tabuleiro.
     * Devolve null se deu certo, ou a mensagem de erro para a tela mostrar.
     */
    public String iniciarPartida(List<String> herois) {
        try {
            FachadaModelo.getInstancia().novaPartida(ARQUIVO_MAPA, herois);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (IOException e) {
            return "Nao foi possivel ler o mapa: " + ARQUIVO_MAPA;
        }
        if (janelaInicio != null) {
            janelaInicio.dispose();
            janelaInicio = null;
        }
        janelaTabuleiro = new JanelaTabuleiro();
        janelaTabuleiro.setVisible(true);
        return null;
    }

    /**
     * O jogador da vez ativa uma coluna no seu tabuleiro; indicePeao escolhe qual peao age
     * (so a Altani tem dois). Se der certo, volta para o mapa para ele mover o peao.
     * Devolve false se a coluna nao pode ser ativada agora.
     */
    public boolean ativarColuna(String coluna, int indicePeao) {
        if (!partidaEmAndamento()) {
            return false;
        }
        if (!FachadaModelo.getInstancia().ativarColuna(coluna, indicePeao)) {
            return false;
        }
        janelaTabuleiro.atualizar();
        janelaTabuleiro.mostrarMapa();
        return true;
    }

    /**
     * Clique numa parada do tabuleiro: se o movimento for valido o peao anda; se nao,
     * nada acontece. O turno so termina pelo botao Passar a vez.
     */
    public void cliqueNaParada(int idParada) {
        if (!partidaEmAndamento()) {
            return;
        }
        FachadaModelo.getInstancia().moverPeaoDaVez(idParada);
        janelaTabuleiro.atualizar();
    }

    /**
     * Encerra o turno do jogador da vez e mostra o mapa para o proximo.
     * Devolve null se a vez passou, ou o motivo para a tela mostrar.
     */
    public String passarTurno() {
        if (!partidaEmAndamento()) {
            return null;
        }
        String motivo = FachadaModelo.getInstancia().encerrarTurno();
        if (motivo == null) {
            janelaTabuleiro.atualizar();
            janelaTabuleiro.mostrarMapa();
        }
        return motivo;
    }

    private boolean partidaEmAndamento() {
        return janelaTabuleiro != null && FachadaModelo.getInstancia().temPartida();
    }

    public void sair() {
        System.exit(0);
    }

    public static void main(String[] args) {
        // Runnable de classe anonima (lambda e proibido no enunciado)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getInstancia().mostrarInicio();
            }
        });
    }
}