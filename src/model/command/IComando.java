// esto es como un contrato para hacer comandos
// cada accion del juego es un objeto que puede ejecutarse y deshacerse
// bien util para el undo del juego
package model.command;

public interface IComando {
    // ejecuta la accion (atacar, jugar carta, etc)
    void ejecutar();
    
    // lo contrario de ejecutar, vuelve todo a como estaba
    void deshacer();
    
    // devuelve texto que describe que se hizo, para mostrarle al usuario
    String getDescripcion();
}
