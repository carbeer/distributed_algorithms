package daProc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import utils.Message;
import utils.Peer;

//this class corresponds to perfect link broadcast that will send the message indefinitely
public class PerfectSendThread extends Thread {

	private DatagramSocket socket;
	private ArrayList<Peer> peers;
	Message message;
	
	public PerfectSendThread(Message msg, ArrayList<Peer> peers, DatagramSocket socket) {
		this.message = msg;
		this.peers = peers;
		this.socket = socket;
	}
	
    public void run() {
        byte[] sendBuffer;
        int sleep_time = 1000;

        //Thread will be crashed/stopped from the main
        //while the thread runs, send the message corresponding to the current seq_num of the parent process
        while (true) {
            sendBuffer = (Integer.toString(message.getSender()) + ":" + Integer.toString(message.getSn())).getBytes();

            for (Peer peer : this.peers) {
                DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, peer.inetAddress, peer.port);
                try {
                    socket.send(packet);
                } catch(java.io.IOException e) {
                    System.out.println("Error while sending DatagramPacket");
                    e.printStackTrace();
                }
            }
            Process.writeLogLine ("b " + message.getSn());

            //slow down the infinite thread
        	try {
				Thread.sleep(sleep_time);
	        	sleep_time = 2*sleep_time;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
        }
    }
}
