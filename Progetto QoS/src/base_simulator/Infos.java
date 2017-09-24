/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base_simulator;

import base_simulator.layers.NetworkLayer;
import base_simulator.layers.TransportLayer;
import base_simulator.layers.physicalLayer;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author afsantamaria
 */
public class Infos {
    private ArrayList<canale> channels;
    private ArrayList<Nodo> nodes;
    private ArrayList<Link> links;
    final Logger logger = LoggerFactory.getLogger(Infos.class);


    public ArrayList<canale> getChannels() {
        return channels;
    }

    public void setChannels(ArrayList<canale> channels) {
        this.channels = channels;
    }

    public ArrayList<Nodo> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<Nodo> nodes) {
        this.nodes = nodes;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<Link> links) {
        this.links = links;
    }
    
    public Infos()
    {
        channels = new ArrayList<canale>();
        nodes = new ArrayList<Nodo>();
        links  = new ArrayList<Link>();
    }
    
    public void addCanale(canale c)
    {
        channels.add(c);
    }
    
    public void addNodo(Nodo n)
    {
        n.setInfo(this);
        nodes.add(n);
    }
    
    public void addLink(Link l)
    {
        links.add(l);
    }
    
    public Nodo getNodo(int id)
    {
        Nodo res = null;
        for(Object n : nodes)
        {
            Nodo item = (Nodo)n;
            if(item.getId() == id){
                res = item;
                break;
            }                                    
        }
        return res;
    }
    
    public canale getCanale(int id)
    {
        canale res = null;
        for(Object c : channels)
        {
            canale item = (canale)c;
            if(item.getId() == id){
                res = item;
                break;
            }                                    
        }
        return res;
    }
    
    public link_extended getLink(int start,int end)
    {
        link_extended res = null;
        for(Object l : links)
        {
            link_extended item = (link_extended)l;
            if(item.getSource() == start && item.getDest()==end){
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
        for(Object obj : this.nodes)
        {
            Nodo n = (Nodo)obj;
            
            TransportLayer tl = n.myTransportLayer;
            tl.stampaStatistiche();
            
           // System.out.println("=====STAMPA STATISTICHE NODO NETWORK LAYER====");
            NetworkLayer nl = n.myNetLayer;
           // String s = nl.getStat();
            nl.stampaStatistiche();
           // System.out.println(s);
           // System.out.println("=====FINE====");
            
            physicalLayer pl = n.myPhyLayer;
            pl.stampaStatistiche();
        }
    }

    

    
    
    
    
    
    
}
