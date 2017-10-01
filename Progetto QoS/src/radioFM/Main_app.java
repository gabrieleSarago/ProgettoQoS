/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package radioFM;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import Mobility.Mh_node;
import Mobility.MobilityMap;
import base_simulator.Grafo;
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

    private static void init_sim_parameters() {
    	//100 minuti di simulazione - ogni secondo corrisponde a 100 secondi
    	//quindi 100 minuti sono simulati in 60 secondi (60000 secondi = 100 minuti , 60000/100 = 60 secondi)
        s = new scheduler(6000000, false);
    }
    
    private String[] stazioni = {"RDS","RTL 102.5", "RAI RADIO 1", "RAI RADIO 2", "RAI RADIO 3", "RADIO DEEJAY"};

    /**
     * Creates new form main_app
     */
    public Main_app() {
    	File conf_file = new File("src/conf.xml");
        if (conf_file.exists()) {
            startParsing(conf_file);
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
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main_app.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main_app.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main_app.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main_app.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        init_sim_parameters();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main_app();
            }
        });
    }

    private Infos info = new Infos();

    MobilityMap roadMap;

    @SuppressWarnings("rawtypes")
	private boolean startParsing(File xmlFile) {
        roadMap = new MobilityMap();
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

            /*associazione router - base station, ogni base station
             * ha un attributo che identifica il router che la gestisce.
             */
            listElement = rootElement.getChildren("router");
            for(Object nodo : listElement) {
            	String id = ((Element) nodo).getAttributeValue("id");
            	int capacita = Integer.parseInt(((Element) nodo).getAttributeValue("cp"));
            	//Si aggiungono i router di 2� livello nella lista della MobilityMap
            	Router r = new Router(s, id, capacita, roadMap);
            	roadMap.addRouter(id, r);
            	r.start();
            	List listElement1 = ((Element) nodo).getChildren("base_station");
            	for(Object n : listElement1) {
            		String bs = ((Element) n).getText();
            		if(roadMap.cityRoadMap.getNode(bs).getAttribute("router") == null)
            			roadMap.cityRoadMap.getNode(bs).addAttribute("router", id);
            		else
            			roadMap.cityRoadMap.getNode(bs).setAttribute("router", id);
            	}
            }
            
            //generazione router di 1� livello
            listElement = rootElement.getChildren("first_router");
            for(Object nodo: listElement) {
            	String id = ((Element) nodo).getAttributeValue("id");
            	int capacita = Integer.parseInt(((Element) nodo).getAttributeValue("cp"));
            	UpperLevelRouter fr = new UpperLevelRouter(s, id, capacita, roadMap);
            	List listElement1 = ((Element) nodo).getChildren("r");
            	LinkedList<Router> downlink_routers = new LinkedList<>();
            	for(Object n : listElement1) {
            		String rID = ((Element) n).getText();
            		//connessioni downlink
            		downlink_routers.add(roadMap.getRouter(rID));
            		//connessioni uplink
            		roadMap.getRouter(rID).setUplink(fr);
            	}
            	roadMap.addFirstRouter(id, fr);
            	fr.addRouters(downlink_routers);
            	fr.start();
            }
            
            //generazione router gateway
            //NOTA: il gateway non ha connessioni uplink
            listElement = rootElement.getChildren("gateway_router");
            for(Object nodo: listElement) {
            	String id = ((Element) nodo).getAttributeValue("id");
            	int capacita = Integer.parseInt(((Element) nodo).getAttributeValue("cp"));
            	UpperLevelRouter gr = new UpperLevelRouter(s, id, capacita, roadMap);
            	List listElement1 = ((Element) nodo).getChildren("fr");
            	LinkedList<Router> downlink_routers = new LinkedList<>();
            	for(Object n : listElement1) {
            		String frID = ((Element) n).getText();
            		//connessioni downlink gateway-first_level
            		downlink_routers.add(roadMap.getFirstRouter(frID));
            		//connessioni uplink first_level-gateway
            		roadMap.getFirstRouter(frID).setUplink(gr);
            	}
            	roadMap.addFirstRouter(id, gr);
            	gr.addRouters(downlink_routers);
            	gr.start();
            }
            
            int lastNodeId = 1000;
            int idCanale = 1;
            listElement = rootElement.getChildren("pozzo");
            int counterNodeId = 1;
            for (Object nodo : listElement) {
                String nodo_ingresso = ((Element) nodo).getAttributeValue("nodo_ingresso");
                String nodo_uscita = ((Element) nodo).getAttributeValue("nodo_uscita");
                double exitGateAt = Double.parseDouble(((Element) nodo).getAttributeValue("exitAt"));
                double generationRate = Double.parseDouble(((Element) nodo).getAttributeValue("generationRate"));
                double maxVehicles = Double.parseDouble(((Element) nodo).getAttributeValue("maxVehicles"));
                int gateway = Integer.valueOf(((Element) nodo).getAttributeValue("gateway"));
                int showUI = Integer.valueOf(((Element) nodo).getAttributeValue("showUI"));

                double interExitTime = 60000.0 / generationRate;

                int vehicleCounter = 0;
                for (vehicleCounter = 0; vehicleCounter < maxVehicles; vehicleCounter++) {

                    int id = lastNodeId + counterNodeId;
                    counterNodeId++;
                    
                    Grafo grafo = new Grafo(5);
                    
                    Physical80211P pl = new Physical80211P(s, 0.0);
                    LinkLayer ll = new LinkLayer(s, 5.0);
                    waveNetLayer nl = new waveNetLayer(s, 5.0, grafo,showUI);
                    waveFSCTPTransportLayer tl = new waveFSCTPTransportLayer(s, 5.0);
                    
                    //Scelta stazione radio casuale
                    int i = (new Random()).nextInt(stazioni.length);
                    String station_name = stazioni[i];
                    System.out.println(station_name);
                    
                    MobileHost nh = new MobileHost(s, id, pl, ll, nl, tl, null, "nodo_host", gateway, station_name);
                    
                    nh.setMappa(roadMap);
                    nh.setNodo_ingresso(nodo_ingresso);
                    nh.setNodo_uscita(nodo_uscita);
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
