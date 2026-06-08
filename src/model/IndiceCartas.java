// indice de cartas usando HashMap para busqueda O(1) por nombre
// sirve para cargar cartas desde archivos sin recorrer listas enteras
package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IndiceCartas {

    // mapa nombre -> carta, acceso instantaneo sin loops
    private Map<String, Carta> indice;

    public IndiceCartas() {
        indice = new HashMap<>();
    }

    public void registrar(Carta carta) {
        indice.put(carta.getNombre(), carta);
    }

    // devuelve null si no encuentra la carta
    public Carta buscar(String nombre) {
        return indice.get(nombre);
    }

    public boolean existe(String nombre) {
        return indice.containsKey(nombre);
    }

    public Set<String> getNombres() {
        return indice.keySet();
    }

    public int total() {
        return indice.size();
    }

    public void limpiar() {
        indice.clear();
    }
}
