package daProc;

import utils.Message;
import java.util.*;
import java.util.logging.Level;
import utils.Peer;

/**
 * FIFOBroadcast implements the properties and methods of a node with FIFO broadcast logic.
 *
 */
public class FIFOBroadcast extends Process {
	// Array of sorted queues of pending messages
	private static HashMap<Integer, PriorityQueue<Message>> pending = new HashMap<>();
	static HashMap<Integer, Integer> fifoNext = new HashMap<>();
	// Amount of messages that shall be sent by this process
	static int nrMessages;

	/**
	 * FIFOBroadcast Process constructor. Takes cmd arguments to initialize the
	 * process.
	 * 
	 * @param args : id of the process, membership and id of message to send
	 * @throws Exception
	 */
	public FIFOBroadcast(String[] args) throws Exception {
		super(args);
		LOGGER.log(Level.INFO, "Starting the FIFOBroadcast protocol");

		// Check that command line arguments provided by the user are correctly formatted
		validateParams();
		nrMessages = Integer.parseInt(extraParams[0]);

		// Initialize variables
		for (Peer peer : peers) {
			pending.put(peer.id, new PriorityQueue<Message>());
			fifoNext.put(peer.id, 1);
		}
		pending.put(id, new PriorityQueue<Message>());
		fifoNext.put(id, 1);

		// Start to listen on the initialized socket.
		new FIFOReceiver().start();
		new FIFOSend().start();

		while (!crashed) {
			// Check whether any message can be delivered according to the FIFO logic, and
			// deliver the messages that can be.
			for (Peer peer : peers) {
				tryToDeliver(getPending(peer.id));
			}
			tryToDeliver(getPending(id));

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method used to validate the extra-parameters given through the cmd
	 * 
	 * @throws Exception
	 */
	private static void validateParams() throws Exception {
		if (extraParams == null) {
			throw new Exception("Please provide the number of messages as argument for FIFOBroadcast");
		} else if (extraParams.length != 1) {
			throw new Exception("You passed invalid arguments: " + Arrays.toString(extraParams));
		}
	}

	/**
	 * Called when process needs to send a messages to the network. Spawns a thread
	 * implementing perfect link logic for all messages to broadcast.
	 *
	 * @param msg to broadcast
	 */
	public static void broadcast(Message msg) {
		PerfectSend thread = new PerfectSend(msg, getPeers(), getSocket());
		thread.start();
	}

	/**
	 * Method that checks whether messages passed in argument can be delivered, and
	 * deliver the ones that can be. This method implements majority ack logic that
	 * if a majority of processes acknowledged a message, then it can be delivered.
	 * 
	 * @param pq : priority queue of messages to acknowledge
	 */
	public synchronized void tryToDeliver(PriorityQueue<Message> pq) {
		Message msg;
		while (true) {
			msg = pq.peek();

			if (msg == null || getAck(msg) == null) {
				return;
			}
			// Just look at the first element and check if it can be delivered
			if ((getAck(msg).size() > peers.size() / 2) && (msg.getSn() == fifoNext.get(msg.getOrigin()))) {

				// Deliver the message
				fifoNext.put(msg.getOrigin(), fifoNext.get(msg.getOrigin()) + 1);
				writeLogLine("d " + msg.getOrigin() + " " + msg.getSn());

				// Remove the first element now (as it has been used)
				pq.poll();
				continue;
			}
			return;
		}
	}

	/*
	 * Method that checks if the message is new for the current process, and updates
	 * the pending, and acknowledgements variable. It also starts the broadcast of
	 * this message by the process so to ack it for other processes.
	 */
	public static synchronized void receiveHandler(Message message) {
		if (message == null) {
			return;
		}
		if (!isDelivered(message)) {

			// Insert message to the list of pending messages
			if (message.getOrigin() != FIFOBroadcast.id && addPending(message)) {

				// Initialize acks if this is a new message
				initAck(message);

				// Ack happens as part of the broadcasting process
				broadcast(message);
				return;
			}
		}

		// The sendingProcess must know the message --> add to ack if not there yet
		// (check is done implicitly through the Set)
		addAck(message);
	}

	/**
	 * Add new message to the pending list
	 * 
	 * @param msg : message to add
	 * @return true if successful, false otherwise
	 */
	public static synchronized boolean addPending(Message msg) {
		if (pending.get(msg.getOrigin()).contains(msg)) {
			return false;
		}
		return pending.get(msg.getOrigin()).add(msg);
	}

	/**
	 * Gets the list of pending messages to deliver for the process with id origin
	 * 
	 * @param origin : id of the process
	 * @return Priority queue of pending messages for the process with id origin
	 */
	public static PriorityQueue<Message> getPending(int origin) {
		return pending.get(origin);
	}

	/**
	 * Checks if the message in argument has already been delivered
	 * 
	 * @param msg
	 * @return true if the message was delivered in the past, false otherwise
	 */
	public static boolean isDelivered(Message msg) {
		if (fifoNext.get(msg.getOrigin()) > msg.getSn()) {
			return true;
		}
		return false;
	}

	public static Message setMessageDependencies(Message msg, ArrayList<Integer> process_dependancies){
		// TODO extract the dependencies and add them to the msg dependencies like this
		// dependencies are the latest messages delivered from the dependancies processes
		
		// find the process dependancies and the latest message delivered for those dependencies
		// like a vector clock
		// Say th process depends on proc 2 and 4, and the latest messages delivered are those
		// 2 dummies message dependancies 
		if (!process_dependancies.isEmpty){
			processdependancy1 = Message(2, 32);
			processdependancy2 = Message(4, 21);
	
			ArrayList<Message> dependencies = new ArrayList<Message>();
			dependencies.add(processdependancy1);
			dependencies.add(processdependancy2);
		
			Message message = msg;
			message.setDependencies(dependencies);
		}

		return message;
	}

	/**
	 * Main method of our process. Is called to initialize our process.
	 * 
	 * @param args : cmd arguments
	 */
	public static void main(String[] args) {
		LOGGER.setLevel(Level.OFF);
		LOGGER.log(Level.FINE, "Entering the main method of the FIFOBroadcast");
		try {
			new FIFOBroadcast(args);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
