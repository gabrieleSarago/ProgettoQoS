/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Mobility;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;

import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.ui.view.Viewer;

import base_simulator.scheduler;
import radioFM.MobileHost;
import radioFM.MA;
import radioFM.UpperLevelMA;
import utils.Hexagon;

/**
 *
 * @author afsantamaria
 */
public class MobilityMap {

    public Graph cityRoadMap;
    public HashMap<String, Mh_node> mobile_hosts = new HashMap<String, Mh_node>();
    public HashMap<Integer, MobileHost> mobHost = new HashMap<>();
    //associazione id_router - oggetto Router
    public HashMap<String, MA> mobility_agents = new HashMap<>();
    //struttura dati che contiene router di primo livello e gateway router
    public HashMap<String, UpperLevelMA> ul_mobility_agents = new HashMap<>();
    //router gateway
    private UpperLevelMA gateway;
    
    private scheduler s;
    
    ProxyPipe pipe;
    
    private double radius;
    private int lenght;
    private int height;
    private int numNodi;
    private double minSpeed, maxSpeed;
    private double handoffDistance, pPaging;
    private int capacitaGMA, capacitaMA;

    public MobilityMap(scheduler s, double radius, int lenght, int height, int numNodi, double minSpeed, double maxSpeed, int capacitaGMA, int capacitaMA, double pPaging) {
    	this.s = s;
    	this.radius = radius;
    	this.lenght = lenght;
    	this.height = height;
    	this.numNodi = numNodi;
    	this.minSpeed = minSpeed;
    	this.maxSpeed = maxSpeed;
    	this.capacitaGMA = capacitaGMA;
    	this.capacitaMA = capacitaMA;
    	this.pPaging = pPaging;
    	
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        createCityMap();

        for (Node n : cityRoadMap) {
            //si colorano i nodi di copertura degli esagoni
            if(n.getId().startsWith("AB") || n.getId().startsWith("GB") || n.getId().startsWith("HB") ||
            		n.getId().startsWith("DB") || n.getId().startsWith("EB") || n.getId().startsWith("FB")){
            	if (n.getAttribute("ui.style") == null){
            		n.addAttribute("ui.style", "fill-color: black; size: 1px,1px;");
            	}
            	else{
            		n.setAttribute("ui.style", "fill-color: black; size: 1px,1px;");
            	}
            }
            else{
            	n.addAttribute("label", n.getId());
            	if(n.getId().startsWith("B")) {
            		if (n.getAttribute("ui.style") == null) {
            			n.addAttribute("ui.style", "fill-color: blue; size: 10px,10px;");
            		}
            		else {
            			n.setAttribute("ui.style", "fill-color: blue; size: 10px,10px;");
            		}
            	}
            	else if (n.getAttribute("ui.style") == null) {
            		n.addAttribute("ui.style", "fill-color: red; size: 10px,10px;");
            	}
            	else {
            		n.setAttribute("ui.style", "fill-color: red; size: 10px,10px;");
            	}
            }
        }
        //Viewer viewer = cityRoadMap.display();
        //viewer.disableAutoLayout();
        /*Viewer viewer;
        for (int i = 0; i < 100; i++) {
            
            cityRoadMap.getNode("Car").setAttribute("xy",500+(i*5),250);
            viewer = cityRoadMap.display();
            viewer.disableAutoLayout();
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(MobilityMap.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/
        Viewer v = cityRoadMap.display();
        v.disableAutoLayout();
        pipe = v.newViewerPipe();
        pipe.addAttributeSink(cityRoadMap);

    }
    
    public UpperLevelMA getGateway(){
    	return gateway;
    }
    
    public Graph getCityRoadMap() {
        return cityRoadMap;
    }

    public void setCityRoadMap(Graph cityRoadMap) {
        this.cityRoadMap = cityRoadMap;
    }
    
    private int getEdgeLenght(Node n1, Node n2){
    	Object x1 = ((Object[]) n1.getAttribute("xy"))[0];
		Object y1 = ((Object[]) n1.getAttribute("xy"))[1];

		Object x2 = ((Object[]) n2.getAttribute("xy"))[0];
		Object y2 = ((Object[]) n2.getAttribute("xy"))[1];

		double a1 = Double.parseDouble(""+x1);
		double b1 = Double.parseDouble(""+y1);

		double a2 = Double.parseDouble(""+x2);
		double b2 = Double.parseDouble(""+y2);

		int lenght = (int)Math.sqrt((a2-a1)*(a2-a1)+(b2-b1)*(b2-b1))/4;
		return lenght;
    }

    /**
     * Creamo la struttura della citta utilizzando una libreria graphStream per
     * la gestione delle strade 1.0 Creamo il grafo della rete 2.0 Associamo la
     * struttura del grafo agli oggetti java
     */
    public void createCityMap() {
        //Node represents the crossway among road
        cityRoadMap = new SingleGraph("Random");
        Generator gen = new DorogovtsevMendesGenerator();
        gen.addSink(cityRoadMap);
        gen.begin();
        for(int i = 0; i < numNodi-3; i++){
        	gen.nextEvents();
        }
        gen.end();
        
        //posizione casuale dei nodi
        for(Node n : cityRoadMap){
        	int x = (new Random()).nextInt(lenght);
        	int y = (new Random()).nextInt(height);
        	boolean uguale = true;
        	while(uguale){
        		uguale = false;
        		//verifica che non ci sia un altro nodo
        		//con le stesse posizioni
        		for(Node n1: cityRoadMap){
        			if(n1.hasAttribute("xy")){
        				Object a1 = ((Object []) n1.getAttribute("xy"))[0];
        				Object b1 = ((Object []) n1.getAttribute("xy"))[1];
        				double x1 = Double.parseDouble(""+a1);
        				double y1 = Double.parseDouble(""+b1);
        				if(Math.abs(x-x1) <= 0.00001 && Math.abs(y-y1)<= 0.00001){
        					uguale = true;
        					break;
        				}
        			}
        		}
        		if(uguale){
        			x = (new Random()).nextInt(lenght);
        			y = (new Random()).nextInt(height);
        		}
        	}
        	n.setAttribute("xy", x, y);
        }
        
        //aggiunta dei pesi agli archi
        for(Edge e : cityRoadMap.getEachEdge()){
        	Node n0 = e.getNode0();
        	Node n1 = e.getNode1();
        	int lenght = getEdgeLenght(n0, n1);
        	e.addAttribute("lenght", lenght);
        	//velocità in un certo range di valori
        	double speed = (new Random()).nextDouble()*(maxSpeed-minSpeed)+minSpeed;
        	e.addAttribute("avgSpeed", speed);
        	if(!(e.getId().startsWith("AG") || e.getId().startsWith("GH") || e.getId().startsWith("HD") ||
        			e.getId().startsWith("DE") || e.getId().startsWith("EF") || e.getId().startsWith("FA"))){
        		e.addAttribute("label", ""+lenght);
        	}
        }
        
        //cityRoadMap.addNode("1").addAttribute("ui.hide");
        
        //Router Gateway
        gateway = new UpperLevelMA(s, "C1", capacitaGMA, pPaging, this);
        //lista di router da aggiungere ai router di primo livello
        LinkedList<MA> r = new LinkedList<>();
        //lista di router di primo livello da aggiungere al gateway
        LinkedList<MA> fr = new LinkedList<>();
        //id router di primo livello
        int id_frouter = 1;
        //id router gestori di BS
        int id_router = 1;
        /*
         * Ogni cluster è gestito da un router, quindi ogni volta che
         * si invoca il metodo makeCluster(...) si crea un oggetto Router
         */
        
        /*
         * I router di I livello gestiscono un insieme di Cluster tramite
         * i router di livello più basso
         */
        UpperLevelMA ul = new UpperLevelMA(s,"F"+id_frouter,capacitaMA*4, pPaging, this);
        Hexagon h = makeCluster(id_router, ul, 1, 0.0, 0.0);
        //per ottenere la distanza di handoff utilizzando il raggio del cerchio
        //che circoinscrive l'esagono
        handoffDistance = calcHandoffDistance(h);
        r.add(mobility_agents.get("R"+id_router));
        while(h.getC().getX() <= lenght){
        	id_router++;
        	h = makeCluster(id_router, ul, h.getID(), h.getO().getX(), h.getO().getY());
            r.add(mobility_agents.get("R"+id_router));
        }
        ul.addRouters(r);
        ul_mobility_agents.put("F"+id_frouter, ul);
        ul.setUplink(gateway);
        fr.add(ul);
        id_frouter++;
        
        int tempID = 2;
        while(h.getA().getY()<= height){
        	Node n = cityRoadMap.getNode("AB"+tempID);
        	Object by = ((Object[])n.getAttribute("xy"))[1];
        	double Oy = Double.parseDouble(""+by);
        	tempID = h.getID()+2;
        	id_router++;
        	ul = new UpperLevelMA(s,"F"+id_frouter,capacitaMA*4, pPaging,this);
        	h = makeCluster(id_router, ul, h.getID()+1, 0.0, Oy);
        	while(h.getC().getX() <= lenght){
        		id_router++;
        		h = makeCluster(id_router, ul, h.getID(), h.getO().getX(), h.getO().getY());
        	}
            ul.addRouters(r);
            ul_mobility_agents.put("F"+id_frouter, ul);
            ul.setUplink(gateway);
            fr.add(ul);
            id_frouter++;
        }
        //in questo modo il gateway gestisce i router di primo livello
        gateway.addRouters(fr);
        
        //per colorare un arco
        //cityRoadMap.getEdge("AI").addAttribute("ui.style", "fill-color: red;");

        /*for (Edge e : cityRoadMap.getEachEdge()) {
        	if(!(e.getId().startsWith("AG") || e.getId().startsWith("GH") || e.getId().startsWith("HD") ||
        			e.getId().startsWith("DE") || e.getId().startsWith("EF") || e.getId().startsWith("FA"))){
        		e.addAttribute("label", "" + (int) e.getNumber("length"));
        	}
        }*/
    }
    
    private Hexagon makeBS(int id_router, int id, double x, double y){
    	Hexagon h = new Hexagon(id, x, y, radius);
    	//se esiste già una BS con lo stesso ID
    	if(cityRoadMap.getNode("B"+id) != null){
    		return h;
    	}
    	else  {
    		//Se esiste una BS con id diverso ma nella stessa posizione
    		for(Node n : cityRoadMap){
    			if(n.getId().startsWith("B")){
    				Object currx = ((Object[])n.getAttribute("xy"))[0];
    				Object curry = ((Object[])n.getAttribute("xy"))[1];
    				double Ox = Double.parseDouble(""+currx);
    				double Oy = Double.parseDouble(""+curry);
    				if(Math.abs(Ox-x) <= 0.000001 && Math.abs(Oy-y)<= 0.000001){
    					//System.out.println("B"+id+"XX = "+x+" Y = "+y);
    					id--;
    					return new Hexagon(id, x, y, radius);
    				}
    			}
    		}
    		//altrimenti crea la BS
    		cityRoadMap.addNode("B"+id);
    		cityRoadMap.getNode("B"+id).setAttribute("xy", x, y);
    		cityRoadMap.getNode("B"+id).addAttribute("router", "R"+id_router);
    		Point2D p = h.getA();
    		String label = "AB"+id;
    		makeNode(label, p);
    		p = h.getB();
    		label = "GB"+id;
    		makeNode(label, p);
    		p = h.getC();
    		label = "HB"+id;
    		makeNode(label, p);
    		p = h.getD();
    		label = "DB"+id;
    		makeNode(label, p);
    		p = h.getE();
    		label = "EB"+id;
    		makeNode(label, p);
    		p = h.getF();
    		label = "FB"+id;
    		makeNode(label, p);
    		cityRoadMap.addEdge("AG"+id, "AB"+id, "GB"+id);
    		cityRoadMap.addEdge("GH"+id, "GB"+id, "HB"+id);
    		cityRoadMap.addEdge("HD"+id, "HB"+id, "DB"+id);
    		cityRoadMap.addEdge("DE"+id, "DB"+id, "EB"+id);
    		cityRoadMap.addEdge("EF"+id, "EB"+id, "FB"+id);
    		cityRoadMap.addEdge("FA"+id, "FB"+id, "AB"+id);
    	}
        return h;
    }
    
    private Hexagon makeCluster(int id_router, UpperLevelMA ul, int id, double x, double y){
    	MA r = new MA(s, "R"+id_router, capacitaMA, pPaging, this);
    	r.setUplink(ul);
    	//Si salva il primo esagono creato, che servirà per creare
    	//i due esagoni successivi
    	Hexagon h1 = makeBS(id_router, id, x, y);
    	//fare questo e non id++ serve a stabilire i casi in cui
    	//esiste già un BS in una determinata posizione.
    	//Se questo accade l'esagono restituito ha id uguale all'esagono precedente.
    	//Facendo getID()+1 sommiamo di nuovo e ritorniamo al nuovo id
    	id = h1.getID()+1;
    	Hexagon h = makeBS(id_router, id, h1.getB().getX()+radius, h1.getB().getY());
    	id = h.getID()+1;
    	h = makeBS(id_router, id, h1.getD().getX()+radius, h1.getD().getY());
    	id = h.getID()+1;
    	//Da notare che questo esagono viene generato grazie alle informazioni
    	//dell'esagono precedente e non dal primo esagono
    	h = makeBS(id_router, id, h.getB().getX()+radius, h.getB().getY());
    	mobility_agents.put("R"+id_router, r);
    	return h;
    }
    
    private void makeNode(String label, Point2D p){
    	cityRoadMap.addNode(label);
    	cityRoadMap.getNode(label).setAttribute("xy", p.getX(), p.getY());
    }
    
    private double calcHandoffDistance(Hexagon h) {
    	Point2D d = h.getD();
    	Point2D a = h.getO();
    	//centro dell'esagono adiacente a sud-est
    	Point2D b = new Point2D.Double(d.getX()+radius, d.getY());
    	double distance = Math.sqrt((b.getX()-a.getX())*(b.getX()-a.getX()) + (b.getY() - a.getY())*(b.getY() - a.getY()));
    	
    	return radius*2 - distance;
    }
    
    public double getHandoffDistance() {
    	return handoffDistance;
    }

    public void addRouter(String id, MA r) {
    	mobility_agents.put(id, r);
    }
    
    public void addFirstRouter(String id, UpperLevelMA ur) {
    	ul_mobility_agents.put(id, ur);
    }
    
    public UpperLevelMA getFirstRouter(String id) {
    	return ul_mobility_agents.get(id);
    }

    public MA getRouter(String id) {
    	return mobility_agents.get(id);
    }
    
    public double getRadius() {
    	return radius;
    }
    
    public void rimuoviMobileHost(String id_router, int id_mh) {
    	MA r = mobility_agents.get(id_router);
    	r.removeMobileHost(id_mh);
    }
    
    //riattesta il mobile host sulla nuova base station
    //inoltre aggiunge l'indirizzo del mobile host nel router
    public void riattesta(String bs, int id_mh, String ip) {
    	Node n = cityRoadMap.getNode(bs);
    	String id_router = n.getAttribute("router");
    	MA r = mobility_agents.get(id_router);
    	//TODO interrogare il router in modo da ottenere l'ip
    	//allo stato attuale il simulatore non � provvisto di indirizzamento IP
    	r.addMobileHost(id_mh, ip);
    }

    public boolean validatePos(String id, double x, double y) {
        boolean res = true;
        for (Entry<String, Mh_node> entry : mobile_hosts.entrySet()) {
            String key = entry.getKey();
            Mh_node car = (Mh_node) entry.getValue();
            if (!key.equals(id) && car.getX() == x && car.getY() == y) {
                res = false;
                break;
            }
        }
        if (res == true) {
            Mh_node car = mobile_hosts.get(id);
            car.setX(x);
            car.setY(y);

            //Test car nodes
            if (cityRoadMap.getNode(id) == null) {
                cityRoadMap.addNode(id);
                cityRoadMap.getNode(id).setAttribute("label", id);
                cityRoadMap.getNode(id).setAttribute("ui.style", "fill-color: green; size: 10px,10px;");
            }

            cityRoadMap.getNode(id).setAttribute("xy", x, y);
            pipe.pump();
        }
        return res;
    }

    public void updateVehiclePos(String id, double x, double y) {
        Mh_node car = mobile_hosts.get(id);
        car.setX(x);
        car.setY(y);
    }
}
