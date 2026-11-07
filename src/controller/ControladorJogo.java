package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import model.FachadaModelo;
import model.ObservadoIF;
import model.ObservadorIF;
import view.JanelaInicio;
import view.JanelaTabuleiro;

/**
 * Liga as telas ao model e decide qual janela aparece. A tela se registra aqui (registra) e
 * passa a ser avisada de duas coisas: das mudancas no estado da partida, que a Fachada
 * notifica, e das mudancas de tela (mapa, cartela, cartas...), que o Controller notifica.
 */
public class ControladorJogo implements ObservadoIF {

    /** get(TELA) devolve a tela que deve aparecer (uma das TELA_*). */
    public static final int TELA = 100;

    public static final int TELA_MAPA = 1;
    public static final int TELA_CARTELA = 2;
    public static final int TELA_CARTAS = 3;
    public static final int TELA_ESCOLHA_CARTA = 4;
    public static final int TELA_RESULTADO = 5;

    private static final String ARQUIVO_MAPA = "resources/mapa_herdeiros.txt";

    private static ControladorJogo instancia = null;

    private JanelaInicio janelaInicio = null;
    private JanelaTabuleiro janelaTabuleiro = null;

    private final List<ObservadorIF> observadoresDeTela = new ArrayList<ObservadorIF>();
    private int telaAtual = TELA_MAPA;

    private ControladorJogo() {
    }

    //OBSERVER

    /**
     * A tela pede para ser avisada: das mudancas na partida (o pedido segue para a Fachada) e
     * das mudancas de tela (guardado aqui).
     */
    public void registra(ObservadorIF o) {
        FachadaModelo.getInstancia().add(o);
        add(o);
    }

    /** Cancela os dois registros feitos por registra. */
    public void cancelaRegistro(ObservadorIF o) {
        FachadaModelo.getInstancia().remove(o);
        remove(o);
    }

    @Override
    public void add(ObservadorIF o) {
        if (o != null && !observadoresDeTela.contains(o)) {
            observadoresDeTela.add(o);
        }
    }

    @Override
    public void remove(ObservadorIF o) {
        observadoresDeTela.remove(o);
    }

    /** Tela que o observador deve mostrar: get(TELA). */
    @Override
    public int get(int i) {
        if (i == TELA) {
            return telaAtual;
        }
        throw new IllegalArgumentException("Dado desconhecido: " + i);
    }

    //avisa as telas registradas (numa copia, porque uma tela pode se remover ao ser avisada)
    private void mostrarTela(int tela) {
        telaAtual = tela;
        for (ObservadorIF o : new ArrayList<ObservadorIF>(observadoresDeTela)) {
            o.notify(this);
        }
    }

    /** Mostra o mapa no centro da janela. */
    public void abrirMapa() {
        mostrarTela(TELA_MAPA);
    }

    /** Mostra o tabuleiro (cartela com as colunas) do jogador da vez. */
    public void abrirCartela() {
        if (partidaEmAndamento()) {
            mostrarTela(TELA_CARTELA);
        }
    }

    /** Abre a janela com as cartas do jogador da vez. */
    public void abrirCartas() {
        if (partidaEmAndamento()) {
            mostrarTela(TELA_CARTAS);
        }
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
        if (janelaTabuleiro != null) { // partida anterior: a janela velha deixa de ser avisada
            cancelaRegistro(janelaTabuleiro);
            janelaTabuleiro.dispose();
        }
        janelaTabuleiro = new JanelaTabuleiro(); // se registra neste controller
        janelaTabuleiro.setVisible(true);
        mostrarTela(TELA_ESCOLHA_CARTA); // antes da primeira rodada
        return null;
    }

    /**
     * O proximo jogador que ainda nao escolheu guarda a carta secreta de id dado. Quando todos
     * tiverem escolhido, mostra o mapa e a partida comeca. Devolve null se deu certo, ou o motivo.
     */
    public String escolherCartaSecreta(String idCarta) {
        if (!partidaEmAndamento()) {
            return null;
        }
        FachadaModelo fachada = FachadaModelo.getInstancia();
        String motivo = fachada.escolherCartaSecreta(fachada.getProximoJogadorSemCartaSecreta(), idCarta);
        if (motivo == null && !fachada.isEscolhaDeCartasPendente()) {
            mostrarTela(TELA_MAPA);
        }
        return motivo;
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
        mostrarTela(TELA_MAPA);
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
        FachadaModelo.getInstancia().moverPeaoDaVez(idParada); // a Fachada avisa a tela
    }

