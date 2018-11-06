package daProc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.logging.Level;

import utils.Message;
import utils.Peer;

/**
 * This class implements the FIFO broadcast logic on top of the perfect link
 * logic as a thread for each message to send. It ensures that messages are sent
 * in order, and that no messages are processed once the process has been crashed.
 * The thread is put to sleep for an increasing amount of time after each broadcast
 * of the message to minimize resource requirements, but comply with the theoretical bases.
 *
 */
public class PerfectSendFIFO extends PerfectSend {

	public PerfectSendFIFO(Message msg, ArrayList<Peer> peers, DatagramSocket socket) {
		super(msg, peers, socket);
	}

	@Override
	public void run() {
		// Wait for the broadcast of the previous message until starting the broadcast
		while (message.getSn() != 1 &&
				(FIFOBroadcast.getAck(new Message(message.getOrigin(), message.getSn() - 1, FIFOBroadcast.id)) == null
				|| FIFOBroadcast.getAck(new Message(message.getOrigin(), message.getSn() - 1, FIFOBroadcast.id)).isEmpty())) {
			try {
				Thread.sleep(5);
				continue;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Start broadcasting through PerfectLinks
		sendMessages();
	}
}