package com.tayek.conrad;
import static com.tayek.utilities.Utility.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import com.tayek.utilities.Et;
public class Main {
    void waitForClientsToShutdown() throws InterruptedException,IOException {
        l.info("wait for: "+clients.size()+" clients to shutdown");
        for(Client client:clients) {
            l.fine("waiting for client: "+client.id+" to shutdown.");
            double et=client.shutdown();
            l.fine("client: "+client.id+" shutdown in: "+et);
        }
        l.info("wait for: "+clients.size()+" clients to shutdown");
    }
    void waitForClientsToComplete() throws InterruptedException {
        l.info("wait for: "+devices+" devices to complete");
        for(boolean done=false;!done;) {
            done=true;
            for(Client client:clients)
                if(client.histories.client.client.attempts()<Client.messagesToSend) done=false;
            if(done) break;
            Thread.sleep(10);
        }
    }
    void run(Service acceptor) throws IOException,InterruptedException {
        threads=Thread.activeCount();
        //Client.messagesToSend=5_000;
        for(int i=1;i<=devices;i++)
            clients.add(Client.createClientAndStart("device: "+i));
        p(clients.size()+" clients started at: "+et);
        if(acceptor!=null) {
            acceptor.waitForServersToStart(devices);
            p(devices+" connections from devices at: "+et);
        }
        waitForClientsToComplete();
        p(devices+" clients completed sending: "+Client.messagesToSend+" at: "+et);
        if(acceptor!=null) {
            acceptor.waitForServersToComplete(Client.messagesToSend);
            p(acceptor.servers.size()+" servers completed receiving : "+Client.messagesToSend+" at: "+et);
        }
        if(shutdownServerFirst&&acceptor!=null) {
            acceptor.stopServer();
            p("main server shutdown completed at: "+et);
        }
        if(shutdownClientsFirst) waitForClientsToShutdown();
        p(devices+" clients shutdown at: "+et);
        if(acceptor!=null) {
            p("wait for: "+devices+" servers to shutdown");
            acceptor.waitForServersToShutdown();
            p(devices+" servers shutdown at: "+et);
        }
        if(!shutdownClientsFirst) waitForClientsToShutdown();
        if(!shutdownServerFirst&&acceptor!=null) {
            acceptor.stopServer();
            p("main server shutdown completed at: "+et);
        }
        p("at: "+et);
        p(devices+" devices, each sending: "+Client.messagesToSend+" "+Client.line.length()+" byte messages: "+Client.messagesToSend/et.etms()*1000+" messages/second");
        if(false) {
            for(Client client:clients)
                p("histories for client: "+client.id+": "+client.histories);
            for(Server server:acceptor.servers)
                p("histories for server: "+server.id+": "+server.histories);
        }
        p((Thread.activeCount()-threads)+" extra threads.");
        printThreads();
        p("exit main run()");
    }
    void test() throws IOException,InterruptedException {
        p("start test at: "+et);
        SocketAddress socketAddress=new InetSocketAddress(host,service);
        Service acceptor=new Service(socketAddress);
        acceptor.startServer();
        run(acceptor);
        p("test completed at: "+et);
    }
    public static void main(String[] args) throws UnknownHostException,IOException,InterruptedException {
        Level level=Level.OFF;
        Service.l.setLevel(level);
        Client.l.setLevel(level);
        Server.l.setLevel(level);
        Main.l.setLevel(level);
        Histories.defaultReportPeriod=0;
        int n=5;
        for(int i=1;i<=n;i++)
            new Main().test();
    }
    final Et et=new Et();
    boolean shutdownClientsFirst;
    boolean shutdownServerFirst;
    int threads;
    int devices=50;
    Set<Client> clients=new LinkedHashSet<>();
    static String host="localhost";
    static int service=12345;
    public static final Logger l=Logger.getLogger(Client.class.getName());
}
