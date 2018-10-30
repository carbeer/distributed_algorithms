package daProc;

import utils.Message;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.Arrays;
import java.util.HashSet;

import sun.misc.Signal;
import sun.misc.SignalHandler;

//follows from page 83 and 85 of the book
public class FIFOBroadcast extends Process {
	HashSet<Message> messages_pending;
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
		this.msgAck = new HashMap<>();
		this.nrMessages = Integer.parseInt(extraParams[0]);
		// Messages that are pending for delivery
		this.messages_pending = new HashSet<>();

		new FIFOReceiverThread(this).start();

		while(!crashed) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (start_broadcast && this.nrMessages >= seqNumber) {
				//broadcast a new message every .5 seconds.
				urbBroadcast(new Message(getId(), seqNumber));
				seqNumber++;
			}
			//check every message in pending and deliver if possible
			if (messages_pending.size() != 0) {
				for (Message msg : messages_pending) {
					tryToDeliver(msg);
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

    public void tryToDeliver(Message msg) {
    	//from page 85 of book, if more acks than half of the peers strictly, can deliver
    	if (msgAck.get(msg).size() > peers.size()/2) {
			writeLogLine("d " + msg.getSender() + " " + msg.getSn());
			messages_pending.remove(msg);
    	}
    }

    public static void main(String []args) {
		LOGGER.log(Level.FINE, "Entering the main method of the FIFOBroadcast");
		try {
			FIFOBroadcast process = new FIFOBroadcast(args);
		} catch (Exception e ) {
			e.printStackTrace();
		}

		// TODO: Move signal handlers to separate class
    	//Signal handlers
		/*
    	Signal.handle(new Signal("SIGUSR1"), new SignalHandler() {
            public void handle(Signal sig) {
                System.out.println("Starting the broadcast\n");
                start_sending = true;
            }
        });
		*/

		// TODO: Do we need this?
    	Runtime r = Runtime.getRuntime();
    	r.addShutdownHook(new Thread(){  
			public void run(){
				crash();
			}
    	});

		Signal.handle(new Signal("INT"), new SignalHandler() {
			public void handle(Signal sig) {
				crash();
			}
		});

		Signal.handle(new Signal("KILL"), new SignalHandler() {
			public void handle(Signal sig) {
				crash();
			}
		});
    }
}

