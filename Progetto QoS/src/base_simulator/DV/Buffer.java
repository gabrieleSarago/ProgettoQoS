package base_simulator.DV;

import java.util.Vector;

/**************************
 * @author Ing. Amilcare-Francesco Santamaria
 **************************/

//TODO : Inserire i Buffer tra il livello link e il livello fisico

public class Buffer {
	private int idBuffer;
	private Vector<Messaggi> buffer_FCA;
	private Vector<Messaggi> buffer_CRA;
	private Vector<Messaggi> buffer_RBDC;
	//TODO : Da inserire questi dati su file di configurazione
	private double dimensione_buffer_FCA = 2*1024*1000; // in bit = 2Mb
	private double dimensione_buffer_CRA = 1*1024*1000;	//1Mb
	private double dimensione_buffer_RBDC = 1*1024*1000;	//1Mb
	
	public Buffer(int id){
		idBuffer=id;
		buffer_FCA = new Vector<Messaggi>();
		buffer_CRA = new Vector<Messaggi>();
		buffer_RBDC = new Vector<Messaggi>();
	}
	
	public int getIdBuffer(){
		return idBuffer;
	}
	
	
	public boolean insertBufferFCA(Messaggi m, double dimPacchetto){
		if(this.dimensione_buffer_FCA-dimPacchetto>0){
			this.dimensione_buffer_FCA-=dimPacchetto;
			Messaggi copia= (Messaggi) m.clone();
			buffer_FCA.addElement(copia);
			return true;
		}
		return false;
	}
	
	public boolean insertBufferCRA(Messaggi m, double dimPacchetto){
		if(this.dimensione_buffer_CRA-dimPacchetto>0){
			this.dimensione_buffer_CRA-=dimPacchetto;
			Messaggi copia= (Messaggi) m.clone();
			buffer_CRA.addElement(copia);
			return true;
		}
		return false;
	}
	
	public boolean insertBufferRBDC(Messaggi m, double dimPacchetto){
		if(this.dimensione_buffer_RBDC-dimPacchetto>0){
			this.dimensione_buffer_RBDC-=dimPacchetto;
			Messaggi copia= (Messaggi) m.clone();
			buffer_RBDC.addElement(copia);
			return true;
		}
		return false;
	}
	
	public int getDimListaFCA(){
		return this.buffer_FCA.size();
	}
	
	public int getDimListaCRA(){
		return this.buffer_CRA.size();
	}
	
	public int getDimListaRBDC(){
		return this.buffer_RBDC.size();
	}
	
	public Messaggi getMsgInTestaListaFCA(double dimPacchetto){
		Messaggi m=this.buffer_FCA.firstElement();
		this.buffer_FCA.removeElementAt(0);
		this.dimensione_buffer_FCA+=dimPacchetto;
		return m;
	}
	
	public Messaggi getMsgInTestaListaCRA(double dimPacchetto){
		Messaggi m=this.buffer_CRA.firstElement();
		this.buffer_CRA.removeElementAt(0);
		this.dimensione_buffer_CRA+=dimPacchetto;
		return m;
	}
	
	public Messaggi getMsgInTestaListaRBDC(double dimPacchetto){
		Messaggi m=this.buffer_RBDC.firstElement();
		this.buffer_RBDC.removeElementAt(0);
		this.dimensione_buffer_RBDC+=dimPacchetto;
		return m;
	}
	
	public boolean esisteMsgNeiBuffer(int idSessione){
		boolean trovato=false;
		if(this.buffer_CRA.size()==0 && this.buffer_FCA.size()==0 && this.buffer_RBDC.size()==0)
			return trovato;
		for(int i=0; i<this.buffer_FCA.size() && !trovato; i++)
			if(this.buffer_FCA.elementAt(i).getSessione()==idSessione)
				trovato=true;
		for(int i=0; i<this.buffer_CRA.size() && !trovato; i++)
			if(this.buffer_CRA.elementAt(i).getSessione()==idSessione)
				trovato=true;
		for(int i=0; i<this.buffer_RBDC.size() && !trovato; i++)
			if(this.buffer_RBDC.elementAt(i).getSessione()==idSessione)
				trovato=true;
		return trovato;
	}
	
}
