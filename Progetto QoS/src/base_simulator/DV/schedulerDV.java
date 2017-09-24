/*
 * schedulerDV.java
 *
 * Created on 8 ottobre 2007, 14.07
 * Last Edit : 15.11.2013
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package base_simulator.DV;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.lang.reflect.Method;
import java.util.Date;

/**
 *
 * @author franco
 */
public class schedulerDV implements Runnable {

    /**
     * Creates a new instance of schedulerDV
     */
    //Tutti i messaggi devono essere immessi in questo buffer per poter essere gestiti
    private ArrayList<Messaggi> buffer; // Eventi da gestire

    /*Cicla viene utilizzato per mettere in pausa la simulazione*/
    private boolean cicla;
    /*Indica se si vuole avere il trace della simulazione*/
    private boolean traced;
    /*L'oggetto che scrive il trace della simulazione*/
    private trace Trace;
    /*Orologio del simulatore*/
    public Timer orologio;
    /*Indica l'orizzonte temporale della simulazione*/
    double max_Time;

    /**
     * Serve per risolvere gli indirizzi tipo DNS
     */
//    public ArrayList<appartenenzaHostNodi> appartenenze;
    private int id_servizio = 0;
    private int id_Multicast;

    private Date d;

    private InfosDV info;

    public void setInfo(InfosDV info) {
        this.info = info;
    }

    public InfosDV getInfo() {
        return info;
    }

    private Grafo grafo;

    public Grafo getGrafo() {
        return grafo;
    }

    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
    }

    public int getId_servizio() {
        id_servizio++;
        return id_servizio;
    }

    public void setId_servizio(int id_servizio) {
        this.id_servizio = id_servizio;
    }

    public schedulerDV(double orizzonte_temporale, boolean traced) {
        id_Multicast = 1;
        buffer = new ArrayList<Messaggi>();
        this.traced = traced;
        cicla = true;
        orologio = new Timer();
        this.max_Time = orizzonte_temporale;
        if (traced) {
            Trace = new trace(this);
        }
    }

    public void addMulticastSession() {
        this.id_Multicast++;
    }

    public int getId_Multicast() {
        return id_Multicast;
    }

    public void run() {
        System.out.println("La simulazione sta per iniziare");
        System.out.println("All' ora...:" + getData());

        while (orologio.getCurrent_Time() <= max_Time && buffer.size() > 0) {

            if (cicla) {
                Messaggi m = buffer.get(0);
                if (traced) {
                    String s = null;
                    try {
                        s = Trace.getTrace(m);
                    } catch (NoSuchMethodException ex) {
                        ex.printStackTrace();
                    }
                    System.out.println(s);
                }

                buffer.remove(0);
                //Avanzo l'orologio del simulatore'
                orologio.setCurrent_Time(m.getTempo_spedizione().getCurrent_Time());
                //Faccio scattare l'evento sul nodo destinazione'
                lanciaHandler(m);

            }

        }

        //Simulazione terminata stampiamo le statistiche dei nodi interessati
        info.stampaStatisticheNodo();

        //Solo aggiornamento interfaccia grafica
        while (buffer.isEmpty() && orologio.getCurrent_Time() <= max_Time) {
            this.orologio.shifta(10000);

        }

        System.out.println("\nLa simulazione è terminata il :" + getData());

    }

    public void setCicla() {
        if (cicla) {
            cicla = false;
        } else {
            cicla = true;
        }
    }

    public synchronized void insertMessage(Messaggi msg) {
        int posto = -1;
        boolean trovato = false;
        for (int i = 0; i < buffer.size() && !trovato; i++) {
            if (buffer.get(i).getTempo_spedizione().getCurrent_Time() > msg.getTempo_spedizione().getCurrent_Time()) {
                trovato = true;
                posto = i;
            }
        }
        if (trovato) {
            buffer.add(posto, msg);
        } else {
            buffer.add(msg);
        }
    }//metodo insertMessage(Messaggi msg)

    /**
     * Utilizzo della reflection
     */
    // Attraverso questo metodo � possibile andare sul nodo destinazione e lanciare il 
    // Metodo Handler
    private void lanciaHandler(Messaggi m) {
        Method m2 = null;
        try {
            Class[] args1 = new Class[1];
            args1[0] = Messaggi.class;
            m2 = m.getDestinazione().getClass().getDeclaredMethod("Handler", args1);

        } catch (SecurityException ex) {
        } catch (NoSuchMethodException ex) {
        }

        try {

            m2.invoke(m.getDestinazione(), m);
        } catch (IllegalArgumentException ex) {
        } catch (InvocationTargetException ex) {
        } catch (IllegalAccessException ex) {
        }

    }

    private String getData() {
        d = new Date();

        int giorno = d.getDate();
        int mese = d.getMonth() + 1;
        int anno = d.getYear() + 1900;

        int ora = d.getHours();
        int minuti = d.getMinutes();
        int secondi = d.getSeconds();

        String s = giorno + "/" + mese + "/" + anno + " ora " + ora + ":" + minuti + ":" + secondi;
        return s;
    }

}
