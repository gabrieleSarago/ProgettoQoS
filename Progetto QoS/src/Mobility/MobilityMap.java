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

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.ui.view.Viewer;

import base_simulator.scheduler;
import radioFM.MobileHost;
import radioFM.Router;
import radioFM.UpperLevelRouter;
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
    public HashMap<String, Router> routers = new HashMap<>();
    //struttura dati che contiene router di primo livello e gateway router
    public HashMap<String, UpperLevelRouter> ul_routers = new HashMap<>();
    //router gateway
    private UpperLevelRouter gateway;
    
    private scheduler s;
    
    ProxyPipe pipe;
    
    private double radius;
    private double startX;
    private double startY;

    public MobilityMap(scheduler s, double radius, double startX, double startY) {
    	this.s = s;
    	this.radius = radius;
    	this.startX = startX;
    	this.startY = startY;
    	
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

        /*for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(100);
                pipe.pump();
                cityRoadMap.getNode("Car").setAttribute("xy",500+i,250+i);
            } catch (InterruptedException ex) {
                Logger.getLogger(MobilityMap.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/
        /*for (Node node : cityRoadMap) {
            System.out.println(node.toString() + " Posizione (x,y) :" + node.getNumber("x") + "," + ((Object[]) node.getAttribute("xy"))[1]);
        }*/

    }
    
    public UpperLevelRouter getGateway(){
    	return gateway;
    }
    
    public Graph getCityRoadMap() {
        return cityRoadMap;
    }

    public void setCityRoadMap(Graph cityRoadMap) {
        this.cityRoadMap = cityRoadMap;
    }

    /**
     * Creamo la struttura della citta utilizzando una libreria graphStream per
     * la gestione delle strade 1.0 Creamo il grafo della rete 2.0 Associamo la
     * struttura del grafo agli oggetti java
     */
    public void createCityMap() {
        //Node represents the crossway among road
        cityRoadMap = new SingleGraph("ColdRiver");
        
        /*
         * Si creano dei nodi di traffico casuali
         */
        int numNodi = 31;
        Random random = new Random();
        for(int i = 1; i < numNodi; i++){
        	int x = random.nextInt(2000);
        	int y = random.nextInt(1000);
        	//verifica che non ci siano nodi nelle stesse posizioni
        	boolean found = true;
        	while(found){
        		found = false;
        		for(Node n : cityRoadMap){
        			Object x1 = ((Object[]) n.getAttribute("xy"))[0];
        			Object y1 = ((Object[]) n.getAttribute("xy"))[1];
        			double a1 = Double.parseDouble(""+x1);
        			double b1 = Double.parseDouble(""+y1);
        			if(Math.abs(a1-x) <= 0.00001 && Math.abs(b1-y) <= 0.00001){
        				x = random.nextInt(2000);
        				y = random.nextInt(1000);
        				found = true;
        				break;
        			}
        		}
        	}
        	cityRoadMap.addNode(""+i);
        	cityRoadMap.getNode(""+i).setAttribute("xy",x,y);
        }
        int currE = 1;
        while(currE <= numNodi*2){
        	String s1 = ""+(random.nextInt(numNodi-1)+1);
        	String s2 = ""+(random.nextInt(numNodi-1)+1);
        	if(!(cityRoadMap.getEdge(s1+s2) != null || cityRoadMap.getEdge(s2+s1) != null)){
        		Node n1 = cityRoadMap.getNode(s1);
        		Node n2 = cityRoadMap.getNode(s2);
        		//per evitare di creare archi che partono e finiscono nello stesso nodo
        		while(n1.getId().equals(n2.getId())){
        			s2 = ""+(random.nextInt(numNodi-1)+1);
        			n2 = cityRoadMap.getNode(s2);
        		}
        		//crea l'arco, associando una determinata etichetta
        		//per calcolare la lunghezza, si usa il teorema di Pitagora
        		Object x1 = ((Object[]) n1.getAttribute("xy"))[0];
        		Object y1 = ((Object[]) n1.getAttribute("xy"))[1];

        		Object x2 = ((Object[]) n2.getAttribute("xy"))[0];
        		Object y2 = ((Object[]) n2.getAttribute("xy"))[1];

        		double a1 = Double.parseDouble(""+x1);
        		double b1 = Double.parseDouble(""+y1);

        		double a2 = Double.parseDouble(""+x2);
        		double b2 = Double.parseDouble(""+y2);

        		int lenght = (int)Math.sqrt((a2-a1)*(a2-a1)+(b2-b1)*(b2-b1))/2;
        		cityRoadMap.addEdge(s1+s2, s1, s2).addAttribute("length", lenght);
        		cityRoadMap.getEdge(s1+s2).addAttribute("avgSpeed", 11.2);
        	}
        	currE++;
        }
        
        //cityRoadMap.addNode("1").addAttribute("ui.hide");
        
        //Router Gateway
        gateway = new UpperLevelRouter(s, "C1", 1000000, this);
        //lista di router da aggiungere ai router di primo livello
        LinkedList<Router> r = new LinkedList<>();
        //lista di router di primo livello da aggiungere al gateway
        LinkedList<Router> fr = new LinkedList<>();
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
        UpperLevelRouter ul = new UpperLevelRouter(s,"F"+id_frouter,1000,this);
        Hexagon h = makeCluster(id_router, ul, 1, startX, startY);
        r.add(routers.get("R"+id_router));
        while(h.getC().getX() <= 2000){
        	id_router++;
        	h = makeCluster(id_router, ul, h.getID(), h.getO().getX(), h.getO().getY());
            r.add(routers.get("R"+id_router));
        }
        ul.addRouters(r);
        ul_routers.put("F"+id_frouter, ul);
        ul.setUplink(gateway);
        fr.add(ul);
        id_frouter++;
        
        int tempID = 2;
        while(h.getA().getY()<= 1000){
        	Node n = cityRoadMap.getNode("AB"+tempID);
        	Object by = ((Object[])n.getAttribute("xy"))[1];
        	double Oy = Double.parseDouble(""+by);
        	tempID = h.getID()+2;
        	id_router++;
        	ul = new UpperLevelRouter(s,"F"+id_frouter,1000,this);
        	h = makeCluster(id_router, ul, h.getID()+1, startX, Oy);
        	while(h.getC().getX() <= 2000){
        		id_router++;
        		h = makeCluster(id_router, ul, h.getID(), h.getO().getX(), h.getO().getY());
        	}
            ul.addRouters(r);
            ul_routers.put("F"+id_frouter, ul);
            ul.setUplink(gateway);
            fr.add(ul);
            id_frouter++;
        }
        //in questo modo il gateway gestisce i router di primo livello
        gateway.addRouters(fr);
        
        //per colorare un arco
        //cityRoadMap.getEdge("AI").addAttribute("ui.style", "fill-color: red;");

        for (Edge e : cityRoadMap.getEachEdge()) {
        	if(!(e.getId().startsWith("AG") || e.getId().startsWith("GH") || e.getId().startsWith("HD") ||
        			e.getId().startsWith("DE") || e.getId().startsWith("EF") || e.getId().startsWith("FA"))){
        		e.addAttribute("label", "" + (int) e.getNumber("length"));
        	}
        }
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
    					System.out.println("B"+id+"XX = "+x+" Y = "+y);
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
    
    private Hexagon makeCluster(int id_router, UpperLevelRouter ul, int id, double x, double y){
    	Router r = new Router(s, "R"+id_router, 100, this);
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
    	routers.put("R"+id_router, r);
    	return h;
    }
    
    private void makeNode(String label, Point2D p){
    	cityRoadMap.addNode(label);
    	cityRoadMap.getNode(label).setAttribute("xy", p.getX(), p.getY());
    }

    public void addRouter(String id, Router r) {
    	routers.put(id, r);
    }
    
    public void addFirstRouter(String id, UpperLevelRouter ur) {
    	ul_routers.put(id, ur);
    }
    
    public UpperLevelRouter getFirstRouter(String id) {
    	return ul_routers.get(id);
    }

    public Router getRouter(String id) {
    	return routers.get(id);
    }
    
    public void rimuoviMobileHost(String id_router, int id_mh) {
    	Router r = routers.get(id_router);
    	r.removeMobileHost(id_mh);
    }
    
    //riattesta il mobile host sulla nuova base station
    //inoltre aggiunge l'indirizzo del mobile host nel router
    public void riattesta(String bs, int id_mh, String station) {
    	Node n = cityRoadMap.getNode(bs);
    	String id_router = n.getAttribute("router");
    	Router r = routers.get(id_router);
    	r.addMobileHost(id_mh, station);
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
