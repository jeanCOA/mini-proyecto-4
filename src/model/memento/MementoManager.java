// Se encarga de guardar y volver a cargar el duelo.
package model.memento;

import model.*;
import java.util.ArrayList;
import java.util.List;

public class MementoManager {

    // Toma una foto del duelo para poder volver atrás después.
    public EstadoDuelo capturar(CampoBatalla campo) {
        EstadoJugador ej1 = capturarJugador(campo.getJugador1());
        EstadoJugador ej2 = capturarJugador(campo.getJugador2());
        return new EstadoDuelo(
            ej1, ej2,
            campo.getTurnoActual(),
            campo.isEsPrimerTurno(),
            campo.getJugadorActivo().getNombre()
        );
    }

    // Convierte al jugador en datos simples que se pueden guardar sin drama.
    private EstadoJugador capturarJugador(Jugador j) {
        List<String> mano = new ArrayList<>();
        for (Carta c : j.getMano()) mano.add(c.getNombre());

        // Guardamos los monstruos del campo en texto simple, separados por pipes.
        List<String> campo = new ArrayList<>();
        for (CartaMonstruo m : j.getCampo()) {
            campo.add(m.getNombre() + "|" + m.getnivelCarta() + "|"
                + m.getAtk() + "|" + m.getDef() + "|"
                + m.puedeAtacar() + "|" + m.estaEnModoDefensa());
        }

        List<String> trampas = new ArrayList<>();
        for (CartaTrampa t : j.getZonaTrampas()) trampas.add(t.getNombre());

        List<String> mazoNombres = new ArrayList<>();
        for (Carta c : j.getMazo().getCartas()) mazoNombres.add(c.getNombre());

        return new EstadoJugador(
            j.getNombre(), j.getLp(),
            j.isYaJugoCartaEsteTurno(), j.isYaAtacoEsteTurno(),
            j.isBloqueadoProximoTurno(),
            mano, campo, trampas, mazoNombres
        );
    }

    // Vuelve a dejar el duelo como estaba cuando se grabó el snapshot.
    public void restaurar(EstadoDuelo snapshot, CampoBatalla campo) {
        restaurarJugador(snapshot.getEstadoJ1(), campo.getJugador1());
        restaurarJugador(snapshot.getEstadoJ2(), campo.getJugador2());
        campo.setTurnoActual(snapshot.getTurnoActual());
        campo.setEsPrimerTurno(snapshot.isEsPrimerTurno());

        // También recuperamos quién estaba jugando en ese momento.
        String activo = snapshot.getNombreJugadorActivo();
        if (campo.getJugador1().getNombre().equals(activo)) {
            campo.setJugadorActivo(campo.getJugador1());
        } else {
            campo.setJugadorActivo(campo.getJugador2());
        }
    }

    // Reconstruye al jugador desde esos datos guardados.
    private void restaurarJugador(EstadoJugador snap, Jugador j) {
        j.setLp(snap.getLp());
        j.setYaJugoCartaEsteTurno(snap.isYaJugoCarta());
        j.setYaAtacoEsteTurno(snap.isYaAtaco());
        j.setBloqueadoProximoTurno(snap.isBloqueado());

        j.getMano().clear();
        for (String nombre : snap.getMano()) {
            Carta c = reconstruirCarta(nombre);
            if (c != null) j.getMano().add(c);
        }

        j.getCampo().clear();
        for (String datoCampo : snap.getCampo()) {
            CartaMonstruo m = reconstruirMonstruoDesdeDato(datoCampo);
            if (m != null) j.getCampo().add(m);
        }

        j.getZonaTrampas().clear();
        for (String nombre : snap.getTrampas()) {
            Carta c = reconstruirCarta(nombre);
            if (c instanceof CartaTrampa) j.getZonaTrampas().add((CartaTrampa) c);
        }

        Mazo nuevoMazo = new Mazo(false);
        for (String nombre : snap.getMazo()) {
            Carta c = reconstruirCarta(nombre);
            if (c != null) nuevoMazo.agregarCarta(c);
        }
        j.setMazo(nuevoMazo);
    }

    // Busca la carta por nombre para volver a crearla desde la fábrica.
    private Carta reconstruirCarta(String nombre) {
        for (CartaMonstruo m : FabricaDeCartas.crearMonstruos()) {
            if (m.getNombre().equals(nombre)) return m;
        }
        for (CartaMagica mg : FabricaDeCartas.crearMagicas()) {
            if (mg.getNombre().equals(nombre)) return mg;
        }
        for (CartaTrampa t : FabricaDeCartas.crearTrampas()) {
            if (t.getNombre().equals(nombre)) return t;
        }
        return null;
    }

    // Lee el texto guardado del monstruo y lo convierte otra vez en objeto.
    private CartaMonstruo reconstruirMonstruoDesdeDato(String dato) {
        String[] partes = dato.split("\\|");
        if (partes.length < 6) return null;

        String nombre = partes[0];
        byte nivel = Byte.parseByte(partes[1]);
        short atk = Short.parseShort(partes[2]);
        short def = Short.parseShort(partes[3]);
        boolean puedeAtacar = Boolean.parseBoolean(partes[4]);
        boolean enDefensa = Boolean.parseBoolean(partes[5]);

        CartaMonstruo m = new CartaMonstruo(nombre, nivel, atk, def);
        m.setPuedeAtacar(puedeAtacar);
        if (enDefensa) m.cambiarPosicion();
        return m;
    }
}
