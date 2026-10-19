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
    public String iniciarPartida(List<String> herois, List<String> cores) {
        try {
            FachadaModelo.getInstancia().novaPartida(ARQUIVO_MAPA, herois, cores);
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

    /** O jogador da vez ativa uma coluna (peao 0 age; a escolha do peao do Altani fica para depois). */
    public void ativarColuna(String coluna) {
        if (!partidaEmAndamento()) {
            return;
        }
        FachadaModelo.getInstancia().ativarColuna(coluna, 0);
        janelaTabuleiro.atualizar();
    }

    /**
     * Clique numa parada do tabuleiro: se o movimento for valido o peao anda; se nao,
     * nada acontece. Quando os movimentos acabam, o turno termina sozinho.
     */
    public void cliqueNaParada(int idParada) {
        if (!partidaEmAndamento()) {
            return;
        }
        FachadaModelo fachada = FachadaModelo.getInstancia();
        if (fachada.moverPeaoDaVez(idParada) && fachada.getMovimentosRestantes() == 0) {
            fachada.encerrarTurno();
        }
        janelaTabuleiro.atualizar();
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