package utils;

// Custom classes
public class Message {
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