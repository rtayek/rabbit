package com.tayek.utilities;
public class Et {
    public Et() {
	reset();
    }
    public void reset() {
	t0=System.nanoTime();
    }
    public long t0() {
        return t0;
    }
    public long dt() {
        return System.nanoTime()-t0();
    }
    public double etms() {
	return etms(dt());
    }
    @Override public String toString() {
        return etms()+" ms.";
    }
    public static double etms(long dt) {
        return dt/1000000.; // 1_000_000. breaks cobertura
    }
    private Long t0;
}
