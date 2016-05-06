package com.tayek.experiment;
import static com.tayek.io.IO.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import com.tayek.*;
import com.tayek.tablet.Message;
import com.tayek.utilities.*;
public class XServer implements Runnable {
    XServer(String host,Integer service,Set<CPair<String,Integer>> set) throws IOException {
        this.host=host;
        this.service=service;
        InetAddress inetAddress=InetAddress.getByName(host);
        messageFactory=Message.instance.create(host,service,new Single<Integer>(0));
        serverSocket=new ServerSocket(service,100/* what should this be?*/,inetAddress);
        if(serverSocket==null) throw new RuntimeException("bind failed!");
        isShuttingDown=false;
    }
    @Override public void run() {
        p("running: "+id()+" accepting on: "+serverSocket);
        l.info("running: "+id()+" accepting on: "+serverSocket);
        while(!isShuttingDown) {
            try {
                Socket socket=serverSocket.accept();
                l.info("server: "+id()+" accepted connection from: "+socket);
                // we might be able to guess the ip and service?
                InetAddress iNetAddress=socket.getInetAddress();
                l.info("socket address: "+iNetAddress);
                Reader reader=new Reader(this,id(),null/* no id yet*/,socket,new Histories());
                synchronized(this) {
                    newConnections.add(reader);
                }
                reader.thread=new Thread(reader);
                reader.thread.setName(socket.getRemoteSocketAddress().toString()+"->"+id());
                reader.thread.start();
                // don't add yet, until we receive a message,so we know where he is listening.
            } catch(IOException e) {
                if(isShuttingDown) {
                    l.info("server: "+id()+" is shutting down");
                    if(e.toString().contains("socket closed")) l.info("0 (maybe normal) server: "+id()+" caught: "+e);
                    else if(e.toString().contains("Socket is closed")) l.info("0 (maybe normal) server: "+id()+" caught: "+e);
                    else l.info("1 server: "+id()+" caught: "+e);
                } else {
                    l.info("server: "+id()+"is not shutting down, server caught: "+e);
                    e.printStackTrace();
                }
                break;
            }
        } // was in the wrong place!
        try {
            l.info(id()+" closing server socket.");
            serverSocket.close();
        } catch(IOException e) {
            if(isShuttingDown) l.info("server: "+id()+" shutting down, server caught: "+e);
            else {
                l.severe("server: "+id()+" not shutting down, server caught: "+e);
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
    public String id() {
        return host+':'+service;
    }
    public Message.Factory messageFactory() {
        return messageFactory;
    }
    @Override public String toString() {
        return host+':'+service;
    }
    public static void main(String[] args) throws IOException {
        Set<CPair<String,Integer>> set=new LinkedHashSet<>();
        for(int i=11;i<16;i++)
            set.add(new CPair("192.168.1.2",defaultReceivePort+i));
        Set<XServer> servers=new LinkedHashSet<>();
        for(CPair<String,Integer> pair:set) {
            p(""+pair);
            servers.add(new XServer(pair.first,pair.second,set));
        }
        for(XServer server:servers) {
            Thread thread=new Thread(server);
            thread.setName(server.id());
            server.thread=thread;
            thread.start();
        }
    }
    final Set<Reader> newConnections=new LinkedHashSet<>();
    // maybe Map<Pair<host,service>,pair<reader,Writer>>
    Map<CPair<String,Integer>,Pair<Reader,Writer>> map=new LinkedHashMap<>();
    // old
    //final Map<String,Pair<Writer,Reader>> idToPair=new TreeMap<>(); // destination id!
    //public static final String ok="ok";
    //static int serialNumbers=0;
    final String host;
    final Integer service;
    final ServerSocket serverSocket;
    final Message.Factory messageFactory;
    Thread thread;
    protected volatile boolean isShuttingDown;
}
