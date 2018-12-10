import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;

/**
 * Class FIFOReceiver handles the reception of datagrams through the
 * socket of the process. It updates the relevant variable for the FIFO delivery
 * logic. It ensure that no messages are processed once the process has been crashed.
 */
public class FIFOReceiver extends Thread {

	public void run() {
		// Initialize the receiving socket
		DatagramSocket socket = FIFOBroadcast.getSocket();
		byte[] receiveBuffer;

		// Processes datagrams at the buffer as long as the process is live
		while (!FIFOBroadcast.crashed) {
			receiveBuffer = new byte[256];
			DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
			try {
				socket.receive(packet);
				// Check whether the process is still alive and is allowed to receive the new
				// packet.
				if (FIFOBroadcast.crashed) {
					break;
				}
				Message message = new Message(packet);
				FIFOBroadcast.receiveHandler(message);
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
