/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reti_tlc_gruppo_0;

import base_simulator.Grafo;
import base_simulator.RoutingRow;
import base_simulator.tabellaRouting;
import java.util.ArrayList;

/**
 *
 * @author franco
 */
class LSA_STORY {

    int lsa_source;
    int lsa_seq_no;
    double age;

    public LSA_STORY(int lsa_source, int lsa_seq_no, double age) {
        this.lsa_source = lsa_source;
        this.lsa_seq_no = lsa_seq_no;
        this.age = age;
    }

}

public class LSDB {

    /**
     * Questa struttura sarà valida solo nel periodo di collezione alla fine
     * sarà svuotata
     */
    ArrayList<Grafo> collection;
    ArrayList<LSA_STORY> lsdb;

    public LSDB() {
        collection = new ArrayList<Grafo>();
        lsdb = new ArrayList<LSA_STORY>();
    }

    /**
     * Creo un entry per lsdb che utilizzo per effettuare il controllo sui LSA packet entranti
     * @param source Nodo che ha generato il pacchetto LSA
     * @param seq_no Numero di sequenza sul nodo sorgente del pacchetto LSA
     * @param age Eta del pacchetto
     */
    public void lsdb_add_packet(int source, int seq_no, double age) {
        LSA_STORY lsdb_entry = new LSA_STORY(source, seq_no, age);
        lsdb.add(lsdb_entry);

    }

    /**
     * Metodo che mi permette di effettuare la join tra il LSDB e il grafo
     *
     * @param g Topologia del nodo che chiama il metodo
     * @return false se non ci sono modifiche al grafo true altrimenti
     * 
     * TODO:QUA VA FATTO AGGIORNAMENTO SOLO SU BASE TEMPORALE AGE DEI LINK
     */
    public boolean getLSDB(Grafo g) {
        boolean res = false;
        for (Object obj : collection) {
            Grafo topologia = (Grafo) obj;
            for (int i = 0; i < topologia.getN(); i++) {
                for (int j = 0; j < topologia.getN(); j++) {
                    if (topologia.getCosto(j, j)>=0.0){
                        res |= g.setCosto(i, j, topologia.getCosto(i, j),topologia.getAged(i,j));
                    }
                }
            }
        }

        collection.clear();
        return res;
    }

    public boolean checkLsaPresence(int sorgente, int seq_no) {
        boolean res = false;
        for(Object o : lsdb)
        {
            if (((LSA_STORY)o).lsa_source == sorgente && 
                ((LSA_STORY)o).lsa_seq_no == seq_no     )    {
                return true;
            }
        }
        return false;
    }

}
