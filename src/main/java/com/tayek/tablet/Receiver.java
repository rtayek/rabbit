package com.tayek.tablet;
import static com.tayek.utilities.Utility.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import com.tayek.tablet.Messages.Message;
import com.tayek.utilities.Et;
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
            history=tablet.group.info(tablet.tabletId()).history.server;
            if(iDs!=null) for(Object x:iDs)
                lastMessageNumbers.put(x,null);
            this.messages=tablet.group.messages;
        }
        @Override public void receive(Object message) {
            processMessageObject("foo",message.toString());
        }
        // move this to messages?
        private void processMessageObject(String name,String string) {
            l.info("enter process message, lastMessageNumbers: "+lastMessageNumbers+", message: "+string);
            if(string!=null&&!string.isEmpty()) {
                // maybe keep the message as just a string and pass it to the receiver
                Message message=messages.from(string);
                if(!lastMessageNumbers.containsKey(message.tabletId)) {
                    l.severe("message from foreign tablet: "+message);
                    p("lastMessageNumbers: "+lastMessageNumbers);
                }
                Integer lastMessageNumber=lastMessageNumbers.get(message.tabletId);
                if(lastMessageNumber!=null) {
                    l.info("last: "+lastMessageNumber+", current: "+message.number);
                    if(message.number==lastMessageNumber+1) history.missing.success();
                    else {
                        l.warning(iD+": got #"+history.server.attempts()+", expected number: "+(lastMessageNumber+1)+" from: "+message.tabletId+", but got: "+message.number);
                        p("lastMessageNumbers: "+lastMessageNumbers);
                        if(message.number<lastMessageNumber+1) {
                            l.warning("#"+history.server.attempts()+", out of order!");
                            history.missing.failure("out of order: "+(message.number-(lastMessageNumber+1)));
                        } else {
                            l.warning("#"+history.server.attempts()+", missing at least one message, expected number: "+(lastMessageNumber+1)+", but got: "+message.number);
                            history.missing.failure("missing: "+(message.number-(lastMessageNumber+1)));
                        }
                    }
                } else history.missing.success(); // count first as success
                l.fine(iD+" received: "+message+" at: "+System.currentTimeMillis());
                l.info(iD+" put last #"+message.number+" into: "+message.tabletId);
                lastMessageNumbers.put(message.tabletId,message.number);
                switch(message.type) {
                    case ping:
                        l.warning("i got a ping!");
                        Message ack=messages.ack(tablet.group.groupId,tablet.tabletId());
                        ackEt=new Et();
                        Future<Void> future=tablet.group.executeTaskAndCancelIfItTakesTooLong(tablet.group.new SendCallable(tablet,ack,message.tabletId,tablet.group.connectTimeout),
                                tablet.group.connectTimeout+100,tablet.group.canceller);
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
                    default:
                        if(!message.tabletId.equals(iD)) 
                            tablet.model.receive(message);
                         else l.fine("discarding message from self");
                }
            }
            l.info("exit lastMessageNumbers: "+lastMessageNumbers);
        }
        final Tablet tablet;
        final Object iD;
        final MessageReceiver messageReceiver;
        final Set<? extends Object> iDs;
        final Histories.ServerHistory history;
        Et ackEt;
        public final Logger l=Logger.getLogger(Tablet.class.getName());
        private final Messages messages;
        private final Map<Object,Integer> lastMessageNumbers=new LinkedHashMap<>();
    }
}