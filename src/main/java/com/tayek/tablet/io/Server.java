package com.tayek.tablet.io;
import static com.tayek.tablet.io.IO.p;
import java.io.*;
import java.net.*;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.Info.Histories;
import com.tayek.tablet.Message.Type;
import com.tayek.utilities.Et;
public class Server implements Runnable {
    public static class History {
        @Override public String toString() {
            String string="server: ";
            if(server.attempts()!=0) string+=server.toString();
            else string+="no attempts";
            if(replies.attempts()!=0) string+="\nreplies: "+replies.toString();
            return string;
        }
        public final H server=new H(),replies=new H();
    }
    public Server(Tablet tablet,SocketAddress socketAddress,Receiver receiver,boolean replying,Histories both) throws IOException {
        serverSocket=new ServerSocket();
        l.info("binding to: "+socketAddress);
        serverSocket.bind(socketAddress);
        this.tablet=tablet;
        name=tablet!=null?tablet.name():null;
        this.receiver=receiver;
        this.replying=replying;
        this.history=both;
    }
    private void processMessage(String name,String string) {
        if(string!=null&&!string.isEmpty()) {
            // maybe keep the message as just a string and pass it to the receiver
            Message message=Message.staticFrom(string);
            l.fine("tablet "+name+" received: "+message+" at: "+System.currentTimeMillis());
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
                            Message ack=Message.ack(tablet.group.groupId,tablet.tabletId());
                            ackEt=new Et();
                            Future<Void> future=tablet.executeTaskAndCancelIfItTakesTooLong(tablet.new SendCallable(ack,message.tabletId,tablet.connectTimeout),tablet.connectTimeout+100,
                                    tablet.canceller);
                            break;
                        case ack:
                            p(tablet+", received ack: "+message+", after: "+ackEt);
                            l.warning(tablet+", received ack: "+message+", after: "+ackEt);
                            break;
                        case rolloverLogNow:
                            p(tablet+", received rollover: "+message+", after: "+ackEt);
                            l.severe(tablet+", received rollover: "+message+", after: "+ackEt);
                            break;
                        default:
                            if(!message.tabletId.equals(tablet.tabletId())) {
                                receiver.receive(message);
                            } else l.fine("discarding message from self");
                    }
                }
            }
        }
    }
    private void reply(Socket socket,String string) {
        if(string!=null&&!string.isEmpty()) {
            PrintWriter w;
            try {
                w=new PrintWriter(socket.getOutputStream());
                w.write("ok");
                w.flush();
                //w.close();
                history.server.replies.success();
            } catch(IOException e) {
                history.server.replies.failure(e.toString());
            }
        }
    }
    private void read(Et et,String name,Socket socket) {
        try {
            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String string;
            l.fine("#"+(history.server.server.attempts()+1)+", try to read");
            string=in.readLine();
            l.fine("#"+(history.server.server.attempts()+1)+", read, try to shudown input");
            //socket.shutdownInput();
            //in.close();
            if(string!=null&&!string.isEmpty()) history.server.server.successes();
            else {
                l.severe("received null or empty message!");
            }
            if(replying) reply(socket,string);
            l.fine("#"+(history.server.server.attempts()+1)+", read, try to shudown output");
            //socket.shutdownOutput();
            l.fine("#"+(history.server.server.attempts()+1)+", read, try to close socket");
            socket.close();
            l.fine("#"+(history.server.server.attempts()+1)+", socket closed");
            history.server.server.success();
            history.server.server.successHistogram.add(et.etms());
            history.server.server.failureHistogram.add(Double.NaN);
            processMessage(name,string);
        } catch(IOException e) {
            history.server.server.failure(e.toString());
            history.server.server.successHistogram.add(Double.NaN);
            history.server.server.failureHistogram.add(et.etms());
            l.severe("#"+(history.server.server.attempts()+1)+", tablet: "+name+", 4 server caught: "+e);
            e.printStackTrace();
        }
        if(history.server.server.attempts()>0&&reportPeriod>0&&history.server.server.attempts()%reportPeriod==0)
            if(true||history.anyFailures()) l.warning("tablet: "+name+", history from server: "+history);
        else l.warning("no failures in server (really?)&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
    }
    @Override public void run() {
        l.info("starting server");
        while(!isShuttingDown) {
            Et et=new Et();
            try {
                l.fine("tablet "+name+" is accepting on: "+serverSocket);
                Socket socket=serverSocket.accept();
                l.fine("#"+(history.server.server.attempts()+1)+", tablet "+name+" accepted connection from: "+socket.getRemoteSocketAddress());
                read(et,name,socket);
            } catch(IOException e) {
                if(isShuttingDown) {
                    l.info("tablet "+name+" is shutting down");
                    if(e.toString().contains("socket closed")) l.info("tablet "+name+"0 (maybe normal) server caught: "+e);
                    else if(e.toString().contains("Socket is closed")) l.info("tablet "+name+" 0 (maybe normal) server caught: "+e);
                    else l.warning("tablet "+name+", 1 server caught: "+e);
                } else {
                    l.warning("tablet "+name+" is not shutting down, server caught: "+e);
                    e.printStackTrace();
                }
                break;
            }
        } // was in the wrong place
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
    private final Histories history;
    private final String name;
    private Et ackEt=null;
    private volatile boolean isShuttingDown;
    public Integer reportPeriod=100;
    public final Logger l=Logger.getLogger(getClass().getName());
    public static final String ok="ok";
}
