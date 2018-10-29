package daProc;

import java.io.*;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;

import utils.Peer;


public class Process {
	final int id;
	final String ip;
	final int port;
	final String[] extraParams;
	DatagramSocket socket;
	static volatile boolean crashed;
	static volatile boolean start_sending;
	int seqNumber;
	ArrayList<Peer> peers;
	HashMap<String, Integer> idFromAddress;
	static private String logs;


	// Never use the FileWriter until the process crashes.
	FileWriter writer;
	// Add logs to write to here.

	public Process(String[] args) {
		// Parse command line arguments
		this.id = Integer.valueOf(args[1]);
		String membership = args[2];

		// Check whether extra parameters were given as command line arguments
		if (args.length > 2) {
			extraParams = new String[args.length-3];
			for (int i = 3; i < args.length; i++) {
				extraParams[i-3] = args[i];
			}
		} else {
			extraParams = null;
		}

		File membershipPath = new File(System.getProperty("user.dir") + "/src/main/java/daProc/" + membership + ".txt");
		String[] processParam = readMembership(membershipPath, id);
		this.ip = processParam[1];
		this.port = Integer.valueOf(processParam[2]);
		this.peers = getPeers(membershipPath, id);
		crashed = false;
		start_sending = false;
		this.seqNumber = 1;

		try {
			this.socket = new DatagramSocket(this.port);
		} catch (java.net.SocketException e) {
			System.out.println("Error while creating a socket at port " + this.port);
			e.printStackTrace();
		}
		try {
			this.writer = new FileWriter(System.getProperty("user.dir") + "/logs/da_proc_" + this.id);
		} catch (java.io.IOException e) {
			System.out.println("Error while creating the file writer");
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

	public void setSocket(DatagramSocket socket) {
		this.socket = socket;
	}

	public boolean isCrashed() {
		return crashed;
	}

	public int getSeqNumber() {
		return seqNumber;
	}

	public void setSeqNumber(int seqNumber) {
		this.seqNumber = seqNumber;
	}

	public ArrayList<Peer> getPeers() {
		return peers;
	}

	public void setPeers(ArrayList<Peer> peers) {
		this.peers = peers;
	}

	public int getId() {
		return id;
	}

	public FileWriter getWriter() {
		return writer;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	//set crash to true
	public void crash() {
		this.socket.close();
	}
	
	//HELPERS

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
