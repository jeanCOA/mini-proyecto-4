// log de eventos del turno usando una Queue
// los eventos se encolan en orden y se consumen de la misma forma
package model;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;

public class LogEventos {

    // cola FIFO: primero en entrar primero en salir igual que los eventos del duelo
    private Queue<String> eventos;

    public LogEventos() {
        eventos = new ArrayDeque<>();
    }

    // agrega un evento al final de la cola
    public void registrar(String mensaje) {
        eventos.offer(mensaje);
    }

    // saca y devuelve el evento mas antiguo
    public String siguiente() {
        return eventos.poll();
    }

    public boolean tieneEventos() {
        return !eventos.isEmpty();
    }

    // limpia la cola al cambiar de turno
    public void limpiar() {
        eventos.clear();
    }

    // devuelve todos los eventos sin vaciar la cola
    public List<String> getTodos() {
        return new ArrayList<>(eventos);
    }
}
