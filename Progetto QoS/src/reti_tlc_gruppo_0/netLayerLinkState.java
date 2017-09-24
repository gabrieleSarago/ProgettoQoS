/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reti_tlc_gruppo_0;

import base_simulator.Grafo;
import base_simulator.Messaggi;
import base_simulator.Nodo;
import base_simulator.layers.NetworkLayer;
import base_simulator.scheduler;
import java.util.ArrayList;

/**
 *
 * @author Ing. Amilcare Francesco Santamaria
 */
public class netLayerLinkState extends NetworkLayer {

    private final String HELLO_GREETINGS = "hello_pckt";
    private final String HELLO_TIMEOUT_MSG = "hello_timeout";
    private final String LSA_MSG = "LSA";

    final int HELLO_TIMEOUT = 15000;
    int TTL_LSA = 1;
    int seq_no = 0;

    final String UPDATE_RT_MSG = "update_routing_table";
    final int UPDATE_ROUTING_TABLE_COLLECTING_TIME = 2000;
    protected boolean tableIsChanged = false;

    private boolean first_hellp_msg_received = true;

    private LSDB myLinkStateDb;
    private boolean DEBUG = true;

    public int getTTL_LSA() {
        return TTL_LSA;
    }

    public void setTTL_LSA(int TTL_LSA) {
        this.TTL_LSA = TTL_LSA;
    }

    public boolean isHELLO_ENABLED() {
        return HELLO_ENABLED;
    }

    public void setHELLO_ENABLED(boolean HELLO_ENABLED) {
        this.HELLO_ENABLED = HELLO_ENABLED;
    }
    boolean HELLO_ENABLED = true;

    public netLayerLinkState(scheduler s, double tempo_di_processamento, Grafo grafo) {
        super(s, tempo_di_processamento, grafo);

        if (HELLO_ENABLED) {
            initializeProtocol();
        }

        myLinkStateDb = new LSDB();
    }

    /**
     * Questo metodo serve per controllare l'aggiornamento delle tabelle di
     * routing permettendo di collezionare informazioni mediante la raccolta di
     * LSA.
     */
    private void generateCollectingMessage() {
        Messaggi m = new Messaggi(UPDATE_RT_MSG, this, this, null, s.orologio.getCurrent_Time());
        m.isData = false;
        m.shifta(UPDATE_ROUTING_TABLE_COLLECTING_TIME);
        m.saliPilaProtocollare = false;

        s.insertMessage(m);
    }

