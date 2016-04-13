package com.tayek.utilities;
public class CPair<First,Second> {
    public CPair(First first,Second second) {
        this.first=first;
        this.second=second;
    }
    @Override public int hashCode() {
        final int prime=31;
        int result=1;
        result=prime*result+((first==null)?0:first.hashCode());
        result=prime*result+((second==null)?0:second.hashCode());
        return result;
    }
    @Override public boolean equals(Object obj) {
        if(this==obj) return true;
        if(obj==null) return false;
        if(getClass()!=obj.getClass()) return false;
        @SuppressWarnings("unchecked") CPair<First,Second> other=(CPair<First,Second>)obj;
        if(first==null) {
            if(other.first!=null) return false;
        } else if(!first.equals(other.first)) return false;
        if(second==null) {
            if(other.second!=null) return false;
        } else if(!second.equals(other.second)) return false;
        return true;
    }
    @Override public String toString() {
        return "["+first+","+second+"]";
    }
    public final First first;
    public final Second second;
}