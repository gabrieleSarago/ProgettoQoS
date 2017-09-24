/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package radioFM;

import java.util.ArrayList;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import Mobility.MobilityMap;
import base_simulator.Grafo;
import base_simulator.Messaggi;
import base_simulator.Nodo;
import base_simulator.canale;
import base_simulator.scheduler;
import base_simulator.layers.LinkLayer;
import base_simulator.layers.NetworkLayer;
import base_simulator.layers.TransportLayer;
import base_simulator.layers.physicalLayer;
import reti_tlc_gruppo_0.nodo_host;

/**
 *
 * @author franco
 */
public class MobileHost extends nodo_host {

    final String UPDATE_POSITION = "update_pos";
    final String START_ROAD_RUN = "start_road_run";
    final double UPDATE_POSITION_TIME = 1000.0; //UPDATE_POSITION_TIME
    final double STOP_WAITING_TIME = 10000.0; //WAIT AT ROAD_CROSS
    String nodo_ingresso;
    String nodo_uscita;
    int index_nodo_attuale;
    
    //stazione radio scelta dal mobile host
    private String stazione;
    
    /*raggio di copertura della BaseStation
     * 130 = 65km * 2
     * essendo la mappa in rapporto 1:2
     */
    final int BS_RADIUS = 130;
    //raggio del cerchio rappresentante questo mobile host
    final int RADIUS = 10;
    //ID base station a cui il mh è registrato
    String currBS = "";
    
    double currX = 0;
    double currY = 0;
    double currDistance = 0;

    MobilityMap cityMap;
    Graph mappa;
    Dijkstra dijkstra;

    ArrayList<Node> list1;
    private canale my_wireless_channel;
    
    private boolean carIsPowerOff = true;
    private String POWER_OFF = "car power off";
        
    public canale getMy_wireless_channel() {
        return my_wireless_channel;
    }

    public void setMy_wireless_channel(canale my_wireless_channel) {
        this.my_wireless_channel = my_wireless_channel;
    }

