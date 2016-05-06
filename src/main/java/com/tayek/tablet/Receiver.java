package com.tayek.tablet;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import com.tayek.*;
import com.tayek.tablet.Group.TabletImpl2;
import com.tayek.tablet.Message.*;
import com.tayek.tablet.io.Sender.Client;
import com.tayek.tablet.io.Sender.Client.SendCallable;
import com.tayek.utilities.Et;
import static com.tayek.io.IO.*;
public interface Receiver { // Consumer<Object>
    void receive(Object message);
    public static class DummyReceiver implements Receiver {
        @Override public void receive(Object message) {
            this.message=message;
        }
        public Object message;
    }
    public static class ReceiverImpl implements Receiver {
        ReceiverImpl(Object id,TabletImpl2 tablet,Set<? extends Object> ids,MessageReceiver messageReceiver) {
            // get rid of tablet
            // use a set of Id's
            this.id=id;
            this.tablet=tablet;
            this.messageReceiver=messageReceiver;
            receiverHistory=tablet.histories().receiverHistory;
            if(ids!=null) for(Object x:ids)
                lastMessageNumbers.put(x,null);
            l.info("initial lastMessageNumbers: "+lastMessageNumbers);
            this.messageFactory=tablet.messageFactory(); // maybe only needs a from?
        }
        @Override public void receive(Object message) {
            if(Message.empty.equals(message)) l.severe("empty message received by: "+id);
            else processMessageObject(id,message.toString());
        }
        void checkForMissing(Message message) {
            if(!lastMessageNumbers.containsKey(message.from())) l.severe("message from foreign tablet: "+message);
            Integer lastMessageNumber=lastMessageNumbers.get(message.from());
            boolean ignoreMissingOrOutOfOrder=true;
            // move this to config!
            if(lastMessageNumber!=null) {
                l.info("last: "+lastMessageNumber+", current: "+message.number());
                if(message.number()==lastMessageNumber+1) receiverHistory.missing.success();
                else {
                    l.warning(id+": got #"+receiverHistory.history.attempts()+", expected number: "+(lastMessageNumber+1)+" from: "+message.from()+", but got: "+message.number());
                    if(message.number()<lastMessageNumber+1) {
                        l.warning("#"+receiverHistory.history.attempts()+", out of order!");
                        if(ignoreMissingOrOutOfOrder) receiverHistory.missing.success(); // count first as success
                        else receiverHistory.missing.failure("out of order: "+(message.number()-(lastMessageNumber+1)));
                    } else {
                        l.warning("#"+receiverHistory.history.attempts()+", missing or out of order, expected number: "+(lastMessageNumber+1)+", but got: "+message.number());
                        if(ignoreMissingOrOutOfOrder) receiverHistory.missing.success(); // count first as success
                        else receiverHistory.missing.failure("missing: "+(message.number()-(lastMessageNumber+1)));
                    }
                }
            } else {
                // maybe missed a whole bunch!
                if(message.number()!=1) l.severe("first message is: #"+message.number()+", "+message);
                receiverHistory.missing.success(); // count first as success
            }
            lastMessageNumbers.put(message.from(),message.number());
            l.info(id+" put last #"+message.number()+" into: "+message.from());
            if(receiverHistory.missed.isDuplicate(message.number())) {
                l.severe(id+": missing detected a duplicate!: "+receiverHistory.missed);
            }
            receiverHistory.missed.adjust(message.number());
        }
        // move this to messages?
        private void processMessageObject(Object id,String string) {
            l.info("enter process message: "+string+", lastMessageNumbers: "+lastMessageNumbers);
            if(string!=null&&!string.isEmpty()) {
                Message message=messageFactory.from(string);
                checkForMissing(message);
                l.fine(id+" received: "+message+" at: "+System.currentTimeMillis());
                switch(message.type()) {
                    case ping:
                        l.warning("i got a ping!");
                        Message ack=messageFactory.other(Type.ack,tablet.group().groupId,tablet.tabletId());
                        ackEt=new Et();
                        InetSocketAddress inetSocketAddress=tablet.group().socketAddress(message.from());
                        Future<Void> future=Client.executeTaskAndCancelIfItTakesTooLong(tablet.executorService,
                                new SendCallable(tablet.tabletId(),ack,message.from(),tablet.group().required(message.from()).histories(),inetSocketAddress),tablet.config.sendTimeout,
                                tablet.config.runCanceller?tablet.canceller:null,tablet.config.waitForSendCallable);
                        break;
                    case ack:
                        p(id+", received ack: "+message+", after: "+ackEt);
                        l.warning(id+", received ack: "+message+", after: "+ackEt);
                        ackEt=null;
                        break;
                    case rolloverLogNow:
                        p(id+", received rollover: "+message);
                        l.warning(id+", received rollover: "+message);
                        break;
                    case drive:
                        tablet.driveInThread(true);
                        break;
                    case stopDriving:
                        tablet.stopDriving=true;
                        break;
                    case forever:
                        tablet.foreverInThread();
                        break;
                    default:
                        if(!message.from().equals(id)) tablet.model().receive(message);
                        else l.fine("discarding message from self");
                }
            }
            l.info("exit lastMessageNumbers: "+lastMessageNumbers);
        }
        final TabletImpl2 tablet; // try to remove this
        // maybe just use group?
        final Object id;
        final MessageReceiver messageReceiver;
        final Histories.ReceiverHistory receiverHistory; // change to histories?
        Et ackEt;
        private final Factory messageFactory;
        private final Map<Object,Integer> lastMessageNumbers=new LinkedHashMap<>();
    }
}