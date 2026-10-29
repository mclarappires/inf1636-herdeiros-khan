package view;

/** O que o mapa mostra da area de melhorias: a peca de cada espaco, o que da para pegar e a pilha. */
class AreaNaTela {

    private final String[] simbolos;      // null = espaco vazio
    private final String[] cores;         // nome da coluna colorida de origem
    private final boolean[] disponiveis;  // o jogador da vez pode comprar / pegar a peca agora
    private final int tamanhoDaPilha;
    private final boolean pilhaDisponivel;
    private final boolean gratuita;       // ha melhoria gratis a escolher (em vez de comprar)

    AreaNaTela(String[] simbolos, String[] cores, boolean[] disponiveis, int tamanhoDaPilha,
            boolean pilhaDisponivel, boolean gratuita) {
        this.simbolos = simbolos;
        this.cores = cores;
        this.disponiveis = disponiveis;
        this.tamanhoDaPilha = tamanhoDaPilha;
        this.pilhaDisponivel = pilhaDisponivel;
        this.gratuita = gratuita;
    }

    int getEspacos() {
        return simbolos.length;
    }

    String getSimbolo(int espaco) {
        return simbolos[espaco];
    }

    String getCor(int espaco) {
        return cores[espaco];
    }

    boolean isDisponivel(int espaco) {
        return disponiveis[espaco];
    }

    int getTamanhoDaPilha() {
        return tamanhoDaPilha;
    }

    boolean isPilhaDisponivel() {
        return pilhaDisponivel;
    }

    boolean isGratuita() {
        return gratuita;
    }
}