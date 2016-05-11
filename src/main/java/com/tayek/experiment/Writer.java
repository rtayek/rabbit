package com.tayek.experiment;
import static com.tayek.utilities.Utility.*;
import java.io.*;
import java.net.*;
import java.util.logging.*;
import com.tayek.*;
import com.tayek.io.IO;
import com.tayek.utilities.*;
import static com.tayek.io.IO.*;
abstract class Connection implements Runnable {
    Connection(String id,Socket socket,Histories histories) {
        if(socket==null) throw new RuntimeException(id+", socket is null!");
        this.id=id;
        this.socket=socket;
        this.histories=histories;
    }
    synchronized void stopThread() {
        l.info(id+": "+this+": enter stop().");
        if(thread==null) {
            l.info(id+": "+this+": no thread to stop!");
        } else {
            l.info(id+": "+this+": stopping thread: "+thread);
            isShuttingDown=true;
            if(!socket.isClosed()) {
                l.info(id+": "+this+": closing socket: "+socket);
                try {
                    socket.close();
                    // causes other thread to wait? 
                } catch(IOException e) {
                    e.printStackTrace();
                }
                l.info(id+": "+this+": socket is closed: "+socket);
            } else l.info(id+": "+this+": socket is already closed.");
            if(!Thread.currentThread().equals(thread)) {
                l.info(id+": "+this+": joining with: "+IO.toString(thread));
                if(thread.getState().equals(Thread.State.WAITING)) {
                    l.warning(id+": "+this+": interrupting: "+IO.toString(thread));
                    thread.interrupt();
                    l.warning(id+": "+this+": after interrupting: "+IO.toString(thread));
                }
                try {
                    l.info(id+"->"+otherId+": waiting to join with: "+IO.toString(thread));
                    thread.join();
                    l.info(id+"->"+otherId+": joined with "+IO.toString(thread));
                } catch(InterruptedException e) {
                    l.warning(id+"->"+otherId+": join interrupted!");
                }
            } else l.warning(id+"->"+otherId+": not joining with self.");
            thread=null;
        }
        l.info(id+"->"+otherId+":exit stop().");
    }
    final String id;
    String otherId;
    final Socket socket;
    volatile protected Thread thread;
    volatile boolean isShuttingDown;
    Histories histories;
    // maybe put histories here?
}
public class Writer extends Connection { // Consumer<Message>
    Writer(String id,String otherId,Required required) throws IOException {
        this(id,otherId,required,connect(new InetSocketAddress(required.host,required.service),timeout));
    }
    private Writer(String id,String otherId,Required required,Socket socket) throws IOException {
        super(id,socket,required.histories());
        this.otherId=otherId;
        this.required=required;
        out=new OutputStreamWriter(socket.getOutputStream());
    }
    private void readReply(Object message) {
        Et et=new Et();
        l.fine(this+"#"+histories.senderHistory.history.attempts()+"reading reply to: "+message+" at: "+System.currentTimeMillis());
        try {
            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String reply=in.readLine();
            if(reply!=null&&!reply.isEmpty()) if(!reply.equals(Reader.ok)) l.warning("no ack!");
            else l.fine(this+" #"+histories.senderHistory.history.attempts()+", client received: "+reply+", took: "+et);
            else l.warning(this+" #"+histories.senderHistory.history.attempts()+", reply is null or empty!");
            histories.senderHistory.replies.success();
            l.fine(this+" #"+histories.senderHistory.history.attempts()+", got reply to: "+message+" at: "+System.currentTimeMillis());
        } catch(IOException e) {
            l.warning("reading reply caught: "+e);
            histories.senderHistory.replies.failure(e.toString());
        }
    }
    public boolean write(Object message) {
        String string=message.toString();
        l.fine(this+" sending: "+message);
        synchronized(histories) {
            if(string.contains("\n")) l.severe(string+" contains a linefeed!");
            Et et=new Et();
            if(socket!=null) try {
                out.write(string+"\n");
                out.flush();
                histories.senderHistory.history.reportSuccess(et);
                l.fine(this+" #"+histories.senderHistory.history.attempts()+", sent: "+string+" at: "+System.currentTimeMillis()+" took: "+et);
                if(replying) {
                    readReply(string);
                }
                return true;
            } catch(Exception e) {
                l.warning(this+" #"+(histories.senderHistory.history.attempts()+1)+", 1 send to: "+socket.getRemoteSocketAddress()+" failed with: '"+e+"'");
                histories.senderHistory.history.reportFailure(et,e.toString());
                e.printStackTrace();
            }
            else {
                l.fine(this+" #"+histories.senderHistory.history.attempts()+"send failed in: "+et);
                histories.senderHistory.history.reportFailure(et,"socket is null!");
            }
            return false;
        }
    }
    @Override public void run() { // not used unless we have continuous sending
        Et et=new Et();
        for(int i=1;i<=messagesToSend;i++) {
            write(i+" "+line);
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {
                l.warning(this+" caught: "+e);
            }
        }
        l.info(this+" sent: "+messagesToSend+" messages of length: "+lineLength+" in: "+et);
        l.info(this+" calling stop thread from run()!");
        if(!isShuttingDown) stopThread(); // avoid deadlock
        l.info(this+" exit run()");
    }
    @Override public String toString() {
        return "sender"+histories.sn()+":"+id+"->"+otherId;
    }
    public Integer reportPeriod=Histories.defaultReportPeriod;
    public final Required required;
    final boolean replying=false;
    private final java.io.Writer out;
    static int messagesToSend=1_000;
    static int timeout=1_000;
    static int lineLength=1024;
    public static final String line;
    static {
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<lineLength;i++)
            sb.append('1');
        line=sb.toString();
    }
}