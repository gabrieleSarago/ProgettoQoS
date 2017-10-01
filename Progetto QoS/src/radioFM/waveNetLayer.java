/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package radioFM;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import base_simulator.Grafo;
import base_simulator.Messaggi;
import base_simulator.Nodo;
import base_simulator.RoutingRow;
import base_simulator.scheduler;
import base_simulator.layers.LinkLayer;
import base_simulator.layers.NetworkLayer;
import base_simulator.layers.TransportLayer;

/**
 *
 * @author afsan_000
 */
public class waveNetLayer extends NetworkLayer {

    final String DISCOVER_NEIGHBOURS = "DISCOVER_NEIGHBOURS";
    final String SEND_DISCOVER_NEIGHBOURS = "SEND_DISCOVER_NEIGHBOURS";
    final double DISCOVER_NEIGHBOURS_TIME = 5000.0; //Time in ms
    final double NEIGHBOUR_THRESHOLD = 250.0; //Distance in meter
    final double LIGH_SPEED = 3.0 * 10E8; //C
    final double Grx = 2; //Guadagno antenna in rx
    final double Gtx = 2; //Guadagno antenna in tx
    final double ff = 2; //Fading factor in URBAN AREA
    final double wave_length = 0.051; //Lunghezza d'onda alla frequenza 5.0 GHz IEEE802.11
    final double pw_tx = 20; // potenza in trasmissione
    double pw_rx = 0;
    private final String POWER_OFF = "car power off";

    HashMap<String, RoutingRow> routes = new HashMap<String, RoutingRow>();
    Graph network;
    Dijkstra mst;
    Viewer viewer;

    //Variabili statisticche
    double pacchettiGestitiRx = 0;
    double pacchettiGestitiTx = 0;
    double ritardoE2E = 0;

    double averageNodeDistance = 0;
    double counterAverageNodeDistance = 0;

    double averagePwRx = 0;
    double counterAvgPwRx = 0;

    public waveNetLayer(scheduler s, double tempo_di_processamento, Grafo grafo) {
        super(s, tempo_di_processamento, grafo);
        sendDiscoverMessage();
        network = new SingleGraph("network");
        mst = new Dijkstra(Dijkstra.Element.EDGE, null, "cost");
        mst.init(network);
        viewer = network.display();

//        viewer.disableAutoLayout();
    }

    public waveNetLayer(scheduler s, double tempo_di_processamento, Grafo grafo, int showUI) {
        super(s, tempo_di_processamento, grafo);
        sendDiscoverMessage();
        network = new SingleGraph("network");
        mst = new Dijkstra(Dijkstra.Element.EDGE, null, "cost");
        mst.init(network);
        if (showUI == 1) {
            viewer = network.display();
        }
    }

