package view;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/** Tabela unica nome da cor (como vem da fachada) -> Color do Swing. */
class CoresJogador {

    private static final Map<String, Color> CORES = new HashMap<String, Color>();

    static {
        CORES.put("AZUL", new Color(40, 90, 200));
        CORES.put("AMARELO", new Color(235, 195, 40));
        CORES.put("PRETO", new Color(30, 30, 30));
        CORES.put("VERMELHO", new Color(200, 40, 40));
        CORES.put("VERDE", new Color(40, 150, 60));
    }

    private CoresJogador() {
    }

    static Color de(String nome) {
        Color cor = CORES.get(nome);
        return cor == null ? Color.GRAY : cor;
    }
}