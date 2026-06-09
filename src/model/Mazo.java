// Clase para el mazo de cartas del jugador
// se usa para barajar robar y repartir cartas al empezar
package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mazo {

    // cartas que quedan en el mazo
    private List<Carta> cartas;

    public Mazo(boolean usarFabrica) {
        this.cartas = new ArrayList<>();
        if (usarFabrica) {
            this.agregarCartas(FabricaDeCartas.crearMazoCompleto());
            this.barajar();
        }
    }

    // mezcla las cartas del mazo aleatoriamente
    public void barajar() {
        Collections.shuffle(cartas);
    }

    // roba la primera carta del mazo
    public Carta robar() {
        if (estaVacio()) return null;
        return cartas.remove(0);
    }

    // revisa si el mazo ya no tiene cartas
    public boolean estaVacio() { return cartas.isEmpty(); }

    // devuelve cuantas cartas quedan en el mazo
    public int tamano() { return cartas.size(); }

    // saca varias cartas para dar la mano inicial
    public List<Carta> repartir(int n) {
        List<Carta> manoRepartida = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Carta c = this.robar();
            if (c != null) manoRepartida.add(c);
            else break;
        }
        return manoRepartida;
    }

    // agrega cartas al mazo sin barajar
    public void agregarCartas(List<? extends Carta> nuevasCartas) {
        this.cartas.addAll(nuevasCartas);
    }

    // devuelve copia de la lista de cartas del mazo
    public List<Carta> getCartas() {
        return new ArrayList<>(cartas);
    }
}
