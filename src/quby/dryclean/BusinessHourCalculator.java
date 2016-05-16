package quby.dryclean;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.log4j.Logger;

/**
 *
 * @author mohamed
 * RQ: You must at least create a class called BusinessHourCalculator that allows one to define the
opening and closing time for each day
 */
public class BusinessHourCalculator {
    private static Logger logger = Logger.getLogger(BusinessHourCalculator.class);
    static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");
    static DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");
    static Format deadlineFormat = new SimpleDateFormat("EEE MMM dd H:mm:ss yyyy");
    static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private String openingTime;
    private String closingTime;
    
    private LocalTime openingLocalTime = null;
    private LocalTime closingLocalTime = null;
    
    private LocalTime defaultOpeningLocalTime = null;
    private LocalTime defaultClosingLocalTime = null;
    
    private  Hashtable daysOfWeekHash = new Hashtable();
    private  Hashtable specificDatesHash = new Hashtable();
    
    private Date deadlineDate = null;

     
    /* RQ: You must be able to instantiate the class using the following constructor:*/
    public BusinessHourCalculator(String defaultOpeningTime, String defaultClosingTime) {
        this.openingTime = defaultOpeningTime;
        this.closingTime = defaultClosingTime;
        setOpenCloseTime();
    }
    
    private void setOpenCloseTime()
    {
        /* RQ: The two parameters should be in a 24h time format */
        try {
            defaultOpeningLocalTime  = LocalTime.parse(openingTime, timeFormat);
        }
        catch (DateTimeParseException exc) {
            logger.error(openingTime + " is not parsable!", exc);    
        }
        try {
             defaultClosingLocalTime = LocalTime.parse(closingTime, timeFormat);
        }
        catch (DateTimeParseException exc) {
            logger.error(closingTime + " is not parsable!", exc);    
        }
    }
    
    private void setOpenCloseTime(String openingTime, String closingTime)
    {
        /* RQ: The two parameters should be in a 24h time format */
        try {
            openingLocalTime  = LocalTime.parse(openingTime, timeFormat);
        }
        catch (DateTimeParseException exc) {
            logger.error(openingTime + " is not parsable!", exc);    
        }
        try {
             closingLocalTime = LocalTime.parse(closingTime, timeFormat);
        }
        catch (DateTimeParseException exc) {
            logger.error(closingTime + " is not parsable!", exc);    
        }
    }
    
    /* RQ: The instance allows for further customization of the opening hours by specifying the day of the week or
a specific date. You should define an enum for the days of the week.*/
    public void setOpeningHours(DayOfWeek dow, String openingTime, String closingTime) {
        setOpenCloseTime(openingTime, closingTime);
        OpenCloseTimes openCloseTime = new OpenCloseTimes(openingLocalTime, closingLocalTime);
        
        logger.debug("For dow : " + dow + " openingLocalTime : " + openingLocalTime);
        daysOfWeekHash.put(dow, openCloseTime);
    }
    
    /* RQ: and for a specific date */
    public void setOpeningHours(String date, String openingTime, String closingTime) {
        setOpenCloseTime(openingTime, closingTime);
        OpenCloseTimes openCloseTime = new OpenCloseTimes(openingLocalTime, closingLocalTime);
        logger.debug("For date : " + date + " openingLocalTime : " + openingLocalTime);
        
        LocalDate localDate = LocalDate.parse(date, dateFormat);
        // Maybe the hash should hold local date only
        // rename to special days hash
        specificDatesHash.put(localDate, openCloseTime);
    }
    
    /* RQ: You must also be able to specify that the store is closed for a specific day of week or date.*/
    /* RQ: The setClosed method accepts multiple arguments.*/
    public void setClosed(DayOfWeek... args)
    {
        for(DayOfWeek d : args)
        {
            // Create an OpenCloseTime object for this day
            OpenCloseTimes openCloseTime = new OpenCloseTimes();
            openCloseTime.setClosed();
       
            // Store LocalDate in the hash table, with the openCloseObject (boolean close = true)
            daysOfWeekHash.put(d, openCloseTime);
            
            logger.debug("For dow : " + d + " Close : " + openCloseTime.close);
        }
    }
    
    public void setClosed(String... args)
    {
        for(String s : args)
        {
            // Create an OpenCloseTime object for this day, and set it to close
           OpenCloseTimes openCloseTime = new OpenCloseTimes();
           openCloseTime.setClosed();

           // The date is parsed into LocalDate object 
           LocalDate date = LocalDate.parse(s, dateFormat);
           
           // Store LocalDate in the hash table, with the openCloseObject (boolean close = true)
           specificDatesHash.put(date, openCloseTime);

           logger.debug("For date: " + s + " Close : " + openCloseTime.close);
        }
    }
    
