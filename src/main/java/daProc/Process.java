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
	private DatagramSocket socket;
	private boolean crashed;
	private int seqNumber;
	private ArrayList<Peer> peers;
	// Never use the FileWriter until the process crashes.
	private FileWriter writer;
	// Add logs here.
	private String logs;

	public Process(int id, String membership) {
		this.id = id;
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
	

	public void send() {
		byte[] sendBuffer;
		while (!this.crashed) {
			sendBuffer = Integer.toString(seqNumber).getBytes();
			for (Peer peer : this.peers) {
				DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, peer.inetAddress, peer.port);
				System.out.printf("Sent message with seqNr %s to peer %s", seqNumber, peer.getIpPort());
				try {
					socket.send(packet);
				} catch(java.io.IOException e) {
					System.out.println("Error while sending DatagramPacket");
					e.printStackTrace();
				}
			}
			this.logs = String.format("%sb %s\n", this.logs, seqNumber);
			System.out.println("Broadcast: b " + seqNumber );
			seqNumber++;
		}
	}

	// TODO: Check prevention of receiving messages after crash() method is called
	public void receive() {
		while (!this.crashed) {
			byte[] receiveBuffer = new byte[256];
			DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
			try {
				// Blocking call
				socket.receive(packet);
				// Message was read after crash() was called --> Do not write it anymore
				if (this.crashed) {
					break;
				}

				// Receive address and port of sender
				// InetAddress address = packet.getAddress();
				// int port = packet.getPort();

				// Read data from packet
				String receivedSeqNr = new String(packet.getData(), 0, packet.getLength());
				this.logs = String.format("%sd %s:%d %s\n", this.logs, packet.getAddress().toString(), packet.getPort(), receivedSeqNr);
				System.out.println("Received packet: " + String.format("d %s:%d %s", packet.getAddress().toString(), packet.getPort(), receivedSeqNr));

			} catch (java.io.IOException e) {
				System.out.println("Error while receiving DatagramPacket");
				e.printStackTrace();
			}
		}
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
    	int processId = Integer.valueOf(args[1]);
    	String membership = args[2];
    	final Process process = new Process(processId, membership);

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
