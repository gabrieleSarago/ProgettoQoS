/*
 * Entita.java
 *
 * Created on 10 ottobre 2007, 12.48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package base_simulator.DV;

/**
 *
 * @author Ing. Amilcare-Francesco Santamaria
 */
public class Entita {

    protected schedulerDV s;
    protected String tipo;

    /**
     * Creates a new instance of Entita
     */
    public Entita(schedulerDV s, String tipo) {
        this.tipo = tipo;
        this.s = s;
    }

    public void Handler(Messaggi m) {

    }

    public String toString() {
        return this.tipo;
    }

    public String getTipo() {
        return this.tipo;
    }

    public String getStat() {
        String s = "\nEntita";
        return s;
    }

    public void stampaInformazione(String tipo_messaggio, int id, String messaggio) {
        System.out.println(tipo_messaggio + ":" + s.orologio.getCurrent_Time() + " Tipo Nodo:" + this.tipo + " ID:" + id + " Messaggio:" + messaggio);
    }
}
