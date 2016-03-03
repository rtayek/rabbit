package com.tayek.tablet;
import java.util.*;
import com.tayek.utilities.Histogram;
public class H/*istory*/ {
    public void success() {
        successes++;
        attempts++;
    }
    public void failure(String reason) {
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
    public final Histogram successHistogram=new Histogram(10,0,1_000);
    public final Histogram failureHistogram=new Histogram(10,0,1_000);
    @Override public String toString() {
        String string="attempts: "+attempts+", successes: "+successes+", failures: "+failures+" "+reasons;
        if(successHistogram.n()!=0) string+="\n\tsuccess times: "+successHistogram;
        if(failureHistogram.n()!=0) string+="\n\tfailure times: "+failureHistogram;
        return string;
    }
}
