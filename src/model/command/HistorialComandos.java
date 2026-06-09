// pila de comandos para el sistema de undo
// implementada con ArrayDeque: push para apilar pop para deshacer
package model.command;

import java.util.ArrayDeque;

public class HistorialComandos {

    // pila LIFO: el ultimo comando es el primero en deshacerse
    private ArrayDeque<IComando> pila;

    public HistorialComandos() {
        pila = new ArrayDeque<>();
    }

    // ejecuta el comando y lo deja en la pila por si hay que deshacerlo
    public void ejecutar(IComando comando) {
        comando.ejecutar();
        pila.push(comando);
    }

    // deshace el ultimo comando y lo saca de la pila
    public boolean deshacer() {
        if (pila.isEmpty()) return false;
        IComando ultimo = pila.pop();
        ultimo.deshacer();
        return true;
    }

    public boolean puedeDeshacer() {
        return !pila.isEmpty();
    }

    // util para mostrarle al usuario lo que se va a deshacer
    public String descripcionUltimo() {
        if (pila.isEmpty()) return "nada";
        return pila.peek().getDescripcion();
    }

    // al terminar el turno ya no se puede deshacer nada
    public void limpiar() {
        pila.clear();
    }
}
