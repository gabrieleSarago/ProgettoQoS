/*
 * canale.java
 *
 * Created on 9 ottobre 2007, 14.40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 * @author Amilcare Francesco Santamaria
 */
package base_simulator;
import java.util.*;

public class canale extends Entita{
    
    /*scheduler del simulatore*/
    private scheduler s;
    /* Il nodo 1 � la sorgente del canale*/
    private Object nodo1;
    /*canale condiviso*/
    private ArrayList<Object> nodiSorgente;
    /*il/i nodo2 rappresentano le uscite*/
    private ArrayList<Object> nodo2;
    /*Capacita del canale in bps*/
    private double capacita;
    /*Indica la capacita allocata sul canale*/
    private double capacitaAllocata;
    /*Indica il tempo necessario affinchè il pacchetto sia trasmesso sul canale*/
    private double tempo_di_trasmissione;
    /*Indica il tempo necessario al pacchetto per raggiungere la destinazione*/
    private double tempo_di_propagazione;
    /**Indica la dimensione del pacchetto in byte*/
    private double dimensione_pacchetto;
    /*potrebbe indicare se di uplink o downlink di default viene posto a ""*/
    private String tipo = "";
    
    private int modelloTrasmissione;
    
    /*indica l'id univoco del canale*/
    private int id;
    
    public boolean occupato = false;
    
    
    /** Creates a new instance of canale */
    public canale(scheduler s,int id,double capacita,double dimensione_pacchetto,double tempo_di_propagazione) {
        super(s,"Canale");
        this.s = s;
        this.id = id;
        this.capacita = capacita;
        this.capacitaAllocata = 0;
        nodo1 = new Object();
        //Da usare solo nel caso di canale condiviso -- nel nostro caso sul canale di Uplink
        nodiSorgente = new ArrayList<Object>();
        nodo2 = new ArrayList<Object>();
        /**il *8 indica la conversione da byte in bit del pacchetto*/
        this.dimensione_pacchetto = dimensione_pacchetto;
        tempo_di_trasmissione = 1000*(dimensione_pacchetto * 8)/capacita;
        this.tempo_di_propagazione = tempo_di_propagazione;
    }

    public double getTempo_di_propagazione() {
        return tempo_di_propagazione;
    }

    public double getDimensione_pacchetto() {
        return dimensione_pacchetto;
    }
    public canale(scheduler s,double capacita,double dimensione_pacchetto,double tempo_di_propagazione) {
        super(s,"Canale");
        this.s = s;
        
        this.capacita = capacita;
        this.capacitaAllocata = 0;
        nodo1 = new Object();
        //Da usare solo nel caso di canale condiviso -- nel nostro caso sul canale di Uplink
        nodiSorgente = new ArrayList<Object>();
        nodo2 = new ArrayList<Object>();
        /**il *8 indica la conversione da byte in bit del pacchetto*/
        tempo_di_trasmissione = 1000*(dimensione_pacchetto * 8)/capacita;
        this.tempo_di_propagazione = tempo_di_propagazione;
    }
    
    public void setNodo1(Object nodo1){
        this.nodo1 = (Object)nodo1;
    }
    public void setNodiSorgenti(ArrayList<Object> nodiSorgente)
    {
        this.nodiSorgente = (ArrayList<Object>) nodiSorgente.clone();
    }
   
    public void setNodo2(ArrayList<Object> nodo2){
        this.nodo2 = (ArrayList<Object>)nodo2.clone();
    }
    
    /**
     * @brief costruttore per canale 1-n 
     * @param s
     * @param id
     * @param capacita
     * @param nodo1
     * @param nodo2
     * @param dimensione_pacchetto
     * @param tempo_di_propagazione
     */
    public canale(scheduler s,int id,double capacita,Object nodo1,Vector<Object> nodo2,double dimensione_pacchetto,double tempo_di_propagazione) {
        super(s,"Canale");
        this.s = s;
        this.id = id;
        this.capacita = capacita;
        this.capacitaAllocata = 0;
        this.nodo1 = nodo1;
        this.nodo2 = (ArrayList<Object>) nodo2.clone();
        tempo_di_trasmissione = 1000*(dimensione_pacchetto * 8)/capacita;
        this.tempo_di_propagazione = tempo_di_propagazione;
    }
    
