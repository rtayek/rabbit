package com.tayek.tablet.io;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import com.tayek.tablet.*;
import com.tayek.tablet.Main;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.Group.Info.Histories;
import com.tayek.tablet.Message.Type;
import com.tayek.utilities.*;
import static com.tayek.tablet.io.IO.*;
public class Client implements Sender {
    public static class History {
        @Override public String toString() {
            String string="client: ";
            if(client.attempts()!=0) string+=client.toString();
            else string+="no attempts";
            if(replies.attempts()!=0) string+="\nreplies: "+replies.toString();
            if(allSendTimes.n()!=0) string+="\nall send times: "+allSendTimes;
            if(allFailures.n()!=0) string+="\nall failures: "+allFailures;
            return string;
        }
        public final H client=new H(),replies=new H();
        public final Histogram allSendTimes=new Histogram(10,0,1000);
        public final Histogram allFailures=new Histogram(10,0,1000);
    }
    public Client(SocketAddress socketAddress,boolean replying,int timeout) {
        this.socketAddress=socketAddress;
        this.replying=replying;
        this.timeout=timeout;
    }
    public boolean send(Message message,History history) {
        l.fine("#"+(history.client.attempts()+1)+"connecting to: "+socketAddress+", with timeout: "+timeout);
        Et et=new Et();
        Socket socket=connect(socketAddress,timeout,history);
        if(socket!=null) try {
            l.fine("#"+(history.client.attempts()+1)+", connect took: "+et);
            Writer out=new OutputStreamWriter(socket.getOutputStream());
            out.write(message.toString()+"\n");
            out.flush();
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
                    else l.fine("#"+history.client.attempts()+"client received: "+string+", took: "+et);
                    else l.warning("#"+history.client.attempts()+"string is null or empty!");
                    history.replies.success();
                    l.fine("#"+history.client.attempts()+"got reply to: "+message+" at: "+System.currentTimeMillis());
                } catch(IOException e) {
                    l.warning("reading reply caught: "+e);
                    history.replies.failure(e.toString());
                }
            }
            //l.fine("#"+(history.client.attempts()+1)+", try to shutdown input.");
            //socket.shutdownInput();
            l.fine("#"+(history.client.attempts()+1)+", try to close socket.");
            socket.close();
            l.fine("#"+(history.client.attempts()+1)+", socket close succeeds.");
            l.fine("#"+(history.client.attempts()+1)+"send to: "+socketAddress+" completed in: "+et);
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
    public static Socket connect(SocketAddress socketAddress,int timeout,Client.History history) {
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
    //group.socketAddress(destinationTabletId)
    // conrad's idea
    public static void connect(Group group,Integer service) throws IOException {
        int n=0;
        for(Info info:group.info().values()) {
            SocketAddress socketAddress=new InetSocketAddress(info.host,service!=null?service:info.service);
            p("try: "+socketAddress);
            Socket socket=connect(socketAddress,Tablet.defaultConnectTimeout,null);
            if(socket!=null) {
                n++;
                p("connected to: "+socketAddress);
                socket.close();
            }
        }
    }
    // conrad's idea
    public static void connect(String networkPrefix,int service) throws IOException {
        int n=0;
        Map<Integer,Info> info=new Group.Groups().groups.get("g0");
        Set<Integer> integers=new TreeSet<>();
        for(Info i:info.values())
            integers.add((int)InetAddress.getByName(i.host).getAddress()[3]);
        p("integers: "+integers);
        for(int i=1;i<=254;i++)
            try { // takes a long time - timeout
                InetAddress inetAddress=InetAddress.getByName(networkPrefix+i);
                InetSocketAddress inetSocketAddress=new InetSocketAddress(inetAddress,service);
                Socket socket=connect(inetSocketAddress,Tablet.defaultConnectTimeout,null);
                if(integers.contains(i)) p(i+": "+inetSocketAddress);
                if(socket!=null) {
                    n++;
                    p("connected to: "+inetAddress);
                    socket.close();
                }
            } catch(UnknownHostException e) {
                p("caught: '"+e+"'");
            }
        p(n+" connections.");
    }
    public static void driveTabletsFromClient() throws InterruptedException {
        Map<Integer,Info> infos=new Groups().groups.get("g0");
        Group group=new Group(1,infos,Model.mark1,false);
        // make tablet and broadcast or ?
        // lets send messages for now and see if any get lost.
        // works fine, but sequential on and off is not going to find failures
        int i=0;
        Model model=group.getModelClone();
        while(true) {
            model.setState(i+1,true);
            Message message=Message.normal(1,99,i+1,model);
            for(Info info:infos.values()) {
                InetSocketAddress inetSocketAddress=new InetSocketAddress(info.host,info.service);
                Client client=new Client(inetSocketAddress,false,200);
                client.send(message,info.history.client);
            }
            Thread.sleep(500);
            model.setState(i+1,false);
            message=Message.normal(1,99,i+1,model);
            for(Info info:infos.values()) {
                InetSocketAddress inetSocketAddress=new InetSocketAddress(info.host,info.service);
                Client client=new Client(inetSocketAddress,false,200);
                client.send(message,info.history.client);
            }
            Thread.sleep(500);
            i=++i%(model.buttons-1/*avoid the reset button*/);
        }
    }
    // make another driver for pc tablets
    public static void driveRealTabletsFromPcTablet() throws InterruptedException {
        Map<Integer,Info> infos=new Groups().groups.get("g0");
        infos.put(99,new Info("T99","localhost",Main.defaultReceivePort));
        Group group=new Group(1,infos,Model.mark1,false);
        Tablet tablet=group.create(99);
        group=null; // tablet has a clone of group;
        int n=100;
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
        LoggingHandler.startSocketHandler(Main.testingHost,LogServer.defaultService);
        LoggingHandler.addSocketHandler(LoggingHandler.socketHandler);
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
    }
    private final SocketAddress socketAddress;
    private final int timeout;
    private final boolean replying;
    public final Logger l=Logger.getLogger(getClass().getName());
    public static final Logger staticLogger=Logger.getLogger(Client.class.getName());
}
