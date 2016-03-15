package com.tayek.utilities;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
@SuppressWarnings("serial") class MissingException extends RuntimeException {
    MissingException(String string) {
        super(string);
    }
}
public class Missing { // tracks missing messages from consecutive messages.
    public boolean isMissing(int n) {
        if(n<0) throw new MissingException("oops");
        return missing.contains(n);
    }
    public boolean isDuplicate(int n) {
        if(n<0) throw new MissingException("oops");
        if(n<largest&&!isMissing(n)) {
            logger.severe("strange duplicate: "+this);
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
                else logger.warning("duplicate out of oreder - may be missed if not in recent!");
            } else {
                logger.warning("error: smaller is not in missing: "+n);
                if(outOfOrder.contains(n)) {
                    logger.warning("but it is in out of order: "+n);
                    logger.warning("so it is a duplicate that may be missed if not in recent!t"+n);
                } else {
                    logger.severe("error: so we will add it in: "+n);
                    outOfOrder.add(n);
                    // throw new MissingException("error: smaller is not in
                    // missing: "+n);
                }
            }
        } else if(n==largest) logger.fine("duplicat largest: "+n);
        else {
            for(int i=largest+1;i<n;i++)
                if(!missing.add(i)) {
                    logger.severe("error: set already contains: "+i);
                    throw new MissingException("error: set already contains: "+i);
                }
            largest=n;
        }
    }
    @Override public String toString() {
        return largest+"-"+missing+" "+outOfOrder;
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
    public int largest=-1;
    private final Set<Integer> missing=new TreeSet<>();
    public final Set<Integer> outOfOrder=new TreeSet<>();
    Logger logger=Logger.getLogger(getClass().getName());
}
