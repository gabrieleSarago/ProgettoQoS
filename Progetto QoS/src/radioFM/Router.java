package radioFM;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import Mobility.MobilityMap;
import base_simulator.Messaggi;
import base_simulator.scheduler;

public class Router{
	
	private String id;
	private int capacita, capacita_paging;
	private int carico = 0;
	protected scheduler s;
	protected MobilityMap map;
	protected final String REFRESH = "check";
	protected double routeUpdateTime;
	protected double pagingUpdateTime;
	
	protected HashMap<Integer,String> cache;
	protected HashMap<Integer,Double> ttl;
	
	protected Router uplink;
	
	public Router(scheduler s, String id, int capacita, double pPaging, MobilityMap m) {
		this.s = s;
		this.id = id;
		this.capacita = capacita;
		//10% di capacita destinata al paging
		capacita_paging = (int) pPaging*capacita;
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
				if(mh.eAttivo() && diff >= routeUpdateTime || !(mh.eAttivo()) && diff >= pagingUpdateTime) {
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
			m.shifta(routeUpdateTime);
			m.setDestinazione((Router)this);
            m.setSorgente((Router)this);
            s.insertMessage(m);
		}

	}
	
	public HashMap<Integer,String> getCache(){
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
	
	public void setRouteUpdateTime(double time) {
		routeUpdateTime = time;
	}
	
	public void setPagingUpdateTime(double time) {
		pagingUpdateTime = time;
	}
	
	public synchronized void addMobileHost(int id_mh, String ip) {
		if(carico < capacita) {
			MobileHost mh = map.mobHost.get(id_mh);
			carico += mh.getAvgRate();
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
				capacita_paging -= map.mobHost.get(id).getAvgRate();
			}
		}
		//System.out.println("Router = "+this.id+" Aggiunta mobile host = "+id_mh);
		cache.put(id_mh, ip);
		ttl.put(id_mh, s.orologio.getCurrent_Time());
		if(uplink != null) {
			uplink.addMobileHost(id_mh, ip);
		}
	}
	
	public synchronized void removeMobileHost(int id_mh) {
		//System.out.println("Router = "+this.id+" rimozione mh = "+id_mh);
		cache.remove(id_mh);
		ttl.remove(id_mh);
		if(uplink != null) {
			uplink.removeMobileHost(id_mh);
		}
		carico -= map.mobHost.get(id_mh).getAvgRate();
	}
	
	public void start() {
        Messaggi m = new Messaggi(REFRESH, this, this, this, s.orologio.getCurrent_Time());        
        m.shifta(0);
        s.insertMessage(m);
    }
}
