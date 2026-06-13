package cc;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;

public class KaraokeUI extends JFrame {
    private JPanel panel1;
    private JButton previousButton;
    private JButton btnPlayPause;
    private JButton nextButton;
    private JLabel mainLyrics;
    private JSlider sliderVolumen;
    private JLabel previousLyrics;
    private JLabel nextLyrics;
    private JLabel titulo;
    private JLabel artista;
    private JLabel album;
    private JLabel duracion;
    private boolean isPaused = true;
    private Reproductor reproductor = new Reproductor();

    // -- variables --
    ArrayList<Cancion> repositorio;
    int index = 0;
    Timer timer;
    ArrayList<LineaLyric> lines;
    Cancion actual;

    CountDownLatch latch = new CountDownLatch(1);

    Thread hiloA = new Thread(() -> {
        syncLines(lines); {}
        try {
            latch.await(); // Espera hasta que el contador llegue a 0
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Hilo A interrumpido.");
        }
    });

    Thread hiloB = new Thread(() -> {
        try {
            reproductor = new Reproductor();
            reproductor.AbrirFichero(actual.getRutamp3());
            reproductor.Play();

            latch.countDown(); // Libera a Hilo A
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.out.println("Hilo B interrumpido.");
        }
    });

    public void setRepositorio(ArrayList<Cancion> repositorio) {
        this.repositorio = repositorio;
    }

    public void nextSong(){
        index++;
        if(index >= repositorio.size()) index = 0;
        try{
            reproductor.Stop();
            reproducir();
        }catch(Exception e){}
    }

    public void prevSong(){
        index--;
        if(index < 0) index = repositorio.size() -1;
        try{
            reproductor.Stop();
            reproducir();
        }catch (Exception e){}
    }

    public void reproducir(){
        Cancion actual = repositorio.get(index);
        lines = actual.getLines();

        CountDownLatch latch = new CountDownLatch(1);

        Map<String,String> metadatos = actual.getMetadatos();
        titulo.setText(metadatos.get("ti"));
        artista.setText(metadatos.get("ar"));
        album.setText(metadatos.get("al"));

        try {
            hiloA.start();
            hiloB.start();
        }catch (Exception e){
            JOptionPane.showMessageDialog(null,e);
        }
    }

    public void syncLines(ArrayList<LineaLyric> lines){
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new java.util.Timer("karaoke-ui-timer", true);

        for (int i = 0; i < lines.size(); i++) {
            final int indice = i;
            LineaLyric linea = lines.get(i);
            timer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        if (indice < 0 || indice >= lines.size()) {
                            return;
                        }
                        mainLyrics.setText(linea.getTexto());
                    });
                }
            }, Math.max(0L, linea.getTiempoMs()));
        }
    }

    public KaraokeUI(ArrayList<Cancion> repositorio) {
        this.repositorio = repositorio;
        actual = repositorio.get(index);
        lines = repositorio.get(index).getLines();

        setTitle("Karaoke");
        setContentPane(panel1);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        
        //Boton Play/Pause
        btnPlayPause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (isPaused) {
                        isPaused = false;
                        reproductor.Continuar();
                        btnPlayPause.setText("❚❚");

                        hiloA.run();
                        hiloB.run();
                    } else {
                        isPaused = true;
                        reproductor.Pausa();
                        btnPlayPause.setText("▶");

                        hiloA.interrupt();
                        hiloB.interrupt();
                    }
                } catch (Exception ex) {
                    System.out.println("No se pudo cambiar el estado de reproducción.");
                    ex.printStackTrace();
                }
            }
        });


        //Volumen
        sliderVolumen.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e){
                int volumenActual = sliderVolumen.getValue();

            }
        });

        //Boton Next
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                nextSong();
            }
        });

        //Boton Previous
        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                prevSong();
            }
        });
    }

}