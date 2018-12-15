package utils;

import daProc.FIFOBroadcast;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.logging.Level;

public class Message implements Comparable<Message> {
    // Id of the process that broadcast the message
    private int originId;
    private int sn;
    private int peerID;
    // TODO: Possibly change to static array to improve performance
    // Problem with dependencies
    private ArrayList<Message> dependencies;

    public Message(int originId, int sn, int peerID) {
        this.originId = originId;
        this.sn = sn;
        this.peerID = peerID;
        this.dependencies = new ArrayList<Message>();
    }

    public Message(DatagramPacket packet) {
        // Read data from packet
        String[] splitted = new String(packet.getData()).split(":");
        FIFOBroadcast.LOGGER.log(Level.FINE, "Received a message: " + splitted[0].trim() + " " + splitted[1].trim());
        // Get data from packet
        this.originId = Integer.parseInt(splitted[0].trim());
        this.sn = Integer.parseInt(splitted[1].trim());
        this.peerID = Integer.parseInt(splitted[2].trim());
        
        // Assumed packet = origin_id:sn:peerID:originId(dependency1):message_seq(dependency1):originId(dependency2):message_seq(dependency2)
        ArrayList<Message> dep = new ArrayList<Message>();
        if (splitted.length > 3) {
            for (int i = 3; i < splitted.length; i = i + 2){
                Message temp = new Message(Integer.parseInt(splitted[i].trim()), Integer.parseInt(splitted[i + 1].trim()), 0);
                dep.add(temp);
            }
        }
        this.dependencies = dep;
    }

    /**
     * This method compares to messages to each other.
     * IMPORTANT: The peerID is not taken into consideration for this comparison, this is intended!
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return (originId == message.originId &&
                sn == message.sn);
    }

    /**
     * IMPORTANT: The peerID is not taken into consideration for the hash, this is intended!
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(originId, sn);
    }

    public int getOrigin() {
        return originId;
    }

    public int getSn() {
        return sn;
    }

    public int getPeerID() {
        return peerID;
    }

    public void setPeerID(int peerID) {
        this.peerID = peerID;
    }

    public void setDependencies(ArrayList<Message> dependencies) {
        this.dependencies = dependencies;
    }

    public ArrayList<Message> getDependencies() {
        return dependencies;
    }

    public String getMessageContent() {
        String cont = String.valueOf(originId);
        cont = cont.concat(":" + String.valueOf(this.sn));
        cont = cont.concat(":" + String.valueOf(this.peerID));

        // :originId(dependency1):message_seq(dependency1):originId(dependency2):message_seq(dependency2)
        // Or change receiving structure in the class Message
        for (Message msg : this.dependencies) {
            cont = cont.concat(":" + msg.getOrigin());
            cont = cont.concat(":" + msg.getSn());
        }
        return cont;
    }


    @Override
    public int compareTo(Message o) {
        if (this.getSn() < o.getSn()) {
            return  -1;
        } else if (this.getSn() > o.getSn()) {
            return 1;
        }
        return 0;
    }

    @Override
    public Message clone() {
        return new Message(originId, sn, peerID);
    }
}