// log de eventos del turno usando una Queue    // estilo de comentario simple
// los eventos se encolan en orden y se consumen de la misma forma    // primero entra primero sale
package model; // paquete del modelo

import java.util.ArrayDeque; // implementacion de cola FIFO
import java.util.Queue; // interfaz de cola
import java.util.ArrayList; // lista para copiar eventos
import java.util.List; // tipo lista

public class LogEventos { // clase que guarda los eventos

    private Queue<String> eventos; // cola de mensajes del duelo

    public LogEventos() { // constructor
        eventos = new ArrayDeque<>(); // creo cola vacia
    }

    public void registrar(String mensaje) { // agrega un evento al final
        eventos.offer(mensaje); // encola el mensaje
    }

    public String siguiente() { // devuelve el evento mas viejo
        return eventos.poll(); // saca y retorna el primer evento
    }

    public boolean tieneEventos() { // pregunta si hay eventos pendientes
        return !eventos.isEmpty(); // true si hay algo en la cola
    }

    public void limpiar() { // borra todos los eventos
        eventos.clear(); // deja la cola vacia
    }

    public List<String> getTodos() { // copia todos los eventos sin borrarlos
        return new ArrayList<>(eventos); // devuelve lista nueva
    }
}
