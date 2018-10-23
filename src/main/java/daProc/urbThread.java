package daProc;

import java.net.DatagramPacket;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.ArrayList;

import daProc.Process.Peer;

//follows from page 83 and 85 of the book
public class urbThread extends Thread {

	private ArrayList<Peer> peers;	
	public Process process;
	ArrayList<int[]> messages_delivered;
	ArrayList<int[]> messages_pending;
	int[][][] ack;
	private volatile boolean running = true;
	public boolean start_broadcast;

	
	public urbThread(Process process, boolean start_broadcast) {
		this.process = process;
		this.peers = process.getPeers();
		this.start_broadcast = start_broadcast;
		
		this.messages_delivered = new ArrayList<int[]>();
		this.messages_pending = new ArrayList<int[]>();
		//did not find a more efficient data structure. 3000 messages to ack for each process should be enough though!
		this.ack = new int[peers.size()][3000][peers.size()];
	}
	
	public void interrupt() {
		this.running = false;
		}
	
    public void urbBroadcast(int sender_id, int msg_seq) {
        int[] message = {sender_id, msg_seq};
    	if(!messages_pending.contains(message)) {
    		messages_pending.add(message);
    	}
		bebSendThread thread = new bebSendThread(process, msg_seq, sender_id);
		thread.start();
    }
	
    public boolean canDeliver(int sender_id, int msg_seq) {
    	//count how many acks recorded for this message
    	int counter = 0;
    	for (int i = 0; i < ack[sender_id][msg_seq].length; i++) {
    	    if (ack[sender_id][msg_seq][i] == 1) {counter++;}
    	}
    	//from page 85 of book, if more acks than half of the peers strictly, can deliver
    	if (counter > peers.size()/2) {
    		return true;
    	}
    	return false;
    }
    
    public void urbDeliver(int sender_id, int msg_seq) throws IOException {
    	FileWriter writer = process.getWriter();
        try { 
        	writer.write("d " + sender_id + " " + msg_seq);
            
        } finally {
        	writer.close();
        }
        System.out.println("Message delivered to process " + process.getId() +" : " + sender_id + " " + msg_seq);
    }
    

	public void run() {    	
    	int seq_msg = process.getSeqNumber();
    	
    	//urbReceiverThread will update the ack variable
    	//start to listen
    	ReceiverThread sender = new ReceiverThread(process, ack, messages_pending);
    	sender.start();
    	    	
    	while(running) {
    		try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		
    		if (start_broadcast) {
				//broadcast a new message every .5 seconds.
				urbBroadcast(process.getId(), process.getSeqNumber());
				seq_msg++;
				process.setSeqNumber(seq_msg);
    		}
    		//check every message in pending and deliver if possible
    		if (messages_pending.size() != 0) {
    			for (int[] temp : messages_pending) {
    				if(canDeliver(temp[0], temp[1])) {
    					messages_delivered.add(temp);
    					try {
							urbDeliver(temp[0], temp[1]);
						} catch (IOException e) {
							e.printStackTrace();
						}
    				}
    			}
    		}
    	}
        //TODO while buffer not empty, write to log!
    	//cleanup
    }
}
