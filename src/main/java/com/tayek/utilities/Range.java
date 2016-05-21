package com.tayek.utilities;
import java.lang.reflect.Constructor;
import java.text.*;
import java.util.*;
import static com.tayek.io.IO.*;
public class Range<T extends Comparable<T>> implements Comparable<Range<T>>,Iterable<T> {
    // https://gleichmann.wordpress.com/2008/01/21/declarative-programming-a-range-type-for-java/
    public interface Sequence<T> {
        T value();
        Sequence<T> next();
        Sequence<T> previous();
        public class IntegerSequence implements Sequence<Integer> {
            
            public IntegerSequence(Integer value) {
                this.value=value;
            }
            public Integer value() {
                return value;
            }
            public Sequence<Integer> next() {
                return new IntegerSequence(value+1);
            }
            public Sequence<Integer> previous() {
                return new IntegerSequence(value-1);
            }
            Integer value=null;
        }
        public class CharacterSequence implements Sequence<Character> {
            public CharacterSequence(Character value) {
                this.value=value;
            }
            public Character value() {
                return value;
            }
            public Sequence<Character> next() {
                return new CharacterSequence((char)(((int)value)+1));
            }
            public Sequence<Character> previous() {
                return new CharacterSequence((char)(((int)value)-1));
            }
            Character value=null;
        }
    }
    public static class RangeIterator<T extends Comparable<T>> implements Iterator<T> {
        public RangeIterator(Sequence<T> sequence,T end) {
            this.sequence=sequence;
            this.to=end;
        }
        public boolean hasNext() {
            return sequence.value().compareTo(to)<=0;
        }
        public T next() {
            T value=sequence.value();
            sequence=sequence.next();
            return value;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
        private Sequence<T> sequence;
        private final T to;
    }
    public Range(T from,T to) {
        this.from=from;
        this.to=to;
    }
    @Override public int hashCode() {
        final int prime=31;
        int result=1;
        result=prime*result+((from==null)?0:from.hashCode());
        result=prime*result+((to==null)?0:to.hashCode());
        return result;
    }
    @Override public boolean equals(Object obj) {
        if(this==obj) return true;
        if(obj==null) return false;
        if(getClass()!=obj.getClass()) return false;
        Range<?> other=(Range<?>)obj;
        if(from==null) {
            if(other.from!=null) return false;
        } else if(!from.equals(other.from)) return false;
        if(to==null) {
            if(other.to!=null) return false;
        } else if(!to.equals(other.to)) return false;
        return true;
    }
    public boolean contains(T value) {
        return from.compareTo(value)<=0&&to.compareTo(value)>=0;
    }
    @Override public int compareTo(Range<T> o) {
        int rc=from.compareTo(o.from);
        if(rc==0) rc=to.compareTo(o.to);
        return rc;
    }
    @SuppressWarnings("unchecked") public Sequence<T> sequence(T from) {
        String className=Range.class.getName()+"$Sequence$"+from.getClass().getSimpleName()+"Sequence";
        Sequence<T> sequence=null;
        try {
            Class<T> clazz=(Class<T>)Class.forName(className);
            Constructor<T> ctor=clazz.getDeclaredConstructor(from.getClass());
            sequence=(Sequence<T>)ctor.newInstance(from);
        } catch(Exception e) {
            throw new RuntimeException("No Sequence found for type "+from.getClass());
        }
        return sequence;
    }
    // combine these!
    @SuppressWarnings("unchecked") public Iterator<T> iterator() {
        String className=Range.class.getName()+"$Sequence$"+from.getClass().getSimpleName()+"Sequence";
        Sequence<T> sequence=null;
        try {
            Class<T> clazz=(Class<T>)Class.forName(className);
            Constructor<T> ctor=clazz.getDeclaredConstructor(from.getClass());
            sequence=(Sequence<T>)ctor.newInstance(from);
        } catch(Exception e) {
            throw new RuntimeException("No Sequence found for type "+from.getClass());
        }
        return new RangeIterator<T>(sequence,to);
    }
    @Override public String toString() {
        if(from.equals(to)) return "("+from+')';
        return "("+from+'-'+to+')';
    }
    public static <S extends Comparable<S>> Range<S> range(S from,S to) {
        return new Range<S>(from,to);
    }
    public static java.util.Date date(String date) {
        try {
            return DateFormat.getDateInstance().parse(date);
        } catch(ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public static <T extends Comparable<T>,R> void reduce(Set<R> set) { // assume disjoint
        synchronized(set) {
            @SuppressWarnings("unchecked") Range<T>[] ranges=set.toArray(new Range[0]);
            boolean any=true;
            int i=0;
            while(any) {
                any=false;
                for(;i<ranges.length-1;i++) {
                    Iterator<T> j=ranges[i].iterator();
                    T to=((RangeIterator<T>)j).to;
                    Sequence<T> sequence=ranges[i].sequence(ranges[i].to);
                    if(sequence.next().value().equals(ranges[i+1].from)) {
                        any=true;
                        Range<T> range=range(ranges[i].from,ranges[i+1].to);
                        set.remove(ranges[i]);
                        set.remove(ranges[i+1]);
                        set.add((R)range);
                        ranges=set.toArray(new Range[0]);
                        break;
                    }
                }
            }
        }
    }
    public static void main(String[] args) {
        Range<Integer> range=range(1,10);
        Iterator<Integer> i=range.iterator();
        if(i.hasNext()) p("next: "+i.next());
        Set<Range<Integer>> set=new TreeSet<>();
        Range<Integer> r1,r2,r3,r4,r5;
        r1=new Range<Integer>(0,0);
        r2=new Range<Integer>(1,3);
        r3=new Range<Integer>(4,4);
        r4=new Range<Integer>(6,7);
        r5=new Range<Integer>(8,9);
        set.add(r1);
        set.add(r2);
        set.add(r3);
        set.add(r4);
        set.add(r5);
        p("set: "+set);
        reduce(set);
        p("reduced: "+set);
        set.add(new Range<Integer>(5,5));
        reduce(set);
        p("reduced: "+set);
        Set<Range<Character>> cset=new TreeSet<>();
        Range<Character> ra,rb,rc;
        ra=new Range<Character>('a','a');
        rb=new Range<Character>('b','b');
        rc=new Range<Character>('c','z');
        cset.add(ra);
        cset.add(rb);
        cset.add(rc);
        p("cset: "+cset);
        reduce(cset);
        p("reduced: "+cset);
    }
    private T from=null,to=null;
}
