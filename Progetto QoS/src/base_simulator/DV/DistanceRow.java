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
public class DistanceRow {

    private int nodo_destinazione;
    private int next_hop;
    private double costo;

    public DistanceRow(int nodo_destinazione, int next_hop, double costo) {
        this.nodo_destinazione = nodo_destinazione;
        this.next_hop = next_hop;
        this.costo = costo;
    }

    public int getNodoDestinazione() {
        return this.nodo_destinazione;
    }

    public int getNextHop() {
        return this.next_hop;
    }

    public double getCosto() {
        return this.costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }
}
