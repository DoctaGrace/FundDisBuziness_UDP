import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 *
 * @author Dominic Rosch-Grace and Robert Fedor
 * @version 11/5/2023
 *
 * 
 * Client class to read user input and communicate with server via UDP
 */

public class FDB_UDPServer {
    
    
    //Event structure
    static HashMap<Integer, Event> eventStructure = new HashMap<>();
    static int eventID = 0;    //To determine event key for HashMap referencing

    //Date format
    static SimpleDateFormat form = new SimpleDateFormat("MM/dd/yyyy");

    //Store events file in file object
    static File evFile = new File("src\\events.txt");
    
    public static void main(String[] args) throws SocketException, ParseException {
        
        //Makes it so imaginary dates are not allowed
        form.setLenient(false);

        try (BufferedReader br = new BufferedReader(new FileReader(evFile))) {

            //Iterate until end of file reached
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineParts = line.split("-");
                Date formattedDate = form.parse(lineParts[2]);
                eventStructure.put(eventID++, (new Event(lineParts[0], Double.parseDouble(lineParts[1]), formattedDate)));
            }
        } catch (IOException ioex) {
            System.out.println("There was an issue moving file contents to HashMap!");
        }

        DatagramSocket serverSocket = new DatagramSocket(6789);
        System.out.println("UDP server activated!");