    @Override
    public void Handler(Messaggi m) {
        //Generate discover message
        if (m.getTipo_Messaggio().equals(DISCOVER_NEIGHBOURS)) {
            super.nr_pkt_prt++;
            MobileHost ns = (MobileHost) m.getNodoSorgente();
            double distance = getDistanceBetweenNodes(ns);
            if (distance < NEIGHBOUR_THRESHOLD) {

                //POSSO INSERIRE NODO NEI MIEI VICINI
                if (distance == 0) {
                    distance = 1;
                }
//                    System.out.println("Livello PwTX :" + pw_tx + " PwRx:" + pw_rx);
                // ptx/prx ratio        
                double comp = Math.pow((4 * Math.PI * distance), 2) / ((Grx * Gtx) * Math.pow(wave_length, 2));
                double path_loss = 10 * Math.log10(comp);
                pw_rx = pw_tx - path_loss;
                pw_rx = Math.pow(10, pw_rx / 10); //Calcolo potenza in mW da dBm
                averagePwRx += pw_rx;
                counterAvgPwRx++;

                //Calcolo della potenza ricevuta                 
                double cost = (averagePwRx / counterAvgPwRx) / pw_rx;
                String edgeLabel = ((Nodo) nodo).getId() + "-" + ns.getId();

                if (network.getNode("" + ns.getId()) == null) {
                    network.addNode("" + ns.getId());
                    Node node = network.getNode("" + ns.getId());
                    node.addAttribute("ui.style", "fill-color: blue; size: 25px,25px;");
                    node.addAttribute("label", node.toString());
                }
//                    double appCost = -1;
//                    if (network.getEdge(edgeLabel) == null) {
//                        network.addEdge(edgeLabel, "" + ((Nodo) nodo).getId(), "" + ns.getId());
//                        network.getEdge(edgeLabel).setAttribute("ui.style", "fill-color: blue;");
//                    } else {
//                        appCost = (Double) network.getEdge(edgeLabel).getAttribute("cost");
//                    }
//                    network.getEdge(edgeLabel).clearAttributes();
//                    network.getEdge(edgeLabel).addAttribute("cost", cost);
//                    if (appCost > 0 && appCost != cost) {
//                        mst.compute();
//                    }

//                viewer.enableAutoLayout();
                RoutingRow entry = new RoutingRow(ns.getId(), ns.getId(), cost);
                routes.put("" + ns.getId(), entry);

                for (HashMap.Entry<String, RoutingRow> e : ((HashMap<String, RoutingRow>) m.getData()).entrySet()) {
                    RoutingRow r = e.getValue();
                    int idNodoCorrente = ((MobileHost) nodo).getId();
                    int destId = Integer.parseInt(e.getKey());
                    int nextHop = ns.getId();
                    double costo_next_hop_dest = r.getCosto();
                    double costo_sorg_next_hop = routes.get("" + ns.getId()).getCosto();

                    if (routes.containsKey("" + e.getKey())) {
                        double currentCost = routes.get("" + destId).getCosto();
                        if (costo_next_hop_dest + costo_sorg_next_hop < currentCost) {
                            RoutingRow r1 = new RoutingRow(destId,
                                    nextHop,
                                    costo_next_hop_dest + costo_sorg_next_hop);

                            routes.put(e.getKey(), r1);
//                            if (network.getNode("" + r.getNodoDestinazione()) == null) {
//                                network.addNode("" + r1.getNodoDestinazione());
//                                network.getNode("" + r1.getNodoDestinazione()).addAttribute("label", "" + r1.getNodoDestinazione());
//                                network.getNode("" + r1.getNodoDestinazione()).addAttribute("ui.style", "fill-color: yellow; size: 25px,25px;");
//
//                                network.addEdge(r1.getNextHop() + "-" + r1.getNodoDestinazione(), "" + r1.getNextHop(), "" + r1.getNodoDestinazione());
//                                network.getEdge(edgeLabel).setAttribute("ui.style", "fill-color: red;");
//                                network.getEdge(edgeLabel).addAttribute("label", "rosso");
//                                
//                            } else {
//                                for (Edge edge : network.getEdgeSet()) {
//                                    if (edge.getNode1().toString().equals("" + r1.getNodoDestinazione())) {
//                                        try {
//                                            network.removeEdge(edge);
//                                            String newEdgeLabel = r1.getNextHop() + "-" + r1.getNodoDestinazione();
//                                            if(network.getEdge(newEdgeLabel) == null){
//                                                network.addEdge(r1.getNextHop() + "-" + r1.getNodoDestinazione(), "" + r1.getNextHop(), "" + r1.getNodoDestinazione());
//                                                network.getEdge(edgeLabel).setAttribute("ui.style", "fill-color: green;");
//                                            }
//                                            break;
//                                        } catch (Exception ex) {
//                                            ex.printStackTrace();
//                                        }
//                                    }
//                                }
//
//                            }
                        }
                    } else if (Integer.parseInt(e.getKey()) != ((MobileHost) nodo).getId()) {

                        RoutingRow r1 = new RoutingRow(destId,
                                nextHop,
                                costo_next_hop_dest + costo_sorg_next_hop);

                        routes.put(e.getKey(), r1);
//                        if (network.getNode("" + r.getNodoDestinazione()) == null) {
//                            network.addNode("" + r1.getNodoDestinazione());
//                            network.getNode("" + r1.getNodoDestinazione()).addAttribute("label", "" + r1.getNodoDestinazione());
//                            network.getNode("" + r1.getNodoDestinazione()).addAttribute("ui.style", "fill-color: yellow; size: 25px,25px;");
//                            network.addEdge("" + r1.getNextHop() + "" + r1.getNodoDestinazione(), "" + r1.getNextHop(), "" + r1.getNodoDestinazione());
//                            network.getEdge(edgeLabel).setAttribute("ui.style", "fill-color: yellow;");
//                        }
                    }
                }

//                System.out.println(s.orologio.getCurrent_Time() + " Inserisco un nuovo vicino " + ((NodoMacchina) nodo).getId() + "->" + ((NodoMacchina) m.getNodoSorgente()).getId() + "(" + cost + ")");
//                System.out.println(routes);
            } else {
                //Se i nodi sono troppo distanti dobbiamo rimuovere tutte le entries dove 
                //abbiamo come next-hop il nodo che ha inviato il messaggio
                boolean noActionDone = false;
                while (noActionDone == false) {
                    noActionDone = true;
                    for (HashMap.Entry<String, RoutingRow> e : routes.entrySet()) {
                        RoutingRow r = e.getValue();
                        if (r.getNextHop() == ns.getId()) {
                            routes.remove("" + r.getNodoDestinazione());
                            if (network.getNode("" + r.getNodoDestinazione()) != null) {
                                network.removeNode("" + r.getNodoDestinazione());
                            }
                            noActionDone = false;
                            break;
                        }
                    }
                }
            }
//            network.clear();
//            network.addNode("" + ((Nodo) this.nodo).getId());
//            network.getNode("" + ((Nodo) this.nodo).getId()).addAttribute("ui.style", "fill-color: red; size: 25px,25px;");
//            for (HashMap.Entry<String, RoutingRow> e : routes.entrySet()) {
//                String node1 = "" + e.getValue().getNextHop();
//                String node2 = "" + e.getValue().getNodoDestinazione();
//                if (network.getNode(node1) == null) {
//                    network.addNode(node1);
//                    network.getNode(node1).addAttribute("label", node1);
//                    network.getNode(node1).addAttribute("ui.style", "fill-color: blue; size: 25px,25px;");
//                }
//                if (network.getNode(node2) == null) {
//                    network.addNode(node2);
//                    network.getNode(node2).addAttribute("label", node2);
//                    network.getNode(node2).addAttribute("ui.style", "fill-color: blue; size: 25px,25px;");
//                }
//
//                if(node1.equals(node2))
//                {                    
//                   String edgeLabel = ((Nodo) this.nodo).getId() + "-" + node2;
//                   network.addEdge(edgeLabel, "" + ((Nodo) this.nodo).getId(), node2);
//                }
//                else
//                {
//                   String edgeLabel = node1 + "-" + node2;
//                   network.addEdge(edgeLabel, node1, node2);
//                }
//                
//            }
//
//            mst.compute();
//            try {
//                BufferedWriter bw = new BufferedWriter(new FileWriter("output/routes_" + ((NodoMacchina) nodo).getId() + ".txt", true));
//                bw.write("\nTempo:" + s.orologio.getCurrent_Time() + "\n");
//                bw.write("Destinazione\t NEXTHOP\t Costo\n");
//                for (HashMap.Entry<String, RoutingRow> e : routes.entrySet()) {
//                    RoutingRow r = e.getValue();
//                    bw.write(r.getNodoDestinazione() + "\t" + r.getNextHop() + "\t" + r.getCosto() + "\n");
//                }
//                bw.write("=========================\n");
//                bw.close();
//            } catch (IOException ex) {
//                Logger.getLogger(waveNetLayer.class.getName()).log(Level.SEVERE, null, ex);
//            }
            updateNetwork();
        } else if (m.getTipo_Messaggio().equals(SEND_DISCOVER_NEIGHBOURS)) {
            MobileHost n = (MobileHost) nodo;

            //Send messages in broadcasting
            ArrayList<Nodo> nodes = n.getInfo().getNodes();
            for (Nodo ns : nodes) {
                if (ns.getId() != n.getId()) {
                    Messaggi m1 = new Messaggi(DISCOVER_NEIGHBOURS, this, this.linkLayer, ns, s.orologio.getCurrent_Time());
                    m1.shifta(this.tempo_di_processamento);
                    m1.saliPilaProtocollare = false;
                    m1.setNodoSorgente(nodo);
                    m1.setNodoDestinazione(ns);
                    m1.setNextHop(ns);
                    m1.setNextHop_id(ns.getId());
                    m1.setData(routes);
                    s.insertMessage(m1);
                }
            }

            sendDiscoverMessage();
        } else if (m.getTipo_Messaggio().equals(this.POWER_OFF)) {
            MobileHost n = (MobileHost) m.getNodoSorgente();
            if (routes.containsKey("" + n.getId())) {
                routes.remove("" + n.getId());
                updateNetwork();
            }

        } else {
            super.nr_pkt_dati++;
            if (m.saliPilaProtocollare == true) {
                gestisciPacchettoLivelloLink(m);
            } else {
                gestisciPacchettoLivelloTransport(m);
            }
        }
    }

