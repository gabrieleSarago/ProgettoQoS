/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base_simulator.layers;

import base_simulator.Messaggi;
import base_simulator.Nodo;
import base_simulator.scheduler;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author AFS
 */
public class LinkLayerStopAndWait extends LinkLayer {

    final int IDLE = 0;
    final int BUSY = 1;

    final String SVUOTA_CODA = "SVUOTA_CODA";
    final String TIME_OUT = "TIME_OUT";
    final String ACK_ARQ = "ACK_ARQ";

    //Variabile per il controllo del UID di ARQ
    int ID_msg_arq = 0;

    //Buffer di appoggio per lo store&Forward dei pacchetti
    ArrayList<Messaggi> buffer;
    
    //Dimensione massima del buffer in termini di pacchetti
    int bufferSize = 500;
    
    /**
     * Tempo di timeout impostato staticamente:
     * TODO : 
     * 1. Generare il metodo di controllo del Timeout mettendolo in
     * relazione con il tempo di ciclo.
     * 
     * 2. Effettuare la raccolta delle
     * statistiche per evidenziare l'andamento temporale
     */
    private double timeout = 1000.0; //Settato inizialmente ad 1 sec
    
    /**
     * TODO:
     * 1. Inserire il calcolo del tempo di ciclo medio
     * 2. Mettere in relazione il tempo di ciclo con il Timeout
     */
    private double tempo_ciclo = 1000.0; 
    
    //Alcune variabili statistiche
    private int packetDropped = 0;
    private int packetResend = 0;
    private int duplicatedAck = 0;
    private int status = 0;

    double latenza_media;
    double count_latenza;
    
    
    
    BufferedWriter bw = null;

