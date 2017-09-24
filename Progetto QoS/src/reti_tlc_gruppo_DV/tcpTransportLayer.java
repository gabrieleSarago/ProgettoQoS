/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reti_tlc_gruppo_DV;

import base_simulator.DV.Messaggi;
import base_simulator.DV.NodoDV;
import base_simulator.layers.DV.Applicazione;
import base_simulator.layers.DV.TransportLayer;
import base_simulator.DV.schedulerDV;

/**
 *
 * @author afsantamaria
 */
public class tcpTransportLayer extends TransportLayer {

    private int AvailableSpaceForSession = 0; //Byte

    //Questo campo va messo nell'applicazione
    public tcpTransportLayer(schedulerDV s, double tempo_di_processamento) {
        super(s, tempo_di_processamento);

    }

    public int getAvailableSpaceForSession() {
        return AvailableSpaceForSession;
    }

    public void setAvailableSpaceForSession(int AvailableSpaceForSession) {
        this.AvailableSpaceForSession = AvailableSpaceForSession;
    }

    @Override
    public void Handler(Messaggi m) {
        if (m.getTipo_Messaggio().toLowerCase().equals(SVUOTA_CODA)) {

            double tempo = 0;
            Applicazione a = super.getApplication(m.getApplication_port());
            if (a.getPort() == m.getApplication_port()) {
                //Inserito meccanismo per invio pacchetti secondo logica TCP 
                //Prendiamo i messaggi utili a riempire la CWND
                //TODO
                int byte_inviati = 0;

                if (a.lastWindow.size() * a.getMSS() < a.getCWND()) {

                    if (!a.lastWindow.isEmpty()) {
                        /**
                         * Se entro in questo loop vuol dire che è scaduto in
                         * timeout
                         *
                         */
                        a.manageTcpCongestionEvent();
                        if (byte_inviati <= a.getCWND()) {
                            for (Object obj : a.lastWindow) {
                                Messaggi m1 = (Messaggi) obj;

                                m1.setNodoSorgente(nodo);
                                m1.setSorgente(this);
                                m1.setDestinazione(this.networkLayer);
                                m1.setTempo_spedizione(s.orologio);
                                m1.setTempo_di_partenza(s.orologio.getCurrent_Time());
                                m1.shifta(tempo_di_processamento);
                                s.insertMessage(m1);

                                byte_inviati += m1.getSize();

                            }
                        }
                    }
                    int counter = 0;
                    for (Object obj : buffer) {
                        Messaggi m1 = (Messaggi) obj;

                        if (byte_inviati < a.getCWND()) {
                            //Da inviare pacchetto al networkLayer

                            m1.setNodoSorgente(nodo);
                            m1.setSorgente(this);
                            m1.setDestinazione(this.networkLayer);

                            m1.setTempo_spedizione(s.orologio);
                            m1.shifta(tempo_di_processamento);
                            m1.setTempo_di_partenza(s.orologio.getCurrent_Time());
                            byte_inviati += m1.getSize();
                            m1.addHeader(this.header_size);
                            s.insertMessage(m1);

                            a.lastWindow.add(m1);
                            counter++;

                        } else {
                            break;
                        }

                    }

                    for (int i = 0; i < counter; i++) {
                        buffer.remove(0);
                        
                    }

                    if (a.lastWindow.size() > 0) {
                        a.setLastWindowSize(a.lastWindow.size());

                        a.setIsWaitingForAcks(true);

                        Messaggi m1 = new Messaggi(this.SVUOTA_CODA, this, this, nodo, s.orologio.getCurrent_Time());
                        m1.setApplication_port(m.getApplication_port());

                        m1.shifta(a.getRTO());

                        s.insertMessage(m1);
                    }

                } else {
                    Messaggi m1 = new Messaggi(this.SVUOTA_CODA, this, this, nodo, s.orologio.getCurrent_Time());
                    m1.setApplication_port(m.getApplication_port());

                    m1.shifta(a.getRTO());

                    s.insertMessage(m1);
                }

            }
        } else if (m.getTipo_Messaggio().toLowerCase().equals(APPLICATION_MSG)) {
            if (isAvailable(m.getApplication_port())) {
                //System.out.println("I:" + this.getTipo() + " su nodo:" + ((NodoDV) nodo).getId() + ": Arrivato messaggio applicazione con ID " + m.getID() + " Sulla porta :" + m.getApplication_port());
                if (m.saliPilaProtocollare == false) {
                    //TODO : I messaggi vanno cmq messi nel buffer e poi inviati rischio di mandare fuori ordine già dalla sorgente
                    //SOLUZIONE CON UDP metto nel buffer ed invio tutto a partire dal primo pacchetto presente nel buffer, altrimenti si invierà allo scadere del timer
                    if (sessionActive(m) == ACCEPTED) {
                        //Inserisco il messaggio in coda ma devo cmq inserirlo nel buffer e forzare uno svuotamento

                        buffer.add(m);
                        Applicazione a = getApplication(m.getApplication_port());
                        if (!a.isWaitingForAcks()) {
                            Messaggi m1 = new Messaggi(this.SVUOTA_CODA, this, this, nodo, s.orologio.getCurrent_Time());
                            m1.setApplication_port(m.getApplication_port());
                            s.insertMessage(m1);

                        }

                    } else if (sessionActive(m) == WAITING) {
                        //Sono in attesa del receiver per attivare la connessione

                        buffer.add(m);

                        Applicazione a = getApplication(m.getApplication_port());
                        if (!a.isWaitingForAcks()) {
                            Messaggi m1 = new Messaggi(this.SVUOTA_CODA, this, this, nodo, s.orologio.getCurrent_Time());
                            m1.setApplication_port(m.getApplication_port());
                            m1.shifta(a.getRTO());
                            s.insertMessage(m1);

                        }

                    } else {
                        packet_refused++;
                    }

                } else {
                    //Il pacchetto è stato ricevuto
                    m.removeHeader(this.header_size);
                    storePayload(m);
                    build_app_ack(m);

                }
            }
        } else if (m.getTipo_Messaggio().toLowerCase().equals(APPLICATION_MSG_ACK)) {
            Applicazione a = getApplication(m.getApplication_port());

            a.setLast_seq_no_received(m.getID());

            if ((a.getReceivedAck() * a.getMSS()) == a.getCWND()) {
                a.setRecvWin(m.getReceiveWin(), s.orologio.getCurrent_Time());
                a.setReceivedAck(0);
                a.setIsWaitingForAcks(false);
                Messaggi m1 = new Messaggi(this.SVUOTA_CODA, this, this, nodo, s.orologio.getCurrent_Time());
                m1.setApplication_port(m.getApplication_port());
                s.insertMessage(m1);
            }
        } else if (m.getTipo_Messaggio().toLowerCase().equals(OPEN_CONNECTION)) {
            boolean res = false;
            Applicazione a = null;
            if (isAvailable(m.getApplication_port())) {
                //return REFUSED to request host
                connection_refused++;
            } else {
                res = true;
                m.removeHeader(header_size);
                a = new Applicazione(m.getSize());

                this.enablePort(m.getApplication_port());

                connection_accepted++;
                //return ACCEPTED to request host and open an application receiver

                a.setPort(m.getApplication_port());
                a.setRecvWin(this.AvailableSpaceForSession);
                a.setStatus(ACCEPTED);
                apps.add(a);
            }

            m.setTipo_Messaggio(OPEN_CONNECTION_ANSWER);
            m.saliPilaProtocollare = false;
            m.setData(res);
            m.setDestinazione(this.networkLayer);
            m.setSorgente(this);
            m.shifta(tempo_di_processamento);
            m.setNodoDestinazione(m.getNodoSorgente());
            m.setNodoSorgente(nodo);
            m.addHeader(this.getHeader_size());
            if (a != null) {
                m.setReceiveWin(a.getRecvWin());
            }

            s.insertMessage(m);

        } else if (m.getTipo_Messaggio().toLowerCase().equals(OPEN_CONNECTION_ANSWER)) {
            if (((Boolean) m.getData()) == true) {
                //Bisogna settare i parametri di trasmissione per il controllo di congestione e di flusso

                active_session++;
                start_sending(m.getApplication_port());
                setAppicationReceiveWin(m);
            }
        } else {
            super.Handler(m);
        }

    }

