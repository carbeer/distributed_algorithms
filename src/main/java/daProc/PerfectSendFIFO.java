package daProc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;

import utils.Message;
import utils.Peer;


public class PerfectSendFIFO extends PerfectSendThread {

    public PerfectSendFIFO(Message msg, ArrayList<Peer> peers, DatagramSocket socket) {
        super(msg, peers, socket);
    }
    
    @Override
    public void run() {
        byte[] sendBuffer;
        
        // Wait for the broadcast of the previous message to begin
        while(FIFOBroadcast.getAck(new Message(message.getOrigin(), message.getSn()-1)) == null || message.getSn() == 1) {
        	try {
        		Thread.sleep(100);
        		continue;
        	} catch (InterruptedException e) {
        		e.printStackTrace();
        	}
        }
        //Thread will be crashed/stopped from the main
        //while the thread runs, send the message corresponding to the current seq_num of the parent process
        while (true) {
            sendBuffer = (Integer.toString(message.getOrigin()) + ":" + Integer.toString(message.getSn())).getBytes();

            FIFOBroadcast.addPending(FIFOBroadcast.id, message);
			FIFOBroadcast.initAck(message);
			FIFOBroadcast.addAck(message, FIFOBroadcast.ip);
            
            for (Peer peer : this.peers) {
                DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, peer.inetAddress, peer.port);
                try {
                    socket.send(packet);
                } catch(java.io.IOException e) {
                    System.out.println("Error while sending DatagramPacket");
                    e.printStackTrace();
                }
            }
            Process.writeLogLine ("b " + message.getSn());

            //slow down the infinite thread
        	try {
				Thread.sleep(sleep_time);
	        	sleep_time = 2 * sleep_time;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
        }
    }
    
}