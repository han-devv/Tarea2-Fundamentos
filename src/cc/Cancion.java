package cc;

import java.io.File;
import java.sql.Time;
import java.util.List;
import java.util.Map;

public class Cancion {

    API api;
    File rutamp3;

    public Cancion(API api, File mp3){
        this.api = api;
        this.rutamp3 = mp3;
    }

    public File getRutamp3(){
        return rutamp3;
    }

    public API getApi(){
        return api;
    }

    public Map<String, String> getMetadatos() {
        return api.getMetadatos();
    }

    public List<LineaLyric> getLines(){
        return api.getLineasCancion();
    }


}
