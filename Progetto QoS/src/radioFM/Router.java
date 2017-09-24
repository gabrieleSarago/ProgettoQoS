package radioFM;

import java.util.HashMap;

public class Router {
	
	private String id;
	//associazione id mobile_host - nome stazione radio scelta
	/*
	 * Non si aggiungono le frequenze radio poichè basta una stringa attestante il nome della stazione
	 * TODO Dopodichè la frequenza verrà inviata dal router al mobile host
	 */
	//TODO: creare una struttura dati per i mobile host inattivi => paging cache
	protected HashMap<Integer, String> routes;
	
	public Router(String id) {
		this.id = id;
		routes = new HashMap<>();
	}
	
	public String getId() {
		return id;
	}
	
	public HashMap<Integer, String> getRoutes(){
		return routes;
	}
	
	public void addMobileHost(int id_mh, String stazione) {
		routes.put(id_mh, stazione);
	}
	
	public void removeMobileHost(int id_mh) {
		routes.remove(id_mh);
	}
}
