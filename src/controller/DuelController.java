package controller;

import java.util.ArrayList;
import java.util.List;
import model.*;
import model.command.*;
import model.memento.*;
import utils.GestorPersistencia;
import view.IDuelView;

// esta es la clase mas importante, todo el juego pasa por aqui
// conecta la pantalla con el juego y maneja todo lo que hace el jugador
public class DuelController implements ObservadorDuelo {

    // el campo de batalla donde se pelea
    private final CampoBatalla campo;
    // la pantalla del juego
    private IDuelView vista;

    // historial de movimientos para poder deshacer lo que el jugador hizo mal
    private final HistorialComandos historial;
    // manager para guardar estados del juego
    private final MementoManager mementoManager;

    public DuelController(CampoBatalla campo) {
        this.campo = campo;
        this.historial = new HistorialComandos();
        this.mementoManager = new MementoManager();
        // nos suscribimos al campo para recibir notificaciones de lo que pasa
        campo.registrarObservador(this);
    }

    public void setVista(IDuelView vista) {
        this.vista = vista;
    }

    // el campo nos avisa cuando pasa algo importante y nosotros decidimos que hacer
    @Override
    public void onEventoDuelo(String tipoEvento, String detalle) {
        // se ejecuta cuando pasa algo importante en el juego
        switch (tipoEvento) {
            case "GANADOR":
                // alguien gano el duelo
                if (vista != null) vista.mostrarGanador();
                break;
            case "SIN_MAZO":
                // alguien se quedo sin cartas en el mazo y pierde
                if (vista != null) vista.agregarLog(detalle + " se quedo sin mazo");
                break;
            case "INICIO_TURNO":
                // turno nuevo asi que se puede deshacer lo del turno anterior
                if (vista != null) historial.limpiar();
                break;
            default:
                break;
        }
    }

    // empieza el primer turno del juego
    public void iniciarPrimerTurno() {
        String log = campo.prepararTurno();
        vista.agregarLog(log);
        vista.actualizarUI();
    }

    // aqui el jugador elige una carta de su mano para jugar
    // usa el sistema de comandos para que pueda deshacer si falla
    public void accionJugarCarta() {
        Jugador activo   = campo.getJugadorActivo();
        Jugador oponente = campo.getOponente();
        Contexto ctx     = new Contexto(activo, oponente, campo);
        List<Carta> mano = activo.getMano();

        // revisamos que el jugador tenga cartas en la mano
        if (mano.isEmpty()) { vista.agregarLog("No tienes cartas en la mano"); return; }
        // y que no haya jugado carta ya en este turno
        if (activo.isYaJugoCartaEsteTurno()) { vista.agregarLog("Ya jugaste una carta este turno"); return; }

        // mostramos la mano del jugador para que elija
        String[] opciones = new String[mano.size() + 1];
        for (int i = 0; i < mano.size(); i++) opciones[i] = (i + 1) + ". " + mano.get(i).toString();
        opciones[mano.size()] = "Cancelar";

        int idx = vista.pedirSeleccion(" Tu mano - " + activo.getNombre(),
            "Elige una carta para jugar:", opciones);
        if (idx < 0 || idx >= mano.size()) return;

        Carta carta = mano.get(idx);
        int indiceSacrificio = -1;

        // si es un monstruo de nivel alto necesita sacrificar otro monstruo
        if (carta.getTipo().equals("MONSTRUO")) {
            CartaMonstruo mon = (CartaMonstruo) carta;
            if (mon.getnivelCarta() > 4) {
                if (activo.getCampo().isEmpty()) {
                    vista.agregarLog("Necesitas sacrificar un monstruo para invocar " + mon.getNombre()
                             + " (nivel " + mon.getnivelCarta() + ") pero no tienes monstruos en campo");
                    return;
                }
                // le pedimos que elige cual monstruo sacrificar
                String[] opSac = new String[activo.getCampo().size()];
                for (int i = 0; i < activo.getCampo().size(); i++)
                    opSac[i] = (i + 1) + ". " + activo.getCampo().get(i).getNombre();
                indiceSacrificio = vista.pedirSeleccion("Sacrificio requerido",
                    mon.getNombre() + " (Lv" + mon.getnivelCarta() + ") requiere un sacrificio\nElige el monstruo a sacrificar:",
                    opSac);
                if (indiceSacrificio < 0) return;
            }
        }

        // creamos un comando para jugar la carta y lo metemos en el historial
        ComandoJugarCarta cmd = new ComandoJugarCarta(activo, ctx, idx, indiceSacrificio);
        historial.ejecutar(cmd);

        // si se jugo la carta bien mostramos un mensaje, sino la deshacemos
        if (activo.isYaJugoCartaEsteTurno()) {
            vista.agregarLog(activo.getNombre() + " jugo: " + carta.getNombre());
            verificarGanador();
        } else {
            // si fallo lo sacamos del historial
            historial.deshacer();
            vista.agregarLog("No se pudo jugar la carta");
        }
        vista.actualizarUI();
    }

