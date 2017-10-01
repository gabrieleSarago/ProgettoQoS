/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package radioFM;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

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
    final String INACTIVE = "inactive";
    //1 secondo => 100 secondi
    final double UPDATE_POSITION_TIME = 1000.0; //UPDATE_POSITION_TIME
    //10 secondi => 1000 secondi
    final double STOP_WAITING_TIME = 10000.0; //WAIT AT ROAD_CROSS
    String nodo_ingresso;
    String nodo_uscita;
    int index_nodo_attuale;
    
    double avgSpeed;
    
    //40 secondi di handover
    private final double HANDOVER_TIME = 40.0;
    //2km di zona intermedia
    private final double HANDOVER_DISTANCE = 1.0;
    //espresso in Kbps
    private final int AVG_RATE = 144;
    //espresso in KByte
    private final int PACKET_SIZE = 1526;
    //stazione radio scelta dal mobile host
    private String stazione;
    
    //inizio zona di handover
    private boolean handover = false;
    //stato del mobile host
    private boolean attivo;
    //distanza percorsa tra inizio e fine zona intermedia
    private double startCollision, endCollision;
    
    /*raggio di copertura della BaseStation
     * 102 = 51km * 2
     * essendo la mappa in rapporto 1:2 (1 corrisponde a 2km)
     */
    private final double BS_RADIUS = 100.5;
    //raggio del cerchio rappresentante questo mobile host
    private final double RADIUS = 10.0;
    //ID base station a cui il mh e attualmente registrato
    private String currBS = "";
    //latenza totale handover
    private double latenza = 0;
    private String id_router = "";
    
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
        this.setStazione(stazione);
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

    public String getStazione() {
		return stazione;
	}

	public void setStazione(String stazione) {
		this.stazione = stazione;
	}
	
	public boolean eAttivo() {
		return attivo;
	}

	public void calcolaNuovaPosizione(Edge e, double x1, double y1, double x2, double y2) {
        avgSpeed = (Double) e.getAttribute("avgSpeed");
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
    	/*
    	 * Quando inattivo aspetta un periodo UPDATE_POSITION_TIME*2
    	 * e torna attivo
    	 */
    	if (m.getTipo_Messaggio().equals(INACTIVE)) {
    		attivo = false;
    		m.setTipo_Messaggio(UPDATE_POSITION);
    		m.shifta(UPDATE_POSITION_TIME*2);
    		m.setDestinazione(this);
    		m.setSorgente(this);
    	    s.insertMessage(m);
    	}
    	else if (m.getTipo_Messaggio().equals(START_ROAD_RUN)) {
    		attivo = true;
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
            //verificaZonaHandover();
            if(verificaRiattestazione(true)) {
            	//non e necessario rimuovere informazioni dato che ancora non ce ne sono
            	Point2D position = new Point2D.Double(currX, currY);
            	//route message
            	cityMap.riattesta(currBS, id_nodo, position);
            	System.out.println("Riattesta! "+id_router);
            	//riattesta
            }
            
            m.setTipo_Messaggio(UPDATE_POSITION);
            m.shifta(UPDATE_POSITION_TIME);
            m.setDestinazione(this);
            m.setSorgente(this);
            s.insertMessage(m);

        } else if (m.getTipo_Messaggio().equals(UPDATE_POSITION)) {
        	attivo = true;
            if (!nodo_ingresso.equals(nodo_uscita)) {
                if (index_nodo_attuale < list1.size() - 1) {                    
                    Node curr = list1.get(index_nodo_attuale);
                    Node next = list1.get(index_nodo_attuale + 1);
                    
                    Object x1 = ((Object[]) curr.getAttribute("xy"))[0];
                    Object x2 = ((Object[]) next.getAttribute("xy"))[0];
                    Object y1 = ((Object[]) curr.getAttribute("xy"))[1];
                    Object y2 = ((Object[]) next.getAttribute("xy"))[1];
                    
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
                    
                  //se il mobile host entra nell'area di copertura di un'altra base station
                    //allora notifica la sua presenza e il router lo registra
                    if(verificaRiattestazione(false)) {
                    	//si rimuovono le informazioni del mobile host dal router
                    	//che gestisce la vecchia bs
                    	cityMap.rimuoviMobileHost(id_router, id_nodo);
                    	//ci si riattesta sulla nuova bs
                    	Point2D position = new Point2D.Double(currX, currY);
                    	//route message
                    	cityMap.riattesta(currBS, id_nodo, position);
                    	System.out.println("Riattesta! "+id_router);
                    	//riattesta
                    }

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
                        //numero casuale tra 0 e 10
                    	//probabilit� del 20% che il mobile host diventa inattivo
                    	if((new Random()).nextInt(11) <= 2) {
                    		m.setTipo_Messaggio(INACTIVE);
                            m.shifta(waitingTime);
                            m.setDestinazione(this);
                            m.setSorgente(this);
                            s.insertMessage(m);
                            return;
                    	}
                        //System.out.println("nodo " + this.getId() + " Arrivato su incrocio " + next + " al tempo " + s.orologio.getCurrent_Time());
                        
                        if(this.nodo_uscita.equals(next.toString())){
                            carIsPowerOff = true;
                            //come il mobile host muore viene rimosso dalla strutture dati
                            cityMap.rimuoviMobileHost(id_router, id_nodo);
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
        	attivo = true;
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
     * Verifica se e necessario riattestarsi su un altra base station
     * @return id router che gestisce la prossima bs
     */
    
    private boolean verificaRiattestazione(boolean generation) {
    	//Si ricava il router che gestisce la bs corrente
    	Node bs = cityMap.cityRoadMap.getNode(currBS);
    	if(bs != null) {
    		id_router = bs.getAttribute("router");
    	}
    	boolean nessunaCollisione = true;
    	for(Node n : cityMap.cityRoadMap) {
    		String next_router = n.getAttribute("router");
    		//Se e un nodo bs e non e la stessa bs su cui si trova il mh
    		//e il router che gestisce la prossima bs e diverso da quello corrente
    		if(n.getId().startsWith("B") && !(n.getId().equals(currBS)) && !(next_router.equals(id_router))) {
    			//coordinate bs prossimo
    			Object x1 = ((Object []) n.getAttribute("xy"))[0];
    			Object y1 = ((Object []) n.getAttribute("xy"))[1];
    			double bsX = Double.parseDouble(""+x1);
    			double bsY = Double.parseDouble(""+y1);
    			double xDif = currX - bsX;
                double yDif = currY - bsY;
                double distanceSquared = xDif * xDif + yDif * yDif;
                boolean collisione = distanceSquared <= (BS_RADIUS + RADIUS) * (BS_RADIUS + RADIUS);
                if(collisione) {
                	nessunaCollisione = false;
                }
                //caso iniziale in cui il mobile host viene generato
                if(collisione && generation) {
                	currBS = n.getId();
                	id_router = n.getAttribute("router");
                	return true;
                }
                //Se trova un'altra base station ma si trova in una zona intermendia di handover
                //non restituisce nulla, questo perch� si sta gi� riattestando
                else if(collisione && !handover) {
                	startCollision = currDistance;
                	System.out.println("inizio handover");
                	handover = true;
                	currBS = n.getId();
                	id_router = n.getAttribute("router");
                	return true;
                }
    		}
    	}
    	//se non ci sono collisioni con altre bs ma handover = true allora siamo appena usciti dalla zona intermedia
    	if(nessunaCollisione && handover) {
    		System.out.println("fine handover");
    		endCollision = currDistance;
    		//metodo che, dato il tempo totale trascorso nella zona intermedia,
    		//verifica se tale tempo rispetta il tempo di Handover. Se non lo rispetta
    		//bisogna calcolare quanti pacchetti perde
    		verificaTempo(endCollision-startCollision);
    		handover = false;
    	}
    	return false;
    }
    
    private void verificaTempo(double distance) {
    	//espresso in km/h
    	/*
    	 * Esempio: speed = 5.0
    	 * 5.0/2.0 = 2.5km/100s quindi 2.5/100.0 = 0.025km/s
    	 * 0.025*3600.0 = 90km/h
    	 */
    	double realSpeed = ((avgSpeed/2.0)/100.0)*3600.0;
    	System.out.println(realSpeed);
    	System.out.println("distanza = "+distance);
    	double time = HANDOVER_DISTANCE/((realSpeed/3600.0));
    	System.out.println("Tempo zona intermedia = "+time);
    	double latenza_handover = HANDOVER_TIME - time;
    	//se lo scarto e positivo allora il tempo nella zona intermedia
    	//e inferiore rispetto al tempo richiesto dall'handover, ne consegue
    	//che ci sara una perdita di pacchetti.
    	System.out.println("latenza = "+latenza_handover);
    	if(latenza_handover > 0) {
    		//si aggiorna la latenza totale incontrata
    		latenza += latenza_handover;
    		System.out.println("latenza totale = "+latenza);
    		//espresso in bit
    		double bitrate_loss = AVG_RATE*1000*latenza_handover;
    		System.out.println("bitrate = "+bitrate_loss);
    		int pckts_loss = (int) bitrate_loss/(PACKET_SIZE*8);
    		System.out.println("pacchetti persi = "+pckts_loss);
    	}
    }
    
    public void notificaRiattesta() {
    	//currBS = "";
    	//siccome le informazioni sono state cancellate dal router
    	//e come se bisognasse riattestarsi alla prima BS
    	//quindi generation = true
    	verificaRiattestazione(true);
    	System.out.println("notifica riattestazione router = "+id_router);
    	//Non c'e bisogno di richiedere la rimozione di informazioni
    	//perche sono gia state rimosse
    	Point2D position = new Point2D.Double(currX, currY);
    	//corrisponde al route message
    	//ci si riattesta sulla nuova bs
    	cityMap.riattesta(currBS, id_nodo, position);
    }
    
    public void setExitFromGate(double exitGateAt) {
        Messaggi m = new Messaggi(START_ROAD_RUN, this, this, this, s.orologio.getCurrent_Time());        
        m.shifta(exitGateAt);
        s.insertMessage(m);
    }

}
