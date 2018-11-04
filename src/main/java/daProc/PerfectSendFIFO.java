package daProc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.security.cert.CRLReason;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;

import utils.Message;
import utils.Peer;

public class PerfectSendFIFO extends PerfectSendThread {

	public PerfectSendFIFO(Message msg, ArrayList<Peer> peers, DatagramSocket socket) {
		super(msg, peers, socket);
	}

	@Override
	public void run() {
		boolean firstRun = true;
		byte[] sendBuffer;

		// Wait for the broadcast of the previous message to begin
		while (FIFOBroadcast.getAck(new Message(message.getOrigin(), message.getSn() - 1, FIFOBroadcast.id)) == null
				&& message.getSn() != 1) {
			try {
				Thread.sleep(50);
				continue;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		message.setPeerID(FIFOBroadcast.id);
		sendBuffer = (Integer.toString(message.getOrigin()) + ":" + Integer.toString(message.getSn()) + ":"
				+ Integer.toString(message.getPeerID())).getBytes();

		// Thread will be crashed/stopped from the main
		// while the thread runs, send the message corresponding to the current seq_num
		// of the parent process
		while (true) {

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
			// TODO: Without the check for FIFOBroadcast.getAck(message) == null the
			// broadcast order gets messed up from time to time. Why? Error somewhere else?
			if (firstRun && FIFOBroadcast.getAck(message) == null) {
				if (message.getOrigin() == FIFOBroadcast.id) {
					Process.writeLogLine("b " + message.getSn());
				}
				FIFOBroadcast.addPending(message);
				FIFOBroadcast.initAck(message);
				FIFOBroadcast.addAck(message);
				firstRun = false;
			}
			// slow down the infinite thread
			try {
				Thread.sleep(sleep_time);
				sleep_time = 2 * sleep_time;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

}