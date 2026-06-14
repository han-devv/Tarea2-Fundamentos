package cc;

/**
 * Modelo de datos que representa una unica linea del subtitulo.
 * Implementa "Comparable" para poder ordenar las letras cronologicamente de forma sencilla.
 */
public class LineaLyric implements Comparable<LineaLyric> {
    private final long tiempoMs; // Instante en el que debe aparecer en milisegundos
    private final String texto;  // Texto de la cancion

    public LineaLyric(long tiempoMs, String texto) {
        this.tiempoMs = Math.max(0L, tiempoMs);
        this.texto = texto == null ? "" : texto.trim();
    }

    public LineaLyric(String tiempoLrc, String texto) {
        this(parseTiempoMs(tiempoLrc), texto);
    }

    public long getTiempoMs() {
        return tiempoMs;
    }

    public String getTexto() {
        return texto;
    }

    /**
     * Convierte el tiempo base a un formato legible [mm:ss.xx]
     */
    public String getTiempoFormateado() {
        long minutos = tiempoMs / 60_000L;
        long segundos = (tiempoMs % 60_000L) / 1_000L;
        long centesimas = (tiempoMs % 1_000L) / 10L;
        return String.format("%02d:%02d.%02d", minutos, segundos, centesimas);
    }

    /**
     * Determina el orden cronologico comparando los milisegundos.
     */
    @Override
    public int compareTo(LineaLyric otra) {
        return Long.compare(this.tiempoMs, otra.tiempoMs);
    }

    @Override
    public String toString() {
        return "[" + getTiempoFormateado() + "]" + texto;
    }

    //Metodos de parsing de respaldo por si se usan constructores en string puro
    public static long parseTiempoMs(String tiempoLrc) {
        if (tiempoLrc == null) {
            return 0L;
        }

        String limpio = tiempoLrc.trim();
        if (limpio.startsWith("[") && limpio.endsWith("]")) {
            limpio = limpio.substring(1, limpio.length() - 1);
        }

        String[] partes = limpio.split(":", 2);
        if (partes.length != 2) {
            return 0L;
        }

        long minutos = parseEnteroSeguro(partes[0]);
        String[] segPartes = partes[1].split("\\.", 2);
        long segundos = segPartes.length > 0 ? parseEnteroSeguro(segPartes[0]) : 0L;
        long centesimas = 0L;
        if (segPartes.length == 2) {
            String fraccion = (segPartes[1] + "00").substring(0, 2);
            centesimas = parseEnteroSeguro(fraccion);
        }

        return (minutos * 60_000L) + (segundos * 1_000L) + (centesimas * 10L);
    }

    private static long parseEnteroSeguro(String valor) {
        try {
            return Long.parseLong(valor.trim());
        } catch (Exception ex) {
            return 0L;
        }
    }
}