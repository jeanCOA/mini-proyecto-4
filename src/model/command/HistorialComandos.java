// una pila donde se guardan todos los movimientos que hizo el jugador
// asi puede deshacer lo que acaba de hacer sin que le duela mucho
package model.command;

import java.util.ArrayDeque;

public class HistorialComandos {

    // pila LIFO: el ultimo comando entra primero y sale primero
    // o sea, lo ultimo que hiciste es lo primero que puedes deshacer
    private ArrayDeque<IComando> pila;

    public HistorialComandos() {
        pila = new ArrayDeque<>();
    }

    // mete el comando en la pila despues de ejecutarlo
    // asi queda guardado por si necesita deshacerlo
    public void ejecutar(IComando comando) {
        comando.ejecutar();
        pila.push(comando);
    }

    // saca el ultimo comando y lo deshace
    // devuelve true si lo logro, false si no hay nada que deshacer
    public boolean deshacer() {
        if (pila.isEmpty()) return false;
        IComando ultimo = pila.pop();
        ultimo.deshacer();
        return true;
    }

    // pregunta si hay algo que deshacer
    public boolean puedeDeshacer() {
        return !pila.isEmpty();
    }

    // te dice que es lo que va a deshacer si presionas undo
    // util para mostrarle al usuario antes de hacer click
    public String descripcionUltimo() {
        if (pila.isEmpty()) return "nada";
        return pila.peek().getDescripcion();
    }

    // al empezar un turno nuevo vaciamos la pila
    // porque ya no se puede deshacer lo del turno pasado
    public void limpiar() {
        pila.clear();
    }
}
