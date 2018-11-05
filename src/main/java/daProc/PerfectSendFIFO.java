package daProc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.logging.Level;

import utils.Message;
import utils.Peer;

/**
 * This class implements the FIFO broadcast logic on top of the perfect link
 * logic as a thread for each message to send. It ensure the messages are sent
 * in order, and that no messages are processed as soon as the socket is closed,
 * or that the process has been crashed. The thread is put to sleep more and
 * more time after each attempt to broadcast the message so not to waste
 * resources, but comply with the theoritical bases.
 *
 */
public class PerfectSendFIFO extends PerfectSendThread {

	public PerfectSendFIFO(Message msg, ArrayList<Peer> peers, DatagramSocket socket) {
		super(msg, peers, socket);
	}

	@Override
	public void run() {
		boolean firstRun = true;
		byte[] sendBuffer;

		// Wait for the broadcast of the previous message to begin sending the next one
		while (message.getSn() != 1 && (FIFOBroadcast
				.getAck(new Message(message.getOrigin(), message.getSn() - 1, FIFOBroadcast.id)) == null
				|| FIFOBroadcast.getAck(new Message(message.getOrigin(), message.getSn() - 1, FIFOBroadcast.id))
						.isEmpty())) {
			try {
				Thread.sleep(5);
				continue;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Format the data to be sent and extract it from the Message data structure
		message.setPeerID(FIFOBroadcast.id);
		sendBuffer = (Integer.toString(message.getOrigin()) + ":" + Integer.toString(message.getSn()) + ":"
				+ Integer.toString(message.getPeerID())).getBytes();

		// While the thread runs, send the message corresponding to the current seq_num
		// of the parent process
		while (true) {

			// Send a packet containing the data of the message to send to all peers
			for (Peer peer : this.peers) {
				DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, peer.inetAddress, peer.port);
				try {
					if (!(FIFOBroadcast.crashed)) {
						socket.send(packet);
					}
				} catch (java.net.SocketException e) {
					Process.LOGGER.log(Level.WARNING,
							"Cannot send the message, the socket is closed for process " + FIFOBroadcast.id);
				} catch (java.io.IOException e) {
					Process.LOGGER.log(Level.WARNING, "Error while sending DatagramPacket");
					e.printStackTrace();
				}
			}

			// Acknowledge the messages we send if this is the first attempt at sending it
			if (firstRun) {
				if (FIFOBroadcast.id == message.getOrigin()) {
					Process.writeLogLine("b " + message.getSn());
				}
				FIFOBroadcast.addAck(message);
				firstRun = false;
			}

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