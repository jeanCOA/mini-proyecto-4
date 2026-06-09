// Fabrica que crea todos los tipos de cartas del mazo
// aqui se arma el mazo completo con monstruos magicas y trampas
package model;

import java.util.ArrayList;
import java.util.List;
import model.cards.magic.*;
import model.cards.trap.*;

public class FabricaDeCartas {

    // crea la lista completa del mazo con todos los tipos de cartas
    public static List<Carta> crearMazoCompleto() {
        List<Carta> mazo = new ArrayList<>();
        // agregar monstruos, magicas y trampas al mazo completo
        mazo.addAll(crearMonstruos());
        mazo.addAll(crearMagicas());
        mazo.addAll(crearTrampas());
        return mazo;
    }

    // crea todas las cartas de monstruo del mazo
    public static List<CartaMonstruo> crearMonstruos() {
        List<CartaMonstruo> lista = new ArrayList<>();
        // crear varias copias del mismo monstruo para el mazo
        for (int i = 0; i < 6; i++)
            lista.add(new CartaMonstruo("Guerrero De La Luz",  (byte) 3, (short) 1200, (short) 1000));
        for (int i = 0; i < 6; i++)
            lista.add(new CartaMonstruo("Bestia del Bosque",   (byte) 4, (short) 1500, (short) 1200));
        for (int i = 0; i < 5; i++)
            lista.add(new CartaMonstruo("Guardian del Hierro", (byte) 5, (short) 1000, (short) 2000));
        for (int i = 0; i < 5; i++)
            lista.add(new CartaMonstruo("Hechicero del Caos",  (byte) 4, (short) 1800, (short) 1500));
        for (int i = 0; i < 5; i++)
            lista.add(new CartaMonstruo("Caballero Real",      (byte) 6, (short) 2300, (short) 2000));
        for (int i = 0; i < 3; i++)
            lista.add(new CartaMonstruo("Dragon Ancestral",    (byte) 8, (short) 3000, (short) 2500));
        return lista;
    }

    // crea las cartas magicas que se pueden activar
    public static List<CartaMagica> crearMagicas() {
        List<CartaMagica> lista = new ArrayList<>();
        // cada carta magica se instancia y se agrega al mazo
        lista.add(new PotOfGreed());
        lista.add(new PotOfGreed());
        lista.add(new EspadaDeZeus());
        lista.add(new EspadaDeZeus());
        lista.add(new EscudoDeAtenea());
        lista.add(new EscudoDeAtenea());
        lista.add(new CuraMilagrosa());
        lista.add(new CuraMilagrosa());
        lista.add(new Fisura());
        lista.add(new LlamadaDelAbismo());
        return lista;
    }

    // crea una carta individual por nombre — usada al restaurar partidas guardadas
    public static Carta crearCarta(String nombre) {
        if (nombre == null) return null;
        switch (nombre.trim()) {
            // Monstruos
            case "Guerrero De La Luz":  return new CartaMonstruo("Guerrero De La Luz",  (byte) 3, (short) 1200, (short) 1000);
            case "Bestia del Bosque":   return new CartaMonstruo("Bestia del Bosque",   (byte) 4, (short) 1500, (short) 1200);
            case "Guardian del Hierro": return new CartaMonstruo("Guardian del Hierro", (byte) 5, (short) 1000, (short) 2000);
            case "Hechicero del Caos":  return new CartaMonstruo("Hechicero del Caos",  (byte) 4, (short) 1800, (short) 1500);
            case "Caballero Real":      return new CartaMonstruo("Caballero Real",       (byte) 6, (short) 2300, (short) 2000);
            case "Dragon Ancestral":    return new CartaMonstruo("Dragon Ancestral",     (byte) 8, (short) 3000, (short) 2500);
            // Magicas
            case "Pot Of Greed":        return new model.cards.magic.PotOfGreed();
            case "PotOfGreed":          return new model.cards.magic.PotOfGreed();
            case "Espada De Zeus":      return new model.cards.magic.EspadaDeZeus();
            case "EspadaDeZeus":        return new model.cards.magic.EspadaDeZeus();
            case "Escudo De Atenea":    return new model.cards.magic.EscudoDeAtenea();
            case "EscudoDeAtenea":      return new model.cards.magic.EscudoDeAtenea();
            case "Cura Milagrosa":      return new model.cards.magic.CuraMilagrosa();
            case "CuraMilagrosa":       return new model.cards.magic.CuraMilagrosa();
            case "Fisura":              return new model.cards.magic.Fisura();
            case "Llamada Del Abismo":  return new model.cards.magic.LlamadaDelAbismo();
            case "LlamadaDelAbismo":    return new model.cards.magic.LlamadaDelAbismo();
            // Trampas
            case "Contra Ataque":       return new model.cards.trap.ContraAtaque();
            case "ContraAtaque":        return new model.cards.trap.ContraAtaque();
            case "Campo Minado":        return new model.cards.trap.CampoMinado();
            case "CampoMinado":         return new model.cards.trap.CampoMinado();
            case "Reflejo Magico":      return new model.cards.trap.ReflejoMagico();
            case "ReflejoMagico":       return new model.cards.trap.ReflejoMagico();
            case "Renacer Del Fenix":   return new model.cards.trap.RenacerDelFenix();
            case "RenacerDelFenix":     return new model.cards.trap.RenacerDelFenix();
            case "Tormenta De Truenos": return new model.cards.trap.TormentaDeTruenos();
            case "TormentaDeTruenos":   return new model.cards.trap.TormentaDeTruenos();
            case "Destino Inexorable":  return new model.cards.trap.DestinoInexorable();
            case "DestinoInexorable":   return new model.cards.trap.DestinoInexorable();
            case "Bolt Divino":         return new model.cards.trap.BoltDivino();
            case "BoltDivino":          return new model.cards.trap.BoltDivino();
            case "Robo Forzado":        return new model.cards.trap.RoboForzado();
            case "RoboForzado":         return new model.cards.trap.RoboForzado();
            case "Escudo Sagrado":      return new model.cards.trap.EscudoSagrado();
            case "EscudoSagrado":       return new model.cards.trap.EscudoSagrado();
            case "Espejo De Almas":     return new model.cards.trap.EspejoDeAlmas();
            case "EspejoDeAlmas":       return new model.cards.trap.EspejoDeAlmas();
            default:
                System.err.println("FabricaDeCartas: carta desconocida al restaurar: " + nombre);
                return null;
        }
    }

    // crea las cartas trampa que se colocan en el campo
    public static List<CartaTrampa> crearTrampas() {
        List<CartaTrampa> lista = new ArrayList<>();
        // cada trampa se instancia y se agrega a la lista final
        lista.add(new ContraAtaque());
        lista.add(new CampoMinado());
        lista.add(new ReflejoMagico());
        lista.add(new RenacerDelFenix());
        lista.add(new TormentaDeTruenos());
        lista.add(new DestinoInexorable());
        lista.add(new BoltDivino());
        lista.add(new RoboForzado());
        lista.add(new EscudoSagrado());
        lista.add(new EspejoDeAlmas());
        return lista;
    }
}
