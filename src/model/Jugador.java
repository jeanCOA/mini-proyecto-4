// jugador con todo su estado: mano mazo campo trampas y vida
// la mano es LinkedList porque addFirst() es O(1) y modela bien robar cartas
package model;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;



public class Jugador {

    private String nombre;
    private int lp = 8000;
    private boolean yaJugoCartaEsteTurno = false;
    private boolean yaAtacoEsteTurno = false;
    private boolean bloqueadoProximoTurno = false;

    // LinkedList para la mano - addFirst O(1) al robar cartas
    private LinkedList<Carta> mano;
    private Mazo mazo;
    private List<CartaMonstruo> campo;
    private List<CartaTrampa> zonaTrampas;

    public Jugador(String nombre) {
        this.nombre = nombre;
        this.mazo = new Mazo(false);
        this.lp = 8000;
        this.mano = new LinkedList<>();
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

    // retorna List para que el resto del codigo no sepa que es LinkedList internamente
    public List<Carta> getMano()            { return mano; }
    public List<CartaMonstruo> getCampo()   { return campo; }
    public Mazo getMazo()                   { return mazo; }
    public void setMazo(Mazo mazo)          { this.mazo = mazo; }
    public List<CartaTrampa> getZonaTrampas() { return zonaTrampas; }

    // roba del mazo y la pone al frente de la mano con addFirst O(1)
    public void robarCarta() {
        if (mazo != null) {
            Carta c = mazo.robar();
            if (c != null) mano.addFirst(c);
        }
    }

    public void recibirDanio(int pts) {
        lp -= pts;
        if (lp < 0) lp = 0;
    }

    public void curarDanio(int pts) { lp += pts; }

    public boolean tieneMonstruosEnCampo()  { return !campo.isEmpty(); }
    public boolean tieneCartasEnMazo()      { return mazo != null && !mazo.estaVacio(); }
    public boolean puedeJugarCarta()        { return !yaJugoCartaEsteTurno; }

    public void bloquearJugarCartaProximoTurno() { bloqueadoProximoTurno = true; }
    public boolean isBloqueadoProximoTurno()     { return bloqueadoProximoTurno; }

    // resetea los flags del turno y habilita monstruos para atacar
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

    public boolean jugarCarta(int indice, Contexto ctx, int indiceSacrificio) {
        if (indice < 0 || indice >= mano.size()) return false;
        if (yaJugoCartaEsteTurno) return false;

        Carta carta = mano.get(indice);

        if (carta.getTipo().equals("MONSTRUO")) {
            CartaMonstruo monstruo = (CartaMonstruo) carta;
            // monstruos de nivel 5+ requieren sacrificio
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

    public boolean activarTrampa(int indiceTrampa, Contexto ctx) {
        if (indiceTrampa < 0 || indiceTrampa >= zonaTrampas.size()) return false;
        CartaTrampa trampa = zonaTrampas.get(indiceTrampa);
        if (!trampa.puedoActivarme(ctx)) return false;
        trampa.activar(ctx);
        zonaTrampas.remove(indiceTrampa);
        return true;
    }

    public boolean hayTrampaActivable(Contexto ctx) {
        for (CartaTrampa t : zonaTrampas) {
            if (t.puedoActivarme(ctx)) return true;
        }
        return false;
    }

    public boolean isYaAtacoEsteTurno()     { return yaAtacoEsteTurno; }
    public void setYaAtacoEsteTurno(boolean v) { yaAtacoEsteTurno = v; }
    public boolean isYaJugoCartaEsteTurno() { return yaJugoCartaEsteTurno; }
    public void setYaJugoCartaEsteTurno(boolean v) { yaJugoCartaEsteTurno = v; }
    public void setBloqueadoProximoTurno(boolean v) { bloqueadoProximoTurno = v; }
}
