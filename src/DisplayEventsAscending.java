
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

/**
 *
 * @author Dominic Rosch-Grace and Robert Fedor
 * @version 10/21/2023
 * 
 * Client class handling the logic for displaying events via the "disev" command in an ascending order
 */
public class DisplayEventsAscending {
    private List<Event> events;

    public DisplayEventsAscending(HashMap<Integer, Event> eventMap) {
        this.events = new ArrayList<>(eventMap.values());
        
        //Sorting based on the deadline
        Collections.sort(events, (event1, event2) -> event1.getDeadline().compareTo(event2.getDeadline()));
    }

    public List<Event> getSortedEvents() {
        return events;
    }
}
