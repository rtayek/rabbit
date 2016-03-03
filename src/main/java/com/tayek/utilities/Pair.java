package com.tayek.utilities;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
public class Pair<First,Second> {
    public Pair(First first,Second second) {
        this.first=first;
        this.second=second;
    }
    @Override public int hashCode() {
        final int prime=31;
        int result=1;
        result=prime*result+((first==null)?0:first.hashCode());
        result=prime*result+((second==null)?0:second.hashCode());
        return result;
    }
    @Override public boolean equals(Object obj) {
        if(this==obj) return true;
        if(obj==null) return false;
        if(getClass()!=obj.getClass()) return false;
        @SuppressWarnings("unchecked") Pair<First,Second> other=(Pair<First,Second>)obj;
        if(first==null) {
            if(other.first!=null) return false;
        } else if(!first.equals(other.first)) return false;
        if(second==null) {
            if(other.second!=null) return false;
        } else if(!second.equals(other.second)) return false;
        return true;
    }
    @Override public String toString() {
        return "["+first+","+second+"]";
    }
    public static Pair<InetAddress,Integer> from(MulticastSocket socket) {
        return new Pair<>(socket.getInetAddress(),socket.getLocalPort());
    }
    public static Pair<InetAddress,Integer> from(ServerSocket socket) {
        return new Pair<>(socket.getInetAddress(),socket.getLocalPort());
    }
    public static Pair<InetAddress,Integer> from(Socket socket) {
        return new Pair<>(socket.getInetAddress(),socket.getLocalPort());
    }
    public static Pair<InetAddress,Integer> from(DatagramPacket packet) {
        return new Pair<>(packet.getAddress(),packet.getPort());
    }
    
    public final First first;
    public final Second second;
}
