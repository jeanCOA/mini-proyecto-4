// campo de batalla donde ocurre todo el duelo    // comentarios faciles de entender
// implementa SujetoDuelo para notificar a los observadores cuando algo importante pasa    // sigue el patron observer
package model; // paquete del modelo

import java.util.ArrayList; // lista para cartas y observadores
import java.util.Collections; // para mezclar mazos
import java.util.List; // colecciones de elementos
import java.util.Random; // para decidir quien empieza
import controller.ObservadorDuelo; // observadores del duelo
import controller.SujetoDuelo; // interfaz de sujeto

public class CampoBatalla implements SujetoDuelo { // clase del duelo principal

    private Jugador jugador1; // jugador uno
    private Jugador jugador2; // jugador dos
    private Jugador jugadorActivo; // jugador que esta jugando ahora
    private boolean esPrimerTurno = true; // bandera para el primer turno
    private int turnoActual = 0; // numero de turno

    private List<ObservadorDuelo> observadores = new ArrayList<>(); // lista de observadores

    public CampoBatalla(Jugador jugador1, Jugador jugador2) { // constructor del campo
        this.jugador1 = jugador1; // guardo jugador uno
        this.jugador2 = jugador2; // guardo jugador dos
    }

    @Override
    public void registrarObservador(ObservadorDuelo obs) { // registra un observador
        if (!observadores.contains(obs)) observadores.add(obs); // lo agrega si no esta
    }

    @Override
    public void quitarObservador(ObservadorDuelo obs) { // quita un observador
        observadores.remove(obs); // lo elimina de la lista
    }

    @Override
    public void notificarObservadores(String tipoEvento, String detalle) { // avisa a todos los observadores
        for (ObservadorDuelo obs : observadores) { // recorro cada observador
            obs.onEventoDuelo(tipoEvento, detalle); // les mando el evento
        }
    }

    public void iniciarDuelo() { // arranca el duelo
        repartirCartasIniciales(); // reparto cartas a cada jugador
        Random random = new Random(); // selecciono quien empieza
        jugadorActivo = random.nextBoolean() ? jugador1 : jugador2; // elijo aleatorio
        notificarObservadores("INICIO_DUELO", jugadorActivo.getNombre() + " comienza"); // aviso inicio
    }

    private void repartirCartasIniciales() { // reparte cartas iniciales
        List<Carta> mazoCompleto = FabricaDeCartas.crearMazoCompleto(); // creo el mazo completo
        Collections.shuffle(mazoCompleto); // mezclo las cartas

        List<Carta> mazo1 = new ArrayList<>(mazoCompleto.subList(0, 25)); // mitad para jugador1
        List<Carta> mazo2 = new ArrayList<>(mazoCompleto.subList(25, 50)); // mitad para jugador2

        jugador1.getMazo().agregarCartas(mazo1); // cargo mazo1
        jugador2.getMazo().agregarCartas(mazo2); // cargo mazo2

        jugador1.getMano().addAll(jugador1.getMazo().repartir(5)); // reparto mano de 5 cartas
        jugador2.getMano().addAll(jugador2.getMazo().repartir(5)); // reparto mano de 5 cartas
    }

