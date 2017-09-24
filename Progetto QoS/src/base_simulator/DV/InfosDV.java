/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base_simulator.DV;

import base_simulator.layers.DV.NetworkLayer;
import java.util.ArrayList;

/**
 *
 * @author afsantamaria
 */
public class InfosDV {

    private ArrayList<canale> channels;
    private ArrayList<NodoDV> nodes;
    private ArrayList<Link> links;

    public InfosDV() {
        channels = new ArrayList<canale>();
        nodes = new ArrayList<NodoDV>();
        links = new ArrayList<Link>();
    }

    public void addCanale(canale c) {
        channels.add(c);
    }

    public void addNodo(NodoDV n) {
        n.setInfo(this);
        nodes.add(n);
    }

    public void addLink(Link l) {
        links.add(l);
    }

    public NodoDV getNodo(int id) {
        NodoDV res = null;
        for (Object n : nodes) {
            NodoDV item = (NodoDV) n;
            if (item.getId() == id) {
                res = item;
                break;
            }
        }
        return res;
    }

    public canale getCanale(int id) {
        canale res = null;
        for (Object c : channels) {
            canale item = (canale) c;
            if (item.getId() == id) {
                res = item;
                break;
            }
        }
        return res;
    }

    public link_extended getLink(int start, int end) {
        link_extended res = null;
        for (Object l : links) {
            link_extended item = (link_extended) l;
            if (item.getSource() == start && item.getDest() == end) {
                res = item;
                break;
            }
        }
        return res;
    }

    /**
     * Stampa statistiche nodo Host
     */
    void stampaStatisticheNodo() {
        for (Object obj : this.nodes) {
            NodoDV n = (NodoDV) obj;
            System.out.println("=====STAMPA STATISTICHE NODO NETWORK LAYER====");
            NetworkLayer nl = n.myNetLayer;
            String s = nl.getStat();
            System.out.println(s);
            System.out.println("=====FINE====\n");
        }
    }

}
