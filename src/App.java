import java.util.Scanner;

import javax.swing.SwingUtilities;

import controller.DuelController;
import model.CampoBatalla;
import model.Jugador;
import utils.GestorPersistencia;
import view.ConsolaDuelo;
import view.VentanaInicio;

// Este es el punto de entrada del juego.
// Aqui le preguntamos al usuario que modo quiere usar y arrancamos la vista correspondiente.
public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Yu-Gi-Oh! Duelo");
        System.out.println("Elige el modo de juego:");
        System.out.println("  [1] Modo GUI (ventana)");
        System.out.println("  [2] Modo Consola");
        System.out.println("  [3] Ver estadisticas (consola)");
        System.out.print("Opcion: ");
        System.out.flush();

        // Leemos lineas hasta obtener una opcion valida (evita problemas con \r\n en Windows)
        String opcion = "";
        while (opcion.isEmpty() && scanner.hasNextLine()) {
            opcion = scanner.nextLine().trim();
        }

        if (opcion.equals("2")) {
            // Modo consola: armamos el duelo y lo conectamos con la vista de texto.
            Jugador j1 = new Jugador("Jugador 1");
            Jugador j2 = new Jugador("Jugador 2");
            CampoBatalla campo = new CampoBatalla(j1, j2);
            campo.iniciarDuelo();
            DuelController controller = new DuelController(campo);
            ConsolaDuelo vista = new ConsolaDuelo(controller, scanner);
            controller.setVista(vista);
            vista.iniciarJuego();
        } else if (opcion.equals("3")) {
            // Este caso solo muestra las estadisticas guardadas y termina ahi.
            System.out.println(GestorPersistencia.getInstance().leerEstadisticas());
            scanner.close();
        } else {
            // Si no eligio otra cosa, arrancamos el modo GUI por defecto con la pantalla de inicio.
            scanner.close();
            SwingUtilities.invokeLater(() -> {
                VentanaInicio ventana = new VentanaInicio();
                ventana.setVisible(true);
            });
        }
    }
}
