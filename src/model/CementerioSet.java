// cementerio usando Set para evitar duplicados y TreeMap para ordenar por ATK
// dos estructuras de datos justificadas en un solo lugar
package model;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;

public class CementerioSet {

    // set de nombres unicos - no admite duplicados y esto es exactamente lo que necesitamos
    private Set<String> nombresDestruidos;

    // treemap ordenado por ATK - util para saber cual fue el monstruo mas fuerte destruido
    private TreeMap<Integer, List<String>> monstruosPorAtk;

    public CementerioSet() {
        nombresDestruidos = new LinkedHashSet<>(); // mantiene el orden de llegada
        monstruosPorAtk = new TreeMap<>();
    }

    // registra una carta que fue destruida
    public void agregar(Carta carta) {
        nombresDestruidos.add(carta.getNombre());
        if (carta instanceof CartaMonstruo) {
            CartaMonstruo m = (CartaMonstruo) carta;
            int atk = m.getAtk();
            monstruosPorAtk.computeIfAbsent(atk, k -> new ArrayList<>()).add(m.getNombre());
        }
    }

    // dice si una carta ya paso por el cementerio
    public boolean yaDestruida(String nombre) {
        return nombresDestruidos.contains(nombre);
    }

    public int total() {
        return nombresDestruidos.size();
    }

    // retorna el nombre del monstruo mas fuerte que paso por aqui
    public String monstruoMasFuerte() {
        if (monstruosPorAtk.isEmpty()) return "ninguno";
        List<String> lista = monstruosPorAtk.lastEntry().getValue();
        return lista.get(0) + " (ATK " + monstruosPorAtk.lastKey() + ")";
    }

    public Set<String> getNombres() {
        return new LinkedHashSet<>(nombresDestruidos);
    }

    public TreeMap<Integer, List<String>> getMonstruosPorAtk() {
        return monstruosPorAtk;
    }
}
