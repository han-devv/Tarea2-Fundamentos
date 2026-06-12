package cc;

import cc.lexer.Lexer;
import cc.node.Start;
import cc.parser.Parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PushbackReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class KaraokeDemo {
//esta guea moría por los espacios casi muero
    private static final String DEFAULT_MP3 = "repmp3/libs/metallica.mp3";
    private static final String DEFAULT_LRC = "repmp3/lyrics/nothing else matters.lrc";

    private static final class RutasEntrada {
        private final String mp3;
        private final String lrc;

        private RutasEntrada(String mp3, String lrc) {
            this.mp3 = mp3;
            this.lrc = lrc;
        }
    }

    private static visitador analizarArchivo(String rutaLrc) throws Exception {
        visitador recolector = new visitador();

        try (BufferedReader reader = new BufferedReader(new FileReader(rutaLrc))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }

                try (PushbackReader lineaReader = new PushbackReader(new StringReader(linea), 32)) {
                    Parser parser = new Parser(new Lexer(lineaReader));
                    Start arbol = parser.parse();
                    arbol.apply(recolector);
                }
            }
        }

        return recolector;
    }

    public static List<LineaLyric> cargarLrc(String rutaLrc) throws Exception {
        return analizarArchivo(rutaLrc).getLineasCancion();
    }

    public static Map<String, String> cargarMetadatos(String rutaLrc) throws Exception {
        return analizarArchivo(rutaLrc).getMetadatos();
    }

    public static void main(String[] args) {
        try {
            RutasEntrada rutas = resolverRutas(args);
            String rutaMp3 = rutas.mp3;
            String rutaLrc = rutas.lrc;

            if (!Files.exists(Paths.get(rutaMp3))) {
                throw new IllegalArgumentException("No existe el MP3: " + rutaMp3);
            }

            if (!Files.exists(Paths.get(rutaLrc))) {
                throw new IllegalArgumentException("No existe el LRC: " + rutaLrc);
            }

            visitador recolector = analizarArchivo(rutaLrc);
            List<LineaLyric> lineas = recolector.getLineasCancion();
            Map<String, String> metadatos = recolector.getMetadatos();

            if (!metadatos.isEmpty()) {
                System.out.println("Metadatos detectados: " + metadatos);
            }

            KaraokeUI ui = new KaraokeUI();
            ui.mostrar(lineas, metadatos);

            Reproductor reproductor = new Reproductor();
            reproductor.AbrirFichero(rutaMp3);
            reproductor.Play();
            ui.iniciarSincronizacion();
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    private static RutasEntrada resolverRutas(String[] args) {
        String mp3Defecto = resolverRuta(valorDefecto(DEFAULT_MP3));
        String lrcDefecto = resolverRuta(valorDefecto(DEFAULT_LRC));

        if (args == null || args.length == 0) {
            return new RutasEntrada(mp3Defecto, lrcDefecto);
        }

        if (args.length >= 2) {
            String mp3Directo = limpiar(args[0]);
            String lrcDirecto = limpiar(args[1]);
            if (esRutaExistente(mp3Directo) && esRutaExistente(lrcDirecto)) {
                return new RutasEntrada(mp3Directo, lrcDirecto);
            }

            for (int corte = 1; corte < args.length; corte++) {
                String mp3 = unir(args, 0, corte);
                String lrc = unir(args, corte, args.length);
                if (esRutaExistente(mp3) && esRutaExistente(lrc)) {
                    return new RutasEntrada(mp3, lrc);
                }
            }
        }

        if (args.length == 1) {
            String unica = limpiar(args[0]);
            if (esRutaExistente(unica)) {
                return new RutasEntrada(unica, lrcDefecto);
            }
        }

        return new RutasEntrada(mp3Defecto, lrcDefecto);
    }

    private static String resolverRuta(String valorDefecto) {
        Path base = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path candidato = base.resolve(valorDefecto).normalize();
        if (Files.exists(candidato)) {
            return candidato.toString();
        }

        Path actual = base;
        while (actual != null) {
            candidato = actual.resolve(valorDefecto).normalize();
            if (Files.exists(candidato)) {
                return candidato.toString();
            }
            actual = actual.getParent();
        }

        return Paths.get(valorDefecto).toAbsolutePath().normalize().toString();
    }

    private static String valorDefecto(String ruta) {
        return ruta == null ? "" : ruta;
    }

    private static boolean esRutaExistente(String ruta) {
        return ruta != null && !ruta.trim().isEmpty() && Files.exists(Paths.get(ruta.trim()));
    }

    private static String limpiar(String ruta) {
        return ruta == null ? "" : ruta.trim();
    }

    private static String unir(String[] args, int inicio, int fin) {
        StringBuilder sb = new StringBuilder();
        for (int i = inicio; i < fin; i++) {
            if (args[i] == null || args[i].trim().isEmpty()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(args[i].trim());
        }
        return sb.toString();
    }
}

