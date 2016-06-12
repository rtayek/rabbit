package com.tayek.io;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.*;
import java.util.logging.Formatter;
import com.tayek.utilities.Pair;
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
        handler.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        handler.setFormatter(MyFormatter.instance);
        logger.addHandler(handler);
        logger.setLevel(level);
    }
    public static void addFileHandler(Logger logger,File logFileDirectory,String prefix) {
        try {
            String pattern=prefix+".%u.%g.log";
            File logFile=new File(logFileDirectory,pattern);
            p("log file get path: "+logFile.getPath());
            Handler handler=new FileHandler(logFile.getPath(),50_000_000,10,false);
            handler.setLevel(Level.ALL);
            logger.addHandler(handler);
            logger.warning("added file handler: "+handler);
        } catch(Exception e) {
            logger.warning("file handler caught: "+e);
        }
    }
    private static Map<Class<?>,Logger> makeMapAndSetLevels(Set<Class<?>> classes) {
        LoggingHandler.addMyHandlerAndSetLevel(Logger.getGlobal(),Level.ALL);
        Map<Class<?>,Logger> map=new LinkedHashMap<Class<?>,Logger>();
        for(Class<?> clazz:classes) {
            Logger logger=Logger.getLogger(clazz.getName());
            LoggingHandler.addMyHandlerAndSetLevel(logger,Level.OFF);
            map.put(clazz,logger);
        }
        return map;
    }
    public static void init() {
        init(null);
    }
    public static void init(File logFileDirectory) {
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
    public static SocketHandler createSocketHandlerAndWait(String host,int service) {
        final SocketHandlerCallable task=new SocketHandlerCallable(host,service);
        task.run();
        return task.socketHandler;
    }
    static void startSocketHandler(Pair<String,Integer> pair) {
        try {
            SocketHandler socketHandler=createSocketHandlerAndWait(pair.first,pair.second);
            if(socketHandler!=null) {
                p("got socket handler: "+socketHandler+" to: "+pair);
                LoggingHandler.addSocketHandler(socketHandler);
                synchronized(logServerHosts) {
                    logServerHosts.put(pair,socketHandler);
                }
                Logger global=Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
                global.addHandler(socketHandler);
                global.warning("global with socket handler.");
            } else p("could not start socket handler to: "+pair.first+':'+pair.second);
        } catch(Exception e) {
            p("caught: "+e);
        }
    }
    public static void stopSocketHandler(SocketHandler socketHandler) {
        if(socketHandler!=null) {
            l.warning("closing: "+socketHandler);
            try {
                socketHandler.close();
            } catch(Exception e) {
                l.warning("caught: "+e);
            }
        }
    }
    public static boolean areAnySockethandlersOn() {
        boolean areAnyOn=false;
        synchronized(logServerHosts) {
            for(Pair<String,Integer> pair:logServerHosts.keySet())
                if(logServerHosts.get(pair)!=null) {
                    SocketHandler socketHandler=logServerHosts.get(pair);
                    areAnyOn=true;
                    break;
                }
        }
        return areAnyOn;
    }
    // these can't really be static if we are testing multiple tablets - fix!
    public static boolean stopSocketHandlers() {
        boolean wereAnyOn=false;
        synchronized(logServerHosts) {
            for(Pair<String,Integer> pair:logServerHosts.keySet())
                if(logServerHosts.get(pair)!=null) {
                    p("stopping socket handler to: "+pair);
                    stopSocketHandler(logServerHosts.get(pair));
                    logServerHosts.put(pair,null);
                    wereAnyOn=true;
                }
        }
        return wereAnyOn;
    }
    public static String socketHandlers() {
        StringBuffer sb=new StringBuffer();
        synchronized(logServerHosts) {
            for(Entry<Pair<String,Integer>,SocketHandler> entry:logServerHosts.entrySet())
                sb.append(entry.getKey()).append(':').append(entry.getValue()!=null).append(',');
        }
        return sb.toString();
    }
    public static void printSocketHandlers() {
        p("socket handlers:");
        synchronized(logServerHosts) {
            for(Entry<Pair<String,Integer>,SocketHandler> entry:logServerHosts.entrySet())
                p(entry.getKey()+";"+entry.getValue());
        }
    }
    public static void startSocketHandlers() {
        synchronized(logServerHosts) {
            for(final Pair<String,Integer> pair:logServerHosts.keySet())
                if(logServerHosts.get(pair)==null) {
                    new Thread(new Runnable() {
                        @Override public void run() {
                            startSocketHandler(pair); // currently this waits, so ...
                        }
                    },"start: "+pair).start();
                }
        }
    }
    public static void toggleSockethandlers() {
        boolean wereAnyOn=stopSocketHandlers();
        if(!wereAnyOn) startSocketHandlers();
    }
    public static boolean once;
    //public static SocketHandler socketHandler;
    //private static final Level levels[]= {Level.SEVERE,Level.WARNING,Level.INFO,Level.CONFIG,Level.FINE,Level.FINER,Level.FINEST};
    static Map<Class<?>,Logger> map;
    public static final Set<Class<?>> loggers=new LinkedHashSet<>();
    static /* wow! */ {
        loggers.add(IO.class);
    }
}
