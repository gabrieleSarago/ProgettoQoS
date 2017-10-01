/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package radioFM;

import java.util.ArrayList;
import java.util.HashMap;

import base_simulator.Messaggi;
import base_simulator.scheduler;
import base_simulator.layers.Applicazione;
import reti_tlc_gruppo_0.tcpTransportLayer;

/**
 *
 * @author afsan_000
 */
class lostPackts {

    public int application_id;
    public ArrayList<Integer> pcktsId = new ArrayList<Integer>();

    public lostPackts(int application_id) {
        this.application_id = application_id;
    }

}

public class waveFSCTPTransportLayer extends tcpTransportLayer {

    final String FSCTP_CONTROL_MESSAGE = "fsctp_control";
    final String FSCTP_CONTROL_MESSAGE_ACK = "fsctp_control_ack";

    final String APPLICATION_MSG_DATA = "udp data";
    final String CONTROLLO_LOST_PCKTS = "controllo lost pckts";

    protected HashMap<Integer, Applicazione> activeApp = new HashMap<Integer, Applicazione>();
    protected HashMap<Integer, Messaggi> outBuffer = new HashMap<Integer, Messaggi>();

    protected HashMap<Integer, Integer> receivedMessages = new HashMap<Integer, Integer>();
    protected HashMap<Integer, lostPackts> lostPachets = new HashMap<Integer, lostPackts>(); // DA CAMBIARE IN STRUTTURA ASSOCIATA AD APPLICAZIONE

    protected int windowSize = 10;
    protected int lastSeqId = 0;

    protected int applicationStart = 0;
    protected int applicationRequest = 0;
    protected int applicationStop = 0;

    protected double PERIODO_OSSERVAZIONE = 10000;
    protected double PERIODO_OSSERVAZIONE_UDT = 1000;
    protected double PERIODO_DI_GUARDIA = 0.1 * PERIODO_OSSERVAZIONE;
    private ArrayList<Integer> resendMessage = new ArrayList<Integer>();
    private boolean isUDT = false; //se false FSCTP
    
    

    public waveFSCTPTransportLayer(scheduler s, double tempo_di_processamento) {
        super(s, tempo_di_processamento);

        if(isUDT == true)
        {
            PERIODO_OSSERVAZIONE = PERIODO_OSSERVAZIONE_UDT;
            PERIODO_DI_GUARDIA = 0.1 * PERIODO_OSSERVAZIONE;
        }
        Messaggi m = new Messaggi(CONTROLLO_LOST_PCKTS, this, this, null, s.orologio.getCurrent_Time());
        m.shifta(PERIODO_DI_GUARDIA + PERIODO_OSSERVAZIONE);
        s.insertMessage(m);
    }

