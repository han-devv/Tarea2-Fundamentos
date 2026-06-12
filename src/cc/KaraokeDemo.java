package cc;

import cc.lexer.Lexer;
import cc.node.Start;
import cc.parser.Parser;

import java.io.*;
import java.util.ArrayList;

public class KaraokeDemo {

    private static final String DEFAULT_MP3 = "mp3/metallica.mp3";
    private static final String DEFAULT_LRC = "lyrics/metallica - nothing else matters.lrc";

    File[] mp3s = new File("mp3").listFiles();
    File[] lyrics = new File("lyrics").listFiles();
    ArrayList<Cancion> repositorio = Repositorio();

    private ArrayList<Cancion> Repositorio () {
        ArrayList<Cancion> repositorio = new ArrayList<>();
        try {
            if (mp3s.length == 0) return repositorio;

            for (int i = 0; i < mp3s.length; i++) {
                repositorio.add(new Cancion(
                        analizarArchivo(lyrics[i].getAbsolutePath()),
                        mp3s[i]
                ));
            }
            return repositorio;
        }catch (Exception e){
            System.out.println("!!!!:" + e.getMessage());
        }
        return null;
    }

    private static API analizarArchivo(String rutaLrc) throws Exception {
        API recolector = new API();

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

    //todo sacar las weas de args[]
    public static void main(String[] args) {
        try {
            //todo Repositorio a ui
            KaraokeUIzzzzzzzz ui = new KaraokeUIzzzzzzzz();
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
}

