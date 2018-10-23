package daProc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import daProc.Process.Peer;

//this class corresponds to best effort broadcast that will send the message indefinitely
public class bebSendThread extends Thread {

	final private int sender_id;
	private DatagramSocket socket;
	private int msg_seq;
	private ArrayList<Peer> peers;
	
	public bebSendThread(Process process, int sender_id, int msg_seq) {
		this.msg_seq = msg_seq;
		this.peers = process.getPeers();
		this.socket = process.getSocket();
		this.sender_id = sender_id;
	}
	
    public void run() {    	
    	//Spawns a new thread to send one message to all peers
    	perfectSendThread sender = new perfectSendThread(sender_id, msg_seq, peers, socket);
    	//this abstraction level supply perfectsendthread with several peers instead of just one to send to
    	//it is not necessary except to comply with the teacher's levels of abstraction
    	sender.start();   	
    }
}
