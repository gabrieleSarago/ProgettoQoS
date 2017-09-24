/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author franco
 */
package base_simulator;
import java.util.*;

public class Link {
    protected ArrayList<Double> pesi;
    public Link() {
        pesi = new ArrayList<Double>();
    }

    public Link(ArrayList<Double> pesi) {
        for(int i = 0;i<pesi.size();i++)
            this.pesi.add(pesi.get(i));
    }
    
    public void addPeso(double peso)
    {
        pesi.add(peso);
    }
    
    public double getPeso(int idx)
    {
        return this.pesi.get(idx);
    }

    public void setPesi(ArrayList<Double> pesi) {
        this.pesi = new ArrayList<Double>();
        for(int i = 0;i<pesi.size();i++)
            this.pesi.add(pesi.get(i));
    }

    public ArrayList<Double> getPesi() {
        return pesi;
    }
    
    
}
