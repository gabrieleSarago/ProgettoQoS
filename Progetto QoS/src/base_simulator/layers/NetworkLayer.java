/*
 * TransportLayer.java
 *
 * Created on 10 ottobre 2007, 8.45
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 * @author franco
 *
 */
package base_simulator.layers;



import base_simulator.*;
import org.slf4j.Logger;

/**
 * il seguent e livello ha il compito di instradare i pacchetti e di occuparsi
 * del routing
 */
public class NetworkLayer extends Entita {

    protected scheduler s;
    protected LinkLayer linkLayer;
    protected double tempo_di_processamento;
    protected Object nodo;
    protected TransportLayer transportLayer;

    //Tabella di instradamento
    protected tabellaRouting myRoutingTable;

    protected double header = 20; //Byte

    //Da utilizzare per calcolare i percorsi per raggiungere le destinazioni
    protected Decisioner decisioner;
    protected Grafo myGrafo;

    /*Inizio variabili statistiche*/
    private float nr_richieste_accettate = 0;
    private float nr_richieste_rifiutate = 0;
    private float nr_servizi_chiusi = 0;
    private float nr_servizi_aperti = 0;

    public int nr_pkt_prt = 0;
    public int nr_pkt_dati = 0;

    public float delay_medio_pkt_prt = 0;
    public float delay_medio_pkt_dati = 0;
    public float jitter_medio = 0;
    public double arrivo_pkt_prec = 0.0;

    private int terminali_disconnessi = 0;
    private int terminali_attivati = 0;
    
    protected double PDU_SIZE = 1480; //Dimensione in byte della PDU
   // final Logger logger = LoggerFactory.getLogger(NetworkLayer.class);

    public double getPDU_SIZE() {
        return PDU_SIZE;
    }

    public void setPDU_SIZE(double PDU_SIZE) {
        this.PDU_SIZE = PDU_SIZE;
    }

    /**
     * Creates a new instance of TransportLayer
     *
     * @param s Scheduler del simulatore
     * @param tempo_di_processamento
     * @param grafo
     */
    public NetworkLayer(scheduler s, double tempo_di_processamento, Grafo grafo) {
        super(s, "Network Layer");
        this.s = s;
        this.tempo_di_processamento = tempo_di_processamento;

        this.myRoutingTable = new tabellaRouting();

        this.myGrafo = grafo;

    }

    /*Gestisce il pacchetto e il comportamento del nodo*/
    @Override
    public void Handler(Messaggi m) {
        //System.out.println("\nMessaggio giunto al NetworkLayer");
        if (m.isData) {
            gestisciPacchettoDati(m);
        } else {
            gestisciPacchettoProtocollo(m);
        }
    }

    /**
     * Utilizzo questo metodo per connettere la pila protocollar
     *
     * @param transportLayer Livello Trasporto
     * @param linkLayer Livello LinkLayer
     * @param nodo Nodo di appartenenza (container dei livelli)
     */
    public void connectNetworkLayer(TransportLayer transportLayer, LinkLayer linkLayer, Object nodo) {
        this.linkLayer = linkLayer;
        this.nodo = nodo;
        this.transportLayer = transportLayer;

        this.decisioner = new Decisioner(this.myRoutingTable, myGrafo, ((Nodo) nodo).getId());
    }

