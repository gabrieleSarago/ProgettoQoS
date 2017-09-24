/*
 * physicalLayer.java
 *
 * Created on 10 ottobre 2007, 8.42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author Amilcare Francesco Santamaria
 * @date 15.11.2013
 * 
 */

package base_simulator.layers;
import base_simulator.*;

public class physicalLayer extends Entita {
    
    protected scheduler s;
    protected double tempo_processamento_bit;
    protected LinkLayer linkLayer;
    /*Oggetto proprietario della pila che mi interfaccierà con il mondo esterno*/
    protected Object nodo;
    public double header_size = 26; //Byte di header

    public double getHeader_size() {
        return header_size;
    }

    public void setHeader_size(double header_size) {
        this.header_size = header_size;
    }
    
    
    
    /** Creates a new instance of physicalLayer
     * @param s
     * @param tempo_processamento_bit */
    public physicalLayer(scheduler s,double tempo_processamento_bit) {
        super(s,"Physical Layer");
        this.s = s;
        this.tempo_processamento_bit = tempo_processamento_bit;
        
    }
    
    public void connectPhysicalLayer(LinkLayer linkLayer, Object nodo) {
        this.linkLayer = linkLayer;
        this.nodo = nodo;
    }
    /**Metodo di default del phy layer
     Per gestire la correzzione di errore e la modulazione codifica etc
     basterà sovrascrivere i metodi di passaggio ai livelli successivi quindi
     *inviaAlinkLayer(Messaggi m)
     *inviaAnodo(Messaggi m)
     * @param m
     */
    @Override
    public void Handler(Messaggi m)
    {
        //System.out.println("\nIl messaggio è arrivato a livello fisico");
        if(m.saliPilaProtocollare)
        {   
            m.removeHeader(header_size);
            inviaAlinkLayer(m);
        }
        else 
        {
            m.addHeader(header_size);
            inviaAnodo(m);
        }
    }
    
    /*Invio il pacchetto al LinkLayer*/
    public void inviaAlinkLayer(Messaggi m){
        m.shifta(tempo_processamento_bit);
        m.setSorgente(this);
        m.setDestinazione(linkLayer);
        s.insertMessage(m);
    }

    /*La gestione interna è terminata posso passare il messaggio all'oggetto nodo che lo metterà sul canale*/
    public void inviaAnodo(Messaggi m) {
        m.shifta(this.tempo_processamento_bit);
        m.setSorgente(this);
        //Invio il messaggio al box esterno che provvederà a mettere il messaggio sul canale fisico il quale poi invierà verso il nodo destinazione
        m.setDestinazione(nodo);
        m.saliPilaProtocollare = false;
//        m.setNextHop(m.getNodoDestinazione());
        s.insertMessage(m);
    }
    
    public void stampaStatistiche()
    {
        getLogger().info("non ci sono statistiche");
    }
    
}