    public void Handler(Messaggi m) {
        if (m.getTipo_Messaggio().equals(CONTROLLO_LOST_PCKTS)) {

            m.shifta(PERIODO_DI_GUARDIA + PERIODO_OSSERVAZIONE);
            s.insertMessage(m);

            for (HashMap.Entry<Integer, lostPackts> e : lostPachets.entrySet()) {
                Applicazione a = activeApp.get(e.getKey());
//                if ((this.isUDT == true && a.getEndAt() != 0) 
//                     || (this.isUDT == false)) 
                
//                    {
                        //Inverto sorgente e destinazione perche' e' un messaggio da inviare al server
                        Messaggi m1 = new Messaggi(FSCTP_CONTROL_MESSAGE, this, this.networkLayer, a.getNodo_sorgente(), s.orologio.getCurrent_Time());
                        m1.setSize(a.getMSS());
                        m1.addHeader(this.header_size);
                        m1.setNodoSorgente(a.getNodo_destinazione());
                        m1.shifta(tempo_di_processamento);
                        m1.setData(e.getValue().pcktsId.clone());
                        m1.setApplication_port(e.getKey());

                        e.getValue().pcktsId.clear();
                        s.insertMessage(m1);
//                    }
                }

            }else if (m.getTipo_Messaggio().equals(FSCTP_CONTROL_MESSAGE_ACK)) {

        } else if (m.getTipo_Messaggio().equals(FSCTP_CONTROL_MESSAGE)) {
            System.out.println("Arrivato richiesta di reinvio pacchetti");
            //Invio ack
            m.setTipo_Messaggio(FSCTP_CONTROL_MESSAGE_ACK);
            m.saliPilaProtocollare = false;
            m.shifta(tempo_di_processamento);
            m.setNodoDestinazione(m.getNodoSorgente());
            m.setNodoSorgente(nodo);
            s.insertMessage(m);

            for (int i = 0; i < ((ArrayList<Integer>) m.getData()).size(); i++) {
                this.resendMessage.add(((ArrayList<Integer>) m.getData()).get(i));
            }

        } else if (m.getTipo_Messaggio().equals(APPLICATION_MSG)) {

            if (m.saliPilaProtocollare) {
//il pacchetto dati e' arrivato a destinazione
                //Pacchetto dati arrivato
                m.removeHeader(super.header_size);
                System.out.println("Arrivato messaggio a destinazione..:" + m.getID() + " ");
                Applicazione a = activeApp.get(m.getApplication_port());

                if (a != null) {
                    a.received_packet++;
                    activeApp.put(m.getApplication_port(), a);
                    int lastId = 0;
                    if(receivedMessages.containsKey(m.getApplication_port()))
                    {
                        lastId = receivedMessages.get(m.getApplication_port());
                    }
                    if (m.getID() > lastId + 1) {
                        lostPackts lp = null;
                        if (lostPachets.containsKey(m.getApplication_port())) {
                            lp = lostPachets.get(m.getApplication_port());
                        } else {
                            lp = new lostPackts(m.getApplication_port());
                        }
                        for (int i = receivedMessages.get(m.getApplication_port()) + 1;
                                i <= m.getID(); i++) {
                            if (!lp.pcktsId.contains(i)) {
                                lp.pcktsId.add(i);
                            }
                        }
                        if(this.isUDT == false)
                        {
                            receivedMessages.put(m.getApplication_port(), m.getID());
                        }
                        lostPachets.put(m.getApplication_port(), lp);
                        
                    } else if (m.getID() >= lastId) {
                        receivedMessages.put(m.getApplication_port(), m.getID());
                    }
                } else {
                    double size = m.getSize();
                    a = new Applicazione(size);
                    a.setStartAt(s.orologio.getCurrent_Time());
                    a.received_packet++;
                    a.setNodo_sorgente(m.getNodoSorgente());
                    a.setNodo_destinazione(m.getNodoDestinazione());
                    a.setPort(m.getApplication_port());

                    activeApp.put(m.getApplication_port(), a);

                    receivedMessages.put(m.getApplication_port(), m.getID());
                }

            } else if (!activeApp.containsKey(m.getApplication_port())) {
                applicationRequest++;
                Applicazione a = new Applicazione(m.getSize());
                a.setIsWaitingForAcks(true);
                a.setStartAt(s.orologio.getCurrent_Time());
                a.setNodo_destinazione(m.getNodoDestinazione());
                a.setNodo_sorgente(m.getNodoSorgente());

                activeApp.put(m.getApplication_port(), a);
                super.Handler(m); // questo messaggio mi permette di aprire la connessione TCP                                
                Messaggi m1 = new Messaggi(APPLICATION_MSG, m.getSorgente(), m.getDestinazione(), m.getNodoDestinazione(), m.getTempo_di_partenza());
                m1.ID = m.getID();
                m1.setApplication_port(m.getApplication_port());
                m1.setNodoSorgente(nodo);
                m1.setSize(m.getSize());
                m1.addHeader(super.header_size);
                m1.setData(m.getData());
                outBuffer.put(m1.ID, m1);
            } else {
                outBuffer.put(m.ID, m);
            }
        } else if (m.getTipo_Messaggio().equals(SVUOTA_CODA)) {
            int counter = 0;

//            Applicazione a = activeApp.get(m.getApplication_port());
//            if (a != null && a.isWaitingForAcks() == false) {
            double tempo_corrente = s.orologio.getCurrent_Time();
            double tempo_invio = 0;
            Applicazione a = null;
            int application_port = 0;
            double inter_delay = 0;
            double MSS = 0;

            for (int i = counter; i < windowSize; i++) {
                Messaggi m1 = null;
                boolean resend = false;
                if (resendMessage.size() > 0) {
                    m1 = outBuffer.get(resendMessage.get(0));
                    resendMessage.remove(0);
                    resend = true;
                } else {
                    m1 = outBuffer.get(lastSeqId + 1);
                }
//                lastSeqId++;
                if (m1 != null) {
                    if (a == null) {
                        application_port = m1.getApplication_port();
                        a = activeApp.get(application_port);

                        MSS = m1.getSize();
                        inter_delay = (windowSize * MSS * 8) / a.availableBandwidth; //Consideriamo 6MBps 

                    }

                    if (a != null && a.isWaitingForAcks() == false) {
                        counter++;
                        Messaggi m2 = new Messaggi(m1.getTipo_Messaggio(),
                                this,
                                this.networkLayer,
                                m1.getNodoDestinazione(),
                                s.orologio.getCurrent_Time());

                        m2.saliPilaProtocollare = false;
                        m2.ID = m1.ID;
                        if (resend == false) {
                            lastSeqId = m2.ID;
                        }
                        m2.setNodoSorgente(nodo);
                        m2.setNodoDestinazione(m1.getNodoDestinazione());
                        m2.setApplication_port(m1.getApplication_port());
                        m2.setInterDelayPacket(m1.getInterDelayPacket());
                        m2.setData(m1.getData());
                        m2.isData = true;
                        m2.setSize(m1.getSize());
                        tempo_invio = tempo_corrente + (counter * m1.getInterDelayPacket());
                        m2.setTempo_di_partenza(tempo_invio);
                        m2.shifta(m1.getInterDelayPacket());
                        s.insertMessage(m2);

                        a.sent_packet++;

                    }
                } else {
                    break;
                }
            }

            if (a != null) {

                double durata_tx = (counter * inter_delay);
                double size = MSS * counter;
                double bitrate = 0;
                if (durata_tx > 0) {
                    bitrate = (size / durata_tx);
                }
                a.avgBitrate = a.avgBitrate + bitrate;
                a.avgBitrateCounter = a.avgBitrateCounter + 1;
                activeApp.put(application_port, a);
            }

            Messaggi m3 = new Messaggi(SVUOTA_CODA, this, this, null, s.orologio.getCurrent_Time());
            m3.shifta(1000);
            s.insertMessage(m3);
//            super.Handler(m);

            System.out.println("Inviati per questa sessione ..:" + counter + " Pacchetti");

        } else if (m.getTipo_Messaggio().equals(APPLICATION_MSG_ACK)) {

        } else if (m.getTipo_Messaggio().equals(super.OPEN_CONNECTION_ANSWER)) {
            //Connessione attiva posso iniziare a svuotare buffer UDP
            if ((Boolean) m.getData() == true) {
                Applicazione a = activeApp.get(m.getApplication_port());
                if (a != null) {
                    applicationStart++;
                    a.setIsWaitingForAcks(false);
                    activeApp.put(m.getApplication_port(), a);
                }
            }
            Messaggi m1 = new Messaggi(SVUOTA_CODA, this, this, null, s.orologio.getCurrent_Time());
            s.insertMessage(m1);
            super.Handler(m);

        } else if (m.getTipo_Messaggio().equals(CLOSE_CONNECTION)) {
            if (outBuffer.containsKey(lastSeqId + 1) && !m.saliPilaProtocollare) {
                //Ci sono ancora pacchetti da inviare
                m.shifta(5000);
                s.insertMessage(m);
            } else {
                Applicazione a = activeApp.get(m.getApplication_port());
                if(m.saliPilaProtocollare)
                   a.setEndAt(s.orologio.getCurrent_Time());                
                activeApp.put(m.getApplication_port(), a);                
                if(m.saliPilaProtocollare == false){
                    Messaggi m1 = new Messaggi("check_close_connection",
                            this,
                            this,
                            this.nodo,
                            s.orologio.getCurrent_Time());
                    m1.shifta(2000);
                    m1.setApplication_port(m.getApplication_port());
                    s.insertMessage(m1);
                }
                super.Handler(m);                                
            }
        }else if(m.getTipo_Messaggio().equals("check_close_connection"))
        {
            Applicazione a = activeApp.get(m.getApplication_port());
            if(a.getEndAt() == 0)
            {
                Messaggi m1 = new Messaggi("close connection", 
                        this, 
                        this.networkLayer, 
                        a.getNodo_destinazione(), 
                        s.orologio.getCurrent_Time());   
                
                m1.isData = true;
                m1.setNodoSorgente(this.nodo);
                m1.setApplication_port(a.getPort());
                m1.setSize(a.getMSS());                
                m1.addHeader(this.getHeader_size());                
                m1.saliPilaProtocollare = false; //Il messaggio deve partire dal livello traporto e scendere nella pila                
                m1.shifta(this.tempo_di_processamento);
                s.insertMessage(m1);
                
                //Ricontrolliamo allo scadere del timeout
                m.shifta(2000);
                s.insertMessage(m);                
            }
            
            
        }
        else if (m.getTipo_Messaggio().equals(CLOSE_CONNECTION_ACK)) {
            applicationStop++;
            Applicazione a = activeApp.get(m.getApplication_port());
            a.setEndAt(s.orologio.getCurrent_Time());
//            a.stampaStatistiche(((NodoMacchina) nodo).getId());
            activeApp.put(m.getApplication_port(), a);

//            stampaStatsApplication();
            super.Handler(m);
        } else {
            super.Handler(m);
        }
        }

    

    private void stampaStatsApplication() {
        System.out.println("======STAMPA APPLICATION LAYER");
        System.out.println("Node id:" + ((MobileHost) nodo).getId());
        System.out.println("Application Request..:" + applicationRequest);
        System.out.println("Application Start....:" + applicationStart);
        System.out.println("Application Stop....:" + applicationStop);

        for (HashMap.Entry<Integer, Applicazione> e : activeApp.entrySet()) {
            Applicazione a = e.getValue();
            a.stampaStatistiche(((MobileHost) nodo).getId());
        }

    }

    @Override
    public void stampaStatistiche() {
        stampaStatsApplication();
    }

}
