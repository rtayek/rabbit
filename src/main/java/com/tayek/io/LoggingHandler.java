package com.tayek.io;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.util.logging.Formatter;
import static com.tayek.io.IO.*;
public class LoggingHandler {
    public static class MyFormatter extends Formatter {
        private MyFormatter() {}
        @Override public String format(LogRecord record) {
            String threadName=Thread.currentThread().getName();
            String className=record.getSourceClassName();
            int x=className.lastIndexOf(".");
            className=className.substring(x+1);
            long time=System.currentTimeMillis();
            long dt=(time-t0);//%100_000;
            if(threadName.length()>maxThreadNameLength) threadName=threadName.substring(0,maxThreadNameLength-3)+'*'+threadName.substring(threadName.length()-1);
            return String.format(format,dt,record.getSequenceNumber(),record.getLevel(),record.getMessage(),threadName,className+"."+record.getSourceMethodName()+"()");
        }
        private static long t0=System.currentTimeMillis();
        private static final Integer maxThreadNameLength=10;
        private static final String format="%06d %05d %7s %-45s in %"+maxThreadNameLength+"s %s\n";
        public static final MyFormatter instance=new MyFormatter();
    }
    public static void addSocketHandler(SocketHandler socketHandler) {
        if(socketHandler!=null) for(Logger logger:map.values())
            if(!Arrays.asList(logger.getHandlers()).contains(socketHandler)) logger.addHandler(socketHandler);
    }
    public static void removeSocketHandler(SocketHandler socketHandler) {
        if(socketHandler!=null) for(Logger logger:map.values())
            if(Arrays.asList(logger.getHandlers()).contains(socketHandler)) logger.removeHandler(socketHandler);
    }
    public static void addMyHandlerAndSetLevel(Logger logger,Level level) {
        Handler handler=new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        logger.setUseParentHandlers(false);
        handler.setFormatter(MyFormatter.instance);
        logger.addHandler(handler);
        logger.setLevel(level);
    }
    private static Map<Class<?>,Logger> makeMapAndSetLevels(Set<Class<?>> classes) {
        LoggingHandler.addMyHandlerAndSetLevel(Logger.getGlobal(),Level.FINEST);
        Map<Class<?>,Logger> map=new LinkedHashMap<Class<?>,Logger>();
        for(Class<?> clazz:classes) {
            Logger logger=Logger.getLogger(clazz.getName());
            LoggingHandler.addMyHandlerAndSetLevel(logger,Level.OFF);
            map.put(clazz,logger);
        }
        return map;
    }
    public static void init() {
        if(!once) {
            map=makeMapAndSetLevels(loggers);
            once=true;
        }
    }
    public static void setLevel(Level level) {
        if(!once) throw new RuntimeException("oops"); //init();
        for(Logger logger:map.values())
            logger.setLevel(level);
    }
    public static void main(String[] arguments) {
        Logger logger=Logger.getLogger("foo");
        logger.info("info");
        addMyHandlerAndSetLevel(logger,Level.INFO);
        logger.fine("fine");
        logger.info("info");
        logger.warning("warning");
    }
    public static void printlLoggers() {
        Enumeration<String> x=LogManager.getLogManager().getLoggerNames();
        for(;x.hasMoreElements();) {
            String name=x.nextElement();
            Logger logger=LogManager.getLogManager().getLogger(name);
            if(logger==null) p("logger: '"+name+" is null!");
            else {
                if(logger.getHandlers()==null) p("logger: '"+name+"', get handlers() returns null!");
                p("logger: '"+name+"' has: "+logger.getHandlers().length+" loggers.");
            }
        }
    }
    public static SocketHandler startSocketHandlerAndWait(String host,int service) {
        SocketHandler socketHandler=null;
        final SocketHandlerCallable task=new SocketHandlerCallable(host,service);
        try {
            socketHandler=runAndWait(task);
        } catch(InterruptedException|ExecutionException e) {
            l.warning("caught: '"+e+"'");
        }
        return socketHandler;
    }
    static void startSocketHandler(final String host) {
        try {
            p("start socket handler");
            SocketHandler socketHandler=startSocketHandlerAndWait(host,LogServer.defaultService);
            if(socketHandler!=null) {
                p("got socket handler: "+socketHandler);
                LoggingHandler.addSocketHandler(socketHandler);
                synchronized(logServerHosts) {
                    logServerHosts.put(host,socketHandler);
                }
                Logger global=Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
                global.addHandler(socketHandler);
                global.warning("global with socket handler.");
            } else p("could not start socket handler to: "+host);
        } catch(Exception e) {
            p("caught: "+e);
        }
    }
    public static void stopSocketHandler(SocketHandler socketHandler) {
        if(socketHandler!=null) {
            l.warning("closing: "+socketHandler);
            socketHandler.close();
        }
    }
    public static boolean areAnySockethandlersOn() {
        boolean areAnyOn=false;
        synchronized(logServerHosts) {
            for(String host:logServerHosts.keySet())
                if(logServerHosts.get(host)!=null) {
                    areAnyOn=true;
                    break;
                }
        }
        return areAnyOn;
    }
    public static boolean stopSocketHandlers() {
        boolean wereAnyOn=false;
        synchronized(logServerHosts) {
            for(String host:logServerHosts.keySet())
                if(logServerHosts.get(host)!=null) {
                    p("stopping socket handler to: "+host);
                    stopSocketHandler(logServerHosts.get(host));
                    logServerHosts.put(host,null);
                    wereAnyOn=true;
                }
        }
        return wereAnyOn;
    }
    public static void startSocketHandlers() {
        // startSocketHandler is sync'ed on logServerHosts
        // should we sync also?
        for(String host:logServerHosts.keySet()) {
            p("starting socket handler to: "+host);
            startSocketHandler(host);
        }
    }
    public static void toggleSockethandlers() {
        boolean wereAnyOn=stopSocketHandlers();
        if(!wereAnyOn) startSocketHandlers();
    }
    public static boolean once;
    //public static SocketHandler socketHandler;
    //private static final Level levels[]= {Level.SEVERE,Level.WARNING,Level.INFO,Level.CONFIG,Level.FINE,Level.FINER,Level.FINEST};
    private static Map<Class<?>,Logger> map;
    public static final Set<Class<?>> loggers=new LinkedHashSet<>();
    static /* wow! */ {
        loggers.add(IO.class);
    }
}
