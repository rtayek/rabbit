package com.tayek.tablet.io;
import static com.tayek.io.IO.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import com.tayek.io.IO.ShutdownOptions;
import com.tayek.tablet.*;
import com.tayek.tablet.Main.Stuff;
import com.tayek.utilities.*;
public interface Sender {
    boolean send(Object message,Stuff stuff);
    // maybe history belongs here?
    // really, why here? 
    // maybe sender history does belong here?
    // maybe they should all stay together in histories?
    public static class Client implements Sender {
        public Client(SocketAddress socketAddress,Stuff stuff,Histories histories) {
            this.socketAddress=socketAddress;
            this.stuff=stuff;
            this.histories=histories;
        }
        // maybe send should take a socket as a parameter?
        // maybe we can reuse the socket?
        @Override public boolean send(Object object,Stuff stuff) {
            String string=object.toString();
            synchronized(histories.client) {
                if(string.contains("\n")) l.severe(string+" contains a linefeed!");
                l.fine("#"+(histories.client.client.attempts()+1)+", connecting to: "+socketAddress+", with timeout: "+stuff.connectTimeout);
                Et et=new Et();
                Socket socket=connect(socketAddress,stuff.connectTimeout,histories.client);
                if(socket!=null) try {
                    l.fine("#"+(histories.client.client.attempts()+1)+", connect took: "+et);
                    Writer out=new OutputStreamWriter(socket.getOutputStream());
                    out.write(string+"\n");
                    out.flush();
                    // add stuff from server for closing/shutting down input and output
                    //out.close();
                    //socket.shutdownOutput(); // closing the writer does this
                    histories.client.client.success();
                    l.fine("#"+histories.client.client.attempts()+", sent: "+object+" at: "+System.currentTimeMillis()+" took: "+et);
                    //Toaster.toaster.toast("sent: "+message+" at: "+System.currentTimeMillis());
                    if(stuff.replying) {
                        l.fine("#"+histories.client.client.attempts()+"reading reply to: "+object+" at: "+System.currentTimeMillis());
                        try {
                            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String reply=in.readLine();
                            if(reply!=null&&!reply.isEmpty()) if(!reply.equals(Server.ok)) l.warning("no ack!");
                            else l.fine("#"+histories.client.client.attempts()+", client received: "+reply+", took: "+et);
                            else l.warning("#"+histories.client.client.attempts()+", reply is null or empty!");
                            histories.client.replies.success();
                            l.fine("#"+histories.client.client.attempts()+", got reply to: "+object+" at: "+System.currentTimeMillis());
                        } catch(IOException e) {
                            l.warning("reading reply caught: "+e);
                            histories.client.replies.failure(e.toString());
                        }
                    }
                    //l.fine("#"+(history.client.attempts()+1)+", try to shutdown input.");
                    //socket.shutdownInput();
                    l.fine("#"+histories.client.client.attempts()+", try to close socket.");
                    socket.close();
                    l.fine("#"+histories.client.client.attempts()+", socket close succeeds.");
                    l.fine("#"+histories.client.client.attempts()+", 0 send to: "+socketAddress+" completed in: "+et);
                    double etms=et.etms();
                    histories.client.client.successHistogram.add(etms);
                    histories.client.client.failureHistogram.add(Double.NaN);
                    return true;
                } catch(SocketException e) {
                    l.warning("#"+(histories.client.client.attempts()+1)+", 1 send to: "+socketAddress+" failed with: '"+e+"'");
                    histories.client.client.failure(e.toString());
                    e.printStackTrace();
                } catch(IOException e) {
                    l.warning("#"+(histories.client.client.attempts()+1)+", 2 send to: "+socketAddress+" failed with: '"+e+"'");
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
        public static Socket connect(SocketAddress socketAddress,int timeout,Histories.ClientHistory history) {
            Et et=new Et();
            l.fine("connecting to: "+socketAddress+" with timeout: "+timeout);
            if(true) {
                Socket socket=Utility.connect(socketAddress,timeout);
                if(socket==null) {
                    history.client.failure("connect returned null!");
                    history.client.successHistogram.add(Double.NaN);
                    history.client.failureHistogram.add(et.etms());
                }
                return socket;
            }
            // maybe add history to connect so we know the reason.
            Socket socket=new Socket();
            try {
                socket.connect(socketAddress,timeout);
                return socket;
            } catch(SocketTimeoutException e) {
                history.client.failure(e.toString());
                history.client.successHistogram.add(Double.NaN);
                history.client.failureHistogram.add(et.etms());
                l.warning("#"+(history.client.attempts()+1)+", after: "+et+", with timeout: "+timeout+", caught: '"+e+"'");
            } catch(IOException e) {
                history.client.failure(e.toString());
                history.client.successHistogram.add(Double.NaN);
                history.client.failureHistogram.add(et.etms());
                l.warning("#"+(history.client.attempts()+1)+", after: "+et+", with timeout: "+timeout+", caught: '"+e+"'");
            }
            return null;
        }
        public static void send(String id,final Object message,String destinationId,SocketAddress socketAddress,Stuff stuff,Histories histories) {
            //InetSocketAddress inetSocketAddress=socketAddress(destinationTabletId);
            Client client=new Client(socketAddress,stuff,histories);
            //if(!destinationTabletId.equals(tabletId()))
            l.info(id+" sending: "+message+" to tablet "+destinationId);
            try {
                boolean ok=client.send(message,stuff);
                if(!ok) ; //l.warning("tablet: "+tabletId+", send to: "+inetSocketAddress+" failed!");
                if(false&&!ok) {
                    p("trying to send again to: "+socketAddress);
                    ok=client.send(message,stuff);
                    if(ok) p("worked the second time send to: "+socketAddress);
                    else p("second time failed sending to: "+socketAddress);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        // maybe make inner class?
        public static class SendCallable implements Callable<Void>,Runnable {
            // needs history for destination tablet.
            // and socket address of destination tablet.
            public SendCallable(String id,Object message,String destinationTabletId,Stuff stuff,Histories histories,SocketAddress socketAddress) {
                this.id=id;
                this.message=message;
                this.destinationId=destinationTabletId;
                this.stuff=stuff;
                this.histories=histories;
                this.socketAddress=socketAddress;
            }
            @Override public void run() {
                Thread.currentThread().setName(getClass().getSimpleName()+", "+id+" send to: "+destinationId);
                l.fine("call send to: "+destinationId);
                sendCalled=true;
                //InetSocketAddress inetSocketAddress=Group.socketAddress(info,destinationId);
                Client.send(id,message,destinationId,socketAddress,stuff,histories);
            }
            @Override public Void call() throws Exception {
                run();
                return null;
            }
            public boolean sendCalled;
            private final String id;
            private final Object message;
            private final String destinationId;
            private final Stuff stuff;
            private final Histories histories;
            private final SocketAddress socketAddress;
        }
        public static Future<Void> executeTaskAndCancelIfItTakesTooLong(ExecutorService executorService,final SendCallable callable,final long timeoutMS,ScheduledExecutorService canceller,boolean wait) {
            final Future<Void> future=executorService.submit((Callable<Void>)callable);
            // awk this makes another thread!
            Et et=new Et();
            if(wait) {
                while(!callable.sendCalled) // wait until the are all called
                    Thread.yield();
                l.fine("send called took: "+et);
            }
            if(canceller!=null) canceller.schedule(new Callable<Void>() {
                public Void call() {
                    if(!future.isDone()) {
                        l.warning("future: "+future+", callable: "+callable+" task is not finished after: "+timeoutMS);
                        future.cancel(true);
                    }
                    return null;
                }
            },timeoutMS,TimeUnit.MILLISECONDS);
            return future;
        }

        private final SocketAddress socketAddress;
        private final Stuff stuff;
        private final Histories histories;
        public final ShutdownOptions shutdownOptions=new ShutdownOptions();
        // use these options like the server does!
    }
}
