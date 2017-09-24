/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base_simulator;

/**
 * Abstraction of Interface Adapter
 * The following parameters are set by using the configuration file
 * 
 * id   :is the Interface ID (The same ID for another interface cannot be exist)
 * ipv4 :is the IPv4 Address assigned to the interface
 * channel_idx : is the reference ID for the related channel
 * 
 * @author afsantamaria
 */


public class NetworkInterface {
    private int id;
    private String ipv4;
    private int channel_idx;
    private int dest;
    private double metrica;
    public NetworkInterface(int id, String ipv4, int dest, int channel_idx,double metrica) {
        this.id = id;
        this.ipv4 = ipv4;
        this.channel_idx = channel_idx;
        this.dest = dest;
        this.metrica = metrica;
    }

    public double getMetrica() {
        return metrica;
    }

    public void setMetrica(double metrica) {
        this.metrica = metrica;
    }
    
    public int getDest()
    {      
        return this.dest;
    }
    
    
    
    public void addDest(int _dest)
    {
//        this.dest = _dest;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public int getChannel_idx() {
        return channel_idx;
    }

    public void setChannel_idx(int channel_idx) {
        this.channel_idx = channel_idx;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NetworkInterface other = (NetworkInterface) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.channel_idx != other.channel_idx) {
            return false;
        }
        if ((this.ipv4 == null) ? (other.ipv4 != null) : !this.ipv4.equals(other.ipv4)) {
            return false;
        }
        return true;
    }
    
}
