package com.tayek.speed;
import java.io.IOException;
import java.net.*;
import java.util.*;
import static com.tayek.io.IO.*;
import com.tayek.*;
import com.tayek.io.IO;
import com.tayek.tablet.Message;
import com.tayek.tablet.Message.Type;
import com.tayek.utilities.*;
public interface Server {
    // if testing use internal id
    // if not testing use socket address of acceptor
    String id();
    String host();
    int service();
    void startServer();
    Message.Factory messageFactory();
    void broadcast(Object message);
    String report();
    void stopServer();
    void startHeartbeat();
    void stopHeartbeat();
    Histories histories();
    String maps();
    void addConnection(String newId,Connection connection);
    Map<String,Pair<Writer,Reader>> idToPair();
    Map<InetAddress,Pair<String,Histories>> iNetAddressToPair();
    Writer createAndAddSender(String destinationId,Required required);
    interface Factory {
        Server create(Required required);
        Server create2(Required required);
        static class FactoryImpl implements Factory {
            @Override public Server create(Required required) {
                Server server=null;
                try {
                    server=new ServerImpl(required);
                } catch(IOException e) {
                    e.printStackTrace();
                    p("can not start server: "+required);
                }
                return server;
            }
            @Override public Server create2(Required required) {
                Server server=null;
                try {
                    server=new ServerImpl2(required);
                } catch(IOException e) {
                    e.printStackTrace();
                }
                return server;
            }
            private static abstract class ServerABC extends Thread implements Server {
                ServerABC(Required required) throws IOException {
                    super(required.id.toString()+" server");
                    this.required=required;
                    InetAddress inetAddress=InetAddress.getByName(required.host);
                    messageFactory=Message.instance.create(required.host,required.service,new Single<Integer>(0));
                    serverSocket=new ServerSocket(required.service,100/* what should this be?*/,inetAddress);
                    if(serverSocket==null) throw new RuntimeException("bind failed!");
                    isShuttingDown=false;
                }
                @Override public String id() {
                    return required.id;
                }
                @Override public String host() {
                    return required.host;
                }
                @Override public int service() {
                    return required.service;
                }
                final Required required;
                final Histories histories=new Histories();
                final ServerSocket serverSocket;
                final Message.Factory messageFactory;
                protected volatile boolean isShuttingDown;
            }
            private static class ServerImpl2 extends ServerABC implements Server {
                ServerImpl2(Required required) throws IOException {
                    super(required);
                    InetAddress inetAddress=InetAddress.getByName(required.host);
                    messageFactory=Message.instance.create(required.host,required.service,new Single<Integer>(0));
                    serverSocket=new ServerSocket(required.service,100/* what should this be?*/,inetAddress);
                    if(serverSocket==null) throw new RuntimeException("bind failed!");
                    isShuttingDown=false;
                }
                @Override public void startServer() {
                    // TODO Auto-generated method stub
                }
                @Override public com.tayek.tablet.Message.Factory messageFactory() {
                    // TODO Auto-generated method stub
                    return null;
                }
                @Override public void broadcast(Object message) {
                    // TODO Auto-generated method stub
                }
                @Override public String report() {
                    // TODO Auto-generated method stub
                    return null;
                }
                @Override public void stopServer() {
                    // TODO Auto-generated method stub
                }
                @Override public void startHeartbeat() {
                    // TODO Auto-generated method stub
                }
                @Override public void stopHeartbeat() {
                    // TODO Auto-generated method stub
                }
                @Override public Histories histories() {
                    // TODO Auto-generated method stub
                    return null;
                }
                @Override public String maps() {
                    // TODO Auto-generated method stub
                    return null;
                }
                @Override public void addConnection(String newId,Connection connection) {
                    // TODO Auto-generated method stub
                }
                @Override public Map<String,Pair<Writer,Reader>> idToPair() {
                    // TODO Auto-generated method stub
                    return null;
                }
                @Override public Map<InetAddress,Pair<String,Histories>> iNetAddressToPair() {
                    // TODO Auto-generated method stub
                    return null;
                }
                @Override public Writer createAndAddSender(String destinationId,Required required) {
                    // TODO Auto-generated method stub
                    return null;
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
                                l.info("server: "+required.id+" is shutting down");
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
                final Message.Factory messageFactory;
                final Histories histories=new Histories();
                final ServerSocket serverSocket;
                private volatile boolean isShuttingDown;
                final Set<Reader> newConnections=new LinkedHashSet<>();
                // maybe Map<Pair<host,service>,pair<reader,Writer>>
                Map<Pair<String,Integer>,Pair<Reader,Writer>> map=new TreeMap<>();
                // old
                //final Map<String,Pair<Writer,Reader>> idToPair=new TreeMap<>(); // destination id!
                //public static final String ok="ok";
                //static int serialNumbers=0;
            }
            private static class ServerImpl extends ServerABC implements Server {
                ServerImpl(Required required) throws IOException {
                    super(required);
                }
                @Override public Message.Factory messageFactory() {
                    return messageFactory;
                }
                @Override public void startServer() { // should we use a different name?
                    super.start();
                    // only works for ourselves!
                    createAndAddSender(id(),required);
                }
                @Override public void broadcast(Object message) {
                    synchronized(this) {
                        for(Pair<Writer,Reader> pair:idToPair.values())
                            if(pair.first!=null) pair.first.write(message);
                    }
                }
                @Override public Histories histories() {
                    return histories;
                }
                void check() {
                    boolean ok=true;
                    for(String key:idToPair.keySet()) {
                        Pair<Writer,Reader> pair=idToPair.get(key);
                        if(pair!=null) {
                            if(pair.first!=null&&pair.second!=null) {
                                if(pair.first.histories.serialNumber!=pair.second.histories.serialNumber) {
                                    ok=false;
                                    p("writer with reader: "+pair.first.histories.serialNumber+"!="+pair.second.histories.serialNumber);
                                }
                                if(key.equals(id())) {
                                    if(histories().serialNumber!=pair.first.histories.serialNumber) {
                                        ok=false;
                                        p("self with writer: "+histories().serialNumber+"!="+pair.first.histories.serialNumber);
                                    }
                                    // probably only need to check one!
                                    if(histories().serialNumber!=pair.second.histories.serialNumber) {
                                        ok=false;
                                        p("self with reader: "+histories().serialNumber+"!="+pair.second.histories.serialNumber);
                                    }
                                }
                            }
                        }
                    }
                    if(!ok) throw(new RuntimeException("oops"));
                }
                @Override public void addConnection(String newId,Connection connection) {
                    l.info("adding: "+connection+" to server: "+id()+" has histories: "+histories.serialNumber);
                    check();
                    connection.otherId=newId;
                    synchronized(this) {
                        l.info("map for: "+id()+" is:"+idToPair());
                        Pair<Writer,Reader> pair=idToPair().get(newId);
                        if(connection instanceof Reader) {
                            InetAddress inetAddress=connection.socket.getInetAddress();
                            if(pair==null) { // no previous connection or not known?
                                pair=new Pair<Writer,Reader>(null,(Reader)connection);
                                l.info(id()+" "+id()+"->"+newId+" received first message from: "+newId+":"+inetAddress);
                                idToPair().put(newId,pair); // sync
                                l.warning(id()+" added pair: "+pair);
                            } else { // there is a previous connection
                                l.info(id()+"->"+newId+" modifying pair: "+pair);
                                if(pair.first!=null) {
                                    l.info(id()+" pair has existing writer: "+pair+" with: "+pair.first.histories.sn());
                                    l.info(id()+" new reader has: "+connection.histories.sn());
                                    l.info("merging histories: "+connection.histories);
                                    if(id().equals(newId)) {
                                        l.info("with self histories: "+histories());
                                        if(!histories.equals(connection.histories)) {
                                            histories().add(connection.histories);
                                            connection.histories=histories();
                                            l.info("resulting histories: "+histories());
                                        }
                                    } else {
                                        l.info("with histories: "+pair.first.histories);
                                        pair.first.histories.add(connection.histories);
                                        connection.histories=pair.first.histories;
                                        l.info("resulting histories: "+pair.first.histories);
                                    }
                                }
                                if(pair.second!=null) { // new reader, what to do here?
                                    l.warning(id()+"<-"+newId+" pair has exiting reader!: "+pair+" with: "+pair.second.histories.sn());
                                    l.info(id()+" new reader has: "+connection.histories.sn());
                                    // save history?
                                    //pair.second.stopThread();
                                    l.warning(id()+"<-"+newId+" pair was not modified!: "+pair);
                                } else { // how can we get here?
                                    l.warning(id()+"<-"+newId+" pair has existing reader!: "+pair);
                                    pair.second=(Reader)connection;
                                    l.info("merging histories: "+connection.histories);
                                    if(id().equals(newId)) {
                                        l.info("with self histories: "+histories());
                                        if(!histories.equals(connection.histories)) {
                                            histories().add(connection.histories);
                                            connection.histories=histories();
                                            l.info("resulting histories: "+histories());
                                        }
                                    } else {
                                        l.info("with histories: "+pair.first.histories);
                                        pair.first.histories.add(connection.histories);
                                        connection.histories=pair.first.histories;
                                        l.info("resulting histories: "+pair.first.histories);
                                    }
                                    l.info(id()+"<-"+newId+" added receiver: "+connection+". modified pair: "+pair);
                                }
                            }
                        } else if(connection instanceof Writer) {
                            if(pair==null) {
                                pair=new Pair<Writer,Reader>((Writer)connection,null);
                                l.info(id()+"->"+newId+" new write to: "+newId);
                                idToPair().put(newId,pair); // sync
                                l.info(id()+" added pair: "+pair);
                            } else {
                                l.info(id()+" modifying pair: "+pair);
                                if(pair.first!=null) {
                                    l.warning(id()+" pair has existing client, stopping thread!: "+pair);
                                    l.info(id()+" new writer has: "+connection.histories.sn());
                                    pair.first.stopThread();
                                }
                                if(pair.second!=null) {
                                    l.warning(id()+" pair has existing receiver!: "+pair);
                                    l.info(id()+" new writer has: "+connection.histories.sn());
                                    p(Thread.currentThread()+" "+id()+": adding sender to existing receiver: "+connection);
                                }
                                pair.first=(Writer)connection;
                                p("adding sender: "+connection);
                                if(pair.second!=null) {
                                    if(!pair.first.histories.equals(pair.second.histories)) {
                                        l.severe("histories are different!");
                                        // use reader histories?
                                        p("sender histories: "+pair.first.histories);
                                        p("reader histories: "+pair.second.histories);
                                    } else l.warning(id()+" added sender: "+connection+", modified pair: "+pair);
                                }
                            }
                        } else l.severe(id()+" strange connection type: "+connection);
                        l.info("new map for: "+id()+" is:"+idToPair());
                    }
                    check();
                }
                public Writer createAndAddSender(String destinationId,Required required) {
                    Writer sender=null;
                    try {
                        l.info("trying: "+required.host+":"+required.service);
                        sender=new Writer(id(),destinationId,required);
                        if(destinationId.equals(id())) {
                            l.warning("adding sender to self.");
                            sender.histories=histories(); // use our histories
                        }
                        sender.otherId=destinationId;
                        l.info("adding: "+sender);
                        addConnection(destinationId,sender);
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                    return sender;
                }
                @Override public void startHeartbeat() {
                    heartbeat=new Thread(new Runnable() {
                        @Override public void run() {
                            while(true) {
                                broadcast(messageFactory.other(Type.heartbeat,"1","T1"));
                                try {
                                    Thread.sleep(100);
                                } catch(InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    heartbeat.start();
                }
                @Override public void stopHeartbeat() {
                    if(heartbeat!=null) {
                        heartbeat.interrupt();
                        try {
                            heartbeat.join();
                        } catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                        heartbeat=null;
                    }
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
                                l.info("server: "+required.id+" is shutting down");
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
                void waitForServersToStart(int devices) {
                    l.info("wait for: "+devices+" devices to connect");
                    while(idToPair.size()<devices)
                        Thread.yield();
                    l.info(devices+" connected.");
                }
                void waitForServersToComplete(int n) throws InterruptedException {
                    l.info("wait for: "+idToPair.size()+" servers to complete: "+n+" messages.");
                    for(boolean done=false;!done;) {
                        done=true;
                        for(Pair<Writer,Reader> pair:idToPair.values()) // how can this work!
                            if(pair.second!=null) if(pair.second.histories.receiverHistory.history.attempts()<n) done=false;
                        if(done) break;
                        Thread.sleep(10);
                    }
                    l.info(idToPair.size()+" servers complete.");
                }
                public void waitForServersToShutdown() throws IOException {
                    for(Pair<Writer,Reader> pair:idToPair.values())
                        if(pair.second!=null) {
                            pair.second.stopThread();
                            // how to set receiver to null?
                        }
                }
                @Override public void stopServer() {
                    l.info(" stopping server: "+this);
                    isShuttingDown=true;
                    l.info(" stopping senders and receivers: "+this);
                    l.info("shutting down old connections:");
                    for(Pair<Writer,Reader> pair:idToPair.values()) {
                        l.info("shutting down old connection: "+pair);
                        if(pair.first!=null) pair.first.stopThread();
                        if(pair.second!=null) pair.second.stopThread();
                    }
                    l.info("shutting down new connections:");
                    for(Reader receiver:newConnections) {
                        l.info("shutting down new connection: "+receiver);
                        receiver.stopThread();
                    }
                    try {
                        l.info("closing server socket");
                        serverSocket.close();
                        l.info("server socket closed");
                    } catch(IOException e) {
                        l.info("caught: "+e);
                        e.printStackTrace();
                    }
                    if(!this.equals(Thread.currentThread())) {
                        l.info("joining with: "+this+", threadinterrupted: "+this.isInterrupted()+", alive: "+this.isAlive());
                        try {
                            this.join();
                        } catch(InterruptedException e) {
                            l.info("join interrupted!");
                        }
                    }
                    l.info(" exit stopping server with thread: "+this);
                }
                @Override public Map<String,Pair<Writer,Reader>> idToPair() {
                    return idToPair;
                }
                @Override public Map<InetAddress,Pair<String,Histories>> iNetAddressToPair() {
                    return iNetAddressToPair();
                }
                @Override public String maps() {
                    StringBuffer sb=new StringBuffer();
                    sb.append("\n id to pair: "+idToPair());
                    return sb.toString();
                }
                @Override public String report() {
                    StringBuffer sb=new StringBuffer();
                    sb.append("report for: "+id()+"\n");
                    //sb.append("from this: "+histories+"\n");
                    sb.append("maps: "+maps()+"\n");
                    synchronized(idToPair) {
                        for(Pair<Writer,Reader> pair:idToPair.values()) {
                            if(pair.first!=null&&pair.second!=null) {
                                if(pair.first.histories.serialNumber!=pair.second.histories.serialNumber) {
                                    sb.append("write and reader: "+pair.first.histories.serialNumber+"!="+pair.second.histories.serialNumber);
                                    sb.append('\n');
                                }
                                /* // no access to id in histories!
                                if(histories().serialNumber!=pair.first.histories.serialNumber) {
                                    sb.append("self and writer: "+histories().serialNumber+"!="+pair.first.histories.serialNumber);
                                    sb.append('\n');
                                }
                                if(histories().serialNumber!=pair.second.histories.serialNumber) {
                                    sb.append("self and reader: "+histories().serialNumber+"!="+pair.second.histories.serialNumber);
                                    sb.append('\n');
                                }
                                */
                            } else sb.append("only one!");
                            if(pair.first!=null) {
                                sb.append(pair.first.id+"->"+pair.first.otherId);
                                sb.append('\n');
                                sb.append(pair.first.histories.senderHistory);
                            } else sb.append("null");
                            if(pair.second!=null) {
                                sb.append('\n');
                                sb.append(pair.second.id+"<-"+pair.second.otherId);
                                sb.append('\n');
                                sb.append(pair.second.histories.receiverHistory);
                                sb.append('\n');
                            }
                            sb.append("-----\n");
                        }
                    }
                    return sb.toString();
                }
                @Override public String toString() {
                    return "server: "+id()+", "+host()+":"+service()+", "+idToPair;
                }
                //final Set<SocketAddress> socketAddresses; // maybe add these in later?
                Thread heartbeat;
                final Set<Reader> newConnections=new LinkedHashSet<>();
                final Map<String,Pair<Writer,Reader>> idToPair=new TreeMap<>(); // destination id!
                public static final String ok="ok";
                static int serialNumbers=0;
            }
        }
    }
    Factory factory=new Factory.FactoryImpl();
}