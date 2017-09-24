/*
 * Entita.java
 *
 * Created on 10 ottobre 2007, 12.48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package base_simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ing. Amilcare-Francesco Santamaria
 */
public class Entita {
    private long incrementalId;
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    protected scheduler s;
    protected String tipo;
    /** Creates a new instance of Entita */
    public Entita(scheduler s,String tipo) {
        this.tipo = tipo;
        this.s = s;        
    }
    
    public Entita(scheduler s, String tipo, long id){
        this.incrementalId= id;
        this.tipo=tipo;
        this.s=s;
    }
    
    public void Handler(Messaggi m){
        
    }
    
    public String toString(){
        return this.tipo;
    }
    
    public String getTipo(){
        return this.tipo;
    }
    
    public String getStat(){
        String s = "\nEntita";
        return s;
    }
    
    
    public void stampaInformazione(String tipo_messaggio, int id,String messaggio)
    {
       // System.out.println(tipo_messaggio+":"+s.orologio.getCurrent_Time()+" Tipo Nodo:"+this.tipo+" ID:"+id+" Messaggio:"+messaggio);
        logger.info("{} : {} Tipo Nodo: {} Id: {} messaggio: {}", tipo_messaggio, s.orologio.getCurrent_Time(),this.tipo,id,messaggio);
    }
    
    public Logger getLogger(){
        return logger;
    }

    public long getIncrementalId() {
        return incrementalId;
    }

    public void setIncrementalId(long incrementalId) {
        this.incrementalId = incrementalId;
    }

    public scheduler getS() {
        return s;
    }

    public void setS(scheduler s) {
        this.s = s;
    }
    
}
