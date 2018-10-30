package daProc;

import utils.Message;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

//this class corresponds to best effort/perfect link receiver that will deliver the message once
//this class merely receive messages and write its activity to the log file
public class FIFOReceiverThread extends Thread {

	public FIFOBroadcast process;
	final static Logger LOGGER = Logger.getLogger(FIFOBroadcast.class.getName());

	// messages that are
	int[][][] urb_ack;
	HashSet<Message> urb_pending;
	
	public FIFOReceiverThread(FIFOBroadcast process) {
		LOGGER.log(Level.INFO, "Creating instance of FIFOReceiverThread now");
		this.process = process;
	}
	
	//follows from page 83 of the book
	public void bebDeliver(Message message) {
        
        //if not already acked, urb_ack it

		if (urb_ack[message.getSender()][message.getSn()][process.getId()] != 1) {
			urb_ack[message.getSender()][message.getSn()][process.getId()] = 1;
		}
		
		//add message to pending list and bebBroadcast it
		HashSet<Message> pending = urb_pending;

		if(!pending.contains(message)) {
			pending.add(message);
			PerfectSendThread thread = new PerfectSendThread(message, process.getPeers(), process.getSocket());
			thread.start();
		}
	}
	
	//implements best effort broadcast
	public void run() {
    	
		HashSet<Message> messages_beb_delivered = new HashSet<>();
		DatagramSocket socket = process.getSocket();

        //Thread will be crashed/stopped from the main
        while (true) {
            byte[] receiveBuffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try {
                socket.receive(packet);
                // Read data from packet
                String received = new String(packet.getData());
                String[] splitted = received.split(":");

				// Get sender id
                //int sender_id = process.idFromAddress.get(packet.getAddress().toString());
                int initial_sender = Integer.parseInt(splitted[0]);
                int msg_seq = Integer.parseInt(splitted[1]);


				Message message = new Message(initial_sender, msg_seq);
                
                //perfect_link logic : deliver only if never delivered before
                if (!messages_beb_delivered.contains(message)) {
                	messages_beb_delivered.add(message);
                	bebDeliver(message);
                }

            } catch (java.io.IOException e) {
                System.out.println("Error while receiving DatagramPacket");
                e.printStackTrace();
            }
        }
    }
}
