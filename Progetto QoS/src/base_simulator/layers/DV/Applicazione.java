/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base_simulator.layers.DV;

import base_simulator.DV.Messaggi;
import java.util.ArrayList;

/**
 * Questa classe permette di memorizzare i dati che arrivano o devono andare al
 * livello applicazione. In particolare, è molto utile nella ricostruzione dei
 * frammenti
 *
 * @author afsantamaria
 */
class Stat {

    double tempo;
    double valore;

    public Stat(double tempo, double valore) {
        this.tempo = tempo;
        this.valore = valore;
    }

    public double getTempo() {
        return tempo;
    }

    public double getValore() {
        return valore;
    }

}

public class Applicazione {

    int port = 0;
    byte message[] = new byte[0];
    int status = -1;
    protected int last_seq_no_received = 0;

    protected int CWND;
    protected int ADVWIN;
    protected int RecvWin;
    protected int SSThreshold;
    protected int scaleFactor;
    protected int RTO = 1000;
    protected int maxwin;
    protected int lastWindowSize = 1;

    private boolean isWaitingForAcks = false;
    private double sent_packet = 0;
    private double received_ack = 0;
    private double received_packet = 0;
    boolean isWaitingForOpenConnectionAnswer = false;

    public int getLastWindowSize() {
        return lastWindowSize;
    }

    public void setLastWindowSize(int lastWindowSize) {
        this.lastWindowSize = lastWindowSize;
        sent_packet += lastWindowSize;
    }

    public ArrayList<Messaggi> lastWindow = new ArrayList<Messaggi>();
    protected double MSS = 1;

    public int getMaxwin() {
        return maxwin;
    }

    public void setMaxwin(int maxwin) {
        this.maxwin = maxwin;
    }

    protected int waiting_for_MSS_counter = 0;

    public int getWaiting_for_MSS_counter() {
        return waiting_for_MSS_counter;
    }

    public void setWaiting_for_MSS_counter(int waiting_for_MSS_counter) {
        this.waiting_for_MSS_counter = waiting_for_MSS_counter;
    }

    protected void addMessage(Messaggi m) {
        lastWindow.add(m);
    }

    /**
     * Ritorna il messaggio presente nell'ultima finestra inviata, questo viene
     * utilizzato per le ritrasmissioni
     *
     * @param msg_id
     * @return Messaggio con msg_id indicato presente nell'ultima finestra
     * inviata
     */
    protected Messaggi getMessaggio(int msg_id) {
        Messaggi res = null;
        for (Object obj : lastWindow) {

            if (((Messaggi) obj).getID() == msg_id) {
                res = (Messaggi) obj;

                break;
            }
        }

        return res;
    }

    /**
     * Questo metodo è utilizzato quando è arrivato alla sorgente ack da parte
     * della destinazione
     *
     * @param msg_id - id del messaggio da rimuovere
     */
    protected void removeMessageFromLastWin(int msg_id) {

        for (Object obj : lastWindow) {

            if (((Messaggi) obj).getID() == msg_id) {
                lastWindow.remove(obj);

                break;
            }
        }

    }

    protected ArrayList<Stat> RecvWin_stats = new ArrayList<Stat>();
    protected ArrayList<Stat> CWND_stats = new ArrayList<Stat>();

    public int getLast_seq_no_received() {
        return last_seq_no_received;
    }

    public void setLast_seq_no_received(int last_seq_no_received) {
        this.last_seq_no_received = last_seq_no_received;
        received_ack++;
        for (Object obj : lastWindow) {
            if (((Messaggi) obj).getID() == last_seq_no_received) {
                lastWindow.remove(obj);
                break;
            }
        }
    }

    public Applicazione(double MSS) {

        CWND = (int) MSS;
        ADVWIN = 146000; //byte
        RecvWin = (int) MSS;
        SSThreshold = 65535; //Byte (2^15 - 1
        scaleFactor = 1;
        maxwin = (int) MSS;
        this.MSS = MSS;

    }

    public int getCWND() {
        return CWND;
    }

    public void setCWND(int CWND, double tempo) {
        this.CWND = CWND;
        Stat st = new Stat(tempo, CWND);
        CWND_stats.add(st);
    }

    public int getADVWIN() {
        return ADVWIN;
    }

    public void setADVWIN(int ADVWIN) {
        this.ADVWIN = ADVWIN;
    }