    /**
     * Viene gestito dalla classe che deve estendere il livello rete deve
     * gestire la ricezione dei pacchetti dat
     *
     * @param m Messaggio dati da gestire sul livello
     */
    public void gestisciPacchettoDati(Messaggi m) {
       // System.out.println("\nE' arrivato un messaggio dati nel nodo " + ((Nodo) this.nodo).getTipo() + " ID:" + ((Nodo) this.nodo).getId() + " a livello di rete");
        getLogger().info("E' arrivato un messaggio dati nel nodo {} ID:{} a livello di rete",((Nodo) this.nodo).getTipo(),((Nodo) this.nodo).getId());
        if (m.saliPilaProtocollare == false) {
            //1. Questa condizione indica che il pacchetto dati è arrivato dai nodi superiori e 
            //deve essere inviato ai livelli sottostanti.
            //2. Il livello rete ha il compito di trovare la destinazione

            int next_hop = this.decisioner.getNextHop(((Nodo) m.getNodoDestinazione()).getId());
            m.setNextHop_id(next_hop);
            m.addHeader(header);
            m.setSorgente(this);
            m.shifta(this.tempo_di_processamento);
            m.setDestinazione(this.linkLayer);

            s.insertMessage(m);
        } else {
            //1. Il pacchetto dati è arrivato sul nodo e dobbiamo quindi gestirlo.
            //se il nodo è un router dobbiamo rilanciarlo se è un host dobbiamo controllare se è la 
            //destinazione del pacchetto

            //Aggiorna Statistiche
            
            this.nr_pkt_dati++;
            this.delay_medio_pkt_dati += (s.orologio.getCurrent_Time() - m.getTempo_di_partenza());
            if (arrivo_pkt_prec > 0) {
                this.jitter_medio += s.orologio.getCurrent_Time() - arrivo_pkt_prec;
            }
            this.arrivo_pkt_prec = s.orologio.getCurrent_Time();
            
            
            if (((Nodo) this.nodo).getTipo().contains("host")) {

                if (((Nodo) m.getNodoDestinazione()).getId() == ((Nodo) this.nodo).getId()) {
                    
                    
                    
                    System.out.println("********Messaggio ARRIVATO a destinazione*******");

                    m.shifta(this.tempo_di_processamento);
                    m.setDestinazione(this.transportLayer);
                    m.setSorgente(this);
                    s.insertMessage(m);
                }
            } else {
                //TODO Devo inoltrare il pacchetto al prossimo nodo
                int next_hop = this.decisioner.getNextHop(((Nodo) m.getNodoDestinazione()).getId());
                    
                    
                    
                m.shifta(this.tempo_di_processamento);
                m.setSorgente(this);
                m.setDestinazione(this.linkLayer);
                m.setNextHop_id(next_hop);
                m.saliPilaProtocollare = false;
                s.insertMessage(m);
                System.out.println("********INOLTRO PACCHETTO*******");
                    System.out.println("Nodo     :"+((Nodo)this.nodo).getId());
                    System.out.println("isData   :"+m.isData);
                    System.out.println("sorgente :"+((Nodo)m.getNodoSorgente()).getId());
                    System.out.println("destinazione :"+((Nodo)m.getNodoDestinazione()).getId());
                    System.out.println("next hop     :"+next_hop);
                    System.out.println("********FINE INOLTRO*******");
            }
        }

    }

    /**
     *
     * Viene gestito dalla classe che deve estendere il livello rete deve
     * gestire i messaggi di protocollo
     *
     * @param m Pacchetto protocollare da gestire
     */
    public void gestisciPacchettoProtocollo(Messaggi m) {
        if (m.getTipo_Messaggio().equals("controlla coda")) {
            System.out.println("Messaggio di controlla coda");
        } else {
            this.nr_pkt_prt++;
            this.delay_medio_pkt_prt += (s.orologio.getCurrent_Time() - m.getTempo_di_partenza());
        }
    }

