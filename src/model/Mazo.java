// mazo de cartas implementado como pila con ArrayDeque
// robar del mazo es pop() O(1) lo que modela bien sacar la carta de arriba
package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mazo {

    // pila de cartas, la cabeza es la que se roba primero
    private ArrayDeque<Carta> cartas;

    public Mazo(boolean usarFabrica) {
        this.cartas = new ArrayDeque<>();
        if (usarFabrica) {
            this.agregarCartas(FabricaDeCartas.crearMazoCompleto());
            this.barajar();
        }
    }

    // ArrayDeque no tiene shuffle directo asi que convertimos ida y vuelta
    public void barajar() {
        List<Carta> lista = new ArrayList<>(cartas);
        Collections.shuffle(lista);
        cartas.clear();
        for (Carta c : lista) cartas.push(c);
    }

    // roba la carta de arriba del mazo en O(1)
    public Carta robar() {
        if (estaVacio()) return null;
        return cartas.pop();
    }

    public boolean estaVacio() { return cartas.isEmpty(); }
    public int tamano() { return cartas.size(); }

    // saca n cartas para armar la mano inicial
    public List<Carta> repartir(int n) {
        List<Carta> manoRepartida = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Carta c = this.robar();
            if (c != null) manoRepartida.add(c);
            else break;
        }
        return manoRepartida;
    }

    public void agregarCartas(List<? extends Carta> nuevasCartas) {
        for (Carta c : nuevasCartas) cartas.push(c);
    }

    // copia de la lista para serializar sin tocar la pila real
    public List<Carta> getCartas() {
        return new ArrayList<>(cartas);
    }

    // agrega al final de la pila, se usa al cargar partida
    public void agregarCarta(Carta carta) {
        cartas.addLast(carta);
    }
}
