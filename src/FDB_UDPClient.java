
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 *
 * @author Dominic Rosch-Grace and Robert Fedor
 * @version 11/2/2023
 * 
 * Client class to read user input and communicate with server via UDP
 */
public class FDB_UDPClient {
    
    public static void main(String[] args) throws IOException {
        String sentence = "";
	String serverResponse;
        
        //Greet user
        System.out.println("Welcome to FundDisBuziness! Now with UDP?!?!?!!?!");
        
        //Explain each command
        System.out.println("The following are the different commands you may run:\n");

        //newev info
        System.out.println("newev - Creates a new event. This command requires an event name, amount to raise, and deadline.");
        System.out.println("Each provided parameter should be seperated by a '-' symbol. Target amount values should be of the format: '1000.00' and date values should be of the format 'MM/DD/YYYY'.");
        System.out.println("Example: newev - New Fundraising Event Name - 1000.00 - 09/22/2054.\n");

	//disev info
        System.out.println("disev - Displays every past or current event instance.");
        System.out.println("This command can be ran by simply entering 'disev'.\n");
        
        //funev info
        System.out.println("funev - Funds an existing event instance. This command requires the event name as well as the amount to be donated. The formatting rules of the newev parameters apply to this command, as well.");
        System.out.println("Example: funev - Existing Fundraising Event Name - 2500.00\n");
        
        //evinfo info
        System.out.println("evinfo - Displays the target amount, remaining amount, and deadline of an existing event.");
        System.out.println("This command requires an event name to be passed as a parameter.");
        System.out.println("Example: evinfo - Existing Fundraising Event Name.\n");
        
        //exit info
        System.out.println("Exit - enter 'exit' at any point to terminate your session.\n");
        
        //Process user input so long as they don't enter 'exit'
        while (!sentence.equals("exit")) {
            
            //Read user input
            BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
            
            //Datagram socket
            DatagramSocket cliSocket = new DatagramSocket();
            
            //Store IP
            InetAddress IP = InetAddress.getByName("localhost");
            
            //Send and receive byte arrays
            byte[] send = new byte[1024];
            byte[] receive = new byte[1024];
            
            String userInput =  read.readLine();
            send = userInput.getBytes();
            
            DatagramPacket sendPacket = new DatagramPacket(send, send.length, IP, 6789);
            
            cliSocket.send(sendPacket);
            
            DatagramPacket receivePacket = new DatagramPacket(receive, receive.length);
            
            cliSocket.receive(receivePacket);
            
            String updatedText = new String(receivePacket.getData());
            
            System.out.println("Server says: " + updatedText);
        }
    }
}


