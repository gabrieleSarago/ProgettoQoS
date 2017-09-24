/*
 * trace.java
 *
 * Created on 9 ottobre 2007, 15.46
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package base_simulator.DV;

import java.util.*;

/**
 * Classe per il tracing delle attività sugli oggetti
 *
 * @author franco
 */
public class trace {

    /*Indica se voglio inserire dei filtri sugli oggetti*/
    public boolean filtered;
    /*Lo schedulerDV del simulatore*/
    private schedulerDV s;
    public Vector<Object> Objects;
    public Vector<Object> FilteredObjects;

    /**
     * Creates a new instance of trace
     */
    public trace(schedulerDV s) {
        this.s = s;
        filtered = false;
        Objects = new Vector<Object>();
        FilteredObjects = new Vector<Object>();
    }

    public String getTrace(Messaggi m) throws NoSuchMethodException {
        String s = "";
        boolean stampa = false;
        if (filtered) {
            if (efiltrato(m.getSorgente()) || efiltrato(m.getDestinazione())) {
                stampa = true;
            }
        } else {
            stampa = true;
        }

        if (stampa) {

            Entita sorgente = (Entita) m.getSorgente();
            Entita destinazione = (Entita) m.getDestinazione();

            s += "\n" + m.getTempo_spedizione().getCurrent_Time() + "  " + m.getTipo_Messaggio() + "  " + sorgente + "  " + destinazione;
        }
        return s;
    }

    public boolean efiltrato(Object o) {
        boolean filtered = false;
        int cont = 0;
        while (!filtered && (cont < this.FilteredObjects.size())) {
            NodoDV n = (NodoDV) this.FilteredObjects.elementAt(cont);
            if (n.getId() == ((NodoDV) o).getId()) {
                filtered = true;
            }
            cont++;
        }
        return filtered;
    }
}
