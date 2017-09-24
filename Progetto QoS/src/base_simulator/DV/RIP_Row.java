/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base_simulator.DV;

/**
 *
 * @author franco
 */
public class RIP_Row {

    private int nodo_destinazione;
    private int next_hop;
    private int n_hop;

    public RIP_Row(int nodo_destinazione, int next_hop, int n_hop) {
        this.nodo_destinazione = nodo_destinazione;
        this.next_hop = next_hop;
        this.n_hop = n_hop;
    }

    public int getNodoDestinazione() {
        return this.nodo_destinazione;
    }

    public int getNextHop() {
        return this.next_hop;
    }

    public int getNHop() {
        return this.n_hop;
    }

    public void setNHop(int n_hop) {
        this.n_hop = n_hop;
    }
}
