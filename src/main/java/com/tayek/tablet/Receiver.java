package com.tayek.tablet;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import com.tayek.tablet.Messages.*;
import com.tayek.tablet.io.Sender.Client;
import com.tayek.tablet.io.Sender.Client.SendCallable;
import com.tayek.utilities.Et;
import static com.tayek.io.IO.*;
public interface Receiver {
    void receive(Object message);
    public static class DummyReceiver implements Receiver {
        @Override public void receive(Object message) {
            this.message=message;
        }
        public Object message;
    }
    public static class ReceiverImpl implements Receiver {
        ReceiverImpl(Object iD,Tablet tablet,Set<? extends Object> iDs,MessageReceiver messageReceiver) {
            // get rid of tablet
            // use a set of Id's
            this.iD=iD;
            this.tablet=tablet;
            this.iDs=iDs;
            this.messageReceiver=messageReceiver;
            history=tablet.histories().server;
            if(iDs!=null) for(Object x:iDs)
                lastMessageNumbers.put(x,null);
            this.messages=tablet.stuff.messages;
        }
        @Override public void receive(Object message) {
            if(Message.empty.equals(message)) l.severe("empty message received by: "+iD);
            else processMessageObject(iD,message.toString());
        }
        void checkForMissing(Message message) {
            if(!lastMessageNumbers.containsKey(message.tabletId)) l.severe("message from foreign tablet: "+message);
            Integer lastMessageNumber=lastMessageNumbers.get(message.tabletId);
            if(lastMessageNumber!=null) {
                l.info("last: "+lastMessageNumber+", current: "+message.number);
                if(message.number==lastMessageNumber+1) history.missing.success();
                else {
                    l.warning(iD+": got #"+history.server.attempts()+", expected number: "+(lastMessageNumber+1)+" from: "+message.tabletId+", but got: "+message.number);
                    if(message.number<lastMessageNumber+1) {
                        l.warning("#"+history.server.attempts()+", out of order!");
                        history.missing.failure("out of order: "+(message.number-(lastMessageNumber+1)));
                    } else {
                        l.warning("#"+history.server.attempts()+", missing or out of order, expected number: "+(lastMessageNumber+1)+", but got: "+message.number);
                        history.missing.failure("missing: "+(message.number-(lastMessageNumber+1)));
                    }
                }
            } else {
                // maybe missed a whole bunch!
                history.missing.success(); // count first as success
            }
            lastMessageNumbers.put(message.tabletId,message.number);
            l.info(iD+" put last #"+message.number+" into: "+message.tabletId);
            if(history.missed.isDuplicate(message.number)) {
                l.severe(iD+": missing detected a duplicate!: "+history.missed);
            }
            history.missed.adjust(message.number);
        }
        // move this to messages?
        private void processMessageObject(Object iD,String string) {
            l.info("enter process message, lastMessageNumbers: "+lastMessageNumbers+", message: "+string);
            if(string!=null&&!string.isEmpty()) {
                Message message=messages.from(string);
                checkForMissing(message);
                l.fine(iD+" received: "+message+" at: "+System.currentTimeMillis());
                switch(message.type) {
                    case ping:
                        l.warning("i got a ping!");
                        Message ack=messages.other(Type.ack,tablet.groupId,tablet.tabletId());
                        ackEt=new Et();
                        InetSocketAddress inetSocketAddress=tablet.stuff.socketAddress(message.tabletId);
                        Future<Void> future=Client.executeTaskAndCancelIfItTakesTooLong(tablet.stuff.executorService,
                                new SendCallable(tablet.tabletId(),ack,message.tabletId,tablet.stuff,tablet.stuff.info(message.tabletId).histories(),inetSocketAddress),tablet.stuff.sendTimeout,
                                tablet.stuff.runCanceller?tablet.stuff.canceller:null,tablet.stuff.waitForSendCallable);
                        break;
                    case ack:
                        p(iD+", received ack: "+message+", after: "+ackEt);
                        l.warning(iD+", received ack: "+message+", after: "+ackEt);
                        ackEt=null;
                        break;
                    case rolloverLogNow:
                        p(iD+", received rollover: "+message);
                        l.warning(iD+", received rollover: "+message);
                        break;
                    case drive:
                        new Thread(new Runnable() {
                            @Override public void run() {
                                Driver.drive(tablet,100,Tablet.driveWait);
                                l.severe("start drive histories.");
                                l.severe("drive: "+tablet.histories());
                                l.severe("end drive histories.");
                            }
                        }).start();
                        break;
                    case forever:
                        new Thread(new Runnable() {
                            @Override public void run() {
                                Driver.forever(tablet);
                            }
                        }).start();
                        break;
                    default:
                        if(!message.tabletId.equals(iD)) tablet.model.receive(message);
                        else l.fine("discarding message from self");
                }
            }
            l.info("exit lastMessageNumbers: "+lastMessageNumbers);
        }
        final Tablet tablet; // try to remove this
        // maybe we only need stuff or just id's?
        final Object iD;
        final MessageReceiver messageReceiver;
        final Set<? extends Object> iDs;
        final Histories.ServerHistory history; // change to histories
        // 
        Et ackEt;
        private final Messages messages;
        private final Map<Object,Integer> lastMessageNumbers=new LinkedHashMap<>();
    }
}