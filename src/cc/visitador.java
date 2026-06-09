package cc;

import cc.analysis.*;
import cc.node.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class visitador extends DepthFirstAdapter{
    // Repositorio para las líneas de la canción sincronizadas
    ArrayList<LineaLyric> lineasCancion = new ArrayList<>();

    // Repositorio para los metadatos (ej: "ar" -> "Queen")
    HashMap<String, String> metadatos = new HashMap<>();

    @Override
    public void caseASongLine(ASongLine node) {
        // Limpiamos los strings (es buena idea quitar espacios en blanco o corchetes extra aquí)
        String tiempo = node.getTiempo().toString().trim();
        String texto = node.getCadena().toString().trim();

        // Guardamos el objeto encapsulado
        lineasCancion.add(new LineaLyric(tiempo, texto));
    }

    @Override
    public void caseADataLine(ADataLine node) {
        // Suponiendo que getCatg() trae "ar", "ti", etc., y getCadena() trae el valor
        String categoria = node.getCatg().toString().trim();
        String valor = node.getCadena().toString().trim();

        metadatos.put(categoria, valor);
    }
}
