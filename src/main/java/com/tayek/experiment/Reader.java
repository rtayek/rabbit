package com.tayek.experiment;
import java.io.*;
import java.net.*;
import static com.tayek.io.IO.*;
import com.tayek.*;
import com.tayek.tablet.Message;
import com.tayek.utilities.*;
class Reader extends Connection { // Supplier<Message>
    Reader(XServer server,String id,String otherId,Socket socket,Histories histories) throws IOException {
        super(id,socket,histories);
        this.otherId=otherId;
        this.server=server;
        in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        l.info("constructed: "+this);
    }
    private java.io.Writer sendReply(Socket socket,String string) { // server
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
        } else l.warning(this+", string is null or empty!");
        return null;
    }
    private String read(Socket socket) { // server
        Et et=new Et();
        l.info(this+" enter read for #"+(histories.receiverHistory.history.attempts()+1));
        String string=null;
        try {
            l.fine(this+" #"+(histories.receiverHistory.history.attempts()+1)+", try to read");
            string=in.readLine();
            if(string==null||string.isEmpty()) {
                l.severe(this+" received null or empty message!");
            } else {
                histories.receiverHistory.history.successes();
                java.io.Writer writer=null;
                if(replying) writer=sendReply(socket,string);
                histories.receiverHistory.history.reportSuccess(et);
                //p("reported success in: "+histories);
            }
        } catch(Exception e) {
            // what do we do with these failures?
            // how to handle?
            if(e.toString().contains("java.net.SocketException: Socket closed")) {
                if(isShuttingDown) l.info(this+" #"+(histories.receiverHistory.history.attempts()+1)+", server caught: "+e+" when shutting down.");
                else {
                    histories.receiverHistory.history.reportFailure(et,e.toString());
                    l.warning(this+" #"+(histories.receiverHistory.history.attempts()+1)+", server caught: "+e);
                }
            } else {
                l.severe(this+" #"+(histories.receiverHistory.history.attempts()+1)+", server caught: "+e);
                histories.receiverHistory.history.reportFailure(et,e.toString());
            }
        }
        if(histories.receiverHistory.history.attempts()>0&&reportPeriod>0&&histories.receiverHistory.history.attempts()%reportPeriod==0) {
            l.warning("histories from: "+this+": "+histories);
            if(histories.receiverHistory.history.attempts()%(10*reportPeriod)==0) ; // print report!
        }
        l.info(this+" exit read for #"+histories.receiverHistory.history.attempts());
        if(string==null||string.isEmpty()) l.info(this+" read eof or empty string!");
        return string;
    }
    // how to get socket address?
    // id, host, and service will be in each message
    // end of problem
    // maybe we don't care anymore (with serverimpl2)
    // maybe just accept anything that connects that's in our table
    // and try to connect with everyone on the list
    // maybe Map<Pair<host,service>,pair<reader,Writer>>
    @Override public void run() {
        l.info(this+" enter run()");
        while(true) {
            String string=read(socket);
            l.info(this+" received: "+string);
            if(string==null) {
                l.info(this+" received eof!");
                break;
            } else if(string.equals("")) {
                l.warning(this+" received empty string!");
                break;
            }
            Message message=server.messageFactory().from(string);
            if(otherId==null) { // new connection?
                String newId=message.from();
                l.info(this+" adding: "+this);
                try {
                    Thread.sleep(1_000);
                    // this sleep makes the one knows test case work
                    // why exactly is that?
                } catch(InterruptedException e1) {
                    e1.printStackTrace();
                }
                p("adding new reader: "+this);
                synchronized(server.newConnections) {
                    
                    server.newConnections.add(this);
                }
                thread.setName(this.toString());
            }
            l.info(this+" received: "+message);
        }
        l.info(this+" exit read loop");
        if(!isShuttingDown) stopThread(); // avoid deadlock
        l.info(this+" exit run()");
    }
    @Override public String toString() {
        return "reader"+histories.sn()+":"+id+"<-"+otherId;
    }
    Integer reportPeriod=Histories.defaultReportPeriod;
    final XServer server;
    final BufferedReader in;
    final boolean replying=false;
    static final String ok="ok";
}
