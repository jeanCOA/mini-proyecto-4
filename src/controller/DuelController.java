package controller;

import java.util.ArrayList;
import java.util.List;
import model.*;
import model.command.*;
import model.memento.*;
import utils.GestorPersistencia;
import view.IDuelView;

// controlador principal del duelo
// conecta la vista con el modelo y maneja todas las acciones del jugador
public class DuelController implements ObservadorDuelo {

    private final CampoBatalla campo;
    private IDuelView vista;

    // historial de comandos para poder deshacer la ultima jugada
    private final HistorialComandos historial;
    private final MementoManager mementoManager;

    public DuelController(CampoBatalla campo) {
        this.campo = campo;
        this.historial = new HistorialComandos();
        this.mementoManager = new MementoManager();
        // nos suscribimos al campo para recibir sus eventos
        campo.registrarObservador(this);
    }

    public void setVista(IDuelView vista) {
        this.vista = vista;
    }

    // el campo nos avisa de lo que pasa y aqui decidimos que hacer con eso
    @Override
    public void onEventoDuelo(String tipoEvento, String detalle) {
        switch (tipoEvento) {
            case "GANADOR":
                if (vista != null) vista.mostrarGanador();
                break;
            case "SIN_MAZO":
                if (vista != null) vista.agregarLog(detalle + " se quedo sin mazo");
                break;
            case "INICIO_TURNO":
                // turno nuevo = limpiamos el historial de undo
                if (vista != null) historial.limpiar();
                break;
            default:
                break;
        }
    }

    public void iniciarPrimerTurno() {
        String log = campo.prepararTurno();
        vista.agregarLog(log);
        vista.actualizarUI();
    }

