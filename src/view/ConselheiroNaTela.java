package view;

/**
 * Um conselheiro no mapa: a parada onde ele fica, a peça de bônus que ainda está sobre ele
 * (null se já foi coletada) e se o jogador da vez pode entregar tesouros a ele agora.
 */
class ConselheiroNaTela {

    private final int parada;
    private final String bonus;
    private final boolean disponivel;

    ConselheiroNaTela(int parada, String bonus, boolean disponivel) {
        this.parada = parada;
        this.bonus = bonus;
        this.disponivel = disponivel;
    }

    int getParada() {
        return parada;
    }

    String getBonus() {
        return bonus;
    }

    boolean isDisponivel() {
        return disponivel;
    }
}
