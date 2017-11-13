package radioFM;

import java.util.LinkedList;
import java.util.Map.Entry;

import Mobility.MobilityMap;
import base_simulator.Messaggi;
import base_simulator.scheduler;

/**
 * Classe che rappresenta si i router di 1� livello che il gateway router
 * @author Gabriele N. Sarag�
 *
 */

public class UpperLevelMobilityAgent extends MobilityAgent {
	
	private LinkedList<MobilityAgent> downlink_routers;
	
	public UpperLevelMobilityAgent(scheduler s,String id, int capacita, double pPaging, MobilityMap m) {
		super(s, id, capacita, pPaging, m);
	}
	
	public void Handler(Messaggi m){
		if (m.getTipo_Messaggio().equals(REFRESH)) {
			double now = s.orologio.getCurrent_Time();
			//TODO errore iteratore!
			LinkedList<Integer> removable = new LinkedList<>();
			for(Entry<Integer,Double> e : ttl.entrySet()) {
				double diff = now - e.getValue();
				MobileHost mh = map.mobHost.get(e.getKey());
				//se il ttl e scaduto
				if(mh.eAttivo() && diff >= routeTimeout) {
					//elimina la route dalle strutture dati
					removable.add(e.getKey());
					//notifica il mobile host di riattestarsi
					mh.notificaRiattesta();
				}
				//ttl scaduto e non e attivo il mobile host
				if(!(mh.eAttivo()) && diff >= pagingTimeout) {
					//elimina la route dalle strutture dati
					removable.add(e.getKey());
					//notifica il mobile host di riattestarsi
					mh.notificaRiattesta();
				}
			}
			for(int id : removable){
				cache.remove(id);
				ttl.remove(id);
			}
			removable.clear();
			removable = null;
			
			m.shifta(routeTimeout);
			m.setDestinazione((MobilityAgent)this);
            m.setSorgente((MobilityAgent)this);
            s.insertMessage(m);
		}

	}
	
	public synchronized void addMobilityAgents(LinkedList<MobilityAgent> down) {
		downlink_routers = down;
		/*
		 * vengono salvati i record dei router downlink
		 * in modo da stabilire la connessione
		 */
		for(MobilityAgent r : downlink_routers) {
			cache.putAll(r.getCache());
			ttl.putAll(r.getTtl());
		}
	}
}
