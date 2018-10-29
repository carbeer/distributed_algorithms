package utils;

import java.util.Objects;

public class Message {
    private int sender;
    private int sn;

    public Message(int sender, int sn) {
        this.sender = sender;
        this.sn = sn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return sender == message.sender &&
                sn == message.sn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, sn);
    }

    public int getSender() {
        return sender;
    }

    public int getSn() {
        return sn;
    }

}