package com.tayek.conrad;
import static com.tayek.utilities.Utility.p;
import java.io.*;
import java.net.*;
import java.util.logging.*;
import com.tayek.utilities.Et;
abstract class Connection implements Runnable {
    Connection(Object id,Socket socket) {
        this.id=id;
        this.socket=socket;
    }
    public synchronized void startThread() throws IOException {
        if(thread!=null) stopThread();
        thread=new Thread(this,id.toString());
        isShuttingDown=false;
        thread.start();
    }
    public synchronized void stopThread() throws IOException {
        l.info(id+", enter stop().");
        if(thread==null) {
            l.fine(id+", no thread to stop!");
        } else {
            isShuttingDown=true;
            if(!socket.isClosed()) {
                l.fine(id+", closing socket.");
                socket.close(); // probably need to check something before doing this
                l.fine(id+", socket closed.");
            } else l.fine(id+", socket is already closed.");
            if(!Thread.currentThread().equals(thread)) {
                l.info(id+", joining with: "+thread+", threadinterrupted: "+thread.isInterrupted()+", alive: "+thread.isAlive());
                if(thread.getState().equals(Thread.State.WAITING)) thread.interrupt();
                try {
                    thread.join();
                } catch(InterruptedException e) {
                    l.warning(id+", join interrupted!");
                }
            } else l.fine(id+", not joining with self.");
            thread=null;
        }
        l.info(id+", exit stop().");
    }
    public double shutdown() throws IOException {
        Et et=new Et();
        stopThread();
        return et.etms();
    }
    final Object id;
    final Socket socket;
    volatile protected Thread thread;
    volatile boolean isShuttingDown;
    public static final Logger l=Logger.getLogger(Client.class.getName());
}
class Client extends Connection {
    Client(Object id,Socket socket,Histories histories) throws IOException {
        super(id,socket);
        this.histories=histories;
        out=new OutputStreamWriter(socket.getOutputStream());
    }
    private void readReply(String string) { // client
        Et et=new Et();
        l.fine("#"+histories.client.client.attempts()+"reading reply to: "+string+" at: "+System.currentTimeMillis());
        try {
            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String reply=in.readLine();
            if(reply!=null&&!reply.isEmpty()) if(!reply.equals(Server.ok)) l.warning("no ack!");
            else l.fine("#"+histories.client.client.attempts()+", client received: "+reply+", took: "+et);
            else l.warning("#"+histories.client.client.attempts()+", reply is null or empty!");
            histories.client.replies.success();
            l.fine("#"+histories.client.client.attempts()+", got reply to: "+string+" at: "+System.currentTimeMillis());
        } catch(IOException e) {
            l.warning("reading reply caught: "+e);
            histories.client.replies.failure(e.toString());
        }
    }
    public boolean send(String string) { // client
        l.fine("sending: "+string);
        synchronized(histories) {
            if(string.contains("\n")) l.severe(string+" contains a linefeed!");
            Et et=new Et();
            if(socket!=null) try {
                out.write(string+"\n");
                out.flush();
                histories.client.client.success();
                l.fine("#"+histories.client.client.attempts()+", sent: "+string+" at: "+System.currentTimeMillis()+" took: "+et);
                double etms=et.etms();
                histories.client.client.successHistogram.add(etms);
                histories.client.client.failureHistogram.add(Double.NaN);
                if(replying) {
                    readReply(string);
                }
                return true;
            } catch(SocketException e) {
                l.warning("#"+(histories.client.client.attempts()+1)+", 1 send to: "+socket.getRemoteSocketAddress()+" failed with: '"+e+"'");
                histories.client.client.failure(e.toString());
                e.printStackTrace();
            } catch(IOException e) {
                l.warning("#"+(histories.client.client.attempts()+1)+", 2 send to: "+socket.getRemoteSocketAddress()+" failed with: '"+e+"'");
                histories.client.client.failure(e.toString());
                e.printStackTrace();
            }
            else {
                l.fine("#"+histories.client.client.attempts()+"send failed in: "+et);
            }
            double etms=et.etms();
            histories.client.client.successHistogram.add(Double.NaN);
            histories.client.client.failureHistogram.add(etms);
            return false;
        }
    }
    @Override public void run() {
        Et et=new Et();
        for(int i=1;i<=messagesToSend;i++) {
            send(i+" "+line);
            try {
                Thread.sleep(0);
            } catch(InterruptedException e) {
                l.warning(id+", caught: "+e);
            }
        }
        l.info(id+", sent: "+messagesToSend+" messages in: "+et);
        try {
            stopThread();
        } catch(IOException e) {
            e.printStackTrace();
        }
        l.info(id+", exit run()");
    }
    static Client createClientAndStart(Object id) throws UnknownHostException,IOException {
        Socket socket=new Socket(Main.host,Main.service);
        Histories histories=new Histories();
        Client client=new Client(id,socket,histories);
        client.startThread();
        return client;
    }
    public static void main(String[] args) throws UnknownHostException,IOException,InterruptedException {
        Level level=Level.WARNING;
        Service.l.setLevel(level);
        Client.l.setLevel(level);
        Server.l.setLevel(level);
        Main.l.setLevel(level);
        Histories.defaultReportPeriod=0;
        SocketAddress socketAddress=new InetSocketAddress(Main.host,Main.service);
    }
    public Integer reportPeriod=Histories.defaultReportPeriod;
    final Histories histories;
    final boolean replying=false;
    final Writer out;
    static int messagesToSend=1000;
    static final String line;
    static {
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<1_024;i++)
            sb.append('1');
        line=sb.toString();
    }
}
