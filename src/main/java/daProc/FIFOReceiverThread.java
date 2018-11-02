package daProc;

import utils.Message;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

// This class corresponds to fifoReceiver that will deliver the messages according the the URB logic
public class FIFOReceiverThread extends Thread {

	public FIFOBroadcast process;

	public FIFOReceiverThread(FIFOBroadcast process) {
		Process.LOGGER.log(Level.FINE, "Creating instance of FIFOReceiverThread now");
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
				process.receiveHandler(message);
            } catch (java.io.IOException e) {
				Process.LOGGER.log(Level.WARNING, "Error while receiving DatagramPacket");
                e.printStackTrace();
            }
        }
    }
}