    /**
     *
     * Viene gestito dalla classe che deve estendere il livello rete deve
     * gestire i messaggi di protocollo
     *
     * In questa versione gli hello_greetings sono anche utilizzati per
     * aggiornare la metrica del nodo In particolare misurando il loro ritardo
     * E2E sarà possibile aggiornare il peso nella tabella di routing
     *
     * @param m Messaggio di protocollo da gestire
     */
    @Override
    public void gestisciPacchettoProtocollo(Messaggi m) {
        //Inserire qui i dati per statistiche
        super.nr_pkt_prt++;
        super.delay_medio_pkt_prt += s.orologio.getCurrent_Time() - m.getTempo_di_partenza();

        if (m.getTipo_Messaggio().equals(HELLO_TIMEOUT_MSG)) {
           // System.out.println("D:" + ((Nodo) super.nodo).getTipo() + ": T:" + s.orologio.getCurrent_Time() + ": Arrivato messagio di generazione HELLO");
            getLogger().info("D: {}: T:{} : Arrivato messagio di generazione HELLO",((Nodo) super.nodo).getTipo(),s.orologio.getCurrent_Time());
            sendHelloGreetingMessage();
        } else if (m.getTipo_Messaggio().equals(this.HELLO_GREETINGS)) {
            //System.out.println("\nD:" + ((Nodo) super.nodo).getTipo() + ": ID :" + ((Nodo) super.nodo).getId() + " T:" + s.orologio.getCurrent_Time() + ":Arrivato messaggio di HELLO");
            getLogger().info("\nD:{} : ID :{} T: {} :Arrivato messaggio di HELLO",((Nodo) super.nodo).getTipo(),((Nodo) super.nodo).getId(),s.orologio.getCurrent_Time());
            if (DEBUG) {
               // System.out.println("D:" + s.orologio.getCurrent_Time() + " Tabella di routing pre-greetings");
                getLogger().info("D:{} Tabella di routing pre-greetings",s.orologio.getCurrent_Time());
                super.myRoutingTable.printTR();
            }
            int id_nodo_sorgente = ((Nodo) m.getNodoSorgente()).getId();
            int myId = ((Nodo) super.nodo).getId();
            if (super.myRoutingTable.controllaPresenzaLinea(id_nodo_sorgente, id_nodo_sorgente) >= 0) {
                //Vuol dire che la linea è gia presente, dobbiamo aggiornare il peso
                double new_peso = super.s.orologio.getCurrent_Time() - m.getTempo_di_partenza();
//                boolean esito = super.myRoutingTable.setPeso(id_nodo_sorgente, id_nodo_sorgente, new_peso);

                boolean esito = super.myGrafo.setCosto(myId, id_nodo_sorgente, new_peso, m.getTempo_di_partenza());

                //Setto questa variabile a true perchè indica al livello rete che la tabella è cambiata
                if(esito == true)
                {
                    tableIsChanged = true;
                }

                //LSA sarà inviato dal nodo se la topologia sarà cambiata a valle dello scattare del collecting TIMEOUT
            } else {
                //Il vicino non è presente nel grafo dobbiamo aggiungerlo
                double new_peso = super.s.orologio.getCurrent_Time() - m.getTempo_di_partenza();
//                super.myRoutingTable.addEntry(id_nodo_sorgente, id_nodo_sorgente, new_peso);
                super.myGrafo.setCosto(myId, id_nodo_sorgente, new_peso, m.getTempo_di_partenza());
                tableIsChanged = true;
            }
            if (DEBUG) {
               // System.out.println("D:" + s.orologio.getCurrent_Time() + " Tabella di routing post-greetings");
                getLogger().info("D:{} Tabella di routing post-greetings",s.orologio.getCurrent_Time());
                super.myRoutingTable.printTR();
                //System.out.println("\nD:" + s.orologio.getCurrent_Time() + " FINE GESTIONE GREETINGS");
                getLogger().info("D:{} FINE GESTIONE GREETINGS",s.orologio.getCurrent_Time());
            }
        } else if (m.getTipo_Messaggio().equals(UPDATE_RT_MSG)) {
           // System.out.println("D:" + s.orologio.getCurrent_Time() + " Il nodo " + ((Nodo) nodo).getId() + " è pronto ad eseguire algoritmo per aggiornare le TR ");
            getLogger().info("D:{} Il nodo {} è pronto ad eseguire algoritmo per aggiornare le TR ",s.orologio.getCurrent_Time(),((Nodo) nodo).getId() );
            //Settata a true da un LSA ovvero da un helloGreetings che ha portato modifiche al costo
            if (tableIsChanged == true) {
                //Update routing table executing routing algorithm
              //  System.out.println("TR Aggiornata");
                getLogger().info("TR Aggiornatqa");
                tableIsChanged = false;

               // System.out.println("D:" + ((Nodo) super.nodo).getId() + " Aggiorna TR");
                getLogger().info("D:{} Aggiorna TR",((Nodo) super.nodo).getId() );
                boolean update_db = myLinkStateDb.getLSDB(super.myGrafo);

                if (update_db == true) {
                   // System.out.println("\n\n\nPRIMA DI AGGIORNAMENTO ");
                    getLogger().info("PRIMA DI AGGIORNAMENTO");
                    super.myRoutingTable.printTR();
                    super.decisioner.updateRoutingTable();
                    //System.out.println(" A SEGUITO DI AGGIORNAMENTO ");
                    getLogger().info("A SEGUITO DI AGGIORNAMeNTO");
                    super.myRoutingTable.printTR();
                    //System.out.println("D:" + ((Nodo) super.nodo).getId() + " Aggiornata TR invio nuovo LSA");
                    getLogger().info("D: {} Aggiornata TR invio nuovo LSA",((Nodo) super.nodo).getId());
                    //devo generare un nuovo LSA e inviarlo ai miei vicini
                    this.sendLSA();
                }
            }

            generateCollectingMessage();

        } else if (m.getTipo_Messaggio().equals(this.LSA_MSG)) {

            //System.out.println("D:" + s.orologio.getCurrent_Time() + " Il nodo " + ((Nodo) nodo).getId() + " Arrivato messaggio LSA ");
            getLogger().info("D: {} Il nodo {} Arrivato messaggio LSA ", s.orologio.getCurrent_Time(),((Nodo) nodo).getId());
            LSA_packet lsa = (LSA_packet) m.getData();

            if (myLinkStateDb.checkLsaPresence(lsa.getSorgente(), lsa.seq_no) == false) {
                System.out.println("D:Nuovo LSA: Aggiungo a collection ");
                //inserisco il la coppia sorgente - no_seq nel lsdb
                myLinkStateDb.lsdb_add_packet(lsa.getSorgente(), lsa.seq_no, s.orologio.getCurrent_Time());
                //inserisco il contenuto del messaggio nel LSDB
                myLinkStateDb.collection.add(lsa.getGrafo());
                //Avviso il collector che la tabella di routing può essere aggiornata se necessagio perchè
                //Ho ricevuto un LSA contenente una topologia
                tableIsChanged = true;

                //Controlla TTL e nel caso invia sulle interfacce d'uscita
                if (lsa.getTtl() > 1) {
                    lsa.setTtl(lsa.getTtl() - 1);
                    lsa.setRilancio(((Nodo) super.nodo).getId());
                    Nodo myNodo = ((Nodo) super.nodo);
                    ArrayList<Integer> nodes = super.myRoutingTable.getNeighbours();
                    for (Object node_id : nodes) {
                        Nodo n = myNodo.getInfo().getNodo((Integer) node_id);
                        Messaggi m1 = new Messaggi(LSA_MSG, this, super.linkLayer, n, s.orologio.getCurrent_Time());
                        m1.setNextHop_id((Integer) node_id);
                        m1.shifta(this.tempo_di_processamento);
                        m1.setData(lsa);
                        m1.isData = false;
                        m1.saliPilaProtocollare = false;
                        m1.setSize(header + PDU_SIZE);

                        s.insertMessage(m1);
                    }
                }
            }

        } else {
            //System.out.println("Messaggio non gestibile da Questo Net invio alla classe super");
            getLogger().info("Messaggio non gestibile da Questo Net invio alla classe super");
            super.gestisciPacchettoDati(m);
        }
    }

