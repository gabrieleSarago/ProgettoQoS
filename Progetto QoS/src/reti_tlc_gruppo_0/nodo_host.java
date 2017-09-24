/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reti_tlc_gruppo_0;

import base_simulator.NetworkInterface;
import base_simulator.Infos;
import base_simulator.Applicazione;
import base_simulator.Grafo;
import base_simulator.Messaggi;
import base_simulator.Nodo;
import base_simulator.canale;
import base_simulator.layers.LinkLayer;
import base_simulator.layers.NetworkLayer;
import base_simulator.layers.TransportLayer;
import base_simulator.layers.physicalLayer;
import base_simulator.scheduler;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author afsantamaria
 */
public class nodo_host extends Nodo {

    
    private int gatewayId;
    
    private int current_seq_no = 0;

    protected Infos info = null;

    public Infos getInfo() {
        return info;
    }

    @Override
    public void setInfo(Infos info) {
        this.info = info;
    }
    private ArrayList<NetworkInterface> nics = new ArrayList<NetworkInterface>();

    public nodo_host(scheduler s, int id_nodo,
            physicalLayer myPhyLayer, LinkLayer myLinkLayer, NetworkLayer myNetLayer, TransportLayer myTransportLayer,
            Grafo network, String tipo, int gtw) {
        super(s, id_nodo, myPhyLayer, myLinkLayer, myNetLayer, myTransportLayer, network, tipo);

        gatewayId = gtw;

        Messaggi m = new Messaggi("inizializza_attivita_nodo", this, this, this, 0.0);
        m.shifta(0);

        s.insertMessage(m);
    }

    /**
     * Invio il messaggio al canale prendendo quello corretto dalle informazioni delle NICs
     * Configurate nel file di configurazione
     * 
     * @param m 
     */
    private void inviaMessaggioACanale(Messaggi m) {
//TODO: Si può aggiungere la possibilità di aggiungere nuove informazioni a runtime        
        int channel_id = 0;
        NetworkInterface i = null;
        for(Object interface_element : nics)
        {
             i = (NetworkInterface)interface_element;
            if(i.getDest() == m.getNextHop_id())
            {
                channel_id = i.getChannel_idx();
                break;
            }                         
        }
        
         
        canale current_channel = info.getCanale(channel_id);

        //Invio il messaggio al canale
        m.shifta(0);
        m.setSorgente(this);        
        m.setNextHop(info.getNodo(m.getNextHop_id()));
        m.setDestinazione(current_channel);
        s.insertMessage(m);
        
//        System.out.println("I : "+this.getTipo()+" con ID "+((Nodo)this).getId()+" con IP :"+i.getIpv4()+" Invia su canale "+i.getChannel_idx());
//        System.out.println("I : "+this.getTipo()+" con ID "+((Nodo)this).getId()+" Invia su canale "+current_channel.getId());
    }

    /**
     * Invia il messaggio a livello fisico
     * @param m 
     */
    private void inviaMessaggioAPhyLayer(Messaggi m) {
        m.shifta(0);
        m.setSorgente(this);
        m.setDestinazione(this.myPhyLayer);
        s.insertMessage(m);
    }

    @Override
    public void Handler(Messaggi m) {
        if (m.getTipo_Messaggio().equals("inizializza_attivita_nodo")) {
            //Controlla se ci sono applicazioni e le schedula
            this.generaEventiApplicazione();
        } else if (m.saliPilaProtocollare == true) {
            //il messaggio entra nel nodo e sarà elaborato al suo interno
            inviaMessaggioAPhyLayer(m);
        } else {
            //il messaggio dovrà essere propagato all'esterno
            inviaMessaggioACanale(m);
        }
    }

    public void addNIC(NetworkInterface nic) {
        this.nics.add(nic);
    }

    public NetworkInterface getNic(int idx) {
        return nics.get(idx);
    }

    public int getGTW() {
        return this.gatewayId;
    }

