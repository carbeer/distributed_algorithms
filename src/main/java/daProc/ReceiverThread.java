package daProc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import java.io.FileWriter;

//this class corresponds to perfect links receiver that will deliver the message once
//this class merely receive messages and write its activity to the log file
public class ReceiverThread extends Thread {

	private DatagramSocket socket;
	private FileWriter writer;
	private volatile boolean running = true;
	
	public ReceiverThread(Process process) {
		this.writer = process.getWriter();
		this.socket = process.getSocket();
	}

	public void interrupt() {
		this.running = false;
		}
	
	public void run() {
    	
		ArrayList<int[]> messages_delivered = new ArrayList<int[]>();

        //Thread will be crashed/stopped from the level above in the main
        while (running) {
            byte[] receiveBuffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try {
                socket.receive(packet);

                // Read data from packet
                String payload = new String(packet.getData(), 0, packet.getLength());
                
                //TODO get seq_msg + sender_id from datagram
                int sender_id = 3;
                int msg_seq = 209;
                int[] message = {sender_id, msg_seq};
                
                //perfect_link logic : deliver only if never delivered before
                if (!messages_delivered.contains(message)) {
                	messages_delivered.add(message);
                	
	                //Write to log = deliver the message
	                try { 
	                 	writer.write("d " + sender_id + " " + msg_seq);
	                    
	                } finally {
	                	writer.close();
	                }
	                System.out.println("Packet delivered: " + String.format("d %s:%d %s", packet.getAddress().toString(), packet.getPort(), payload));
                }

            } catch (java.io.IOException e) {
                System.out.println("Error while receiving DatagramPacket");
                e.printStackTrace();
            }
        }
        
        //Cleanup when stopped
        //TODO while buffer not empty, write to log!
    }
}
