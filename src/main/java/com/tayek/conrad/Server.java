package com.tayek.conrad;
import static com.tayek.utilities.Utility.p;
import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;
import com.tayek.utilities.Et;
class Server extends Connection {
    Server(Object id,Socket socket,Histories histories) throws IOException {
        super(id,socket);
        this.histories=histories;
        in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    private Writer sendReply(Socket socket,String string) { // server
        if(string!=null&&!string.isEmpty()) {
            PrintWriter w;
            try {
                w=new PrintWriter(socket.getOutputStream());
                w.write("ok");
                w.flush();
                histories.server.replies.success();
                return w;
            } catch(IOException e) {
                histories.server.replies.failure(e.toString());
            }
        } else l.warning(id+", string is null or empty!");
        return null;
    }
    private String read(Socket socket) { // server
        Et et=new Et();
        l.info(id+", enter read for #"+(histories.server.server.attempts()+1));
        String string=null;
        try {
            l.fine(id+", #"+(histories.server.server.attempts()+1)+", try to read");
            string=in.readLine();
            if(string!=null&&!string.isEmpty()) histories.server.server.successes();
            else l.severe(id+", received null or empty message!");
            Writer writer=null;
            if(replying) writer=sendReply(socket,string);
            histories.server.server.success();
            histories.server.server.successHistogram.add(et.etms());
            histories.server.server.failureHistogram.add(Double.NaN);
        } catch(IOException e) {
            // what do we do with these failures?
            // how to handle?
            histories.server.server.failure(e.toString());
            histories.server.server.successHistogram.add(Double.NaN);
            histories.server.server.failureHistogram.add(et.etms());
            l.severe(id+", #"+(histories.server.server.attempts()+1)+", 4 server caught: "+e);
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
        if(histories.server.server.attempts()>0&&reportPeriod>0&&histories.server.server.attempts()%reportPeriod==0) l.warning("history from : "+id+": "+histories);
        l.info(id+", exit read for #"+histories.server.server.attempts());
        if(string==null||string.isEmpty()) l.severe(id+", return eof or empty string!");
        return string;
    }
    @Override public void run() {
        l.info(id+", enter run()");
        while(true) {
            String string=read(socket);
            l.fine(id+", received: "+string);
            if(string==null){
                l.warning(id+", received eof!");
                break;
            }
        }
        l.info(id+", exit read loop");
        try {
            stopThread();
        } catch(IOException e) {
            e.printStackTrace();
        }
        l.info(id+", exit run()");
    }
    Integer reportPeriod=Histories.defaultReportPeriod;
    final Histories histories;
    final BufferedReader in;
    final boolean replying=false;
    static final String ok="ok";
    public static final Logger l=Logger.getLogger(Server.class.getName());
}
