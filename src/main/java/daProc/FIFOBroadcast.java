package daProc;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import sun.misc.Signal;
import sun.misc.SignalHandler;
import utils.Peer;

//follows from page 83 and 85 of the book
public class FIFOBroadcast extends Process {

	ArrayList<int[]> urb_messages_delivered;
	ArrayList<int[]> urb_messages_pending;
	public int[][][] urb_ack;
	public boolean start_broadcast;

	
	public FIFOBroadcast(String[] args) {
		super(args);
		
		this.urb_messages_delivered = new ArrayList<int[]>();
		this.urb_messages_pending = new ArrayList<int[]>();
		//did not find a more efficient data structure. 3000 messages to ack for each process should be enough though!
		this.urb_ack = new int[peers.size()][3000][peers.size()];
	}

	
    public void urbBroadcast(int sender_id, int msg_seq) {
        int[] message = {sender_id, msg_seq};
    	if(!urb_messages_pending.contains(message)) {
    		urb_messages_pending.add(message);
    	}
		PerfectSendThread thread = new PerfectSendThread(sender_id, msg_seq, getPeers(), getSocket());
		thread.start();
    }
	
    public boolean canDeliver(int sender_id, int msg_seq) {
    	//count how many acks recorded for this message
    	int counter = 0;
    	for (int i = 0; i < urb_ack[sender_id][msg_seq].length; i++) {
    	    if (urb_ack[sender_id][msg_seq][i] == 1) {counter++;}
    	}
    	//from page 85 of book, if more acks than half of the peers strictly, can deliver
    	if (counter > peers.size()/2) {
    		return true;
    	}
    	return false;
    }
    
    public void urbDeliver(int sender_id, int msg_seq) throws IOException {
        writeLogLine("d " + sender_id + " " + msg_seq);
    }
    

    public static void main(String []args) {
   	
    	//Initialize
    	FIFOBroadcast process = new FIFOBroadcast(args);
    	
    	int seq_msg = process.getSeqNumber();

    	//Signal handlers
    	Runtime r = Runtime.getRuntime();
    	r.addShutdownHook(new Thread(){  
    	public void run(){
    		crashed = true;
    	    System.out.println("Process has been crashed, cleaning up and exiting.");  
    	    }  
    	}  
    	);
    	
    	Signal.handle(new Signal("SIGUSR2"), new SignalHandler() {
            public void handle(Signal sig) {
                System.out.println("Starting the broadcast\n");
                start_sending = true;
            }
        });
    	
    	//urbReceiverThread will update the ack variable
    	//start to listen
    	FIFOReceiverThread sender = new FIFOReceiverThread(process);
    	sender.start();

    	while(!crashed) {
    		try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		
    		if (process.start_broadcast) {
				//broadcast a new message every .5 seconds.
				process.urbBroadcast(process.id, process.seqNumber);
				seq_msg++;
				process.setSeqNumber(seq_msg);
    		}
    		//check every message in pending and deliver if possible
    		if (process.urb_messages_pending.size() != 0) {
    			for (int[] temp : process.urb_messages_pending) {
    				if(process.canDeliver(temp[0], temp[1])) {
    					process.urb_messages_delivered.add(temp);
    					process.urb_messages_pending.remove(temp);
    					try {
							process.urbDeliver(temp[0], temp[1]);
						} catch (IOException e) {
							e.printStackTrace();
						}
    				}
    			}
    		}
    	}
        //TODO while buffer not empty, write to log!
    	//cleanup
    	
    	//Handles the crash and interrupts the receiver cleanly
    	process.crash();
    	
        //Kills process + all threads!
    	System.exit(0);
    }
}
