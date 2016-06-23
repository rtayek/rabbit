package com.tayek.speed;
import java.io.*;
import java.net.*;
import static com.tayek.io.IO.*;
import com.tayek.*;
import com.tayek.tablet.*;
import com.tayek.utilities.*;
// maybe this can implement an interface
// receiver?
class Reader extends Connection { // Supplier<Message>
    private Reader(Server server,String id,String otherId,Socket socket,Histories histories) throws IOException {
        super(id,socket,histories);
        this.otherId=otherId;
        this.server=server;
        in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        l.info(id+": constructed: "+this);
    }
    static Reader create(Server server,String id,String otherId,Socket socket,Histories histories) {
        Reader reader=null;
        try {
            reader=new Reader(server,id,otherId,socket,histories);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return reader;
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
        l.info(id+": "+this+" enter read for #"+(histories.receiverHistory.history.attempts()+1));
        String string=null;
        try {
            l.fine(id+": "+this+" #"+(histories.receiverHistory.history.attempts()+1)+", try to read");
            string=in.readLine();
            if(string==null||string.isEmpty()) {
                l.severe(id+": "+this+" received null or empty message!");
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
                if(isShuttingDown) l.info(id+": "+this+" #"+(histories.receiverHistory.history.attempts()+1)+", server caught: "+e+" when shutting down.");
                else {
                    histories.receiverHistory.history.reportFailure(et,e.toString());
                    l.warning(id+": "+this+" #"+(histories.receiverHistory.history.attempts()+1)+", server caught: "+e);
                }
            } else {
                l.severe(id+": "+this+" #"+(histories.receiverHistory.history.attempts()+1)+", server caught: "+e);
                histories.receiverHistory.history.reportFailure(et,e.toString());
            }
        }
        if(histories.receiverHistory.history.attempts()>0&&reportPeriod>0&&histories.receiverHistory.history.attempts()%reportPeriod==0) {
            l.warning(id+": "+"histories from: "+this+": "+histories.toString("receiver"));
            if(histories.receiverHistory.history.attempts()%(10*reportPeriod)==0) ; // print report!
        }
        l.info(id+": "+this+" exit read for #"+histories.receiverHistory.history.attempts());
        if(string==null||string.isEmpty()) l.info(id+": "+this+" read eof or empty string!");
        return string;
    }
    void newConnection(Message message) {
        String newId=message.from();
        this.otherId=newId;
        l.info(id+": "+this+" received first message:  "+message);
        if(false) try {
            Thread.sleep(1_000);
            // this sleep makes the one knows test case work
            // why exactly is that? - probably a sync problem
            // looks like it may have been
        } catch(InterruptedException e1) {
            e1.printStackTrace();
        }
        server.addConnection(newId,this); // yuck! - nope, necessary
        // maybe not
        thread.setName(this.toString());
        Pair<Writer,Reader> pair=server.idToPair().get(newId);
        if(false) // let's wait and see what happens
            // bad idea, nothing happens :)
            // true, but that's because we don't need to add a writer
            // now that we know all of them up front.
            // but we do if we don't create them up front.
            if(pair.first==null) try {
                // check that new id is equal to host:service?
            Required required=new Required(newId,message.host(),message.service());
            Writer writer=Writer.create(id,newId,required);
            if(writer!=null) {
            l.info(id+": "+this+" adding sender: "+writer);
            server.addConnection(newId,writer);
            } else l.warning(id+": "+"can not create writer!");
            } catch(IOException e) {
            e.printStackTrace();
            }
    }
    // how to get socket address?
    // id, host, and service will be in each message
    // end of problem
    // maybe we don't care anymore (with serverimpl2)
    // maybe just accept anything that connects that's in our table
    // and try to connect with everyone on the list
    // maybe Map<Pair<host,service>,pair<reader,Writer>>
    @Override public void run() {
        l.info(id+": "+this+" enter run()");
        while(true) {
            String original=read(socket);
            synchronized(this) {
                String string=original; // wtf!
                l.info(id+": "+this+" received: "+string);
                if(string==null) {
                    l.info(id+": "+this+" received eof!");
                    break;
                } else if(string.equals("")) {
                    l.warning(id+": "+this+" received empty string!");
                    break;
                }
                Message message=server.messageFactory().from(string);
                if(otherId==null) { // new connection?
                    newConnection(message);
                }
                l.info(id+": "+this+" received: "+message);
            }
        }
        l.info(id+": "+this+" exit read loop");
        if(!isShuttingDown) stopThread(); // avoid deadlock
        l.info(id+": "+this+" exit run()");
    }
    @Override public String toString() {
        return "reader"+sn()+histories.sn()+" "+id+":"+id+"<-"+otherId;
    }
    Integer reportPeriod=Histories.defaultReportPeriod;
    final Server server;
    final BufferedReader in;
    final boolean replying=false;
    static final String ok="ok";
}
