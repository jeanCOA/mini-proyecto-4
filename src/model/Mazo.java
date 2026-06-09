// mazo de cartas como una pila para robar rapido    // estilo chico de 16 anos
// la carta que se roba siempre es la de arriba    // no hay puntos ni tildes
package model; // paquete del modelo

import java.util.ArrayDeque; // deque para la pila
import java.util.ArrayList; // lista para convertir y desordenar
import java.util.Collections; // para barajar
import java.util.List; // lista generica

public class Mazo { // clase que representa el mazo

    // pila de cartas    // el primer elemento es el de arriba
    private ArrayDeque<Carta> cartas; // cartas dentro del mazo

    public Mazo(boolean usarFabrica) { // constructor del mazo
        this.cartas = new ArrayDeque<>(); // creo la pila vacia
        if (usarFabrica) { // si quiero usar la fabrica
            this.agregarCartas(FabricaDeCartas.crearMazoCompleto()); // agrego las cartas del mazo completo
            this.barajar(); // mezclo el mazo
        }
    }

    public void barajar() { // mezcla las cartas del mazo
        List<Carta> lista = new ArrayList<>(cartas); // convierto la pila a lista
        Collections.shuffle(lista); // desordeno la lista
        cartas.clear(); // borro el mazo viejo
        for (Carta c : lista) cartas.push(c); // vuelvo a poner las cartas en la pila
    }

    public Carta robar() { // roba la carta de arriba
        if (estaVacio()) return null; // si no hay cartas regreso null
        return cartas.pop(); // saco la carta de arriba rapido
    }

    public boolean estaVacio() { return cartas.isEmpty(); } // true si no hay cartas
    public int tamano() { return cartas.size(); } // cuenta las cartas que quedan

    public List<Carta> repartir(int n) { // da n cartas para la mano inicial
        List<Carta> manoRepartida = new ArrayList<>(); // lista donde va la mano
        for (int i = 0; i < n; i++) { // repito n veces
            Carta c = this.robar(); // saco una carta del mazo
            if (c != null) manoRepartida.add(c); // si hay carta la agrego a la mano
            else break; // si no quedan cartas paro
        }
        return manoRepartida; // regreso la mano lista
    }

    public void agregarCartas(List<? extends Carta> nuevasCartas) { // agrega varias cartas al mazo
        for (Carta c : nuevasCartas) cartas.push(c); // las puse arriba en la pila
    }

    public List<Carta> getCartas() { // devuelve copia de las cartas sin tocar el mazo
        return new ArrayList<>(cartas); // copio la pila en lista
    }

    public void agregarCarta(Carta carta) { // agrega una carta al fondo del mazo
        cartas.addLast(carta); // la agrego al final para cargar partidas
    }
}
