
package quby.dryclean;

import java.time.DayOfWeek;
import java.time.LocalTime;


/**
 *
 * @author Mohamed Tleis
 */
public class OpenCloseTimes {
    protected LocalTime openingTime;
    protected LocalTime closingTime;
    protected Boolean close = false;
    
    // empty constructor
    public OpenCloseTimes()
    {
        
    }
    
    public OpenCloseTimes(LocalTime open, LocalTime close) {
        this.openingTime = open;
        this.closingTime = close;
    }
    
    public void setClosed() {
        close = true;     
    }
    
    public String toString() {
        String s ="" ;
        if(close==true)
            s+="Closed Day!";
        else {
            s+="Opening: " + openingTime.format(BusinessHourCalculator.timeFormat);
            s+=" - Closing: " + closingTime.format(BusinessHourCalculator.timeFormat);
        }
        return s;
    }
}
