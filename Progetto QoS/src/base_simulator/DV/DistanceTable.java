package base_simulator.DV;

import java.util.ArrayList;

public class DistanceTable implements Cloneable {

    private ArrayList<DistanceRow> entries;

    public DistanceTable(ArrayList<DistanceRow> entries) {
        this.entries = entries;
    }

    public DistanceTable() {
        entries = new ArrayList<DistanceRow>();
    }

    /**
     *
     * @param nodo_destinazione Nodo-Id - Identificativo del nodo destinazione
     * (Univoco)
     * @param next_hop Next-Hop - Identificativo del prossimo nodo per
     * raggiungere dest
     * @param costo - Costo per raggiungere il nodo_destinazione
     * @brief Aggiunge un entry alla tabella di routing
     */
    public void addEntry(int nodo_destinazione, int next_hop, double costo) {
        DistanceRow entry = new DistanceRow(nodo_destinazione, next_hop, costo);

        int pos = controllaPresenzaLinea(nodo_destinazione, next_hop);

        if (pos == -1) {
            entries.add(entry);
        } else if ((entries.get(pos)).getCosto() != costo) {
            entries.get(pos).setCosto(costo);
        }
    }

    /**
     *
     * @param dest : Nodo-Id - Identificativo del nodo destinazione (Univoco)
     * @param next : Next-Hop - Identificativo del prossimo nodo per raggiungere
     * dest
     * @return ritorna la posizione (Linea) nella tabella di routing se presente
     * altrimenti -1
     */
    public int controllaPresenzaLinea(int dest, int next) {
        boolean found = false;
        int pos = -1;
        for (int i = 0; i < entries.size() && !found; i++) {
            if (entries.get(i).getNodoDestinazione() == dest && entries.get(i).getNextHop() == next) {
                pos = i;
                found = true;
            }
        }
        return pos;
    }

    /**
     * Ritorna il next hop di una destinazione aggiunta sia tramite informazioni
     * statiche (Conf.xml) che attraverso informazioni di routing dinamiche
     * (Protocollo di routing)
     *
     * @param dest - Nodo destinazine da raggingere
     * @return
     */
    public int getNextHop(int dest) {
        int res = -1;
        for (Object linea : entries) {
            if (dest == ((DistanceRow) linea).getNodoDestinazione()) {
                res = ((DistanceRow) linea).getNextHop();
                break;
            }
        }
        return res;
    }

    void removeEntries() {
        this.entries.clear();
    }

    public ArrayList<Integer> getNeighbours() {
        ArrayList<Integer> nodes = new ArrayList<Integer>();
        for (Object linea : entries) {
            if (((DistanceRow) linea).getNodoDestinazione() == ((DistanceRow) linea).getNextHop()) {
                nodes.add(((DistanceRow) linea).getNextHop());
            }
        }
        return nodes;

    }

    @Override
    public DistanceTable clone() throws CloneNotSupportedException {
        ArrayList<DistanceRow> old = new ArrayList<DistanceRow>();
        old.addAll(entries);
        DistanceTable oldDT = new DistanceTable(old);
        return oldDT;
    }

    /**
     * Stampa su standard output la tabella di routing del nodo
     */
    public void printDT() {
        System.out.println("\n********************STAMPA DT*******************");
        System.out.println("|DESTINAZIONE|NEXT HOP|COSTO|");
        for (Object entry : entries) {
            DistanceRow obj = (DistanceRow) entry;
            System.out.println("|" + obj.getNodoDestinazione() + "|" + obj.getNextHop() + "|" + obj.getCosto() + "|");
        }
        System.out.println("********************FINE STAMPA DT*******************");
    }

    /**
     * Setta il peso sulla linea della tabella di routing e cambia il next hop
     *
     * @param destinazione : Nodo destinazione
     * @param next_hop : Nodo per arrivare alla destinazione
     * @param new_peso : Nuovo peso da mettere sulla linea
     * @return Ritorna true se il peso da settare è diverso da quello presente
     * sulla tabella false altrimenti
     */
    public boolean setPeso(int destinazione, int next_hop, double new_peso) {
        for (Object entry : entries) {
            DistanceRow obj = (DistanceRow) entry;
            if (obj.getNodoDestinazione() == destinazione) {
                entries.remove(obj);
                DistanceRow dr = new DistanceRow(destinazione, next_hop, new_peso);
                entries.add(dr);
                return true;
            }
        }
        return false;
        

    }

    public double getPeso(int destinazione) {
        double peso = -1;
        for (Object entry : entries) {
            DistanceRow obj = (DistanceRow) entry;
            if (obj.getNodoDestinazione() == destinazione) {
                peso = obj.getCosto();
            }
        }
        return peso;

    }

    public ArrayList<DistanceRow> getEntries() {
        return this.entries;

    }
    public boolean controlloHop(int hop){
         for (Object entry : entries) {
            DistanceRow obj = (DistanceRow) entry;
            if (obj.getNextHop() == hop) {
                entries.remove(obj);
                return true;
                
            }
        }
         return false;
    }
}
