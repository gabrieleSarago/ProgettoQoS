/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base_simulator.layers;

import base_simulator.Entita;
import base_simulator.Messaggi;
import base_simulator.Nodo;
import base_simulator.scheduler;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Livello Trasporto : Tutti i dati sono trasferiti utilizzando connessioni non
 * sicure non è implementato in questa classe il controllo di congestione e di
 * flusso del TCP
 *
 * I dati sono inviati utilizzando delle connessioni UDP-Like.
 *
 * @author afsantamaria
 */
public class TransportLayer extends Entita {

    protected NetworkLayer networkLayer;
    protected double tempo_di_processamento;
    protected Object nodo;
    protected ArrayList<Integer> enabled_ports = new ArrayList<Integer>();
    protected int header_size = 20; //Byte
    protected int base_mss = 536; //Byte

    protected String TCP = "tcp";
    protected String UDP = "udp";

    protected ArrayList<Applicazione> apps = new ArrayList<Applicazione>();

    protected String OPEN_CONNECTION = "open connection";
    protected String OPEN_CONNECTION_ANSWER = "open connection answer";
    protected String CLOSE_CONNECTION = "close connection";
    protected String CLOSE_CONNECTION_ACK = "close connection ack";
    protected String APPLICATION_MSG = "applicazione";
    protected String APPLICATION_MSG_ACK = "applicazione ack";
    protected String SVUOTA_CODA = "svuota coda";

    protected int connection_refused = 0;
    protected int connection_accepted = 0;
    protected int droppedPacket = 0;
    protected int packet_refused = 0;
    protected int active_session = 0;

    protected int WAITING = 1;
    protected int ACCEPTED = 2;
    protected int REFUSED = 0;

    protected ArrayList<Messaggi> buffer;
    protected boolean wating_for_send_messages = false;
    private double OPEN_CONNECTION_TIMEOUT = 5000;
    private String OPEN_CONNECTION_TIMEOUT_MSG = "open connection timeout";
    final Logger logger = LoggerFactory.getLogger(TransportLayer.class);


    public int getHeader_size() {
        return header_size;
    }

    public void setHeader_size(int header_size) {
        this.header_size = header_size;
    }

    public TransportLayer(scheduler s, double tempo_di_processamento) {
        super(s, "Transport Layer");
        this.tempo_di_processamento = tempo_di_processamento;
        buffer = new ArrayList<Messaggi>();

    }

    public void connectTransportLayer(NetworkLayer networkLayer, Object nodo) {
        this.networkLayer = networkLayer;
        this.nodo = nodo;
    }

    public void enablePort(int port) {
        enabled_ports.add(port);
    }