    private void sendDiscoverMessage() {
        Messaggi m = new Messaggi(SEND_DISCOVER_NEIGHBOURS, this, this, this, s.orologio.getCurrent_Time());
        m.shifta(this.DISCOVER_NEIGHBOURS_TIME);
        m.saliPilaProtocollare = false;
        s.insertMessage(m);

    }

    private double getDistanceBetweenNodes(MobileHost ns) {
        double res = 0;
        MobileHost n = (MobileHost) nodo;
        res = Math.sqrt(Math.pow((ns.currX - n.currX), 2) + Math.pow((ns.currY - n.currY), 2));

        averageNodeDistance += res;
        counterAverageNodeDistance++;

        return res;
    }

    private void gestisciPacchettoLivelloLink(Messaggi m) {
        ritardoE2E += s.orologio.getCurrent_Time() - m.getTempo_di_partenza();
        pacchettiGestitiRx++;
        if (((MobileHost) m.getNodoDestinazione()).getId() == ((MobileHost) nodo).getId()) {
            m.shifta(this.tempo_di_processamento);
            m.setSorgente(this);
            m.setDestinazione(this.transportLayer);
            s.insertMessage(m);
        } else {
            int id_dest = ((MobileHost) m.getNodoDestinazione()).getId();
            if(routes.get("" + id_dest) != null)
            {
            m.setNextHop_id(routes.get("" + id_dest).getNextHop());
            m.setNextHop(((Nodo) nodo).getInfo().getNodo(id_dest));
            m.shifta(this.tempo_di_processamento);
            m.setSorgente(this);
            m.setDestinazione(this.linkLayer);

            m.saliPilaProtocollare = false;

            s.insertMessage(m);
            }
        }

    }