    // aqui el jugador ataca con un monstruo
    // usa el mismo sistema de comandos que jugar carta
    public void accionAtacar() {
        Jugador activo   = campo.getJugadorActivo();
        Jugador oponente = campo.getOponente();

        // vemos cuales monstruos pueden atacar
        List<CartaMonstruo> disponibles = new ArrayList<>();
        for (CartaMonstruo m : activo.getCampo()) if (m.puedeAtacar()) disponibles.add(m);

        // checamos que tenga monstruos disponibles y que no haya atacado ya
        if (disponibles.isEmpty()) { vista.agregarLog("Ningun monstruo puede atacar"); return; }
        if (activo.isYaAtacoEsteTurno()) { vista.agregarLog("Ya atacaste este turno"); return; }

        // le mostramos los monstruos para que elige cual atacara
        String[] opAtacantes = new String[disponibles.size() + 1];
        for (int i = 0; i < disponibles.size(); i++) opAtacantes[i] = (i + 1) + ". " + disponibles.get(i);
        opAtacantes[disponibles.size()] = "Cancelar";

        int idxAtac = vista.pedirSeleccion("Ataque - " + activo.getNombre(),
            "Elige el monstruo ATACANTE:", opAtacantes);
        if (idxAtac < 0 || idxAtac >= disponibles.size()) return;
        CartaMonstruo atacante = disponibles.get(idxAtac);

        // si el oponente tiene monstruos le preguntamos cual quiere atacar
        CartaMonstruo defensor = null;
        if (!oponente.getCampo().isEmpty()) {
            List<CartaMonstruo> defensores = oponente.getCampo();
            String[] opDef = new String[defensores.size() + 1];
            for (int i = 0; i < defensores.size(); i++) opDef[i] = (i + 1) + ". " + defensores.get(i);
            opDef[defensores.size()] = "Cancelar";
            int idxDef = vista.pedirSeleccion("Selecciona objetivo",
                "Elige el monstruo a atacar:", opDef);
            if (idxDef < 0 || idxDef >= defensores.size()) return;
            defensor = defensores.get(idxDef);
        }

        // antes del ataque le preguntamos al otro jugador si quiere usar una trampa
        Cont// preguntamos si quiere activar una trampa
            boolean activoTrampa = ofrecerRespuestaTrampas(oponente, ctxDefensa);
            if (activoTrampa) {
                verificarGanador();
                if (campo.hayGanador()) { vista.actualizarUI(); return; }
                // si el atacante fue destruido por la trampa el ataque no llega
                if (!activo.getCampo().contains(atacante)) {
                    vista.agregarLog(atacante.getNombre() + " fue destruido el ataque queda cancelado");
                    activo.setYaAtacoEsteTurno(true);
                    vista.actualizarUI();
                    return;
                }
            }
        }

        // creamos el comando del ataque y lo ejecutamos
        ComandoAtacar cmd = new ComandoAtacar(campo, atacante, defensor, activo, oponente);
        historial.ejecutar(cmd);
        vista.agregarLog(cmd.getLogResultado());

