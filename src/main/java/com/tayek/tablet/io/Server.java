package com.tayek.tablet.io;
import static com.tayek.utilities.Utility.*;
import java.io.*;
import java.net.*;
import com.tayek.*;
import com.tayek.io.IO.ShutdownOptions;
import com.tayek.tablet.*;
import com.tayek.Tablet.*;
import com.tayek.utilities.Et;
import static com.tayek.io.IO.*;
public class Server implements Runnable {
    public Server(Object iD,SocketAddress socketAddress,Receiver receiver,Config config,Histories history) throws IOException {
        this(iD,serverSocket(socketAddress),receiver,config,history);
    }
    public Server(Object iD,ServerSocket serverSocket,Receiver receiver,Config config,Histories histories) {
        this.serverSocket=serverSocket;
        this.id=iD;
        this.receiver=receiver;
        this.config=config;
        this.histories=histories;
        //p("server ctor: "+method(3));
    }
    private Writer reply(Socket socket,String string) {
        if(string!=null&&!string.isEmpty()) {
            PrintWriter w;
            try {
                w=new PrintWriter(socket.getOutputStream());
                w.write("ok");
                w.flush();
                histories.receiverHistory.replies.success();
                return w;
            } catch(IOException e) {
                histories.receiverHistory.replies.failure(e.toString());
            }
        } else l.warning("string is null or empty!");
        return null;
    }
    private String read(Object iD,Socket socket) {
        Et et=new Et();
        l.info("enter read for #"+(histories.receiverHistory.history.attempts()+1));
        String string=null;
        boolean useTimeout=false;
        try {
            if(useTimeout) socket.setSoTimeout(200);
            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            l.fine("#"+(histories.receiverHistory.history.attempts()+1)+", try to read");
            string=in.readLine();
            if(string==null||string.isEmpty()) {
                l.warning("1eof or empty string!");
                if(!isShuttingDown) throw new RuntimeException("eof or empty string");
            }
            if(shutdownOptions.closeInput) {
                l.fine("#"+(histories.receiverHistory.history.attempts()+1)+", read, try to close input");
                in.close();
            }
            if(shutdownOptions.shutdownInput) {
                l.fine("#"+(histories.receiverHistory.history.attempts()+1)+", read, try to shudown input");
                socket.shutdownInput();
            }
            if(string!=null&&!string.isEmpty()) histories.receiverHistory.history.successes(); // ??
            else {
                l.severe("received null or empty message!");
            }
            Writer writer=null;
            l.info("read: "+string);
            if(config.replying) writer=reply(socket,string);
            if(shutdownOptions.closeOutput&&writer!=null) {
                l.fine("#"+(histories.receiverHistory.history.attempts()+1)+", read, try to close output");
                writer.close();
            }
            if(shutdownOptions.shutdownOutput) {
                l.fine("#"+(histories.receiverHistory.history.attempts()+1)+", read, try to shudown output");
                socket.shutdownOutput();
            }
            l.fine("#"+(histories.receiverHistory.history.attempts()+1)+", read, try to close socket");
            if(shutdownOptions.closeSocket) {
                l.fine("#"+(histories.receiverHistory.history.attempts()+1)+", read, try to close socket");
                socket.close();
                l.fine("#"+(histories.receiverHistory.history.attempts()+1)+", socket closed");
            }
            histories.receiverHistory.history.success();
            histories.receiverHistory.history.successHistogram.add(et.etms());
            histories.receiverHistory.history.failureHistogram.add(Double.NaN);
        } catch(IOException e) {
            histories.receiverHistory.history.failure(e.toString());
            histories.receiverHistory.history.successHistogram.add(Double.NaN);
            histories.receiverHistory.history.failureHistogram.add(et.etms());
            l.severe("#"+(histories.receiverHistory.history.attempts()+1)+", "+iD+", 4 server caught: "+e);
            e.printStackTrace();
        } catch(Exception e) {
            histories.receiverHistory.history.failure(e.toString());
            histories.receiverHistory.history.successHistogram.add(Double.NaN);
            histories.receiverHistory.history.failureHistogram.add(et.etms());
            l.severe("#"+(histories.receiverHistory.history.attempts()+1)+", "+iD+", 4 server caught: "+e);
            e.printStackTrace();
        }
        if(string==null||string.isEmpty()) p("return eof or empty string!");
        l.info("exit read for #"+histories.receiverHistory.history.attempts());
        return string;
    }
    @Override public void run() {
        l.info("starting server");
        while(!isShuttingDown) {
            try {
                l.fine(id+" is accepting on: "+serverSocket);
                Socket socket=serverSocket.accept();
                l.fine("#"+(histories.receiverHistory.history.attempts()+1)+", "+id+" accepted connection from: "+socket.getRemoteSocketAddress());
                Et et=new Et();
                synchronized(histories) { // try to find missing
                    l.info("#"+(histories.receiverHistory.history.attempts()+1)+", "+id+" time to sync: "+et);
                    String string=read(id,socket);
                    if(string==null||string.isEmpty()) if(!isShuttingDown) l.severe("server: "+id+", read eof or empty string!");
                    if(receiver!=null) if(string!=null) receiver.receive(string);
                    l.info("#"+histories.receiverHistory.history.attempts()+", "+id+" time to sync and read: "+et);
                } // maybe we do not need to sync this?
            } catch(SocketException e) {
                if(isShuttingDown) {
                    l.info(id+" is shutting down");
                    if(e.toString().contains("socket closed")) l.info(id+" 0 (maybe normal) server caught: "+e);
                    else if(e.toString().contains("Socket is closed")) l.info(id+" 0 (maybe normal) server caught: "+e);
                    else l.warning(id+", 1 server caught: "+e);
                } else {
                    l.warning(id+" is not shutting down, server caught: "+e);
                    e.printStackTrace();
                }
                break;
            } catch(IOException e) {
                if(isShuttingDown) {
                    l.info(id+" is shutting down");
                    if(e.toString().contains("socket closed")) l.info(id+" 0 (maybe normal) server caught: "+e);
                    else if(e.toString().contains("Socket is closed")) l.info(id+" 0 (maybe normal) server caught: "+e);
                    else l.warning(id+", 1 server caught: "+e);
                } else {
                    l.warning(id+" is not shutting down, server caught: "+e);
                    e.printStackTrace();
                }
                break;
            } catch(Exception e) {
                if(isShuttingDown) {
                    l.info(id+" is shutting down");
                    if(e.toString().contains("socket closed")) l.info(id+" 0 (maybe normal) server caught: "+e);
                    else if(e.toString().contains("Socket is closed")) l.info(id+" 0 (maybe normal) server caught: "+e);
                    else l.warning(id+", 1 server caught: "+e);
                } else {
                    l.warning(id+" is not shutting down, server caught: "+e);
                    e.printStackTrace();
                }
                break;
            }
            if(histories.reportPeriod>0&&histories.anyAttempts()&&histories.receiverHistory.history.attempts()%histories.reportPeriod==0) l.warning("history from server: "+id+": "+histories.toString("server"));
        } // was in the wrong place!
        try {
            l.info(id+" is closing server socket.");
            serverSocket.close();
        } catch(IOException e) {
            if(isShuttingDown) l.warning(id+", shutting down, server caught: "+e);
            else {
                l.severe(id+", not shutting down, server caught: "+e);
                e.printStackTrace();
                l.severe(id+"server socket caught:"+e);
                l.severe(id+"how to restart server socket?");
            }
        }
    }
    public void startServer() { // long running thread
        if(thread!=null) {
            stopServer();
        }
        thread=new Thread(this,id+" server");
        isShuttingDown=false;
        thread.start();
    }
    public void stopServer() {
        l.warning(id+" stopping server");
        isShuttingDown=true;
        try {
            l.fine(id+", closing server socket");
            serverSocket.close();
            l.fine(id+", server socket closed");
        } catch(IOException e) {
            l.severe(id+" caught: "+e);
            e.printStackTrace();
        }
        if(true) {
            l.fine(id+", joining with: "+thread+", threadinterrupted: "+thread.isInterrupted()+", alive: "+thread.isAlive());
            try {
                thread.join();
            } catch(InterruptedException e) {
                l.warning(id+", join interrupted!");
            }
        }
        thread=null;
    }
    private Thread thread;
    public final Object id;
    private final ServerSocket serverSocket;
    private final Config config;
    private final Receiver receiver;
    private final Histories histories;
    private volatile boolean isShuttingDown;
    public final ShutdownOptions shutdownOptions=new ShutdownOptions();
    public Integer reportPeriod=Histories.defaultReportPeriod;
    public static final String ok="ok";
}
