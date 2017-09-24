/*
 * dataLinkLayer.java
 *
 * Created on 10 ottobre 2007, 8.44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 * @author franco
 */
package base_simulator.layers.DV;

import base_simulator.DV.Messaggi;
import base_simulator.DV.schedulerDV;
import base_simulator.DV.Entita;

public class LinkLayer extends Entita {

    /**
     * Creates a new instance of dataLinkLayer
     */
    protected schedulerDV s;
    protected double tempo_processamento;
    protected NetworkLayer netLayer;
    protected physicalLayer phyLayer;
    protected Object nodo;
    private double header_size = 20; // Header in Byte

    public double getHeader_size() {
        return header_size;
    }

    public void setHeader_size(double header_size) {
        this.header_size = header_size;
    }

    public LinkLayer(schedulerDV s, double tempo_processamento) {
        super(s, "Link Layer");
        this.s = s;
        this.tempo_processamento = tempo_processamento;
        this.netLayer = null;
    }

    public void connectLinkLayer(physicalLayer phyLayer, NetworkLayer netLayer, Object nodo) {
        this.nodo = nodo;
        this.phyLayer = phyLayer;
        this.netLayer = netLayer;
    }

    public void setlinkLayer(physicalLayer phyLayer) {
        this.phyLayer = phyLayer;
    }

    public void Handler(Messaggi m) {
        //System.out.println("\nMessaggio arrivato a LinkLayer");
        if (m.saliPilaProtocollare) {
            m.removeHeader(header_size);
            inviaANetworkLayer(m);
        } else {
            m.addHeader(header_size);

            inviaAphysicalLayer(m);
        }
    }

    private void inviaANetworkLayer(Messaggi m) {
        //System.out.println("   Dal LinkLayer ---> network");
        m.shifta(tempo_processamento);
        m.setSorgente(this);
        m.setDestinazione(netLayer);
        s.insertMessage(m);
    }

    private void inviaAphysicalLayer(Messaggi m) {
        //System.out.println("   Dal LinkLayer ---> fisico");

        m.shifta(tempo_processamento);
        m.setSorgente(this);
        m.setDestinazione(phyLayer);
        s.insertMessage(m);
    }

}
