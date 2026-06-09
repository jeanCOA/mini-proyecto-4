package view;

import controller.InicioController;
import javax.swing.*;
import java.awt.*;
import java.util.List;

// pantalla de inicio con opciones para nuevo duelo, cargar partida y estadisticas
public class VentanaInicio extends JFrame {

    private JTextField campoNombre1;
    private JTextField campoNombre2;
    private InicioController controller;

    public VentanaInicio() {
        controller = new InicioController(this);
        construirUI();
    }

    private void construirUI() {
        setTitle("Yu-Gi-Oh! — Duelo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(15, 15, 40));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel("  DUELO DE YU-GI-OH!  ", SwingConstants.CENTER);
        titulo.setFont(new Font("Serif", Font.BOLD, 26));
        titulo.setForeground(new Color(255, 215, 0));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titulo, gbc);

        JLabel subtitulo = new JLabel("\"Confia en el corazon de las cartas\"", SwingConstants.CENTER);
        subtitulo.setFont(new Font("Serif", Font.ITALIC, 13));
        subtitulo.setForeground(new Color(180, 180, 220));
        gbc.gridy = 1;
        panel.add(subtitulo, gbc);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 215, 0));
        gbc.gridy = 2; gbc.insets = new Insets(4, 0, 16, 0);
        panel.add(sep, gbc);
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridwidth = 1; gbc.gridy = 3; gbc.gridx = 0;
        JLabel lbl1 = new JLabel("Jugador 1:");
        lbl1.setForeground(Color.WHITE);
        lbl1.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(lbl1, gbc);

        campoNombre1 = new JTextField("Yugi", 16);
        estilizarCampo(campoNombre1);
        gbc.gridx = 1;
        panel.add(campoNombre1, gbc);

        gbc.gridy = 4; gbc.gridx = 0;
        JLabel lbl2 = new JLabel("Jugador 2:");
        lbl2.setForeground(Color.WHITE);
        lbl2.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(lbl2, gbc);

        campoNombre2 = new JTextField("Kaiba", 16);
        estilizarCampo(campoNombre2);
        gbc.gridx = 1;
        panel.add(campoNombre2, gbc);

        // boton principal para nuevo duelo
        JButton btnIniciar = crearBoton("INICIAR NUEVO DUELO", new Color(180, 0, 0));
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 4, 8);
        panel.add(btnIniciar, gbc);
        btnIniciar.addActionListener(e -> iniciarDuelo());

        // boton para cargar partida guardada
        JButton btnCargar = crearBoton("CARGAR PARTIDA GUARDADA", new Color(0, 80, 160));
        gbc.gridy = 6; gbc.insets = new Insets(4, 8, 4, 8);
        panel.add(btnCargar, gbc);
        btnCargar.addActionListener(e -> mostrarDialogoCargar());

        // boton para ver estadisticas historicas
        JButton btnStats = crearBoton("VER ESTADISTICAS", new Color(50, 100, 50));
        gbc.gridy = 7;
        panel.add(btnStats, gbc);
        btnStats.addActionListener(e -> mostrarEstadisticas());

        campoNombre1.addActionListener(e -> iniciarDuelo());
        campoNombre2.addActionListener(e -> iniciarDuelo());

        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
    }

    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void estilizarCampo(JTextField campo) {
        campo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        campo.setBackground(new Color(30, 30, 60));
        campo.setForeground(Color.WHITE);
        campo.setCaretColor(Color.WHITE);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
    }

    private void iniciarDuelo() {
        String n1 = campoNombre1.getText().trim();
        String n2 = campoNombre2.getText().trim();
        controller.iniciarDuelo(n1, n2);
    }

    // muestra dialogo para elegir un guardado y cargarlo
    private void mostrarDialogoCargar() {
        List<String> guardados = controller.getGuardados();
        if (guardados.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No hay partidas guardadas.", "Sin guardados", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] opciones = guardados.toArray(new String[0]);
        String elegido = (String) JOptionPane.showInputDialog(this,
            "Elige una partida para cargar:", "Cargar partida",
            JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
        if (elegido != null) {
            controller.cargarPartida(elegido);
        }
    }

    private void mostrarEstadisticas() {
        String stats = controller.getEstadisticas();
        JTextArea area = new JTextArea(stats);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this,
            new JScrollPane(area), "Estadisticas historicas", JOptionPane.PLAIN_MESSAGE);
    }

    // muestra un mensaje de error en la pantalla
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
