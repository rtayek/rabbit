package com.tayek.tablet;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.Messages.*;
import com.tayek.tablet.Receiver.Model;
import com.tayek.tablet.io.*;
import static com.tayek.tablet.io.IO.*;
import com.tayek.utilities.Et;
public interface Sender {
    boolean send(Message message,Histories.ClientHistory history);
    // maybe history belongs here?
    public static class Client implements Sender {
        public Client(SocketAddress socketAddress,boolean replying,int timeout) {
            this.socketAddress=socketAddress;
            this.replying=replying;
            this.timeout=timeout;
        }
        public boolean send(Message message,Histories.ClientHistory history) {
            synchronized(history) {
                l.fine("#"+(history.client.attempts()+1)+", connecting to: "+socketAddress+", with timeout: "+timeout);
                Et et=new Et();
                Socket socket=connect(socketAddress,timeout,history);
                if(socket!=null) try {
                    l.fine("#"+(history.client.attempts()+1)+", connect took: "+et);
                    Writer out=new OutputStreamWriter(socket.getOutputStream());
                    out.write(message.toString()+"\n");
                    out.flush();
                    // add stuff from server for closing/shutting down input and output
                    //out.close();
                    //socket.shutdownOutput(); // closing the writer does this
                    history.client.success();
                    l.fine("#"+history.client.attempts()+", sent: "+message+" at: "+System.currentTimeMillis()+" took: "+et);
                    //Toaster.toaster.toast("sent: "+message+" at: "+System.currentTimeMillis());
                    if(replying) {
                        l.fine("#"+history.client.attempts()+"reading reply to: "+message+" at: "+System.currentTimeMillis());
                        try {
                            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String string=in.readLine();
                            if(string!=null&&!string.isEmpty()) if(!string.equals(Server.ok)) l.warning("no ack!");
                            else l.fine("#"+history.client.attempts()+", client received: "+string+", took: "+et);
                            else l.warning("#"+history.client.attempts()+", string is null or empty!");
                            history.replies.success();
                            l.fine("#"+history.client.attempts()+", got reply to: "+message+" at: "+System.currentTimeMillis());
                        } catch(IOException e) {
                            l.warning("reading reply caught: "+e);
                            history.replies.failure(e.toString());
                        }
                    }
                    //l.fine("#"+(history.client.attempts()+1)+", try to shutdown input.");
                    //socket.shutdownInput();
                    l.fine("#"+history.client.attempts()+", try to close socket.");
                    socket.close();
                    l.fine("#"+history.client.attempts()+", socket close succeeds.");
                    l.fine("#"+history.client.attempts()+", 0 send to: "+socketAddress+" completed in: "+et);
                    double etms=et.etms();
                    history.client.successHistogram.add(etms);
                    history.client.failureHistogram.add(Double.NaN);
                    return true;
                } catch(SocketException e) {
                    l.warning("#"+(history.client.attempts()+1)+", 1 send to: "+socketAddress+" failed with: '"+e+"'");
                    history.client.failure(e.toString());
                    e.printStackTrace();
                } catch(IOException e) {
                    l.warning("#"+(history.client.attempts()+1)+", 2 send to: "+socketAddress+" failed with: '"+e+"'");
                    history.client.failure(e.toString());
                    e.printStackTrace();
                }
                else {
                    l.fine("#"+history.client.attempts()+"send failed in: "+et);
                }
                double etms=et.etms();
                history.client.successHistogram.add(Double.NaN);
                history.client.failureHistogram.add(etms);
                return false;
            }
        }
        public static Socket connect(SocketAddress socketAddress,int timeout,Histories.ClientHistory history) {
            staticLogger.fine("connecting to: "+socketAddress+" with timeout: "+timeout);
            Socket socket=new Socket();
            Et et=new Et();
            try {
                socket.connect(socketAddress,timeout);
                return socket;
            } catch(SocketTimeoutException e) {
                history.client.failure(e.toString());
                history.client.successHistogram.add(Double.NaN);
                history.client.failureHistogram.add(et.etms());
                staticLogger.warning("#"+(history.client.attempts()+1)+", after: "+et+", with timeout: "+timeout+", caught: '"+e+"'");
            } catch(IOException e) {
                history.client.failure(e.toString());
                history.client.successHistogram.add(Double.NaN);
                history.client.failureHistogram.add(et.etms());
                staticLogger.warning("#"+(history.client.attempts()+1)+", after: "+et+", with timeout: "+timeout+", caught: '"+e+"'");
            }
            return null;
        }
        // make another driver for pc tablets
        public static void driveRealTabletsFromPcTablet() throws InterruptedException {
            Map<Integer,Info> infos=new Groups().groups.get("g0");
            infos.put(99,new Info("T99","localhost",Main.defaultReceivePort));
            Group group=new Group(1,infos,Model.mark1,false);
            Tablet tablet=group.create(99);
            group=null; // tablet has a clone of group;
            int n=100;
            tablet.group.reportPeriod=n;
            tablet.startListening();
            tablet.server.reportPeriod=n;
            tablet.drive(n,group.driveWait);
            tablet.stopListening();
            tablet.accumulateToAll();
            tablet.l.severe("start histories()");
            tablet.l.severe(tablet.group.histories(tablet));
            tablet.l.severe("start histories()");
            p("start histories()");
            p(tablet.group.histories(tablet));
            p("start histories()");
        }
        public static void drivePcTabletsFromPcTablet() throws InterruptedException {
            Map<Integer,Info> infos=new Groups().groups.get("g6");
            infos.put(99,new Info("T99","localhost",Main.defaultReceivePort));
            Set<Tablet> tablets=Group.createGroupAndstartTablets(infos);
            int n=100;
            /*
            tablet.reportPeriod=n;
            tablet.startListening();
            tablet.server.reportPeriod=n;
            tablet.drive(n,tablet.driveWait);
            tablet.stopListening();
            tablet.accumulateToAll();
            tablet.l.severe("start histories()");
            tablet.l.severe(tablet.group.histories(tablet));
            tablet.l.severe("start histories()");
            p("start histories()");
            p(tablet.group.histories(tablet));
            p("start histories()");
            */
        }
        public static void main(String[] arguments) throws IOException,InterruptedException {
            LoggingHandler.init();
            SocketHandler socketHandler=LoggingHandler.startSocketHandler(Main.networkHost,LogServer.defaultService);
            LoggingHandler.addSocketHandler(socketHandler);
            LoggingHandler.setLevel(Level.WARNING);
            //tryConnect();
            //driveTabletsFromClient();
            int n=1;
            for(int i=1;i<=n;i++) {
                p("i: "+i);
                driveRealTabletsFromPcTablet();
                Thread.sleep(3_000);
            }
            printThreads();
            LoggingHandler.stopSocketHandler(socketHandler);
        }
        private final SocketAddress socketAddress;
        private final int timeout;
        private final boolean replying;
        public final Logger l=Logger.getLogger(getClass().getName());
        public static final Logger staticLogger=Logger.getLogger(Client.class.getName());
    }
}