    private void sendHelloGreetingMessage() {
        ArrayList<Integer> nodes = super.myRoutingTable.getNeighbours();
        for (Object n : nodes) {
            Nodo node = ((Nodo) super.nodo).getInfo().getNodo((Integer) n);
            Messaggi m = new Messaggi(HELLO_GREETINGS, this, super.linkLayer, node, s.orologio.getCurrent_Time());
            m.setNodoSorgente(nodo);
            m.saliPilaProtocollare = false;
            m.setNextHop(node);
            m.setNextHop_id(node.getId());
            m.shifta(super.tempo_di_processamento);
            m.setSize(PDU_SIZE + header);
            m.isData = false;
            s.insertMessage(m);
        }

        //prepare next messagge of hello
        Messaggi m1 = new Messaggi(HELLO_TIMEOUT_MSG, this, this, null, s.orologio.getCurrent_Time());
        m1.isData = false;
        m1.shifta(HELLO_TIMEOUT);
        m1.saliPilaProtocollare = false;
        s.insertMessage(m1);

        //If first hello message send lsa as well
        if (first_hellp_msg_received) {
            sendLSA();
            first_hellp_msg_received = false;
        }

    }

    private void generateHelloMessage() {
        Messaggi m1 = new Messaggi(HELLO_TIMEOUT_MSG, this, this, null, s.orologio.getCurrent_Time());
        m1.isData = false;
        m1.shifta(HELLO_TIMEOUT);
        m1.saliPilaProtocollare = false;
        s.insertMessage(m1);
    }

    private void initializeProtocol() {

        generateHelloMessage();
        generateCollectingMessage();

    }

    @Override
    public void Handler(Messaggi m) {               
        if (!m.isData) {
            gestisciPacchettoProtocollo(m);
        } else {
            super.Handler(m);
        }
    }

    public void enableFullOSPF() {
        HELLO_ENABLED = true;
    }

    private void sendLSA() {
        seq_no++;
        Nodo myNodo = ((Nodo) super.nodo);

//Devo inviare il messaggio in flooding sui nodi adiacenti
        ArrayList<Integer> nodes = super.myRoutingTable.getNeighbours();
        for (Object node_id : nodes) {
            LSA_packet lsa = new LSA_packet(seq_no, myNodo.getId(),
                    myNodo.getId(),
                    this.TTL_LSA,
                    super.myGrafo);

            Nodo n = myNodo.getInfo().getNodo((Integer) node_id);
            Messaggi m = new Messaggi(LSA_MSG, this, super.linkLayer, n, s.orologio.getCurrent_Time());
            m.setNextHop_id((Integer) node_id);
            m.shifta(this.tempo_di_processamento);
            m.setData(lsa);
            m.isData = false;
            m.saliPilaProtocollare = false;
            m.setSize(PDU_SIZE + header);

            s.insertMessage(m);
        }
    }
//TODO: Aggiungere gestione LSA nel gestisci pacchetto protocollo
}
