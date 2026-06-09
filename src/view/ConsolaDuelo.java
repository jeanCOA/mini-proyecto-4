package view;

import controller.DuelController;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import model.*;

// vista de consola del duelo, no tiene logica de juego, solo muestra y pregunta
public class ConsolaDuelo implements IDuelView {

    private static final int MAX_INTENTOS = 3;
    private static final int MAX_NOMBRE_MONSTRUO = 18;

    private final DuelController controller;
    private final Scanner scanner;

    // cuando esto sea true el bucle para
    private boolean dueloTerminado = false;

    public ConsolaDuelo(DuelController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner    = scanner;
    }

    public void iniciarJuego() {
        mostrarBannerDuelo();
        controller.iniciarPrimerTurno();
        while (!dueloTerminado) {
            mostrarMenuTurno();
        }
    }

    private void mostrarBannerDuelo() {
        CampoBatalla campo = controller.getCampo();
        String n1 = campo.getJugadorActivo().getNombre();
        String n2 = campo.getOponente().getNombre();
        System.out.println();
        System.out.println("  Yu-Gi-Oh! Duelo");
        System.out.println("  " + truncar(n1, 20) + " VS " + truncar(n2, 20));
        System.out.println();
    }

    private void mostrarMenuTurno() {
        if (dueloTerminado) return;

        CampoBatalla campo = controller.getCampo();
        Jugador activo = campo.getJugadorActivo();

        String lpInfo = String.format("LP: %d  |  Mano: %d  |  Mazo: %d",
            activo.getLp(), activo.getMano().size(), activo.getMazo().tamano());

        System.out.println();
        System.out.println("Turno " + campo.getTurnoActual() + " - " + activo.getNombre().toUpperCase());
        System.out.println(lpInfo);
        System.out.println("  1. Jugar Carta");
        System.out.println("  2. Atacar");
        System.out.println("  3. Activar Trampa");
        System.out.println("  4. Cambiar Posicion de Monstruo");
        System.out.println("  5. Ver estado del campo");
        System.out.println("  6. Terminar Turno");
        System.out.println("  7. Deshacer ultima jugada");
        System.out.println("  8. Guardar partida");
        System.out.print("  Opcion: ");

        String linea = leerLinea();
        if (linea == null) return;

        int opcion;
        try {
            opcion = Integer.parseInt(linea);
        } catch (NumberFormatException e) {
            System.out.println("  Eso no es un numero, intenta de nuevo.");
            return;
        }

        switch (opcion) {
            case 1 -> controller.accionJugarCarta();
            case 2 -> controller.accionAtacar();
            case 3 -> controller.accionActivarTrampa();
            case 4 -> controller.accionCambiarPosicion();
            case 5 -> actualizarUI();
            case 6 -> controller.accionTerminarTurno();
            case 7 -> controller.accionDeshacer();
            case 8 -> controller.accionGuardarPartida();
            default -> System.out.println("  Opcion fuera de rango (1-8), intenta de nuevo.");
        }
    }

    @Override
    public void agregarLog(String texto) {
        if (texto == null || texto.isBlank()) return;
        for (String linea : texto.split("\n")) {
            if (!linea.isBlank()) System.out.println("  >> " + linea.trim());
        }
    }

    @Override
    public void actualizarUI() {
        CampoBatalla campo = controller.getCampo();
        Jugador activo   = campo.getJugadorActivo();
        Jugador oponente = campo.getOponente();

        System.out.println();
        System.out.println("  === ESTADO DEL CAMPO ===");
        imprimirEstadoJugador("TU ZONA", activo, " (activo)");
        System.out.println("  ---");
        imprimirEstadoJugador("OPONENTE", oponente, "");
        System.out.println("  ========================");
    }

    private void imprimirEstadoJugador(String etiqueta, Jugador j, String extra) {
        System.out.printf("  %s %s%s%n", etiqueta, j.getNombre(), extra);
        System.out.printf("  LP: %-5d  Mano: %d  Mazo: %d  Trampas: %d%n",
            j.getLp(), j.getMano().size(), j.getMazo().tamano(), j.getZonaTrampas().size());
        System.out.println("  Campo: " + describirCampo(j.getCampo()));
    }

    private String describirCampo(List<CartaMonstruo> monstruos) {
        if (monstruos.isEmpty()) return "(vacio)";
        StringBuilder sb = new StringBuilder();
        for (CartaMonstruo m : monstruos) {
            sb.append("[").append(truncar(m.getNombre(), MAX_NOMBRE_MONSTRUO))
              .append(" ATK:").append(m.getAtk())
              .append("/DEF:").append(m.getDef())
              .append(" ").append(m.estaEnModoDefensa() ? "DEF" : "ATK")
              .append(m.puedeAtacar() ? "*" : "x")
              .append("] ");
        }
        return sb.toString().trim();
    }

    @Override
    public void mostrarGanador() {
        dueloTerminado = true;
        Jugador ganador = controller.getCampo().getGanador();
        String nombre = (ganador != null) ? ganador.getNombre() : "Nadie";
        actualizarUI();
        System.out.println();
        System.out.println("  DUELO TERMINADO");
        System.out.println("  " + nombre.toUpperCase() + " GANA EL DUELO!");
    }

    @Override
    public int pedirSeleccion(String titulo, String mensaje, String[] opciones) {
        System.out.println();
        System.out.println("  -- " + titulo + " --");
        for (String linea : mensaje.split("\n")) System.out.println("  " + linea);
        System.out.println();
        for (int i = 0; i < opciones.length; i++) {
            System.out.printf("  [%d] %s%n", i + 1, opciones[i]);
        }

        int intentos = 0;
        while (intentos < MAX_INTENTOS) {
            System.out.print("  Elige (1-" + opciones.length + ", 0=cancelar): ");
            String linea = leerLinea();
            if (linea == null) return -1;
            int eleccion;
            try {
                eleccion = Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                intentos++;
                continue;
            }
            if (eleccion == 0) return -1;
            if (eleccion >= 1 && eleccion <= opciones.length) return eleccion - 1;
            intentos++;
        }
        return -1;
    }

    @Override
    public void mostrarMensaje(String titulo, String mensaje) {
        System.out.println();
        System.out.println("  -- " + titulo + " --");
        for (String linea : mensaje.split("\n")) System.out.println("  " + linea);
        System.out.print("  (ENTER para continuar) ");
        leerLinea();
    }

    @Override
    public String pedirTexto(String titulo, String mensaje, String valorInicial) {
        System.out.println();
        System.out.println("  -- " + titulo + " --");
        System.out.println("  " + mensaje);
        if (valorInicial != null && !valorInicial.isBlank()) {
            System.out.println("  Nombre sugerido: " + valorInicial);
        }
        System.out.print("  Nombre: ");
        String linea = leerLinea();
        if (linea == null || linea.isBlank()) return null;
        return linea.trim();
    }

    private String leerLinea() {
        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException | IllegalStateException e) {
            dueloTerminado = true;
            return null;
        }
    }

    private String truncar(String texto, int max) {
        if (texto.length() <= max) return texto;
        return texto.substring(0, max - 1) + "~";
    }
}
