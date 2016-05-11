package com.tayek.utilities;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
public class OldMissing implements Missing<Integer,Integer> { // tracks missing messages from consecutive messages.
    @Override public boolean areAnyMissing() {
        return missing().size()>0;
    }
    @Override public boolean areAnyOutOfOrder() {
        return outOfOrder().size()>0;
    }
    @Override public Integer largest() {
        return largest;
    }
    @Override public Set<Integer> outOfOrder() {
        return outOfOrder;
    }
    @Override public boolean isMissing(Integer n) {
        if(n<0) throw new MissingException("oops");
        return missing.contains(n);
    }
    @Override public boolean isDuplicate(Integer n) {
        if(n<0) throw new MissingException("oops");
        if(n<largest&&!isMissing(n)) {
            logger.severe("strange duplicate: "+this);
            System.err.flush();
        }
        return n<largest&&!isMissing(n);
    }
    @Override public void adjust(Integer n) {
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
    @Override public Set<Integer> missing() {
        return missing;
    }
    @Override public String toString() {
        return largest+"-"+missing+" "+outOfOrder;
    }
    public static void main(String[] args) throws IOException {
        OldMissing m=new OldMissing();
        System.out.println(m);
        m.adjust(0);
        System.out.println(m);
    }
    public int largest=-1;
    private final Set<Integer> missing=new TreeSet<>();
    public final Set<Integer> outOfOrder=new TreeSet<>();
    Logger logger=Logger.getLogger(getClass().getName());
}
