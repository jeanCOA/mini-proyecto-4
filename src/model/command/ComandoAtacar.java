// comando que hace que un monstruo ataque a otro
// guarda todo lo que paso antes para poder deshacer el combate si falla
package model.command;

import model.*;

public class ComandoAtacar implements IComando {

    private CampoBatalla campo;
    private CartaMonstruo atacante;
    private CartaMonstruo defensor; // puede ser null si es ataque directo sin defensor
    private Jugador jugActivo;
    private Jugador oponente;

    // estos guardan como estaba todo antes del combate por si hay que deshacer
    private int lpActivoAntes;
    private int lpOponenteAntes;
    private boolean atacanteExistia;
    private boolean defensorExistia;

    private String logResultado;

    public ComandoAtacar(CampoBatalla campo, CartaMonstruo atacante, CartaMonstruo defensor,
                          Jugador jugActivo, Jugador oponente) {
        this.campo = campo;
        this.atacante = atacante;
        this.defensor = defensor;
        this.jugActivo = jugActivo;
        this.oponente = oponente;
    }

    @Override
    // ejecuta el ataque y guarda como estaba todo antes
    public void ejecutar() {
        // guardamos los LP y el estado de los monstruos antes de atacar
        lpActivoAntes  = jugActivo.getLp();
        lpOponenteAntes = oponente.getLp();
        atacanteExistia = jugActivo.getCampo().contains(atacante);
        defensorExistia = defensor != null && oponente.getCampo().contains(defensor);

        // si no hay monstruo para defender o defendor es null es ataque directo
        if (oponente.getCampo().isEmpty() || defensor == null) {
            logResultado = campo.ataqueDirecto(atacante, oponente);
        } else {
            // sino, los monstruos se pelean entre si
            logResultado = campo.resolverCombate(atacante, defensor, jugActivo, oponente);
        }

        // marcamos que ya ataco este turno
        jugActivo.setYaAtacoEsteTurno(true);
    }

    @Override
    // deshace el ataque y vuelve todo como estaba antes
    public void deshacer() {
        // los LP vuelven a lo que eran antes del combate
        jugActivo.setLp(lpActivoAntes);
        oponente.setLp(lpOponenteAntes);

        // si el atacante fue destruido lo traemos de vuelta
        if (atacanteExistia && !jugActivo.getCampo().contains(atacante)) {
            jugActivo.getCampo().add(atacante);
            atacante.setPuedeAtacar(true);
        } else {
            // si no fue destruido solo lo hacemos poder atacar de nuevo
            atacante.setPuedeAtacar(true);
        }

        // si el defensor fue destruido tambien lo traemos de vuelta
        if (defensorExistia && !oponente.getCampo().contains(defensor)) {
            oponente.getCampo().add(defensor);
        }

        // y marcamos que ya no ataco
        jugActivo.setYaAtacoEsteTurno(false);
    }

    @Override
    // devuelve un texto que dice que paso el ataque
    public String getDescripcion() {
        String obj = defensor != null ? defensor.getNombre() : "ataque directo";
        return "Atacar: " + atacante.getNombre() + " -> " + obj;
    }

    // devuelve lo que paso en el combate para mostrar en el log
    public String getLogResultado() { 
        return logResultado; 
    }
}
