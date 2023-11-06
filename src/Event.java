
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


/**
 * 
 * @authors Dominic Rosch-Grace and Robert Fedor
 * @version 10/17/2023
 * 
 * Class containing logic for an event instance
 */
public class Event {
    
    //Instance variables
    String event_name;
    double target_amount;
    Date deadline;
    
    //Date format
    static SimpleDateFormat form = new SimpleDateFormat("MM/dd/yyyy");
    
    //Event constructor
    public Event(String event_name, double target_amount, Date deadline) {
        this.event_name = event_name;
        this.target_amount = target_amount;
        this.deadline = deadline;
    }
    
    //Setters
    
    /**
     * 
     * @param event_name 
     */
    public void setEventName(String event_name) {
        this.event_name = event_name;
    }
    
    /**
     * 
     * @param target_amount 
     */
    public void setTargetAmount(double target_amount) {
        this.target_amount = target_amount;
    }
    
    /**
     * 
     * @param deadline 
     */
    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }
    
    //Getters
    
    /**
     * 
     * @return event_name
     */
    public String getEventName() {
        return event_name;
    }
    
    /**
     * 
     * @return target_amount
     */
    public double getTargetAmount() {
        return target_amount;
    }
    
    /**
     * 
     * @return deadline
     */
    public Date getDeadline() {
        return deadline;
    }
    
    /**
     * 
     * @return String representation of event
     */
    public String toString() {
        String output = "";
        output = this.event_name + "-" + this.target_amount + "-" + form.format(this.deadline);
        return output;
    }
    /**
     * 
     * @param eventStructure
     * @param arg_name
     * @return boolean indicating whether name is available for addition
     */
    public boolean nameAvailable(HashMap<Integer, Event> eventStructure, String arg_name) {

        for (Event event : eventStructure.values()) {
            if (arg_name.equals(event.getEventName())) {
                return false;
            }
        }
        return true;
    } 
}

    

