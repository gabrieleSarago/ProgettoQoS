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

public class UpperLevelRouter extends Router {
	
	private LinkedList<Router> downlink_routers;
	
	public UpperLevelRouter(scheduler s,String id, int capacita, MobilityMap m) {
		super(s, id, capacita, m);
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
				if(mh.eAttivo() && diff >= ROUTE_TIMEOUT) {
					//elimina la route dalle strutture dati
					removable.add(e.getKey());
					//notifica il mobile host di riattestarsi
					mh.notificaRiattesta();
				}
				//ttl scaduto e non e attivo il mobile host
				if(!(mh.eAttivo()) && diff >= PAGING_TIMEOUT) {
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
			/*
			 * Periodo di refresh di 9 secondi
			 */
			m.shifta(ROUTE_TIMEOUT);
			m.setDestinazione((Router)this);
            m.setSorgente((Router)this);
            s.insertMessage(m);
		}

	}
	
	public synchronized void addRouters(LinkedList<Router> down) {
		downlink_routers = down;
		/*
		 * vengono salvati i record dei router downlink
		 * in modo da stabilire la connessione
		 */
		for(Router r : downlink_routers) {
			cache.putAll(r.getCache());
			ttl.putAll(r.getTtl());
		}
	}
}