    /**
     * Clique numa melhoria da area: se o jogador da vez tem melhoria gratis a escolher, pega
     * a peca de graca; senao, compra (2 moedas). Devolve null se deu certo, ou o motivo.
     */
    public String cliqueNaMelhoria(int espaco) {
        if (!partidaEmAndamento()) {
            return null;
        }
        FachadaModelo fachada = FachadaModelo.getInstancia();
        String motivo = fachada.getMelhoriasGratuitasPendentes() > 0
                ? fachada.pegarMelhoriaGratuita(espaco) : fachada.comprarMelhoria(espaco);
        return motivo;
    }

    /** Clique na pilha de melhorias: igual a cliqueNaMelhoria, com a peca do topo (sem ver antes). */
    public String cliqueNaPilhaDeMelhorias() {
        if (!partidaEmAndamento()) {
            return null;
        }
        FachadaModelo fachada = FachadaModelo.getInstancia();
        String motivo = fachada.getMelhoriasGratuitasPendentes() > 0
                ? fachada.pegarMelhoriaGratuitaDoTopo() : fachada.comprarMelhoriaDoTopo();
        return motivo;
    }

    /**
     * Clique com o botao direito numa parada: o jogador da vez gasta 1 peca de yurt para
     * construir um yurt nela. Devolve null se construiu, ou o motivo para a tela mostrar.
     */
    public String cliqueDireitoNaParada(int idParada) {
        if (!partidaEmAndamento()) {
            return null;
        }
        String motivo = FachadaModelo.getInstancia().construirYurt(idParada);
        return motivo;
    }

    /**
     * Clique na pilha de tributo de uma provincia: o jogador da vez gasta 1 punho para
     * pegar 1 peca. Devolve null se pegou, ou o motivo para a tela mostrar.
     */
    public String cliqueNaProvincia(int idProvincia) {
        if (!partidaEmAndamento()) {
            return null;
        }
        String motivo = FachadaModelo.getInstancia().pegarTributo(idProvincia);
        return motivo;
    }

    /**
     * Clique num lugar do Khan (provincia do Khan ou area de melhorias): o jogador da vez
     * move o Khan para la. Devolve null se moveu, ou o motivo para a tela mostrar.
     */
    public String cliqueNoKhan(int local) {
        if (!partidaEmAndamento()) {
            return null;
        }
        String motivo = FachadaModelo.getInstancia().usarKhan(local);
        return motivo;
    }

    /**
     * Clique numa peca de bonus do inventario: o jogador da vez a usa. No Bau, a tela ainda
     * pede a escolha do tesouro (escolherTesouroDoBau). Devolve null se usou, ou o motivo.
     */
    public String usarBonus(String bonus) {
        if (!partidaEmAndamento()) {
            return null;
        }
        String motivo = FachadaModelo.getInstancia().usarBonus(bonus);
        return motivo;
    }

    /** O jogador da vez fica com um dos tesouros sorteados pelo Bau. Devolve null ou o motivo. */
    public String escolherTesouroDoBau(String tesouro) {
        if (!partidaEmAndamento()) {
            return null;
        }
        String motivo = FachadaModelo.getInstancia().escolherTesouroDoBau(tesouro);
        return motivo;
    }

    /**
     * Clique num tesouro de uma cidade: o jogador da vez paga espadas para toma-lo.
     * Devolve null se tomou, ou o motivo para a tela mostrar.
     */
    public String cliqueNoTesouroDaCidade(int idCidade, String tesouro) {
        if (!partidaEmAndamento()) {
            return null;
        }
        String motivo = FachadaModelo.getInstancia().atacarCidade(idCidade, tesouro);
        return motivo;
    }

    /**
     * Clique no conselheiro de uma parada: o jogador da vez entrega os tesouros escolhidos
     * (tipos com repeticao). Devolve null se entregou, ou o motivo para a tela mostrar.
     */
    public String entregarTesouros(int idParada, String[] tesouros) {
        if (!partidaEmAndamento()) {
            return null;
        }
        String motivo = FachadaModelo.getInstancia().entregarTesouros(idParada, tesouros);
        return motivo;
    }

    /**
     * Encerra o turno do jogador da vez e mostra o mapa para o proximo.
     * Devolve null se a vez passou, ou o motivo para a tela mostrar.
     */
    public String passarTurno() {
        if (!partidaEmAndamento()) {
            return null;
        }
        FachadaModelo fachada = FachadaModelo.getInstancia();
        String motivo = fachada.encerrarTurno();
        if (motivo == null) {
            mostrarTela(fachada.isPartidaEncerrada() ? TELA_RESULTADO : TELA_MAPA);
        }
        return motivo;
    }

    /** Ha partida iniciada e a ultima rodada ainda nao acabou (depois disso nenhuma acao vale). */
    private boolean partidaEmAndamento() {
        FachadaModelo fachada = FachadaModelo.getInstancia();
        return janelaTabuleiro != null && fachada.temPartida() && !fachada.isPartidaEncerrada();
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