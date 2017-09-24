/**
 * jNET - SIM
 * V-0.1 - BETA RELEASE
 *
 * Ing. Amilcare Francesco Santamaria, Ph.D, 2016
 * Last rev. 23/01/2016
 *
 * All right reserved - This code can be used only for teaching activities!
 * Using of this full code or portion of it is not allowed for any other scope
 */
package base_simulator.DV;

public class DecisionerDV {
    private boolean RIP_TABLE=false;
    private RIP_Table ript;
    private int myId;
    public DistanceTable dt;
    private int default_gateway = 0;
    int max_hop;

    /**
     * Cotruttore della classe Decisioner : Questa classe si preoccupa di
     * popolare le tabelle di routing del nodo
     *
     * @param ripTable - tabelle di instradamento condivisa con il network layer
     * @param id - id del nodo che possiede il network layer che istanzia questo
     * decisioner
     * @param distanceTable - tabella delle distanze
     */
    public DecisionerDV(RIP_Table ripTable, int id, DistanceTable distanceTable) {
        super();
        this.ript = ripTable;
        this.dt = distanceTable;
        myId = id;
    }
    public void enableRipTable(){
        RIP_TABLE=true;
        max_hop=15;
    }
    /**
     *
     * @return RIP_Table - Ritorna la tabella di RIP
     */
    
    public RIP_Table getRTable() {
        return ript;
    }

    public DistanceTable getDT() {
        return dt;
    }

    public int getDefault_gateway() {
        return default_gateway;
    }

    public int getNextHop(int dest) {
        int next_hop = dt.getNextHop(dest);
        if (next_hop < 0) {
            next_hop = default_gateway;
        }
        return next_hop;
    }

    public void setDefault_gateway(int default_gateway) {
        this.default_gateway = default_gateway;
    }

    public void setRTable(RIP_Table ript) {
        this.ript = ript;
    }

    public void setDT(DistanceTable dt) {
        this.dt = dt;
    }

    /**
     * Aggiunge una entry alla tabella RIP
     *
     * @param dest
     * @param next_hop
     * @param num_hop
     */
    public void addRIPEntry(int dest, int next_hop, int num_hop) {
        ript.addEntry(dest, next_hop, num_hop);
    }

    public void addDistanceTableEntry(int dest, int next, double costo) {
        dt.addEntry(dest, next, costo);
    }

    public void updateDistanceTable(DistanceTable m, int nodo_id) {
        executeBellmanFordAlgorithm(m, nodo_id);
    }

    private void executeBellmanFordAlgorithm(DistanceTable distance, int nodo_id) {
        for (DistanceRow entry : distance.getEntries()) {
            if (entry.getNodoDestinazione() != myId&& dt.controllaPresenzaLinea(nodo_id, nodo_id)!=-1) {
                double newPeso = entry.getCosto() + this.dt.getPeso(nodo_id);
                boolean presente = false;
                for (DistanceRow en : dt.getEntries()) {
                    if (en.getNodoDestinazione() == entry.getNodoDestinazione()) {
                        presente = true;
                    }
                }
                if(RIP_TABLE){
                    if (!presente){
                        if(newPeso<=max_hop){
                            dt.addEntry(entry.getNodoDestinazione(), nodo_id, newPeso);
                        }
                        else{ 
                            dt.addEntry(entry.getNodoDestinazione(), nodo_id,max_hop+1);
                        }
                    }
                    else{
                        if(dt.getPeso(entry.getNodoDestinazione()) > newPeso && newPeso>max_hop){
                            dt.setPeso(entry.getNodoDestinazione(), nodo_id, max_hop+1);
                        }
                        else if(dt.getPeso(entry.getNodoDestinazione()) > newPeso){
                            dt.setPeso(entry.getNodoDestinazione(), nodo_id, newPeso);
                        }
                    }
                }
                else {
                    if(!presente){
                        dt.addEntry(entry.getNodoDestinazione(), nodo_id, newPeso);
                    }
                    else if (dt.getPeso(entry.getNodoDestinazione()) > newPeso ) {
                        dt.setPeso(entry.getNodoDestinazione(), nodo_id, newPeso);
                }
            }

        }
    }
    }
    public void update() {
        ript.removeEntries();
        for (DistanceRow entry : dt.getEntries()) {
            ript.addEntry(entry.getNodoDestinazione(), entry.getNextHop(), (int) entry.getCosto());
        }
    }
   
}
