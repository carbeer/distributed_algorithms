package daProc;

import utils.Message;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

//this class corresponds to urbReceiver that will deliver the messages according the the URB logic

public class FIFOReceiverThread extends Thread {

	final static Logger LOGGER = Logger.getLogger(FIFOBroadcast.class.getName());
	public FIFOBroadcast process;
	// TODO: What happens with those pending messages? Do we need them for anything?!
	HashSet<Message> pending;
	
	public FIFOReceiverThread(FIFOBroadcast process) {
		LOGGER.log(Level.INFO, "Creating instance of FIFOReceiverThread now");
		this.process = process;
		this.pending = new HashSet<>();
	}
	
	//follows from page 83 of the book
	public void bebDeliver(Message message, int sender_id) {
		if (process.msgAck.get(message) == null) {
			process.msgAck.put(message, new ArrayList<Integer>());
		}
		
		// page 83 : If I received a message from sender_id,
		// I know sender_id received the message, and I can ack it for sender_id
		if (process.msgAck.get(message).contains(sender_id)) {
			process.msgAck.get(message).add(sender_id);
		}

		//add message to pending list and bebBroadcast it
		if(!pending.contains(message)) {
			pending.add(message);
			PerfectSendThread thread = new PerfectSendThread(message, process.getPeers(), process.getSocket());
			thread.start();
		}
	}
	

	public void run() {
    	
		HashSet<Message> knownMessages = new HashSet<>();
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

				// Get data from packet
                int sender_id = process.idFromAddress.get(packet.getAddress().toString());
                int initial_sender = Integer.parseInt(splitted[0]);
                int msg_seq = Integer.parseInt(splitted[1]);

				Message message = new Message(initial_sender, msg_seq);
                
                //perfect_link logic : deliver only if never delivered before
                if (!knownMessages.contains(message)) {
                	knownMessages.add(message);
                	bebDeliver(message, sender_id);
                }

            } catch (java.io.IOException e) {
                System.out.println("Error while receiving DatagramPacket");
                e.printStackTrace();
            }
        }
    }
}
