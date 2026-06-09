import javazoom.jlgui.basicplayer.BasicPlayer;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class Reproductor {

    private BasicPlayer player;

    Reproductor(){
        player = new BasicPlayer();
    }

    public void Play() throws Exception {
        player.play();
    }

    public void AbrirFichero(String ruta) throws Exception {
        player.open(new File(ruta));
    }

    public void Pausa() throws Exception {
        player.pause();
    }

    public void Continuar() throws Exception {
        player.resume();
    }

    public void Stop() throws Exception {
        player.stop();
    }

    public static void main(String args[]){
        try {
            Reproductor mi_reproductor = new Reproductor();
            mi_reproductor.AbrirFichero("C:\\java_mp3\\metallica.mp3");
            mi_reproductor.Play();
            String st = "Se cumplieron 5 segundos";
            mi_reproductor.new Reminder(5000, st);
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }


    public class Reminder {
        Timer timer;
        String texto;

        public Reminder(long seconds, String texto) {
            this.texto = texto;
            timer = new Timer();
            timer.schedule(new RemindTask(), seconds); //5000 milisegundos = 5 segundos
        }

        class RemindTask extends TimerTask {
            public void run() { //se ejecuta solo cuando se cumple el tiempo
                System.out.println(texto);
            }
        }
    }

}
