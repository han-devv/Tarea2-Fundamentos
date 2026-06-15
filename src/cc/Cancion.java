package cc;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * Modelo de datos que representa una cancion entera.
 * Relaciona el archivo MP3 físico con los datos ya parseados del archivo LRC (Objeto API).
 */
public class Cancion {

    API api;
    File rutamp3;

    public Cancion(API api, File mp3){
        this.api = api; // Contiene los datos visitados por el parser (Letras y metadatos)
        this.rutamp3 = mp3; // El archivo de audio fisico
    }

    public String getRutamp3(){
        return rutamp3.getAbsolutePath();
    }

    public API getApi(){
        return api;
    }

    public Map<String, String> getMetadatos() {
        return api.getMetadatos();
    }

    public ArrayList<LineaLyric> getLines(){
        return (ArrayList<LineaLyric>) api.getLineasCancion();
    }


}
