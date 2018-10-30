package daProc;

import utils.Message;

import java.util.*;
import java.util.logging.Level;

import sun.misc.Signal;
import sun.misc.SignalHandler;
import utils.Peer;

public class FIFOBroadcast extends Process {
	// Array of sorted queues of pending messages
	HashMap<Integer, PriorityQueue<Message>> pending;
	HashSet<Message> delivered;
	boolean start_broadcast;
	// Amount of messages that shall be sent by this process
	int nrMessages;
	int[] fifo_next;
	
	public FIFOBroadcast(String[] args) throws Exception {
		super(args);
		LOGGER.log(Level.INFO, "Creating instance of FIFOBroadcast now");
		validateParams();
		this.msgAck = new HashMap<>();
		this.delivered = new HashSet<>();
		this.nrMessages = Integer.parseInt(extraParams[0]);
		// PriorityQueue of ordered messages can be accessed by the id of the origin process
		this.pending = new HashMap<>();
		for (Peer peer : peers) {
			pending.put(peer.id, new PriorityQueue<>());
		}

		// FIFO keep track of next o deliver for each process
		this.fifo_next = new int[peers.size()];
		Arrays.fill(fifo_next, 1);

		new FIFOReceiverThread(this).start();

		while(!crashed) {

			if (start_broadcast && this.nrMessages >= seqNumber) {
				Message msg = new Message(id, seqNumber);
				pending.get(msg.getOrigin()).add(msg);
				msgAck.get(msg).add(ip);
				broadcast(msg);
				seqNumber++;
				// TODO: Since the delivery method is in this loop as well, sleeping the entire thread might make us not
				// TODO (cont'd): deliver some messages if the process is crashed in the meantime --> Fix somehow
				// Broadcast a new message every .5 seconds.
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// Check whether any message can be delivered
			for (Peer peer : peers) {
				PriorityQueue<Message> pq = pending.get(peer.id);
				tryToDeliver(pq);
			}
		}
	}

	private void validateParams() throws Exception {
		if (extraParams == null) {
			throw new Exception("Please provide the number of messages as argument for FIFOBroadcast");
		} else if (extraParams.length != 1) {
			throw new Exception("You passed invalid arguments: " + Arrays.toString(extraParams));
		}
	}

	public void broadcast(Message msg) {
		PerfectSendThread thread = new PerfectSendThread(msg, getPeers(), getSocket());
		thread.start();
    }

    // Corresponds to majorityAck
    public void tryToDeliver(PriorityQueue<Message> pq) {
		if (pq.isEmpty()) {
			return;
		}
		Message msg = pq.peek();
		while (true) {
			if ((msgAck.get(msg).size() > peers.size()/2) && (msg.getSn() == fifo_next[msg.getOrigin()] - 1)) {
				// Deliver the message
				fifo_next[msg.getOrigin()]++;
				writeLogLine("d " + msg.getOrigin() + " " + msg.getSn());
				delivered.add(msg);
				msg = pq.poll();
				continue;
			}
			break;
		}
    }

    // TODO: Think about how we could parallelize the processing of incoming messages --> Distinguish by read/ write access for Threads?
	public void receiveHandler(Message message, String receivedFrom) {
		// Verify whether the message is new for the current process
		if(!pending.get(message.getOrigin()).contains(message) && !delivered.contains(message)) {
			// Add message to the list of pending messages
			pending.get(message.getOrigin()).add(message);
			msgAck.put(message, new HashSet<>());
			// Add self to list of acked processes
			msgAck.get(message).add(ip);
			// Start broadcasting the message to others
			broadcast(message);
		}

		// TODO: Since the data type is a set, it might be more efficient to just add it (without the prior if clause) --> Test later on or Google now
		// The sendingProcess must know the message --> add to ack if not there yet
		if (msgAck.get(message).contains(receivedFrom)) {
			msgAck.get(message).add(receivedFrom);
		}
	}


    public static void main(String []args) {
		LOGGER.log(Level.FINE, "Entering the main method of the FIFOBroadcast");
		try {
			FIFOBroadcast process = new FIFOBroadcast(args);
		} catch (Exception e ) {
			e.printStackTrace();
		}

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

		Signal.handle(new Signal("TERM"), new SignalHandler() {
			public void handle(Signal sig) {
				crash();
			}
		});
    }
}

