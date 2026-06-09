package model.memento;

import java.util.ArrayList;
import java.util.List;
import model.Carta;
import model.CartaMonstruo;
import model.CartaTrampa;
import model.CampoBatalla;
import model.FabricaDeCartas;
import model.Jugador;
import model.Mazo;

// Gestiona la captura y restauracion del estado del duelo (patron Memento)
// capturar() toma una foto del estado actual
// restaurar() reconstruye el duelo a partir de un EstadoDuelo guardado
public class MementoManager {

    // Captura el estado completo del duelo en este momento
    public EstadoDuelo capturar(CampoBatalla campo) {
        Jugador j1 = campo.getJugador1();
        Jugador j2 = campo.getJugador2();

        EstadoJugador ej1 = capturarJugador(j1);
        EstadoJugador ej2 = capturarJugador(j2);

        String nombreActivo = campo.getJugadorActivo().getNombre();
        return new EstadoDuelo(ej1, ej2, campo.getTurnoActual(),
                               campo.isEsPrimerTurno(), nombreActivo);
    }

    // Convierte el estado de un Jugador en un EstadoJugador (lista de nombres de cartas)
    private EstadoJugador capturarJugador(Jugador j) {
        List<String> mano = new ArrayList<>();
        for (Carta c : j.getMano()) mano.add(c.getNombre());

        List<String> campo = new ArrayList<>();
        for (CartaMonstruo m : j.getCampo()) {
            // guardamos nombre + modo defensa para poder restaurarlo
            campo.add(m.getNombre() + (m.estaEnModoDefensa() ? ":DEF" : ":ATK"));
        }

        List<String> trampas = new ArrayList<>();
        for (CartaTrampa t : j.getZonaTrampas()) trampas.add(t.getNombre());

        List<String> mazo = new ArrayList<>();
        for (Carta c : j.getMazo().getCartas()) mazo.add(c.getNombre());

        return new EstadoJugador(
            j.getNombre(), j.getLp(),
            j.isYaJugoCartaEsteTurno(), j.isYaAtacoEsteTurno(),
            j.isBloqueadoProximoTurno(),
            mano, campo, trampas, mazo
        );
    }

    // Restaura el estado de un CampoBatalla a partir de un EstadoDuelo cargado
    public void restaurar(EstadoDuelo snapshot, CampoBatalla campo) {
        restaurarJugador(campo.getJugador1(), snapshot.getEstadoJ1());
        restaurarJugador(campo.getJugador2(), snapshot.getEstadoJ2());
        campo.setTurnoActual(snapshot.getTurnoActual());
        campo.setEsPrimerTurno(snapshot.isEsPrimerTurno());

        // determinar quien es el jugador activo
        String nombreActivo = snapshot.getNombreJugadorActivo();
        if (campo.getJugador2().getNombre().equals(nombreActivo)) {
            campo.setJugadorActivo(campo.getJugador2());
        } else {
            campo.setJugadorActivo(campo.getJugador1());
        }
    }

    // Reconstruye el estado de un jugador (mano, campo, trampas, mazo, stats)
    private void restaurarJugador(Jugador j, EstadoJugador ej) {
        j.setLp(ej.getLp());
        j.setYaJugoCartaEsteTurno(ej.isYaJugoCarta());
        j.setYaAtacoEsteTurno(ej.isYaAtaco());
        j.setBloqueadoProximoTurno(ej.isBloqueado());

        // limpiar estado actual
        j.getMano().clear();
        j.getCampo().clear();
        j.getZonaTrampas().clear();

        // restaurar mano
        for (String nombre : ej.getMano()) {
            Carta c = FabricaDeCartas.crearCarta(nombre);
            if (c != null) j.getMano().add(c);
        }

        // restaurar campo (monstruos con su modo ATK/DEF)
        for (String entrada : ej.getCampo()) {
            String nombre = entrada;
            boolean modoDefensa = false;
            if (entrada.endsWith(":DEF")) {
                nombre = entrada.substring(0, entrada.length() - 4);
                modoDefensa = true;
            } else if (entrada.endsWith(":ATK")) {
                nombre = entrada.substring(0, entrada.length() - 4);
            }
            Carta c = FabricaDeCartas.crearCarta(nombre);
            if (c instanceof CartaMonstruo) {
                CartaMonstruo m = (CartaMonstruo) c;
                if (modoDefensa) m.cambiarPosicion();
                j.getCampo().add(m);
            }
        }

        // restaurar trampas
        for (String nombre : ej.getTrampas()) {
            Carta c = FabricaDeCartas.crearCarta(nombre);
            if (c instanceof CartaTrampa) {
                j.getZonaTrampas().add((CartaTrampa) c);
            }
        }

        // restaurar mazo
        Mazo mazoNuevo = new Mazo(false);
        for (String nombre : ej.getMazo()) {
            Carta c = FabricaDeCartas.crearCarta(nombre);
            if (c != null) mazoNuevo.agregarCarta(c);
        }
        j.setMazo(mazoNuevo);
    }
}
