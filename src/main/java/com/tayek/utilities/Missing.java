package com.tayek.utilities;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import static com.tayek.io.IO.*;
@SuppressWarnings("serial") class MissingException extends RuntimeException {
    MissingException(String string) {
        super(string);
    }
}
public class Missing { // tracks missing messages from consecutive messages.
    public Missing() {
        this(0);
    }
    public Missing(int n) {
        largest=n-1;
    }
    public boolean isMissing(int n) {
        if(n<0) throw new MissingException("oops");
        return missing.contains(n);
    }
    public boolean areAnyMissing() {
        return missing.size()>0;
    }
    public boolean areAnyOutOfOrder() {
        return outOfOrder.size()>0;
    }
    public boolean isDuplicate(int n) {
        if(n<0) throw new MissingException("oops");
        if(n<largest&&!isMissing(n)) {
            l.severe("strange duplicate: "+this); // getting this on tablets
            System.err.flush();
        }
        return n<largest&&!isMissing(n);
    }
    public void adjust(int n) {
        if(n<0) throw new MissingException("oops");
        if(n<largest) {
            if(missing.contains(n)) {
                missing.remove(n);
                if(!outOfOrder.contains(n)) outOfOrder.add(n);
                else l.warning("duplicate out of oreder - may be missed if not in recent!");
            } else {
                l.warning("error: smaller is not in missing: "+n);
                if(outOfOrder.contains(n)) {
                    l.warning("but it is in out of order: "+n);
                    l.warning("so it is a duplicate that may be missed if not in recent!t"+n);
                } else {
                    l.severe("error: so we will add it in: "+n);
                    outOfOrder.add(n);
                    // throw new MissingException("error: smaller is not in
                    // missing: "+n);
                }
            }
        } else if(n==largest) l.fine("duplicat largest: "+n);
        else {
            for(int i=largest+1;i<n;i++)
                if(!missing.add(i)) {
                    l.severe("error: set already contains: "+i);
                    throw new MissingException("error: set already contains: "+i);
                }
            largest=n;
        }
    }
    @Override public String toString() {
        return largest+"-"+missing+", ooo: "+outOfOrder;
    }
    public static void main(String[] args) throws IOException {
        Missing m=new Missing();
        System.out.println(m);
        for(int i=0;i<10;i++) {
            m.adjust(i);
            System.out.println(m);
        }
    }
    public Set<Integer> missing() {
        return missing;
    }
    public int largest;
    private final Set<Integer> missing=new TreeSet<>();
    public final Set<Integer> outOfOrder=new TreeSet<>();
}
