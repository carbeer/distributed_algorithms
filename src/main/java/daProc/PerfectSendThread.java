package daProc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.logging.Level;

import utils.Message;
import utils.Peer;

/**
 * This class implements the perfect link logic as a thread for each message to
 * send. The thread is put to sleep more and more time after each attempt to
 * broadcast the message so not to waste resources, but comply with the
 * theoritical bases.
 *
 */
public class PerfectSendThread extends Thread {

	DatagramSocket socket;
	ArrayList<Peer> peers;
	Message message;
	int sleepTime = 10;

	/**
	 * Constructor of the thread
	 * 
	 * @param msg    : message to broadcast
	 * @param peers  : list of peers to broadcast the message to
	 * @param socket : socket to send the message from
	 */
	public PerfectSendThread(Message msg, ArrayList<Peer> peers, DatagramSocket socket) {
		this.message = msg.clone();
		this.peers = peers;
		this.socket = socket;
		// FIFOBroadcast.LOGGER.log(Level.FINE, "Instantiating PerfectSendThread");
	}

	public void run() {
		byte[] sendBuffer;

		// While the thread runs, send the message corresponding to the current seq_num
		// of the parent process
		while (true) {

			// Format the data to be sent and extract it from the Message data structure
			sendBuffer = (Integer.toString(message.getOrigin()) + ":" + Integer.toString(message.getSn())).getBytes();

			// Send a packet containing the data of the message to send to all peers
			for (Peer peer : this.peers) {
				DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, peer.inetAddress, peer.port);
				try {
					socket.send(packet);
				} catch (java.net.SocketException e) {
					Process.LOGGER.log(Level.WARNING, "Cannot send the message, the socket is closed");
				} catch (java.io.IOException e) {
					Process.LOGGER.log(Level.WARNING, "Error while sending DatagramPacket");
					e.printStackTrace();
				}
			}
			
			//TODO should only log the broadcast of the messages that are our own no?
			// Log the broadcast of the message
			Process.writeLogLine("b " + message.getSn());

			// Slow down the infinite thread
			try {
				Thread.sleep(sleepTime);
				sleepTime = 2 * sleepTime;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
}
