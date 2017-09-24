/**
 * jNET - SIM
 * V-0.1 - BETA RELEASE
 *
 * Ing. Amilcare Francesco Santamaria, Ph.D, 2016
 * Last rev. 23/01/2016
 *
 * All right reserved - This code can be used only for teaching activities!
 * Using of this full code or portion of it is not allowed for any other scope
 */
package base_simulator;

import java.util.ArrayList;

public class Decisioner {

    private tabellaRouting tr;
    private Grafo topology;
    private Integer myId;

    public int getMyAlgorithm() {
        return myAlgorithm;
    }

    public void setMyAlgorithm(int myAlgorithm) {
        this.myAlgorithm = myAlgorithm;
    }
    private int default_gateway = 0;

    final int DIJKSTRA = 1;
    private int myAlgorithm;

    /**
     * Costruttore deprecato
     *
     * @param tr
     */
    public Decisioner(tabellaRouting tr) {
        super();
        this.tr = tr;

        //TODO : Il grafo deve essere costruito mana mano con il protocollo
        this.topology = new Grafo(1);
    }

    /**
     * Cotruttore della classe Decisioner : Questa classe si preoccupa di
     * popolare le tabelle di routing del nodo
     *
     * @param routingTable - tabelle di instradamento condivisa con il network
     * layer
     * @param grafo - Rappresentazione astratta della topologia conosciuta dal
     * nodo
     * @param id - id del nodo che possiede il network layer che istanzia questo
     * decisioner
     */
    public Decisioner(tabellaRouting routingTable, Grafo grafo, int id) {
        super();
        this.tr = routingTable;

        this.topology = grafo;
//TODO: SARAA POSSIBILE CAMBIARE QUESTO COMPORTAMENTO INSERENDO UN NUOVO ALGORITMO
        myAlgorithm = DIJKSTRA;
        myId = id;
    }

    public tabellaRouting getTr() {
        return tr;
    }

    public int getDefault_gateway() {
        return default_gateway;
    }

    public int getNextHop(int dest) {
        int next_hop = tr.getNextHop(dest);
        if (next_hop < 0) {
            next_hop = default_gateway;
        }

        return next_hop;
    }

    public void setDefault_gateway(int default_gateway) {
        this.default_gateway = default_gateway;
    }

    public void setTr(tabellaRouting tr) {
        this.tr = tr;
    }

    public tabellaRouting findPath(int source, int destination) {
        //TODO : Return a single path from source to destination
        tabellaRouting res = this.tr;

        return res;
    }

    public void addRoutingEntry(int dest, int next_hop, double costo) {
        tr.addEntry(dest, next_hop, costo);
    }

    /**
     * Questo metodo permette di aggiornare le tabelle di routing dovrà essere
     * eseguito dal protocollo di rete che si trova all'interno del network
     * layer.
     *
     * CONSIGLIO: IMPLEMENTARE UNA CLASSE CHE ESTENDE IL NETWORK LAYER ED
     * EFFTTUARE L'OVERRIDE DEL METODO GESTISCI PACCHETTO PROTOCOLLO. QUESTO
     * PERMETTERA' DI CAMBIARE A PIACIMENTO IL COMPORTAMENTO DEL ROUTER
     */
    public void updateRoutingTable() {
        //Devo Eseguire algoritmo di Routing

        tr.removeEntries();

        switch (myAlgorithm) {
            case DIJKSTRA:
            default:
                executeDijkstraAlgorithm();
                break;

        }

    }

    private void executeDijkstraAlgorithm() {
        ArrayList<Integer> N = new ArrayList<Integer>();
        ArrayList<Integer> nodes = topology.getNodesExceptSource(myId);
        Grafo dijkstra = new Grafo(topology.getN());
        N.add(myId);

        double rigapesi[] = new double[topology.getN()];
        for (int i = 0; i < topology.getN(); i++) {
            rigapesi[i] = 9999.0; //-1 indica il peso infinito
        }
        rigapesi[myId] = 0.0;

        while (!nodes.isEmpty()) {
            for (Object n : N) {
                int source = (Integer) n;
                for (Object d : nodes) {
                    int dest = (Integer) d;
                    double value = topology.getCosto(source, dest);
                    if (value > 0.0) {
                        value = value + rigapesi[source];
                        //Indica che esiste una connessione tra il nodo sorgente e la destinazione
                        if (value < rigapesi[dest]) {
                            //Ho trovato un costo inferiore
                            rigapesi[dest] = value;
                            int padre = dijkstra.getPadre(dest);
                            if (padre > 0) {
                                dijkstra.setCosto(padre, dest, 0.0,0.0);
                            }

                            dijkstra.setCosto(source, dest, topology.getCosto(source, dest),0.0);

                        }
                    }
                }

            }
            int scelta = -1;
            for (Object d : nodes) {
                int dest = (Integer) d;
                if (scelta >= 0) {
                    if (rigapesi[dest] < rigapesi[scelta]) {
                        scelta = dest;
                    }
                } else {
                    scelta = dest;

                }
            }

            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i) == scelta) {
                    nodes.remove(i);
                    break;
                }
            }
            N.add(scelta);

        }

        nodes = topology.getNodesExceptSource(myId);
//System.out.println("D:Dijkstra:Preparo TR del nodo "+myId+" partendo dalla lista dei nodi ");
        while (!nodes.isEmpty()) {

            int dest = nodes.get(0);
//System.out.println("D:Dijkstra:Nodo sulla topologia: "+dest);            
            int next_hop = -1;
            double peso = 9999;
            for (int i = 0; i < dijkstra.getN(); i++) {
                if (dijkstra.getCosto(i, dest) > 0 && dijkstra.getCosto(i, dest) < peso) {
                    next_hop = i;
                    peso = dijkstra.getCosto(i, dest);
                }
            }
            if (next_hop == -1) {
                next_hop = dest;
                peso = 0.0;
            }

            if (next_hop != myId) {
                int padre = dijkstra.getPadre(next_hop);
                peso += dijkstra.getCosto(padre, next_hop);
                while (padre != myId) {
                    next_hop = padre;
                    padre = dijkstra.getPadre(next_hop);
                    peso += dijkstra.getCosto(padre, next_hop);
                }
            }
            else{
                next_hop = dest;
            }

            nodes.remove(0);
//System.out.println("D:Dijkstra:Nodo sulla topologia: "+dest+" NEXT HOP: "+next_hop);               
            tr.addEntry(dest, next_hop, peso);
        }

    }

}
