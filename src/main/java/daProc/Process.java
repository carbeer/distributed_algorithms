package daProc;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Process {
	
	//Messages sent but not acked yet
	private ArrayList<Message> sentMsg;
	//
	final private int id;
	final private String ip;
	final private int port;
	final private String[] extraParams;
	private DatagramSocket socket;
	private boolean crashed;
	private int seqNumber;
	private ArrayList<Peer> peers;
	// Never use the FileWriter until the process crashes.
	private FileWriter writer;
	// Add logs here.
	private String logs;

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
		this.logs = "";
		this.peers = getPeers(membershipPath, id);
		this.crashed = false;
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
		// receive();
	}

	// TODO: Check prevention of receiving messages after crash() method is called
	public void receive() {
	}

	//TODO Anything else necessary here?
	public void crash() {
		this.crashed = true;
		this.socket.close();
		try {
			writer.write(logs);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<Peer> getPeers(File f, int procID) {
		ArrayList<Peer> initPeers = new ArrayList<Peer>();
		try{
			BufferedReader b = new BufferedReader(new FileReader(f));
			String line;
			String[] splitted;
			while ((line = b.readLine()) != null) {
				splitted = line.split("\\s+");
				if (Integer.valueOf(splitted[0]) != procID && splitted.length == 3) {
					initPeers.add(new Peer(splitted[1], Integer.valueOf(splitted[2])));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return initPeers;
	}

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
	
	// Custom classes
    private class Message {
    	private int sender;
    	private int receiver;
    	private int sn;
    	
    	public Message(int s, int r, int n) {
    		sender = s;
    		receiver = r;
    		sn = n;
    	}
    	
    	public int getSender() {
    		return sender;	
    	}
    	public int getReceiver() {
    		return receiver;
    	}
    	public int getSn() {
    		return sn;
    	}
    }

	static class Peer {
		public String address;
		public int port;
		public InetAddress inetAddress;

		public Peer(String address, int port) {
			this.address = address;
			this.port = port;
			try {
				inetAddress = InetAddress.getByName(this.address);
			} catch (java.net.UnknownHostException e) {
				System.out.println("Couldn't resolve address " + this.address);
				e.printStackTrace();
			}
		}

		public String getIpPort() {
			return this.address + ":" + this.port;
		}

		@Override()
		public boolean equals(Object obj) {
			if (obj instanceof Peer) {
				Peer peer = (Peer) obj;
				return peer.address.equals(this.address) && peer.port == this.port;
			}
			return false;
		}
	}
    
    public static void main(String []args) {
    	final Process process = new Process(args);

    	Signal.handle(new Signal("SIGINT"), new SignalHandler() {
            public void handle(Signal sig) {
                System.out.println("Stopping network packet processing...\n");
                process.crash(); //clean up using crash method
                System.out.println("Teardown complete, exiting now.\n");
            	System.exit(0); //kill process
            }
        });
    	
    	Signal.handle(new Signal("SIGUSR2"), new SignalHandler() {
            public void handle(Signal sig) {
                System.out.println("Sending message!\n");
            	//send(); //broadcast one message
            }
        });
    }
}