    //Il nodo genera gli eventi di Applicazione che scenderanno al livello trasporto
    public void generaEventiApplicazione() {
        for (Object app : this.apps) {

            Applicazione item = (Applicazione) app;
            gestisciApplicazione(item);

        }
    }
/**
 * This method is used by source to generate application packets, these packets will be sent
 * to the TransportLayer at first.
 * @param app 
 */
    private void gestisciApplicazione(Applicazione app) {
        
        Nodo dest = info.getNodo(app.getDest());
        double file_size = app.getSize() * 8.0 * 1000000; //Dimensione del file in Mbyte -> lo porto in bit
        double pckt_size = app.getPacket_size() * 8; //porto il valore in bit
        int numero_pckt = (int) Math.ceil(file_size / pckt_size); // Ritorna Intero superiore
        double rate = app.getRate() * 1000; //Il rate solitamente è fornito in Kbit/sec
        int packet_inter_delay = (int)((pckt_size/rate) * 1000); //riporto interdelay in ms
        String payload = app.getPayload();
        
        File file = new File(app.getFile());
        byte fileData[] = null;
        if(file.exists())
        {
            try {
                FileInputStream inStreamFile = new FileInputStream(file);
                
                fileData = new byte[(int) file.length()];
                file_size = ((double) file.length())*8.0;
                numero_pckt = (int) Math.ceil(file_size / pckt_size); // Ritorna Intero superiore
                
                inStreamFile.read(fileData);
                
                inStreamFile.close();
                
            } catch (FileNotFoundException ex) {
                Logger.getLogger(nodo_host.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(nodo_host.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
                               
        if (app.getTipo().toLowerCase().contains("source")) {
            //TODO : CONTROLLARE TIPO TRASPORTO SE TCP INIZIALIZZARE LA TRASMISSIONE CON PROTOCOLLO TCP
            String msg_info = "INIZIO GENERAZIONE PACCHETTI ("+app.getPacket_size()+"Byte) : Da Generare:"+numero_pckt;        
            super.stampaInformazione("I", super.getId(), msg_info);

            //PREPARARE IL TRASPORTO : FRAMMENTAZIONE INFORMAZIONE IN MSS
            int tempo = app.getStart();
            for(int i = 0; i< numero_pckt;i++)
            {
                Messaggi m = new Messaggi("applicazione", this, this.myTransportLayer, dest, tempo);
                current_seq_no++;
                m.ID = current_seq_no;
                m.isData = true;
                m.setNodoSorgente(this);
                m.setApplication_port(app.getPort());
                m.setSize(app.getPacket_size());
                m.setData(null);
                if(fileData != null)
                {
                    int finalOffset = (int) ((i+1)*app.getPacket_size());
                    if(finalOffset > (file_size/8))
                        finalOffset = (int) (((file_size/8)) - (i*app.getPacket_size()));  
                    else
                        finalOffset = (int)app.getPacket_size();
                    
                    byte appo[] = new byte[finalOffset];
                    System.arraycopy(fileData, (int) ((i)*app.getPacket_size()), appo, 0, finalOffset);                    
                    m.setData(appo);
                }
                m.setInterDelayPacket(packet_inter_delay);

                m.saliPilaProtocollare = false; //Il messaggio deve partire dal livello traporto e scendere nella pila
                tempo = tempo + packet_inter_delay; //tempo di lancio del prossimo paccketto in ms
                s.insertMessage(m);
            }
            

            //1. creare un messaggio specifico di close connection
            Messaggi m = new Messaggi("close connection", this, this.myTransportLayer, dest, tempo+5000);
                current_seq_no++;
                m.ID = current_seq_no;
                m.isData = true;
                m.setNodoSorgente(this);
                m.setApplication_port(app.getPort());
                m.setSize(app.getPacket_size());
                
                m.saliPilaProtocollare = false; //Il messaggio deve partire dal livello traporto e scendere nella pila
                
                s.insertMessage(m);
                
            msg_info = "FINE GENERAZIONE PACCHETTI ("+app.getPacket_size()+"Byte) : Generati:"+numero_pckt;        
            super.stampaInformazione("I", super.getId(), msg_info);

        }

    }

}
