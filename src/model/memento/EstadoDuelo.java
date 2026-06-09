// Snapshot del duelo en un punto fijo del juego.
package model.memento;

public class EstadoDuelo {

    private final EstadoJugador estadoJ1;
    private final EstadoJugador estadoJ2;
    private final int turnoActual;
    private final boolean esPrimerTurno;
    private final String nombreJugadorActivo;
    private final long timestamp;

    // Guardamos la hora para saber cuándo se hizo este backup del duelo.
    public EstadoDuelo(EstadoJugador estadoJ1, EstadoJugador estadoJ2,
                        int turnoActual, boolean esPrimerTurno,
                        String nombreJugadorActivo) {
        this.estadoJ1 = estadoJ1;
        this.estadoJ2 = estadoJ2;
        this.turnoActual = turnoActual;
        this.esPrimerTurno = esPrimerTurno;
        this.nombreJugadorActivo = nombreJugadorActivo;
        this.timestamp = System.currentTimeMillis();
    }

    public EstadoJugador getEstadoJ1() { return estadoJ1; }
    public EstadoJugador getEstadoJ2() { return estadoJ2; }
    public int getTurnoActual() { return turnoActual; }
    public boolean isEsPrimerTurno() { return esPrimerTurno; }
    public String getNombreJugadorActivo() { return nombreJugadorActivo; }
    public long getTimestamp() { return timestamp; }
}
