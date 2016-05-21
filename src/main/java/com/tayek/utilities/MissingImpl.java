package com.tayek.utilities;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import static com.tayek.utilities.Range.*;
import static com.tayek.io.IO.*;
abstract class MissingABC<T extends Comparable<T>,R> implements Missing<T,R> {
    MissingABC(T t) {
        range=range(t,t);
        largest=range.sequence(t).previous().value();
    }
    @Override public T largest() {
        return largest;
    }
    @Override public synchronized boolean isMissing(T n) {
        //if(n<0) throw new MissingException("oops");
        return missing().contains(n);
    }
    @Override public synchronized boolean areAnyMissing() {
        return missing().size()>0;
    }
    @Override public synchronized boolean areAnyOutOfOrder() {
        return outOfOrder().size()>0;
    }
    @Override public synchronized boolean isDuplicate(T n) {
        //if(n<0) throw new MissingException("oops");
        if(n.compareTo(largest)<0&&!isMissing(n)) {
            l.severe(n+" is strange duplicate: "+this); // getting this on tablets
            System.err.flush();
        }
        return n.compareTo(largest)<0&&!isMissing(n); // maybe should just return n<largest?
    }
    protected T largest;
    protected Range<T> range; // initial value
}
public class MissingImpl<T extends Comparable<T>,R>extends MissingABC<T,R> { // tracks missing messages from consecutive messages.
    public MissingImpl(T t) { // expect to start with n.
        super(t);
    }
    @SuppressWarnings("unchecked") @Override public synchronized void adjust(T n) {
        //if(n<0) throw new MissingException("oops");
        l.fine("adjust: "+n+" "+this);
        if(n.compareTo(largest)<0) {
            if(missing.contains(n)) {
                missing.remove(n);
                if(!outOfOrder.contains(n)){
                    l.info("adjust: adding: "+n+" to: "+this);
                    outOfOrder.add((R)n);
                }
                else l.warning("duplicate out of oreder - may be missed if not in recent!");
            } else {
                l.warning("error: smaller is not in missing: "+n);
                if(outOfOrder.contains(n)) {
                    l.warning("but it is in out of order: "+n);
                    l.warning("so it is a duplicate that may be missed if not in recent!t"+n);
                } else {
                    l.severe("error: so we will add it in: "+n);
                    outOfOrder.add((R)n);
                    // throw new MissingException("error: smaller is not in
                    // missing: "+n);
                }
            }
        } else if(n.equals(largest)) l.fine("duplicat largest: "+n);
        else {
            T nMinus1=range(n,n).sequence(n).previous().value();
            l.fine("range missing: "+range(range.sequence(largest).next().value(),nMinus1));
            for(T t:range(range.sequence(largest).next().value(),nMinus1)) {
                l.fine("adding missing: "+t+" to: "+this);
                if(!missing.add(t)) {
                    l.severe("error: set already contains: "+t);
                    //throw new MissingException("error: set already contains: "+t);
                }
            }
            largest=n;
        }
        l.fine("after adjust: "+n+" "+this);
    }
    @Override synchronized public String toString() {
        return largest+"-"+missing+", ooo: "+outOfOrder;
    }
    public synchronized Set<T> missing() {
        return missing;
    }
    public synchronized Set<R> outOfOrder() {
        return outOfOrder;
    }
    public static void main(String[] args) throws IOException {
        Missing<Integer,Integer> m=Missing.factory.createNormal(0);
        System.out.println(m);
        for(int i=0;i<10;i++) {
            m.adjust(i);
            System.out.println(m);
        }
    }
    private final Set<T> missing=new TreeSet<>();
    public final Set<R> outOfOrder=new TreeSet<>();
}
