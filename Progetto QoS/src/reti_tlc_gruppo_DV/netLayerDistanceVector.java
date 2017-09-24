/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reti_tlc_gruppo_DV;

import base_simulator.DV.DistanceTable;
import base_simulator.DV.Grafo;
import base_simulator.DV.InfosDV;
import base_simulator.DV.Messaggi;
import base_simulator.DV.NodoDV;
import base_simulator.layers.DV.NetworkLayer;
import base_simulator.DV.schedulerDV;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ing. Amilcare Francesco Santamaria
 */
public class netLayerDistanceVector extends NetworkLayer {

    /*
        Dobbiamo chiedere se i messaggi di Hello vengono mandati solo all'inizio
        o peiodicamente.
     */
    private boolean RIP_TABLE = false;
    private final String GREETINGS_NEIGHBOURS = "greetings_neighbours";
    private final String NEIGHBOURS_TIMEOUT_MSG = "neighbours_timeout";
    private final String DV_MSG = "distance_vector";
    private final String DV_TIMEOUT_MSG = "dv_timeout";
    // lo utilizziamo per aggiornare sia le tabelle di routing che quelle delle distanze
    final String UPDATE_MSG = "update_table";
    final int UPDATE_TABLE_COLLECTING_TIME = 30000;
    boolean HELLO_ENABLED = true;
    
            boolean esegui=true;

    private boolean DEBUG = true;

    public boolean isHELLO_ENABLED() {
        return HELLO_ENABLED;
    }

    public void setHELLO_ENABLED(boolean HELLO_ENABLED) {
        this.HELLO_ENABLED = HELLO_ENABLED;
    }

    public netLayerDistanceVector(schedulerDV s, double tempo_di_processamento, Grafo grafo) {
        super(s, tempo_di_processamento, grafo);

        if (HELLO_ENABLED) {
            initializeProtocol();
        }

    }
    public void enableRipTable(){
        RIP_TABLE=true;
        super.decisioner.enableRipTable();
    }

    /**
     * Questo metodo serve per controllare l'aggiornamento delle tabelle di
     * routing permettendo di collezionare informazioni mediante la raccolta dei
     * distance vector.
     */
    private void generateCollectingMessage() {
        Messaggi m = new Messaggi(UPDATE_MSG, this, this, null, s.orologio.getCurrent_Time());
        m.isData = false;
        m.shifta(this.tempo_di_processamento);
        m.saliPilaProtocollare = false;

        s.insertMessage(m);
    }

