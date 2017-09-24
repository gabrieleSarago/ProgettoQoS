/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Mobility;

/**
 *
 * @author afsantamaria
 */
public class Mh_node {
    double x;
    double y;
    int id;

    public Mh_node(double x, double y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
    
}
