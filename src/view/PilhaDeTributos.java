package view;

/** Peças de tributo de uma província: qual província, de que tipo e quantas. */
class PilhaDeTributos {

    private final int provincia;
    private final String tipo; // "ESPADA", "YURT" ou "MOEDA", como vem da fachada
    private final int quantidade;

    PilhaDeTributos(int provincia, String tipo, int quantidade) {
        this.provincia = provincia;
        this.tipo = tipo;
        this.quantidade = quantidade;
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
}