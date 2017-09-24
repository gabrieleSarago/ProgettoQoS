package base_simulator.DV;

import java.util.ArrayList;

public class RIP_Table {

    private ArrayList<RIP_Row> entries;

    public RIP_Table() {
        entries = new ArrayList<RIP_Row>();
    }

    /**
     *
     * @param nodo_destinazione Nodo-Id - Identificativo del nodo destinazione
     * (Univoco)
     * @param next_hop Next-Hop - Identificativo del prossimo nodo per
     * raggiungere dest
     * @param n_hop - Numero hop per raggiungere il nodo_destinazione
     * @brief Aggiunge un entry alla tabella di routing
     */
    public void addEntry(int nodo_destinazione, int next_hop, int n_hop) {
        RIP_Row entry = new RIP_Row(nodo_destinazione, next_hop, n_hop);

        int pos = controllaPresenzaLinea(nodo_destinazione, next_hop);

        if (pos == -1) {
            entries.add(entry);
        } else if ((entries.get(pos)).getNHop() != n_hop) {
            entries.get(pos).setNHop(n_hop);
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
            if (dest == ((RIP_Row) linea).getNodoDestinazione()) {
                res = ((RIP_Row) linea).getNextHop();
                break;
            }
        }
        return res;
    }

    void removeEntries() {
        this.entries.clear();
    }

    /**
     * Ritorna la lista dei vicini.
     *
     * @return nodes la lista dei vicini
     */

    public ArrayList<Integer> getNeighbours() {
        ArrayList<Integer> nodes = new ArrayList<Integer>();
        for (Object linea : entries) {
            if (((RIP_Row) linea).getNodoDestinazione() == ((RIP_Row) linea).getNextHop()) {
                nodes.add(((RIP_Row) linea).getNextHop());
            }
        }
        return nodes;

    }

    /**
     * Stampa su standard output la tabella di routing del nodo
     */
    public void printRIP() {
        System.out.println("\n********************STAMPA RIP*******************");
        System.out.println("|DESTINAZIONE|NEXT HOP|NUMERO HOP|");
        for (Object entry : entries) {
            RIP_Row obj = (RIP_Row) entry;
            System.out.println("|" + obj.getNodoDestinazione() + "|" + obj.getNextHop() + "|" + obj.getNHop() + "|");
        }
        System.out.println("********************FINE STAMPA RIP*******************");
    }

    /**
     * Setta il peso sulla linea della tabella di routing
     *
     * @param destinazione : Nodo destinazione
     * @param next_hop : Nodo per arrivare alla destinazione
     * @param num_hop : Nuovo peso da mettere sulla linea
     * @return Ritorna true se il peso da settare è diverso da quello presente
     * sulla tabella false altrimenti
     */
    public boolean setNHop(int destinazione, int next_hop, int num_hop) {
        for (Object entry : entries) {
            RIP_Row obj = (RIP_Row) entry;
            if (obj.getNodoDestinazione() == destinazione && obj.getNextHop() == next_hop) {
                if (obj.getNHop() != num_hop) {
                    obj.setNHop(num_hop);
                    return true;
                }

            }
        }
        return false;

    }

    public ArrayList<RIP_Row> getEntries() {
        return this.entries;

    }
}
