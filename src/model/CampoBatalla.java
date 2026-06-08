// campo de batalla donde ocurre todo el duelo
// implementa SujetoDuelo para notificar a los observadores cuando algo importante pasa
package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import controller.ObservadorDuelo;
import controller.SujetoDuelo;

public class CampoBatalla implements SujetoDuelo {

    private Jugador jugador1;
    private Jugador jugador2;
    private Jugador jugadorActivo;
    private boolean esPrimerTurno = true;
    private int turnoActual = 0;

    // lista de observadores registrados, se notifican cuando algo cambia
    private List<ObservadorDuelo> observadores = new ArrayList<>();

    public CampoBatalla(Jugador jugador1, Jugador jugador2) {
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
    }

    @Override
    public void registrarObservador(ObservadorDuelo obs) {
        if (!observadores.contains(obs)) observadores.add(obs);
    }

    @Override
    public void quitarObservador(ObservadorDuelo obs) {
        observadores.remove(obs);
    }

    // avisa a todos los que estan escuchando que algo paso
    @Override
    public void notificarObservadores(String tipoEvento, String detalle) {
        for (ObservadorDuelo obs : observadores) {
            obs.onEventoDuelo(tipoEvento, detalle);
        }
    }

    public void iniciarDuelo() {
        repartirCartasIniciales();
        Random random = new Random();
        jugadorActivo = random.nextBoolean() ? jugador1 : jugador2;
        notificarObservadores("INICIO_DUELO", jugadorActivo.getNombre() + " comienza");
    }

    private void repartirCartasIniciales() {
        List<Carta> mazoCompleto = FabricaDeCartas.crearMazoCompleto();
        Collections.shuffle(mazoCompleto);

        List<Carta> mazo1 = new ArrayList<>(mazoCompleto.subList(0, 25));
        List<Carta> mazo2 = new ArrayList<>(mazoCompleto.subList(25, 50));

        jugador1.getMazo().agregarCartas(mazo1);
        jugador2.getMazo().agregarCartas(mazo2);

        jugador1.getMano().addAll(jugador1.getMazo().repartir(5));
        jugador2.getMano().addAll(jugador2.getMazo().repartir(5));
    }

    public String prepararTurno() {
        turnoActual++;
        jugadorActivo.resetTurno();

        StringBuilder log = new StringBuilder();
        log.append("=== TURNO ").append(turnoActual)
           .append(" : ").append(jugadorActivo.getNombre()).append(" ===\n");

        if (esPrimerTurno) {
            // primer turno: no se roba carta y no se puede atacar
            log.append("[Primer turno] ").append(jugadorActivo.getNombre())
               .append(" no roba carta y no puede atacar\n");
            for (CartaMonstruo m : jugadorActivo.getCampo()) {
                m.marcarComoAtacado();
            }
        } else {
            if (!jugadorActivo.tieneCartasEnMazo()) {
                log.append(jugadorActivo.getNombre())
                   .append(" no tiene cartas en el mazo pierde el duelo\n");
                notificarObservadores("SIN_MAZO", jugadorActivo.getNombre());
                return log.toString();
            }
            jugadorActivo.robarCarta();
            log.append(jugadorActivo.getNombre()).append(" robo una carta\n");

            for (CartaMonstruo m : jugadorActivo.getCampo()) {
                m.decrementarMejora();
            }
        }

        notificarObservadores("INICIO_TURNO", "Turno " + turnoActual + " - " + jugadorActivo.getNombre());
        return log.toString();
    }

    public void terminarTurno() {
        notificarObservadores("FIN_TURNO", jugadorActivo.getNombre() + " termina turno");
        jugadorActivo = (jugadorActivo == jugador1) ? jugador2 : jugador1;
        esPrimerTurno = false;
    }

