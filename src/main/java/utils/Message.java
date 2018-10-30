package utils;

import java.net.DatagramPacket;
import java.util.Comparator;
import java.util.Objects;

public class Message implements Comparator<Message> {
    // Id of the process that broadcast the message
    private int originId;
    private int sn;

    public Message(int originId, int sn) {
        this.originId = originId;
        this.sn = sn;
    }

    public Message(DatagramPacket packet) {
        // Read data from packet
        String[] splitted = packet.getData().toString().split(":");

        // Get data from packet
        this.originId = Integer.parseInt(splitted[0]);
        this.sn = Integer.parseInt(splitted[1]);
    }

    @Override
    public int compare(Message o1, Message o2) {
        // TODO: Compare by sequenceNumber to make it feasible for the PriorityQueue. https://www.callicoder.com/java-priority-queue/
        return 0;
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

}