    /**
     * Viene gestito dalla classe che deve estendere il livello rete deve
     * gestire i messaggi di protocollo
     * @param m Messaggio di protocollo da gestire
     */
    @Override
    public void gestisciPacchettoProtocollo(Messaggi m) {
        

        if (m.getTipo_Messaggio().equals(NEIGHBOURS_TIMEOUT_MSG)) {
            System.out.println("D:" + ((NodoDV) super.nodo).getTipo() + ": T:" + s.orologio.getCurrent_Time() + ": Arrivato messaggio di generazione Neighbours MSG");
            try {
                sendGreetingsMessage();
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(netLayerDistanceVector.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (m.getTipo_Messaggio().equals(DV_TIMEOUT_MSG)) {
            try {
                sendDVT();
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(netLayerDistanceVector.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (m.getTipo_Messaggio().equals(GREETINGS_NEIGHBOURS)) {
            System.out.println("\nD:" + ((NodoDV) super.nodo).getTipo() + ": ID :" + ((NodoDV) super.nodo).getId() + " T:" + s.orologio.getCurrent_Time() + ":Arrivato messaggio di Greetings");
            super.nr_pkt_prt++;
            super.delay_medio_pkt_prt += s.orologio.getCurrent_Time() - m.getTempo_di_partenza();
            if (DEBUG) {
                System.out.println("D:" + s.orologio.getCurrent_Time() + " Tabella di routing pre-greetings" + ((NodoDV) nodo).getId());
                super.myDistanceTable.printDT();
            }
            int id_nodo_sorgente = ((NodoDV) m.getNodoSorgente()).getId();
            int myId = ((NodoDV) super.nodo).getId();
            InfosDV info = s.getInfo();
            if (info.getLink(myId, id_nodo_sorgente) != null) {
                super.myDistanceTable.addEntry(id_nodo_sorgente, id_nodo_sorgente, super.myGrafo.getCosto(myId, id_nodo_sorgente));
                super.myRIPTable.addEntry(id_nodo_sorgente, id_nodo_sorgente, 1);
            }

            if (DEBUG) {
                System.out.println("D:" + s.orologio.getCurrent_Time() + " Tabella di routing post-greetings" + ((NodoDV) nodo).getId());
                //super.myRIPTable.printTR();
                //System.out.println("TABELLA DELLE DISTANZE:\n");
                super.myDistanceTable.printDT();
                System.out.println("\nD:" + s.orologio.getCurrent_Time() + " FINE GESTIONE GREETINGS");
            }
            try {
                sendDVT();
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(netLayerDistanceVector.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (m.getTipo_Messaggio().equals(UPDATE_MSG)) {
            if (RIP_TABLE) {
                System.out.println("D:" + s.orologio.getCurrent_Time() + " Il nodo " + ((NodoDV) nodo).getId() + "  pronto ad eseguire algoritmo per aggiornare le DT e RT ");
                System.out.println("\n\n\nPRIMA DI AGGIORNAMENTO ");
                super.myRIPTable.printRIP();
                super.decisioner.update();
                System.out.println(" A SEGUITO DI AGGIORNAMENTO ");
                super.myRIPTable.printRIP();
            }

        } else if (m.getTipo_Messaggio().equals(DV_MSG)) {
            super.nr_pkt_prt++;
            super.delay_medio_pkt_prt += s.orologio.getCurrent_Time() - m.getTempo_di_partenza();
            System.out.println("D:" + s.orologio.getCurrent_Time() + " Il nodo " + ((NodoDV) nodo).getId() + " Arrivata DVT ");
            int id_nodo_c = ((NodoDV) m.getNodoSorgente()).getId();
            DistanceTable dt_c = (DistanceTable) m.getDati();
            System.out.println("Tabella delle distanze di nodo" + ((NodoDV) nodo).getId() + " prima di aggiornare:");
            super.myDistanceTable.printDT();
            super.decisioner.updateDistanceTable(dt_c, id_nodo_c);
            System.out.println("Tabella delle distanze di nodo" + ((NodoDV) nodo).getId() + " dopo aggiornamento :");
            super.myDistanceTable.printDT();
            generateCollectingMessage();
            System.out.println("Aggiornato Distance Vector. Invia nuova DVT. ");
        } else {
            System.out.println("Messaggio non gestibile da Questo Net invio alla classe super");
            super.gestisciPacchettoProtocollo(m);
        }
    }

    private void sendGreetingsMessage() throws CloneNotSupportedException {
        ArrayList<Integer> nodes = super.myDistanceTable.getNeighbours();
        for (Object n : nodes) {
            NodoDV node = ((NodoDV) super.nodo).getInfo().getNodo((Integer) n);
            Messaggi m = new Messaggi(GREETINGS_NEIGHBOURS, this, super.linkLayer, node, s.orologio.getCurrent_Time());
            m.setNodoSorgente(nodo);
            m.saliPilaProtocollare = false;
            m.setNextHop(node);
            m.setDati(super.myDistanceTable.clone());
            m.setNextHop_id(node.getId());
            m.shifta(super.tempo_di_processamento);
            m.setSize(PDU_SIZE + header);
            m.isData = false;
            s.insertMessage(m);
        }
    }

    private void generateGreetingsNeighbours() {
        Messaggi m1 = new Messaggi(NEIGHBOURS_TIMEOUT_MSG, this, this, null, s.orologio.getCurrent_Time());
        m1.isData = false;
        m1.shifta(UPDATE_TABLE_COLLECTING_TIME);
        m1.saliPilaProtocollare = false;
        s.insertMessage(m1);
    }

    private void initializeProtocol() {
        generateGreetingsNeighbours();
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

    void enableRIP() {
        HELLO_ENABLED = true;
    }

    private void sendDVT() throws CloneNotSupportedException {
        NodoDV myNodo = ((NodoDV) super.nodo);
        ArrayList<Integer> nodes = super.myDistanceTable.getNeighbours();
        for (Object node_id : nodes) {
            NodoDV n = myNodo.getInfo().getNodo((Integer) node_id);
            Messaggi m = new Messaggi(DV_MSG, this, super.linkLayer, n, s.orologio.getCurrent_Time());
            m.setNodoSorgente(this.nodo);
            m.setNextHop_id((Integer) node_id);
            m.shifta(this.tempo_di_processamento);
            m.setDati(super.myDistanceTable.clone());
            m.isData = false;
            m.saliPilaProtocollare = false;
            m.setSize(PDU_SIZE + header);

            s.insertMessage(m);
        }
        Messaggi m1 = new Messaggi(DV_TIMEOUT_MSG, this, this, null, s.orologio.getCurrent_Time());
        m1.isData = false;
        m1.shifta(UPDATE_TABLE_COLLECTING_TIME);
        m1.saliPilaProtocollare = false;
        s.insertMessage(m1);
    }

}
