package cc;

import cc.analysis.*;
import cc.node.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Clase visitadora que reconoce el arbol de derivacion generado por el sablecc,
 * su funcion principal es obtener la informacion de cada item del archivo .lrc
 * y almacenarlos en memoria para ser usado mas adelante.
 */

public class API extends DepthFirstAdapter {
    private final ArrayList<LineaLyric> lineasCancion = new ArrayList<>();
    private final LinkedHashMap<String, String> metadatos = new LinkedHashMap<>();

    /**
     * Metodo que visita el nodo ALinea del árbol sintáctico.
     * Evalúa si la línea corresponde a un metadato o a una línea de tiempo con letra sincronizada.
     */
    @Override
    public void outALinea(ALinea node) {
        // Obtiene la parte izquierda
        String izq = node.getIzq() != null ? node.getIzq().getText().trim() : "";

        // Reensambla todo lo que este dentro de los corchetes
        String der = extraerTexto(node.getAdentro()).trim();

        // Reensambla la letra de la canción (fuera de los corchetes)
        String letra = extraerTexto(node.getAfuera()).trim();

        // Si la parte izquierda es un numero, significa que es una marca de tiempo
        if (izq.matches("\\d+")) {
            long tiempoMs = calcularTiempoMs(izq, der);
            lineasCancion.add(new LineaLyric(tiempoMs, letra));
        } else {
            // Si no es nymero y no esta vacio, entonces es un metadato (ar, ti ,al, etc)
            if (!izq.isEmpty()) {
                metadatos.put(izq, der);
            }
        }
    }

    /**
     * Metodo auxiliar que vuelve a unir texto fragmentado por los "dos puntos" en la gramatica.
     */
    private String extraerTexto(LinkedList<PFragmento> lista) {
        StringBuilder sb = new StringBuilder();
        for (PFragmento frag : lista) {
            if (frag instanceof ATxtFragmento) {
                sb.append(((ATxtFragmento) frag).getTexto().getText());
            } else if (frag instanceof AColFragmento) {
                sb.append(((AColFragmento) frag).getDPuntos().getText());
            }
        }
        return sb.toString();
    }

    /**
     * Retorna la lista de letras de la cancion ordenadas cronologicamente.
     */
    public List<LineaLyric> getLineasCancion() {
        ArrayList<LineaLyric> copia = new ArrayList<>(lineasCancion);
        Collections.sort(copia);
        return copia;
    }

    /**
     * Retorna el mapa inmodificable de metadatos reconocidos.
     */
    public Map<String, String> getMetadatos() {
        return Collections.unmodifiableMap(metadatos);
    }

    /**
     * Convierte el formato de tiempo [mm:ss.xx] a milisegundos totales para facilitar la sincronización.
     */
    private long calcularTiempoMs(String minStr, String segMsStr) {
        try {
            long minutos = Long.parseLong(minStr.trim());
            long segundos = 0;
            long centesimas = 0;

            // Por si el archivo tiene un formato raro tipo [00:06:28]
            segMsStr = segMsStr.replace(":", ".");

            if (segMsStr.contains(".")) {
                String[] partes = segMsStr.split("\\.");
                segundos = Long.parseLong(partes[0].trim());
                if (partes.length > 1) {
                    centesimas = Long.parseLong(partes[1].trim());
                }
            } else {
                segundos = Long.parseLong(segMsStr.trim());
            }

            // Formula de conversión a milisegundos
            return (minutos * 60_000L) + (segundos * 1_000L) + (centesimas * 10L);
        } catch (Exception e) {
            return 0L;
        }
    }
}