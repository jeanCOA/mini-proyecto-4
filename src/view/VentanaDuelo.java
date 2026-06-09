package view;

import controller.DuelController;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import model.*;

// ventana principal del duelo, igual que el MP3 pero con botones de Deshacer y Guardar
public class VentanaDuelo extends JFrame implements IDuelView {

    private final DuelController controller;

    private JLabel lblTurno;
    private JTextArea areaLog;

    private JLabel lblNombreJ1, lblLpJ1, lblManoJ1, lblMazoJ1, lblTrampasJ1;
    private JPanel panelCampoJ1;

    private JLabel lblNombreJ2, lblLpJ2, lblManoJ2, lblMazoJ2, lblTrampasJ2;
    private JPanel panelCampoJ2;

    private JButton btnJugarCarta;
    private JButton btnAtacar;
    private JButton btnActivarTrampa;
    private JButton btnCambiarPosicion;
    private JButton btnTerminarTurno;
    // nuevos botones del MP4
    private JButton btnDeshacer;
    private JButton btnGuardar;

    private static final Color BG_DARK      = new Color(10, 10, 30);
    private static final Color BG_FIELD     = new Color(0, 60, 30);
    private static final Color COLOR_GOLD   = new Color(255, 215, 0);
    private static final Color COLOR_RED    = new Color(200, 20, 20);
    private static final Color COLOR_BLUE   = new Color(20, 100, 200);
    private static final Color COLOR_GREEN  = new Color(30, 160, 30);
    private static final Color COLOR_PURPLE = new Color(120, 30, 160);
    private static final Color COLOR_GRAY   = new Color(80, 80, 100);
    private static final Color FG_WHITE     = Color.WHITE;

    public VentanaDuelo(DuelController controller) {
        this.controller = controller;
        construirUI();
    }

