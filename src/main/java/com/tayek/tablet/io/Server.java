package com.tayek.tablet.io;
import static com.tayek.tablet.io.IO.p;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import com.tayek.tablet.*;
import com.tayek.tablet.Histories.ServerHistory;
import com.tayek.tablet.Messages.Message;
import com.tayek.utilities.Et;
public class Server implements Runnable {
    public Server(Tablet tablet,SocketAddress socketAddress,Receiver receiver,boolean replying,ServerHistory history,Set<Integer> tabletIds,Messages messages) throws IOException {
        serverSocket=new ServerSocket();
        l.info("binding to: "+socketAddress);
        serverSocket.bind(socketAddress);
        this.tablet=tablet;
        name=tablet!=null?tablet.name():null;
        this.receiver=receiver;
        this.replying=replying;
        this.history=history;
        if(tabletIds!=null)
            for(int tabletId:tabletIds)
                lastMessageNumbers.put(tabletId,null);
        this.messages=messages;
    }
    private void processMessage(String name,String string) {
        l.info("enter process message, lastMessageNumbers: "+lastMessageNumbers+", message: "+string);
        if(string!=null&&!string.isEmpty()) {
            // maybe keep the message as just a string and pass it to the receiver
            Message message=messages.from(string);
            if(!lastMessageNumbers.containsKey(message.tabletId)) {
                l.severe("message from foreign tablet: "+message);
                p("lastMessageNumbers: "+lastMessageNumbers);
            }
            Integer lastMessageNumber=lastMessageNumbers.get(message.tabletId);
            if(lastMessageNumber!=null) {
                l.info("last: "+lastMessageNumber+", current: "+message.number);
                if(message.number==lastMessageNumber+1) history.missing.success();
                else {
                    l.warning("tablet: "+tablet+": got #"+history.server.attempts()+", expected number: "+(lastMessageNumber+1)+" from: "+message.tabletId+", but got: "+message.number);
                    p("lastMessageNumbers: "+lastMessageNumbers);
                    if(message.number<lastMessageNumber+1) {
                        l.warning("#"+history.server.attempts()+", out of order!");
                        history.missing.failure("out of order: "+(message.number-(lastMessageNumber+1)));
                    } else {
                        l.warning("#"+history.server.attempts()+", missing at least one message, expected number: "+(lastMessageNumber+1)+", but got: "+message.number);
                        history.missing.failure("missing: "+(message.number-(lastMessageNumber+1)));
                    }
                }
            } else history.missing.success(); // count first as success
            l.fine("tablet "+name+" received: "+message+" at: "+System.currentTimeMillis());
            l.info("tablet "+name+" put last #"+message.number +" into: "+message.tabletId);
            lastMessageNumbers.put(message.tabletId,message.number);
            if(tablet!=null) switch(message.type) {
                case name:
                    tablet.setName(message.string);
                    break;
                default:
                    break;
            }
            if(receiver!=null) {
                if(tablet==null) {
                    receiver.receive(message);
                    // see if we want to do something special with pings here
                } else {
                    switch(message.type) {
                        case ping:
                            l.warning("i got a ping!");
                            Message ack=tablet.group.messages.ack(tablet.group.groupId,tablet.tabletId());
                            ackEt=new Et();
                            Future<Void> future=tablet.group.executeTaskAndCancelIfItTakesTooLong(tablet.group.new SendCallable(tablet,ack,message.tabletId,tablet.group.connectTimeout),
                                    tablet.group.connectTimeout+100,tablet.group.canceller);
                            break;
                        case ack:
                            p(tablet+", received ack: "+message+", after: "+ackEt);
                            l.warning(tablet+", received ack: "+message+", after: "+ackEt);
                            ackEt=null;
                            break;
                        case rolloverLogNow:
                            p(tablet+", received rollover: "+message);
                            l.warning(tablet+", received rollover: "+message);
                            break;
                        default:
                            if(!message.tabletId.equals(tablet.tabletId())) {
                                receiver.receive(message);
                            } else l.fine("discarding message from self");
                    }
                }
            }
        }
        l.info("exit lastMessageNumbers: "+lastMessageNumbers);
    }
    private Writer reply(Socket socket,String string) {
        if(string!=null&&!string.isEmpty()) {
            PrintWriter w;
            try {
                w=new PrintWriter(socket.getOutputStream());
                w.write("ok");
                w.flush();
                history.replies.success();
                return w;
            } catch(IOException e) {
                history.replies.failure(e.toString());
            }
        } else l.warning("string is null or empty!");
        return null;
    }
    private String read(String name,Socket socket) {
        Et et=new Et();
        l.info("enter read for #"+(history.server.attempts()+1));
        String string=null;
        try {
            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            l.fine("#"+(history.server.attempts()+1)+", try to read");
            string=in.readLine();
            if(closeInput) {
                l.fine("#"+(history.server.attempts()+1)+", read, try to close input");
                in.close();
            }
            if(shutdownInput) {
                l.fine("#"+(history.server.attempts()+1)+", read, try to shudown input");
                socket.shutdownInput();
            }
            if(string!=null&&!string.isEmpty()) history.server.successes();
            else {
                l.severe("received null or empty message!");
            }
            Writer writer=null;
            if(replying) writer=reply(socket,string);
            if(closeOutput&&writer!=null) {
                l.fine("#"+(history.server.attempts()+1)+", read, try to close output");
                writer.close();
            }
            if(shutdownOutput) {
                l.fine("#"+(history.server.attempts()+1)+", read, try to shudown output");
                socket.shutdownOutput();
            }
            l.fine("#"+(history.server.attempts()+1)+", read, try to close socket");
            if(closeSocket) {
                l.fine("#"+(history.server.attempts()+1)+", read, try to close socket");
                socket.close();
                l.fine("#"+(history.server.attempts()+1)+", socket closed");
            }
            history.server.success();
            history.server.successHistogram.add(et.etms());
            history.server.failureHistogram.add(Double.NaN);
        } catch(IOException e) {
            history.server.failure(e.toString());
            history.server.successHistogram.add(Double.NaN);
            history.server.failureHistogram.add(et.etms());
            l.severe("#"+(history.server.attempts()+1)+", tablet: "+name+", 4 server caught: "+e);
            e.printStackTrace();
        }
        if(history.server.attempts()>0&&reportPeriod>0&&history.server.attempts()%reportPeriod==0) l.warning("tablet: "+name+", history from server: "+history);
        l.info("exit read for #"+history.server.attempts());
        return string;
    }
    @Override public void run() {
        l.info("starting server");
        while(!isShuttingDown) {
            try {
                l.fine("tablet "+name+" is accepting on: "+serverSocket);
                Socket socket=serverSocket.accept();
                l.fine("#"+(history.server.attempts()+1)+", tablet "+name+" accepted connection from: "+socket.getRemoteSocketAddress());
                Et et=new Et();
                synchronized(history) { // try to find missing
                    l.info("#"+(history.server.attempts()+1)+", tablet "+name+" time to sync: "+et);
                    String string=read(name,socket);
                    if(string!=null&&!string.isEmpty()) processMessage(name,string);
                    l.info("#"+history.server.attempts()+", tablet "+name+" time to sync and read: "+et);
                } // maybe we do not need to sync this?
            } catch(IOException e) {
                if(isShuttingDown) {
                    l.info("tablet "+name+" is shutting down");
                    if(e.toString().contains("socket closed")) l.info("tablet "+name+" 0 (maybe normal) server caught: "+e);
                    else if(e.toString().contains("Socket is closed")) l.info("tablet "+name+" 0 (maybe normal) server caught: "+e);
                    else l.warning("tablet "+name+", 1 server caught: "+e);
                } else {
                    l.warning("tablet "+name+" is not shutting down, server caught: "+e);
                    e.printStackTrace();
                }
                break;
            }
        } // was in the wrong place!
        try {
            l.info("tablet "+name+" is closing server socket.");
            serverSocket.close();
        } catch(IOException e) {
            if(isShuttingDown) l.warning("tablet "+name+", shutting down, server caught: "+e);
            else {
                l.severe("tablet "+name+", not shutting down, server caught: "+e);
                e.printStackTrace();
            }
        }
    }
    public void startServer() { // long running thread
        if(thread!=null) {
            stopServer();
        }
        thread=new Thread(this,name+" server");
        isShuttingDown=false;
        thread.start();
    }
    public void stopServer() {
        l.info("tablet "+name+" stopping server");
        isShuttingDown=true;
        try {
            l.fine("tablet "+name+", closing server socket");
            serverSocket.close();
            l.fine("tablet "+name+", server socket closed");
        } catch(IOException e) {
            l.severe("tablet "+name+" caught: "+e);
            e.printStackTrace();
        }
        if(true) {
            l.fine("tablet "+name+", joining with: "+thread+", threadinterrupted: "+thread.isInterrupted()+", alive: "+thread.isAlive());
            try {
                thread.join();
            } catch(InterruptedException e) {
                l.warning("tablet "+name+", join interrupted!");
            }
        }
        thread=null;
    }
    private Thread thread;
    private final Tablet tablet;
    private final ServerSocket serverSocket;
    private final boolean replying;
    private final Receiver receiver;
    private final ServerHistory history;
    private final String name;
    private final Messages messages;
    private final Map<Integer,Integer> lastMessageNumbers=new TreeMap<>();
    boolean shutdownInput,shutdownOutput,closeInput,closeOutput,closeSocket=true;
    private Et ackEt=null;
    private volatile boolean isShuttingDown;
    public Integer reportPeriod=Group.defaultReportPeriod;
    public final Logger l=Logger.getLogger(getClass().getName());
    public static final String ok="ok";
}
