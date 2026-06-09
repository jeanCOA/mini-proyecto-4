import controller.DuelController;
import java.util.Scanner;
import javax.swing.SwingUtilities;
import model.CampoBatalla;
import model.Jugador;
import utils.GestorPersistencia;
import view.ConsolaDuelo;
import view.VentanaInicio;

// punto de entrada del juego
// le preguntas al usuario que modo quiere y arrancas la vista correspondiente
public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Yu-Gi-Oh! Duelo");
        System.out.println("Elige el modo de juego:");
        System.out.println("  [1] Modo GUI (ventana)");
        System.out.println("  [2] Modo Consola");
        System.out.println("  [3] Ver estadisticas (consola)");
        System.out.print("Opcion: ");

        String opcion = scanner.nextLine().trim();

        if (opcion.equals("2")) {
            // modo consola: creas el campo y arrancas la vista de texto
            Jugador j1 = new Jugador("Jugador 1");
            Jugador j2 = new Jugador("Jugador 2");
            CampoBatalla campo = new CampoBatalla(j1, j2);
            campo.iniciarDuelo();
            DuelController controller = new DuelController(campo);
            ConsolaDuelo vista = new ConsolaDuelo(controller, scanner);
            controller.setVista(vista);
            vista.iniciarJuego();
        } else if (opcion.equals("3")) {
            // solo muestra las stats historicas y cierra
            System.out.println(GestorPersistencia.getInstance().leerEstadisticas());
            scanner.close();
        } else {
            // modo GUI por defecto, arranca la ventana de inicio
            scanner.close();
            SwingUtilities.invokeLater(() -> {
                VentanaInicio ventana = new VentanaInicio();
                ventana.setVisible(true);
            });
        }
    }
}
