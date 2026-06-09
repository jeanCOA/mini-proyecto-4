
import javax.swing.SwingUtilities;
import view.VentanaInicio;
import view.ConsolaDuelo;
import controller.DuelController;
import model.CampoBatalla;
import model.Jugador;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║      ⚔  DUELO YU-GI-OH!  ⚔      ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.println("║  Elige el modo de juego:         ║");
        System.out.println("║  [1] Modo GUI (ventana)          ║");
        System.out.println("║  [2] Modo Consola (terminal)     ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("  Opción: ");

        String opcion = scanner.nextLine().trim();

        if (opcion.equals("2")) {
            // Modo consola
            Jugador j1 = new Jugador("Jugador 1");
            Jugador j2 = new Jugador("Jugador 2");
            CampoBatalla campo = new CampoBatalla(j1, j2);
            campo.iniciarDuelo();

            DuelController controller = new DuelController(campo);
            ConsolaDuelo vista = new ConsolaDuelo(controller, scanner);
            controller.setVista(vista);
            vista.iniciarJuego();
        } else {
            // Modo GUI (opcion 1 o cualquier otra)
            scanner.close();
            SwingUtilities.invokeLater(() -> {
                VentanaInicio ventana = new VentanaInicio();
                ventana.setVisible(true);
            });
        }
    }
}