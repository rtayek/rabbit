package com.tayek.conrad;
import java.util.*;
import com.tayek.utilities.Histogram;
public class Histories {
    public static class History {
        public synchronized void success() {
            successes++;
            attempts++;
        }
        public synchronized void failure(String reason) {
            failures++;
            attempts++;
            int n=0;
            if(reasons.get(reason)!=null) n=reasons.get(reason);
            n++;
            reasons.put(reason,n);
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
        private int attempts,successes,failures;
        private final Map<String,Integer> reasons=new LinkedHashMap<>();
        public final Histogram successHistogram=new Histogram(20,0,100);
        public final Histogram failureHistogram=new Histogram(20,0,100);
        @Override public String toString() {
            String string="attempts: "+attempts+", successes: "+successes+", failures: "+failures+" "+reasons;
            if(successHistogram.n()!=0) string+="\n\tsuccess times: "+successHistogram;
            if(failureHistogram.n()!=0) string+="\n\tfailure times: "+failureHistogram;
            return string;
        }
    }
    public static class ClientHistory {
        @Override public String toString() {
            String string="client: ";
            if(client.attempts()!=0) string+=client.toString();
            else string+="no attempts";
            if(replies.attempts()!=0) string+="\nreplies: "+replies.toString();
            return string;
        }
        public final History client=new History(),replies=new History();
    }
    public static class ServerHistory {
        @Override public String toString() {
            String string="server: ";
            if(server.attempts()!=0) string+=server.toString();
            else string+="no attempts";
            if(replies.attempts()!=0) string+="\nreplies: "+replies.toString();
            if(missing.attempts()!=0) string+="\nmissing: "+missing.toString();
            return string;
        }
        public final History server=new History(),replies=new History(),missing=new History();
    }
    public static class ModelHistory {
        @Override public String toString() {
            String string="model: ";
            if(model.attempts()!=0) string+=model.toString();
            else string+="no attempts";
            return string;
        }
        public final History model=new History();
    }
    public int failures() {
        return client.client.failures()+client.replies.failures()+server.server.failures()+server.replies.failures();
    }
    public boolean anyAttempts() {
        if(client.client.attempts()!=0) return true;
        else if(client.replies.attempts()!=0) return true;
        else if(server.server.attempts()!=0) return true;
        else if(server.replies.attempts()!=0) return true;
        return false;
    }
    public boolean anyFailures() {
        if(client.client.failures()!=0) return true;
        else if(client.replies.failures()!=0) return true;
        else if(server.server.failures()!=0) return true;
        else if(server.replies.failures()!=0) return true;
        else if(server.missing.failures()!=0) return true;
        return false;
    }
    @Override public String toString() {
        return "\nclient: "+client+"\nserver: "+server;
    }
    public final ClientHistory client=new ClientHistory();
    public final ServerHistory server=new ServerHistory();
    public static Integer defaultConnectTimeout=200;
    public static Integer defaultSendTimeout=250;
    public static Integer defaultReportPeriod=100;
}

