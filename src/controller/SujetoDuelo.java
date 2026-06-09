// interfaz del sujeto en el patron Observer
// el campo de batalla la implementa para avisar a los observadores
package controller;

public interface SujetoDuelo {
    void registrarObservador(ObservadorDuelo obs);
    void quitarObservador(ObservadorDuelo obs);
    void notificarObservadores(String tipoEvento, String detalle);
}
