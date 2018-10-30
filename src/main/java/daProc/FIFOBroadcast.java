package daProc;

import utils.Message;

import java.util.logging.Level;
import java.util.Arrays;
import java.util.HashSet;

import sun.misc.Signal;
import sun.misc.SignalHandler;

//follows from page 83 and 85 of the book
public class FIFOBroadcast extends Process {
	HashSet<Message> messages_delivered;
	HashSet<Message> messages_pending;
	int[][][] urb_ack;
	boolean start_broadcast;
	// Amount of messages that shall be sent by this process
	int nrMessages;

	
	public FIFOBroadcast(String[] args) throws Exception {
		super(args);
		LOGGER.log(Level.INFO, "Creating instance of FIFOBroadcast now");
		if (extraParams == null) {
			throw new Exception("Please provide the number of messages as argument for FIFOBroadcast");
		} else if (extraParams.length != 1) {
			throw new Exception("You passed invalid arguments: " + Arrays.toString(extraParams));
		}
		this.nrMessages = Integer.parseInt(extraParams[0]);
		this.messages_delivered = new HashSet<>();
		this.messages_pending = new HashSet<>();
		//did not find a more efficient data structure. 3000 messages to urb_ack for each process should be enough though!
		this.urb_ack = new int[peers.size()][3000][peers.size()];

		new FIFOReceiverThread(this).start();

		while(!crashed) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int seq_msg = getSeqNumber();

			if (start_broadcast && this.nrMessages >= seq_msg) {
				//broadcast a new message every .5 seconds.
				urbBroadcast(new Message(getId(), getSeqNumber()));
				seq_msg++;
				setSeqNumber(seq_msg);
			}
			//check every message in pending and deliver if possible
			if (messages_pending.size() != 0) {
				for (Message temp : messages_pending) {
					if(canDeliver(temp)) {
						messages_delivered.add(temp);
						urbDeliver(temp);
					}
				}
			}
		}
	}


    public void urbBroadcast(Message msg) {
    	if(!messages_pending.contains(msg)) {
    		messages_pending.add(msg);
    	}
		PerfectSendThread thread = new PerfectSendThread(msg, getPeers(), getSocket());
		thread.start();
    }
	
    public boolean canDeliver(Message msg) {
    	//count how many acks recorded for this message
    	int counter = 0;
    	for (int i = 0; i < urb_ack[msg.getSender()][msg.getSn()].length; i++) {
    	    if (urb_ack[msg.getSender()][msg.getSn()][i] == 1) {counter++;}
    	}
    	//from page 85 of book, if more acks than half of the peers strictly, can deliver
    	if (counter > peers.size()/2) {
    		return true;
    	}
    	return false;
    }
    
    public void urbDeliver(Message msg) {
        writeLogLine("d " + msg.getSender() + " " + msg.getSn());
    }
    

    public static void main(String []args) {
		LOGGER.log(Level.FINE, "Entering the main method");
		//Initialize
		try {
			FIFOBroadcast process = new FIFOBroadcast(args);
		} catch (Exception e ) {
			e.printStackTrace();
		}

    	//Signal handlers
    	Runtime r = Runtime.getRuntime();
    	r.addShutdownHook(new Thread(){  
			public void run(){
				LOGGER.log(Level.SEVERE, "Process has been crashed, cleaning up and exiting.");
				crashed = true;
				writeLogsToFile();
				socket.close();
				System.exit(0);
			}
    	});

    	/*
    	Signal.handle(new Signal("SIGUSR1"), new SignalHandler() {
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
		*/
    }
}

