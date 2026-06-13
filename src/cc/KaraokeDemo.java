package cc;

import cc.lexer.Lexer;
import cc.node.Start;
import cc.parser.Parser;

import java.io.*;
import java.util.ArrayList;

public class KaraokeDemo {

    static File[] mp3s = new File("C:\\Users\\illan\\IdeaProjects\\Tarea2-Fundamentos\\src\\mp3").listFiles();
    static File[] lyrics = new File("C:\\Users\\illan\\IdeaProjects\\Tarea2-Fundamentos\\src\\lyrics").listFiles();
    static ArrayList<Cancion> repositorio = Repositorio();

    private static ArrayList<Cancion> Repositorio () {
        ArrayList<Cancion> repo = new ArrayList<>();
        try {
            if (mp3s == null || mp3s.length == 0 || lyrics == null) return repo;

            for (int i = 0; i < mp3s.length; i++) {
                repo.add(new Cancion(
                        analizarArchivo(lyrics[i].getAbsolutePath()),
                        mp3s[i]
                ));
            }
            return repo;
        } catch (Exception e){
            System.out.println("Error inicializando repositorio: " + e.getMessage());
        }
        return null;
    }

    private static API analizarArchivo(String rutaLrc) throws Exception {
        API recolector = new API();

        try (BufferedReader reader = new BufferedReader(new FileReader(rutaLrc))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                // Forzamos el trim a la línea completa
                linea = linea.trim();

                if (linea.isEmpty()) {
                    continue;
                }

                // Limpieza del Byte Order Mark
                if (linea.startsWith("\uFEFF")) {
                    linea = linea.substring(1);
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

    public static void main(String[] args) {
        try {
            // Ejecución directa de prueba utilizando los archivos por defecto
            KaraokeUI ui = new KaraokeUI(repositorio);

            System.out.println(repositorio.size());
            repositorio.forEach(c -> {
                System.out.println(c.toString());
            });


        } catch (Exception ex) {
            System.out.println("Error en la ejecución del Karaoke: " + ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }
}