    private void construirUI() {
        setTitle("Yu-Gi-Oh! — Duelo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(6, 6));
        getContentPane().setBackground(BG_DARK);

        add(construirPanelSuperior(), BorderLayout.NORTH);
        add(construirPanelCampo(),    BorderLayout.CENTER);
        add(construirPanelAcciones(), BorderLayout.EAST);
        add(construirPanelLog(),      BorderLayout.SOUTH);

        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(null);
    }

    private JPanel construirPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(20, 20, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        lblTurno = new JLabel("TURNO 0", SwingConstants.CENTER);
        lblTurno.setFont(new Font("Serif", Font.BOLD, 18));
        lblTurno.setForeground(COLOR_GOLD);
        panel.add(lblTurno, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirPanelCampo() {
        JPanel panelTotal = new JPanel(new GridLayout(2, 1, 4, 4));
        panelTotal.setBackground(BG_DARK);
        panelTotal.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        // zona del oponente (J2 arriba)
        JPanel zonaJ2 = new JPanel(new BorderLayout(4, 4));
        zonaJ2.setBackground(new Color(20, 20, 60));
        zonaJ2.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COLOR_BLUE), " Oponente ",
            TitledBorder.LEFT, TitledBorder.TOP, null, COLOR_BLUE));

        JPanel infoJ2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        infoJ2.setBackground(new Color(20, 20, 60));
        lblNombreJ2 = infoLabel("", Font.BOLD, 14, COLOR_BLUE);
        lblLpJ2     = infoLabel("LP: 8000", Font.BOLD, 13, new Color(100, 200, 100));
        lblManoJ2   = infoLabel("Mano: 5", Font.PLAIN, 12, FG_WHITE);
        lblMazoJ2   = infoLabel("Mazo: 20", Font.PLAIN, 12, FG_WHITE);
        lblTrampasJ2= infoLabel("Trampas: 0", Font.PLAIN, 12, new Color(180, 100, 220));
        infoJ2.add(lblNombreJ2); infoJ2.add(lblLpJ2);
        infoJ2.add(lblManoJ2);   infoJ2.add(lblMazoJ2); infoJ2.add(lblTrampasJ2);

        panelCampoJ2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        panelCampoJ2.setBackground(BG_FIELD);
        panelCampoJ2.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BG_FIELD), "Campo",
            TitledBorder.LEFT, TitledBorder.TOP, null, Color.LIGHT_GRAY));

        zonaJ2.add(infoJ2, BorderLayout.NORTH);
        zonaJ2.add(new JScrollPane(panelCampoJ2), BorderLayout.CENTER);
        panelTotal.add(zonaJ2);

        // zona del jugador activo (J1 abajo)
        JPanel zonaJ1 = new JPanel(new BorderLayout(4, 4));
        zonaJ1.setBackground(new Color(40, 10, 10));
        zonaJ1.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COLOR_RED), " Tu zona ",
            TitledBorder.LEFT, TitledBorder.TOP, null, COLOR_RED));

        JPanel infoJ1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        infoJ1.setBackground(new Color(40, 10, 10));
        lblNombreJ1 = infoLabel("", Font.BOLD, 14, COLOR_RED);
        lblLpJ1     = infoLabel("LP: 8000", Font.BOLD, 13, new Color(100, 200, 100));
        lblManoJ1   = infoLabel("Mano: 5", Font.PLAIN, 12, FG_WHITE);
        lblMazoJ1   = infoLabel("Mazo: 20", Font.PLAIN, 12, FG_WHITE);
        lblTrampasJ1= infoLabel("Trampas: 0", Font.PLAIN, 12, new Color(180, 100, 220));
        infoJ1.add(lblNombreJ1); infoJ1.add(lblLpJ1);
        infoJ1.add(lblManoJ1);   infoJ1.add(lblMazoJ1); infoJ1.add(lblTrampasJ1);

        panelCampoJ1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        panelCampoJ1.setBackground(BG_FIELD);
        panelCampoJ1.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BG_FIELD), "Campo",
            TitledBorder.LEFT, TitledBorder.TOP, null, Color.LIGHT_GRAY));

        zonaJ1.add(infoJ1, BorderLayout.NORTH);
        zonaJ1.add(new JScrollPane(panelCampoJ1), BorderLayout.CENTER);
        panelTotal.add(zonaJ1);

        return panelTotal;
    }

    private JPanel construirPanelAcciones() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(20, 20, 50));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_GOLD),
            BorderFactory.createEmptyBorder(12, 10, 12, 10)));
        panel.setPreferredSize(new Dimension(170, 0));

        JLabel titulo = new JLabel("ACCIONES", SwingConstants.CENTER);
        titulo.setFont(new Font("Serif", Font.BOLD, 15));
        titulo.setForeground(COLOR_GOLD);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titulo);
        panel.add(Box.createVerticalStrut(14));

        btnJugarCarta      = crearBotonAccion("Jugar Carta",     COLOR_GREEN);
        btnAtacar          = crearBotonAccion("Atacar",           COLOR_RED);
        btnActivarTrampa   = crearBotonAccion("Activar Trampa",   COLOR_PURPLE);
        btnCambiarPosicion = crearBotonAccion("Cambiar Posicion", COLOR_GRAY);
        // nuevos botones MP4
        btnDeshacer        = crearBotonAccion("Deshacer Jugada",  new Color(130, 70, 0));
        btnGuardar         = crearBotonAccion("Guardar Partida",  new Color(0, 90, 90));
        btnTerminarTurno   = crearBotonAccion("Terminar Turno",   new Color(80, 80, 20));

        panel.add(btnJugarCarta);      panel.add(Box.createVerticalStrut(8));
        panel.add(btnAtacar);          panel.add(Box.createVerticalStrut(8));
        panel.add(btnActivarTrampa);   panel.add(Box.createVerticalStrut(8));
        panel.add(btnCambiarPosicion); panel.add(Box.createVerticalStrut(8));
        panel.add(btnDeshacer);        panel.add(Box.createVerticalStrut(8));
        panel.add(btnGuardar);         panel.add(Box.createVerticalStrut(8));
        panel.add(Box.createVerticalGlue());
        panel.add(btnTerminarTurno);

        btnJugarCarta.addActionListener(e -> controller.accionJugarCarta());
        btnAtacar.addActionListener(e -> controller.accionAtacar());
        btnActivarTrampa.addActionListener(e -> controller.accionActivarTrampa());
        btnCambiarPosicion.addActionListener(e -> controller.accionCambiarPosicion());
        btnDeshacer.addActionListener(e -> controller.accionDeshacer());
        btnGuardar.addActionListener(e -> controller.accionGuardarPartida());
        btnTerminarTurno.addActionListener(e -> controller.accionTerminarTurno());

        return panel;
    }

    private JPanel construirPanelLog() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COLOR_GOLD), " Registro de batalla ",
            TitledBorder.LEFT, TitledBorder.TOP, null, COLOR_GOLD));

        areaLog = new JTextArea(6, 80);
        areaLog.setEditable(false);
        areaLog.setBackground(new Color(5, 5, 20));
        areaLog.setForeground(new Color(180, 220, 180));
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaLog.setLineWrap(true);
        areaLog.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setPreferredSize(new Dimension(0, 130));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JLabel infoLabel(String texto, int estilo, int size, Color color) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("SansSerif", estilo, size));
        lbl.setForeground(color);
        return lbl;
    }

    private JButton crearBotonAccion(String texto, Color bg) {
        JButton btn = new JButton("<html><center>" + texto + "</center></html>");
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(150, 42));
        btn.setPreferredSize(new Dimension(150, 42));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    @Override
    public void agregarLog(String texto) {
        if (texto == null || texto.isBlank()) return;
        areaLog.append(texto);
        areaLog.append("\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    @Override
    public void actualizarUI() {
        CampoBatalla campo = controller.getCampo();
        Jugador activo   = campo.getJugadorActivo();
        Jugador oponente = campo.getOponente();

        lblTurno.setText("Turno " + campo.getTurnoActual() + "  --  Turno de: " + activo.getNombre().toUpperCase());

        lblNombreJ1.setText(activo.getNombre());
        lblLpJ1.setText("LP: " + activo.getLp());
        lblManoJ1.setText("Mano: " + activo.getMano().size());
        lblMazoJ1.setText("Mazo: " + activo.getMazo().tamano());
        lblTrampasJ1.setText("Trampas: " + activo.getZonaTrampas().size());

        lblNombreJ2.setText(oponente.getNombre());
        lblLpJ2.setText("LP: " + oponente.getLp());
        lblManoJ2.setText("Mano: " + oponente.getMano().size());
        lblMazoJ2.setText("Mazo: " + oponente.getMazo().tamano());
        lblTrampasJ2.setText("Trampas: " + oponente.getZonaTrampas().size());

        colorearLP(lblLpJ1, activo.getLp());
        colorearLP(lblLpJ2, oponente.getLp());

        refrescarPanelCampo(panelCampoJ1, activo.getCampo());
        refrescarPanelCampo(panelCampoJ2, oponente.getCampo());

        boolean puedoJugar   = !activo.isYaJugoCartaEsteTurno() && !activo.getMano().isEmpty();
        boolean hayAtacantes = activo.getCampo().stream().anyMatch(CartaMonstruo::puedeAtacar);
        Contexto ctx         = new Contexto(activo, oponente, campo);
        boolean hayTrampas   = activo.hayTrampaActivable(ctx);

        btnJugarCarta.setEnabled(puedoJugar);
        btnAtacar.setEnabled(hayAtacantes && !activo.isYaAtacoEsteTurno());
        btnActivarTrampa.setEnabled(hayTrampas);
        btnCambiarPosicion.setEnabled(!activo.getCampo().isEmpty());
        btnTerminarTurno.setEnabled(true);
        btnDeshacer.setEnabled(true);
        btnGuardar.setEnabled(true);
    }

    @Override
    public void mostrarGanador() {
        btnJugarCarta.setEnabled(false);
        btnAtacar.setEnabled(false);
        btnActivarTrampa.setEnabled(false);
        btnCambiarPosicion.setEnabled(false);
        btnTerminarTurno.setEnabled(false);
        btnDeshacer.setEnabled(false);
        btnGuardar.setEnabled(false);

        Jugador ganador = controller.getCampo().getGanador();
        String nombre = (ganador != null) ? ganador.getNombre() : "Nadie";

        actualizarUI();

        JDialog dialogo = new JDialog(this, "DUELO TERMINADO", true);
        dialogo.setLayout(new BorderLayout());
        dialogo.getContentPane().setBackground(new Color(10, 10, 30));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(10, 10, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 20, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(8, 0, 8, 0);

        JLabel lblGana = new JLabel(nombre.toUpperCase() + " GANA EL DUELO!", SwingConstants.CENTER);
        lblGana.setFont(new Font("Serif", Font.BOLD, 22));
        lblGana.setForeground(COLOR_GOLD);
        panel.add(lblGana, gbc);

        gbc.gridy = 1;
        JLabel cita = new JLabel("\"Confia en el corazon de las cartas\" -- Yugi Muto", SwingConstants.CENTER);
        cita.setFont(new Font("Serif", Font.ITALIC, 13));
        cita.setForeground(new Color(180, 180, 220));
        panel.add(cita, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(20, 0, 8, 0);
        JButton btnNuevo = new JButton("Nueva partida");
        btnNuevo.setBackground(COLOR_GREEN);
        btnNuevo.setForeground(Color.WHITE);
        btnNuevo.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnNuevo.setFocusPainted(false);
        btnNuevo.addActionListener(e -> {
            dialogo.dispose();
            new VentanaInicio().setVisible(true);
            VentanaDuelo.this.dispose();
        });
        panel.add(btnNuevo, gbc);

        dialogo.add(panel, BorderLayout.CENTER);
        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    @Override
    public int pedirSeleccion(String titulo, String mensaje, String[] opciones) {
        String elegida = (String) JOptionPane.showInputDialog(
            this, mensaje, titulo,
            JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
        if (elegida == null) return -1;
        return java.util.Arrays.asList(opciones).indexOf(elegida);
    }

    @Override
    public String pedirTexto(String titulo, String mensaje, String valorInicial) {
        String valor = (String) JOptionPane.showInputDialog(
            this, mensaje, titulo, JOptionPane.PLAIN_MESSAGE, null, null, valorInicial);
        if (valor == null || valor.isBlank()) return null;
        return valor.trim();
    }

    @Override
    public void mostrarMensaje(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    private void colorearLP(JLabel lbl, int lp) {
        if (lp > 4000)      lbl.setForeground(new Color(80, 220, 80));
        else if (lp > 1500) lbl.setForeground(new Color(240, 200, 40));
        else                lbl.setForeground(new Color(240, 60, 60));
    }

    private void refrescarPanelCampo(JPanel panel, List<CartaMonstruo> monstruos) {
        panel.removeAll();
        if (monstruos.isEmpty()) {
            JLabel vacio = new JLabel("(campo vacio)");
            vacio.setForeground(Color.GRAY);
            panel.add(vacio);
        } else {
            for (CartaMonstruo m : monstruos) panel.add(crearTarjetaMonstruo(m));
        }
        panel.revalidate();
        panel.repaint();
    }

    private JPanel crearTarjetaMonstruo(CartaMonstruo m) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(105, 115));
        card.setMaximumSize(new Dimension(105, 115));

        Color borde = m.estaEnModoDefensa() ? new Color(80, 140, 220) : new Color(220, 80, 80);
        card.setBorder(BorderFactory.createLineBorder(borde, 2));
        card.setBackground(new Color(20, 40, 20));

        JLabel nombre = new JLabel("<html><center>" + m.getNombre() + "</center></html>", SwingConstants.CENTER);
        nombre.setFont(new Font("SansSerif", Font.BOLD, 10));
        nombre.setForeground(COLOR_GOLD);
        nombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel stats = new JLabel("ATK:" + m.getAtk() + " DEF:" + m.getDef(), SwingConstants.CENTER);
        stats.setFont(new Font("Monospaced", Font.PLAIN, 10));
        stats.setForeground(Color.WHITE);
        stats.setAlignmentX(Component.CENTER_ALIGNMENT);

        String modoStr  = m.estaEnModoDefensa() ? "DEF" : "ATK";
        Color modoColor = m.estaEnModoDefensa() ? new Color(80, 140, 220) : new Color(220, 80, 80);
        JLabel modo = new JLabel("[ " + modoStr + " ]", SwingConstants.CENTER);
        modo.setFont(new Font("SansSerif", Font.BOLD, 10));
        modo.setForeground(modoColor);
        modo.setAlignmentX(Component.CENTER_ALIGNMENT);

        String ataqueStr = m.puedeAtacar() ? "puede atacar" : "ya ataco";
        JLabel ataque = new JLabel(ataqueStr, SwingConstants.CENTER);
        ataque.setFont(new Font("SansSerif", Font.PLAIN, 9));
        ataque.setForeground(m.puedeAtacar() ? new Color(100, 200, 100) : Color.GRAY);
        ataque.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nivel = new JLabel("Lv" + m.getnivelCarta(), SwingConstants.CENTER);
        nivel.setFont(new Font("SansSerif", Font.PLAIN, 9));
        nivel.setForeground(new Color(200, 200, 100));
        nivel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalStrut(4));
        card.add(nombre);
        card.add(Box.createVerticalStrut(3));
        card.add(nivel);
        card.add(stats);
        card.add(modo);
        card.add(ataque);
        card.add(Box.createVerticalStrut(4));

        return card;
    }
}
