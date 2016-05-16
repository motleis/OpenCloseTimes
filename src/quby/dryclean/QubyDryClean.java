/*
* A Java program that will determine the guaranteed time given a business hour
* schedule. 
* You must at least create a class called BusinessHourCalculator that allows one to define the
* opening and closing time for each day. 
* By default the store is in business 7 days a week. 
* Assuming no additional requirements such as time zones and daylight savings
 */

package quby.dryclean;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * @author Mohamed Tleis;
 * Date: January 20,2016.
 */

public class QubyDryClean {
    private static QubyDryClean qdc;
    URL url = getClass().getClassLoader().getResource("log4j.xml"); 
    private static Logger logger = Logger.getLogger(QubyDryClean.class);
    
    public QubyDryClean() {
    }
    
    // Driver to test the program
    public static void main(String[] args) {
        qdc = new QubyDryClean();
        qdc.prepareLogging();       
        
        BusinessHourCalculator businessHourCalculator = new BusinessHourCalculator("09:00", "15:00");
        businessHourCalculator.setOpeningHours(DayOfWeek.FRIDAY, "10:00", "17:00");
        businessHourCalculator.setOpeningHours("2010-12-24", "8:00", "13:00");
        businessHourCalculator.setClosed(DayOfWeek.SUNDAY, DayOfWeek.WEDNESDAY);
        businessHourCalculator.setClosed("2010-12-25"); 
        
        // example #1 
        businessHourCalculator.calculateDeadline(2*60*60, "2010-06-07 09:10"); 
        System.out.println("Example 1: => " + businessHourCalculator.getDeadline());
        // => Mon Jun 07 11:10:00 2010 
        // example #2 
        businessHourCalculator.calculateDeadline(15*60, "2010-06-08 14:48"); 
        System.out.println("Example 2: => " + businessHourCalculator.getDeadline());
        // => Thu Jun 10 09:03:00 2010 
        
        // example #3 
        businessHourCalculator.calculateDeadline(7*60*60, "2010-12-24 06:45"); 
        System.out.println("Example 3: => " + businessHourCalculator.getDeadline());
        // => Mon Dec 27 11:00:00 2010 
        
        // example 4
        businessHourCalculator.calculateDeadline(7*60*60, "2010-12-24 14:45"); 
        System.out.println("Example 4: => " + businessHourCalculator.getDeadline());
        
        // example 5
        int a = 60;
        int b = 60;
        long c = 1000000;
        long d = a*b*c;
        businessHourCalculator.calculateDeadline(d, "2010-12-27 24:00"); 
        System.out.println("Example 5: => " + businessHourCalculator.getDeadline());
        
        // example 6: Wrong Time Format
        businessHourCalculator.calculateDeadline(2*60*60, "2010-12-27 25:00"); 
        System.out.println("Example 6: => " + businessHourCalculator.getDeadline());
        
        // example 7: Wrong Date Format
        businessHourCalculator.calculateDeadline(2*60*60, "2010-13-27 24:00"); 
        System.out.println("Example 7: => " + businessHourCalculator.getDeadline());
        
//      example 8: all days closed // takes a while and returns null
//      businessHourCalculator.setClosed(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY, 
//                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
//                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
//        businessHourCalculator.calculateDeadline(2*60*60, "2010-12-27 24:00"); 
//        System.out.println("Example 8: => " + businessHourCalculator.getDeadline());
    }
  
    // Prepare logging file
    private static void prepareLogging()
    {
        try
            {
                URL u = qdc.url;
                DOMConfigurator.configure(u);
                logger.debug("Prepared Logger file");
            }
            catch(Exception x){
                logger.error("Prepare Logging error", x);}
    }
    
}
