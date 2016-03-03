package com.tayek.utilities;
import java.util.*;
//should be called stats and should have two implementatons
//one like this that uses buckets and one that keeps an accurate frequency distribution.
//maybe use http://jakarta.apache.org/commons/math/userguide/stat.html instead
public class Histogram {
    public Histogram() {
        this(10,0,1);
    }
    public Histogram(int bins,double low,double high) {
        // low=high will never fill any bins
        // bins=1 should work
        // numBins=0 should work also, but maybe should force it to be one?
        // we need an add(Histogram histogram)
        // but we would need to check for compatibility?
        this.bins=bins;
        bin=new int[bins];
        this.low=low;
        this.high=high;
        range=high-low;
        if(range<=0) throw new RuntimeException(range+"<=0!");
        clear();
    }
    public void add(double[] x) {
        for(int i=0;i<x.length;i++)
            add(x[i]);
    }
    public void add(double x) { // omit and track really strange values!
        synchronized(recent) {
            recent.put(n,x);
        }
        if(Double.valueOf(x).equals(Double.NaN)) {
            missing++; // omitted?
            return;
        }
        n++;
        sum+=x;
        final double x2=x*x;
        sum2+=x2;
        // sum3+=x2*x;
        // sum4+=x2*x2;
        min=Math.min(min,x);
        max=Math.max(max,x);
        if(x>=high) { // this test seem right, but sort sucks since you need an
                      // extra bin for high
                      // fix by giving high a value like 6.0001
            if(false) System.out.println("high "+x+">="+high+" *******");
            overflows++;
        } else if(x<low) underflows++;
        else {
            double value=x-low;
            int index=(int)(bins*(value/range));
            bin[index]++;
        }
    }
    public void add(Histogram histogram) {
        if(histogram.bins!=bins||histogram.low!=low||histogram.high!=high) throw new RuntimeException(histogram+" is incompatablet with: "+this);
        synchronized(recent) {
            recent.putAll(histogram.recent);
        }
        missing+=histogram.missing;
        n+=histogram.n;
        sum+=histogram.sum;
        sum2+=histogram.sum2;
        // sum3+=x2*x;
        // sum4+=x2*x2;
        min=Math.min(min,histogram.min);
        max=Math.max(max,histogram.max);
        overflows+=histogram.overflows;
        underflows+=histogram.underflows;
        for(int i=0;i<bins;i++)
            bin[i]+=histogram.bin[i];
    }
    public int missing() {
        return missing;
    }
    public List<Double> recent() {
        synchronized(recent) {
            ArrayList<Double> x=new ArrayList<>(recent.size());
            x.addAll(recent.values());
            return x;
        }
    }
    public void clear() {
        n=underflows=overflows=missing=0;
        sum=sum2=0;
        min=Double.MAX_VALUE;
        max=Double.MIN_VALUE;
        for(int i=0;i<bins;i++)
            bin[i]=0;
        recent.clear();
        // anything else? 
    }
    public int n() {
        return n;
    }
    public double low() {
        return low;
    }
    public double high() {
        return high;
    }
    public double range() {
        return high()-low();
    }
    public int bins() {
        return bins;
    }
    public double min() {
        return n==0?Double.NaN:min;
    }
    public double max() {
        return n==0?Double.NaN:max;
    }
    public double sum() { // maybe should retun zero?
        return n==0?Double.NaN:sum;
    }
    public double mean() { // maybe should retun zero?
        return n==0?Double.NaN:sum/n;
    }
    public double variance() { // maybe should retun zero?
        return n==0?Double.NaN:sum2/n-mean()*mean();
    }
    public int bin(int index) {
        if(index<0) return underflows;
        else if(index>=bins) return overflows;
        else return bin[index];
    }
    public double maxDifference() { // what exactly is this?
        // looks like max deviation from expected frequency if uniformly distributed.
        double max=0;
        for(int i=0;i<bins();i++)
            max=Math.max(max,Math.abs(bin(i)-n()/(double)bins())/(n()/(double)bins()));
        return max;
    }
    public int overflows() {
        return overflows;
    }
    public int underflows() {
        return underflows;
    }
    public String toString() {
        final StringBuffer sb=new StringBuffer();
        sb.append((float)min()).append("<=").append((float)mean());
        sb.append('/').append(n);
        sb.append("<=").append((float)max()).append(" ");
        sb.append(bin(-1)).append(",[");
        for(int i=0;i<bins;i++)
            sb.append(i>0?",":"").append(bin(i));
        sb.append("],").append(bin(bins));
        sb.append(" missing: ").append(missing);
        return sb.toString();
    }
    public String toStringFrequency() {
        final StringBuffer sb=new StringBuffer();
        sb.append((float)min()).append("<=").append((float)mean()).append("<=").append((float)max()).append(" ");
        sb.append(bin(-1)).append(",[");
        for(int i=0;i<bins;i++)
            sb.append(i>0?",":"").append(bin(i)/(double)n);
        sb.append("],").append(bin(bins));
        sb.append(" missing: ").append(missing);
        return sb.toString();
    }
    private int[] bin;
    private int n,bins,underflows,overflows,missing;
    private final double low,high,range;
    private double min=Double.MAX_VALUE,max=Double.MIN_VALUE,sum,sum2;
    private LruMap<Integer,Double> recent=new LruMap<>(101);
    // probably have to add median back in here, but do it so both flavors are
    // the same size, since we use arrays of these
}
