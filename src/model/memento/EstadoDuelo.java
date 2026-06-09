package model.memento;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Snapshot completo del duelo para el patron Memento
// Contiene el estado de los dos jugadores y los datos del turno
public class EstadoDuelo {

    private final EstadoJugador estadoJ1;
    private final EstadoJugador estadoJ2;
    private final int turnoActual;
    private final boolean esPrimerTurno;
    private final String nombreJugadorActivo;
    private final String timestamp;

    public EstadoDuelo(EstadoJugador estadoJ1, EstadoJugador estadoJ2,
                       int turnoActual, boolean esPrimerTurno, String nombreJugadorActivo) {
        this.estadoJ1            = estadoJ1;
        this.estadoJ2            = estadoJ2;
        this.turnoActual         = turnoActual;
        this.esPrimerTurno       = esPrimerTurno;
        this.nombreJugadorActivo = nombreJugadorActivo;
        this.timestamp           = LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public EstadoJugador getEstadoJ1()          { return estadoJ1; }
    public EstadoJugador getEstadoJ2()          { return estadoJ2; }
    public int getTurnoActual()                 { return turnoActual; }
    public boolean isEsPrimerTurno()            { return esPrimerTurno; }
    public String getNombreJugadorActivo()      { return nombreJugadorActivo; }
    public String getTimestamp()                { return timestamp; }
}
