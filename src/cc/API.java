package cc;

import cc.analysis.*;
import cc.node.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class API extends DepthFirstAdapter {
    private final ArrayList<LineaLyric> lineasCancion = new ArrayList<>();
    private final LinkedHashMap<String, String> metadatos = new LinkedHashMap<>();

    @Override
    public void outALinea(ALinea node) {
        String izq = node.getIzq() != null ? node.getIzq().getText().trim() : "";

        // Reensambla todo lo que esté dentro de los corchetes
        String der = extraerTexto(node.getAdentro()).trim();

        // Reensambla la letra de la canción (fuera de los corchetes)
        String letra = extraerTexto(node.getAfuera()).trim();

        if (izq.matches("\\d+")) {
            long tiempoMs = calcularTiempoMs(izq, der);
            lineasCancion.add(new LineaLyric(tiempoMs, letra));
        } else {
            if (!izq.isEmpty()) {
                metadatos.put(izq, der);
            }
        }
    }

    // Método mágico que vuelve a unir texto picado por los "dos puntos"
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

    public List<LineaLyric> getLineasCancion() {
        ArrayList<LineaLyric> copia = new ArrayList<>(lineasCancion);
        Collections.sort(copia);
        return copia;
    }

    public Map<String, String> getMetadatos() {
        return Collections.unmodifiableMap(metadatos);
    }

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

            return (minutos * 60_000L) + (segundos * 1_000L) + (centesimas * 10L);
        } catch (Exception e) {
            return 0L;
        }
    }
}