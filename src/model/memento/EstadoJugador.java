// Snapshot del jugador en un punto concreto.
// Guardamos datos simples y nombres de cartas, no objetos reales.
package model.memento;

import java.util.ArrayList;
import java.util.List;

public class EstadoJugador {

    private String nombre;
    private int lp;
    private boolean yaJugoCarta;
    private boolean yaAtaco;
    private boolean bloqueado;

    // nombres de las cartas en cada zona, no referencias directas
    private List<String> mano;
    private List<String> campo; // formato: nombre|nivel|atk|def|puedeAtacar|enDefensa
    private List<String> trampas;
    private List<String> mazo;

    public EstadoJugador(String nombre, int lp, boolean yaJugoCarta, boolean yaAtaco,
                          boolean bloqueado, List<String> mano, List<String> campo,
                          List<String> trampas, List<String> mazo) {
        this.nombre = nombre;
        this.lp = lp;
        this.yaJugoCarta = yaJugoCarta;
        this.yaAtaco = yaAtaco;
        this.bloqueado = bloqueado;

        // Copiamos las listas para no tocar la original.
        // Así el snapshot queda fijo y no se rompe por accidente.
        this.mano = new ArrayList<>(mano);
        this.campo = new ArrayList<>(campo);
        this.trampas = new ArrayList<>(trampas);
        this.mazo = new ArrayList<>(mazo);
    }

    // Estos getters sirven para leer el snapshot del jugador.
    public String getNombre() { return nombre; }
    public int getLp() { return lp; }
    public boolean isYaJugoCarta() { return yaJugoCarta; }
    public boolean isYaAtaco() { return yaAtaco; }
    public boolean isBloqueado() { return bloqueado; }
    public List<String> getMano() { return mano; }
    public List<String> getCampo() { return campo; }
    public List<String> getTrampas() { return trampas; }
    public List<String> getMazo() { return mazo; }
}