        verificarGanador();
        vista.actualizarUI();
    }

    // el jugador puede deshacer lo que acaba de hacer en este turno
    public void accionDeshacer() {
        if (!historial.puedeDeshacer()) {
            vista.agregarLog("No hay acciones para deshacer en este turno");
            return;
        }
        String desc = historial.descripcionUltimo();
        historial.deshacer();
        vista.agregarLog("Deshecho: " + desc);
        vista.actualizarUI();
    }

    // le preguntamos al otro jugador si quiere activar una trampa para defender
        vista.actualizarUI();
    }

    // le ofrece al defensor activar una trampa antes de que llegue el ataque
    private boolean ofrecerRespuestaTrampas(Jugador defensor, Contexto ctx) {
        List<CartaTrampa> trampas = defensor.getZonaTrampas();
        List<Integer> indices = new ArrayList<>();
        List<String>  nombres = new ArrayList<>();

        for (int i = 0; i < trampas.size(); i++) {
            if (trampas.get(i).puedoActivarme(ctx)) {
                indices.add(i);
                nombres.add((indices.size()) + ". " + trampas.get(i).toString());
            }
        }
        nombres.add("No activar");

        String[] ops = nombres.toArray(new String[0]);
        int elegida = vista.pedirSeleccion("Respuesta de trampas - " + defensor.getNombre(),
            "ATAQUE DECLARADO\n\n" + defensor.getNombre() + " deseas activar una trampa en respuesta\n"
            + "(Si no activas nada el combate se resuelve normalmente)", ops);

        if (elegida < 0 || elegida >= indices.size()) return false;
        int idxReal = indices.get(elegida);
        CartaTrampa trampa = trampas.get(idxReal);
        vista.agregarLog(defensor.getNombre() + " activo trampa en respuesta: " + trampa.getNombre());
        boolean ok = defensor.activarTrampa(idxReal, ctx);
        if (!ok) { vista.agregarLog("La trampa no pudo activarse"); return false; }
        return true;
    }

    public void accionActivarTrampa() {
        Jugador activo   = campo.getJugadorActivo();
        Jugador oponente = campo.getOponente();
        Contexto ctx     = new Contexto(activo, oponente, campo);

        List<CartaTrampa> trampas = activo.getZonaTrampas();
        if (trampas.isEmpty()) { vista.agregarLog("No tienes trampas colocadas"); return; }

        List<Integer> indices = new ArrayList<>();
        List<String>  nombres = new ArrayList<>();
        for (int i = 0; i < trampas.size(); i++) {
            if (trampas.get(i).puedoActivarme(ctx)) {
                indices.add(i);
                nombres.add((i + 1) + ". " + trampas.get(i).toString());
            }
        }
        nombres.add("Cancelar");

        if (indices.isEmpty()) { vista.agregarLog("Ninguna trampa puede activarse ahora"); return; }

        String[] ops = nombres.toArray(new String[0]);
        int posLista = vista.pedirSeleccion("Trampas - " + activo.getNombre(),
            "Elige la trampa a activar:", ops);

        if (posLista < 0 || posLista >= indices.size()) return;
        int idxReal = indices.get(posLista);

        CartaTrampa trampa = trampas.get(idxReal);
        vista.agregarLog(">>> " + activo.getNombre() + " activo trampa: " + trampa.getNombre());
        boolean ok = activo.activarTrampa(idxReal, ctx);
        if (!ok) vista.agregarLog("La trampa no pudo activarse");

        verificarGanador();
        vista.actualizarUI();
    }

    public void accionCambiarPosicion() {
        Jugador activo = campo.getJugadorActivo();
        if (activo.getCampo().isEmpty()) { vista.agregarLog("No tienes monstruos en campo"); return; }

        String[] ops = new String[activo.getCampo().size() + 1];
        for (int i = 0; i < activo.getCampo().size(); i++) ops[i] = (i + 1) + ". " + activo.getCampo().get(i);
        ops[activo.getCampo().size()] = "Cancelar";

        int idx = vista.pedirSeleccion("Cambiar posicion",
            "Elige el monstruo para cambiar posicion:", ops);
        if (idx < 0 || idx >= activo.getCampo().size()) return;

        CartaMonstruo m = activo.getCampo().get(idx);
        m.cambiarPosicion();
        vista.agregarLog(">>> " + m.getNombre() + " cambio a modo " + (m.estaEnModoDefensa() ? "DEFENSA" : "ATAQUE"));
        vista.actualizarUI();
    }

    // el turno del jugador se acaba y le toca al otro
    public void accionTerminarTurno() {
        Jugador terminando = campo.getJugadorActivo();
        vista.agregarLog("-- " + terminando.getNombre() + " termina su turno --");
        // limpiamos el historial porque no se puede deshacer entre turnos
        historial.limpiar();
        campo.terminarTurno();

        if (campo.hayGanador()) { vista.mostrarGanador(); return; }

        // preparamos el turno del siguiente jugador
        String log = campo.prepararTurno();
        vista.agregarLog(log);

        if (campo.hayGanador()) { vista.mostrarGanador(); return; }

        vista.actualizarUI();
        vista.mostrarMensaje("Nuevo Turno",
            "Es el turno de:\n" + campo.getJugadorActivo().getNombre().toUpperCase());
    }

    // guarda el juego actual en un archivo para poder jugar despues
    public void accionGuardarPartida() {
        // creamos una captura del estado del juego
        MementoManager mm = new MementoManager();
        EstadoDuelo snapshot = mm.capturar(campo);

        // le pedimos al usuario el nombre del archivo
        String nombreArchivo = vista.pedirTexto(
            "Guardar partida",
            "Introduce el nombre del archivo para la partida guardada:",
            "duelo_" + System.currentTimeMillis()
        );
        if (nombreArchivo == null) {
            vista.agregarLog("Guardado cancelado");
            return;
        }

        // limpiamos el nombre para que no tenga caracteres raros
        nombreArchivo = normalizarNombreArchivo(nombreArchivo);
        if (nombreArchivo.isBlank()) {
            vista.agregarLog("Nombre de archivo no valido guardado cancelado");
            return;
        }

        // guardamos la partida en un archivo
        boolean ok = GestorPersistencia.getInstance().guardarPartida(snapshot, nombreArchivo);
        if (ok) {
            vista.agregarLog("Partida guardada: " + nombreArchivo + ".txt");
        } else {
            vista.agregarLog("Error al guardar la partida");
        }
    }

    // quita caracteres raros del nombre para que no cause problemas al guardar
    private String normalizarNombreArchivo(String nombreArchivo) {
        String limpio = nombreArchivo.trim();
        // si termina en .txt lo quitamos para que no lo repita
        if (limpio.toLowerCase().endsWith(".txt")) {
            limpio = limpio.substring(0, limpio.length() - 4).trim();
        }
        // reemplazamos los caracteres raros con guiones bajos
        return limpio.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    // revisa si alguien gano el duelo y si gano lo registra
    private void verificarGanador() {
        // preguntamos si hay un ganador
        if (campo.hayGanador()) {
            // obtenemos quien gano
            Jugador ganador = campo.getGanador();
            // si encontramos un ganador lo registramos
            if (ganador != null) {
                // le avisamos al campo que tiene ganador
                campo.notificarGanador(ganador);
                // registramos el resultado en la base de datos para las estadisticas
                GestorPersistencia.getInstance().registrarResultado(
                    campo.getJugador1().getNombre(),
                    campo.getJugador2().getNombre(),
                    ganador.getNombre(),
                    campo.getTurnoActual(),
                    campo.getJugador1().getLp(),
                    campo.getJugador2().getLp()
                );
            }
            // mostramos al ganador en la pantalla
            vista.mostrarGanador();
        }
    }

    // devuelve el campo de batalla
    public CampoBatalla getCampo() { return campo; }
}
