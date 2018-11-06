package daProc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

import utils.Message;
import utils.Peer;

/**
 * This class implements the perfect link logic as a thread for each message to send.
 * The thread is put to sleep for an increasing amount of time after each broadcast
 * of the message to minimize resource requirements, but comply with the theoretical bases.
 *
 */
public class PerfectSend extends Thread {

	DatagramSocket socket;
	ArrayList<Peer> peers;
	Message message;
	int sleepTime = 10;
	boolean firstRun;

	/**
	 * Constructor of the thread
	 * 
	 * @param msg    : message to broadcast
	 * @param peers  : list of peers to broadcast the message to
	 * @param socket : socket to send the message from
	 */
	public PerfectSend(Message msg, ArrayList<Peer> peers, DatagramSocket socket) {
		this.message = msg.clone();
		this.peers = peers;
		this.socket = socket;
		this.firstRun = true;
	}

	public void run() {
		sendMessages();
	}

	void sendMessages() {
		byte[] sendBuffer;
		// Format the data to be sent and extract it from the Message data structure
		message.setPeerID(FIFOBroadcast.id);
		sendBuffer = (Integer.toString(message.getOrigin()) + ":" + Integer.toString(message.getSn()) + ":"
				+ Integer.toString(message.getPeerID())).getBytes();

		// While the thread runs, send the message corresponding to the current sequence number
		// of the parent process
		while (true) {
			if (FIFOBroadcast.startBroadcast) {
				// Send a packet containing the data of the message to send to all peers
				for (Peer peer : this.peers) {
					DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, peer.inetAddress, peer.port);
					try {
						if (!(FIFOBroadcast.crashed)) {
							socket.send(packet);
						} else {
							return;
						}
					} catch (java.net.SocketException e) {
						Process.LOGGER.log(Level.WARNING,
								"Cannot send the message, the socket is closed for process " + FIFOBroadcast.id);
					} catch (java.io.IOException e) {
						Process.LOGGER.log(Level.WARNING, "Error while sending DatagramPacket");
						e.printStackTrace();
					}
				}
				logBroadcast();

				// Slow down the infinite thread
				try {
					Thread.sleep(sleepTime);
					sleepTime = 2 * sleepTime;
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} else {
				// Wait for the broadcast to begin
				try {
					Thread.sleep(5);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * Log the broadcast message if this is the first attempt at sending it
	 */
	void logBroadcast() {
		if (this.firstRun) {
			if (message.getOrigin() == FIFOBroadcast.id) {
				Process.writeLogLine("b " + message.getSn());
			}
			FIFOBroadcast.addAck(message);
			this.firstRun = false;
		}
	}
}
