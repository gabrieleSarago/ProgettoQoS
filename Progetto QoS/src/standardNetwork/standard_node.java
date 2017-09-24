package standardNetwork;

import base_simulator.Entita;
import base_simulator.Messaggi;
import base_simulator.scheduler;
import base_simulator.layers.*;

/**
 *
 */
public class standard_node extends Entita {

    private LinkLayer ll;
    private NetworkLayer nl;
    private physicalLayer pl;
    String stats;

    public standard_node(scheduler s, String tipo) {
        super(s, tipo);
    }

    /**
     *
     * @param m : Informazione da gestire
     */
    public void Handler(Messaggi m) {
        if(m.saliPilaProtocollare == true)
        {
            //Passo il messaggio al livello fisico, ma non lo gestisco 
            //non ho avanzamento temporale
            m.shifta(0);
            m.setSorgente(this);
            m.setDestinazione(this.pl);
            s.insertMessage(m);
        }
        else
        {
            //Devo inviare il messaggio sul canale
        }
    }

    /**
     *
     */
    @Override
    public String getStat(){
        String s;
        s = "====standard_node====\n";
        s+= "Type..:"+this.tipo+"\n";
        return s;        
    }
}
