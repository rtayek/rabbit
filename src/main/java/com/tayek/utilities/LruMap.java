package com.tayek.utilities;
import java.util.LinkedHashMap;
import java.util.Map;
public @SuppressWarnings("serial") class LruMap<K,V>extends LinkedHashMap<K,V> {
    public LruMap(int max) {
	super(max+1);
	this.max=max;
    }
    @Override public boolean removeEldestEntry(Map.Entry<K,V> eldest) {
	return size()>max;
    }
    final int max;
}
