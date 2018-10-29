package daProc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import daProc.Process.Peer;
import utils.Peer;

//this class corresponds to perfect link broadcast that will send the message indefinitely
public class PerfectSendThread extends Thread {

	private DatagramSocket socket;
	private int seqNumber;
	private ArrayList<Peer> dst_peer;
	private int sender_id;
	
	public PerfectSendThread(int sender_id, int seqNumber, ArrayList<Peer> dst_peer, DatagramSocket socket) {
		this.seqNumber = seqNumber;
		this.dst_peer = dst_peer;
		this.socket = socket;
		this.sender_id = sender_id;
	}
	
    public void run() {
        byte[] sendBuffer;
        int sleep_time = 1000;
        //TODO write to log file the broadcast of the msg sending + process id

        //Thread will be crashed/stopped from the main
        //while the thread runs, send the message corresponding to the current seq_num of the parent process
        while (true) {
        	//TODO send sender_id + seq num
            sendBuffer = (Integer.toString(sender_id) + " "  + Integer.toString(seqNumber)).getBytes();
            

            for (Process.Peer peer : this.dst_peer) {
                DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, peer.inetAddress, peer.port);
                System.out.printf("Sent message with seqNr %s to peer %s", seqNumber, peer.getIpPort());
                try {
                    socket.send(packet);
                } catch(java.io.IOException e) {
                    System.out.println("Error while sending DatagramPacket");
                    e.printStackTrace();
                }
            }
            
            //slow down the infinite thread
        	try {
				Thread.sleep(sleep_time);
	        	sleep_time = 2*sleep_time;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
        }
    }
}
