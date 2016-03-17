package com.tayek.tablet.io;
import static com.tayek.tablet.io.IO.p;
import java.io.*;
import java.net.*;
import java.util.logging.Logger;
import com.tayek.tablet.*;
import com.tayek.tablet.Histories.ServerHistory;
import com.tayek.utilities.Et;
import static com.tayek.utilities.Utility.*;
public class Server implements Runnable {
    public Server(Object iD,SocketAddress socketAddress,Receiver receiver,boolean replying,ServerHistory history) throws IOException {
        this(iD,serverSocket(socketAddress),receiver,replying,history);
    }
    public static ServerSocket serverSocket(SocketAddress socketAddress) throws IOException {
        ServerSocket serverSocket=new ServerSocket();
        staticLogger.info("binding to: "+socketAddress);
        serverSocket.bind(socketAddress);
        return serverSocket;
    }
    public Server(Object iD,ServerSocket serverSocket,Receiver receiver,boolean replying,ServerHistory history) {
        this.serverSocket=serverSocket;
        this.iD=iD;
        this.receiver=receiver;
        this.replying=replying;
        this.history=history;
        //p("server ctor: "+method(3));
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
    private String read(Object iD,Socket socket) {
        Et et=new Et();
        l.info("enter read for #"+(history.server.attempts()+1));
        String string=null;
        boolean useTimeout=false;
        try {
            if(useTimeout) socket.setSoTimeout(200);
            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            l.fine("#"+(history.server.attempts()+1)+", try to read");
            string=in.readLine();
            if(string==null||string.isEmpty()) {
                p("1eof or empty string!");
                throw new RuntimeException("eof or empty string");
            }
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
            l.severe("#"+(history.server.attempts()+1)+", tablet: "+iD+", 4 server caught: "+e);
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
        if(history.server.attempts()>0&&reportPeriod>0&&history.server.attempts()%reportPeriod==0) l.warning("tablet: "+iD+", history from server: "+history);
        l.info("exit read for #"+history.server.attempts());
        if(string==null||string.isEmpty()) p("return eof or empty string!");
        return string;
    }
    @Override public void run() {
        l.info("starting server");
        while(!isShuttingDown) {
            try {
                l.fine("tablet "+iD+" is accepting on: "+serverSocket);
                Socket socket=serverSocket.accept();
                l.fine("#"+(history.server.attempts()+1)+", "+iD+" accepted connection from: "+socket.getRemoteSocketAddress());
                Et et=new Et();
                synchronized(history) { // try to find missing
                    l.info("#"+(history.server.attempts()+1)+", "+iD+" time to sync: "+et);
                    String string=read(iD,socket);
                    if(string==null||string.isEmpty()) l.severe("2eof or empty string!");
                    if(receiver!=null) receiver.receive(string);
                    l.info("#"+history.server.attempts()+", "+iD+" time to sync and read: "+et);
                } // maybe we do not need to sync this?
            } catch(IOException e) {
                if(isShuttingDown) {
                    l.info(iD+" is shutting down");
                    if(e.toString().contains("socket closed")) l.info(iD+" 0 (maybe normal) server caught: "+e);
                    else if(e.toString().contains("Socket is closed")) l.info(iD+" 0 (maybe normal) server caught: "+e);
                    else l.warning(iD+", 1 server caught: "+e);
                } else {
                    l.warning(iD+" is not shutting down, server caught: "+e);
                    e.printStackTrace();
                }
                break;
            }
        } // was in the wrong place!
        try {
            l.info(iD+" is closing server socket.");
            serverSocket.close();
        } catch(IOException e) {
            if(isShuttingDown) l.warning(iD+", shutting down, server caught: "+e);
            else {
                l.severe(iD+", not shutting down, server caught: "+e);
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
    public void startServer() { // long running thread
        if(thread!=null) {
            stopServer();
        }
        thread=new Thread(this,iD+" server");
        isShuttingDown=false;
        thread.start();
    }
    public void stopServer() {
        l.info(iD+" stopping server");
        isShuttingDown=true;
        try {
            l.fine(iD+", closing server socket");
            serverSocket.close();
            l.fine(iD+", server socket closed");
        } catch(IOException e) {
            l.severe(iD+" caught: "+e);
            e.printStackTrace();
        }
        if(true) {
            l.fine(iD+", joining with: "+thread+", threadinterrupted: "+thread.isInterrupted()+", alive: "+thread.isAlive());
            try {
                thread.join();
            } catch(InterruptedException e) {
                l.warning(iD+", join interrupted!");
            }
        }
        thread=null;
    }
    private Thread thread;
    public final Object iD;
    private final ServerSocket serverSocket;
    private final boolean replying;
    private final Receiver receiver;
    private final ServerHistory history;
    boolean shutdownInput,shutdownOutput,closeInput,closeOutput,closeSocket=true;
    private volatile boolean isShuttingDown;
    public Integer reportPeriod=Histories.defaultReportPeriod;
    public final Logger l=Logger.getLogger(getClass().getName());
    public static final String ok="ok";
    public static final Logger staticLogger=Logger.getLogger(Server.class.getName());
}
