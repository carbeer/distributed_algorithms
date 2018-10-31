package utils;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class Message implements Comparable<Message> {
    // Id of the process that broadcast the message
    private int originId;
    private int sn;

    public Message(int originId, int sn) {
        this.originId = originId;
        this.sn = sn;
    }

    public Message(DatagramPacket packet) {
        // Read data from packet
        String[] splitted = new String(packet.getData()).split(":");
        System.out.println("Received a new message: " + splitted[0] + " " + splitted[1].trim());
        // Get data from packet
        this.originId = Integer.parseInt(splitted[0].trim());
        this.sn = Integer.parseInt(splitted[1].trim());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return originId == message.originId &&
                sn == message.sn;
    }

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

    @Override
    public int compareTo(Message o) {
        // TODO: Compare by sequenceNumber to make it feasible for the PriorityQueue. https://www.callicoder.com/java-priority-queue/
        return 0;
    }
}