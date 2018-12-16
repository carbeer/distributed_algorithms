package daProc;

import java.io.*;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.misc.Signal;
import sun.misc.SignalHandler;
import utils.Message;
import utils.Peer;


/**
 * Class Process implements the properties and methods of a node
 *
 */
public class Process {
	static int id;
	static String ip;
	final int port;
	static boolean startBroadcast;
	static String[] extraParams;
	static DatagramSocket socket;
	static volatile boolean crashed = false;
	static int seqNumber = 1;
	static ArrayList<Peer> peers;
	static ArrayList<Integer> process_dependencies;
	private static volatile HashMap<Message, HashSet<Integer>> msgAck = new HashMap<>();;
	static private String logs = "";
	static FileWriter writer;
	public final static Logger LOGGER = Logger.getLogger(Process.class.getName());

	/**
	 * Process constructor. Takes cmd arguments to initialize the process.
	 * 
	 * @param args : id of the process, membership and id of message to send
	 * @throws Exception
	 */
	public Process(String[] args) throws Exception {
		// Parse command line arguments
		this.id = Integer.valueOf(args[0]);
		String membership = args[1];

		// Check whether extra parameters were given as command line arguments
		if (args.length > 2) {
			extraParams = new String[args.length - 2];
			for (int i = 2; i < args.length; i++) {
				extraParams[i - 2] = args[i];
			}
		} else {
			extraParams = null;
		}

		// Reads the membership file and gather information about the processes
		File membershipPath = new File(System.getProperty("user.dir") + File.separator + membership);
		//Keep track of nodes in the network 
		this.peers = readMembership(membershipPath, id);
		//Save dependencies of id process 
		this.process_dependencies = readDependencies(membershipPath, id);
		this.ip = peers.get(0).address;
		this.port = Integer.valueOf(peers.get(0).port);
		peers.remove(0);
		// Create socket to send messages to and from
		try {
			this.socket = new DatagramSocket(this.port);
		} catch (java.net.SocketException e) {
			throw e;
		}

		// Initialize the writer for the log file
		try {
			File directory = new File(System.getProperty("user.dir"));
			writer = new FileWriter(directory + File.separator + "da_proc_" + this.id + ".out");
		} catch (java.io.IOException e) {
			LOGGER.log(Level.SEVERE, "Error while creating the file writer");
			e.printStackTrace();
		}

		// Signal handlers
		//SIGNAL USR2 to start broadcasting messages
		Signal.handle(new Signal("USR2"), new SignalHandler() {
			public void handle(Signal sig) {
				LOGGER.log(Level.INFO, "Process " + id + " Received USR2 signal. Starting the broadcast.");
				startBroadcast = true;
			}
		});
		
		//SIGNAL INT to make the process crash
		Signal.handle(new Signal("INT"), new SignalHandler() {
			public void handle(Signal sig) {
				crash();
			}
		});
		//SIGNAL TERM to make the process crash
		Signal.handle(new Signal("TERM"), new SignalHandler() {
			public void handle(Signal sig) {
				crash();
			}
		});
	}

	/**
	 * Method called when a crash is triggered to clean up the buffer of the process
	 * before exiting it.
	 */
	public static void crash() {
		LOGGER.log(Level.SEVERE, "Process " + id + " has been crashed, cleaning up.");
		crashed = true;
		try {
			writeLogsToFile();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Process " + id + " Couldn't write the logs... We're screwed!");
			e.printStackTrace();
		} finally {
			socket.close();
			LOGGER.log(Level.SEVERE, "Process " + id + " Exiting now.");
			System.exit(0);
		}
	}

	/**
	 * Method to flush the log variable out to a log file at the end of the
	 * execution
	 */
	public static synchronized void writeLogsToFile() {
		try {
			writer.write(logs);
			logs = "";
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to add a log event to the log variable
	 * @param s : log event to write
	 */
	public static synchronized void writeLogLine(String s) {
		if (!crashed) {
			logs = logs + s + "\n";
			LOGGER.log(Level.FINE, s + "\n");
		}
	}

	/**
	 * Method to parse the membership file to our data structure
	 * @param f : membership file path
	 * @param procID : id of the process
	 * @return ArrayList of all peers set up in the membership file (including self)
	 */
	public ArrayList<Peer> readMembership(File f, int procID) {
		ArrayList<Peer> initPeers = new ArrayList<Peer>();
		try {
			BufferedReader b = new BufferedReader(new FileReader(f));
			String line;
			int line_nr = 0;
			String[] splitted;
			Peer temp = null;
			int nr_processes = 0;
			while ((line = b.readLine()) != null) {
				splitted = line.split("\\s+");

				// Finds out how many processes there is
				if((splitted.length == 1) && (line_nr ==0)){
					nr_processes = Integer.valueOf(splitted[0]);
				}

				// Gather information on the peers : their IP and port
				if(line_nr <= nr_processes){
					if (Integer.valueOf(splitted[0]) != procID && splitted.length == 3) {
						initPeers.add(new Peer(splitted[1], Integer.valueOf(splitted[2]), Integer.valueOf(splitted[0])));
					}
					if (Integer.valueOf(splitted[0]) == procID && splitted.length == 3) {
						temp = new Peer(splitted[1], Integer.valueOf(splitted[2]), Integer.valueOf(splitted[0]));
					}
				}
				line_nr++;
			}
			initPeers.add(0, temp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return initPeers;
	}

	/**
	 * Method to read the causal dependencies from the membership file
	 * @param f : membership file path
	 * @param procID : id of the process
	 * @return ArrayList of all peers dependencies of the process
	 */
	public ArrayList<Integer> readDependencies(File f, int procID) {
		ArrayList<Integer> process_dependencies = new ArrayList<Integer>();
		try {
			BufferedReader b = new BufferedReader(new FileReader(f));
			String line;
			int line_nr = 0;
			String[] splitted;
			int nr_processes = 0;
			while ((line = b.readLine()) != null) {
				splitted = line.split("\\s+");

				// Finds out how many processes there is
				if((splitted.length == 1) && (line_nr ==0) ){
					nr_processes = Integer.valueOf(splitted[0]);
				}

				// Gather infromation on localized causal dependancies
				if((line_nr > nr_processes) && (line_nr <=2*nr_processes) && (Integer.valueOf(splitted[0]) == procID)){
					// Check if empty dependancies
					if (splitted.length > 0) {
						for (int i = 1; i < splitted.length; i++){
						process_dependencies.add(Integer.valueOf(splitted[i]));
						}
					}
				}
				line_nr++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return process_dependencies;
	}

	/**
	 * Method used to update the acknowledgment variable of the current process by adding the msg in argument to it
	 * @param msg to acknowledge
	 */
	public static synchronized void addAck(Message msg) {
		msgAck.get(msg).add(msg.getPeerID());
	}

	/**
	 * Method used to initialize the acknowledgment HashSet of a new message
	 * @param msg for which to initialize the ack
	 */
	public static synchronized void initAck(Message msg) {
		msgAck.put(msg, new HashSet<Integer>());
	}

	/**
	 * 
	 * @param msg
	 * @return HashSet containing all acknowledgement for the given message
	 */
	public static HashSet<Integer> getAck(Message msg) {
		return msgAck.get(msg);
	}

    /**
     * Getter for the Socket of the Process
     * @return socket
     */
    public static DatagramSocket getSocket() {
        return socket;
    }

    /**
     * Getter for the Peers of the process
     * @return peers
     */
    public static ArrayList<Peer> getPeers() {
        return peers;
    }
}
