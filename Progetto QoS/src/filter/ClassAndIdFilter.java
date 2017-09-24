/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filter;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author seph
 */
public class ClassAndIdFilter extends Filter {

    private boolean acceptOnMatch = false;
  
    private String className;

    @Override
    public int decide(LoggingEvent event) {
        if (this.className != null) {
            if (event.getLocationInformation().getClassName().startsWith(className)) {
               
                // this is event for specified class
                return Filter.ACCEPT;
               
            }else{
                return Filter.DENY;
            }
        }
        return Filter.NEUTRAL;

  
    }

    public boolean isAcceptOnMatch() { return acceptOnMatch; }
    public void setAcceptOnMatch(boolean acceptOnMatch) { this.acceptOnMatch = acceptOnMatch; }

   

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
}
