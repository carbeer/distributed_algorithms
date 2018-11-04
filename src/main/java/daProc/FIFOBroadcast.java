package daProc;

import utils.Message;

import java.util.*;
import java.util.logging.Level;
import utils.Peer;

import javax.swing.plaf.synth.SynthDesktopIconUI;

public class FIFOBroadcast extends Process {
	// Array of sorted queues of pending messages
	private static HashMap<Integer, PriorityQueue<Message>> pending = new HashMap<>();
	static HashSet<Message> delivered = new HashSet<>();
	static HashMap<Integer, Integer> fifoNext = new HashMap<>();
	// Amount of messages that shall be sent by this process
	final int nrMessages;
	
	public FIFOBroadcast(String[] args) throws Exception {
		super(args);
		LOGGER.log(Level.FINE, "Creating instance of FIFOBroadcast now");
		validateParams();
		this.nrMessages = Integer.parseInt(extraParams[0]);

		// PriorityQueue of ordered messages can be accessed by the id of the origin process
		for (Peer peer : peers) {
			pending.put(peer.id, new PriorityQueue<Message>());
			fifoNext.put(peer.id, 1);
		}
		pending.put(id, new PriorityQueue<Message>());
		fifoNext.put(id, 1);
		

		new FIFOReceiverThread(this).start();

		while(!crashed) {
			if ((this.nrMessages >= seqNumber) && start_broadcast) {
				Message msg = new Message(id, seqNumber, id);
				broadcast(msg);
				seqNumber++;
			}

			// Check whether any message can be delivered
			for (Peer peer : peers) {
				tryToDeliver(getPending(peer.id));
			}
			tryToDeliver(getPending(id));
		}
	}

	private void validateParams() throws Exception {
		if (extraParams == null) {
			throw new Exception("Please provide the number of messages as argument for FIFOBroadcast");
		} 
		//Guillaume : we don t need the elif as we will just disregard addtional parameters right?
		//and we need to be able to input other arguments for assignment 2 anyways
		else if (extraParams.length != 1) {
			throw new Exception("You passed invalid arguments: " + Arrays.toString(extraParams));
		}
		
	}

	public void broadcast(Message msg) {
		PerfectSendFIFO thread = new PerfectSendFIFO(msg, getPeers(), getSocket());
		thread.start();
    }

    // Corresponds to majorityAck
    public synchronized void tryToDeliver(PriorityQueue<Message> pq) {
		Message msg = pq.poll();
		for (int i=0; i<pq.size();i++) {
			if ((getAck(msg).size() > peers.size()/2) && (msg.getSn() == fifoNext.get(msg.getOrigin()))) {
				// Deliver the message
				fifoNext.put(msg.getOrigin(), fifoNext.get(msg.getOrigin())+1);
				writeLogLine("d " + msg.getOrigin() + " " + msg.getSn());
				delivered.add(msg);
				msg = pq.poll();
				continue;
			}
			break;
		}
    }

    // TODO: Think about how we could parallelize the processing of incoming messages --> Distinguish by read/ write access for Threads?
	public synchronized void receiveHandler(Message message) {
		// Verify whether the message is new for the current process
		if(!getPending(message.getOrigin()).contains(message) && !delivered.contains(message)) {
			// Add message to the list of pending messages
			addPending(message);
			initAck(message);
			// Add self to list of acked processes
			addAck(message);
			// Start broadcasting the message to others
			broadcast(message);
		}

		// TODO: Since the data type is a set, it might be more efficient to just add it (without the prior if clause) --> Test later on or Google now
		// The sendingProcess must know the message --> add to ack if not there yet
		if (!getAck(message).contains(message.getPeerID())) {
			addAck(message);
		}
	}
	
    public static synchronized void addPending(Message msg) {
    	pending.get(msg.getOrigin()).add(msg);
    }
    
    
    public static PriorityQueue<Message> getPending(int origin) {
    	return pending.get(origin);
    }


    public static void main(String []args) {
		LOGGER.log(Level.FINE, "Entering the main method of the FIFOBroadcast");
		try {
			new FIFOBroadcast(args);
		} catch (Exception e ) {
			e.printStackTrace();
			System.exit(0);
		}
    }
    

}

