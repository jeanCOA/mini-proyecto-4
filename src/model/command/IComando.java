// interfaz del patron Command para las acciones del duelo
// cada jugada es un objeto con execute y undo independiente
package model.command;

public interface IComando {
    void ejecutar();
    void deshacer();
    String getDescripcion();
}
