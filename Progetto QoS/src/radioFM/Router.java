package radioFM;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import Mobility.MobilityMap;
import base_simulator.Messaggi;
import base_simulator.scheduler;

public class Router{
	
	private String id;
	private int capacita, capacita_paging;
	private int numConnessioni = 0;
	protected scheduler s;
	protected MobilityMap map;
	protected final String REFRESH = "check";
	//50 millisecondi
	protected final double ROUTE_TIMEOUT = 50.0;
	//500 millisecondi
	protected final double PAGING_TIMEOUT = 500.0;
	//associazione (id , posizione mobile host) - time to live
	/*
	 * TODO Aggiungere una struttura dati id - stazione radio FM.
	 * Non si aggiungono le frequenze radio poiche basta una stringa attestante il nome della stazione
	 * dopodiche la frequenza verra inviata dal router al mobile host
	*/
	
	protected HashMap<Integer,Point2D> cache;
	protected HashMap<Integer,Double> ttl;
	
	protected Router uplink;
	
	public Router(scheduler s, String id, int capacita, MobilityMap m) {
		this.s = s;
		this.id = id;
		this.capacita = capacita;
		//10% di capacita destinata al paging
		capacita_paging = (int) 0.1*capacita;
		this.map = m;
		cache = new HashMap<>();
		ttl = new HashMap<>();
	}
	
	public void Handler(Messaggi m){
		if (m.getTipo_Messaggio().equals(REFRESH)) {
			double now = s.orologio.getCurrent_Time();
			//System.out.println("periodo di refresh router = "+this.id+" ora = "+now);
			LinkedList<Integer> removable = new LinkedList<>();
			for(Entry<Integer,Double> e : ttl.entrySet()) {
				double diff = now - e.getValue();
				MobileHost mh = map.mobHost.get(e.getKey());
				//se il ttl e scaduto ed e attivo o non attivo
				if(mh.eAttivo() && diff >= ROUTE_TIMEOUT || !(mh.eAttivo()) && diff >= PAGING_TIMEOUT) {
					//aggiungi ai candidati per il refresh
					removable.add(e.getKey());
				}
			}
			for(int id : removable){
				//elimina la route dalle strutture dati
				removeMobileHost(id);
				//notifica il mobile host di riattestarsi
				MobileHost mh = map.mobHost.get(id);
				mh.notificaRiattesta();
			}
			removable.clear();
			removable = null;
			/*
			 * Periodo di refresh di 50 millisecondi
			 */
			m.shifta(ROUTE_TIMEOUT);
			m.setDestinazione((Router)this);
            m.setSorgente((Router)this);
            s.insertMessage(m);
		}

	}
	
	public HashMap<Integer,Point2D> getCache(){
		return cache;
	}
	
	public HashMap<Integer,Double> getTtl(){
		return ttl;
	}
	
	public void setUplink(Router uplink) {
		this.uplink = uplink;
	}
	
	public String getIdRouter() {
		return id;
	}
	
	public synchronized void addMobileHost(int id_mh, Point2D position) {
		if(numConnessioni < capacita) {
			numConnessioni++;
		}
		//capacita raggiunta, si eliminano le info dei mobile host inattivi per fare spazio
		else if(capacita_paging > 0){
			//id mobile host candidato a essere rimosso
			int id = -1;
			//il primo mh inattivo viene rimosso
			for(Entry<Integer,Double> e : ttl.entrySet()) {
				if(map.mobHost.get(e.getKey()).eAttivo()) {
					id = e.getKey();
					break;
				}
			}
			//Router pieno di connessioni attive, chiamata rifiutata
			if(id == -1) {
				System.out.println("capacita router superata!");
				return;
			}
			else {
				System.out.println("Router = "+this.id+" rimozione mh inattivo = "+id);
				ttl.remove(id);
				cache.remove(id);
				if(uplink != null) {
					uplink.removeMobileHost(id);
				}
				capacita_paging--;
			}
		}
		//System.out.println("Router = "+this.id+" Aggiunta mobile host = "+id_mh);
		cache.put(id_mh, position);
		ttl.put(id_mh, s.orologio.getCurrent_Time());
		if(uplink != null) {
			uplink.addMobileHost(id_mh, position);
		}
	}
	
	public synchronized void removeMobileHost(int id_mh) {
		//System.out.println("Router = "+this.id+" rimozione mh = "+id_mh);
		cache.remove(id_mh);
		ttl.remove(id_mh);
		if(uplink != null) {
			uplink.removeMobileHost(id_mh);
		}
		numConnessioni--;
	}
	
	public void start() {
        Messaggi m = new Messaggi(REFRESH, this, this, this, s.orologio.getCurrent_Time());        
        m.shifta(0);
        s.insertMessage(m);
    }
}
