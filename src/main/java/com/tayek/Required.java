package com.tayek;

public class Required {
    // let's try and work this into speed now.
    // and see what falls out.
    // moved to top level now. 
    public Required(String id,String host,int service) {
        this.id=id;
        this.host=host;
        this.service=service;
        // add socketAddress?
        histories=new Histories();
        accumulative=new Histories();
        // need an accumulative period?
        // or just use report period?
        // or just always accumulate?
    }
    public Histories histories() {
        return histories;
    }
    @Override public String toString() {
        return id+" "+host+" "+service;
    }
    public final String id;
    public final String host;
    public final Integer service;
    private final Histories histories,accumulative;
}
