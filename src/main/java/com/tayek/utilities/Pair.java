package com.tayek.utilities;
public class Pair<First,Second> {
    public Pair(First first,Second second) {
        this.first=first;
        this.second=second;
    }
    @Override public String toString() {
        return "["+first+","+second+"]";
    }
    public First first;
    public Second second;
}