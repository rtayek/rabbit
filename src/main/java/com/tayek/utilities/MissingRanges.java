package com.tayek.utilities;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import static com.tayek.utilities.Range.*;
import static com.tayek.io.IO.*;
@SuppressWarnings("serial") public class MissingRanges<T extends Comparable<T>,R>extends MissingABC<T,R> {
    public MissingRanges(T n) {
        super(n);
    }
    @Override public synchronized void adjust(T n) {
        //if(n<0) throw new MissingException("oops");
        Range<T> range=range(n,n);
        if(n.compareTo(largest)<0) {
            if(missing.contains(n)) {
                missing.remove(n);
                if(!outOfOrder.contains(range)) {
                    outOfOrder.add((R)range);
                    reduce(outOfOrder);
                } else l.warning("duplicate out of oreder - may be missed if not in recent!");
            } else {
                l.warning("error: smaller is not in missing: "+n);
                if(outOfOrder.contains(n)) {
                    l.warning("but it is in out of order: "+n);
                    l.warning("so it is a duplicate that may be missed if not in recent!t"+n);
                } else {
                    l.severe("error: so we will add it in: "+n);
                    outOfOrder.add((R)range);
                    reduce(outOfOrder);
                    // throw new MissingException("error: smaller is not in
                    // missing: "+n);
                }
            }
        } else if(n.equals(largest)) l.fine("duplicat largest: "+n);
        else {
            for(T t:range(range.sequence(largest).next().value(),n))
                if(!missing.add(t)) {
                    l.severe("error: set already contains: "+t);
                    throw new MissingException("error: set already contains: "+t);
                }
            largest=n;
        }
    }
    @Override synchronized public String toString() {
        return largest+"-"+missing+", ooo: "+outOfOrder;
    }
    public static void main(String[] args) throws IOException {
        Missing<Integer,Integer> m=Missing.factory.createNormal(0);
        System.out.println(m);
        for(int i=0;i<10;i++) {
            m.adjust(i);
            System.out.println(m);
        }
    }
    public synchronized Set<T> missing() {
        return missing;
    }
    public synchronized Set<R> outOfOrder() {
        return outOfOrder;
    }
    private final Set<T> missing=new TreeSet<>();
    public final Set<R> outOfOrder=new TreeSet<>();
}
