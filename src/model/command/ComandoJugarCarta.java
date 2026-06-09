// comando para jugar una carta desde la mano
// guarda el estado antes de ejecutar para poder revertirlo con deshacer
package model.command;

import model.*;

public class ComandoJugarCarta implements IComando {

    private Jugador jugador;
    private Contexto ctx;
    private int indiceCarta;
    private int indiceSacrificio;

    // guardamos estos antes de ejecutar para usarlos en deshacer
    private Carta cartaJugada;
    private CartaMonstruo monstruoSacrificado;
    private boolean exito;

    public ComandoJugarCarta(Jugador jugador, Contexto ctx, int indiceCarta, int indiceSacrificio) {
        this.jugador = jugador;
        this.ctx = ctx;
        this.indiceCarta = indiceCarta;
        this.indiceSacrificio = indiceSacrificio;
    }

    @Override
    public void ejecutar() {
        // guardamos las referencias antes de que la logica mueva las cartas
        if (indiceCarta >= 0 && indiceCarta < jugador.getMano().size()) {
            cartaJugada = jugador.getMano().get(indiceCarta);
        }

        if (indiceSacrificio >= 0 && indiceSacrificio < jugador.getCampo().size()) {
            monstruoSacrificado = jugador.getCampo().get(indiceSacrificio);
        }

        exito = jugador.jugarCarta(indiceCarta, ctx, indiceSacrificio);
    }

    @Override
    public void deshacer() {
        if (!exito || cartaJugada == null) return;

        String tipo = cartaJugada.getTipo();

        if (tipo.equals("MONSTRUO")) {
            // saca el monstruo del campo y lo devuelve a la mano
            jugador.getCampo().remove(cartaJugada);
            jugador.getMano().add(cartaJugada);
            if (monstruoSacrificado != null) jugador.getCampo().add(monstruoSacrificado);
        } else if (tipo.equals("MAGICA")) {
            // la carta vuelve a la mano, el efecto ya se aplico y no se revierte
            jugador.getMano().add(cartaJugada);
        } else if (tipo.equals("TRAMPA")) {
            jugador.getZonaTrampas().remove(cartaJugada);
            jugador.getMano().add(cartaJugada);
        }

        jugador.setYaJugoCartaEsteTurno(false);
    }

    @Override
    public String getDescripcion() {
        return "Jugar carta: " + (cartaJugada != null ? cartaJugada.getNombre() : "desconocida");
    }
}
