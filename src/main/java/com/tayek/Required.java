package com.tayek;
public class Required {
    // let's try and work this into speed now.
    // and see what falls out.
    // moved to top level now. 
    public Required(String host,int service) {
        this(defaultId(host,service),host,service);
    }
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
    public String shortId() {
        int index=host.lastIndexOf('.');
        if(index==-1) return id;
        else {
            String lastOctet=host.substring(index+1);
            return lastOctet+':'+service;
        }
    }
    public String defaultId() {
        return defaultId(host,service);
    }
    public static String defaultId(String host,Integer service) {
        return host+':'+service;
    }
    public final String id;
    public final String host;
    public final Integer service;
    private final Histories histories,accumulative;
}
