package com.tayek.speed;
import static com.tayek.io.IO.*;
import static com.tayek.utilities.Utility.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import com.tayek.Required;
import com.tayek.io.*;
import com.tayek.tablet.Group;
import com.tayek.tablet.Message.Type;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.utilities.Et;
import static com.tayek.speed.Server.*;
public class Main {
    static void run(int n) throws InterruptedException {
        int threads=Thread.activeCount();
        Set<Server> servers=new LinkedHashSet<>();
        // map<string,Pair<String,Integer> map again?
        for(Integer i=1;i<=n;i++)
            if(i==1) servers.add(factory.create(new Required(raysPcOnRaysNetwork,defaultReceivePort+i),Model.mark1.clone()));
            else servers.add(factory.create(new Required(defaultHost,defaultReceivePort+i),Model.mark1.clone()));
        for(Server server:servers)
            p(""+server);
        for(Server tablet:servers)
            tablet.startServer();
        Thread.sleep(100);
        // try to discover instead of adding senders and broadcasting!
        p("adding sender(s) -------------------------------------");
        if(false) {
            for(Server server:servers)
                for(Server server2:servers)
                    if(server!=server2) {
                        Required required=new Required(server2.id(),server2.host(),server2.service());
                        server.createAndAddWriter(server2.id(),required);
                    }
        } else {
            Iterator<Server> i=servers.iterator();
            Server first=i.next(),next;
            for(int j=1;j<=servers.size();j++)
                if(n>j) {
                    next=i.next();
                    Required required=new Required(next.id(),next.host(),next.service());
                    first.createAndAddWriter(next.id(),required);
                }
        }
        Thread.sleep(200);
        p("broadcasting -------------------------------------");
        int messages=10;
        Et et=new Et();
        for(int i=0;i<messages;i++) {
            for(Server server:servers) {
                server.broadcast(server.messageFactory().other(Type.dummy,"1",server.id()));
            }
            Thread.sleep(10);
        }
        p((messages*servers.size()*servers.size())+" messages sent in: "+et+"="+(1000.*messages*messages*servers.size()/et.etms())+" messages/second");
        Thread.sleep(200);
        p((messages*servers.size()*servers.size())+" messages sent in: "+et+"="+(1000.*messages*messages*servers.size()/et.etms())+" messages/second");
        p("reporting -----------------------------------------------------");
        for(Server server:servers) {
            p("---------\n"+server.report());
        }
        //new Thread(new Joiner(threads)).start();
        Thread.sleep(200);
        p("stopping-----------------------------------------------------");
        printThreads();
        for(Server server:servers)
            server.stopServer();
        Thread.sleep(1000);
        p("end");
        printThreads();
        p("exit main");
    }
    public static void main(String[] args) throws InterruptedException {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
        Map<String,Required> requireds=new TreeMap<>(new Group.Groups().groups.get("g0"));
        p("requireds: "+requireds);
        if(true) run(4);
        else {
            Server server=factory.create(new Required("localhost",defaultReceivePort),Model.mark1.clone());
            Thread.sleep(100);
            server.broadcast("foo");
            Thread.sleep(100);
            server.broadcast("bar");
            Thread.sleep(100);
            //server.stopserver();
        }
    }
    boolean shutdownClientsFirst;
    boolean shutdownServerFirst;
    int threads;
    int devices=50;
    Set<Writer> clients=new LinkedHashSet<>();
}
