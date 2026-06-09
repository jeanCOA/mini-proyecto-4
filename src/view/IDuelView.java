package view;

// interfaz que deben cumplir todas las vistas del duelo
// asi el controller no sabe si esta hablando con consola o GUI
public interface IDuelView {

    // agrega un mensaje al log del duelo
    void agregarLog(String texto);

    // refresca todos los datos visuales del campo
    void actualizarUI();

    // muestra el dialogo de fin de partida
    void mostrarGanador();

    // le pide al usuario que escriba un texto y lo devuelve
    String pedirTexto(String titulo, String mensaje, String valorInicial);

    // muestra opciones al usuario y devuelve el indice elegido
    int pedirSeleccion(String titulo, String mensaje, String[] opciones);

    // muestra un mensaje informativo y espera confirmacion
    void mostrarMensaje(String titulo, String mensaje);
}