    public MobileHost(scheduler s, int id_nodo, physicalLayer myPhyLayer, LinkLayer myLinkLayer, NetworkLayer myNetLayer, TransportLayer myTransportLayer, Grafo network, String tipo, int gtw) {
        super(s, id_nodo, myPhyLayer, myLinkLayer, myNetLayer, myTransportLayer, network, tipo, gtw);
        dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "length");
    }
    
    public MobileHost(scheduler s, int id_nodo, physicalLayer myPhyLayer, LinkLayer myLinkLayer, NetworkLayer myNetLayer, TransportLayer myTransportLayer, Grafo network, String tipo, int gtw, String stazione) {
        super(s, id_nodo, myPhyLayer, myLinkLayer, myNetLayer, myTransportLayer, network, tipo, gtw);
        this.stazione = stazione;
        dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "length");
    }

    public String getNodo_ingresso() {
        return nodo_ingresso;
    }

    public void setNodo_ingresso(String nodo_ingresso) {
        this.nodo_ingresso = nodo_ingresso;
    }

    public String getNodo_uscita() {
        return nodo_uscita;
    }

    public void setNodo_uscita(String nodo_uscita) {
        this.nodo_uscita = nodo_uscita;
    }

    public MobilityMap getMappa() {
        return cityMap;
    }

    public void setMappa(MobilityMap mappa) {
        this.cityMap = mappa;
        this.mappa = mappa.cityRoadMap;
    }

    public void calcolaNuovaPosizione(Edge e, double x1, double y1, double x2, double y2) {
        double avgSpeed = (Double) e.getAttribute("avgSpeed");
        double angle = Math.atan(Math.abs(y2 - y1) / Math.abs(x2 - x1));
        double distance = avgSpeed * UPDATE_POSITION_TIME / 1000.0;

        double addX = 0;
        double addY = 0;
        //Find quadrante
        if ((x2 >= x1) && (y2 >= y1)) {
            //SONO nel primo quadrante sono tutti contributi positivi
            addX = 1;
            addY = 1;
        } else if ((x2 >= x1) && (y2 < y1)) {
            //SONO nel secondo quadrante i contributi di y sono negativi
            addX = 1;
            addY = -1;
        } else if ((x2 < x1) && (y2 < y1)) {
            //SONO nel terzo quadrante i contributi di y sono negativi
            addX = -1;
            addY = -1;
        } else if ((x2 < x1) && (y2 >= y1)) {
            //SONO nel terzo quadrante i contributi di y sono negativi
            addX = -1;
            addY = 1;
        }

        double temp_currX = currX + (addX) * distance * Math.cos(angle);
        double temp_currY = currY + (addY) * distance * Math.sin(angle);

        if (cityMap.validatePos("" + this.id_nodo, temp_currX, temp_currY)) {
            currX = temp_currX;
            currY = temp_currY;
            currDistance = currDistance + distance;
        }
    }

    @Override
    public void Handler(Messaggi m) {
        if (m.getTipo_Messaggio().equals(START_ROAD_RUN)) {
            carIsPowerOff = false;
            dijkstra.init(mappa);
            dijkstra.setSource(mappa.getNode(nodo_ingresso));
            dijkstra.compute();

            index_nodo_attuale = 0;
            list1 = new ArrayList<Node>();
            for (Node node : dijkstra.getPathNodes(mappa.getNode(nodo_uscita))) {
                list1.add(0, node);
            }

            Node curr = list1.get(index_nodo_attuale);
            Object x1 = ((Object[]) curr.getAttribute("xy"))[0];
            Object y1 = ((Object[]) curr.getAttribute("xy"))[1];

            currX = Double.parseDouble("" + x1);
            currY = Double.parseDouble("" + y1);
            
            currDistance = 0;
            
            //se il mobile host entra nell'area di copertura di un'altra base station
            //allora notifica la sua presenza e il router lo registra
            String id_router = verificaRiattestazione();
            if(id_router != null) {
            	//non è necessario rimuovere informazioni dato che ancora non ce ne sono
            	cityMap.riattesta(currBS, id_nodo, stazione);
            	System.out.println("Riattesta! "+id_router);
            	//riattesta
            }
            
            m.setTipo_Messaggio(UPDATE_POSITION);
            m.shifta(UPDATE_POSITION_TIME);
            m.setDestinazione(this);
            m.setSorgente(this);
            s.insertMessage(m);

        } else if (m.getTipo_Messaggio().equals(UPDATE_POSITION)) {
            if (!nodo_ingresso.equals(nodo_uscita)) {
                if (index_nodo_attuale < list1.size() - 1) {                    
                    Node curr = list1.get(index_nodo_attuale);
                    Node next = list1.get(index_nodo_attuale + 1);
                    
                    Object x1 = ((Object[]) curr.getAttribute("xy"))[0];
                    Object x2 = ((Object[]) next.getAttribute("xy"))[0];
                    Object y1 = ((Object[]) curr.getAttribute("xy"))[1];
                    Object y2 = ((Object[]) next.getAttribute("xy"))[1];
                    
                    double nextX = Double.parseDouble(""+x2);
                    double nextY = Double.parseDouble(""+y2);

                    //se il mobile host entra nell'area di copertura di un'altra base station
                    //allora notifica la sua presenza e il router lo registra
                    String id_router = verificaRiattestazione();
                    if(id_router != null) {
                    	//si rimuovono le informazioni del mobile host dal router
                    	//che gestisce la vecchia bs
                    	cityMap.rimuoviMobileHost(id_router, id_nodo);
                    	//ci si riattesta sulla nuova bs
                    	cityMap.riattesta(currBS, id_nodo, stazione);
                    	System.out.println("Riattesta! "+id_router);
                    	//riattesta
                    }
                    

                    double xComp = Math.pow((Double.parseDouble("" + x2) - Double.parseDouble("" + x1)), 2.0);
                    double yComp = Math.pow((Double.parseDouble("" + y2) - Double.parseDouble("" + y1)), 2.0);
                    double segment_length = Math.sqrt(xComp + yComp);

                    //Get average speed from cityMap by reading edge info
                    String edge_label = curr.toString() + next.toString();
                    Edge e = mappa.getEdge(edge_label);
                    
                    if(e == null) {
                    	edge_label = next.toString() + curr.toString();
                    	e = mappa.getEdge(edge_label);
                    }

                    calcolaNuovaPosizione(e, Double.parseDouble("" + x1),
                            Double.parseDouble("" + y1),
                            Double.parseDouble("" + x2),
                            Double.parseDouble("" + y2));

                    double waitingTime = UPDATE_POSITION_TIME;
                    
                    //Sono arrivato alla fine del segmento
                    //quindi sto per attraversare il prossimo nodo
                    if (currDistance >= segment_length) {
                        currX = Double.parseDouble("" + x2);
                        currY = Double.parseDouble("" + y2);
                        cityMap.updateVehiclePos("" + this.id_nodo, currX, currY);
                        currDistance = 0;
                        index_nodo_attuale++;
                        waitingTime = STOP_WAITING_TIME;
                        //System.out.println("nodo " + this.getId() + " Arrivato su incrocio " + next + " al tempo " + s.orologio.getCurrent_Time());
                        
                        if(this.nodo_uscita.equals(next.toString())){
                            carIsPowerOff = true;
                            for(Nodo n : info.getNodes()){
                                Messaggi m1 = new Messaggi(POWER_OFF,this,my_wireless_channel,n,s.orologio.getCurrent_Time());
                                m1.setNodoSorgente(this);
                                m1.saliPilaProtocollare = false;
                                m1.setNextHop(n);
                                m1.setNextHop_id(n.getId());
                                s.insertMessage(m1);
                            }
                        }
                    }

                    //System.out.println("nodo " + this.getId() + " posizione x,y (" + currX + "," + currY + ") al tempo " + s.orologio.getCurrent_Time());
                    if(carIsPowerOff == false)
                    {
                      m.shifta(waitingTime);
                      s.insertMessage(m);
                    }
                }
            }

        } else if (m.getTipo_Messaggio().equals("DISCOVER_NEIGHBOURS")) {
            if(this.carIsPowerOff == false)
            {
                if (m.saliPilaProtocollare == false) {
                    //Invia messaggio a canale
                    m.shifta(0);
                    m.setDestinazione(my_wireless_channel);
                    m.setSorgente(this);
                    s.insertMessage(m);
                } else {
                    //invia messaggio a PHY
                    m.shifta(0);
                    m.setDestinazione(this.myPhyLayer);
                    m.setSorgente(this);
                    s.insertMessage(m);
                }
            }
        } else {
            super.Handler(m);
        }
    }
    
    /**
     * Verifica se è necessario riattestarsi su un altra base station
     * @return id router che gestisce la prossima bs
     */
    
    private String verificaRiattestazione() {
    	//Si ricava il router che gestisce la bs corrente
    	Node bs = cityMap.cityRoadMap.getNode(currBS);
    	String id_router = "";
    	if(bs != null) {
    		id_router = bs.getAttribute("router");
    	}
    	for(Node n : cityMap.cityRoadMap) {
    		//Se è un nodo bs e non è la stessa bs su cui si trova il mh
    		//e il router che gestisce la prossima bs è diverso da quello corrente
    		//TODO: verificare la zona intermedia in cui ci sono collisioni sia
    		//con la copertura della BS corrente che di quella successiva
    		String next_router = n.getAttribute("router");
    		if(n.getId().startsWith("B") && !(n.getId().equals(currBS)) && !(next_router.equals(id_router))) {
    			//coordinate bs prossimo
    			Object x1 = ((Object []) n.getAttribute("xy"))[0];
    			Object y1 = ((Object []) n.getAttribute("xy"))[1];
    			double bsX = Double.parseDouble(""+x1);
    			double bsY = Double.parseDouble(""+y1);
    			double xDif = currX - bsX;
                double yDif = currY - bsY;
                double distanceSquared = xDif * xDif + yDif * yDif;
                boolean collision = distanceSquared <= (BS_RADIUS + RADIUS) * (BS_RADIUS + RADIUS);
                if(collision) {
                	currBS = n.getId();
                	return next_router;
                }
    		}
    	}
    	return null;
    }
    
    public void setExitFromGate(double exitGateAt) {
        Messaggi m = new Messaggi(START_ROAD_RUN, this, this, this, s.orologio.getCurrent_Time());        
        m.shifta(exitGateAt);
        s.insertMessage(m);
    }

}