        while (true) {
            try {
                byte[] receiveData = new byte[1024]; // Buffer for receiving data
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                // Receive a packet (blocking call)
                serverSocket.receive(receivePacket);

                // Start a new thread for handling the packet/client
                new ClientHandler(serverSocket, receivePacket).start();
                System.out.println("New thread created for client!");
            } catch (IOException e) {
                System.err.println("IOException: " + e.getMessage());
            }
        }
    }
    
    //Client handler 
    static class ClientHandler extends Thread {
        
        private DatagramSocket socket;
        private DatagramPacket packet;

        public ClientHandler(DatagramSocket socket, DatagramPacket packet) {
            this.socket = socket;
            this.packet = packet;
        }

        //Method responsible for processing user commands
        public void run() {
            
            //Time to process data from client
            byte[] sendData;
            String clientInput = new String(packet.getData(), 0, packet.getLength());
            String processedData = "";

            //Break command into parts using "-" as delimiter
            String[] segments = clientInput.split("-");

            //Remove leading/trailing white spaces from command
            for (int i = 0; i < segments.length; i++) {
                segments[i] = segments[i].trim();
            }
            //New event
            if (segments[0].equals("newev")) {

                try {
                    //Create new event
                    Event event = new Event(segments[1], Double.parseDouble(segments[2]), form.parse(segments[3]));

                    //If name not taken, event successfully added
                    if (event.nameAvailable(eventStructure, event.getEventName())) {
                        eventStructure.put(eventID++, event);

                        //Write to file
                        try {
                            FileWriter writer = new FileWriter("src\\events.txt", true);
                            writer.append(event.toString() + '\n');
                            writer.flush();
                            writer.close();
                        } catch (FileNotFoundException ex) {
                        }

                        System.out.println("Notify client event was created.");
                        processedData = "Event created!" + '\n';
                    } else {
                        System.out.println("Error message sent to client!");
                        processedData = "Name taken! Try again\n";
                    }

                //Invalid newev syntax; prompt user to try again
                } catch (Exception ex) {
                    System.out.println("Error message sent to client!");
                    processedData = "Invalid newev command!\n";
                }
                
            //Display events
            } else if (segments[0].equals("disev")) {
                try {
                    //First, display all currently active events from most to least urgent
                    DisplayEventsAscending disevAsc = new DisplayEventsAscending(eventStructure);

                    //Output list to contain currently active events in ascending order
                    List<Event> outputListAsc = disevAsc.getSortedEvents();

                    //Iterate through each event in the output list and write them to output stream
                    StringBuilder output = new StringBuilder();
                    output.append("Currently active events: \t");
                    for (Event ev : outputListAsc) {

                        //Access current date
                        LocalDate localDate = LocalDate.now();
                        Date currentDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                        //Ensure only active events are displayed
                        if (ev.deadline.after(currentDate)) {
                            output.append(ev.event_name).append(":   ").append(ev.deadline).append(" |---|---| ");
                        }
                    }

                    //Second, display all past events from most to least recent
                    DisplayEventsDescending disevDes = new DisplayEventsDescending(eventStructure);

                    //Output list to contain currently active events in ascending order
                    List<Event> outputListDes = disevDes.getSortedEvents();

                    output.append("############## Past events: \t");
                    for (Event ev : outputListDes) {

                        //Access current date
                        LocalDate localDate = LocalDate.now();
                        Date currentDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                        //Ensure only active events are displayed
                        if (ev.deadline.before(currentDate)) {
                            output.append(ev.event_name).append(":   ").append(ev.deadline).append(" |---|---| ");
                        }
                    }

                    //Write current events to output
                    processedData = output.toString();

                } catch (Exception ex) {
                    System.out.println("Error message sent to client!");
                    processedData = "There was an issue displaying events!\n";
                }
            //Fund event
            } else if (segments[0].equals("funev")) {
                try {
                                
                    //Flag whether event was found
                    boolean eventFound = false;

                    //Check for whether passed event exists in structure
                    for (Event event : eventStructure.values()) {
                        if (segments[1].equals(event.getEventName())) {
                            eventFound = true;  //Event has been identified
                            Event fundedEvent = event;  //To easily reference identified event
                            double existingAmount = fundedEvent.getTargetAmount();
                            fundedEvent.setTargetAmount(existingAmount - Double.parseDouble(segments[2]));

                            //If event has been funded entirely
                            if (fundedEvent.getTargetAmount() <= 0) {

                                //Remove from eventStructure
                                int keyToRemove = -1;
                                for (HashMap.Entry<Integer, Event> entry : eventStructure.entrySet()) {
                                    if (entry.getValue().getEventName().equals(fundedEvent.getEventName())) {
                                        keyToRemove = entry.getKey();
                                        break;
                                    }
                                }

                                //Key identified; event removed
                                if (keyToRemove != -1) {
                                    eventStructure.remove(keyToRemove);
                                }

                                // Read existing events
                                List<String> updatedLines = new ArrayList<>();
                                try ( BufferedReader br = new BufferedReader(new FileReader("src\\events.txt"))) {
                                    String line;

                                    while ((line = br.readLine()) != null) {
                                        String[] segments2 = line.split("-");
                                        if (!segments2[0].equals(fundedEvent.getEventName())) {
                                            // Only add lines that don't match the funded event
                                            updatedLines.add(line);
                                        }
                                    }
                                } catch (IOException e) {
                                    // Handle IOException

                                }

                                //Write updated lines back to the file
                                try ( FileWriter writer = new FileWriter("src\\events.txt")) {
                                    for (String updatedLine : updatedLines) {
                                        writer.write(updatedLine + "\n");
                                    }
                                } catch (IOException e) {
                                    //Handle IOException

                                }
                                System.out.println("Notify client that goal was reached!");
                                processedData = fundedEvent.getEventName() + " goal reached!\n";
                            } 
                            //Event has been partially funded
                            else {

                                //Store modified and existing events in updatedLines to be added to text file
                                List<String> updatedLines = new ArrayList<>();
                                try ( BufferedReader br = new BufferedReader(new FileReader("src\\events.txt"))) {
                                    String line;
                                    while ((line = br.readLine()) != null) {
                                        String[] segments2 = line.split("-");
                                        if (segments2[0].equals(fundedEvent.getEventName())) {
                                            //Replace with the updated event's string representation
                                            updatedLines.add(fundedEvent.toString());
                                        } else {
                                            updatedLines.add(line);
                                        }
                                    }
                                } catch (IOException e) {
                                    //Handle IOException

                                }

                                //Write updated lines back to the file
                                try ( FileWriter writer = new FileWriter("src\\events.txt")) {
                                    for (String updatedLine : updatedLines) {
                                        writer.write(updatedLine + "\n");
                                    }
                                } catch (IOException e) {
                                }

                                //Format the target amount and notify client of successful processing
                                BigDecimal bd = new BigDecimal(fundedEvent.getTargetAmount());
                                bd = bd.setScale(2, RoundingMode.CEILING);
                                double roundedValue = bd.doubleValue();

                                processedData = "Successfully funded " + fundedEvent.getEventName() + ". Remaining target amount: " + roundedValue + "\n";
                            }
                            //Now that event target amount has been updated, we replace it in the file
                            try {

                                //Read existing events
                                List<String> updatedLines = new ArrayList<>();
                                BufferedReader br = new BufferedReader(new FileReader("src\\events.txt"));
                                String line;
                                while ((line = br.readLine()) != null) {
                                    String[] segments2 = line.split("-");
                                    if (segments2[0].equals(fundedEvent.getEventName())) {
                                        //Replace with the updated event's string representation
                                        updatedLines.add(fundedEvent.toString());
                                    } else {
                                        updatedLines.add(line);
                                    }
                                }
                                br.close();

                                // Write updated lines back to the file
                                FileWriter writer = new FileWriter("src\\events.txt");
                                for (String updatedLine : updatedLines) {
                                    writer.write(updatedLine + "\n");
                                }
                                writer.close();

                            } catch (FileNotFoundException ex) {
                            }
                            break;
                        }
                    }

                    //If event not found, notify the client
                    if (!eventFound) {
                        System.out.println("Error message sent to client!");
                        processedData = "Looks like that event doesn't exist!\n";
                    }
                } catch (Exception ex) {
                    System.out.println("Error message sent to client!");
                    processedData = "There was an issue funding the event!\n";
                }
                
            //Event info
            } else if (segments[0].equals("evinfo")){
                try {
                    
                    //Flag whether event was found
                    boolean found = false;

                    //Iterate through each event to identify the one requested by client
                    for (HashMap.Entry<Integer, Event> entry : eventStructure.entrySet()) {
                        if (entry.getValue().getEventName().equals(segments[1])) {
                            BigDecimal bd = new BigDecimal(entry.getValue().getTargetAmount());
                            bd = bd.setScale(2, RoundingMode.CEILING);
                            double roundedValue = bd.doubleValue();
                            System.out.println("Return event into to client!");
                            found = true;
                            processedData = entry.getValue().getEventName() + " deadline is " + entry.getValue().getDeadline() + " and target amount is " + roundedValue + '\n';
                            break;
                        }
                    }
                    //Event was not found
                    if (!found)
                        processedData = "Looks like that event doesn't exist!\n";

                } catch (Exception ex) {
                    System.out.println("Error message sent to client!");
                    processedData = "There was an issue processing your evinfo commadn!\n";
                }
            //User sent 'exit' command
            } else if (segments[0].equals("exit")) { 
                System.out.println("Sent farewell to client!\n");
                processedData = "Going so soon, eh? We'll leave the light on for you!\n";
            }
            //User tried entering something that can't be processed! Prompt 'em to try again
            else {
                System.out.println("Error message sent to client!");
                processedData = "Invalid command. Please try again!\n";
            }
            //Store server output in sendData, which is subsuquently sent to client
            sendData = processedData.getBytes();

            //Prepare response packet
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
            
            //Send response to client
            try {
                socket.send(sendPacket);
            } catch (IOException e) {
                System.err.println("IOException while sending packet: " + e.getMessage());
            }
        }
    }
}
