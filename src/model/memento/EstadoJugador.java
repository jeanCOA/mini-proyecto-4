package model.memento;

import java.util.ArrayList;
import java.util.List;

// Snapshot del estado de un jugador para el patron Memento
// Guarda todo en texto (nombres de cartas) para poder serializar/deserializar facil
public class EstadoJugador {

    private final String nombre;
    private final int lp;
    private final boolean yaJugoCarta;
    private final boolean yaAtaco;
    private final boolean bloqueado;
    private final List<String> mano;
    private final List<String> campo;
    private final List<String> trampas;
    private final List<String> mazo;

    public EstadoJugador(String nombre, int lp, boolean yaJugoCarta, boolean yaAtaco,
                         boolean bloqueado, List<String> mano, List<String> campo,
                         List<String> trampas, List<String> mazo) {
        this.nombre      = nombre;
        this.lp          = lp;
        this.yaJugoCarta = yaJugoCarta;
        this.yaAtaco     = yaAtaco;
        this.bloqueado   = bloqueado;
        this.mano        = new ArrayList<>(mano);
        this.campo       = new ArrayList<>(campo);
        this.trampas     = new ArrayList<>(trampas);
        this.mazo        = new ArrayList<>(mazo);
    }

    public String getNombre()      { return nombre; }
    public int getLp()             { return lp; }
    public boolean isYaJugoCarta() { return yaJugoCarta; }
    public boolean isYaAtaco()     { return yaAtaco; }
    public boolean isBloqueado()   { return bloqueado; }
    public List<String> getMano()    { return new ArrayList<>(mano); }
    public List<String> getCampo()   { return new ArrayList<>(campo); }
    public List<String> getTrampas() { return new ArrayList<>(trampas); }
    public List<String> getMazo()    { return new ArrayList<>(mazo); }
}
