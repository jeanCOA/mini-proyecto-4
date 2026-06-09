// comando para jugar una carta desde la mano    // comentario joven 16 anos sin tildes ni puntos
// guarda el estado antes de ejecutar para poder revertirlo con deshacer    // para poder deshacer la jugada
package model.command; // paquete con los comandos del juego

import model.*; // importo todo lo del paquete model

public class ComandoJugarCarta implements IComando { // clase que representa el comando jugar carta

    private Jugador jugador; // jugador que ejecuta el comando
    private Contexto ctx; // contexto del duelo
    private int indiceCarta; // indice de la carta en la mano
    private int indiceSacrificio; // indice del monstruo a sacrificar si hace falta

    // guardamos estos antes de ejecutar para usarlos en deshacer    // se usan para volver las cosas atras
    private Carta cartaJugada; // referencia a la carta que se juega
    private CartaMonstruo monstruoSacrificado; // referencia al monstruo sacrificado
    private boolean exito; // marca si la jugada tuvo exito

    public ComandoJugarCarta(Jugador jugador, Contexto ctx, int indiceCarta, int indiceSacrificio) { // constructor del comando
        this.jugador = jugador; // asigno el jugador
        this.ctx = ctx; // asigno el contexto
        this.indiceCarta = indiceCarta; // asigno el indice de la carta
        this.indiceSacrificio = indiceSacrificio; // asigno el indice del sacrificio
    }

    @Override
    public void ejecutar() { // metodo que ejecuta el comando
        // guardamos las referencias antes de que la logica mueva las cartas    // para tener las cartas originales
        if (indiceCarta >= 0 && indiceCarta < jugador.getMano().size()) { // si el indice de la carta es valido
            cartaJugada = jugador.getMano().get(indiceCarta); // guardo la carta de la mano
        }

        if (indiceSacrificio >= 0 && indiceSacrificio < jugador.getCampo().size()) { // si el indice del sacrificio es valido
            monstruoSacrificado = jugador.getCampo().get(indiceSacrificio); // guardo el monstruo sacrificado
        }

        exito = jugador.jugarCarta(indiceCarta, ctx, indiceSacrificio); // intento jugar la carta y guardo si funciono
    }

    @Override
    public void deshacer() { // metodo para deshacer la accion si es posible
        if (!exito || cartaJugada == null) return; // si no se jugo o no hay carta no hago nada

        String tipo = cartaJugada.getTipo(); // tipo de la carta que se jugo

        if (tipo.equals("MONSTRUO")) { // si es monstruo
            // saca el monstruo del campo y lo devuelve a la mano    // quita el monstruo y lo devuelve
            jugador.getCampo().remove(cartaJugada); // quito la carta del campo
            jugador.getMano().add(cartaJugada); // la vuelvo a poner en la mano
            if (monstruoSacrificado != null) jugador.getCampo().add(monstruoSacrificado); // si sacrifique vuelvo el monstruo
        } else if (tipo.equals("MAGICA")) { // si es magica
            // la carta vuelve a la mano el efecto ya se aplico y no se revierte    // efecto no se quita pero carta vuelve
            jugador.getMano().add(cartaJugada); // la devuelvo a la mano
        } else if (tipo.equals("TRAMPA")) { // si es trampa
            jugador.getZonaTrampas().remove(cartaJugada); // la saco de la zona de trampas
            jugador.getMano().add(cartaJugada); // la devuelvo a la mano
        }

        jugador.setYaJugoCartaEsteTurno(false); // indico que el jugador puede jugar otra carta en el turno
    }

    @Override
    public String getDescripcion() { // descripcion del comando para el historial o logs
        return "Jugar carta: " + (cartaJugada != null ? cartaJugada.getNombre() : "desconocida"); // nombre de la carta jugada o desconocida
    }
}
