package controller;

import model.CampoBatalla;
import model.Jugador;
import model.memento.EstadoDuelo;
import model.memento.MementoManager;
import utils.GestorPersistencia;
import view.VentanaDuelo;
import view.VentanaInicio;
import java.io.IOException;
import java.util.List;

// controla la pantalla de inicio
// arranca duelos nuevos o carga partidas guardadas
public class InicioController {

    private final VentanaInicio vista;

    public InicioController(VentanaInicio vista) {
        this.vista = vista;
    }

    public void iniciarDuelo(String n1, String n2) {
        if (n1 == null || n1.isBlank()) n1 = "Jugador 1";
        if (n2 == null || n2.isBlank()) n2 = "Jugador 2";

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

    // carga un archivo de guardado y reconstruye el estado del duelo
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

    // devuelve la lista de guardados disponibles para mostrar en el dialogo
    public List<String> getGuardados() {
        return GestorPersistencia.getInstance().listarGuardados();
    }

    // devuelve las estadisticas historicas para mostrar en la pantalla de inicio
    public String getEstadisticas() {
        return GestorPersistencia.getInstance().leerEstadisticas();
    }
}
