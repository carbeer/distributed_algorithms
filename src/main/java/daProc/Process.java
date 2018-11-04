package daProc;

import java.io.*;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.misc.Signal;
import sun.misc.SignalHandler;
import utils.Message;
import utils.Peer;

@SuppressWarnings("restriction")
public class Process {
	static int id;
	static String ip;
	final int port;
	static boolean start_broadcast;
	final String[] extraParams;
	static DatagramSocket socket;
	static volatile boolean crashed = false;
	int seqNumber = 1;
	ArrayList<Peer> peers;
	private static volatile HashMap<Message, HashSet<Integer>> msgAck = new HashMap<>();;
	static private String logs = "";
	static FileWriter writer;
	public final static Logger LOGGER = Logger.getLogger(Process.class.getName());

	@SuppressWarnings("restriction")
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
		File membershipPath = new File(System.getProperty("user.dir") + File.separator + membership);
		String[] processParam = readMembership(membershipPath, id);
		this.ip = processParam[1];
		this.port = Integer.valueOf(processParam[2]);
		this.peers = getPeers(membershipPath, id);
		try {
			this.socket = new DatagramSocket(this.port);
		} catch (java.net.SocketException e) {
			// Doesn't make sense to be handled
			throw e;
		}
		try {
			File directory = new File(System.getProperty("user.dir"));
			writer = new FileWriter(directory + File.separator + "da_proc_" + this.id + ".out");
		} catch (java.io.IOException e) {
			System.out.println("Error while creating the file writer");
			e.printStackTrace();
		}

		// Signal handlers
		Signal.handle(new Signal("USR2"), new SignalHandler() {
			public void handle(Signal sig) {
				LOGGER.log(Level.INFO, "Process " + id + " Received USR2 signal. Starting the broadcast.");
				start_broadcast = true;
			}
		});

		Signal.handle(new Signal("INT"), new SignalHandler() {
			public void handle(Signal sig) {
				//LOGGER.log(Level.INFO, "Process " + id + " Received INT signal");
				crash();
			}
		});

		Signal.handle(new Signal("TERM"), new SignalHandler() {
			public void handle(Signal sig) {
				//LOGGER.log(Level.INFO, "Process " + id + " Received TERM signal");
				crash();
			}
		});
	}

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

	public static void writeLogsToFile() {
		try {
			writer.write(logs);
			logs = "";
			//LOGGER.log(Level.INFO, "Wrote logs.");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// Allow only one Thread to write at a time
	public static synchronized void writeLogLine(String s) {
		if (!crashed) {
			logs = logs + s + "\n";
			System.out.println(s);
		}
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public ArrayList<Peer> getPeers() {
		return peers;
	}

	// TODO: Merge with readMembership
	// Parses the peers from the membership file
	public ArrayList<Peer> getPeers(File f, int procID) {
		ArrayList<Peer> initPeers = new ArrayList<Peer>();
		try {
			BufferedReader b = new BufferedReader(new FileReader(f));
			String line;
			String[] splitted;
			while ((line = b.readLine()) != null) {
				splitted = line.split("\\s+");
				if (Integer.valueOf(splitted[0]) != procID && splitted.length == 3) {
					initPeers.add(new Peer(splitted[1], Integer.valueOf(splitted[2]), Integer.valueOf(splitted[0])));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return initPeers;
	}

	// Parses the membership file
	public static String[] readMembership(File f, int procID) {
		String[] splitted;
		try {
			BufferedReader b = new BufferedReader(new FileReader(f));
			String readLine = "";

			while ((readLine = b.readLine()) != null) {
				String line = readLine;
				splitted = line.split("\\s+");
				if (Integer.valueOf(splitted[0]) == procID && splitted.length == 3) {
					return splitted;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isCrashed() {
		return crashed;
	}

	public static synchronized void addAck(Message msg) {
		msgAck.get(msg).add(msg.getPeerID());
	}

	public static synchronized void initAck(Message msg) {
		msgAck.put(msg, new HashSet<Integer>());
	}

	public static HashSet<Integer> getAck(Message msg) {
		return msgAck.get(msg);
	}
}