    /**
     * creo messaggio di risposta conosciuto come acknowledgment nel ack
     * inserisco il numero di sequenza per il quale ho ricevuto il pacchetto Per
     * la fast retransmit bisogna modificare questo metodo e la raccolta degli
     * ack
     *
     * I tre ack consecutivi del TCP tahoe vengono considerati con la conta
     * degli ack ricevuti e il consecutivo timeout che si genera (SVUOTA_CODA +
     * isWaitingForAck Attivo
     *
     * @param m
     */
    protected void build_app_ack(Messaggi m) {

        //Settiamo parametri TCP
        Applicazione a = getApplication(m.getApplication_port());
        a.setLast_seq_no_received(m.getID());

        m.setReceiveWin(a.getRecvWin());

        //Parametri per iviare la risposta verso la sorgente
        m.saliPilaProtocollare = false;
        m.setTipo_Messaggio(APPLICATION_MSG_ACK);
        m.setAckType();
        m.setNodoDestinazione(m.getNodoSorgente());
        m.setNodoSorgente(nodo);
        m.setDestinazione(this.networkLayer);

        m.setSorgente(this);
        m.setSize(base_mss);
        m.addHeader(header_size);

        m.shifta(tempo_di_processamento);
        s.insertMessage(m);
    }

    private void setAppicationReceiveWin(Messaggi m) {

        Applicazione a = getApplication(m.getApplication_port());
        if (a != null) {
            a.setRecvWin(m.receiveWin);
        }
    }

}
