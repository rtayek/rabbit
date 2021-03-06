package com.tayek;
import static com.tayek.io.IO.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import com.tayek.*;
import com.tayek.Tablet.*;
import com.tayek.io.IO.ShutdownOptions;
import com.tayek.tablet.io.Server;
import com.tayek.utilities.*;
public interface Sender {
    boolean send(Object message); // Consumer<Object>
    public class Client implements Sender {
        public Client(SocketAddress socketAddress,Config config,Histories histories) {
            this.socketAddress=socketAddress;
            this.config=config;
            this.histories=histories;
        }
        // maybe send should take a socket as a parameter?
        // maybe we can reuse the socket?
        @Override public boolean send(Object object) {
            String string=object.toString();
            boolean wasSuccessful=false;
            synchronized(histories.senderHistory) {
                l.info("locked: "+histories.senderHistory);
                if(string.contains("\n")) l.severe(string+" contains a linefeed!");
                l.fine("#"+(histories.senderHistory.history.attempts()+1)+", connecting to: "+socketAddress+", with timeout: "+config.connectTimeout);
                Et et=new Et();
                Socket socket=connect(socketAddress,config.connectTimeout,config,histories.senderHistory);
                if(socket!=null) try {
                    l.fine("#"+(histories.senderHistory.history.attempts()+1)+", connect took: "+et);
                    Writer out=new OutputStreamWriter(socket.getOutputStream());
                    out.write(string+"\n");
                    out.flush();
                    // add stuff from server for closing/shutting down input and output
                    //out.close();
                    //socket.shutdownOutput(); // closing the writer does this
                    histories.senderHistory.history.reportSuccess(et);
                    l.fine("#"+histories.senderHistory.history.attempts()+", sent: "+object+" at: "+System.currentTimeMillis()+" took: "+et);
                    //Toaster.toaster.toast("sent: "+message+" at: "+System.currentTimeMillis());
                    wasSuccessful=true;
                    l.info("sent: "+object);
                    if(config.replying) {
                        l.fine("#"+histories.senderHistory.history.attempts()+"reading reply to: "+object+" at: "+System.currentTimeMillis());
                        try {
                            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String reply=in.readLine();
                            if(reply!=null&&!reply.isEmpty()) if(!reply.equals(Server.ok)) l.warning("no ack!");
                            else l.fine("#"+histories.senderHistory.history.attempts()+", client received: "+reply+", took: "+et);
                            else l.warning("#"+histories.senderHistory.history.attempts()+", reply is null or empty!");
                            histories.senderHistory.replies.success();
                            l.fine("#"+histories.senderHistory.history.attempts()+", got reply to: "+object+" at: "+System.currentTimeMillis());
                        } catch(IOException e) {
                            l.warning("reading reply caught: "+e);
                            histories.senderHistory.replies.failure(e.toString());
                        }
                    }
                    //l.fine("#"+(history.client.attempts()+1)+", try to shutdown input.");
                    //socket.shutdownInput();
                    l.fine("#"+histories.senderHistory.history.attempts()+", try to close socket.");
                    socket.close();
                    l.fine("#"+histories.senderHistory.history.attempts()+", socket close succeeds.");
                    l.fine("#"+histories.senderHistory.history.attempts()+", 0 send to: "+socketAddress+" completed in: "+et);
                } catch(SocketException e) {
                    histories.senderHistory.history.reportFailure(et,e.toString());
                    l.warning("#"+(histories.senderHistory.history.attempts()+1)+", 1 send to: "+socketAddress+" failed with: '"+e+"'");
                } catch(IOException e) {
                    histories.senderHistory.history.reportFailure(et,e.toString());
                    l.warning("#"+(histories.senderHistory.history.attempts()+1)+", 2 send to: "+socketAddress+" failed with: '"+e+"'");
                    e.printStackTrace();
                } catch(Exception e) {
                    histories.senderHistory.history.reportFailure(et,e.toString());
                    l.warning("#"+(histories.senderHistory.history.attempts()+1)+", 3 send to: "+socketAddress+" failed with: '"+e+"'");
                    e.printStackTrace();
                }
                else l.fine("#"+histories.senderHistory.history.attempts()+"send failed in: "+et);
            }
            return wasSuccessful;
        }
        // refactor this and the on ein io that speed uses!
        public static Socket connect(SocketAddress socketAddress,int timeout,Config config,Histories.SenderHistory senderHistory) {
            Et et=new Et();
            l.fine("connecting to: "+socketAddress+" with timeout: "+timeout);
            Socket socket=new Socket();
            try {
                socket.connect(socketAddress,timeout);
                return socket;
            } catch(SocketTimeoutException e) {
                if(senderHistory!=null) {
                    senderHistory.history.reportFailure(et,e.toString());
                    if(config.logErrors) {
                        l.warning("#"+(senderHistory.history.attempts()+1)+", after: "+et+", with timeout: "+timeout+", caught: '"+e+"'");
                        l.warning("history: "+senderHistory.history);
                    }
                } else l.warning("failed to connect to: "+socketAddress);
            } catch(IOException e) {
                if(senderHistory!=null) {
                    senderHistory.history.reportFailure(et,e.toString());
                    if(config.logErrors) {
                        l.warning("#"+(senderHistory.history.attempts()+1)+", after: "+et+", with timeout: "+timeout+", caught: '"+e+"'");
                        l.warning("history: "+senderHistory.history);
                    }
                } else l.warning("failed to connect to: "+socketAddress);
            } catch(Exception e) {
                if(senderHistory!=null) {
                    senderHistory.history.reportFailure(et,e.toString());
                    if(config.logErrors) {
                        
                        l.warning("#"+(senderHistory.history.attempts()+1)+", after: "+et+", with timeout: "+timeout+", caught: '"+e+"'");
                        l.warning("history: "+senderHistory.history);
                        e.printStackTrace();
                    }
                } else l.warning("failed to connect to: "+socketAddress);
            }
            return null;
        }
        public static void send(String id,final Object message,String destinationId,SocketAddress socketAddress,Histories histories) {
            Config config=new Config(); // maybe pass this in?
            Client client=new Client(socketAddress,config,histories);
            l.info(id+" sending: "+message+" to tablet "+destinationId);
            try {
                boolean ok=client.send(message);
                if(!ok) {
                    l.info("trying to send again to: "+socketAddress);
                    Et et=new Et();
                    ok=client.send(message);
                    if(ok) {
                        histories.senderHistory.retries.success();
                        histories.senderHistory.retries.successHistogram.add(et.etms());
                        histories.senderHistory.replies.failureHistogram.add(Double.NaN);
                        if(config.logErrors) l.warning("worked the second time send to: "+socketAddress);
                    } else {
                        histories.senderHistory.retries.failure("second");
                        histories.senderHistory.retries.successHistogram.add(Double.NaN);
                        histories.senderHistory.replies.failureHistogram.add(et.etms());
                        if(config.logErrors) l.severe("second time failed sending to: "+socketAddress);
                    }
                }
            } catch(Exception e) {
                l.warning("unhandled exception: "+e);
                e.printStackTrace();
            }
        }
        // maybe make inner class?
        public static class SendCallable implements Callable<Void>,Runnable {
            // needs history for destination tablet.
            // and socket address of destination tablet.
            public SendCallable(String id,Object message,String destinationTabletId,Histories histories,SocketAddress socketAddress) {
                this.id=id;
                this.message=message;
                this.destinationId=destinationTabletId;
                this.histories=histories;
                this.socketAddress=socketAddress;
            }
            @Override public void run() {
                Thread.currentThread().setName(getClass().getSimpleName()+", "+id+" send to: "+destinationId);
                l.fine("call send to: "+destinationId);
                sendCalled=true;
                Client.send(id,message,destinationId,socketAddress,histories);
                l.fine("send completed.");
            }
            @Override public Void call() throws Exception {
                run();
                return null;
            }
            public boolean sendCalled;
            private final String id;
            private final Object message;
            private final String destinationId;
            private final Histories histories;
            private final SocketAddress socketAddress;
        }
        private final SocketAddress socketAddress;
        public final Config config;
        private final Histories histories;
        public final ShutdownOptions shutdownOptions=new ShutdownOptions();
        // use these options like the server does!
    }
}
