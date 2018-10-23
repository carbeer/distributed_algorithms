package daProc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

//this class corresponds to best effort/perfect link receiver that will deliver the message once
//this class merely receive messages and write its activity to the log file
public class ReceiverThread extends Thread {

	public Process process;
	int[][][] urb_ack;
	ArrayList<int[]> urb_pending;
	
	public ReceiverThread(Process process, int[][][] urb_ack, ArrayList<int[]> urb_pending) {
		this.process = process;
		this.urb_ack = urb_ack;
		this.urb_pending = urb_pending;
	}
	
	//follows from page 83 of the book
	public void bebDeliver(int[] message) {
        
        //if not already acked, ack it
		if (urb_ack[message[0]][message[1]][process.getId()] != 1) {
			urb_ack[message[0]][message[1]][process.getId()] = 1;
		}
		
		//add message to pending list and bebBroadcast it
		ArrayList<int[]> pending = urb_pending;
		if(!pending.contains(message)) {
			pending.add(message);
			bebSendThread thread = new bebSendThread(process, message[0], message[1]);
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
                String payload = new String(packet.getData(), 0, packet.getLength());
                
                //TODO get seq_msg + sender_id from datagram
                int sender_id = 3;
                int msg_seq = 209;
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
