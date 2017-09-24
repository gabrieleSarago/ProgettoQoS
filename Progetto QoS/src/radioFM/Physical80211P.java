package radioFM;

import java.util.Random;

import base_simulator.Messaggi;
import base_simulator.scheduler;
import base_simulator.layers.physicalLayer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author afsan_000
 */
public class Physical80211P extends physicalLayer {
//STATISTICHE
    double lostPacket = 0;
    double avg_pw_rx = 0;
    double cnt_pw_rx = 0;
    double receivedPacket = 0;
    
    final double Grx = 2; //Guadagno antenna in rx
    final double Gtx = 2; //Guadagno antenna in tx
    final double ff = 2.5; //Fading factor in URBAN AREA
    final double wave_length = 0.051; //Lunghezza d'onda alla frequenza 5.9 GHz IEEE802.11
    final double pw_tx = 20; // potenza in trasmissione dbm
    double pw_rx = 0;
    double POWER_THRESHOLD = -95; //dbm
    
    

    public Physical80211P(scheduler s, double tempo_processamento_bit) {
        super(s, tempo_processamento_bit);
    }

    @Override
    public void Handler(Messaggi m) {
        //System.out.println("\nIl messaggio è arrivato a livello fisico");
        if (m.saliPilaProtocollare) {
            m.removeHeader(header_size);
            receivedPacket++;
            double prx = getPowerRx(m);
            if ( prx > POWER_THRESHOLD) {
                if(Math.abs(prx - POWER_THRESHOLD) < 10)
                {
                   Random r = new Random();
                   
                   if(r.nextDouble() < 0.94 || 
                           !m.isData)
                   {
                       super.inviaAlinkLayer(m);               
                   }
                   else
                   {
                      lostPacket++; 
                   }
                }else{
                   super.inviaAlinkLayer(m);               
                }
            } else {                
//                System.out.println("Pacchetto perso a livello fisico del nodo "+((NodoMacchina)nodo).getId()+" PW_RX :"+pw_rx);
                lostPacket++;
            }           
        } else {
            m.addHeader(header_size);
            super.inviaAnodo(m);
            
        }
    }

    private double getDistanceBetweenNodes(MobileHost ns) {
        double res = 0;        
        MobileHost n = (MobileHost) nodo;
        res = Math.sqrt(Math.pow((ns.currX - n.currX), 2) + Math.pow((ns.currY - n.currY), 2));

        return res;
    }

    private double getPowerRx(Messaggi m) {        
        try
        {
        MobileHost ns = (MobileHost) m.getNodoSorgente();
        double distance = getDistanceBetweenNodes(ns);
        if(distance == 0)
            distance = 1;
        
        // ptx/prx ratio        
        double comp = Math.pow((4*Math.PI*distance),ff)/ ((Grx * Gtx)* Math.pow(wave_length,ff));
        double path_loss = 10*Math.log10(comp);
        pw_rx = pw_tx - path_loss;
        
        avg_pw_rx += pw_rx;
        cnt_pw_rx++;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
//        System.out.println("Livello PwTX :" +pw_tx+" PwRx:"+pw_rx);
        
        return pw_rx;
    }
    
    
    @Override
    public void stampaStatistiche()
    {
        System.out.println("STATISTICHE LIVELLO FISICO");
        System.out.println("NODO..:"+((MobileHost)nodo).getId());
        System.out.println("Pacchetti Rigettati..:"+this.lostPacket);
        System.out.println("Pacchetti Ricevuti...:"+this.receivedPacket);        
        System.out.println("Average Power Received (dBm):"+this.avg_pw_rx/this.cnt_pw_rx);
    }

}
