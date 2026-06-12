package cc;

import cc.analysis.*;
import cc.node.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class visitador extends DepthFirstAdapter{
    // Repositorio para las líneas de la canción sincronizadas
    private final ArrayList<LineaLyric> lineasCancion = new ArrayList<>();

    // Repositorio para los metadatos (ej: "ar" -> "Queen")
    private final LinkedHashMap<String, String> metadatos = new LinkedHashMap<>();

    @Override
    public void outASongLine(ASongLine node) {
        lineasCancion.add(new LineaLyric(extraerTiempoMs(node.getTiempo()), extraerCadena(node.getCadena())));
    }

    @Override
    public void outADataLine(ADataLine node) {
        String categoria = tokenText(node.getCatg());
        if (!categoria.isEmpty()) {
            metadatos.put(categoria, extraerCadena(node.getCadena()));
        }
    }

    public List<LineaLyric> getLineasCancion() {
        ArrayList<LineaLyric> copia = new ArrayList<>(lineasCancion);
        Collections.sort(copia);
        return copia;
    }

    public Map<String, String> getMetadatos() {
        return Collections.unmodifiableMap(metadatos);
    }

    public void limpiar() {
        lineasCancion.clear();
        metadatos.clear();
    }

    private long extraerTiempoMs(PTiempo tiempo) {
        if (!(tiempo instanceof ATiempo)) {
            return 0L;
        }

        ATiempo at = (ATiempo) tiempo;
        long minutos = parseLong(tokenText(at.getMin()));
        long segundos = parseLong(tokenText(at.getSeg()));
        long centesimas = parseLong(tokenText(at.getMs()));
        return (minutos * 60_000L) + (segundos * 1_000L) + (centesimas * 10L);
    }

    private String extraerCadena(PCadena cadena) {
        if (cadena == null) {
            return "";
        }

        if (cadena instanceof ASimpleCadena) {
            return tokenText(((ASimpleCadena) cadena).getTitulo());
        }

        if (cadena instanceof ACompuestaCadena) {
            ACompuestaCadena compuesta = (ACompuestaCadena) cadena;
            return tokenText(compuesta.getTitulo()) + extraerCadena(compuesta.getCadena());
        }

        return cadena.toString().trim();
    }

    private String tokenText(Token token) {
        return token == null ? "" : token.getText().trim();
    }

    private long parseLong(String valor) {
        try {
            return Long.parseLong(valor);
        } catch (Exception ex) {
            return 0L;
        }
    }
}
