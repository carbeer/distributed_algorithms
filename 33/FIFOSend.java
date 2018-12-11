package daProc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

import utils.Message;
import utils.Peer;

/**
 * This class implements the FIFO broadcast logic on top of the perfect link
 * logic as a thread for each message to send.
 *
 */
public class FIFOSend extends Thread {

	@Override
	public void run() {
		while (true) {
			// If the broadcast has been triggered, and that we can still send messages,
			// send all messages possible.
			if (FIFOBroadcast.nrMessages >= FIFOBroadcast.seqNumber) {
				Message message = new Message(FIFOBroadcast.id, FIFOBroadcast.seqNumber, FIFOBroadcast.id);
				FIFOBroadcast.initAck(message);
				FIFOBroadcast.addPending(message);
				// Wait for the broadcast of the previous message until starting the broadcast
				while (message.getSn() != 1
						&& !FIFOBroadcast.getAck(new Message(message.getOrigin(), message.getSn() - 1, FIFOBroadcast.id)).contains(FIFOBroadcast.id)) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				// Set message dependencies of the process

				// TODO verify that this does not confilcts with retransmitting messages that are not our own!!
				// Update : it should not, since we broadcast messages that are not our our through perfect links and not 
				// this method.
				// @Carolin, could you double check?
				message = FIFOBroadcast.setMessageDependencies(message);

				// Start broadcast of the message once the previous message has been broadcast
				FIFOBroadcast.broadcast(message);
				FIFOBroadcast.seqNumber++;
			} else {
				break;
			}
		}
	}
}