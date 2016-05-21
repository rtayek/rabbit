package com.tayek.utilities;
import java.util.Set;
import com.tayek.utilities.*;
public interface Missing<T extends Comparable<T>,R /*should be T or Range<T>*/ > {
    @SuppressWarnings("serial") class MissingException extends RuntimeException {
        MissingException(String string) {
            super(string);
        }
    }
    boolean isMissing(T n);
    boolean areAnyMissing();
    boolean areAnyOutOfOrder();
    boolean isDuplicate(T n);
    void adjust(T n);
    T largest();
    Set<T> missing(); // should be unmodifiable!
    Set<R> outOfOrder(); // should be unmodifiable!
    static class Factory {
        public Missing<Integer,Integer> createNormal(int n) {
            return new MissingImpl<Integer,Integer>(n);
        }
        public Missing<Integer,Range<Integer>> createRanges(int n) {
            return new MissingRanges<Integer,Range<Integer>>(n);
        }
    }
    Factory factory=new Factory();
}