package controller;

import java.io.IOException;
import java.util.List;

import model.CampoBatalla;
import model.Jugador;
import model.memento.EstadoDuelo;
import model.memento.MementoManager;
import utils.GestorPersistencia;
import view.VentanaDuelo;
import view.VentanaInicio;

// Esta clase organiza lo primero que ve el usuario: empezar un duelo nuevo
// o volver a abrir una partida guardada desde antes.
public class InicioController {

    private final VentanaInicio vista;

    public InicioController(VentanaInicio vista) {
        this.vista = vista;
    }

    // Arranca un duelo completamente nuevo.
    // Si el usuario no pone nombres, usamos valores por defecto para que no se rompa nada.
    public void iniciarDuelo(String n1, String n2) {
        if (n1 == null || n1.isBlank()) {
            n1 = "Jugador 1";
        }
        if (n2 == null || n2.isBlank()) {
            n2 = "Jugador 2";
        }

        Jugador j1 = new Jugador(n1);
        Jugador j2 = new Jugador(n2);
        CampoBatalla campo = new CampoBatalla(j1, j2);
        campo.iniciarDuelo();

        DuelController controller = new DuelController(campo);
        VentanaDuelo ventanaDuelo = new VentanaDuelo(controller);
        controller.setVista(ventanaDuelo);
        controller.iniciarPrimerTurno();

        ventanaDuelo.setVisible(true);
        vista.dispose();
    }

    // Aquí cargamos una partida vieja y la volvemos a montar como si fuera nueva.
    // El snapshot guarda todo el estado, y el manager se encarga de restaurarlo.
    public void cargarPartida(String nombreArchivo) {
        try {
            EstadoDuelo snapshot = GestorPersistencia.getInstance().cargarPartida(nombreArchivo);

            Jugador j1 = new Jugador(snapshot.getEstadoJ1().getNombre());
            Jugador j2 = new Jugador(snapshot.getEstadoJ2().getNombre());
            CampoBatalla campo = new CampoBatalla(j1, j2);

            // el MementoManager reconstruye todo el estado a partir del snapshot
            new MementoManager().restaurar(snapshot, campo);

            DuelController controller = new DuelController(campo);
            VentanaDuelo ventanaDuelo = new VentanaDuelo(controller);
            controller.setVista(ventanaDuelo);

            ventanaDuelo.setVisible(true);
            ventanaDuelo.agregarLog("Partida cargada: " + nombreArchivo);
            ventanaDuelo.actualizarUI();
            vista.dispose();

        } catch (IOException e) {
            vista.mostrarError("No se pudo cargar la partida: " + e.getMessage());
        }
    }

    // Devuelve los archivos de guardado que ya existen, para mostrarlos en la pantalla.
    public List<String> getGuardados() {
        return GestorPersistencia.getInstance().listarGuardados();
    }

    // Trae las estadísticas guardadas para mostrar algo extra en la pantalla principal.
    public String getEstadisticas() {
        return GestorPersistencia.getInstance().leerEstadisticas();
    }
}
