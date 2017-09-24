package base_simulator;

import java.util.LinkedList;

class interfaceEntry
{
	private int interface_idx;
	private int dest_address;
	public interfaceEntry(int interface_idx, int dest_address) {
		super();
		this.interface_idx = interface_idx;
		this.dest_address = dest_address;
	}
	public int getInterface_idx() {
		return interface_idx;
	}
	public void setInterface_idx(int interface_idx) {
		this.interface_idx = interface_idx;
	}
	public int getDest_address() {
		return dest_address;
	}
	public void setDest_address(int dest_address) {
		this.dest_address = dest_address;
	}
	
	
	
}
public class InterfaceTable {
	
	private LinkedList<interfaceEntry> it;
	
	public InterfaceTable()
	{
		this.it = new LinkedList<interfaceEntry>();
	}
	
	/**
	 * 
	 * @param nodo_dest Nodo destinazione
	 * @return indice dell'interfaccia di rete
	 * 0 - Loopback
	 * 1 - Eth0
	 * 2 - Eth1
	 * 3 - Wlan0
	 * 4 - Wlan1
	 */
	public int getInterfaceIdxForAddress(int nodo_dest)
	{
		int res = -1;
		boolean found = false;
		for(int i = 0; i<it.size() && !found;i++)
		{
			interfaceEntry ie = it.get(i);
			if(ie.getDest_address() == nodo_dest)
			{
				res = ie.getInterface_idx();
				found = true;
			}
		}
		return res;
	}
}
