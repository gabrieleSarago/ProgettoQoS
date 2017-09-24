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
package base_simulator.layers.DV;

import base_simulator.DV.Grafo;
import base_simulator.DV.Messaggi;
import base_simulator.DV.schedulerDV;
import base_simulator.DV.RIP_Table;
import base_simulator.DV.DistanceTable;
import base_simulator.DV.DecisionerDV;
import base_simulator.DV.Entita;
import base_simulator.DV.NodoDV;
import reti_tlc_gruppo_DV.nodo_host;
import reti_tlc_gruppo_DV.nodo_router;

/**
 * il seguent e livello ha il compito di instradare i pacchetti e di occuparsi
 * del routing
 */
public class NetworkLayer extends Entita {

    protected schedulerDV s;
    protected LinkLayer linkLayer;
    protected double tempo_di_processamento;
    protected Object nodo;
    protected TransportLayer transportLayer;

    //Tabella di instradamento
    protected RIP_Table myRIPTable;
    protected DistanceTable myDistanceTable;

    protected double header = 20; //Byte

    //Da utilizzare per calcolare i percorsi per raggiungere le destinazioni
    protected DecisionerDV decisioner;
    protected Grafo myGrafo;

    /*Inizio variabili statistiche*/
    private float nr_richieste_accettate = 0;
    private float nr_richieste_rifiutate = 0;
    private float nr_servizi_chiusi = 0;
    private float nr_servizi_aperti = 0;

    public float nr_pkt_prt = 0;
    private float nr_pkt_dati = 0;

    public float delay_medio_pkt_prt = 0;
    private float delay_medio_pkt_dati = 0;
    private float jitter_medio = 0;
    private double arrivo_pkt_prec = 0.0;

    private int terminali_disconnessi = 0;
    private int terminali_attivati = 0;

    protected double PDU_SIZE = 1480; //Dimensione in byte della PDU

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
    public NetworkLayer(schedulerDV s, double tempo_di_processamento, Grafo grafo) {
        super(s, "Network Layer");
        this.s = s;
        this.tempo_di_processamento = tempo_di_processamento;

        this.myRIPTable = new RIP_Table();
        this.myDistanceTable = new DistanceTable();

        this.myGrafo = grafo;

    }

    /*Gestisce il pacchetto e il comportamento del nodo*/
    @Override
    public void Handler(Messaggi m) {
        //System.out.println("\nMessaggio giunto al NetworkLayer");
        if (m.isData) {
            gestisciPacchettoDati(m);
            m.incrHop();
        } else {
            gestisciPacchettoProtocollo(m);
        }
    }

    /**
     * Utilizzo questo metodo per connettere la pila protocollar
     *
     * @param transportLayer Livello Trasporto
     * @param linkLayer Livello LinkLayer
     * @param nodo NodoDV di appartenenza (container dei livelli)
     */
    public void connectNetworkLayer(TransportLayer transportLayer, LinkLayer linkLayer, Object nodo) {
        this.linkLayer = linkLayer;
        this.nodo = nodo;
        this.transportLayer = transportLayer;

        this.decisioner = new DecisionerDV(this.myRIPTable, ((NodoDV) nodo).getId(), this.myDistanceTable);
    }

    /**
     * Viene gestito dalla classe che deve estendere il livello rete deve
     * gestire la ricezione dei pacchetti dat
     *
     * @param m Messaggio dati da gestire sul livello
     */
    public void gestisciPacchettoDati(Messaggi m) {
        //System.out.println("\nE' arrivato un messaggio dati nel nodo " + ((NodoDV) this.nodo).getTipo() + " ID:" + ((NodoDV) this.nodo).getId() + " a livello di rete");

        if (m.saliPilaProtocollare == false) {
            //1. Questa condizione indica che il pacchetto dati è arrivato dai nodi superiori e 
            //deve essere inviato ai livelli sottostanti.
            //2. Il livello rete ha il compito di trovare la destinazione

            int next_hop = this.decisioner.getNextHop(((NodoDV) m.getNodoDestinazione()).getId());
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
            if(!m.isAck()){
            this.nr_pkt_dati++;
            this.delay_medio_pkt_dati += (s.orologio.getCurrent_Time() - m.getTempo_di_partenza());
            if (arrivo_pkt_prec > 0) {
                this.jitter_medio += s.orologio.getCurrent_Time() - arrivo_pkt_prec;
            }
            this.arrivo_pkt_prec = s.orologio.getCurrent_Time();}
            if (((NodoDV) this.nodo).getTipo().contains("host")) {

                if (((NodoDV) m.getNodoDestinazione()).getId() == ((NodoDV) this.nodo).getId()) {
                    //System.out.println("********Messaggio ARRIVATO a destinazione*******");

                    m.shifta(this.tempo_di_processamento);
                    m.setDestinazione(this.transportLayer);
                    m.setSorgente(this);
                    s.insertMessage(m);
                }
            } else {
                //TODO Devo inoltrare il pacchetto al prossimo nodo
                int next_hop = this.decisioner.getNextHop(((NodoDV) m.getNodoDestinazione()).getId());

                m.shifta(this.tempo_di_processamento);
                m.setSorgente(this);
                m.setDestinazione(this.linkLayer);
                m.setNextHop_id(next_hop);
                m.saliPilaProtocollare = false;
                s.insertMessage(m);
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
        int numeroLink=0;
        if(((NodoDV)this.nodo).getTipo().contains("host")){
            numeroLink=((nodo_host)this.nodo).getNicsSize();
        }
        if(((NodoDV)this.nodo).getTipo().contains("router")){
            numeroLink=((nodo_router)this.nodo).getNicsSize();
        }

        //TODO : Da aggiornare le statistiche del nodo
        String s = "\n--->Statistiche network Layer";
        s += "\nTipo Nodo " + ((NodoDV) nodo).getTipo();
        s += "\nId del Nodo " + ((NodoDV) nodo).getId();
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
        s += "\nNumero di link del nodo:" +numeroLink;
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
     * @param dest - NodoDV Destinazione
     * @param next_hop
     * @param costo
     */
    public void addRIPTableEntry(int dest, int next_hop, int num_hop) {
        decisioner.addRIPEntry(dest, next_hop, num_hop);
    }

    public void addDistanceTableEntry(int dest, int next_hop, double costo) {
        decisioner.addDistanceTableEntry(dest, next_hop, costo);
    }

    public DistanceTable getDistanceTable() {
        return this.myDistanceTable;
    }

}
