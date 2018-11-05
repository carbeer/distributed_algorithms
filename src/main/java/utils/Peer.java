package utils;

import java.net.InetAddress;

/**
 * Datastructure representing a peer, and containing all useful variables depicting it
 *
 */
public class Peer {
    public String address;
    public int port;
    public InetAddress inetAddress;
    public int id;

    public Peer(String address, int port, int id) {
        this.address = address;
        this.port = port;
        this.id = id;
        try {
            inetAddress = InetAddress.getByName(this.address);
        } catch (java.net.UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override()
    public boolean equals(Object obj) {
        if (obj instanceof Peer) {
            Peer peer = (Peer) obj;
            return peer.address.equals(this.address) && peer.port == this.port && peer.id==this.id;
        }
        return false;
    }
}