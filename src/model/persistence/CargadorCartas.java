// cargador de cartas usando Reflection de Java    // comentarios sencillos sin puntos
// lee archivos .txt de /resources/cartas e instancia las clases sin recompilar nada    // carga cartas dinamicas
// para agregar una carta nueva: creas el archivo y listo, sin tocar este codigo    // facil de usar
package persistence; // paquete de persistence

import java.io.*; // clase base de cartas
import java.lang.reflect.Constructor; // archivos y lectura
import java.util.ArrayList; // reflection para crear instancias
import java.util.List; // lista para guardar cartas
import java.util.Properties; // tipo de lista
import model.Carta; // para leer propiedades

public class CargadorCartas { // clase que carga cartas desde archivos

    private static final String DIR_CARTAS = "resources/cartas/"; // carpeta donde estan los txt

    public static List<Carta> cargarDesdeCarpeta() { // carga todas las cartas desde la carpeta
        List<Carta> resultado = new ArrayList<>(); // lista donde guardo las cartas
        File dir = new File(DIR_CARTAS); // carpeta de cartas

        if (!dir.exists() || !dir.isDirectory()) { // si no existe la carpeta
            System.out.println("Carpeta de cartas no encontrada: " + DIR_CARTAS); // aviso por consola
            return resultado; // devuelvo lista vacia
        }

        File[] archivos = dir.listFiles((d, n) -> n.endsWith(".txt")); // solo archivos .txt
        if (archivos == null) return resultado; // si no hay archivo retorno vacio

        for (File archivo : archivos) { // recorro cada archivo
            try {
                Carta c = cargarCartaDesdeArchivo(archivo); // cargo la carta del archivo
                if (c != null) resultado.add(c); // la agrego si se cargo bien
            } catch (Exception e) {
                System.err.println("No se pudo cargar " + archivo.getName() + ": " + e.getMessage()); // mensaje de error
            }
        }

        return resultado; // regreso todas las cartas cargadas
    }

    private static Carta cargarCartaDesdeArchivo(File archivo) throws Exception { // lee y crea una carta de un archivo
        Properties props = new Properties(); // propiedades del archivo
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) { // abro el archivo
            props.load(br); // cargo las propiedades
        }

        String nombreClase = props.getProperty("clase"); // nombre de la clase a instanciar
        if (nombreClase == null || nombreClase.isBlank()) { // si falta la clase
            System.err.println("Falta propiedad 'clase' en " + archivo.getName()); // aviso error
            return null; // no puedo crear la carta
        }

        Class<?> clazz = Class.forName(nombreClase); // carrego la clase en tiempo de ejecucion

        Carta carta; // variable para la carta creada
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor(); // intento constructor sin args
            ctor.setAccessible(true); // lo hago accesible si esta privado
            carta = (Carta) ctor.newInstance(); // creo la instancia
        } catch (NoSuchMethodException e) { // si no hay constructor vacio
            String nombre = props.getProperty("nombre", ""); // pido el nombre
            String descripcion = props.getProperty("descripcion", ""); // pido la descripcion
            Constructor<?> ctor = clazz.getDeclaredConstructor(String.class, String.class); // constructor con nombre y descripcion
            ctor.setAccessible(true); // lo hago accesible
            carta = (Carta) ctor.newInstance(nombre, descripcion); // creo la carta con esos datos
        }

        return carta; // devuelvo la carta creada
    }

    public static List<Carta> getCartasReflection() { // metodo publico para obtener las cartas cargadas
        return cargarDesdeCarpeta(); // llamo al metodo principal
    }
}