    private void gestisciPacchettoLivelloTransport(Messaggi m) {
        //Bisogna trovare il percorso per raggiungere la destinazione utilizzando
        //Algoritmo di dijkstra nel caso non esista entry nelle routes
        int destId = 0;
        try {
            destId = ((Nodo) m.getNodoDestinazione()).getId();
        } catch (Exception e) {
            e.printStackTrace();

        }
        m.setDestinazione(this.linkLayer);
        m.setSorgente(this);
        if (routes.containsKey("" + destId)) {
            RoutingRow entry = routes.get("" + destId);
            int nodeId = entry.getNextHop();
            m.setNextHop(((Nodo) nodo).getInfo().getNodo(nodeId));
            m.setNextHop_id(nodeId);
            m.shifta(this.tempo_di_processamento);
            s.insertMessage(m);
        } else {
            mst.compute();
            for (Node node : mst.getPathNodes(network.getNode("" + destId))) {
//                node.addAttribute("ui.style", "fill-color: blue; size: 25px,25px;");                        
                int nodeId = Integer.parseInt(node.getAttribute("label").toString());
                m.setNextHop(((Nodo) nodo).getInfo().getNodo(nodeId));
                m.setNextHop_id(nodeId);
                m.shifta(this.tempo_di_processamento);
                s.insertMessage(m);
                break;
            }
        }

        pacchettiGestitiTx++;
    }

