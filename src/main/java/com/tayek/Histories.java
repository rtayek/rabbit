package com.tayek;
import java.util.*;
import com.tayek.utilities.*;
import static com.tayek.io.IO.*;
public class Histories implements Addable<Histories> {
    // maybe some parts of this should just be some Map<String,History>'s?
    // we need a short term history for the tablets status display.
    // consider the last n send's?
    // so startup is yellow at first.
    // the only errors we usually see are connection timeouts.
    // so keep track of the last n successes and failures?
    // so maybe have an lru list<Boolean>? 
    public static class History implements Addable<History> {
        @Override public void add(History history) {
            if(history.attempts==0) return;
            attempts+=history.attempts;
            successes+=history.successes;
            failures+=history.failures;
            reasons.putAll(history.reasons);
            if(history.successHistogram!=null) successHistogram.add(history.successHistogram);
            if(history.failureHistogram!=null) failureHistogram.add(history.failureHistogram);
        }
        public synchronized void success() {
            successes++;
            attempts++;
            recent.put(attempts,true);
        }
        public synchronized void failure(String reason) {
            failures++;
            attempts++;
            int n=0;
            if(reasons.get(reason)!=null) n=reasons.get(reason);
            n++;
            reasons.put(reason,n);
            recent.put(attempts,false);
        }
        public Integer failures() {
            return failures;
        }
        public Integer successes() {
            return successes;
        }
        public Integer attempts() {
            return attempts;
        }
        public Double failureRate() {
            return attempts!=0?failures/(double)attempts:0;
        }
        public Double recentFailureRate() {
            double rate=0;
            if(attempts!=0) {
                for(Map.Entry<Integer,Boolean> entry:recent.entrySet())
                    if(!entry.getValue()) rate++;
                rate/=recent.size();
            }
            return rate;
        }
        public void reportSuccess(Et et) {
            double etms=et.etms();
            success();
            successHistogram.add(etms);
            failureHistogram.add(Double.NaN);
        }
        public void reportFailure(Et et,String reason) {
            double etms=et.etms();
            failure(reason);
            successHistogram.add(Double.NaN);
            failureHistogram.add(etms);
        }

        public String toString(String prefix) {
            String string=prefix+": attempts: "+attempts+", successes: "+successes+", recent: "+recent+", failures: "+failures+" "+reasons;
            if(successHistogram.n()!=0) string+="\n"+prefix+"success times: "+successHistogram;
            if(failureHistogram.n()!=0) string+="\n"+prefix+"failure times: "+failureHistogram;
            return string;
        }
        @Override public String toString() {
            return toString("");
        }
        private int attempts,successes,failures;
        private final Map<String,Integer> reasons=new LinkedHashMap<>();
        private LruMap<Integer,Boolean> recent=new LruMap<>(lruMax);
        public final Histogram successHistogram=new Histogram(10,0,1_000);
        public final Histogram failureHistogram=new Histogram(10,0,1_000);
        public static final int lruMax=10;
    }
    public class SenderHistory implements Addable<SenderHistory> {
        @Override public void add(SenderHistory senderHistory) {
            history.add(senderHistory.history);
            history.add(senderHistory.replies);
        }
        @Override public String toString() {
            String string="",prefix="\t";
            if(history.attempts()!=0) string+=history.toString(prefix+"("+serialNumber+") sends: ");
            else string+=prefix+"no attempts";
            if(replies.attempts()!=0) string+="\nreplies: "+replies.toString(prefix+"replies: ");
            if(retries.attempts()!=0) string+="\n\tretries: "+retries.toString();
            if(allSendTimes.n()!=0) string+="\n\tall send times: "+allSendTimes;
            if(allFailures.n()!=0) string+="\n\tall failures: "+allFailures;
            return string;
        }
        public final History history=new History(),replies=new History(),retries=new History();
        public final Histogram allSendTimes=new Histogram(history.successHistogram.bins(),history.successHistogram.low(),history.successHistogram.high());
        public final Histogram allFailures=new Histogram(history.failureHistogram.bins(),history.failureHistogram.low(),history.failureHistogram.high());
    }
    public class ReceiverHistory implements Addable<ReceiverHistory> {
        @Override public void add(ReceiverHistory receiverHistory) {
            history.add(receiverHistory.history);
            replies.add(receiverHistory.replies);
            missing.add(receiverHistory.missing);
        }
        @Override public String toString() {
            String string="",prefix="\t";
            if(history.attempts()!=0) string+=history.toString(prefix+"("+serialNumber+") received: ");
            else string+=prefix+"("+serialNumber+") no attempts";
            if(replies.attempts()!=0) string+="\nreplies: "+replies.toString(prefix+"replies: ");
            if(missing.attempts()!=0) string+="\nmissing: "+missing.toString(prefix+"missing: ");
            if(missing.attempts()!=0) string+="\n\tmissed: "+missed;
            return string;
        }
        public final History history=new History(),replies=new History(),missing=new History();
        public final Missing missed=new Missing(1);
    }
    public class ModelHistory implements Addable<ModelHistory> {
        @Override public void add(ModelHistory modelHistory) {
            history.add(modelHistory.history);
        }
        @Override public String toString() {
            String string="\tmodel: ";
            if(history.attempts()!=0) string+=history.toString();
            else string+="no attempts";
            return string;
        }
        public final History history=new History();
    }
    @Override public void add(Histories histories) {
        senderHistory.add(histories.senderHistory);
        receiverHistory.add(histories.receiverHistory);
    }
    public int failures() {
        return senderHistory.history.failures()+senderHistory.replies.failures()+receiverHistory.history.failures()+receiverHistory.replies.failures()+modelHistory.history.failures();
    }
    public boolean anyAttempts() {
        if(senderHistory.history.attempts()!=0) return true;
        else if(senderHistory.replies.attempts()!=0) return true;
        else if(receiverHistory.history.attempts()!=0) return true;
        else if(receiverHistory.replies.attempts()!=0) return true;
        else if(modelHistory.history.attempts()!=0) return true;
        return false;
    }
    public boolean anyFailures() {
        if(senderHistory.history.failures()!=0) return true;
        else if(senderHistory.replies.failures()!=0) return true;
        else if(receiverHistory.history.failures()!=0) return true;
        else if(receiverHistory.replies.failures()!=0) return true;
        else if(receiverHistory.missing.failures()!=0) return true;
        else if(modelHistory.history.failures()!=0) return true;
        return false;
    }
    public String toString(String prefix) {
        return prefix+'\t'+"sender("+serialNumber+"): "+senderHistory+"\n\treceiver("+serialNumber+"): "+receiverHistory;
    }
    @Override public String toString() {
        return "sender("+serialNumber+"): "+senderHistory+"\nreceiver("+serialNumber+"): "+receiverHistory;
    }
    public final SenderHistory senderHistory=new SenderHistory();
    public final ReceiverHistory receiverHistory=new ReceiverHistory();
    public final ModelHistory modelHistory=new ModelHistory();
    public Integer reportPeriod=defaultReportPeriod;
    public final int serialNumber=++serialNumbers;
    {
        l.info("created "+getClass().getSimpleName()+"("+serialNumber+").");
    }
    static int serialNumbers=0;
    public static Integer defaultReportPeriod=100;
}