    public String resolverCombate(CartaMonstruo atacante, CartaMonstruo defensor,
                                   Jugador jugActivo, Jugador oponente) {
        StringBuilder log = new StringBuilder();
        log.append(atacante.getNombre()).append(" ataca a ").append(defensor.getNombre()).append("!\n");

        int atkAtacante = atacante.getAtk();

        if (defensor.estaEnModoDefensa()) {
            int defDefensor = defensor.getDef();
            log.append(defensor.getNombre()).append(" esta en MODO DEFENSA (DEF: ").append(defDefensor).append(")\n");
            if (atkAtacante > defDefensor) {
                eliminarMonstruo(defensor, oponente);
                log.append(defensor.getNombre()).append(" fue destruido en modo defensa\n");
            } else {
                log.append("Ataque bloqueado la defensa de ").append(defensor.getNombre()).append(" es demasiado alta\n");
            }
        } else {
            if (atkAtacante > defensor.getAtk()) {
                int danio = atkAtacante - defensor.getAtk();
                eliminarMonstruo(defensor, oponente);
                oponente.recibirDanio(danio);
                log.append(defensor.getNombre()).append(" destruido ")
                   .append(oponente.getNombre()).append(" pierde ").append(danio).append(" LP\n");
            } else if (atkAtacante == defensor.getAtk()) {
                eliminarMonstruo(defensor, oponente);
                eliminarMonstruo(atacante, jugActivo);
                log.append("Empate ambos monstruos fueron destruidos\n");
            } else {
                int danio = defensor.getAtk() - atkAtacante;
                jugActivo.recibirDanio(danio);
                log.append(atacante.getNombre()).append(" fue repelido ")
                   .append(jugActivo.getNombre()).append(" pierde ").append(danio).append(" LP\n");
            }
        }

        notificarObservadores("COMBATE", atacante.getNombre() + " vs " + defensor.getNombre());
        atacante.marcarComoAtacado();
        return log.toString();
    }

    public String ataqueDirecto(CartaMonstruo atacante, Jugador oponente) {
        oponente.recibirDanio(atacante.getAtk());
        atacante.marcarComoAtacado();
        notificarObservadores("ATAQUE_DIRECTO", atacante.getNombre() + " -> " + oponente.getNombre());
        return atacante.getNombre() + " ataca directamente a " + oponente.getNombre()
               + " pierde " + atacante.getAtk() + " LP\n";
    }

    public void aplicarBoostAtk(Jugador j, short boost) {
        if (!j.getCampo().isEmpty()) j.getCampo().get(0).aplicarBoostAtk(boost);
    }

    public void aplicarBoostDef(Jugador j, short boost) {
        if (!j.getCampo().isEmpty()) j.getCampo().get(0).aplicarBoostDef(boost);
    }

    public void destruirMenorAtkOponente(Jugador jugActivo) {
        Jugador oponente = (jugActivo == jugador1) ? jugador2 : jugador1;
        if (oponente.getCampo().isEmpty()) return;
        CartaMonstruo menor = oponente.getCampo().get(0);
        for (CartaMonstruo m : oponente.getCampo()) {
            if (m.getAtk() < menor.getAtk()) menor = m;
        }
        eliminarMonstruo(menor, oponente);
    }

    public void eliminarMonstruo(CartaMonstruo m, Jugador j) {
        j.getCampo().remove(m);
    }

    public boolean hayGanador() {
        return jugador1.getLp() <= 0
            || jugador2.getLp() <= 0
            || !jugador1.tieneCartasEnMazo()
            || !jugador2.tieneCartasEnMazo();
    }

    public Jugador getGanador() {
        if (jugador1.getLp() <= 0 || !jugador1.tieneCartasEnMazo()) return jugador2;
        if (jugador2.getLp() <= 0 || !jugador2.tieneCartasEnMazo()) return jugador1;
        return null;
    }

    public void notificarGanador(Jugador ganador) {
        notificarObservadores("GANADOR", ganador.getNombre());
    }

    public Jugador getJugadorActivo() { return jugadorActivo; }
    public Jugador getOponente() {
        return (jugadorActivo == jugador1) ? jugador2 : jugador1;
    }
    public Jugador getJugador1()    { return jugador1; }
    public Jugador getJugador2()    { return jugador2; }
    public int getTurnoActual()     { return turnoActual; }
    public boolean isEsPrimerTurno(){ return esPrimerTurno; }

    // setters usados al cargar partida desde archivo
    public void setTurnoActual(int turno) { this.turnoActual = turno; }
    public void setEsPrimerTurno(boolean v) { this.esPrimerTurno = v; }
    public void setJugadorActivo(Jugador j) { this.jugadorActivo = j; }
}
