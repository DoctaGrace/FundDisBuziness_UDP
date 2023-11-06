
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

/**
 *
 * @author Dominic Rosch-Grace and Robert Fedor
 * @version 10/21/2023
 * 
 * Client class handling the logic for displaying events via the "disev" command in a descending order
 */
public class DisplayEventsDescending {
    private List<Event> events;

    public DisplayEventsDescending(HashMap<Integer, Event> eventMap) {
        this.events = new ArrayList<>(eventMap.values());
        
        //Sorting based on the deadline
        Collections.sort(events, (event1, event2) -> event2.getDeadline().compareTo(event1.getDeadline()));
    }

    public List<Event> getSortedEvents() {
        return events;
    }
}
