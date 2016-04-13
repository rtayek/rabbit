package com.tayek.speed;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.regex.Pattern;
import static com.tayek.io.IO.*;
import com.tayek.Histories;
import com.tayek.utilities.Pair;
public interface Server {
    interface Message {
        String from();
        String host();
        int service();
        String string();
        interface Factory {
            Message create(String string);
            Message from(String string);
        }
    }
    // if testing use internal id
    // if not testing use socket address of acceptor
    String id();
    String host();
    int service();
    void startServer();
    void broadcast(Object message);
    Message create(String string);
    void stopServer();
    void startHeartbeat();
    void stopHeartbeat();
    Histories histories();
    String report();
    String maps();
    void addConnection(String newId,Connection connection);
    Map<String,Pair<Sender,Reader>> idToPair();
    Map<InetAddress,Pair<String,Histories>> iNetAddressToPair();
    Sender createAndAddSender(String destinationId,SocketAddress socketAddress);
    interface Factory {
        Server create(String id,String host,int service,Set<SocketAddress> socketAddresses);
        class FactoryImpl implements Factory {
            @Override public Server create(String id,String host,int service,Set<SocketAddress> socketAddresses) {
                Server server=null;
                try {
                    server=new ServerImpl(id,host,service,socketAddresses);
                } catch(IOException e) {
                    e.printStackTrace();
                }
                return server;
            }
        }
    }
    Factory factory=new Factory.FactoryImpl();
    Character delimiter='|';
    class ServerImpl extends Thread implements Server,Server.Message.Factory {
        // push tablet implementation down into server?
        // and push message implementation down into server also?
        //
        // maybe have sender add sequence number?
        public static class MessageImpl implements Message { // inner?
            private MessageImpl(String from,String host,int service,String string) {
                this.from=from;
                this.host=host;
                this.service=service;
                this.string=string;
                /*
                this.number=null;
                this.type=null;
                this.groupId=null;
                this.tabletId=null;
                this.button=null;
                this.string=null;
                */
            }
            @Override public String from() {
                return from;
            }
            @Override public String host() {
                return host;
            }
            @Override public int service() {
                return service;
            }
            @Override public String string() {
                return string;
            }
            @Override public String toString() {
                return from+delimiter+host+delimiter+service+delimiter+string+delimiter+Sender.line;
            }
            final String from,host,string;
            final int service;
        }
        ServerImpl(String id,String host,int service,Set<SocketAddress> socketAddresses) throws IOException {
            super(id.toString()+" server");
            this.id=id;
            this.host=host;
            this.service=service;
            InetAddress inetAddress=InetAddress.getByName(host);
            serverSocket=new ServerSocket(service,100/* what should this be?*/,inetAddress);
            if(serverSocket==null) throw new RuntimeException("bind failed!");
            iNetAddressToPair.put(inetAddress,new Pair<String,Histories>(id,new Histories()));
            isShuttingDown=false;
            this.socketAddresses=socketAddresses;
        }
        @Override public void startServer() { // should we use a different name?
            super.start();
            // only works for ourselves!
            createAndAddSender(id(),serverSocket.getLocalSocketAddress());
        }
        @Override public MessageImpl from(String string) { // move to tablet?
            String[] parts=string.split(Pattern.quote(""+delimiter));
            MessageImpl message=new MessageImpl(parts[0],parts[1],Integer.valueOf(parts[2]),parts[3]);
            return message;
        }
        @Override public MessageImpl create(String string) {
            return new MessageImpl(id,host,service,string);
        }
        @Override public void broadcast(Object message) {
            synchronized(this) {
                for(Pair<Sender,Reader> pair:idToPair.values())
                    if(pair.first!=null) pair.first.write(message);
            }
        }
        @Override public String id() {
            return id;
        }
        @Override public Histories histories() {
            return histories;
        }
        @Override public void addConnection(String newId,Connection connection) {
            connection.otherId=newId;
            synchronized(this) {
                l.info("map for: "+id+" is:"+idToPair());
                Pair<Sender,Reader> pair=idToPair().get(newId);
                if(connection instanceof Reader) {
                    InetAddress inetAddress=connection.socket.getInetAddress();
                    if(pair==null) { // no previous connection or not known?
                        pair=new Pair<Sender,Reader>(null,(Reader)connection);
                        l.info(id+" "+id+"->"+newId+" received first message from: "+newId+":"+inetAddress);
                        idToPair().put(newId,pair); // sync
                        l.warning(id+" added pair: "+pair);
                    } else { // there is a previous connection
                        l.info(id+"->"+newId+" modifying pair: "+pair);
                        if(pair.first!=null) l.info(id+" pair has existing client!: "+pair);
                        if(pair.second!=null) {
                            l.warning(id+"<-"+newId+" pair has exiting receiver!: "+pair);
                            // save history?
                            //pair.second.stopThread();
                            l.warning(id+"<-"+newId+" pair was not modified!: "+pair);
                        } else {
                            pair.second=(Reader)connection;
                            l.info("merging histories: "+connection.histories);
                            l.info("with histories: "+pair.first.histories);
                            pair.first.histories.add(connection.histories);
                            connection.histories=pair.first.histories;
                            l.info("resulting histories: "+pair.first.histories);
                            l.info(id+"<-"+newId+" added receiver: "+connection+". modified pair: "+pair);
                        }
                    }
                } else if(connection instanceof Sender) {
                    if(pair==null) {
                        pair=new Pair<Sender,Reader>((Sender)connection,null);
                        l.info(id+"->"+newId+" new sender to: "+newId);
                        idToPair().put(newId,pair); // sync
                        l.info(id+" added pair: "+pair);
                    } else {
                        l.info(id+" modifying pair: "+pair);
                        if(pair.first!=null) {
                            l.warning(id+" pair has existing client!: "+pair);
                            pair.first.stopThread();
                        }
                        if(pair.second!=null) l.warning(id+" pair has existing receiver!: "+pair);
                        pair.first=(Sender)connection;
                        l.warning(id+" added sender: "+connection+", modified pair: "+pair);
                    }
                } else l.severe(id+" strange connection type: "+connection);
                l.info("new map for: "+id+" is:"+idToPair());
            }
        }
        public Sender createAndAddSender(String destinationId,SocketAddress socketAddress) {
            Sender client=null;
            try {
                l.info("trying: "+socketAddress);
                Histories histories=destinationId!=id?new Histories():this.histories;
                client=new Sender(id,destinationId,socketAddress,histories);
                client.otherId=destinationId;
                l.info("adding: "+client);
                addConnection(destinationId,client);
            } catch(IOException e) {
                e.printStackTrace();
            }
            return client;
        }
        @Override public void startHeartbeat() {
            heartbeat=new Thread(new Runnable() {
                @Override public void run() {
                    while(true) {
                        broadcast(create("heartbeat"));
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
            l.info("running: "+id+" accepting on: "+serverSocket);
            while(!isShuttingDown) {
                try {
                    Socket socket=serverSocket.accept();
                    l.info("server: "+id+" accepted connection from: "+socket);
                    // we might be able to guess the up and ser
                    InetAddress iNetAddress=socket.getInetAddress();
                    l.info("socket address: "+iNetAddress);
                    Reader receiver=new Reader(this,id,null,socket,new Histories());
                    synchronized(this) {
                        newConnections.add(receiver);
                    }
                    receiver.thread=new Thread(receiver);
                    receiver.thread.setName(socket.getRemoteSocketAddress().toString()+"->"+id);
                    receiver.thread.start();
                    // don't add yet, until we receive a message,so we know where he is listening.
                } catch(IOException e) {
                    if(isShuttingDown) {
                        l.info("server: "+id+" is shutting down");
                        if(e.toString().contains("socket closed")) l.info("0 (maybe normal) server: "+id+" caught: "+e);
                        else if(e.toString().contains("Socket is closed")) l.info("0 (maybe normal) server: "+id+" caught: "+e);
                        else l.info("1 server: "+id+" caught: "+e);
                    } else {
                        l.info("server: "+id+"is not shutting down, server caught: "+e);
                        e.printStackTrace();
                    }
                    break;
                }
            } // was in the wrong place!
            try {
                l.info(id+" closing server socket.");
                serverSocket.close();
            } catch(IOException e) {
                if(isShuttingDown) l.info("server: "+id+" shutting down, server caught: "+e);
                else {
                    l.severe("server: "+id+" not shutting down, server caught: "+e);
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
                for(Pair<Sender,Reader> pair:idToPair.values()) // how can this work!
                    if(pair.second!=null) if(pair.second.histories.receiverHistory.history.attempts()<n) done=false;
                if(done) break;
                Thread.sleep(10);
            }
            l.info(idToPair.size()+" servers complete.");
        }
        public void waitForServersToShutdown() throws IOException {
            for(Pair<Sender,Reader> pair:idToPair.values())
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
            for(Pair<Sender,Reader> pair:idToPair.values()) {
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
        @Override public Map<String,Pair<Sender,Reader>> idToPair() {
            return idToPair;
        }
        @Override public Map<InetAddress,Pair<String,Histories>> iNetAddressToPair() {
            return iNetAddressToPair();
        }
        @Override public String host() {
            return host;
        }
        @Override public int service() {
            return service;
        }
        @Override public String maps() {
            StringBuffer sb=new StringBuffer();
            sb.append("\n id to pair: "+idToPair());
            sb.append("\n socket address to pair: ");
            for(InetAddress inetAddress:iNetAddressToPair.keySet())
                sb.append(inetAddress+":"+iNetAddressToPair.get(inetAddress).first);
            return sb.toString();
        }
        @Override public String report() {
            StringBuffer sb=new StringBuffer();
            sb.append("report for: "+id()+"\n");
            //sb.append("from this: "+histories+"\n");
            sb.append("maps: "+maps()+"\n");
            synchronized(idToPair) {
                for(Pair<Sender,Reader> pair:idToPair.values()) {
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
            return "server: "+id+", "+host+":"+service+", "+idToPair;
        }
        final String id,host;
        final int service;
        final Set<SocketAddress> socketAddresses;
        Thread heartbeat;
        final Histories histories=new Histories();
        // use Map<String,Pair<Sender,Receiver>> ?
        final ServerSocket serverSocket;
        private volatile boolean isShuttingDown;
        final Set<Reader> newConnections=new LinkedHashSet<>();
        final Map<InetAddress,Pair<String,Histories>> iNetAddressToPair=new LinkedHashMap<>();
        final Map<String,Pair<Sender,Reader>> idToPair=new TreeMap<>(); // destination id!
        public static final String ok="ok";
        static int serialNumbers=0;
    }
}