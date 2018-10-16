package daProc;

import java.net.DatagramPacket;

public class SenderThread {
    // TODO: Make it work for Threads (consistency, crash handling)
    public void run() {
        byte[] sendBuffer;
        while (!this.crashed) {
            sendBuffer = Integer.toString(seqNumber).getBytes();
            for (Process.Peer peer : this.peers) {
                DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, peer.inetAddress, peer.port);
                System.out.printf("Sent message with seqNr %s to peer %s", seqNumber, peer.getIpPort());
                try {
                    socket.send(packet);
                } catch(java.io.IOException e) {
                    System.out.println("Error while sending DatagramPacket");
                    e.printStackTrace();
                }
            }
            this.logs = String.format("%sb %s\n", this.logs, seqNumber);
            System.out.println("Broadcast: b " + seqNumber );
            seqNumber++;
        }
    }
}
