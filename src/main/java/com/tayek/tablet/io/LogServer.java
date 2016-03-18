package com.tayek.tablet.io;
import java.io.*;
import java.net.*;
import java.util.*;
import com.tayek.tablet.*;
import com.tayek.tablet.Messages.*;
import static com.tayek.tablet.io.IO.*;
import static com.tayek.utilities.Utility.*;
public class LogServer implements Runnable {
    // need some way to end a log file when tablet stops
    // need some way to use a date or prefix.
    private class LogFile {
        private LogFile(Socket socket,String prefix,int sequenceNumber) {
            this.socket=socket;
            this.prefix=prefix;
            this.sequenceNumber=sequenceNumber;
        }
        private int first() { // figure out something reasonable to do here.
            int min=Integer.MAX_VALUE,max=Integer.MIN_VALUE;
            for(int i=1;i<99;i++)
                if(file(i).exists()) {
                    max=Math.max(max,i);
                    min=Math.min(min,i);
                }
            if(min>=50) return 1;
            else if(max<50) return 50;
            return 100;
        }
        private File file(int n) {
            return new File("logs",name(n));
        }
        File file() {
            return new File("logs",name(sequenceNumber));
        }
        private String name(int n) {
            InetAddress inetAddress=socket.getInetAddress();
            String address=inetAddress.getHostAddress();
            String name=prefix!=null&&!prefix.equals("")?(prefix+"."):"";
            name+=address+"."+socket.getLocalPort()+".";
            name+=serverSocket.getInetAddress().getHostAddress()+".";
            name+=n;
            name+=".log";
            return name;
        }
        Socket socket;
        String prefix;
        int sequenceNumber=1;
    }
    public static class Factory { // is this just for testing? (i think so)
        public Factory(Writer writer) {
            this.writer=writer;
        }
        public Copier create(Socket socket) {
            return new Copier(socket,writer,false);
        }
        final Writer writer;
    }
    public static class Copier extends Thread {
        public Copier(Socket socket,Writer out,boolean verbose) {
            super(""+socket.getRemoteSocketAddress());
            this.socket=socket;
            this.out=out;
            this.verbose=verbose;
        }
        public void close() {
            try {
                out.flush();
                out.close();
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            } catch(IOException e) {}
        }
        @Override public void run() {
            try {
                InputStream is=socket.getInputStream();
                BufferedReader br=new BufferedReader(new InputStreamReader(is,"US-ASCII"));
                String line=null;
                while((line=br.readLine())!=null) {
                    if(line.contains(Type.rolloverLogNow.name())) rollover();
                    out.write(line+"\n");
                    out.flush();
                    if(verbose) p("copier wrote: "+line);
                    if(file!=null&&file.exists()&&file.length()>maxSize) if(line.equals("</record>")) {
                        rollover();
                    }
                }
                p("end of file");
            } catch(IOException e) {
                if(isShuttingdown) ;
                else p("log copier caught: '"+e+"'");
            } finally {
                close();
            }
        }
        private void rollover() throws IOException {
            // close file and start new one
            out.close();
            logFile.sequenceNumber++; // starts over at 1!
            File newFile=logFile.file();
            out=new FileWriter(newFile);
            file=newFile;
            p("rollover to: "+newFile+" &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        }
        final Socket socket;
        final boolean verbose;
        Writer out;
        public File file; // may be null for testing
        LogFile logFile; // may be null for testing
        private boolean isShuttingdown;
    }
    public LogServer(String host,int service,String prefix) throws IOException {
        this(host,service,null,prefix);
    }
    public LogServer(String host,int service,Factory factory,String prefix) {
        this.host=host;
        this.service=service;
        this.prefix=prefix!=null?prefix:"";
        InetAddress inetAddress;
        p("trying "+host+":"+service);
        try {
            inetAddress=InetAddress.getByName(host);
            serverSocket=new ServerSocket(service,10/* what should this be?*/,inetAddress);
        } catch(IOException e) {
            p("can not create log server: "+e);
            p("make sure host: "+host+":"+service+" is up.");
            throw new RuntimeException(e);
        }
        this.factory=factory;
    }
    @Override public void run() {
        p("LogServer running on: "+serverSocket);
        while(true)
            if(!serverSocket.isClosed()&&!isShuttingDown) {
                Socket socket=null;
                try {
                    socket=serverSocket.accept();
                    p("accepted connection from: "+socket);
                    Copier copier=null;
                    if(factory!=null) copier=factory.create(socket);
                    else {
                        LogFile logFile=new LogFile(socket,prefix,1);
                        File file=logFile.file();
                        p("log file: "+file);
                        Writer out=new FileWriter(file);
                        copier=new Copier(socket,out,true);
                        copier.file=file;
                        copier.logFile=logFile;
                        synchronized(copiers) {
                            copiers.add(copier);
                        }
                    }
                    copier.start();
                    p("started copier: "+copier);
                } catch(IOException e) {
                    if(!isShuttingDown) {
                        p("log acceptor caught: '"+e+"'");
                        e.printStackTrace();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            } else break;
    }
    public void stop() throws IOException {
        synchronized(copiers) {
            for(Iterator<Copier> i=copiers.iterator();i.hasNext();) {
                Copier copier=i.next();
                if(copier.isAlive()) { // why test for alive?
                    copier.close();
                    i.remove();
                    // how to kill off thread?
                } else p(copier+" is not alive!");
            }
        }
        isShuttingDown=true;
        serverSocket.close();
    }
    public static void print() {}
    public static void main(String args[]) {
        for(String host:Main.logServerHosts.keySet())
            try {
                new Thread(new LogServer(host,defaultService,null)).start();
            } catch(Exception e) {
                p("caught: '"+e+"'");
            }
    }
    public final String host;
    public final int service;
    public final String prefix;
    private boolean isShuttingDown;
    private final ServerSocket serverSocket;
    private final Factory factory;
    public final List<Copier> copiers=new ArrayList<>();
    public static final String defaultHost="127.0.0.1";
    public static final int defaultService=5000;
    public static final int maxSize=1_000_000;
}
