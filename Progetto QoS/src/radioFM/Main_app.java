/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package radioFM;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import Mobility.Mh_node;
import Mobility.MobilityMap;
import base_simulator.Infos;
import base_simulator.canale;
import base_simulator.link_extended;
import base_simulator.scheduler;
import base_simulator.layers.LinkLayer;

/**
 *
 * @author afsantamaria
 */
public class Main_app {
	
	private static scheduler s;
	private static SettingsWindow frame;

    private static void init_sim_parameters() {
        //Tempo simulazione = 600 secondi
    	s = new scheduler(600000, false);
    }
    
    /**
     * Creates new form main_app
     */
    public Main_app() {
    	File conf_file = new File("src/conf.xml");
        if (conf_file.exists()) {
            startSimulation(conf_file);
        } else {
            System.out.println("File non esistente");
        }
        //3 . Ready to start simulation
        new Thread(s).start();
        //initComponents();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
    	try {
    	    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
    	        if ("Nimbus".equals(info.getName())) {
    	            UIManager.setLookAndFeel(info.getClassName());
    	            break;
    	        }
    	    }
    	} catch (Exception e) {
    	    // If Nimbus is not available, you can set the GUI to another look and feel.
    	}
       
        //</editor-fold>
        init_sim_parameters();
        
        frame = new SettingsWindow();

    }

    private Infos info = new Infos();

    MobilityMap roadMap;

    @SuppressWarnings("rawtypes")
	private boolean startSimulation(File xmlFile) {
    	double radius = frame.radius;
        int numNodi = frame.numNodi;
        //3.0 => 1.5 km/100 sec => 54 km/h
        //5.0 => 2.5 km/100 sec => 90 km/h => 2 pacchetti persi
        //11.2 => 5.6 km/100 sec => circa 200 km/h
        //5.6 => 2.8 km/100 sec = > circa 100 km/h => 5 pacchetti persi
        double maxSpeed = frame.speedMax;
        double minSpeed = frame.speedMin;
        //espressi in secondi
        double minHandoff = frame.minTimeHandoff;
        double maxHandoff = frame.maxTimeHandoff;
    	int avgRate = frame.avgRate;
    	int packetSize = frame.pSize;
    	//mobile host
        int numMobHost = frame.numMH;
        int generationRate = frame.rate;
        //MA
        //espressi in ms
    	double routeUpdateTime = frame.routeUpdateTime;
    	double pagingUpdateTime = frame.pagingUpdateTime;
    	//capacita GMA
    	int capacitaGMA = frame.capacitaGMA;
    	int capacitaMA = frame.capacitaMA;
    	//mappa
    	int width = frame.width;
    	int height = frame.height;
    	
    	/*double radius = 100.02;
        int numNodi = 10;
        //3.0 => 1.5 km/100 sec => 54 km/h
        //5.0 => 2.5 km/100 sec => 90 km/h => 2 pacchetti persi
        //11.2 => 5.6 km/100 sec => circa 200 km/h
        //5.6 => 2.8 km/100 sec = > circa 100 km/h => 5 pacchetti persi
        double maxSpeed = 11.2;
        double minSpeed = 3.0;
        //espressi in secondi
        double minHandoff = 0.0;
        double maxHandoff = 0.09;
    	int avgRate = 144;
    	int packetSize = 1526;
    	//mobile host
        int numMobHost = 10;
        double generationRate = 2.0;
        //MA
        //espressi in ms
    	double routeUpdateTime = 50.0;
    	double pagingUpdateTime = 500.0;
    	//capacita MA
    	int cap = 0;
    	//mappa
    	int lenght = 2000;
    	int height = 1000;*/
        roadMap = new MobilityMap(s, radius, width, height, numNodi, minSpeed, maxSpeed);
        //Avvio dei router
        for(Entry<String, Router> e : roadMap.routers.entrySet()){
        	e.getValue().setRouteUpdateTime(routeUpdateTime);
        	e.getValue().setPagingUpdateTime(pagingUpdateTime);
        	e.getValue().start();
        }
        //Avvio dei router di primo livello
        for(Entry<String, UpperLevelRouter> e : roadMap.ul_routers.entrySet()){
        	e.getValue().setRouteUpdateTime(routeUpdateTime);
        	e.getValue().setPagingUpdateTime(pagingUpdateTime);
        	e.getValue().start();
        }
        //avvio del gateway
        roadMap.getGateway().setRouteUpdateTime(routeUpdateTime);
        roadMap.getGateway().setPagingUpdateTime(pagingUpdateTime);
        roadMap.getGateway().start();
        
        SAXBuilder saxBuilder = new SAXBuilder();
        boolean res = false;
        try {

            Document document = (Document) saxBuilder.build(xmlFile);

            Element rootElement = document.getRootElement();

            List listElement = rootElement.getChildren("canali");

            for (int i = 0; i < listElement.size(); i++) {
                /*
<canale id="0" tipo="WIRED" capacita="5000000" dim_pacchetto = "1526" tempo_propagazione = "10"></canale> 
                 */
                Element node = (Element) listElement.get(i);

                List listElement1 = node.getChildren("canale");
                for (Object listElement2 : listElement1) {

                    int id = Integer.valueOf(((Element) listElement2).getAttributeValue("id"));
                    double capacita = Double.valueOf(((Element) listElement2).getAttributeValue("capacita"));
                    System.out.println("tipo: "
                            + ((Element) listElement2).getAttributeValue("tipo"));
                    double dim_pckt = Double.valueOf(((Element) listElement2).getAttributeValue("dim_pacchetto"));

                    double tempo_prop = Double.valueOf(((Element) listElement2).getAttributeValue("tempo_propagazione"));

                    canale c = new canale(s, id, capacita, dim_pckt, tempo_prop);

                    info.addCanale(c);
                }
            }
            
            int lastNodeId = 1000;
            int idCanale = 1;
            int counterNodeId = 1;
            
            double exitGateAt = 0.0;
            int gateway = 0;
            int showUI = 0;
            double interExitTime = 60000.0 / generationRate;
            for (int j = 0; j < numMobHost; j++) {
            	Random r = new Random();
                String nodo_ingresso = "" + r.nextInt(numNodi);
                String nodo_uscita = "" + r.nextInt(numNodi);
                while(nodo_ingresso.equals(nodo_uscita)){
                	nodo_uscita =  "" + r.nextInt(numNodi);
                }
                
                int id = lastNodeId + counterNodeId;
                counterNodeId++;

                //Grafo grafo = new Grafo(5);

                Physical80211P pl = new Physical80211P(s, 0.0);
                LinkLayer ll = new LinkLayer(s, 5.0);
                waveNetLayer nl = new waveNetLayer(s, 5.0,/*grafo*/ null,showUI);
                waveFSCTPTransportLayer tl = new waveFSCTPTransportLayer(s, 5.0);

                //previsto dato da inserire
                MobileHost nh = new MobileHost(s, id, pl, ll, nl, tl, null, "nodo_host", gateway, null);

                nh.setMappa(roadMap);
                nh.setNodo_ingresso(nodo_ingresso);
                nh.setNodo_uscita(nodo_uscita);
                nh.setMinHandoff(minHandoff);
                nh.setMaxHandoff(maxHandoff);
                nh.setAvgRate(avgRate);
                nh.setPacketSize(packetSize);
                nh.setExitFromGate(exitGateAt);
                Mh_node car = new Mh_node(id, 0, 0);
                roadMap.mobile_hosts.put("" + id, car);
                roadMap.mobHost.put(id, nh);

                canale c = new canale(s, idCanale,
                		info.getCanale(0).returnCapacita(),
                		info.getCanale(0).getDimensione_pacchetto(),
                		info.getCanale(0).getTempo_di_propagazione());
                info.addCanale(c);
                idCanale++;

                nh.setMy_wireless_channel(c);

                nl.setDefaultGateway(gateway);

                //Aggiorno il tempo di uscita del prossimo veicolo
                exitGateAt += interExitTime;
                info.addNodo(nh);
            }

            /*listElement = rootElement.getChildren("router");

            for (Object routers_list : listElement) {

                int node_id = Integer.valueOf(((Element) routers_list).getAttributeValue("id"));
                int gateway = Integer.valueOf(((Element) routers_list).getAttributeValue("gateway"));
                int numero_nodi = Integer.parseInt(((Element) routers_list).getAttributeValue("net_size"));
                Grafo grafo = new Grafo(numero_nodi);

                physicalLayer pl = new physicalLayer(s, 0.0);
                LinkLayer ll = new LinkLayer(s, 5.0);
                netLayerLinkState nl = new netLayerLinkState(s, 5.0, grafo);
                TransportLayer tl = new TransportLayer(s, 5.0);

                nodo_router nr = new nodo_router(s, node_id, pl, ll, nl, tl, null, "nodo_router", 0);
//PHY
                pl.connectPhysicalLayer(ll, nr);
//LL                
                ll.connectLinkLayer(pl, nl, nr);
//NET                
                nl.connectNetworkLayer(tl, ll, nr);
                nl.setDefaultGateway(gateway);
//TRASP                
                tl.connectTransportLayer(nl, nr);

                System.out.println("Ho aggiunto un " + nr.getTipo() + " con id..:" + nr.getId());

                List protocol_list = ((Element) routers_list).getChildren("protocol");

                for (Object protocol_element : protocol_list) {

                    Element item = (Element) protocol_element;

                    String tipo = item.getAttributeValue("tipo");
                    int TTL = 0;

                    if (tipo.equals("OSPF")) {
                        TTL = Integer.valueOf(item.getAttributeValue("TTL"));

                        nl.enableFullOSPF();
                        nl.setTTL_LSA(TTL);
                    }

                    String routing = item.getAttributeValue("ROUTING");
                    nr.setProtocol(tipo);
                    nr.setRouting(routing);
                    nr.setTTL(TTL);
                }

                List listElement1 = ((Element) routers_list).getChildren("interfaces");

                //Faccio la clear dei rami allocati e alloco una nuova struttura dati
                //popolo il grafo solo con i propri vicini                
                for (Object interfaces_list : listElement1) {
                    List intertace_list = ((Element) interfaces_list).getChildren("interface");

                    for (Object obj_interfaccia : intertace_list) {
                        System.out.println("idinterfaccia:" + ((Element) obj_interfaccia).getAttributeValue("id"));
                        int if_id = Integer.valueOf(((Element) obj_interfaccia).getAttributeValue("id"));
                        String IP = ((Element) obj_interfaccia).getAttributeValue("IP");
                        int channelId = Integer.valueOf(((Element) obj_interfaccia).getAttributeValue("canale"));
                        int dest = Integer.valueOf(((Element) obj_interfaccia).getAttributeValue("dest"));
                        double metrica = Double.valueOf(((Element) obj_interfaccia).getAttributeValue("metrica"));
                        NetworkInterface nic = new NetworkInterface(if_id, IP, dest, channelId, metrica);
                        nr.addNIC(nic);

                        //Inserimento dati di routing
                        nl.addRoutingTableEntry(dest, dest, metrica);

//Popolazione iniziale topologia                        
                        grafo.setCosto(nr.getId(), dest, metrica, 0.0);
                        grafo.setCosto(dest, nr.getId(), metrica, 0.0);

                    }
                }

                info.addNodo(nr);

            }*/

            listElement = rootElement.getChildren("network");

            for (Object network_list : listElement) {
                List branches = ((Element) network_list).getChildren("ramo");
                for (Object branch_element : branches) {
                    Element branch = ((Element) branch_element);

                    double metric = Double.valueOf(branch.getAttributeValue("metrica"));
                    int nodo_iniziale = Integer.valueOf(branch.getAttributeValue("start"));
                    int nodo_finale = Integer.valueOf(branch.getAttributeValue("end"));
                    String tipo = branch.getAttributeValue("tipo");
                    link_extended l = new link_extended(nodo_iniziale, nodo_finale, metric);
                    info.addLink(l);

                    if (tipo.equals("full")) {
                        link_extended l1 = new link_extended(nodo_finale, nodo_iniziale, metric);
                        info.addLink(l1);
                    }
                }
            }

            s.setInfo(info);

        } catch (IOException io) {
            System.out.println(io.getMessage());
        } catch (JDOMException jdomex) {

            System.out.println(jdomex.getMessage());

        }
        return res;
    }

}
