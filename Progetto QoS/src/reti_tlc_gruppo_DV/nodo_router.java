/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reti_tlc_gruppo_DV;

import base_simulator.DV.NetworkInterface;
import base_simulator.DV.InfosDV;
import base_simulator.DV.Grafo;
import base_simulator.DV.Messaggi;
import base_simulator.DV.NodoDV;
import base_simulator.DV.canale;
import base_simulator.layers.DV.LinkLayer;
import base_simulator.layers.DV.NetworkLayer;
import base_simulator.layers.DV.TransportLayer;
import base_simulator.layers.DV.physicalLayer;
import base_simulator.DV.schedulerDV;
import java.util.ArrayList;

/**
 *
 * @author afsantamaria
 */
public class nodo_router extends NodoDV {

    /*PROTOCOL TYPE*/
    final String RIP_STR = "RIP";
    final String TABLE_STR_AUTO = "auto"; //Le tabelle sono popolate all'inizio come se il nodo conoscesse tutta la rete
    final String TABLE_STR_ROUND = "round"; //Le tabelle sono aggiornate di passo in passo seguendo il protocollo
    /**/

    private InfosDV info = null;

    public InfosDV getInfo() {
        return info;
    }

    public void setInfo(InfosDV info) {
        this.info = info;
    }

    private int gatewayId; //router di default
    private ArrayList<NetworkInterface> nics = new ArrayList<NetworkInterface>();
    private ArrayList<Integer> neighbours = new ArrayList<Integer>();

    private String protocol = RIP_STR;
    private String routing = TABLE_STR_AUTO;

    public nodo_router(schedulerDV s, int id_nodo,
            physicalLayer myPhyLayer, LinkLayer myLinkLayer, NetworkLayer myNetLayer, TransportLayer myTransportLayer,
            Grafo network, String tipo, int gtw) {
        super(s, id_nodo, myPhyLayer, myLinkLayer, myNetLayer, myTransportLayer, network, tipo);

        gatewayId = gtw;
    }

    public int getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(int gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getRouting() {
        return routing;
    }

    public void setRouting(String routing) {
        this.routing = routing;
    }

    private void inviaMessaggioACanale(Messaggi m) {

        int channel_id = 0;
        NetworkInterface i = null;
        for (Object interface_element : nics) {
            i = (NetworkInterface) interface_element;
            if (i.getDest() == m.getNextHop_id()) {
                channel_id = i.getChannel_idx();
                break;
            }
        }

        canale current_channel = info.getCanale(channel_id);

        //Invio il messaggio al canale
        m.shifta(0);
        m.setSorgente(this);
        m.setNextHop(info.getNodo(m.getNextHop_id()));
        m.setDestinazione(current_channel);
        s.insertMessage(m);

        //System.out.println("I : "+this.getTipo()+": IP :"+ i.getIpv4() +" Invio messaggio su canale :"+current_channel.getId());
    }

    private void inviaMessaggioAPhyLayer(Messaggi m) {

        //System.out.println("I : "+this.getTipo()+": Ricevuto msg dal canale :"+((canale)m.getSorgente()).getId());
        m.shifta(0);
        m.setSorgente(this);
        m.setDestinazione(this.myPhyLayer);
        s.insertMessage(m);
    }

    @Override
    public void Handler(Messaggi m) {
        if (m.saliPilaProtocollare == true) {
            //il messaggio entra nel nodo e sarà elaborato al suo interno
            inviaMessaggioAPhyLayer(m);
        } else {
            //il messaggio dovrà essere propagato all'esterno
            inviaMessaggioACanale(m);
        }
    }

    public void addNIC(NetworkInterface nic) {
        this.nics.add(nic);
    }

    public NetworkInterface getNic(int idx) {
        return nics.get(idx);
    }

    public int getNicsSize() {
        return this.nics.size();
    }

    int getGTW() {
        return this.gatewayId;
    }

}
