/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;


import base_simulator.Nodo;
import base_simulator.layers.TransportLayer;
import filter.ClassFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author seph
 */
public class LoggerTester {
    
    public static void main (String [] args){
        
        final Logger logger = LoggerFactory.getLogger(LoggerTester.class);
        
        logger.info("prova di log info ");
        logger.debug("prova di log debug");
        logger.error("qualche problema divertente");
        
        TransportLayer layer = new TransportLayer(null, 20.0);
        layer.stampaStatistiche();
        ClassFilter f = new ClassFilter();

   }
    
        
}
