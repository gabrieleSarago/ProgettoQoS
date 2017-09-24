/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base_simulator;

/**
 *
 * @author afsantamaria
 */
public class Applicazione {
    
    public final String SIMPLE_APP_TCP = "simple_source_tcp";
     double rate;
     int TON; 
     int TOFF;
     int port;
     int dest;
     double size;
     String tipo;
     int start;
     double packet_size;
     String payload;
     

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
     String file;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public int getTON() {
        return TON;
    }

    public void setTON(int TON) {
        this.TON = TON;
    }

    public int getTOFF() {
        return TOFF;
    }

    public void setTOFF(int TOFF) {
        this.TOFF = TOFF;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getDest() {
        return dest;
    }

    public void setDest(int dest) {
        this.dest = dest;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public Applicazione(double rate, int TON, int TOFF, int port, int dest, double size,double pckt_size, String tipo, int start,String payload,String file) {
        this.rate = rate;
        this.TON = TON;
        this.TOFF = TOFF;
        this.port = port;
        this.dest = dest;
        this.size = size;
        this.tipo = tipo;
        this.start = start;
        this.packet_size = pckt_size;
        this.payload = payload;
        this.file = file;
    }

    public double getPacket_size() {
        return packet_size;
    }

    public void setPacket_size(double packet_size) {
        this.packet_size = packet_size;
    }
     
     
}
