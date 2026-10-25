package view;

/**
 * Peças de tributo de uma província: qual província, de que tipo, quantas e se o
 * jogador da vez pode pegar uma agora (a pilha é destacada no mapa).
 */
class PilhaDeTributos {

    private final int provincia;
    private final String tipo; // "ESPADA", "YURT" ou "MOEDA", como vem da fachada
    private final int quantidade;
    private final boolean disponivel;

    PilhaDeTributos(int provincia, String tipo, int quantidade) {
        this(provincia, tipo, quantidade, false);
    }

    PilhaDeTributos(int provincia, String tipo, int quantidade, boolean disponivel) {
        this.provincia = provincia;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.disponivel = disponivel;
    }

    int getProvincia() {
        return provincia;
    }

    String getTipo() {
        return tipo;
    }

    int getQuantidade() {
        return quantidade;
    }

    boolean isDisponivel() {
        return disponivel;
    }
}
