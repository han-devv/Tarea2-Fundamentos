package cc;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KaraokeUI {

    private final JFrame frame;
    private final JLabel tituloValor;
    private final JLabel artistaValor;
    private final JLabel albumValor;
    private final JLabel tiempoValor;
    private final JLabel lineaActualValor;
    private final JLabel siguienteValor;
    private final DefaultListModel<String> listaModelo;
    private final JList<String> listaLineas;
    private java.util.Timer timer;
    private List<LineaLyric> lineas = Collections.emptyList();

    public KaraokeUI() {
        frame = new JFrame("Karaoke");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(900, 560));
        frame.setLocationRelativeTo(null);

        JPanel principal = new JPanel(new BorderLayout(12, 12));
        principal.setBorder(new EmptyBorder(16, 16, 16, 16));
        principal.setBackground(new Color(18, 18, 18));

        JPanel encabezado = new JPanel(new GridLayout(4, 2, 8, 8));
        encabezado.setOpaque(false);
        encabezado.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)), "Metadatos"));

        tituloValor = crearValor();
        artistaValor = crearValor();
        albumValor = crearValor();
        tiempoValor = crearValor();

        encabezado.add(crearEtiqueta("Título"));
        encabezado.add(tituloValor);
        encabezado.add(crearEtiqueta("Artista"));
        encabezado.add(artistaValor);
        encabezado.add(crearEtiqueta("Álbum"));
        encabezado.add(albumValor);
        encabezado.add(crearEtiqueta("Tiempo activo"));
        encabezado.add(tiempoValor);

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.setOpaque(false);

        JPanel estado = new JPanel(new GridLayout(2, 1, 8, 8));
        estado.setOpaque(false);

        lineaActualValor = crearLineaGrande();
        siguienteValor = crearLineaMediana();
        estado.add(conPanelTitulito("Línea actual", lineaActualValor));
        estado.add(conPanelTitulito("Siguiente línea", siguienteValor));

        listaModelo = new DefaultListModel<>();
        listaLineas = new JList<>(listaModelo);
        listaLineas.setBackground(new Color(30, 30, 30));
        listaLineas.setForeground(Color.WHITE);
        listaLineas.setSelectionBackground(new Color(80, 140, 255));
        listaLineas.setSelectionForeground(Color.WHITE);
        listaLineas.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));

        JScrollPane scroll = new JScrollPane(listaLineas);
        scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)), "Letra completa"));
        scroll.setPreferredSize(new Dimension(420, 300));

        centro.add(estado, BorderLayout.CENTER);
        centro.add(scroll, BorderLayout.EAST);

        principal.add(encabezado, BorderLayout.NORTH);
        principal.add(centro, BorderLayout.CENTER);

        frame.setContentPane(principal);
    }

    public void mostrar(List<LineaLyric> lineas, Map<String, String> metadatos) {
        ArrayList<LineaLyric> ordenadas = new ArrayList<>(lineas == null ? Collections.emptyList() : lineas);
        Collections.sort(ordenadas);
        this.lineas = ordenadas;

        SwingUtilities.invokeLater(() -> {
            tituloValor.setText(valorMeta(metadatos, "ti", "Sin título"));
            artistaValor.setText(valorMeta(metadatos, "ar", "Desconocido"));
            albumValor.setText(valorMeta(metadatos, "al", "No disponible"));
            tiempoValor.setText("00:00.00");

            listaModelo.clear();
            for (LineaLyric linea : this.lineas) {
                listaModelo.addElement(linea.toString());
            }

            if (this.lineas.isEmpty()) {
                lineaActualValor.setText("No se encontraron líneas de karaoke.");
                siguienteValor.setText("-");
            } else {
                actualizarVistaLinea(0);
            }

            frame.setVisible(true);
        });
    }

    public void iniciarSincronizacion() {
        cancelarSincronizacion();
        timer = new java.util.Timer("karaoke-ui-timer", true);

        for (int i = 0; i < lineas.size(); i++) {
            final int indice = i;
            LineaLyric linea = lineas.get(i);
            timer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> actualizarVistaLinea(indice));
                }
            }, Math.max(0L, linea.getTiempoMs()));
        }
    }

    public void cancelarSincronizacion() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void actualizarVistaLinea(int indice) {
        if (indice < 0 || indice >= lineas.size()) {
            return;
        }

        LineaLyric actual = lineas.get(indice);
        LineaLyric siguiente = indice + 1 < lineas.size() ? lineas.get(indice + 1) : null;

        tiempoValor.setText(actual.getTiempoFormateado());
        lineaActualValor.setText(actual.getTexto().isEmpty() ? "[línea vacía]" : actual.getTexto());
        siguienteValor.setText(siguiente == null ? "Fin de la canción" : siguiente.getTexto().isEmpty() ? "[línea vacía]" : siguiente.getTexto());
        listaLineas.setSelectedIndex(indice);
        listaLineas.ensureIndexIsVisible(indice);
    }

    private static JLabel crearEtiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(new Color(200, 200, 200));
        etiqueta.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        return etiqueta;
    }

    private static JLabel crearValor() {
        JLabel valor = new JLabel("-");
        valor.setForeground(Color.WHITE);
        valor.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        return valor;
    }

    private static JLabel crearLineaGrande() {
        JLabel etiqueta = new JLabel("-", SwingConstants.CENTER);
        etiqueta.setOpaque(true);
        etiqueta.setBackground(new Color(28, 28, 28));
        etiqueta.setForeground(new Color(240, 240, 240));
        etiqueta.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        etiqueta.setBorder(new EmptyBorder(18, 18, 18, 18));
        return etiqueta;
    }

    private static JLabel crearLineaMediana() {
        JLabel etiqueta = new JLabel("-", SwingConstants.CENTER);
        etiqueta.setOpaque(true);
        etiqueta.setBackground(new Color(35, 35, 35));
        etiqueta.setForeground(new Color(210, 210, 210));
        etiqueta.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        etiqueta.setBorder(new EmptyBorder(14, 14, 14, 14));
        return etiqueta;
    }

    private static JPanel conPanelTitulito(String titulo, JLabel contenido) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel etiqueta = crearEtiqueta(titulo);
        panel.add(etiqueta, BorderLayout.NORTH);
        panel.add(contenido, BorderLayout.CENTER);
        return panel;
    }

    private static String valorMeta(Map<String, String> metadatos, String clave, String defecto) {
        if (metadatos == null) {
            return defecto;
        }

        String valor = metadatos.get(clave);
        return valor == null || valor.trim().isEmpty() ? defecto : valor.trim();
    }
}

