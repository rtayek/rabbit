package com.tayek.tablet.io;
import java.io.*;
import java.net.*;
import java.util.logging.*;
import com.tayek.tablet.Histories;
import static com.tayek.tablet.io.IO.*;
import com.tayek.utilities.Et;
public interface Sender {
    boolean send(Object message,Histories.ClientHistory history);
    // maybe history belongs here?
    // really, why here? 
    // maybe sender history does belong here?
    // maybe they should all stay together in histories?
    public static class Client implements Sender {
        public Client(SocketAddress socketAddress,boolean replying,int timeout) {
            this.socketAddress=socketAddress;
            this.replying=replying;
            this.timeout=timeout;
        }
        @Override public boolean send(Object object,Histories.ClientHistory history) {
            String string=object.toString();
            synchronized(history) {
                if(string.contains("\n")) l.severe(string+" contains a linefeed!");
                l.fine("#"+(history.client.attempts()+1)+", connecting to: "+socketAddress+", with timeout: "+timeout);
                Et et=new Et();
                Socket socket=connect(socketAddress,timeout,history);
                if(socket!=null) try {
                    l.fine("#"+(history.client.attempts()+1)+", connect took: "+et);
                    Writer out=new OutputStreamWriter(socket.getOutputStream());
                    out.write(string+"\n");
                    out.flush();
                    // add stuff from server for closing/shutting down input and output
                    //out.close();
                    //socket.shutdownOutput(); // closing the writer does this
                    history.client.success();
                    l.fine("#"+history.client.attempts()+", sent: "+object+" at: "+System.currentTimeMillis()+" took: "+et);
                    //Toaster.toaster.toast("sent: "+message+" at: "+System.currentTimeMillis());
                    if(replying) {
                        l.fine("#"+history.client.attempts()+"reading reply to: "+object+" at: "+System.currentTimeMillis());
                        try {
                            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String reply=in.readLine();
                            if(reply!=null&&!reply.isEmpty()) if(!reply.equals(Server.ok)) l.warning("no ack!");
                            else l.fine("#"+history.client.attempts()+", client received: "+reply+", took: "+et);
                            else l.warning("#"+history.client.attempts()+", reply is null or empty!");
                            history.replies.success();
                            l.fine("#"+history.client.attempts()+", got reply to: "+object+" at: "+System.currentTimeMillis());
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
        private final SocketAddress socketAddress;
        private final int timeout;
        private final boolean replying;
        public final Logger l=Logger.getLogger(getClass().getName());
        public static final Logger staticLogger=Logger.getLogger(Client.class.getName());
    }
}