    @Override
    public void Handler(Messaggi m) {
        if (m.getTipo_Messaggio().toLowerCase().equals(APPLICATION_MSG)) {
            //N.B. : La porta è messa ad enabled dal file di configurazione altrimenti va creata la gestione per l'apertura della porta
            //da programma
            if (isAvailable(m.getApplication_port())) {
               // System.out.println("I:" + this.getTipo() + " su nodo:" + ((Nodo) nodo).getId() + ": Arrivato messaggio applicazione con ID " + m.getID() + " Sulla porta :" + m.getApplication_port());
                logger.debug("I {} su nodo {} : arrivato messaggio applicazione con ID {} sulla porta {}",this.getTipo(),((Nodo) nodo).getId(),m.getID(),m.getApplication_port());
                if (m.saliPilaProtocollare == false) {
                    //TODO : I messaggi vanno cmq messi nel buffer e poi inviati rischio di mandare fuori ordine già dalla sorgente
                    //SOLUZIONE CON UDP metto nel buffer ed invio tutto a partire dal primo pacchetto presente nel buffer, altrimenti si invierà allo scadere del timer
                    if (sessionActive(m) == ACCEPTED) {
                        //Inserisco il messaggio in coda ma devo cmq inserirlo nel buffer e forzare uno svuotamento
                        buffer.add(m);
                        Messaggi m1 = new Messaggi(this.SVUOTA_CODA, this, this, nodo, s.orologio.getCurrent_Time());
                        s.insertMessage(m1);

                    } else if (sessionActive(m) == WAITING) {
                        //TODO : devo cambiare il metodo mettendo pacchetto nel buffer e generando un metodo svuota buffer

                        buffer.add(m);
                        if (!wating_for_send_messages) {
                            wating_for_send_messages = true;
                            Messaggi m1 = new Messaggi(this.SVUOTA_CODA, this, this, nodo, s.orologio.getCurrent_Time() + 1000);
                            s.insertMessage(m1);
                        }
                    } else {
                        packet_refused++;
                    }

                } else {
                    //Il pacchetto è stato ricevuto
                    m.removeHeader(this.header_size);
                    storePayload(m);
                    if (m.getAckType() == m.WITH_ACK) {
                        //TODO : Devo inviare ack indietro alla sorgente se previsto
                    }

                }
            }
        } else if (m.getTipo_Messaggio().equals(this.OPEN_CONNECTION_TIMEOUT_MSG)) {
            Applicazione a = this.getApplication(m.getApplication_port());
            if (a.isWaitingForOpenConnectionAnswer == true) {
                Messaggi m1 = new Messaggi(this.OPEN_CONNECTION, this, this.networkLayer, m.getNodoDestinazione(), s.orologio.getCurrent_Time());
                m1.saliPilaProtocollare = false;
                m1.isData = true;
                m1.setNodoSorgente(nodo);
                m1.shifta(0);
                m1.setApplication_port(m.getApplication_port());
                m1.setSize(m.getSize());

                m.shifta(this.OPEN_CONNECTION_TIMEOUT);
                s.insertMessage(m);
                s.insertMessage(m1);
            }
        } else if (m.getTipo_Messaggio().toLowerCase().equals(SVUOTA_CODA)) {
//Gestione tipo UDP ; Per la gestione TCP implementare meccanismo di Congestion Avoidance            
            wating_for_send_messages = false;
            double tempo = 0;
            for (Object obj : buffer) {
                Messaggi m1 = (Messaggi) obj;
                try {
                    //Da inviare pacchetto al networkLayer
                    m1.addHeader(this.header_size);
                    m1.setNodoSorgente(nodo);
                    m1.setSorgente(this);
                    m1.setDestinazione(this.networkLayer);
                    m1.setTempo_di_partenza(s.orologio.getCurrent_Time());
                    m1.shifta(tempo_di_processamento);
                    s.insertMessage(m1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                buffer.remove(obj);
            }

        } else if (m.getTipo_Messaggio().toLowerCase().equals(OPEN_CONNECTION)) {
            boolean res = false;
            if (isAvailable(m.getApplication_port())) {
                //return REFUSED to request host
                connection_refused++;
            } else {
                res = true;
                this.enablePort(m.getApplication_port());
                connection_accepted++;
                m.removeHeader(header_size);
                //return ACCEPTED to request host and open an application receiver
                Applicazione a = new Applicazione(m.getSize());
                a.setPort(m.getApplication_port());
                a.setStatus(this.ACCEPTED);

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
            m.isData = false;

            s.insertMessage(m);

        } else if (m.getTipo_Messaggio().toLowerCase().equals(OPEN_CONNECTION_ANSWER)) {
            if (((Boolean) m.getData()) == true) {
                active_session++;
                Applicazione a = this.getApplication(m.getApplication_port());
                a.isWaitingForOpenConnectionAnswer = false;
                start_sending(m.getApplication_port());
            }
        } else if (m.getTipo_Messaggio().toLowerCase().equals(TCP)) {
            //Deprecato estendere la classe per implmentare TCP

        } else if (m.getTipo_Messaggio().toLowerCase().equals(UDP)) {
            //UDP è possibile utilizzarlo utilizzando questa classe

        } else if (m.getTipo_Messaggio().toLowerCase().equals(this.CLOSE_CONNECTION)) {

            //ripulire la connessione sul nodo liberando risorse porta e app
            if (m.saliPilaProtocollare == false) {
                if (sessionActive(m) == ACCEPTED) {
                    m.setSorgente(this);
                    m.setDestinazione(this.networkLayer);
                    m.addHeader(header_size);
                    m.shifta(tempo_di_processamento);
                    m.setNodoSorgente(nodo);
                    s.insertMessage(m);
                } else if (sessionActive(m) == WAITING) {
                    m.shifta(1000);
                    s.insertMessage(m);
                } else {
                    this.packet_refused++;
                }
            } else {
                //Il messaggio è arrivato dalla sorgente sulla destinazione
                //Possiamo stampare il messaggio completo ricevuto
                System.out.println("L'applicazione ha terminato l'invio e ho ricevuto dal nodo :" + ((Nodo) m.getNodoSorgente()).getId() + "il seguente messaggio");
                Applicazione a = null;
                for (Object obj : apps) {
                    a = (Applicazione) obj;
                    if (a.getPort() == m.getApplication_port()) {
                        FileOutputStream OutFile = null;
                        try {
//Scrivo i byte su un file di uscita
                            OutFile = new FileOutputStream(
                                    "test.txt");
                            OutFile.write(a.getMessage());
                            OutFile.flush();
                            OutFile.close();
                            break;
                        } catch (FileNotFoundException ex) {
                            //Logger.getLogger(TransportLayer.class.getName()).log(Level.SEVERE, null, ex);
                            logger.error(ex.getMessage());
                        } catch (IOException ex) {
                           // Logger.getLogger(TransportLayer.class.getName()).log(Level.SEVERE, null, ex);
                            logger.error(ex.getMessage());
                        } finally {
                            try {
                                OutFile.close();
                            } catch (IOException ex) {
                                //Logger.getLogger(TransportLayer.class.getName()).log(Level.SEVERE, null, ex);
                                logger.error(ex.getMessage());
                            }
                        }
                    }
                }
                if (a != null) {
                    //apps.remove(a);
                }

                //Chiudiamo la porta
                closePort(m.getApplication_port());
                m.setDestinazione(this.networkLayer);
                m.saliPilaProtocollare = false;
                m.setNodoDestinazione(m.getNodoSorgente());
                m.setNodoSorgente(this.nodo);
                m.shifta(tempo_di_processamento);
                m.setTipo_Messaggio(CLOSE_CONNECTION_ACK);
                s.insertMessage(m);

            }
        } else if (m.getTipo_Messaggio().toLowerCase().equals(this.CLOSE_CONNECTION_ACK)) {
            //Alla ricezione dell'ack possiamo distruggere applicazione e liberare la porta anche sulla sorgente
            Applicazione a = this.getApplication(m.getApplication_port());

            if (a != null) {
                //TODO : GET STATS prima di rimuovere app
                a.setEndAt(s.orologio.getCurrent_Time());
                a.stampaStatistiche(((Nodo) nodo).getId());
                apps.remove(a);
            }

            //Chiudiamo la porta
            closePort(m.getApplication_port());
        }

    }

    /**
     * Questo metodo ritorna true se la porta è attiva e quindi è possibile
     * inviare e rivevere messaggi su tale porta
     *
     * @param application_port - porta da verificare
     * @return true se la porta è attiva false altrimenti
     */
    protected boolean isAvailable(int application_port) {
        boolean res = false;
        for (Object port : enabled_ports) {
            if (application_port == ((Integer) port)) {
                res = true;
                break;
            }
        }
        return res;
    }

    /**
     * Chiude la porta se attiva per future connessioni
     *
     * @param application_port
     * @return true se la porta era attiva false altrimenti
     */
    private boolean closePort(int application_port) {
        boolean res = false;
        int count = -1;
        for (Object port : enabled_ports) {
            count++;
            if (application_port == ((Integer) port)) {
                res = true;
                break;
            }
        }
        if (res == true) {
            enabled_ports.remove(count);
        }
        return res;
    }

    /**
     * Memorizza il payload del messaggio appplicazione
     *
     * @param m : Messaggio applicazione da memorizzare; i dati sono contenuti
     * nel campo Data del messaggio
     */
    protected void storePayload(Messaggi m) {
        if (m.getData() != null) {

            for (Object obj : apps) {
                if (((Applicazione) obj).getPort() == m.getApplication_port()) {
                    ((Applicazione) obj).setMessage((byte[]) m.getData());
                    break;
                }
            }
        }
    }

    /**
     * Controlla se la sessione è attiva e ritorna lo stato
     *
     * @param m messaggio sul quale controllare la sessione di riferimento
     * @return Stato della sessione WAITING se la sessione non era ancora stata
     * attivata aspettiamo il ritorno dalla destinazione che verifica se è
     * possibile attivare la connessione Se Refused non si può attivare la
     * connessione
     */
    protected int sessionActive(Messaggi m) {
        int status = -1;
        Applicazione a = new Applicazione(m.getSize());
        for (Object obj : apps) {
            if (((Applicazione) obj).getPort() == m.getApplication_port()) {
                status = ((Applicazione) obj).getStatus();
                break;
            }
        }
        if (status == -1) {
            status = WAITING;
            a.setStatus(WAITING);
            a.setPort(m.getApplication_port());
            a.isWaitingForOpenConnectionAnswer = true;
            apps.add(a);

            //Devo inviare messaggio al nodo destinazione
            Messaggi m1 = new Messaggi(this.OPEN_CONNECTION, this, this.networkLayer, m.getNodoDestinazione(), s.orologio.getCurrent_Time());
            m1.saliPilaProtocollare = false;
            m1.isData = true;
            m1.setNodoSorgente(nodo);
            m1.shifta(0);
            m1.setApplication_port(m.getApplication_port());
            m1.setSize(m.getSize());

            Messaggi m2 = new Messaggi(this.OPEN_CONNECTION_TIMEOUT_MSG, this, this, this.nodo, s.orologio.getCurrent_Time());
            m2.shifta(OPEN_CONNECTION_TIMEOUT);
            m2.setApplication_port(m.getApplication_port());
            m2.setNodoSorgente(nodo);
            s.insertMessage(m2);

            s.insertMessage(m1);

        }
        return status;
    }

    /**
     * Metodo che permette l'inizio delle attività della sessione sulla porta
     * indicata
     *
     * @param application_port
     */
    protected void start_sending(int application_port) {
        for (Object obj : apps) {
            Applicazione a = (Applicazione) obj;
            a.setStartAt(s.orologio.getCurrent_Time());
            if (((Applicazione) obj).getPort() == application_port) {
                ((Applicazione) obj).setStatus(ACCEPTED);
                break;
            }
        }
    }

    protected Applicazione getApplication(int application_port) {
        Applicazione a = null;
        for (Object o : apps) {
            if (((Applicazione) o).getPort() == application_port) {
                a = (Applicazione) o;
                break;
            }
        }
        return a;
    }

    public void stampaStatistiche() {
       getLogger().info("non ci sono statistiche da stampare");
    }

}
