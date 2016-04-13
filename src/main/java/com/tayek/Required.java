package com.tayek;

public class Required {
    public Required(String iD,String host,int port) {
        this.iD=iD;
        this.host=host;
        this.service=port;
        // add socketAddress?
        histories=new Histories();
        accumulative=new Histories();
        // need an accumulative period?
        // or just use report period?
    }
    public Histories histories() {
        return histories;
    }
    @Override public String toString() {
        return iD+" "+host+" "+service;
    }
    public final String iD;
    public final String host;
    public final Integer service;
    private final Histories histories,accumulative;
}
