package daProc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import daProc.Process.Peer;

//this class corresponds to perfect links broadcast that will send the message indefinitely
public class SenderThread extends Thread {

	final private int id;
	private DatagramSocket socket;
	private int seqNumber;
	private ArrayList<Peer> peers;
	
	public SenderThread(Process process) {
		this.seqNumber = process.getSeqNumber();
		this.peers = process.getPeers();
		this.socket = process.getSocket();
		this.id = process.getId();
	}
	
    public void run() {
        byte[] sendBuffer;
        int sleep_time = 1000;
        //TODO write to log file when thread spawned the sequence of the msg sending + process id

        //Thread will be crashed/stopped from the level above in the main
        //while the thread runs, send the message corresponding to the current seq_num of the parent process
        while (true) {
        	//TODO send sender_id + seq num
            sendBuffer = Integer.toString(seqNumber).getBytes();
            for (Process.Peer peer : this.peers) {
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
