package daProc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

//this class corresponds to best effort/perfect link receiver that will deliver the message once
//this class merely receive messages and write its activity to the log file
public class FIFOReceiverThread extends Thread {

	public FIFOBroadcast process;
	
	public FIFOReceiverThread(FIFOBroadcast process) {
		this.process = process;
	}
	
	//follows from page 83 of the book
	public void bebDeliver(int[] message) {
        
        //if not already acked, ack it
		if (process.urb_ack[message[0]][message[1]][process.getId()] != 1) {
			process.urb_ack[message[0]][message[1]][process.getId()] = 1;
		}
		
		//add message to pending list and bebBroadcast it
		ArrayList<int[]> pending = process.urb_messages_pending;
		if(!pending.contains(message)) {
			pending.add(message);
			PerfectSendThread thread = new PerfectSendThread(message[0], message[1], process.getPeers(), process.getSocket());
			thread.start();
		}
	}
	
	//implements best effort broadcast
	public void run() {
    	
		ArrayList<int[]> messages_beb_delivered = new ArrayList<int[]>();
		DatagramSocket socket = process.getSocket();

        //Thread will be crashed/stopped from the main
        while (true) {
            byte[] receiveBuffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try {
            	//listen continuously
                socket.receive(packet);

                // Read data from packet
                int msg_seq = Integer.parseInt(new String(packet.getData(), 0, packet.getLength()));

				// Receive address of sender
				InetAddress address = packet.getAddress();

                int sender_id = 3;
				//TODO get sender_id from address
				int[] message = {sender_id, msg_seq};
                
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
