package utils;

import java.net.InetAddress;

public class Peer {
    public String address;
    public int port;
    public InetAddress inetAddress;

    public Peer(String address, int port) {
        this.address = address;
        this.port = port;
        try {
            inetAddress = InetAddress.getByName(this.address);
        } catch (java.net.UnknownHostException e) {
            System.out.println("Couldn't resolve address " + this.address);
            e.printStackTrace();
        }
    }

    public String getIpPort() {
        return this.address + ":" + this.port;
    }

    @Override()
    public boolean equals(Object obj) {
        if (obj instanceof Peer) {
            Peer peer = (Peer) obj;
            return peer.address.equals(this.address) && peer.port == this.port;
        }
        return false;
    }
}