package radioFM;

import java.util.LinkedList;

/**
 * Classe che rappresenta si ai router di 1° livello che il gateway router
 * @author Gabriele N. Saragò
 *
 */

public class UpperLevelRouter extends Router {
	
	LinkedList<Router> routers;
	
	public UpperLevelRouter(String id) {
		super(id);
		routers = new LinkedList<>();
	}
	
	public void addRouter(Router r) {
		routers.add(r);
	}

}