    /**
     * @brief Canale di tipo 1-1
     * @param s
     * @param id
     * @param capacita
     * @param nodo1
     * @param nodo2
     * @param dimensione_pacchetto
     * @param tempo_di_propagazione
     */
    public canale(scheduler s,int id,double capacita,Object nodo1,Object nodo2,double dimensione_pacchetto,double tempo_di_propagazione) {
        super(s,"Canale");
        this.s = s;
        this.id = id;
        this.capacita = capacita;
        this.capacitaAllocata = 0;
        this.nodo1 = nodo1;
        this.nodo2 = new ArrayList<Object>();
        this.nodo2.add(nodo2);
        tempo_di_trasmissione = 1000*(dimensione_pacchetto * 8)/capacita;
        this.tempo_di_propagazione = tempo_di_propagazione;
    }
    
    
    
    
    public void Handler(Messaggi m){
//        System.out.println("Il messaggio è arrivato nel canale");
        gestisciData(m);
    }
    
    public void setTipo(String tipo){
        this.tipo = tipo;
    }
    public String toString(){
        return "canale id "+id+" "+tipo;
    }
    
    public int getId(){
        return id;
    }
    
    public Object getNodo2at(int pos)
    {
        return this.nodo2.get(pos);
    }
    
    public int getSizeNodo2(){
        return nodo2.size();
    }
    
    public void gestisciData(Messaggi m){    
        Messaggi m1;
        m1 = applicaModellocanale(m);
        /*Il pacchetto transita nel canale*/
        m1.setSorgente(this);
        /**
         * In questo modo indico che parte della banda è dedicata all'invio dei messaggi di protocollo
         * Devo ricordarmi di settare il rate preallocata nei terminali in modo da avere sempre l'invio alla banda allocata
         */
        
        //TODO - Da verificare questo controllo
        boolean isMessaggioAllocazione = false;
/*        
        if(!m.isData && !isMessaggioAllocazione)
            m1.rate = capacita;
        if(isMessaggioAllocazione){
            this.tempo_di_trasmissione = 1000*(dimensione_pacchetto * 8)/capacita;
            m1.rate=m.rate;
        }
        else {
            this.tempo_di_trasmissione = 1000*(dimensione_pacchetto * 8)/m.rate;
            m1.rate=m.rate;
        }
*/        
        m1.shifta(this.tempo_di_trasmissione+this.tempo_di_propagazione);
        m1.setDestinazione(m1.getNextHop());
        m1.saliPilaProtocollare = true;
        s.insertMessage(m1);
    }
    
    /*Pu� essere specificato nelle classi ereditarie per simulare il canale wireless o satellitare
     nel caso di default il messaggio non viene toccato*/
    public Messaggi applicaModellocanale(Messaggi m){
        return m;
        //return null e gestire se il messaggio viene perso
    }
    /**
     * Questo metodo permette di aggiungere un nodo destinazione alla parte di output del canale
     * @param nodo
     */
    public void addNodoalCanale(Object nodo) {
        this.nodo2.add(nodo);
    }
    
    /** 
     * Questo metodo mi permette di aggiungere più nodi sorgente al canale, da utilizzare solo quando si ha un canale condiviso da più sorgenti
     * @param nodo
     */
    public void addNodiSorgente(Object nodo){
        this.nodiSorgente.add(nodo);
    }
    public ArrayList getNodiSorgenti(){
        ArrayList ns = new ArrayList();
        for(int i = 0;i<nodiSorgente.size();i++)
            ns.add(nodiSorgente.get(i));
        return ns;
    }
    public double returnCapacita(){
        return this.capacita;
    }
    public double returnCapacitaAllocata(){
        return this.capacitaAllocata;
    }
    public double returnCapacitaResidua(){
        return this.capacita-this.capacitaAllocata;
    }
    public void allocaBanda(double rate){
        this.capacitaAllocata+=rate;
        //System.out.println("Capacità residua:"+(this.capacita-this.capacitaAllocata));
        getLogger().info("Capacità residua: {}",(this.capacita-this.capacitaAllocata));
    }
}
