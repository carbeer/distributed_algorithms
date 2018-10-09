package daProc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import sun.misc.Signal;
import sun.misc.SignalHandler;
 

public class Process {
	
	//Messages sent but not acked yet
	private ArrayList<Message> sentMsg;
	//
	private int id;
	private String ip;
	private int port;
	private String outPath;
	
	public Process(int id, String membership) {
		
		
		this.id = id;
		File membershipPath = new File(System.getProperty("user.dir") + "/src/main/java/daProc/membership.txt");
		try {
			String[] processParam = readMembership(membershipPath, id);
			this.ip = processParam[1];
			this.port = Integer.valueOf(processParam[2]);
			
		} catch (Exception e) {
            e.printStackTrace();
		}
		
		//create outPath
		this.outPath = "da_proc_" + this.id;
	}
	
	
	
	public void send() {
		//TODO
	}
	public void receive() {
		//TODO
	}
	public static void crash() {
		//just to clean up buffers and log unlogged data/packets/messages before closing
		//TODO
	}
	
	public static String[] readMembership(File f, int procID) {

		String[] splited;
		
		try {

            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine = "";

            while ((readLine = b.readLine()) != null) {
            	String line = readLine;
            	splited = line.split("\\s+");
            	if (Integer.valueOf(splited[0]) == procID && splited.length == 3) {
            		return splited;
            	}
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
		return null;
		
	}
	
	//Custom classes
    private class Message {
    	private int sender;
    	private int receiver;
    	private int sn;
    	
    	public Message(int s, int r, int n) {
    		sender = s;
    		receiver = r;
    		sn = n;
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
 
    
    public static void main(String []args) {

    	Signal.handle(new Signal("SIGINT"), new SignalHandler() {
            public void handle(Signal sig) {
                System.out.println("Stopping network packet processing...\n");
            	//crash(); //clean up using crash method
                System.out.println("Changes logged, exiting now.\n");
            	System.exit(0); //kill process
            }
        });
    	
    	Signal.handle(new Signal("SIGUSR2"), new SignalHandler() {
            public void handle(Signal sig) {
                System.out.println("Sending message!\n");
            	//send(); //broadcast one message
            }
        });
    	
        while(true) {
            //execute program
        	//listen to incoming packets
        }
    }
}
