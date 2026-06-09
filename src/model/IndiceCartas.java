// indice de cartas usando HashMap para busqueda O(1) por nombre    // comentarios faciles sin tilde
// sirve para cargar cartas desde archivos sin recorrer listas enteras    // rapido para buscar carta
package model; // paquete del modelo

import java.util.HashMap; // mapa rapido
import java.util.Map; // interface de mapa
import java.util.Set; // conjunto de nombres

public class IndiceCartas { // indice de cartas por nombre

    private Map<String, Carta> indice; // mapa nombre -> carta

    public IndiceCartas() { // constructor del indice
        indice = new HashMap<>(); // creo el mapa vacio
    }

    public void registrar(Carta carta) { // agrega carta al indice
        indice.put(carta.getNombre(), carta); // guardo por nombre
    }

    public Carta buscar(String nombre) { // busca carta por nombre
        return indice.get(nombre); // devuelve null si no existe
    }

    public boolean existe(String nombre) { // pregunta si hay carta con ese nombre
        return indice.containsKey(nombre); // true si esta en el mapa
    }

    public Set<String> getNombres() { // devuelve los nombres guardados
        return indice.keySet(); // conjunto de claves
    }

    public int total() { // numero de cartas en el indice
        return indice.size(); // cantidad de entradas
    }

    public void limpiar() { // borra todo el indice
        indice.clear(); // mapa vacio otra vez
    }
}
