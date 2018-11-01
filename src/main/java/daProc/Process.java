package daProc;

import java.io.*;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.misc.Signal;
import sun.misc.SignalHandler;
import utils.Message;
import utils.Peer;


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
	// Lock for msgAck
	private static final Lock lock = new ReentrantLock();
	private static volatile HashMap<Message, HashSet<String>>  msgAck = new HashMap<>();;
	static private String logs = "";
	static FileWriter writer;
	final static Logger LOGGER = Logger.getLogger(FIFOBroadcast.class.getName());

	public Process(String[] args) throws Exception {
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
			File directory = new File(System.getProperty("user.dir") + File.separator + "logs");
			if (! directory.exists()) {
				directory.mkdir();
			}
			writer = new FileWriter(directory + File.separator + "da_proc_" + this.id + ".txt");
		} catch (java.io.IOException e) {
			System.out.println("Error while creating the file writer");
			e.printStackTrace();
		}

		// Signal handlers
		Signal.handle(new Signal("USR2"), new SignalHandler() {
			public void handle(Signal sig) {
				LOGGER.log(Level.INFO, "Received USR2 signal");
				start_broadcast = true;
			}
		});

		Signal.handle(new Signal("INT"), new SignalHandler() {
			public void handle(Signal sig) {
				LOGGER.log(Level.INFO, "Received INT signal");
				crash();
			}
		});

		Signal.handle(new Signal("TERM"), new SignalHandler() {
			public void handle(Signal sig) {
				LOGGER.log(Level.INFO, "Received TERM signal");
				crash();
			}
		});
	}

	public static Lock getLock() {
		return lock;
	}

	public static void crash() {
		LOGGER.log(Level.SEVERE, "Process has been crashed, cleaning up.");
		crashed = true;
		try {
            writeLogsToFile();
        } catch(Exception e) {
		    LOGGER.log(Level.SEVERE, "Couldn't write the logs... We're screwed!");
		    e.printStackTrace();
        } finally {
            socket.close();
            LOGGER.log(Level.SEVERE, "Exiting now.");
            System.exit(0);
        }
	}


	public static void writeLogsToFile() {
		try {
			writer.write(logs);
			logs = "";
			LOGGER.log(Level.INFO, "Wrote logs.");
			writer.close();
		} catch(IOException e) {
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
		try{
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

	public static boolean isCrashed() {
		return crashed;
	}
	
    public static synchronized void addAck(Message msg, String receivedFrom) {
    	msgAck.get(msg).add(receivedFrom);
    }
    
    public static synchronized void initAck(Message msg) {
    	msgAck.put(msg, new HashSet<>());
    }
    
    public static HashSet<String> getAck(Message msg) {
    	return msgAck.get(msg);
    }
    

	@SuppressWarnings("deprecation")
	class SigHandler implements SignalHandler {
		Process p;

		private SigHandler(Process p) {
			super();
			this.p = p;
		}

		@Override
		public void handle(Signal signal) {
			LOGGER.log(Level.INFO, "Handling signal: %s\n", signal.toString());

			switch(signal.getName()) {
				case "USR2":
					start_broadcast = true;
					break;
				case "TERM":
				case "INT":
					crash();
			}
		}
	}
}