    @Override
    public void connectNetworkLayer(TransportLayer transportLayer, LinkLayer linkLayer, Object nodo) {
        super.connectNetworkLayer(transportLayer, linkLayer, nodo);
        network.addNode("" + ((Nodo) nodo).getId());
        Node node = network.getNode("" + ((Nodo) nodo).getId());
        node.addAttribute("ui.style", "fill-color: red; size: 35px,35px;");
        node.addAttribute("label", node.toString());

        mst.setSource(network.getNode("" + ((Nodo) nodo).getId()));

    }

    @Override
    public String getStat() {
        String res = "";
        res += "STATISTICHE WAVE NETLAYER......\n";
        res += "NODO ID: " + ((MobileHost) nodo).getId() + "\n";
        res += "Pacchetti Ricevuti :" + pacchettiGestitiRx + "\n";
        res += "Pacchetti Inviati  :" + pacchettiGestitiTx + "\n";
        res += "Average E2E........:" + ritardoE2E / pacchettiGestitiRx + "\n";
        res += "Numero pckt prot...:" + super.nr_pkt_prt + "\n";
        res += "Numero pckt dati...:" + super.nr_pkt_dati + "\n";
        return res;
    }

    private void updateNetwork() {
        network.clear();
        network.addNode("" + ((Nodo) this.nodo).getId());
        network.getNode("" + ((Nodo) this.nodo).getId()).addAttribute("label", ""+((Nodo) this.nodo).getId());
        network.getNode("" + ((Nodo) this.nodo).getId()).addAttribute("ui.style", "fill-color: red; size: 25px,25px;");
        for (HashMap.Entry<String, RoutingRow> e : routes.entrySet()) {
            String node1 = "" + e.getValue().getNextHop();
            String node2 = "" + e.getValue().getNodoDestinazione();
            if (network.getNode(node1) == null) {
                network.addNode(node1);
                network.getNode(node1).addAttribute("label", node1);
                network.getNode(node1).addAttribute("ui.style", "fill-color: blue; size: 25px,25px;");
            }
            if (network.getNode(node2) == null) {
                network.addNode(node2);
                network.getNode(node2).addAttribute("label", node2);
                network.getNode(node2).addAttribute("ui.style", "fill-color: blue; size: 25px,25px;");
            }

            if (node1.equals(node2)) {
                String edgeLabel = ((Nodo) this.nodo).getId() + "-" + node2;
                network.addEdge(edgeLabel, "" + ((Nodo) this.nodo).getId(), node2);
            } else {
                String edgeLabel = node1 + "-" + node2;
                network.addEdge(edgeLabel, node1, node2);
            }

        }

        mst.compute();
        /*try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("output/routes_" + ((MobileHost) nodo).getId() + ".txt", true));
            bw.write("\nTempo:" + s.orologio.getCurrent_Time() + "\n");
            bw.write("Destinazione\t NEXTHOP\t Costo\n");
            for (HashMap.Entry<String, RoutingRow> e : routes.entrySet()) {
                RoutingRow r = e.getValue();
                bw.write(r.getNodoDestinazione() + "\t" + r.getNextHop() + "\t" + r.getCosto() + "\n");
            }
            bw.write("=========================\n");
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(waveNetLayer.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
}
