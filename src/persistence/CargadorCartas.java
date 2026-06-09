// Esto lee cartas desde archivos .txt y las crea en vivo, sin tocar el código cada vez.
package persistence;

import model.Carta;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CargadorCartas {

    private static final String DIR_CARTAS = "resources/cartas/";

    // Busca todos los .txt de la carpeta y va creando las cartas una por una.
    public static List<Carta> cargarDesdeCarpeta() {
        List<Carta> resultado = new ArrayList<>();
        File dir = new File(DIR_CARTAS);

        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Carpeta de cartas no encontrada: " + DIR_CARTAS);
            return resultado;
        }

        File[] archivos = dir.listFiles((d, n) -> n.endsWith(".txt"));
        if (archivos == null) return resultado;

        for (File archivo : archivos) {
            try {
                Carta c = cargarCartaDesdeArchivo(archivo);
                if (c != null) resultado.add(c);
            } catch (Exception e) {
                System.err.println("No se pudo cargar " + archivo.getName() + ": " + e.getMessage());
            }
        }

        return resultado;
    }

    // Abre el archivo de la carta, lee sus datos y arma el objeto correcto.
    private static Carta cargarCartaDesdeArchivo(File archivo) throws Exception {
        Properties props = new Properties();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            props.load(br);
        }

        // La propiedad 'clase' dice qué clase Java tiene que abrir.
        String nombreClase = props.getProperty("clase");
        if (nombreClase == null || nombreClase.isBlank()) {
            System.err.println("Falta propiedad 'clase' en " + archivo.getName());
            return null;
        }

        // Class.forName la carga en caliente, así no hace falta importar todo a mano.
        Class<?> clazz = Class.forName(nombreClase);

        Carta carta;
        try {
            // Primero probamos el constructor simple; si no existe, usamos el de nombre y descripción.
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            carta = (Carta) ctor.newInstance();
        } catch (NoSuchMethodException e) {
            // Si la clase necesita datos extra, le pasamos nombre y descripción desde el archivo.
            String nombre = props.getProperty("nombre", "");
            String descripcion = props.getProperty("descripcion", "");
            Constructor<?> ctor = clazz.getDeclaredConstructor(String.class, String.class);
            ctor.setAccessible(true);
            carta = (Carta) ctor.newInstance(nombre, descripcion);
        }

        return carta;
    }

    // Este es el acceso simple para obtener todas las cartas cargadas desde archivos.
    public static List<Carta> getCartasReflection() {
        return cargarDesdeCarpeta();
    }
}
