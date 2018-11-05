package daProc;

import utils.Message;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import utils.Peer;

public class FIFOBroadcast extends Process {
	// Array of sorted queues of pending messages
	private static HashMap<Integer, PriorityQueue<Message>> pending = new HashMap<>();
	static HashMap<Integer, Integer> fifoNext = new HashMap<>();
	// Amount of messages that shall be sent by this process
	final int nrMessages;
	
	public FIFOBroadcast(String[] args) throws Exception {
		super(args);
		LOGGER.log(Level.INFO, "Starting the FIFOBroadcast protocol");
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
			if ((this.nrMessages >= seqNumber) && startBroadcast) {
				Message msg = new Message(id, seqNumber, id);
				initAck(msg);
				broadcast(msg);
				addPending(msg);
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
		Message msg;
		while (true) {
			msg = pq.peek();
			if (msg == null) {
				return;
			}
			// Just look at the first element
			if ((getAck(msg).size() > peers.size()/2) && (msg.getSn() == fifoNext.get(msg.getOrigin()))) {
				// Deliver the message
				fifoNext.put(msg.getOrigin(), fifoNext.get(msg.getOrigin())+1);
				writeLogLine("d " + msg.getOrigin() + " " + msg.getSn());
				// Remove the first element now (as it has been used)
				pq.poll();
				continue;
			}
			return;
		}
    }

    // TODO: Think about how we could parallelize the processing of incoming messages --> Distinguish by read/ write access for Threads?
	public synchronized void receiveHandler(Message message) {
		// Verify whether the message is new for the current process
		if(!isDelivered(message)) {
			// Insert message to the list of pending messages
			if (addPending(message)) {
				// Initialize acks if this is a new message
				initAck(message);
				// Ack happens as part of the broadcasting process
				broadcast(message);
				return;
			}
		}
		// The sendingProcess must know the message --> add to ack if not there yet (check is done implicitly through the Set)
		addAck(message);
	}

    public static synchronized boolean addPending(Message msg) {
		if (pending.get(msg.getOrigin()).contains(msg)) {
			return false;
		}
		return pending.get(msg.getOrigin()).add(msg);
    }
    
    public static PriorityQueue<Message> getPending(int origin) {
    	return pending.get(origin);
    }

    public static boolean isDelivered(Message msg) {
		if (fifoNext.get(msg.getOrigin()) > msg.getSn()) {
			return true;
		}
		return false;
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

