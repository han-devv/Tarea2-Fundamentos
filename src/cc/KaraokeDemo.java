package cc;

import cc.lexer.Lexer;
import cc.node.Start;
import cc.parser.Parser;

import java.io.*;
import java.util.ArrayList;
/**
 * Clase principal (Main) encargada de inicializar el entorno del programa.
 * Su responsabilidad es leer los directorios, parsear los archivos .lrc utilizando SableCC
 * y cargar la vista (KaraokeUI).
 */
public class KaraokeDemo {
    // Rutas relativas sugeridas en la estructura del enunciado
    static File[] mp3s = new File("./src/mp3").listFiles();
    static File[] lyrics = new File("./src/lyrics").listFiles();
    static ArrayList<Cancion> repositorio = Repositorio();

    /**
     * Construye un ArrayList con las canciones que hacen match entre MP3 y archivos .lrc.
     */
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

    /**
     * Este metodo configura y ejecuta el analisis sintáctico de SableCC (Etapa 1 y 2).
     * @param rutaLrc La ruta absoluta del archivo .lrc
     * @return El objeto API (visitador) ya cargado con la info en memoria.
     */
    private static API analizarArchivo(String rutaLrc) throws Exception {
        API recolector = new API();

        try (BufferedReader reader = new BufferedReader(new FileReader(rutaLrc))) {
            String linea;
            // Leemos el archivo LRC linea por linea
            while ((linea = reader.readLine()) != null) {
                // Forzamos el trim a la línea completa
                linea = linea.trim();

                if (linea.isEmpty()) {
                    continue; // Ignorar lineas en blanco
                }

                // Limpieza del Byte Order Mark
                if (linea.startsWith("\uFEFF")) {
                    linea = linea.substring(1);
                }

                // Configuracion y ejecucion del Lexer y Parser generados por SableCC
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
            // Se inicializa la UI pasando el repositorio en memoria
            KaraokeUI ui = new KaraokeUI(repositorio);

            // Output por consola para validación de cuántas canciones cargaron
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