    @Override
    public String getStat() {

        //TODO : Da aggiornare le statistiche del nodo
        String s = "\n--->Statistiche network Layer";
        s += "\nTipo Nodo " + ((Nodo) nodo).getTipo();
        s += "\nId del Nodo " + ((Nodo) nodo).getId();
        /*        
        s+="\nNumero di richieste accettate (Nr):"+this.nr_richieste_accettate;
        s+="\nNumero di richieste rifiutate (Nr):"+this.nr_richieste_rifiutate;
        s+="\nNumero di terminali attivati (Nr):"+this.terminali_attivati;
        s+="\nNumero di terminali disconnessi (Nr):"+this.terminali_disconnessi;
        s+="\nNumero di servizi aperti (Nr) :"+this.nr_servizi_aperti;
        s+="\nNumero di servizi chiusi (Nr) :"+this.nr_servizi_chiusi;
         */
        float pkt_tot = this.nr_pkt_prt + this.nr_pkt_dati;
        s += "\nNumero di pacchetti totali gestiti (Nr) :" + pkt_tot;
        s += "\nNumero di pacchetti prt gestiti (Nr) :" + this.nr_pkt_prt;
        s += "\nNumero di pacchetti dati gestiti (Nr) :" + this.nr_pkt_dati;
        if (pkt_tot > 0) {
            s += "\nPercentuale di OverHead (%) :" + (nr_pkt_prt / pkt_tot) * 100;
        }
        if (nr_pkt_prt > 0) {
            s += "\nDelay medio pkt protocollo (ms) :" + this.delay_medio_pkt_prt / nr_pkt_prt;
        }
        if (nr_pkt_dati > 1) {
            s += "\nDelay medio pkt dati (ms) :" + this.delay_medio_pkt_dati / nr_pkt_dati;
            s += "\nDelay jitter Medio pkt dati (ms) :" + this.jitter_medio / (nr_pkt_dati - 1);
        }
        return s;
    }

    public void setDefaultGateway(int gateway) {
        this.decisioner.setDefault_gateway(gateway);
    }

    /**
     * Aggiungo una destinazione alla tabella di routing attraverso il
     * decisioneer
     *
     * @param dest - Nodo Destinazione
     * @param next_hop
     * @param costo
     */
    public void addRoutingTableEntry(int dest, int next_hop, double costo) {
        decisioner.addRoutingEntry(dest, next_hop, costo);
    }
    
    public void stampaStatistiche(){
            //TODO : Da aggiornare le statistiche del nodo
        Logger logger = getLogger();
        logger.info("statistiche del network layer");
        String s = "\n--->Statistiche network Layer";
        s += "\nTipo Nodo " + ((Nodo) nodo).getTipo();
        s += "\nId del Nodo " + ((Nodo) nodo).getId();
        /*        
        s+="\nNumero di richieste accettate (Nr):"+this.nr_richieste_accettate;
        s+="\nNumero di richieste rifiutate (Nr):"+this.nr_richieste_rifiutate;
        s+="\nNumero di terminali attivati (Nr):"+this.terminali_attivati;
        s+="\nNumero di terminali disconnessi (Nr):"+this.terminali_disconnessi;
        s+="\nNumero di servizi aperti (Nr) :"+this.nr_servizi_aperti;
        s+="\nNumero di servizi chiusi (Nr) :"+this.nr_servizi_chiusi;
         */
        float pkt_tot = this.nr_pkt_prt + this.nr_pkt_dati;
        s += "\nNumero di pacchetti totali gestiti (Nr) :" + pkt_tot;
        s += "\nNumero di pacchetti prt gestiti (Nr) :" + this.nr_pkt_prt;
        s += "\nNumero di pacchetti dati gestiti (Nr) :" + this.nr_pkt_dati;
        if (pkt_tot > 0) {
            s += "\nPercentuale di OverHead (%) :" + (nr_pkt_prt / pkt_tot) * 100;
        }
        if (nr_pkt_prt > 0) {
            s += "\nDelay medio pkt protocollo (ms) :" + this.delay_medio_pkt_prt / nr_pkt_prt;
        }
        if (nr_pkt_dati > 1) {
            s += "\nDelay medio pkt dati (ms) :" + this.delay_medio_pkt_dati / nr_pkt_dati;
            s += "\nDelay jitter Medio pkt dati (ms) :" + this.jitter_medio / (nr_pkt_dati - 1);
        }
        
        logger.info(s);
    }
}
