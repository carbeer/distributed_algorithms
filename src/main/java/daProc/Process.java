package daProc;

import java.util.ArrayList;

public class Process {
	
	//Messages sent but not acked yet
	private ArrayList<Message> sentMsg;
	//
	private int id;
	private int ip;
	private int port;
	private String outPath;
	
	public Process(int id, String membership) {
		
		
		this.id = id;
		//TODO Read membership and id port
		readMembership();
		
		//create outPath
		this.outPath = "da_proc_" + this.id;
	}
	
	
	
	public void send() {
		//TODO
	}
	public void receive() {
		//TODO
	}
	public void crash() {
		//TODO
	}
	public void readMembership () {
		//TODO
	}
	
	//Custom classes
    private class Message {
    	private int sender;
    	private int receiver;
    	private int sn;
    	
    	public Message(int s, int r, int n) {
    		sender = s;
    		receiver = r;
    		sn =n;
    	}
    	
    	public int getSender() {
    		return sender;	
    	}
    	public int getReceiver() {
    		return receiver;
    	}
    	public int getSn() {
    		return sn;
    	}
    }
}