    public String prepararTurno() { // prepara el turno actual
        turnoActual++; // sube el numero de turno
        jugadorActivo.resetTurno(); // resetea el turno del jugador activo

        StringBuilder log = new StringBuilder(); // creo el texto del log
        log.append("=== TURNO ").append(turnoActual)
           .append(" : ").append(jugadorActivo.getNombre()).append(" ===\n"); // armamos la cabecera

        if (esPrimerTurno) { // si es el primer turno
            log.append("[Primer turno] ").append(jugadorActivo.getNombre())
               .append(" no roba carta y no puede atacar\n"); // no roba y no ataca
            for (CartaMonstruo m : jugadorActivo.getCampo()) { // recorro los monstruos del campo
                m.marcarComoAtacado(); // marco que ya no pueden atacar
            }
        } else { // si no es el primer turno
            if (!jugadorActivo.tieneCartasEnMazo()) { // si no hay cartas en el mazo
                log.append(jugadorActivo.getNombre())
                   .append(" no tiene cartas en el mazo pierde el duelo\n"); // pierdes por deckout
                notificarObservadores("SIN_MAZO", jugadorActivo.getNombre()); // aviso a los observadores
                return log.toString(); // retorno el log
            }
            jugadorActivo.robarCarta(); // roba una carta del mazo
            log.append(jugadorActivo.getNombre()).append(" robo una carta\n"); // agrega al log

            for (CartaMonstruo m : jugadorActivo.getCampo()) { // recorro monstruos en el campo
                m.decrementarMejora(); // bajo mejoras temporales si hay
            }
        }

        notificarObservadores("INICIO_TURNO", "Turno " + turnoActual + " - " + jugadorActivo.getNombre()); // aviso inicio turno
        return log.toString(); // devuelvo el log
    }

    public void terminarTurno() { // termina el turno actual
        notificarObservadores("FIN_TURNO", jugadorActivo.getNombre() + " termina turno"); // aviso fin de turno
        jugadorActivo = (jugadorActivo == jugador1) ? jugador2 : jugador1; // paso el turno al otro
        esPrimerTurno = false; // ya no es primer turno
    }

    public String resolverCombate(CartaMonstruo atacante, CartaMonstruo defensor,
                                   Jugador jugActivo, Jugador oponente) { // resuelve un combate
        StringBuilder log = new StringBuilder(); // texto del combate
        log.append(atacante.getNombre()).append(" ataca a ").append(defensor.getNombre()).append("!\n"); // mensaje inicial

        int atkAtacante = atacante.getAtk(); // ataque del monstruo atacante

        if (defensor.estaEnModoDefensa()) { // si el defensor esta en defensa
            int defDefensor = defensor.getDef(); // defensa del defensor
            log.append(defensor.getNombre()).append(" esta en MODO DEFENSA (DEF: ").append(defDefensor).append(")\n"); // log defensa
            if (atkAtacante > defDefensor) { // si vence la defensa
                eliminarMonstruo(defensor, oponente); // destruyo al defensor
                log.append(defensor.getNombre()).append(" fue destruido en modo defensa\n"); // mensaje destruccion
            } else { // si no vence la defensa
                log.append("Ataque bloqueado la defensa de ").append(defensor.getNombre()).append(" es demasiado alta\n"); // ataque bloqueado
            }
        } else { // si el defensor esta en ataque
            if (atkAtacante > defensor.getAtk()) { // si el atacante es mas fuerte
                int danio = atkAtacante - defensor.getAtk(); // calculo danio
                eliminarMonstruo(defensor, oponente); // destruyo al defensor
                oponente.recibirDanio(danio); // el oponente recibe danio
                log.append(defensor.getNombre()).append(" destruido ")
                   .append(oponente.getNombre()).append(" pierde ").append(danio).append(" LP\n"); // mensaje daño
            } else if (atkAtacante == defensor.getAtk()) { // empate
                eliminarMonstruo(defensor, oponente); // destruyo ambos
                eliminarMonstruo(atacante, jugActivo); // destruyo atacante tambien
                log.append("Empate ambos monstruos fueron destruidos\n"); // mensaje de empate
            } else { // si gana el defensor
                int danio = defensor.getAtk() - atkAtacante; // calculo danio por ataque fallido
                jugActivo.recibirDanio(danio); // el jugador activo recibe danio
                log.append(atacante.getNombre()).append(" fue repelido ")
                   .append(jugActivo.getNombre()).append(" pierde ").append(danio).append(" LP\n"); // mensaje repelido
            }
        }

        notificarObservadores("COMBATE", atacante.getNombre() + " vs " + defensor.getNombre()); // aviso que hubo combate
        atacante.marcarComoAtacado(); // marco que ya ataco este monstruo
        return log.toString(); // retorno el texto del combate
    }