    /* A method called calculateDeadline should determine the resulting business time given a time longerval
(in seconds) along with a starting datetime (as a string). The returned object should be an instance of
java.util.Date. */
    // private Date calculateDeadline(Duration duration, String startingDateTime)
    public Date calculateDeadline(long duration, String startingDateTime)
    {
        try{
        LocalDateTime deadlineLocalDateTime = null;
        deadlineDate =  null;
        
        LocalDateTime startingLocalDateTime =null;
        
            startingLocalDateTime = LocalDateTime.parse(startingDateTime, dateTimeFormat);
        
        DayOfWeek dow = startingLocalDateTime.getDayOfWeek();
        
        
        LocalDate initialLocalDate = startingLocalDateTime.toLocalDate();
        LocalDate localDate = initialLocalDate;
        LocalTime startingTime = startingLocalDateTime.toLocalTime();
        
        logger.debug("Local Date extracted: " + localDate.toString() + " It is : " + dow.toString());
        
        long remainingTime = duration;
        logger.debug("long: " + duration);
        
        // count is for a safe exit; in case the loop never exit
        long count = 0;

        // 1000000 days is the maximum allowed
        while(remainingTime > 0 || count > 1000000)
        {
            OpenCloseTimes oct = null;
            dow = localDate.getDayOfWeek();
            
            // First check DaysOfWeekHash
            oct = (OpenCloseTimes)daysOfWeekHash.get(dow);
            if(oct!=null)
            {
                logger.debug("Entry found in daysOfWeek Hash ; localDate: " + localDate.toString());
                if(oct.close){
                    localDate = localDate.plusDays(1); logger.debug("Closed day; next day : " + localDate);
                }            
            }
            // Now check if the date is in the specificDates Hash
            OpenCloseTimes checkoct = (OpenCloseTimes)specificDatesHash.get(localDate);
            if(checkoct!=null)
            {
                oct = checkoct;
                logger.debug("Entry found in specificDates Hash ; localDate: " + localDate.toString());
                if(oct.close){
                    localDate = localDate.plusDays(1); logger.debug("Closed day; next day : " + localDate);
                }            
            }
            
            /* * * Set Local Time * * */
            LocalTime ot = null ;
            LocalTime ct = null ;
            if(oct!=null) { 
                if(oct.close==false) {
                    // There is an OpenCloseTimes Object extracted from either speificDate or DaysOfWeek hash
                    ot = oct.openingTime;
                    ct = oct.closingTime;
                    logger.debug("There were an OpenClosingTime object from speific or DaysOfWeek hashs, Open: " 
                            + ot.toString() + " - Closeing: " + ct.toString());
                }
                else // Closed day, move to next day; restart the loop!
                    continue;              
            }
            else {
                ot = defaultOpeningLocalTime;
                ct = defaultClosingLocalTime;
                logger.debug("There were an No OpenClosingTime object from speific or DaysOfWeek hashs, " +
                        " so we set to Default Times : Open: " 
                        + ot.toString() + " - Closeing: " + ct.toString());
            }
            
            if (startingTime.isBefore(ot))
                startingTime = ot;
            if (startingTime.isAfter(ct))            {
                localDate = localDate.plusDays(1);
                continue;
            }
                    
            // set the starting time to openingTime in case we move to a new day
            if(localDate!=initialLocalDate)
            {
                if(startingTime!=null)
                {
                    logger.debug("starting time : " + ot.toString());
                    logger.debug("local date : " + localDate.toString());
                    startingTime = ot;
                    startingLocalDateTime = LocalDateTime.of(localDate, startingTime);
                    logger.debug("LocalDate has changes; i.e. task is so long and cannot be finished"+
                            " In the inital date; So startingLocalDateTime has to be adjusted");
                }
            }
            
            
            LocalDateTime sdt = LocalDateTime.of(localDate, startingTime);
            LocalDateTime cdt = LocalDateTime.of(localDate, ct);
            
            // int is enough to hold time of around 68 years in seconds; If the requirement is more than 68 years, 
            // we should use long datatype.
            long businessDayHours = (long)Duration.between(sdt, cdt).getSeconds();
            logger.debug("Getting businessDay Hours: " + businessDayHours + " for date: " +localDate);
                    
            if(remainingTime <= businessDayHours)
            {
                logger.debug("Remaining Time : " + remainingTime + " is less than businessDayHours : " + businessDayHours);
                logger.debug("Task can be done in the same day");
                deadlineLocalDateTime = startingLocalDateTime.plusSeconds((long)remainingTime);                
                logger.debug("Deadline LocalDateTime : " + deadlineLocalDateTime.toString());
                deadlineDate = Date.from(deadlineLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
                
                logger.debug("Deadline Date: " + deadlineFormat.format(deadlineDate));
                logger.debug("Deadline Date: " + deadlineDate.toString());
            
                return deadlineDate;
            }
            else { // Task cannot be done in same day localDate is incremented
                localDate = localDate.plusDays(1);
                remainingTime = remainingTime - businessDayHours;
                logger.debug("We still have Remaining Time " + remainingTime + " for next day:"+
                        localDate.toString());
            }
            count++;
        } // End of While remaining time > 0
        
        } 
        catch(Exception x){logger.debug("couldn't parse", x);}
        // in case count is more than 100000; The deadline returns null
        logger.debug("Some thing is wrong, maybe remainingTime is less than 0");
        return null;
    }
    
    public Date getDeadline()
    {
        return deadlineDate;
    }
    
    public String getFormattedDeadline()
    {
        return deadlineFormat.format(deadlineDate);        
    }
    
    public String toString()
    {
        String s = "";
        if (deadlineDate == null)
            s+= "Couldn't return a Deadline";
        else {
            s += "Deadline Computed : " + deadlineFormat.format(deadlineDate);        
        }
        return s;
    }
}
