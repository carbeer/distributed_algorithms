package daProc;

import utils.Message;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

// This class corresponds to fifoReceiver that will deliver the messages according the the URB logic
public class FIFOReceiverThread extends Thread {

	final static Logger LOGGER = Logger.getLogger(FIFOBroadcast.class.getName());
	public FIFOBroadcast process;

	public FIFOReceiverThread(FIFOBroadcast process) {
		LOGGER.log(Level.INFO, "Creating instance of FIFOReceiverThread now");
		this.process = process;
	}


	public void run() {
		DatagramSocket socket = process.getSocket();
		byte[] receiveBuffer;

		while (!process.crashed) {
            receiveBuffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try {
               	socket.receive(packet);
               	// Check whether the process is still alive.
               	if (process.crashed) {
               		break;
				}
				Message message = new Message(packet);
				process.receiveHandler(message, packet.getAddress().toString());
            } catch (java.io.IOException e) {
                System.out.println("Error while receiving DatagramPacket");
                e.printStackTrace();
            }
        }
    }
}
