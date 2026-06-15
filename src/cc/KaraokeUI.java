package cc;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

/**
 * Interfaz grafica principal del Karaoke.
 * Gestiona la reproducción visual, controles de usuario y el hilo de sincronización
 * entre la música (MP3) y las letras (LRC).
 */
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

    private boolean isPaused = false;
    private Reproductor reproductor = new Reproductor();

    // -- Variables de estado del reproductor --
    private ArrayList<Cancion> repositorio;
    private int index = 0;
    private Cancion actual;

    // Ajuste de milisegundos para sincronizar retrasos del audio respecto a la letra
    private volatile long latenciaGlobal = -1100;

    // Control del hilo de fondo para no bloquear la interfaz grafica
    private Thread hiloReproduccion;

    // Variables para calcular la sincronizacian
    private long startTime;
    private long accumulatedPauseTime;
    private long pauseBeginTime;

    public KaraokeUI(ArrayList<Cancion> repositorio) {
        this.repositorio = repositorio;

        // Configuracion basica de la ventana
        setTitle("Karaoke");
        setContentPane(panel1);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        // Boton Play/Pause: Gestiona la reanudacion y la lagica de tiempos acumulados en pausa
        btnPlayPause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (actual == null) return; // Por si no hay canciones

                try {
                    if (isPaused) {
                        isPaused = false;
                        // Acumulamos el tiempo que duro la pausa para no desfasar la letra
                        accumulatedPauseTime += (System.currentTimeMillis() - pauseBeginTime);
                        reproductor.Continuar();
                        btnPlayPause.setText("❚❚");
                    } else {
                        isPaused = true;
                        // Registramos el instante exacto en el que pausamos
                        pauseBeginTime = System.currentTimeMillis();
                        reproductor.Pausa();
                        btnPlayPause.setText("▶");
                    }
                } catch (Exception ex) {
                    System.out.println("No se pudo cambiar el estado de reproducción.");
                }
            }
        });

        // Boton Next
        nextButton.addActionListener(e -> nextSong());

        // Boton Previous
        previousButton.addActionListener(e -> prevSong());

        // Volumen: Se ajusta segun el slider
        sliderVolumen.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // Solo cambiar si el usuario está moviendo activamente la barra
                int volumenActual = sliderVolumen.getValue();
                if (reproductor != null) {
                    reproductor.setVolumen(volumenActual);
                }
            }
        });

        // Inicia automaticamente la primera cancion si existe repositorio
        if (this.repositorio != null && !this.repositorio.isEmpty()) {
            this.index = 0; // Asegura que partimos en la canción 1
            reproducir();   // Inicia la carga y la reproducción
        }
    }

    /**
     * Avanza a la siguiente cancion en la lista, volviendo al inicio si llega al final.
     */
    public void nextSong() {
        if (repositorio == null || repositorio.isEmpty()) return;
        index++;
        if (index >= repositorio.size()) index = 0;
        reproducir();
    }

    /**
     * Retrocede a la cancion anterior.
     */
    public void prevSong() {
        if (repositorio == null || repositorio.isEmpty()) return;
        index--;
        if (index < 0) index = repositorio.size() - 1;
        reproducir();
    }

    /**
     * Logica principal de reproducción y sincronización en tiempo real.
     */
    public void reproducir() {
        // 1. Detener hilo anterior y la cancion actual en curso
        if (hiloReproduccion != null && hiloReproduccion.isAlive()) {
            hiloReproduccion.interrupt();
        }
        try {
            reproductor.Stop();
        } catch (Exception ignored) {}

        // 2. Cargar los metadatos de la nueva canción a la UI
        actual = repositorio.get(index);
        Map<String, String> metadatos = actual.getMetadatos();
        titulo.setText(metadatos.getOrDefault("ti", "Desconocido"));
        artista.setText(metadatos.getOrDefault("ar", "Desconocido"));
        album.setText(metadatos.getOrDefault("al", "Desconocido"));

        isPaused = false;
        btnPlayPause.setText("❚❚");

        // --- PREPARAR LAS LETRAS PARA LA INTRO MUSICAL ---
        ArrayList<LineaLyric> lines = actual.getLines();
        if (lines != null && !lines.isEmpty()) {
            previousLyrics.setText("");
            mainLyrics.setText("♪♪♪♪"); // Indicador visual
            nextLyrics.setText(lines.get(0).getTexto()); // Previsualizar la primera línea
        } else {
            previousLyrics.setText("");
            mainLyrics.setText("Letra no disponible");
            nextLyrics.setText("");
        }
        // -----------------------------------------------------------

        // 3. Crear el hilo independiente de sincronizacion de letras
        hiloReproduccion = new Thread(() -> {
            try {
                // Leer el offset del archivo si existe
                long offsetLrc = 0;
                try {
                    if (metadatos.containsKey("offset")) {
                        offsetLrc = Long.parseLong(metadatos.get("offset").trim());
                    }
                } catch (Exception ignored) {}

                // Reproducir audio
                reproductor.AbrirFichero(actual.getRutamp3());
                reproductor.Play();

                // Preparar relojes
                startTime = System.currentTimeMillis();
                accumulatedPauseTime = 0;
                long duracionTotalMs = calcularDuracionTotal(actual);
                long lastSecond = -1;

                // Bucle de sincronizacion: Recorre cada línea de letra cargada
                for (int i = 0; i < lines.size(); i++) {
                    LineaLyric linea = lines.get(i);

                    // Preparar los textos en "cascada" (Anterior, Actual y Siguiente)
                    final String txtMain = linea.getTexto();
                    final String txtPrev = (i > 0) ? lines.get(i - 1).getTexto() : "";
                    final String txtNext = (i < lines.size() - 1) ? lines.get(i + 1).getTexto() : "";

                    while (true) {
                        // Verifica si el hilo fue interrumpido
                        if (Thread.currentThread().isInterrupted()) return;
                        if (isPaused) { Thread.sleep(50); continue; }

                        // La latenciaGlobal controlable con el teclado se suma
                        long ajusteTotal = offsetLrc + latenciaGlobal;
                        long currentAudioTime = (System.currentTimeMillis() - startTime - accumulatedPauseTime) + ajusteTotal;

                        // Actualizar cronometro cada segundo
                        long currentSec = Math.max(0, currentAudioTime / 1000);
                        if (currentSec != lastSecond) {
                            lastSecond = currentSec;
                            final String textoTiempo = formatearTiempo(currentSec * 1000) + " / " + formatearTiempo(duracionTotalMs);
                            SwingUtilities.invokeLater(() -> duracion.setText(textoTiempo));
                        }

                        // --- ACTUALIZACION DE LETRAS ---
                        // Si el tiempo de reproducción actual alcanza el timestamp de la letra
                        if (currentAudioTime >= linea.getTiempoMs()) {
                            SwingUtilities.invokeLater(() -> {
                                mainLyrics.setText(txtMain);
                                previousLyrics.setText(txtPrev);
                                nextLyrics.setText(txtNext);
                            });
                            break;
                        }

                        Thread.sleep(10);
                    }
                }

                // Ciclo final para cuando se acaba la letra pero la musica sigue
                while (!Thread.currentThread().isInterrupted()) {
                    if (isPaused) { Thread.sleep(50); continue; }

                    long ajusteTotal = offsetLrc + latenciaGlobal;
                    long currentAudioTime = (System.currentTimeMillis() - startTime - accumulatedPauseTime) + ajusteTotal;
                    if (currentAudioTime > duracionTotalMs) {
                        // Limpiar las letras al terminar
                        SwingUtilities.invokeLater(() -> {
                            previousLyrics.setText("");
                            mainLyrics.setText("Fin de la canción");
                            nextLyrics.setText("");
                        });
                        break;
                    }
                    long currentSec = Math.max(0, currentAudioTime / 1000);
                    if (currentSec != lastSecond) {
                        lastSecond = currentSec;
                        final String textoTiempo = formatearTiempo(currentSec * 1000) + " / " + formatearTiempo(duracionTotalMs);
                        SwingUtilities.invokeLater(() -> duracion.setText(textoTiempo));
                    }
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
            } catch (Exception e) {
                System.out.println("Error en reproducción: " + e.getMessage());
            }
        });

        // 4. Iniciar el hilo
        hiloReproduccion.start();
    }

    /**
     * Metodo auxiliar que convierte milisegundos a formato estándar MM:SS
     */
    private String formatearTiempo(long millis) {
        long segundosTotales = millis / 1000;
        long minutos = segundosTotales / 60;
        long segundos = segundosTotales % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    /**
     * Metodo auxiliar para calcular la duración total de la canción basado en los metadatos
     * o asumiendo un margen extra al final de la Ultima letra cantada.
     */
    private long calcularDuracionTotal(Cancion cancion) {
        Map<String, String> meta = cancion.getMetadatos();
        // Intentar leer el tag [length: 06:28] del archivo .lrc
        if (meta.containsKey("length")) {
            try {
                String lengthStr = meta.get("length").trim();
                if (lengthStr.contains(":")) {
                    String[] partes = lengthStr.split(":");
                    return (Long.parseLong(partes[0]) * 60 + Long.parseLong(partes[1])) * 1000;
                }
            } catch (Exception ignored) {}
        }
        // Si no hay tag de duracion, tomamos la ultima linea cantada + 10 segundos extra de final
        ArrayList<LineaLyric> lineas = cancion.getLines();
        if (lineas != null && !lineas.isEmpty()) {
            return lineas.get(lineas.size() - 1).getTiempoMs() + 10000;
        }
        return 0;
    }
}