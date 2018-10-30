package utils;

import java.util.Objects;

public class Message {
    private int initial_sender;
    private int sn;

    public Message(int sender, int sn) {
        this.initial_sender = sender;
        this.sn = sn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return initial_sender == message.initial_sender &&
                sn == message.sn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(initial_sender, sn);
    }

    public int getSender() {
        return initial_sender;
    }

    public int getSn() {
        return sn;
    }

}