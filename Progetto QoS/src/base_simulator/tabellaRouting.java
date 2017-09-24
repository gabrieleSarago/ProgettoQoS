package base_simulator;

import java.util.ArrayList;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;


public class tabellaRouting
{
	private ArrayList<RoutingRow> entries;
         final Logger logger = LoggerFactory.getLogger(tabellaRouting.class);
	
	public tabellaRouting()
	{
            entries = new ArrayList<RoutingRow>();
	}
	
	/**
	 * 
	 * @param nodo_destinazione Nodo-Id  - Identificativo del nodo destinazione (Univoco)
	 * @param next_hop Next-Hop - Identificativo del prossimo nodo per raggiungere dest
	 * @param costo - Costo per raggiungere il nodo_destinazione
	 * @brief Aggiunge un entry alla tabella di routing 
	 */
	public void addEntry(int nodo_destinazione, int next_hop, double costo)
	{
		RoutingRow entry = new RoutingRow(nodo_destinazione,next_hop,costo);
		
		int pos = controllaPresenzaLinea(nodo_destinazione,next_hop);
		
		if(pos == -1)
		{
			entries.add(entry);
		}
		else
		{
			if((entries.get(pos)).getCosto() != costo)
			{
				entries.get(pos).setCosto(costo);
			}
		}
	}
	
	/**
	 * 
	 * @param dest : Nodo-Id  - Identificativo del nodo destinazione (Univoco)
	 * @param next : Next-Hop - Identificativo del prossimo nodo per raggiungere dest
	 * @return ritorna la posizione (Linea) nella tabella di routing se presente altrimenti -1 
	 */
	public int controllaPresenzaLinea(int dest,int next)
	{
		boolean found = false;
		int pos = -1;
		for(int i = 0; i<entries.size() && !found;i++)
		{
			if(entries.get(i).getNodoDestinazione() == dest && entries.get(i).getNextHop()==next)
			{
				pos = i;
				found = true;
			}
		}
		return pos;
	}

    /**
     * Ritorna il next hop di una destinazione aggiunta sia tramite informazioni statiche (Conf.xml) che attraverso
     * informazioni di routing dinamiche (Protocollo di routing)
     * @param dest - Nodo destinazine da raggingere
     * @return
     */
    public int getNextHop(int dest) {
        int res = -1;
        for(Object linea : entries)
        {
            if(dest == ((RoutingRow)linea).getNodoDestinazione())
            {
                res = ((RoutingRow)linea).getNextHop();
                break;
            }
        }
        return res;
    }

    void removeEntries() {
        this.entries.clear();
    }
    
    
    public ArrayList<Integer> getNeighbours()
    {
        ArrayList<Integer> nodes = new ArrayList<Integer>();
        for(Object linea : entries)
        {
            if(((RoutingRow)linea).getNodoDestinazione() == ((RoutingRow)linea).getNextHop())
            {
                nodes.add(((RoutingRow)linea).getNextHop());
            }
        }
        return nodes;
        
    }

    /**
     * Stampa su standard output la tabella di routing del nodo
     */
    public void printTR()
    {
       // System.out.println("\n********************STAMPA TR*******************");
        logger.info("***************stampa della tabella di routing*******************");
     logger.info("|DESTINAZIONE|NEXT HOP|COSTO|");
        for(Object entry : entries)
        {
            RoutingRow obj = (RoutingRow) entry;
           // System.out.println("|"+obj.getNodoDestinazione()+"|"+obj.getNextHop()+"|"+obj.getCosto()+"|");
            logger.info("|{}|{}|{}|",obj.getNodoDestinazione(),obj.getNextHop(),obj.getCosto());
        }
            logger.info("********************FINE STAMPA TR*******************");
    }

    /**
     * Setta il peso sulla linea della tabella di routing
     * @param desinazione : Nodo destinazione
     * @param next_hop :  Nodo per arrivare alla destinazione
     * @param new_peso : Nuovo peso da mettere sulla linea
     * @return Ritorna true se il peso da settare è diverso da quello presente sulla tabella false altrimenti
     */
    public boolean setPeso(int desinazione, int next_hop, double new_peso) {
        for(Object entry : entries)
        {
            RoutingRow obj = (RoutingRow) entry;
            if(obj.getNodoDestinazione() == desinazione && obj.getNextHop() == next_hop)
            {
                if(obj.getCosto() != new_peso)
                {
                   obj.setCosto(new_peso);
                   return true;
                }
                
            }            
        }
        return false;

    }
    
    public ArrayList<RoutingRow> getEntries()
    {
        return this.entries;

    }
}