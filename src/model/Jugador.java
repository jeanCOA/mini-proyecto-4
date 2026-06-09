// Clase para el jugador que controla mano mazo campo y vida
// aqui se decide si puede jugar cartas atacar o activar trampas
package model;

import java.util.ArrayList;
import java.util.List;

public class Jugador {

    // nombre del jugador
    private String nombre;
    // puntos de vida del jugador
    private int lp = 8000;
    // si ya jugo una carta este turno
    private boolean yaJugoCartaEsteTurno = false;
    // si ya ataco con algun monstruo este turno
    private boolean yaAtacoEsteTurno = false;
    // si esta bloqueado para jugar cartas el proximo turno
    private boolean bloqueadoProximoTurno = false;
    // cartas que tiene en la mano
    private List<Carta> mano;
    // mazo de donde roba las cartas
    private Mazo mazo;
    // monstruos que estan en el campo
    private List<CartaMonstruo> campo;
    // trampas colocadas en el campo
    private List<CartaTrampa> zonaTrampas;

    public Jugador(String nombre) {
        this.nombre = nombre;
        this.mazo = new Mazo(false);
        this.lp = 8000;
        this.mano = new ArrayList<>();
        this.campo = new ArrayList<>();
        this.zonaTrampas = new ArrayList<>();
        this.yaJugoCartaEsteTurno = false;
        this.yaAtacoEsteTurno = false;
        this.bloqueadoProximoTurno = false;
    }

    public String getNombre() { return nombre; }

    public int getLp() { return lp; }

    public void setLp(int lp) {
        this.lp = lp;
        if (this.lp < 0) this.lp = 0;
    }

    // devuelve las cartas que el jugador tiene en la mano
    public List<Carta> getMano()            { return mano; }
    // devuelve los monstruos que el jugador tiene en el campo
    public List<CartaMonstruo> getCampo()   { return campo; }
    // devuelve el mazo del jugador
    public Mazo getMazo()                   { return mazo; }
    // permite cambiar el mazo si hace falta en el duelo
    public void setMazo(Mazo mazo)          { this.mazo = mazo; }
    // devuelve las trampas que el jugador ya colocó
    public List<CartaTrampa> getZonaTrampas() { return zonaTrampas; }

    // roba una carta desde el mazo a la mano
    public void robarCarta() {
        if (mazo != null) {
            Carta c = mazo.robar();
            if (c != null) mano.add(c);
        }
    }

    // quita puntos de vida al jugador
    public void recibirDanio(int pts) {
        lp -= pts;
        if (lp < 0) lp = 0;
    }

    // suma vida cuando se cura
    public void curarDanio(int pts) { lp += pts; }

    // revisa si hay monstruos en campo para atacar o defender
    public boolean tieneMonstruosEnCampo()  { return !campo.isEmpty(); }
    public boolean tieneCartasEnMazo()      { return mazo != null && !mazo.estaVacio(); }
    public boolean puedeJugarCarta()        { return !yaJugoCartaEsteTurno; }

    public void bloquearJugarCartaProximoTurno() { bloqueadoProximoTurno = true; }
    public boolean isBloqueadoProximoTurno()     { return bloqueadoProximoTurno; }

    // resetea el estado de turno para poder jugar y atacar otra vez
    public void resetTurno() {
        if (bloqueadoProximoTurno) {
            yaJugoCartaEsteTurno = true;
            bloqueadoProximoTurno = false;
        } else {
            yaJugoCartaEsteTurno = false;
        }
        yaAtacoEsteTurno = false;
        for (CartaMonstruo m : campo) {
            m.setPuedeAtacar(true);
        }
    }

    // intenta jugar la carta de la mano segun su tipo
    public boolean jugarCarta(int indice, Contexto ctx, int indiceSacrificio) {
        if (indice < 0 || indice >= mano.size()) return false;
        if (yaJugoCartaEsteTurno) return false;

        Carta carta = mano.get(indice);

        if (carta.getTipo().equals("MONSTRUO")) {
            CartaMonstruo monstruo = (CartaMonstruo) carta;
            if (monstruo.getnivelCarta() > 4) {
                if (campo.isEmpty()) return false;
                if (indiceSacrificio < 0 || indiceSacrificio >= campo.size()) return false;
                campo.remove(indiceSacrificio);
            }
            campo.add(monstruo);
            mano.remove(indice);
            yaJugoCartaEsteTurno = true;
            monstruo.setPuedeAtacar(!ctx.getCampo().isEsPrimerTurno());
            return true;

        } else if (carta.getTipo().equals("MAGICA")) {
            if (carta instanceof Activable) {
                ((Activable) carta).activar(ctx);
                mano.remove(indice);
                yaJugoCartaEsteTurno = true;
                return true;
            }

        } else if (carta.getTipo().equals("TRAMPA")) {
            CartaTrampa trampa = (CartaTrampa) carta;
            zonaTrampas.add(trampa);
            mano.remove(indice);
            yaJugoCartaEsteTurno = true;
            return true;
        }

        return false;
    }

    // activa una trampa cuando se cumplen las condiciones
    public boolean activarTrampa(int indiceTrampa, Contexto ctx) {
        if (indiceTrampa < 0 || indiceTrampa >= zonaTrampas.size()) return false;
        CartaTrampa trampa = zonaTrampas.get(indiceTrampa);
        if (!trampa.puedoActivarme(ctx)) return false;
        trampa.activar(ctx);
        zonaTrampas.remove(indiceTrampa);
        return true;
    }

    // revisa si hay cartas trampa que se pueden activar ahora
    public boolean hayTrampaActivable(Contexto ctx) {
        for (CartaTrampa t : zonaTrampas) {
            if (t.puedoActivarme(ctx)) return true;
        }
        return false;
    }

    public boolean isYaAtacoEsteTurno()     { return yaAtacoEsteTurno; }
    public void setYaAtacoEsteTurno(boolean v) { yaAtacoEsteTurno = v; }
    public boolean isYaJugoCartaEsteTurno() { return yaJugoCartaEsteTurno; }
}
