package com.tayek.utilities;
import static org.junit.Assert.*;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.tayek.io.LoggingHandler;
import com.tayek.utilities.Missing;
public class OldMissingTestCase {
    @Before public void setUp() throws Exception {
        LoggingHandler.setLevel(Level.OFF);
    }
    @After public void tearDown() throws Exception {}
    @Test public void testAdd0() {
        m.adjust(0);
        assertEquals(0,m.largest);
        assertEquals(0,m.missing().size());
    }
    @Test public void testAdd1() {
        m.adjust(1);
        assertEquals(1,m.largest);
        assertEquals(1,m.missing().size());
        assertTrue(m.missing().contains(0));
    }
    @Test public void testAdd2() {
        m.adjust(2);
        assertEquals(2,m.largest);
        assertEquals(2,m.missing().size());
        assertTrue(m.missing().contains(0));
        assertTrue(m.missing().contains(1));
    }
    @Test public void testAdd0ThenAdd2() {
        m.adjust(0);
        m.adjust(2);
        assertEquals(2,m.largest);
        assertEquals(1,m.missing().size());
        assertTrue(m.missing().contains(1));
    }
    @Test public void testAdd2ThenAdd0() {
        m.adjust(2);
        m.adjust(0);
        assertEquals(2,m.largest);
        assertEquals(1,m.missing().size());
        assertTrue(m.missing().contains(1));
    }
    @Test() public void testdooo() {
        int[] x=new int[] {0,2,1,3,1,4,1};
        for(int i=0;i<x.length;i++) {
            m.adjust(x[i]);
        }
    }
    @Test() public void testduplicateLargest() {
        int[] x=new int[] {0,1,1};
        for(int i=0;i<x.length;i++)
            m.adjust(x[i]);
    }
    private List<Integer> run(Random random,int length,int max) {
        m=new OldMissing();
        List<Integer> integers=new ArrayList<>();
        try {
            for(int i=0;i<length;i++) {
                int x=random.nextInt(max);
                integers.add(x);
                m.adjust(x);
            }
            Set<Integer> set=new TreeSet<>(integers);
            if(true||set.size()>1) {
                // System.out.println("works with: "+integers);
                boolean ok=use(integers);
                // System.out.println("use says: "+ok);
                if(!ok) System.out.println("diagree! **********************");
            }
        } catch(Exception e) {
            // System.out.println("failed with: "+integers);
            boolean ok=use(integers);
            // System.out.println("use says: "+ok);
            if(ok) System.out.println("diagree! **********************");
        }
        return integers;
    }
    boolean use(List<Integer> sequence) {
        m=new OldMissing();
        try {
            for(int i:sequence)
                m.adjust(i);
            return true;
        } catch(Exception e) {
            return false;
        }
    }
    private void random(Random random,int length,int symbols,int reps) {
        Set<List<Integer>> good=new LinkedHashSet<>(),bad=new LinkedHashSet<>();
        for(int i=0;i<reps;i++) {
            List<Integer> sequence=run(random,length,symbols);
            if(use(sequence)) good.add(sequence);
            else bad.add(sequence);
        }
        double n=(int)Math.round(Math.pow(symbols,length));
        //if(good.size()+bad.size()!=n) System.out.println(symbols+"^"+length+": "+n+"!="+(good.size()+bad.size()));
        if(false) {
            System.out.println("good:");
            for(List<Integer> sequence:good)
                System.out.println(sequence);
        }
        if(bad.size()>0) {
            System.out.println("bad:");
            for(List<Integer> sequence:bad)
                System.out.println(sequence);
        }
    }
    @Test() public void test010() {
        m.adjust(0);
        m.adjust(1);
        m.adjust(0);
    }
    @Test() public void testRandom() {
        Random random=new Random();
        int length=3,symbols=5,reps=10000;
        for(int i=0;i<5;i++)
            random(random,length+i,symbols,reps);
    }
    OldMissing m=new OldMissing();
}
