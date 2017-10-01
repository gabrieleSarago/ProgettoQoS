/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Mobility;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map.Entry;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.ui.view.Viewer;

import radioFM.MobileHost;
import radioFM.Router;
import radioFM.UpperLevelRouter;

/**
 *
 * @author afsantamaria
 */
public class MobilityMap {

    public Graph cityRoadMap;
    //Dijkstra dijkstra;
    public HashMap<String, Mh_node> mobile_hosts = new HashMap<String, Mh_node>();
    public HashMap<Integer, MobileHost> mobHost = new HashMap<>();
    private HashMap<String, Router> routers = new HashMap<>();
    //struttura dati che contiene router di primo livello e gateway router
    private HashMap<String, UpperLevelRouter> ul_routers = new HashMap<>();
    ProxyPipe pipe;

    public Graph getCityRoadMap() {
        return cityRoadMap;
    }

    public void setCityRoadMap(Graph cityRoadMap) {
        this.cityRoadMap = cityRoadMap;
    }

    public MobilityMap() {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        createCityMap();

        for (Node n : cityRoadMap) {
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
        for (Node node : cityRoadMap) {

            System.out.println(node.toString() + " Posizione (x,y) :" + node.getNumber("x") + "," + ((Object[]) node.getAttribute("xy"))[1]);
        }

    }

    /**
     * Creamo la struttura della citta utilizzando una libreria graphStream per
     * la gestione delle strade 1.0 Creamo il grafo della rete 2.0 Associamo la
     * struttura del grafo agli oggetti java
     */
    public void createCityMap() {
        //Node represents the crossway among road
        cityRoadMap = new SingleGraph("ColdRiver");
        
        //nasconde i nodi di traffico
        cityRoadMap.addNode("1").addAttribute("ui.hide");
        cityRoadMap.getNode("1").setAttribute("xy", 0, 500);
        cityRoadMap.addNode("2").addAttribute("ui.hide");
        cityRoadMap.getNode("2").setAttribute("xy", 150, 600);
        cityRoadMap.addNode("3").addAttribute("ui.hide");
        cityRoadMap.getNode("3").setAttribute("xy", 150, 400);
        cityRoadMap.addNode("4").addAttribute("ui.hide");
        cityRoadMap.getNode("4").setAttribute("xy", 200, 200);
        cityRoadMap.addNode("5").addAttribute("ui.hide");
        cityRoadMap.getNode("5").setAttribute("xy", 400, 0);
        cityRoadMap.addNode("6").addAttribute("ui.hide");
        cityRoadMap.getNode("6").setAttribute("xy", 350, 350);
        cityRoadMap.addNode("7").addAttribute("ui.hide");
        cityRoadMap.getNode("7").setAttribute("xy", 300, 550);
        cityRoadMap.addNode("8").addAttribute("ui.hide");
        cityRoadMap.getNode("8").setAttribute("xy", 400, 750);
        cityRoadMap.addNode("9").addAttribute("ui.hide");
        cityRoadMap.getNode("9").setAttribute("xy", 500, 700);
        cityRoadMap.addNode("10").addAttribute("ui.hide");
        cityRoadMap.getNode("10").setAttribute("xy", 500, 500);
        cityRoadMap.addNode("11").addAttribute("ui.hide");
        cityRoadMap.getNode("11").setAttribute("xy", 500, 400);
        cityRoadMap.addNode("12").addAttribute("ui.hide");
        cityRoadMap.getNode("12").setAttribute("xy", 900, 200);
        cityRoadMap.addNode("13").addAttribute("ui.hide");
        cityRoadMap.getNode("13").setAttribute("xy", 1200, 350);
        cityRoadMap.addNode("14").addAttribute("ui.hide");
        cityRoadMap.getNode("14").setAttribute("xy", 1200, 500);
        cityRoadMap.addNode("15").addAttribute("ui.hide");
        cityRoadMap.getNode("15").setAttribute("xy", 1000, 650);
        cityRoadMap.addNode("16").addAttribute("ui.hide");
        cityRoadMap.getNode("16").setAttribute("xy", 850, 800);
        cityRoadMap.addNode("17").addAttribute("ui.hide");
        cityRoadMap.getNode("17").setAttribute("xy", 1000, 1000);
        cityRoadMap.addNode("18").addAttribute("ui.hide");
        cityRoadMap.getNode("18").setAttribute("xy", 1400, 650);
        cityRoadMap.addNode("19").addAttribute("ui.hide");
        cityRoadMap.getNode("19").setAttribute("xy", 1400, 500);
        cityRoadMap.addNode("20").addAttribute("ui.hide");
        cityRoadMap.getNode("20").setAttribute("xy", 1400, 300);
        cityRoadMap.addNode("21").addAttribute("ui.hide");
        cityRoadMap.getNode("21").setAttribute("xy", 1500, 200);
        cityRoadMap.addNode("22").addAttribute("ui.hide");
        cityRoadMap.getNode("22").setAttribute("xy", 1800, 650);
        cityRoadMap.addNode("23").addAttribute("ui.hide");
        cityRoadMap.getNode("23").setAttribute("xy", 1800, 500);
        cityRoadMap.addNode("24").addAttribute("ui.hide");
        cityRoadMap.getNode("24").setAttribute("xy", 1800, 300);
        cityRoadMap.addNode("25").addAttribute("ui.hide");
        cityRoadMap.getNode("25").setAttribute("xy", 1800, 200);
        cityRoadMap.addNode("26").addAttribute("ui.hide");
        cityRoadMap.getNode("26").setAttribute("xy", 2000, 650);
        cityRoadMap.addNode("27").addAttribute("ui.hide");
        cityRoadMap.getNode("27").setAttribute("xy", 2000, 500);
        cityRoadMap.addNode("28").addAttribute("ui.hide");
        cityRoadMap.getNode("28").setAttribute("xy", 500, 200);
        
        //Nodi Base Station
        cityRoadMap.addNode("B1");
        cityRoadMap.getNode("B1").setAttribute("xy", 100, 100);
        cityRoadMap.addNode("B2");
        cityRoadMap.getNode("B2").setAttribute("xy", 300, 100);
        cityRoadMap.addNode("B3");
        cityRoadMap.getNode("B3").setAttribute("xy", 500, 100);
        cityRoadMap.addNode("B4");
        cityRoadMap.getNode("B4").setAttribute("xy", 700, 100);
        cityRoadMap.addNode("B5");
        cityRoadMap.getNode("B5").setAttribute("xy", 900, 100);
        cityRoadMap.addNode("B6");
        cityRoadMap.getNode("B6").setAttribute("xy", 1100, 100);
        cityRoadMap.addNode("B7");
        cityRoadMap.getNode("B7").setAttribute("xy", 1300, 100);
        cityRoadMap.addNode("B8");
        cityRoadMap.getNode("B8").setAttribute("xy", 1500, 100);
        cityRoadMap.addNode("B9");
        cityRoadMap.getNode("B9").setAttribute("xy", 1700, 100);
        cityRoadMap.addNode("B10");
        cityRoadMap.getNode("B10").setAttribute("xy", 1900, 100);
        cityRoadMap.addNode("B11");
        cityRoadMap.getNode("B11").setAttribute("xy", 100, 300);
        cityRoadMap.addNode("B12");
        cityRoadMap.getNode("B12").setAttribute("xy", 300, 300);
        cityRoadMap.addNode("B13");
        cityRoadMap.getNode("B13").setAttribute("xy", 500, 300);
        cityRoadMap.addNode("B14");
        cityRoadMap.getNode("B14").setAttribute("xy", 700, 300);
        cityRoadMap.addNode("B15");
        cityRoadMap.getNode("B15").setAttribute("xy", 900, 300);
        cityRoadMap.addNode("B16");
        cityRoadMap.getNode("B16").setAttribute("xy", 1100, 300);
        cityRoadMap.addNode("B17");
        cityRoadMap.getNode("B17").setAttribute("xy", 1300, 300);
        cityRoadMap.addNode("B18");
        cityRoadMap.getNode("B18").setAttribute("xy", 1500, 300);
        cityRoadMap.addNode("B19");
        cityRoadMap.getNode("B19").setAttribute("xy", 1700, 300);
        cityRoadMap.addNode("B20");
        cityRoadMap.getNode("B20").setAttribute("xy", 1900, 300);
        cityRoadMap.addNode("B21");
        cityRoadMap.getNode("B21").setAttribute("xy", 100, 500);
        cityRoadMap.addNode("B22");
        cityRoadMap.getNode("B22").setAttribute("xy", 300, 500);
        cityRoadMap.addNode("B23");
        cityRoadMap.getNode("B23").setAttribute("xy", 500, 500);
        cityRoadMap.addNode("B24");
        cityRoadMap.getNode("B24").setAttribute("xy", 700, 500);
        cityRoadMap.addNode("B25");
        cityRoadMap.getNode("B25").setAttribute("xy", 900, 500);
        cityRoadMap.addNode("B26");
        cityRoadMap.getNode("B26").setAttribute("xy", 1100, 500);
        cityRoadMap.addNode("B27");
        cityRoadMap.getNode("B27").setAttribute("xy", 1300, 500);
        cityRoadMap.addNode("B28");
        cityRoadMap.getNode("B28").setAttribute("xy", 1500, 500);
        cityRoadMap.addNode("B29");
        cityRoadMap.getNode("B29").setAttribute("xy", 1700, 500);
        cityRoadMap.addNode("B30");
        cityRoadMap.getNode("B30").setAttribute("xy", 1900, 500);
        cityRoadMap.addNode("B31");
        cityRoadMap.getNode("B31").setAttribute("xy", 100, 700);
        cityRoadMap.addNode("B32");
        cityRoadMap.getNode("B32").setAttribute("xy", 300, 700);
        cityRoadMap.addNode("B33");
        cityRoadMap.getNode("B33").setAttribute("xy", 500, 700);
        cityRoadMap.addNode("B34");
        cityRoadMap.getNode("B34").setAttribute("xy", 700, 700);
        cityRoadMap.addNode("B35");
        cityRoadMap.getNode("B35").setAttribute("xy", 900, 700);
        cityRoadMap.addNode("B36");
        cityRoadMap.getNode("B36").setAttribute("xy", 1100, 700);
        cityRoadMap.addNode("B37");
        cityRoadMap.getNode("B37").setAttribute("xy", 1300, 700);
        cityRoadMap.addNode("B38");
        cityRoadMap.getNode("B38").setAttribute("xy", 1500, 700);
        cityRoadMap.addNode("B39");
        cityRoadMap.getNode("B39").setAttribute("xy", 1700, 700);
        cityRoadMap.addNode("B40");
        cityRoadMap.getNode("B40").setAttribute("xy", 1900, 700);
        cityRoadMap.addNode("B41");
        cityRoadMap.getNode("B41").setAttribute("xy", 100, 900);
        cityRoadMap.addNode("B42");
        cityRoadMap.getNode("B42").setAttribute("xy", 300, 900);
        cityRoadMap.addNode("B43");
        cityRoadMap.getNode("B43").setAttribute("xy", 500, 900);
        cityRoadMap.addNode("B44");
        cityRoadMap.getNode("B44").setAttribute("xy", 700, 900);
        cityRoadMap.addNode("B45");
        cityRoadMap.getNode("B45").setAttribute("xy", 900, 900);
        cityRoadMap.addNode("B46");
        cityRoadMap.getNode("B46").setAttribute("xy", 1100, 900);
        cityRoadMap.addNode("B47");
        cityRoadMap.getNode("B47").setAttribute("xy", 1300, 900);
        cityRoadMap.addNode("B48");
        cityRoadMap.getNode("B48").setAttribute("xy", 1500, 900);
        cityRoadMap.addNode("B49");
        cityRoadMap.getNode("B49").setAttribute("xy", 1700, 900);
        cityRoadMap.addNode("B50");
        cityRoadMap.getNode("B50").setAttribute("xy", 1900, 900);
        
        cityRoadMap.getNode("B1").addAttribute("router", "R1");
        cityRoadMap.getNode("B2").addAttribute("router", "R1");
        cityRoadMap.getNode("B11").addAttribute("router", "R1");
        cityRoadMap.getNode("B12").addAttribute("router", "R1");
        cityRoadMap.getNode("B21").addAttribute("router", "R2");
        cityRoadMap.getNode("B31").addAttribute("router", "R2");
        cityRoadMap.getNode("B41").addAttribute("router", "R3");
        cityRoadMap.getNode("B42").addAttribute("router", "R3");
        cityRoadMap.getNode("B43").addAttribute("router", "R3");
        cityRoadMap.getNode("B13").addAttribute("router", "R4");
        cityRoadMap.getNode("B14").addAttribute("router", "R4");
        cityRoadMap.getNode("B15").addAttribute("router", "R4");
        cityRoadMap.getNode("B4").addAttribute("router", "R4");
        cityRoadMap.getNode("B5").addAttribute("router", "R4");
        cityRoadMap.getNode("B3").addAttribute("router", "R5");
        cityRoadMap.getNode("B22").addAttribute("router", "R5");
        cityRoadMap.getNode("B23").addAttribute("router", "R5");
        cityRoadMap.getNode("B24").addAttribute("router", "R5");
        cityRoadMap.getNode("B32").addAttribute("router", "R6");
        cityRoadMap.getNode("B33").addAttribute("router", "R6");
        cityRoadMap.getNode("B34").addAttribute("router", "R6");
        cityRoadMap.getNode("B35").addAttribute("router", "R6");
        cityRoadMap.getNode("B44").addAttribute("router", "R7");
        cityRoadMap.getNode("B45").addAttribute("router", "R7");
        cityRoadMap.getNode("B46").addAttribute("router", "R7");
        cityRoadMap.getNode("B47").addAttribute("router", "R7");
        cityRoadMap.getNode("B36").addAttribute("router", "R7");
        cityRoadMap.getNode("B37").addAttribute("router", "R7");
        cityRoadMap.getNode("B25").addAttribute("router", "R8");
        cityRoadMap.getNode("B26").addAttribute("router", "R8");
        cityRoadMap.getNode("B27").addAttribute("router", "R8");
        cityRoadMap.getNode("B16").addAttribute("router", "R9");
        cityRoadMap.getNode("B17").addAttribute("router", "R9");
        cityRoadMap.getNode("B18").addAttribute("router", "R9");
        cityRoadMap.getNode("B6").addAttribute("router", "R9");
        cityRoadMap.getNode("B7").addAttribute("router", "R9");
        cityRoadMap.getNode("B8").addAttribute("router", "R9");
        cityRoadMap.getNode("B9").addAttribute("router", "R10");
        cityRoadMap.getNode("B10").addAttribute("router", "R10");
        cityRoadMap.getNode("B19").addAttribute("router", "R10");
        cityRoadMap.getNode("B28").addAttribute("router", "R11");
        cityRoadMap.getNode("B29").addAttribute("router", "R11");
        cityRoadMap.getNode("B30").addAttribute("router", "R11");
        cityRoadMap.getNode("B20").addAttribute("router", "R11");
        cityRoadMap.getNode("B48").addAttribute("router", "R12");
        cityRoadMap.getNode("B49").addAttribute("router", "R12");
        cityRoadMap.getNode("B50").addAttribute("router", "R12");
        cityRoadMap.getNode("B38").addAttribute("router", "R12");
        cityRoadMap.getNode("B39").addAttribute("router", "R12");
        cityRoadMap.getNode("B40").addAttribute("router", "R12");

        cityRoadMap.addEdge("12", "1", "2").addAttribute("length", 80);
        cityRoadMap.addEdge("13", "1", "3").addAttribute("length", 80);
        cityRoadMap.addEdge("34", "3", "4").addAttribute("length", 120);
        cityRoadMap.addEdge("45", "4", "5").addAttribute("length", 150);
        cityRoadMap.addEdge("36", "3", "6").addAttribute("length", 120);
        cityRoadMap.addEdge("611", "6", "11").addAttribute("length", 80);
        cityRoadMap.addEdge("1011", "10", "11").addAttribute("length", 50);
        cityRoadMap.addEdge("910", "9", "10").addAttribute("length", 120);
        cityRoadMap.addEdge("27", "2", "7").addAttribute("length", 80);
        cityRoadMap.addEdge("28", "2", "8").addAttribute("length", 130);
        cityRoadMap.addEdge("89", "8", "9").addAttribute("length", 50);
        cityRoadMap.addEdge("79", "7", "9").addAttribute("length", 100);
        cityRoadMap.addEdge("916", "9", "16").addAttribute("length", 180);
        cityRoadMap.addEdge("915", "9", "15").addAttribute("length", 270);
        cityRoadMap.addEdge("1115", "11", "15").addAttribute("length", 280);
        cityRoadMap.addEdge("1113", "11", "13").addAttribute("length", 350);
        cityRoadMap.addEdge("1517", "15", "17").addAttribute("length", 180);
        cityRoadMap.addEdge("1415", "14", "15").addAttribute("length", 120);
        cityRoadMap.addEdge("1314", "13", "14").addAttribute("length", 80);
        cityRoadMap.addEdge("1320", "13", "20").addAttribute("length", 120);
        cityRoadMap.addEdge("1112", "11", "12").addAttribute("length", 200);
        cityRoadMap.addEdge("1221", "12", "21").addAttribute("length", 300);
        cityRoadMap.addEdge("1718", "17", "18").addAttribute("length", 220);
        cityRoadMap.addEdge("1419", "14", "19").addAttribute("length", 100);
        cityRoadMap.addEdge("1920", "19", "20").addAttribute("length", 100);
        cityRoadMap.addEdge("2021", "20", "21").addAttribute("length", 80);
        cityRoadMap.addEdge("2125", "21", "25").addAttribute("length", 150);
        cityRoadMap.addEdge("2425", "24", "25").addAttribute("length", 50);
        cityRoadMap.addEdge("1924", "19", "24").addAttribute("length", 220);
        cityRoadMap.addEdge("1923", "19", "23").addAttribute("length", 200);
        cityRoadMap.addEdge("1822", "18", "22").addAttribute("length", 200);
        cityRoadMap.addEdge("2226", "22", "26").addAttribute("length", 100);
        cityRoadMap.addEdge("2327", "23", "27").addAttribute("length", 100);
        cityRoadMap.addEdge("2627", "26", "27").addAttribute("length", 80);
        cityRoadMap.addEdge("2324", "23", "24").addAttribute("length", 100);
        cityRoadMap.addEdge("2223", "22", "23").addAttribute("length", 80);
        cityRoadMap.addEdge("1819", "18", "19").addAttribute("length", 80);
        cityRoadMap.addEdge("816", "8", "16").addAttribute("length", 230);
        cityRoadMap.addEdge("1617", "16", "17").addAttribute("length", 100);
        cityRoadMap.addEdge("428", "4", "28").addAttribute("length", 150);
        cityRoadMap.addEdge("2811", "28", "11").addAttribute("length", 100);
        
        //3.0 => 1.5 km/100 sec => 54 km/h
        //5.0 => 2.5 km/100 sec => 90 km/h
        //11.2 => 5.6 km/100 sec => circa 200 km/h => 261 pacchetti persi
        //5.6 => 2.8 km/100 sec = > circa 100 km/h => circa 5 secondi di latenza handover => 50 pacchetti persi
        cityRoadMap.getEdge("12").addAttribute("avgSpeed", 5.6);
        cityRoadMap.getEdge("13").addAttribute("avgSpeed", 5.6);
        cityRoadMap.getEdge("34").addAttribute("avgSpeed", 5.6);
        cityRoadMap.getEdge("45").addAttribute("avgSpeed", 5.6);
        cityRoadMap.getEdge("36").addAttribute("avgSpeed", 5.6);
        cityRoadMap.getEdge("611").addAttribute("avgSpeed", 5.6);
        cityRoadMap.getEdge("1011").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("910").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("27").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("28").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("89").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("79").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("916").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("915").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("1115").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("1113").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("1517").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("1415").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("1314").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("1320").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("1112").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("1221").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("1718").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("1419").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("1920").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("2021").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("2125").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("2425").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("1924").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("1923").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("1822").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("2226").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("2327").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("2627").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("2324").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("2223").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("1819").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("816").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("1617").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("428").addAttribute("avgSpeed", 5.0);
        cityRoadMap.getEdge("2811").addAttribute("avgSpeed", 5.0);
        
        //nasconde gli archi
        cityRoadMap.getEdge("12").addAttribute("ui.hide");
        cityRoadMap.getEdge("13").addAttribute("ui.hide");
        cityRoadMap.getEdge("34").addAttribute("ui.hide");
        cityRoadMap.getEdge("45").addAttribute("ui.hide");
        cityRoadMap.getEdge("36").addAttribute("ui.hide");
        cityRoadMap.getEdge("611").addAttribute("ui.hide");
        cityRoadMap.getEdge("1011").addAttribute("ui.hide");
        cityRoadMap.getEdge("910").addAttribute("ui.hide");
        cityRoadMap.getEdge("27").addAttribute("ui.hide");
        cityRoadMap.getEdge("28").addAttribute("ui.hide");
        cityRoadMap.getEdge("89").addAttribute("ui.hide");
        cityRoadMap.getEdge("79").addAttribute("ui.hide");
        cityRoadMap.getEdge("916").addAttribute("ui.hide");
        cityRoadMap.getEdge("915").addAttribute("ui.hide");
        cityRoadMap.getEdge("1115").addAttribute("ui.hide");
        cityRoadMap.getEdge("1113").addAttribute("ui.hide");
        cityRoadMap.getEdge("1517").addAttribute("ui.hide");
        cityRoadMap.getEdge("1415").addAttribute("ui.hide");
        cityRoadMap.getEdge("1314").addAttribute("ui.hide");
        cityRoadMap.getEdge("1320").addAttribute("ui.hide");
        cityRoadMap.getEdge("1112").addAttribute("ui.hide");
        cityRoadMap.getEdge("1221").addAttribute("ui.hide");
        cityRoadMap.getEdge("1718").addAttribute("ui.hide");
        cityRoadMap.getEdge("1419").addAttribute("ui.hide");
        cityRoadMap.getEdge("1920").addAttribute("ui.hide");
        cityRoadMap.getEdge("2021").addAttribute("ui.hide");
        cityRoadMap.getEdge("2125").addAttribute("ui.hide");
        cityRoadMap.getEdge("2425").addAttribute("ui.hide");
        cityRoadMap.getEdge("1924").addAttribute("ui.hide");
        cityRoadMap.getEdge("1923").addAttribute("ui.hide");
        cityRoadMap.getEdge("1822").addAttribute("ui.hide");
        cityRoadMap.getEdge("2226").addAttribute("ui.hide");
        cityRoadMap.getEdge("2327").addAttribute("ui.hide");
        cityRoadMap.getEdge("2627").addAttribute("ui.hide");
        cityRoadMap.getEdge("2324").addAttribute("ui.hide");
        cityRoadMap.getEdge("2223").addAttribute("ui.hide");
        cityRoadMap.getEdge("1819").addAttribute("ui.hide");
        cityRoadMap.getEdge("816").addAttribute("ui.hide");
        cityRoadMap.getEdge("1617").addAttribute("ui.hide");
        cityRoadMap.getEdge("428").addAttribute("ui.hide");
        cityRoadMap.getEdge("2811").addAttribute("ui.hide");
        
        //per colorare un arco
        //cityRoadMap.getEdge("AI").addAttribute("ui.style", "fill-color: red;");

        for (Edge e : cityRoadMap.getEachEdge()) {
            e.addAttribute("label", "" + (int) e.getNumber("length"));
        }
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
    public void riattesta(String bs, int id_mh, Point2D position) {
    	Node n = cityRoadMap.getNode(bs);
    	String id_router = n.getAttribute("router");
    	Router r = routers.get(id_router);
    	r.addMobileHost(id_mh, position);
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
