// gestor de persistencia como Singleton
// una sola instancia maneja todos los archivos de guardado y resultados
package utils;

import model.*;
import model.memento.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GestorPersistencia {

    // unica instancia del singleton
    private static GestorPersistencia instancia;

    private static final String DIR_GUARDADOS = "guardados/";
    private static final String ARCHIVO_RESULTADOS = "resultados.txt";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // privado para que nadie lo instancie directamente
    private GestorPersistencia() {
        new File(DIR_GUARDADOS).mkdirs();
    }

    // devuelve la instancia unica, la crea si no existe
    public static GestorPersistencia getInstance() {
        if (instancia == null) instancia = new GestorPersistencia();
        return instancia;
    }

    // guarda la partida en un archivo de texto plano legible
    public boolean guardarPartida(EstadoDuelo snapshot, String nombreArchivo) {
        String ruta = DIR_GUARDADOS + nombreArchivo + ".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(ruta))) {

            pw.println("# Partida guardada - Yu-Gi-Oh!");
            pw.println("turno=" + snapshot.getTurnoActual());
            pw.println("primerTurno=" + snapshot.isEsPrimerTurno());
            pw.println("jugadorActivo=" + snapshot.getNombreJugadorActivo());
            pw.println("timestamp=" + snapshot.getTimestamp());

            escribirJugador(pw, snapshot.getEstadoJ1(), "J1");
            escribirJugador(pw, snapshot.getEstadoJ2(), "J2");

            return true;
        } catch (IOException e) {
            System.err.println("Error guardando partida: " + e.getMessage());
            return false;
        }
    }

    // escribe los datos de un jugador con un prefijo para distinguirlos
    private void escribirJugador(PrintWriter pw, EstadoJugador ej, String prefijo) {
        pw.println(prefijo + ".nombre=" + ej.getNombre());
        pw.println(prefijo + ".lp=" + ej.getLp());
        pw.println(prefijo + ".yaJugo=" + ej.isYaJugoCarta());
        pw.println(prefijo + ".yaAtaco=" + ej.isYaAtaco());
        pw.println(prefijo + ".bloqueado=" + ej.isBloqueado());
        pw.println(prefijo + ".mano=" + String.join(",", ej.getMano()));
        pw.println(prefijo + ".campo=" + String.join(";", ej.getCampo()));
        pw.println(prefijo + ".trampas=" + String.join(",", ej.getTrampas()));
        pw.println(prefijo + ".mazo=" + String.join(",", ej.getMazo()));
    }

    // carga una partida desde el archivo y devuelve el snapshot
    public EstadoDuelo cargarPartida(String nombreArchivo) throws IOException {
        String ruta = DIR_GUARDADOS + nombreArchivo;
        Map<String, String> datos = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;
                int eq = linea.indexOf('=');
                if (eq < 0) continue;
                String clave = linea.substring(0, eq).trim();
                String valor = linea.substring(eq + 1).trim();
                datos.put(clave, valor);
            }
        }

        int turno = Integer.parseInt(datos.getOrDefault("turno", "0"));
        boolean primerTurno = Boolean.parseBoolean(datos.getOrDefault("primerTurno", "false"));
        String activo = datos.getOrDefault("jugadorActivo", "");

        EstadoJugador ej1 = leerJugador(datos, "J1");
        EstadoJugador ej2 = leerJugador(datos, "J2");

        return new EstadoDuelo(ej1, ej2, turno, primerTurno, activo);
    }

    // lee los datos de un jugador del mapa de clave-valor
    private EstadoJugador leerJugador(Map<String, String> datos, String prefijo) {
        String nombre = datos.getOrDefault(prefijo + ".nombre", "Jugador");
        int lp = Integer.parseInt(datos.getOrDefault(prefijo + ".lp", "8000"));
        boolean yaJugo = Boolean.parseBoolean(datos.getOrDefault(prefijo + ".yaJugo", "false"));
        boolean yaAtaco = Boolean.parseBoolean(datos.getOrDefault(prefijo + ".yaAtaco", "false"));
        boolean bloqueado = Boolean.parseBoolean(datos.getOrDefault(prefijo + ".bloqueado", "false"));

        List<String> mano = parsearLista(datos.getOrDefault(prefijo + ".mano", ""), ",");
        List<String> campo = parsearLista(datos.getOrDefault(prefijo + ".campo", ""), ";");
        List<String> trampas = parsearLista(datos.getOrDefault(prefijo + ".trampas", ""), ",");
        List<String> mazo = parsearLista(datos.getOrDefault(prefijo + ".mazo", ""), ",");

        return new EstadoJugador(nombre, lp, yaJugo, yaAtaco, bloqueado, mano, campo, trampas, mazo);
    }

    // convierte un string separado en una lista limpia
    private List<String> parsearLista(String valor, String sep) {
        List<String> lista = new ArrayList<>();
        if (valor == null || valor.isEmpty()) return lista;
        for (String s : valor.split(sep, -1)) {
            String t = s.trim();
            if (!t.isEmpty()) lista.add(t);
        }
        return lista;
    }

    // lista los archivos .txt que hay en la carpeta de guardados
    public List<String> listarGuardados() {
        List<String> archivos = new ArrayList<>();
        File dir = new File(DIR_GUARDADOS);
        if (!dir.exists()) return archivos;
        File[] fs = dir.listFiles((d, n) -> n.endsWith(".txt"));
        if (fs != null) {
            for (File f : fs) archivos.add(f.getName());
        }
        Collections.sort(archivos);
        return archivos;
    }

    // agrega una linea al archivo de resultados cuando termina un duelo
    public void registrarResultado(String nombre1, String nombre2, String ganador,
                                    int turnos, int lp1Final, int lp2Final) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_RESULTADOS, true))) {
            String linea = LocalDateTime.now().format(FMT) + " | "
                + nombre1 + " | " + nombre2 + " | "
                + ganador + " | turnos:" + turnos + " | "
                + "LP1:" + lp1Final + " | LP2:" + lp2Final;
            pw.println(linea);
        } catch (IOException e) {
            System.err.println("Error escribiendo resultados: " + e.getMessage());
        }
    }

    // lee el archivo de resultados y calcula estadisticas basicas
    public String leerEstadisticas() {
        File f = new File(ARCHIVO_RESULTADOS);
        if (!f.exists()) return "Sin resultados registrados";

        Map<String, Integer> victorias = new HashMap<>();
        int totalPartidas = 0;
        int maxTurnos = 0;
        String partidaMasLarga = "";

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                String[] partes = linea.split("\\|");
                if (partes.length < 6) continue;

                totalPartidas++;
                String ganador = partes[3].trim();
                victorias.merge(ganador, 1, Integer::sum);

                String turnoStr = partes[4].trim().replace("turnos:", "");
                try {
                    int turnos = Integer.parseInt(turnoStr);
                    if (turnos > maxTurnos) {
                        maxTurnos = turnos;
                        partidaMasLarga = linea;
                    }
                } catch (NumberFormatException ignore) {}
            }
        } catch (IOException e) {
            return "Error leyendo estadisticas";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Total de partidas jugadas: ").append(totalPartidas).append("\n");
        sb.append("Victorias por jugador:\n");
        victorias.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append("\n"));
        if (!partidaMasLarga.isEmpty()) {
            sb.append("Partida mas larga (").append(maxTurnos).append(" turnos):\n  ").append(partidaMasLarga).append("\n");
        }
        return sb.toString();
    }
}
