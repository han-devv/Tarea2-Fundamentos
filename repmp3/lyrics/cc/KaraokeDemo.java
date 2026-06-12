package cc;

import cc.lexer.Lexer;
import cc.node.Start;
import cc.parser.Parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

public class KaraokeDemo {

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
        if (args.length < 2) {
            System.out.println("Uso: KaraokeDemo <archivo.mp3> <archivo.lrc>");
            return;
        }

        try {
            String rutaMp3 = args[0];
            String rutaLrc = args[1];

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
        }
    }
}