    public int getRecvWin() {
        return RecvWin;
    }

    /**
     * Alla fine della ricezione di tutti gli ACK della CWND controllo la
     * finestra del ricevitore e aggiorno la CWND; Aggiorno anche la dimensione
     * della CWND e le statistiche
     *
     * @param RecvWin
     * @param tempo
     */
    public void setRecvWin(int RecvWin, double tempo) {
        this.RecvWin = RecvWin;
        Stat st = new Stat(tempo, RecvWin);
        RecvWin_stats.add(st);
        if (RecvWin < ADVWIN) {
            maxwin = RecvWin;
        } else {
            maxwin = ADVWIN;
        }

        if (CWND < SSThreshold) {
            //SLOW START
            CWND = CWND + (int) (MSS * this.lastWindowSize);
        } else {
            //CONGESTION AVOIDANCE
            CWND = CWND + (int) MSS;
        }

        if (CWND > maxwin) {
            CWND = maxwin;
        }

        setCWND(CWND, tempo);

    }

    /**
     * Questo meto è utilizzato inizialmente per il settare la RecvWin con il
     * Three-way Quando ricevo ack della open connection
     *
     * @param RecvWin
     */
    public void setRecvWin(int RecvWin) {
        this.RecvWin = RecvWin;

        if (RecvWin < ADVWIN) {
            maxwin = RecvWin;
        } else {
            maxwin = ADVWIN;
        }
    }

    public double getMSS() {
        return MSS;
    }

    public void setMSS(double MSS) {
        this.MSS = MSS;
    }

    public int getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(int scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public byte[] getMessage() {
        return message;
    }

    /**
     * Metodo per la ricostruzione dei frammenti della sessione applicazione i
     * dati arrivano attraverso la rete e poi vengono ricostruiti da questo
     * metodo
     *
     * @param message - byte array contenente frammenti dell'informazione da
     * utilizzare a livello applicazione
     */
    public void setMessage(byte message[]) {

        received_packet++;

        int new_size = this.message.length + message.length;

        byte appo[] = new byte[this.message.length];
        System.arraycopy(this.message, 0, appo, 0, this.message.length);

        this.message = new byte[new_size];
        System.arraycopy(appo, 0, this.message, 0, appo.length);

        System.arraycopy(message, 0, this.message, appo.length, message.length);

    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int stato) {
        this.status = stato;
    }

    public double getRTO() {
        //TODO : CALCOLO RTO con JACOBSON        
        return RTO;
    }

    public void setReceivedAck(int acks) {
        this.received_ack = acks;
    }

    public void setIsWaitingForAcks(boolean isWaiting) {
        this.isWaitingForAcks = isWaiting;
    }

    public double getReceivedAck() {
        return this.received_ack;
    }

    public boolean isWaitingForAcks() {
        return this.isWaitingForAcks;
    }

    public void manageTcpCongestionEvent() {
        //TCP TAHOE Gestisco il TIMEOUT COME 3ACKs
        this.SSThreshold = CWND / 2;
        if (SSThreshold < (2 * MSS)) {
            SSThreshold = (int) (2 * MSS);
        }
        CWND = (int) MSS;

    }

    void stampaStatistiche(int node_id) {
        System.out.println("\n\n========STAMPA STATISTICHE TRANSPORT LAYER");
       System.out.println("Nodo :"+node_id);
       System.out.println("Applicazione su porta :"+this.port);
       System.out.println("Pacchetti Inviati :"+sent_packet);
       System.out.println("Ack Ricevuti      :"+received_ack);
       System.out.println("Byte Inviati      :"+sent_packet * MSS);
       System.out.println("Pacchetti Ricevuti:"+received_packet);
       System.out.println("Byte Ricevuti      :"+received_packet*MSS);
       
       System.out.println("\nAndamento RCVWIN");
       for(Object o : this.RecvWin_stats)
       {
           Stat s = (Stat)o;
           System.out.println(s.tempo+";"+s.valore);
       }
       
       System.out.println("\nAndamento CWND");
       for(Object o : this.CWND_stats)
       {
           Stat s = (Stat)o;
           System.out.println(s.tempo+";"+s.valore);
       }
       
       System.out.println("\n\n========END STAMPA STATISTICHE TRANSPORT LAYER");
         
    }

    void setEndAt(double current_Time) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void setStartAt(double current_Time) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
