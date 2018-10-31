package daProc;

import utils.Message;

import java.util.*;
import java.util.logging.Level;
import utils.Peer;

public class FIFOBroadcast extends Process {
	// Array of sorted queues of pending messages
	HashMap<Integer, PriorityQueue<Message>> pending = new HashMap<>();
	HashSet<Message> delivered = new HashSet<>();;
	// Amount of messages that shall be sent by this process
	final int nrMessages;
	int[] fifo_next;
	
	public FIFOBroadcast(String[] args) throws Exception {
		super(args);
		LOGGER.log(Level.INFO, "Creating instance of FIFOBroadcast now");
		validateParams();
		this.nrMessages = Integer.parseInt(extraParams[0]);

		// PriorityQueue of ordered messages can be accessed by the id of the origin process
		for (Peer peer : peers) {
			pending.put(peer.id, new PriorityQueue<Message>());
		}
		pending.put(id, new PriorityQueue<Message>());

		// FIFO keep track of next o deliver for each process
		// TODO: Quite ugly like this as [0] is unused --> Potentially change
		//Guillaume : that's why I used to use it, I don't get the 2 though? Why not +1 only?
		this.fifo_next = new int[peers.size()+2];
		Arrays.fill(fifo_next, 1);

		new FIFOReceiverThread(this).start();


		/*
		// Only for testing purposes since WSL doesn't allow to send signals
		Thread.sleep(10000);
		start_broadcast = true;
		*/

		while(!crashed) {
			if (start_broadcast && this.nrMessages >= seqNumber) {
				Message msg = new Message(id, seqNumber);
				pending.get(msg.getOrigin()).add(msg);
				msgAck.put(msg, new HashSet<String>());
				msgAck.get(msg).add(ip);
				broadcast(msg);
				seqNumber++;
				// TODO: Since the delivery method is in this loop as well, sleeping the entire thread might make us not
				// TODO (cont'd): deliver some messages if the process is crashed in the meantime --> Fix somehow
				
				// Guillaume : we could clean this up by running a trytodeliver method in the crash() method?
				// Then we will ensure that's pending to deliver will be handled
				
				// Broadcast a new message every .5 seconds.
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// Check whether any message can be delivered
			for (Peer peer : peers) {
				tryToDeliver(pending.get(peer.id));
			}
			tryToDeliver(pending.get(id));
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
		if (!crashed) {
		PerfectSendThread thread = new PerfectSendThread(msg, getPeers(), getSocket());
		thread.start();
		}
    }

    // Corresponds to majorityAck
    public void tryToDeliver(PriorityQueue<Message> pq) {
		if (pq.isEmpty()) {
			return;
		}
		Message msg = pq.peek();
		while (true) {
			if ((msgAck.get(msg).size() > peers.size()/2) && (msg.getSn() == fifo_next[msg.getOrigin()])) {
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
    //Guillaume : why do we need that? The receive buffer enables us to keep a list of messages to process
    
	public synchronized void receiveHandler(Message message, String receivedFrom) {
		// Verify whether the message is new for the current process
		if(!pending.get(message.getOrigin()).contains(message) && !delivered.contains(message)) {
			// Add message to the list of pending messages
			pending.get(message.getOrigin()).add(message);
			msgAck.put(message, new HashSet<String>());
			// Add self to list of acked processes
			
			//Guillaume : !! I may have misunderstood your code here. But we need to ack this message ourselves, yes, 
			//but then we also need to ack for the process that we received this message from since we know that it has received it as well
			//Edit : My bad, you did this below! :P
		
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
			new FIFOBroadcast(args);
		} catch (Exception e ) {
			e.printStackTrace();
			System.exit(0);
		}
    }
}

