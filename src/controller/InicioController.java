package controller;

import model.CampoBatalla;
import model.Jugador;
import view.VentanaDuelo;
import view.VentanaInicio;

// controla la pantalla de inicio
// valida nombres crea jugadores y arranca el duelo

public class InicioController {

    private final VentanaInicio vista;

    public InicioController(VentanaInicio vista) {
        this.vista = vista;
    }

    // inicia el duelo creando jugadores y preparando la pantalla
    public void iniciarDuelo(String n1, String n2) {
        // si el primer nombre no existe pongo un nombre por defecto
        if (n1 == null || n1.isBlank()) n1 = "Jugador 1";
        // si el segundo nombre no existe pongo otro nombre por defecto
        if (n2 == null || n2.isBlank()) n2 = "Jugador 2";

        // creo los dos jugadores con sus nombres
        Jugador j1 = new Jugador(n1);
        Jugador j2 = new Jugador(n2);
        // armo el campo de batalla con ambos jugadores
        CampoBatalla campo = new CampoBatalla(j1, j2);
        // inicializa el duelo en el modelo
        campo.iniciarDuelo();

        // creo el controlador del duelo y la ventana del duelo
        DuelController controller = new DuelController(campo);
        VentanaDuelo ventanaDuelo = new VentanaDuelo(controller);
        controller.setVista(ventanaDuelo);
        // lanza el primer turno ahora que la vista ya esta lista
        controller.iniciarPrimerTurno();

        // muestro la ventana del duelo y cierro esta ventana de inicio
        ventanaDuelo.setVisible(true);
        vista.dispose();
    }
}
