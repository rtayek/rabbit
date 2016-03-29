package com.tayek.tablet.io;
import static com.tayek.utilities.Utility.*;
import java.io.*;
import java.net.*;
import com.tayek.io.IO.ShutdownOptions;
import com.tayek.tablet.*;
import com.tayek.tablet.Main.Stuff;
import com.tayek.utilities.Et;
import static com.tayek.io.IO.*;
public class Server implements Runnable {
    public Server(Object iD,SocketAddress socketAddress,Receiver receiver,Stuff stuff,Histories history) throws IOException {
        this(iD,serverSocket(socketAddress),receiver,stuff,history);
    }
    public Server(Object iD,ServerSocket serverSocket,Receiver receiver,Stuff stuff,Histories history) {
        this.serverSocket=serverSocket;
        this.id=iD;
        this.receiver=receiver;
        this.stuff=stuff;
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
                history.server.replies.success();
                return w;
            } catch(IOException e) {
                history.server.replies.failure(e.toString());
            }
        } else l.warning("string is null or empty!");
        return null;
    }
    private String read(Object iD,Socket socket) {
        Et et=new Et();
        l.info("enter read for #"+(history.server.server.attempts()+1));
        String string=null;
        boolean useTimeout=false;
        try {
            if(useTimeout) socket.setSoTimeout(200);
            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            l.fine("#"+(history.server.server.attempts()+1)+", try to read");
            string=in.readLine();
            if(string==null||string.isEmpty()) {
                p("1eof or empty string!");
                throw new RuntimeException("eof or empty string");
            }
            if(shutdownOptions.closeInput) {
                l.fine("#"+(history.server.server.attempts()+1)+", read, try to close input");
                in.close();
            }
            if(shutdownOptions.shutdownInput) {
                l.fine("#"+(history.server.server.attempts()+1)+", read, try to shudown input");
                socket.shutdownInput();
            }
            if(string!=null&&!string.isEmpty()) history.server.server.successes();
            else {
                l.severe("received null or empty message!");
            }
            Writer writer=null;
            if(stuff.replying) writer=reply(socket,string);
            if(shutdownOptions.closeOutput&&writer!=null) {
                l.fine("#"+(history.server.server.attempts()+1)+", read, try to close output");
                writer.close();
            }
            if(shutdownOptions.shutdownOutput) {
                l.fine("#"+(history.server.server.attempts()+1)+", read, try to shudown output");
                socket.shutdownOutput();
            }
            l.fine("#"+(history.server.server.attempts()+1)+", read, try to close socket");
            if(shutdownOptions.closeSocket) {
                l.fine("#"+(history.server.server.attempts()+1)+", read, try to close socket");
                socket.close();
                l.fine("#"+(history.server.server.attempts()+1)+", socket closed");
            }
            history.server.server.success();
            history.server.server.successHistogram.add(et.etms());
            history.server.server.failureHistogram.add(Double.NaN);
        } catch(IOException e) {
            history.server.server.failure(e.toString());
            history.server.server.successHistogram.add(Double.NaN);
            history.server.server.failureHistogram.add(et.etms());
            l.severe("#"+(history.server.server.attempts()+1)+", "+iD+", 4 server caught: "+e);
            e.printStackTrace();
        } catch(Exception e) {
            history.server.server.failure(e.toString());
            history.server.server.successHistogram.add(Double.NaN);
            history.server.server.failureHistogram.add(et.etms());
            l.severe("#"+(history.server.server.attempts()+1)+", "+iD+", 4 server caught: "+e);
            e.printStackTrace();
        }
        if(string==null||string.isEmpty()) p("return eof or empty string!");
        l.info("exit read for #"+history.server.server.attempts());
        return string;
    }
    @Override public void run() {
        l.info("starting server");
        while(!isShuttingDown) {
            try {
                l.fine(id+" is accepting on: "+serverSocket);
                Socket socket=serverSocket.accept();
                l.fine("#"+(history.server.server.attempts()+1)+", "+id+" accepted connection from: "+socket.getRemoteSocketAddress());
                Et et=new Et();
                synchronized(history) { // try to find missing
                    l.info("#"+(history.server.server.attempts()+1)+", "+id+" time to sync: "+et);
                    String string=read(id,socket);
                    if(string==null||string.isEmpty()) l.severe("server: "+id+", read eof or empty string!");
                    if(receiver!=null) if(string!=null) receiver.receive(string);
                    l.info("#"+history.server.server.attempts()+", "+id+" time to sync and read: "+et);
                    if(history.server.server.attempts()>0&&reportPeriod>0&&history.server.server.attempts()%reportPeriod==0) l.warning(id+", history from server: "+history);
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
            if(stuff.reportPeriod>0&&history.anyAttempts()&&history.server.server.attempts()%stuff.reportPeriod==0) l.warning("report history from server: "+id+": "+history);
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
        l.info(id+" stopping server");
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
    private final Stuff stuff;
    private final Receiver receiver;
    private final Histories history;
    private volatile boolean isShuttingDown;
    public final ShutdownOptions shutdownOptions=new ShutdownOptions();
    public Integer reportPeriod=Histories.defaultReportPeriod;
    public static final String ok="ok";
}