    // jugar carta usando el patron Command para poder deshacerlo
    public void accionJugarCarta() {
        Jugador activo   = campo.getJugadorActivo();
        Jugador oponente = campo.getOponente();
        Contexto ctx     = new Contexto(activo, oponente, campo);
        List<Carta> mano = activo.getMano();

        if (mano.isEmpty()) { vista.agregarLog("No tienes cartas en la mano"); return; }
        if (activo.isYaJugoCartaEsteTurno()) { vista.agregarLog("Ya jugaste una carta este turno"); return; }

        String[] opciones = new String[mano.size() + 1];
        for (int i = 0; i < mano.size(); i++) opciones[i] = (i + 1) + ". " + mano.get(i).toString();
        opciones[mano.size()] = "Cancelar";

        int idx = vista.pedirSeleccion(" Tu mano - " + activo.getNombre(),
            "Elige una carta para jugar:", opciones);
        if (idx < 0 || idx >= mano.size()) return;

        Carta carta = mano.get(idx);
        int indiceSacrificio = -1;

        // si el monstruo es de nivel alto necesita sacrificio
        if (carta.getTipo().equals("MONSTRUO")) {
            CartaMonstruo mon = (CartaMonstruo) carta;
            if (mon.getnivelCarta() > 4) {
                if (activo.getCampo().isEmpty()) {
                    vista.agregarLog("Necesitas sacrificar un monstruo para invocar " + mon.getNombre()
                             + " (nivel " + mon.getnivelCarta() + ") pero no tienes monstruos en campo");
                    return;
                }
                String[] opSac = new String[activo.getCampo().size()];
                for (int i = 0; i < activo.getCampo().size(); i++)
                    opSac[i] = (i + 1) + ". " + activo.getCampo().get(i).getNombre();
                indiceSacrificio = vista.pedirSeleccion("Sacrificio requerido",
                    mon.getNombre() + " (Lv" + mon.getnivelCarta() + ") requiere un sacrificio\nElige el monstruo a sacrificar:",
                    opSac);
                if (indiceSacrificio < 0) return;
            }
        }

        // creamos el comando y lo ejecutamos
        ComandoJugarCarta cmd = new ComandoJugarCarta(activo, ctx, idx, indiceSacrificio);
        historial.ejecutar(cmd);

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

    // ataque usando Command igual que con jugar carta
    public void accionAtacar() {
        Jugador activo   = campo.getJugadorActivo();
        Jugador oponente = campo.getOponente();

        List<CartaMonstruo> disponibles = new ArrayList<>();
        for (CartaMonstruo m : activo.getCampo()) if (m.puedeAtacar()) disponibles.add(m);

        if (disponibles.isEmpty()) { vista.agregarLog("Ningun monstruo puede atacar"); return; }
        if (activo.isYaAtacoEsteTurno()) { vista.agregarLog("Ya atacaste este turno"); return; }

        String[] opAtacantes = new String[disponibles.size() + 1];
        for (int i = 0; i < disponibles.size(); i++) opAtacantes[i] = (i + 1) + ". " + disponibles.get(i);
        opAtacantes[disponibles.size()] = "Cancelar";

        int idxAtac = vista.pedirSeleccion("Ataque - " + activo.getNombre(),
            "Elige el monstruo ATACANTE:", opAtacantes);
        if (idxAtac < 0 || idxAtac >= disponibles.size()) return;
        CartaMonstruo atacante = disponibles.get(idxAtac);

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

        // antes del ataque le preguntamos al oponente si quiere activar una trampa
        Contexto ctxDefensa = new Contexto(oponente, activo, campo);
        ctxDefensa.setMonstruoAtacante(atacante);

        if (oponente.hayTrampaActivable(ctxDefensa)) {
            boolean activoTrampa = ofrecerRespuestaTrampas(oponente, ctxDefensa);
            if (activoTrampa) {
                verificarGanador();
                if (campo.hayGanador()) { vista.actualizarUI(); return; }
                if (!activo.getCampo().contains(atacante)) {
                    vista.agregarLog(atacante.getNombre() + " fue destruido el ataque queda cancelado");
                    activo.setYaAtacoEsteTurno(true);
                    vista.actualizarUI();
                    return;
                }
            }
        }

        ComandoAtacar cmd = new ComandoAtacar(campo, atacante, defensor, activo, oponente);
        historial.ejecutar(cmd);
        vista.agregarLog(cmd.getLogResultado());

        verificarGanador();
        vista.actualizarUI();
    }

    // deshace la ultima accion del turno si se puede
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

    public void accionTerminarTurno() {
        Jugador terminando = campo.getJugadorActivo();
        vista.agregarLog("-- " + terminando.getNombre() + " termina su turno --");
        historial.limpiar(); // no se puede deshacer entre turnos
        campo.terminarTurno();

        if (campo.hayGanador()) { vista.mostrarGanador(); return; }

        String log = campo.prepararTurno();
        vista.agregarLog(log);

        if (campo.hayGanador()) { vista.mostrarGanador(); return; }

        vista.actualizarUI();
        vista.mostrarMensaje("Nuevo Turno",
            "Es el turno de:\n" + campo.getJugadorActivo().getNombre().toUpperCase());
    }

    // guarda la partida actual con Memento + GestorPersistencia
    public void accionGuardarPartida() {
        MementoManager mm = new MementoManager();
        EstadoDuelo snapshot = mm.capturar(campo);

        String nombreArchivo = vista.pedirTexto(
            "Guardar partida",
            "Introduce el nombre del archivo para la partida guardada:",
            "duelo_" + System.currentTimeMillis()
        );
        if (nombreArchivo == null) {
            vista.agregarLog("Guardado cancelado");
            return;
        }

        nombreArchivo = normalizarNombreArchivo(nombreArchivo);
        if (nombreArchivo.isBlank()) {
            vista.agregarLog("Nombre de archivo no valido guardado cancelado");
            return;
        }

        boolean ok = GestorPersistencia.getInstance().guardarPartida(snapshot, nombreArchivo);
        if (ok) {
            vista.agregarLog("Partida guardada: " + nombreArchivo + ".txt");
        } else {
            vista.agregarLog("Error al guardar la partida");
        }
    }

    // limpia caracteres raros del nombre del archivo para que no rompa el sistema
    private String normalizarNombreArchivo(String nombreArchivo) {
        String limpio = nombreArchivo.trim();
        if (limpio.toLowerCase().endsWith(".txt")) {
            limpio = limpio.substring(0, limpio.length() - 4).trim();
        }
        return limpio.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    // checa si hay ganador y si hay lo registra y avisa a la vista
    private void verificarGanador() {
        if (campo.hayGanador()) {
            Jugador ganador = campo.getGanador();
            if (ganador != null) {
                campo.notificarGanador(ganador);
                GestorPersistencia.getInstance().registrarResultado(
                    campo.getJugador1().getNombre(),
                    campo.getJugador2().getNombre(),
                    ganador.getNombre(),
                    campo.getTurnoActual(),
                    campo.getJugador1().getLp(),
                    campo.getJugador2().getLp()
                );
            }
            vista.mostrarGanador();
        }
    }

    public CampoBatalla getCampo() { return campo; }
}
