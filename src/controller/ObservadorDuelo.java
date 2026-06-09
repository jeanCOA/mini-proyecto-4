// interfaz del patron Observer
// cualquier clase que quiera escuchar lo que pasa en el campo implementa esto
package controller;

public interface ObservadorDuelo {
    // se llama cuando algo importante sucede en el duelo
    void onEventoDuelo(String tipoEvento, String detalle);
}
