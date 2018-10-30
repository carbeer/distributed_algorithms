package daProc;

import java.io.*;
import java.lang.reflect.Array;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import utils.Message;
import utils.Peer;


public class Process {
	final int id;
	final String ip;
	final int port;
	final String[] extraParams;
	static DatagramSocket socket;
	static volatile boolean crashed;
	int seqNumber;
	ArrayList<Peer> peers;
	// Lock for msgAck
	private static final Lock lock = new ReentrantLock();
	static volatile HashMap<Message, ArrayList<Integer>>  msgAck;
	HashMap<String, Integer> idFromAddress;
	static private String logs;
	static FileWriter writer;
	final static Logger LOGGER = Logger.getLogger(FIFOBroadcast.class.getName());

	public Process(String[] args) {
		// Parse command line arguments
		this.id = Integer.valueOf(args[0]);
		String membership = args[1];

		// Check whether extra parameters were given as command line arguments
		if (args.length > 2) {
			extraParams = new String[args.length-2];
			for (int i = 2; i < args.length; i++) {
				extraParams[i-2] = args[i];
			}
		} else {
			extraParams = null;
		}

		this.idFromAddress = new HashMap<>();
		File membershipPath = new File(System.getProperty("user.dir") + "\\" + membership);
		String[] processParam = readMembership(membershipPath, id);
		this.ip = processParam[1];
		this.port = Integer.valueOf(processParam[2]);
		this.peers = getPeers(membershipPath, id);
		crashed = false;
		this.seqNumber = 1;

		try {
			this.socket = new DatagramSocket(this.port);
		} catch (java.net.SocketException e) {
			System.out.println("Error while creating a socket at port " + this.port);
			e.printStackTrace();
		}
		try {
			this.writer = new FileWriter(System.getProperty("user.dir") + "/logs/da_proc_" + this.id + ".txt");
		} catch (java.io.IOException e) {
			System.out.println("Error while creating the file writer");
			e.printStackTrace();
		}
	}

	public static Lock getLock() {
		return lock;
	}

	public static void crash() {
		LOGGER.log(Level.SEVERE, "Process has been crashed, cleaning up.");
		crashed = true;
		writeLogsToFile();
		socket.close();
		LOGGER.log(Level.SEVERE, "Exiting now.");
		System.exit(0);
	}

	public static void writeLogsToFile() {
		try {
			writer.write(logs);
			logs = "";
			LOGGER.log(Level.INFO, "Wrote logs.");
		} catch(IOException e) {
			e.printStackTrace();
		}

	}

	public static void writeLogLine(String s) {
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

	public int getId() {
		return id;
	}

	// TODO: Merge with readMembership
	// Parses the peers from the membership file and create idFromAddress HashMap
	public ArrayList<Peer> getPeers(File f, int procID) {
		ArrayList<Peer> initPeers = new ArrayList<Peer>();
		try{
			BufferedReader b = new BufferedReader(new FileReader(f));
			String line;
			String[] splitted;
			while ((line = b.readLine()) != null) {
				splitted = line.split("\\s+");
				if (Integer.valueOf(splitted[0]) != procID && splitted.length == 3) {
					this.idFromAddress.put(splitted[1], Integer.parseInt(splitted[0]));
					initPeers.add(new Peer(splitted[1], Integer.valueOf(splitted[2])));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return initPeers;
	}
	
	//Parses the membership file
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
}