    public String ataqueDirecto(CartaMonstruo atacante, Jugador oponente) { // ataque directo a LP
        oponente.recibirDanio(atacante.getAtk()); // el oponente recibe danio directo
        atacante.marcarComoAtacado(); // el monstruo ya ataco
        notificarObservadores("ATAQUE_DIRECTO", atacante.getNombre() + " -> " + oponente.getNombre()); // aviso ataque directo
        return atacante.getNombre() + " ataca directamente a " + oponente.getNombre()
               + " pierde " + atacante.getAtk() + " LP\n"; // mensaje final
    }

    public void aplicarBoostAtk(Jugador j, short boost) { // aplica boost de ataque al primer monstruo
        if (!j.getCampo().isEmpty()) j.getCampo().get(0).aplicarBoostAtk(boost); // si hay monstruo lo boosteo
    }

    public void aplicarBoostDef(Jugador j, short boost) { // aplica boost de defensa al primer monstruo
        if (!j.getCampo().isEmpty()) j.getCampo().get(0).aplicarBoostDef(boost); // si hay monstruo lo boosteo
    }

    public void destruirMenorAtkOponente(Jugador jugActivo) { // destruye el monstruo oponente con menor atk
        Jugador oponente = (jugActivo == jugador1) ? jugador2 : jugador1; // el otro jugador
        if (oponente.getCampo().isEmpty()) return; // si no hay monstruos no hago nada
        CartaMonstruo menor = oponente.getCampo().get(0); // tomo el primer monstruo
        for (CartaMonstruo m : oponente.getCampo()) { // recorro los monstruos
            if (m.getAtk() < menor.getAtk()) menor = m; // encuentro el menor
        }
        eliminarMonstruo(menor, oponente); // destruyo el monstruo menor
    }

    public void eliminarMonstruo(CartaMonstruo m, Jugador j) { // elimina un monstruo del campo
        j.getCampo().remove(m); // lo saco del campo
    }

    public boolean hayGanador() { // pregunta si el duelo ya termino
        return jugador1.getLp() <= 0
            || jugador2.getLp() <= 0
            || !jugador1.tieneCartasEnMazo()
            || !jugador2.tieneCartasEnMazo(); // si alguno pierde por LP o deckout
    }

    public Jugador getGanador() { // devuelve el ganador si hay
        if (jugador1.getLp() <= 0 || !jugador1.tieneCartasEnMazo()) return jugador2; // gana jugador2
        if (jugador2.getLp() <= 0 || !jugador2.tieneCartasEnMazo()) return jugador1; // gana jugador1
        return null; // si no hay ganador aun
    }

    public void notificarGanador(Jugador ganador) {
        notificarObservadores("GANADOR", ganador.getNombre());
    }

    public void notificarGanador(Jugador ganador) { // avisa al ganador
        notificarObservadores("GANADOR", ganador.getNombre()); // notificacion final
    }

    public Jugador getJugadorActivo() { return jugadorActivo; } // devuelve jugador activo
    public Jugador getOponente() { // devuelve el otro jugador
        return (jugadorActivo == jugador1) ? jugador2 : jugador1;
    }
    public Jugador getJugador1()    { return jugador1; } // primer jugador
    public Jugador getJugador2()    { return jugador2; } // segundo jugador
    public int getTurnoActual()     { return turnoActual; } // turno actual
    public boolean isEsPrimerTurno(){ return esPrimerTurno; } // primer turno?

    public void setTurnoActual(int turno) { this.turnoActual = turno; } // setter de turno actual
    public void setEsPrimerTurno(boolean v) { this.esPrimerTurno = v; } // setter primer turno
    public void setJugadorActivo(Jugador j) { this.jugadorActivo = j; } // set activo
}