    /**
     * 
     * @param s
     * @param tempo_processamento : Tempo di processamento del livello
     * @param trace_id : Utilizzato per la memorizzazione dei dati di log su file
     * 
     * TODO:
     * Spostare trace_id in un metodo apposito per la gestione e inizializzazione dei log
     */
    public LinkLayerStopAndWait(scheduler s, double tempo_processamento, int trace_id) {
        super(s, tempo_processamento);
        buffer = new ArrayList<>();
        latenza_media = 0;
        count_latenza = 0;
        try {
            bw = new BufferedWriter(new FileWriter("LinkLayerStopAndWait_" + trace_id + ".csv"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Gestore dei messaggi dedicati al LinkLayer stop&wait
     * @param m 
     */
    @Override
    public void Handler(Messaggi m) {

        if (m.getTipo_Messaggio().equals(SVUOTA_CODA)) {
            if (buffer.size() > 0) {
                Messaggi m1 = null;
                try {
                    m1 = (Messaggi) (buffer.get(0).clone());
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(LinkLayerStopAndWait.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (m1 != null) {
                    try {
                        if (bw != null) {
                            bw.append(s.orologio.getCurrent_Time() + ";"
                                    + ((Nodo) this.nodo).getId() + ";"
                                    + "Invio Messaggio;"
                                    + m1.getTipo_Messaggio()+";"
                                    + "ARQ_ID:"+ID_msg_arq + ";"
                                    + "NEXT_HOP:"+m1.getNextHop_id() + ";"                                    
                                    + "\n"
                            );
                            // scrivo nel log 
                            getLogger().info("{} ; {} ; Invio Messaggio; {} ; ARQ_ID: {} ; NEXT_HOP: {} ;",s.orologio.getCurrent_Time(),((Nodo) this.nodo).getId(),m1.getTipo_Messaggio(),ID_msg_arq,m1.getNextHop_id());
                            // fine log 
                            bw.flush();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    m1.setIDArq(ID_msg_arq);
                    m1.setNodoPrecedente(this.nodo);
                    super.Handler(m1);
                    generaMessaggioTimeout();
                    this.status = BUSY;
                } else {
                    this.status = IDLE;
                }
            } else {
                this.status = IDLE;
            }
        } else if (m.getTipo_Messaggio().equals(TIME_OUT)) {

            if (m.getIDArq() == ID_msg_arq) {
                //E'' scattato Timeout per id messaggio id_msg_arq
                try {
                    if (bw != null) {
                        bw.append(s.orologio.getCurrent_Time() + ";"
                                + "Timeout su Nodo:"+((Nodo) this.nodo).getId() + ";"
                                + "Timeout;"
                                + "ARQ_ID:"+ID_msg_arq + ";"
                                + "Aspettato da:"+m.getNextHop_id() + ";"                                
                                + "\n"
                        );
                        bw.flush();
                        //loggo
                        getLogger().info("{} ; Timeout su nodo: {} ; Timeout; ARQ_ID: {} ; Aspettato da {} ; ",s.orologio.getCurrent_Time(),((Nodo) this.nodo).getId(),ID_msg_arq,m.getNextHop_id());
                        // fine log 
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                packetResend++;
                generaMessaggioSvuotaCoda();
            }
        } else if (m.saliPilaProtocollare == false) {
            if (this.status == IDLE) {
                if (buffer.size() < bufferSize) {
                    buffer.add(m);
                    this.status = BUSY;
                    generaMessaggioSvuotaCoda();
                } else {
                    packetDropped++;
                }
            } else if (buffer.size() < bufferSize) {
                buffer.add(m);
            } else {
                packetDropped++;
            }
        } else if (m.getTipo_Messaggio().equals(ACK_ARQ)) {
            if (m.getIDArq() < ID_msg_arq) {
                //This message is dropped because already received an ack
                duplicatedAck++;
            } else {
                try {
                    if (bw != null) {
                        bw.append(s.orologio.getCurrent_Time() + ";"
                                + "ACK Ricevuto da..:"+((Nodo) this.nodo).getId() + ";"
                                + "Ricevuto Messaggio da :"+((Nodo)m.getNodoSorgente()).getId()+";"
                                + "Tipo Messaggio:"+m.getTipo_Messaggio()+";"
                                + "ARQ_ID -> m:" + m.getIDArq() + "/ n:" + ID_msg_arq + ";"                                
                                + "\n"
                        );
                        bw.flush();
                        getLogger().info("{} ; ACK Ricevuto da..:{};Ricevuto Messaggio da : {}; Tipo Messaggio {}; ARQ_ID ->m: {}  {} ;",s.orologio.getCurrent_Time(),((Nodo) this.nodo).getId(),((Nodo)m.getNodoSorgente()).getId(),m.getTipo_Messaggio(),m.getIDArq(),ID_msg_arq);
                        
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                buffer.remove(0);
                ID_msg_arq++;
                if (status == IDLE) {
                    this.status = BUSY;
                    
                }
                generaMessaggioSvuotaCoda(); //questo non va nell'if?
            }
        } else {
            //Per qualsiasi messaggio entrante dobbiamo rispondere con un ACK al nodo che lo ha inviato
            rispondiConAck(m.getIDArq(), (Nodo) m.getNodoPrecedente());
            
            //nel caso in cui il messaggio non debba essere gestito dal livello, la sua gestione viene demandata alla super class
            
            super.Handler(m);
        }

    }

    private void generaMessaggioSvuotaCoda() {
        Messaggi m = new Messaggi(SVUOTA_CODA, this, this, this.nodo, s.orologio.getCurrent_Time());
        s.insertMessage(m);
    }

    private void generaMessaggioTimeout() {
        Messaggi m = new Messaggi(TIME_OUT, this, this, this.nodo, s.orologio.getCurrent_Time());
        m.shifta(timeout);
        m.setIDArq(ID_msg_arq);
        s.insertMessage(m);
    }

    private void rispondiConAck(int idArq, Nodo nodo_precedente) {
        int id_nodo_corrente = ((Nodo) (this.nodo)).getId();
        int id_nodo_sorgente = nodo_precedente.getId();

        Messaggi m = new Messaggi(ACK_ARQ, this, this.phyLayer, nodo_precedente, s.orologio.getCurrent_Time());
        m.saliPilaProtocollare = false;
        m.shifta(tempo_processamento);
        m.setIDArq(idArq);
        m.setNodoSorgente(this.nodo);
        m.setNextHop(nodo_precedente);
        m.setNextHop_id(id_nodo_sorgente);
        s.insertMessage(m);

        try {
            if (bw != null) {
                bw.append(s.orologio.getCurrent_Time() + ";"
                        + "Nodo :"+((Nodo) this.nodo).getId() + ";"
                        + "Genero Messaggio ACK;"
                        + "ARQ ID..:"+idArq + ";"
                        + "Verso Nodo..:"+m.getNextHop_id() + ";"
                        + "Tipo Messaggio.:"+m.getTipo_Messaggio()
                        + "\n"
                );
                bw.flush();
                getLogger().info("{} ; Nodo : {}; Genero Messaggio ACK; ARQ ID..:{} ; Verso Nodo..: {} ; Tipo Messaggio.: {} ",s.orologio.getCurrent_Time(),((Nodo) this.nodo).getId(),idArq,m.getNextHop_id(),m.getTipo_Messaggio());
                
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void endSim() {
        try
        {
            bw.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
