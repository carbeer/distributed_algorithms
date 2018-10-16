package daProc;

import java.net.DatagramPacket;

public class ReceiverThread implements Runnable {
    // TODO: Make it work for Threads (consistency, crash handling)
    public void run() {
        while (!this.crashed) {
            byte[] receiveBuffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try {
                // Blocking call
                socket.receive(packet);
                // Message was read after crash() was called --> Do not write it anymore
                if (this.crashed) {
                    break;
                }

                // Receive address and port of sender
                // InetAddress address = packet.getAddress();
                // int port = packet.getPort();

                // Read data from packet
                String receivedSeqNr = new String(packet.getData(), 0, packet.getLength());
                this.logs = String.format("%sd %s:%d %s\n", this.logs, packet.getAddress().toString(), packet.getPort(), receivedSeqNr);
                System.out.println("Received packet: " + String.format("d %s:%d %s", packet.getAddress().toString(), packet.getPort(), receivedSeqNr));

            } catch (java.io.IOException e) {
                System.out.println("Error while receiving DatagramPacket");
                e.printStackTrace();
            }
        }
    }
}
