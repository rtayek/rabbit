package com.tayek.speed;
import java.io.IOException;
import java.net.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.*;
import java.util.Map.Entry;
import static com.tayek.io.IO.*;
import com.tayek.*;
import com.tayek.io.IO;
import com.tayek.tablet.*;
import com.tayek.tablet.Message.Type;
import com.tayek.utilities.*;
public interface Server {
    // if testing use internal id
    // if not testing use socket address of acceptor
    String id();
    String host();
    int service();
    boolean startServer();
    boolean isServerRunning();
    void stopServer();
    void broadcast(Object message);
    String report();
    Histories histories();
    ServerSocket serverSocket();
    MessageReceiver messageReceiver();
    Message.Factory messageFactory();
    String maps();
    void addConnection(String newId,Connection connection);
    Map<String,Pair<Writer,Reader>> idToPair();
    Map<InetAddress,Pair<String,Histories>> iNetAddressToPair();
    Writer createAndAddWriter(String destinationId,Required required);
    interface Factory {
        Server create(Required required,MessageReceiver messageReceiver);
        static class FactoryImpl implements Factory {
            @Override public Server create(Required required,MessageReceiver messageReceiver) {
                Server server=null;
                try {
                    server=new ServerImpl(required,messageReceiver);
                    ((ServerImpl)server).start();
                } catch(IOException e) {
                    e.printStackTrace();
                    p("can not start server: "+required);
                }
                return server;
            }
            public static abstract class ServerABC extends Thread implements Server {
                // make private again!
                ServerABC(Required required,MessageReceiver messageReceiver) throws IOException {
                    super(required.id.toString()+" server");
                    this.required=required;
                    this.messageReceiver=messageReceiver;
                    //InetAddress inetAddress=InetAddress.getByName(required.host);
                    messageFactory=Message.instance.create(required.host,required.service,new Single<Integer>(0));
                    //serverSocket=new ServerSocket();
                    //if(serverSocket==null) throw new RuntimeException("serverSocket is null!");
                    //serverSocket.setReuseAddress(true);
                    isShuttingDown=false;
                }
                @Override public boolean startServer() {
                    InetAddress inetAddress;
                    try {
                        inetAddress=InetAddress.getByName(required.host);
                        p("creating new ServerSocket.");
                        serverSocket=new ServerSocket();
                        serverSocket.bind(new InetSocketAddress(inetAddress,required.service),100);
                        p("created new ServerSocket: "+serverSocket);
                        return true;
                    } catch(UnknownHostException e) {
                        l.warning(id()+" caught: "+e);
                    } catch(IOException e) {
                        l.warning(id()+" caught: "+e);
                    } catch(Exception e) {
                        l.warning(id()+" caught: "+e);
                    }
                    return false;
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
                @Override public MessageReceiver messageReceiver() {
                    return messageReceiver;
                }
             final Required required;
                final Histories histories=new Histories();
                ServerSocket serverSocket;
                final Message.Factory messageFactory;
                final MessageReceiver messageReceiver;
                public volatile boolean isShuttingDown;
            }
            private static class ServerImpl extends ServerABC {
                ServerImpl(Required required,MessageReceiver messageReceiver) throws IOException {
                    super(required,messageReceiver);
                }
                @Override public ServerSocket serverSocket() {
                    return serverSocket;
                }
                @Override public Message.Factory messageFactory() {
                    return messageFactory;
                }
                @Override public boolean startServer() { // should we use a different name?
                    boolean ok=super.startServer();
                    if(ok) createAndAddWriter(id(),required);
                    return ok;
                }
                @Override public boolean isServerRunning() {
                    return serverSocket!=null&&serverSocket.isBound();
                }
                @Override public void broadcast(Object message) {
                    p("broadcating: "+message);
                    synchronized(this) {
                        for(Pair<Writer,Reader> pair:idToPair.values()) {
                            p("write to: "+pair.first);
                            if(pair.first!=null) pair.first.write(message);
                        }
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
                private void newWriter(String newId,Writer writer,Pair<Writer,Reader> pair) {
                    if(pair==null) {
                        pair=new Pair<Writer,Reader>(writer,null);
                        l.info(id()+": "+id()+"->"+newId+" adding new writer to: "+newId);
                        idToPair().put(newId,pair); // sync
                        l.info(id()+": "+id()+"->"+newId+" added pair: "+pair);
                    } else {
                        l.info(id()+" modifying pair: "+pair);
                        if(pair.first!=null) {
                            l.warning(id()+": pair has existing writer: "+pair);
                            l.warning(id()+" new writer: "+writer);
                            Writer old=pair.first;
                            if(connections.remove(old)) {
                                p(id()+": stoping: "+old);
                                writer.stopThread();
                            } else l.severe(id()+": can not remove old writer: "+old);
                        }
                        if(pair.second!=null) {
                            l.warning(id()+" pair has existing receiver!: "+pair);
                            l.info(id()+" new writer has: "+writer.histories.sn());
                            p(id()+": adding sender to existing receiver: "+writer);
                        }
                        pair.first=writer;
                        p("adding writer: "+writer);
                        if(pair.second!=null) {
                            if(!pair.first.histories.equals(pair.second.histories)) {
                                l.severe(id()+": histories are different!");
                                // use reader histories?
                                p(id()+": sender histories: "+pair.first.histories);
                                p(id()+": reader histories: "+pair.second.histories);
                            } else l.warning(id()+": added sender: "+writer+", modified pair: "+pair);
                        }
                    }
                }
                private void newReader(String newId,Reader reader,Pair<Writer,Reader> pair) {
                    InetAddress inetAddress=reader.socket.getInetAddress();
                    if(pair==null) { // no previous connection or not known?
                        pair=new Pair<Writer,Reader>(null,reader);
                        l.info(id()+" "+id()+"->"+newId+" added new reader");
                        l.info(id()+" "+id()+"->"+newId+" received first message from: "+newId+":"+inetAddress);
                        idToPair().put(newId,pair); // sync
                        l.info(id()+" added pair: "+pair);
                    } else { // there is a previous connection
                        l.info(id()+": "+id()+"->"+newId+" modifying pair: "+pair);
                        if(pair.first!=null) {
                            l.info(id()+": pair has existing writer!: "+pair);
                            l.info(id()+": new reader: "+reader);
                            l.fine(id()+": merging histories: "+reader.histories);
                            if(id().equals(newId)) {
                                l.fine(id()+": with self histories: "+histories());
                                if(!histories.equals(reader.histories)) {
                                    l.info("histories 1 adding: "+reader.histories.sn()+" to"+histories().sn());
                                    histories().add(reader.histories);
                                    reader.histories=histories();
                                    l.fine(id()+": resulting histories: "+histories());
                                }
                            } else {
                                l.fine(id()+": with histories: "+pair.first.histories);
                                l.info("histories 2 adding: "+reader.histories.sn()+" to"+pair.first.histories.sn());
                                pair.first.histories.add(reader.histories);
                                reader.histories=pair.first.histories;
                                l.fine(id()+": resulting histories: "+pair.first.histories);
                            }
                        }
                        if(pair.second!=null) { // new reader, what to do here?
                            l.info(id()+" current: "+pair.second+", new: "+reader);
                            l.info(id()+" current: "+pair.second.serialNumber+", new: "+reader.serialNumber);
                            l.info(id()+": "+id()+"<-"+newId+" new reader: "+reader);
                            l.info(id()+": "+id()+"<-"+newId+" pair has existing reader!: "+pair);
                            l.info(id()+": new reader: "+reader);
                            // save history?
                            Reader old=pair.second;
                            if(connections.remove(old)) {
                                l.warning(id()+": "+id()+"<-"+newId+" stopping old reader: "+old);
                                old.stopThread();
                            } else l.warning(id()+": can not remove old writer: "+old);
                            pair.second=reader;
                            l.warning(id()+": "+id()+"<-"+newId+" pair was modified!: "+pair);
                        } else { // how can we get here?
                            // easy, connection just received first message from a new tablet
                            pair.second=reader;
                            l.fine(id()+": merging histories: "+reader.histories);
                            if(id().equals(newId)) { // from ourselves
                                l.fine(id()+": with self histories: "+histories());
                                if(!histories.equals(reader.histories)) {
                                    l.info("histories 3 adding: "+reader.histories.sn()+" to"+histories().sn());
                                    histories().add(reader.histories);
                                    reader.histories=histories();
                                    l.fine(id()+": resulting histories: "+histories());
                                }
                            } else { // connection just received first message from another tablet
                                l.fine(id()+": with histories: "+pair.first.histories);
                                if(reader.histories.sn().equals(pair.first.histories.sn())) ;
                                else {
                                    l.info("histories 4 adding: "+reader.histories.sn()+" to"+pair.first.histories.sn());
                                    pair.first.histories.add(reader.histories);
                                    reader.histories=pair.first.histories;
                                    l.fine(id()+": resulting histories: "+pair.first.histories);
                                }
                            }
                            l.info(id()+": "+id()+"<-"+newId+" added receiver: "+reader+". modified pair: "+pair);
                        }
                    }
                }
                @Override public void addConnection(String newId,Connection connection) {
                    synchronized(this) {
                        l.info(id()+": current map is:"+idToPair());
                        l.info(id()+": adding: "+connection+" to server: "+id()+" has histories: "+histories.sn());
                        check();
                        connection.otherId=newId;
                        Pair<Writer,Reader> pair=idToPair().get(newId);
                        if(connection instanceof Reader) newReader(newId,(Reader)connection,pair);
                        else if(connection instanceof Writer) newWriter(newId,(Writer)connection,pair);
                        else l.severe(id()+": strange connection type: "+connection);
                        l.info(id()+": new map for: "+id()+" is:"+idToPair());
                    }
                    check();
                }
                public Writer createAndAddWriter(String destinationId,Required required) {
                    Writer sender=null;
                    try {
                        l.info(id()+": trying: "+required.host+":"+required.service);
                        sender=Writer.create(id(),destinationId,required);
                        if(sender!=null) {} else {
                            l.warning(id()+": can not create writer!");
                            return null;
                        }
                        if(destinationId.equals(id())) {
                            l.info(id()+": adding sender to self.");
                            sender.histories=histories(); // use our histories
                        }
                        sender.otherId=destinationId;
                        l.info(id()+": adding: "+sender);
                        addConnection(destinationId,sender);
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                    return sender;
                }
                @Override public void run() {
                    while(!isShuttingDown) {
                        if(serverSocket!=null) l.info(id()+" accepting on: "+serverSocket);
                        // idea: a log server for java that uses wifi and greps into windows?
                        if(serverSocket!=null&&!serverSocket.isClosed()&&serverSocket.isBound()) {
                            p("try to accept: ");
                            try {
                                p("accepting: ");
                                Socket socket=serverSocket.accept();
                                l.info(id()+" accepted connection from: "+socket);
                                // we might be able to guess the ip and service?
                                InetAddress iNetAddress=socket.getInetAddress();
                                l.info(id()+": socket address: "+iNetAddress);
                                Reader reader;
                                synchronized(this) {
                                    reader=Reader.create(this,id(),null/* no id yet*/,socket,new Histories());
                                    l.info(id()+" connection created: "+reader);
                                    connections.add(reader);
                                    if(false) {
                                        p("connections: ");
                                        for(Connection connection:connections)
                                            p(connection.id+": "+connection);
                                    }
                                }
                                reader.thread=new Thread(reader);
                                reader.thread.setName(socket.getRemoteSocketAddress().toString()+"->"+id());
                                reader.thread.start();
                                // don't add yet, until we receive a message,so we know where he is listening.
                            } catch(IOException e) {
                                if(isShuttingDown) {
                                    l.info(id()+": server: "+required.id+" is shutting down");
                                    if(e.toString().contains("socket closed")) l.info("0 (maybe normal) server: "+id()+" caught: "+e);
                                    else if(e.toString().contains("Socket is closed")) l.info("0 (maybe normal) server: "+id()+" caught: "+e);
                                    else l.info(id()+": 1 server: "+id()+" caught: "+e);
                                } else {
                                    l.info(id()+": is not shutting down, server caught: "+e);
                                    e.printStackTrace();
                                }
                                break;
                            }
                            try {
                                Thread.sleep(0);
                            } catch(InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else { // not bound
                            try {
                                Thread.sleep(1);
                            } catch(InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } // was in the wrong place!
                    try {
                        l.info(id()+": closing server socket.");
                        serverSocket.close();
                    } catch(IOException e) {
                        if(isShuttingDown) l.info(id()+": shutting down, server caught: "+e);
                        else {
                            l.severe(id()+": is not shutting down, server caught: "+e);
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }
                }
                void waitForServersToStart(int devices) {
                    l.info(id()+": wait for: "+devices+" devices to connect");
                    while(idToPair.size()<devices)
                        Thread.yield();
                    l.info(id()+": "+devices+" connected.");
                }
                void waitForServersToComplete(int n) throws InterruptedException {
                    l.info(id()+": wait for: "+idToPair.size()+" servers to complete: "+n+" messages.");
                    for(boolean done=false;!done;) {
                        done=true;
                        for(Pair<Writer,Reader> pair:idToPair.values()) // how can this work!
                            if(pair.second!=null) if(pair.second.histories.receiverHistory.history.attempts()<n) done=false;
                        if(done) break;
                        Thread.sleep(10);
                    }
                    l.info(id()+": "+idToPair.size()+" servers complete.");
                }
                public void waitForServersToShutdown() throws IOException {
                    for(Pair<Writer,Reader> pair:idToPair.values())
                        if(pair.second!=null) {
                            pair.second.stopThread();
                            // how to set receiver to null?
                        }
                }
                @Override public void stopServer() { // sync?
                    isShuttingDown=true;
                    if(serverSocket!=null) {
                        l.info(id()+":  stopping server: "+this);
                        try {
                            l.info(id()+": closing server socket");
                            serverSocket.close();
                            l.info(id()+": server socket closed");
                        } catch(IOException e) {
                            l.info(id()+": caught: "+e);
                            e.printStackTrace();
                        }
                        serverSocket=null; // hack
                    } else l.warning("server socket is null!");
                    if(true) {
                        l.info(id()+": stopping writers and readers: "+this);
                        for(Pair<Writer,Reader> pair:idToPair.values()) {
                            l.info(id()+": shutting down old connections: "+pair);
                            if(pair.first!=null) pair.first.stopThread();
                            if(pair.second!=null) pair.second.stopThread();
                        }
                    } else {
                        l.info(id()+": shutting down new connections:");
                        for(Reader receiver:connections) {
                            l.info(id()+": shutting down new connection: "+receiver);
                            receiver.stopThread();
                        }
                    }
                    if(!this.equals(Thread.currentThread())) {
                        l.info(id()+": joining with: "+this+", threadinterrupted: "+this.isInterrupted()+", alive: "+this.isAlive());
                        try {
                            this.join();
                            l.info(id()+": join completed: "+this);
                        } catch(InterruptedException e) {
                            l.info(id()+": join interrupted!");
                        }
                    }
                    l.info(id()+":  exit stopping server with thread: "+this);
                }
                @Override public Map<String,Pair<Writer,Reader>> idToPair() {
                    return idToPair;
                }
                @Override public Map<InetAddress,Pair<String,Histories>> iNetAddressToPair() {
                    return iNetAddressToPair();
                }
                @Override public String maps() {
                    StringBuffer sb=new StringBuffer();
                    sb.append("\n\tid to pair: ");
                    for(Entry<String,Pair<Writer,Reader>> entry:idToPair().entrySet())
                        sb.append("\n\t"+entry);
                    return sb.toString();
                }
                @Override public String report() {
                    StringBuffer sb=new StringBuffer();
                    sb.append("\nreport for: "+id()+"\n");
                    //sb.append("from this: "+histories+"\n");
                    synchronized(idToPair) {
                        sb.append("\tmaps:\n");
                        sb.append(maps());
                        sb.append("\n");
                        for(Pair<Writer,Reader> pair:idToPair.values()) {
                            if(pair.first!=null&&pair.second!=null) {
                                if(pair.first.histories.serialNumber!=pair.second.histories.serialNumber) {
                                    sb.append("\twriter and reader: "+pair.first.histories.serialNumber+"!="+pair.second.histories.serialNumber);
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
                            } else sb.append("\tonly one!");
                            if(pair.first!=null) {
                                sb.append('\t');
                                sb.append(pair.first.id+"->"+pair.first.otherId);
                                sb.append(pair.first.histories.senderHistory);
                                sb.append('\n');
                            } else sb.append("null\n");
                            if(pair.second!=null) {
                                sb.append('\t');
                                sb.append(pair.second.id+"<-"+pair.second.otherId);
                                sb.append(pair.second.histories.receiverHistory);
                                sb.append('\n');
                            } else sb.append("null\n");
                            sb.append("-----\n");
                        }
                    }
                    return sb.toString();
                }
                @Override public String toString() {
                    return "server: "+id()+", "+host()+":"+service()+", "+idToPair;
                }
                //final Set<SocketAddress> socketAddresses; // maybe add these in later?
                final Set<Reader> connections=new LinkedHashSet<>();
                final Map<String,Pair<Writer,Reader>> idToPair=new TreeMap<>(); // destination id!
                public static final String ok="ok";
                static int serialNumbers=0;
            }
        }
    }
    Factory factory=new Factory.FactoryImpl();
}