package daProc;

import utils.Message;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class FIFOReceiverThread handles the reception of datagrams through the
 * socket of the process. It updates the relevant variable for the FIFO delivery
 * logic. It ensure that no messages are processed as soon as the socket is
 * closed, or that the process has been crashed.
 */
public class FIFOReceiverThread extends Thread {

	public FIFOBroadcast process;

	public FIFOReceiverThread(FIFOBroadcast process) {
		//Process.LOGGER.log(Level.FINE, "Creating instance of FIFOReceiverThread now");
		this.process = process;
	}

	public void run() {
		
		// Initialize the receiving socket
		DatagramSocket socket = process.getSocket();
		byte[] receiveBuffer;

		// Processes datagrams at the buffer as long as the process is live
		while (!process.crashed) {
			receiveBuffer = new byte[256];
			DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
			try {
				socket.receive(packet);
				// Check whether the process is still alive and is allowed to receive the new
				// packet.
				if (process.crashed) {
					break;
				}
				Message message = new Message(packet);
				process.receiveHandler(message);
			} catch (java.net.SocketException e) {
				Process.LOGGER.log(Level.WARNING, "Error while receiving DatagramPacket");
				e.printStackTrace();

			} catch (java.io.IOException e) {
				Process.LOGGER.log(Level.WARNING, "Error while receiving DatagramPacket");
				e.printStackTrace();
			}
		}
	}
}
