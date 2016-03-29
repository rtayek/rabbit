package com.tayek.speed;
import static com.tayek.utilities.Utility.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
public class Service extends Thread {
    public Service(SocketAddress socketAddress) throws IOException {
        this(serverSocket(socketAddress));
    }
    public Service(ServerSocket serverSocket) {
        if(serverSocket==null) 
            throw new RuntimeException("server socket is null!");
        
        this.serverSocket=serverSocket;
    }
    @Override public void run() {
        l.info("running main server with: "+serverSocket);
        while(!isShuttingDown) {
            try {
                l.info("server is accepting on: "+serverSocket);
                Socket socket=serverSocket.accept();
                l.info("server accepted connection from: "+socket);
                Server server=new Server("from: "+socket.getRemoteSocketAddress(),socket,new Histories());
                Thread thread=new Thread(server);
                thread.setName(socket.getRemoteSocketAddress().toString());
                thread.start();
                servers.add(server);
            } catch(IOException e) {
                if(isShuttingDown) {
                    l.info("server is shutting down");
                    if(e.toString().contains("socket closed")) l.info("0 (maybe normal) server caught: "+e);
                    else if(e.toString().contains("Socket is closed")) l.info("0 (maybe normal) server caught: "+e);
                    else l.info("1 server caught: "+e);
                } else {
                    l.info("is not shutting down, server caught: "+e);
                    e.printStackTrace();
                }
                break;
            }
        } // was in the wrong place!
        try {
            l.info("closing server socket.");
            serverSocket.close();
        } catch(IOException e) {
            if(isShuttingDown) l.info("shutting down, server caught: "+e);
            else {
                l.severe("not shutting down, server caught: "+e);
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
    void waitForServersToStart(int devices) {
        l.info("wait for: "+devices+" devices to connect");
        while(servers.size()<devices)
            Thread.yield();
        l.info(devices+" connected.");
    }
    void waitForServersToComplete(int n) throws InterruptedException {
        l.info("wait for: "+servers.size()+" servers to complete: "+n+" messages.");
        for(boolean done=false;!done;) {
            done=true;
            for(Server server:servers)
                if(server.histories.server.server.attempts()<n) done=false;
            if(done) break;
            Thread.sleep(10);
        }
        l.info(servers.size()+" servers complete.");
    }
    public void waitForServersToShutdown() throws IOException {
        for(Server server:servers) {
            double et=server.shutdown();
            l.fine("server: "+server.id+" shutdown in: "+et);
        }
    }
    public void startServer() { // long running thread
        if(thread!=null) {
            stopServer();
        }
        thread=new Thread(this,"server");
        isShuttingDown=false;
        thread.start();
    }
    public void stopServer() {
        l.info(" stopping server with thread: "+thread);
        isShuttingDown=true;
        try {
            l.info("closing server socket");
            serverSocket.close();
            l.info("server socket closed");
        } catch(IOException e) {
            l.info("caught: "+e);
            e.printStackTrace();
        }
        if(thread!=null) {
            l.info("joining with: "+thread+", threadinterrupted: "+thread.isInterrupted()+", alive: "+thread.isAlive());
            try {
                thread.join();
            } catch(InterruptedException e) {
                l.info("join interrupted!");
            }
            thread=null;
        }
    }
    enum L {
        Client,Server,Main;
    }
    public static void main(String[] args) throws UnknownHostException,IOException,InterruptedException {
        Level level=Level.ALL;
        Service.l.setLevel(level);
        Client.l.setLevel(level);
        Server.l.setLevel(level);
        Histories.defaultReportPeriod=0;
        SocketAddress socketAddress=new InetSocketAddress("192.168.0.101",Main.service);
        Service acceptor=new Service(socketAddress);
        acceptor.startServer();

    }
    // move int service here!
    private Thread thread;
    private final ServerSocket serverSocket;
    private volatile boolean isShuttingDown;
    List<Server> servers=new ArrayList<>();
    public static final String ok="ok";
    public static final Logger l=Logger.getLogger(Service.class.getName());
}
