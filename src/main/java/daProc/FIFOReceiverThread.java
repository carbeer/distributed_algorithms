package daProc;

import utils.Message;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

//this class corresponds to best effort/perfect link receiver that will deliver the message once
//this class merely receive messages and write its activity to the log file
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
	public void bebDeliver(Message message) {
		if (process.msgAck.get(message) == null) {
			process.msgAck.put(message, new ArrayList<>());
		}
		// TODO: Is this always the case here?
		if (process.msgAck.get(message).contains(process.getId())) {
			process.msgAck.get(message).add(process.getId());
		}

		//add message to pending list and bebBroadcast it
		if(!pending.contains(message)) {
			pending.add(message);
			PerfectSendThread thread = new PerfectSendThread(message, process.getPeers(), process.getSocket());
			thread.start();
		}
	}
	
	//implements best effort broadcast
	public void run() {
    	
		HashSet<Message> knownMessages = new HashSet<>();
		DatagramSocket socket = process.getSocket();

        //Thread will be crashed/stopped from the main
        while (true) {
            byte[] receiveBuffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try {
                socket.receive(packet);
                int msg_seq = Integer.parseInt(new String(packet.getData(), 0, packet.getLength()));
                int sender_id = process.idFromAddress.get(packet.getAddress().toString());
                Message message = new Message(sender_id, msg_seq);
                
                //perfect_link logic : deliver only if never delivered before
                if (!knownMessages.contains(message)) {
                	knownMessages.add(message);
                	bebDeliver(message);
                }

            } catch (java.io.IOException e) {
                System.out.println("Error while receiving DatagramPacket");
                e.printStackTrace();
            }
        }
    }
}
