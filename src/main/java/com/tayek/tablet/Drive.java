package com.tayek.tablet;
import static com.tayek.tablet.io.IO.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.*;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.Messages.Message;
import com.tayek.tablet.io.*;
import com.tayek.utilities.Et;
public class Drive {
    public static void drive(Tablet tablet,int n,int wait) {
        // try sending 3 real fast then waiting a while
        // repeat a lot.
        if(!conrad) {
            Message message=tablet.group.messages.rolloverLogNow(tablet.group.groupId,tablet.tabletId());
            tablet.broadcast(message,0);
            try {
                Thread.sleep(1_000);
            } catch(InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        Random random=new Random();
        int i=0;
        tablet.click(tablet.model.resetButtonId);
        try {
            Thread.sleep(1_000);
        } catch(InterruptedException e1) {
            e1.printStackTrace();
        }
        boolean sequential=true;
        double lastToggle=Double.NaN;
        Et et=new Et();
        for(int j=3;j<=n;j++) {
            if(lastToggle!=Double.NaN) ; //p((et.etms()-lastToggle)+" between toggles.");
            if(sequential) i=(j-3)%(tablet.model.buttons-1);
            else i=random.nextInt(tablet.model.buttons-1);
            tablet.toggle(i+1);
            try {
                Thread.sleep(wait);
            } catch(InterruptedException e) {
                tablet.l.warning("drive caught: '"+e+"'");
                e.printStackTrace();
            }
            lastToggle=et.etms();
        }
        //l.warning(histories());
    }
    // make another driver for pc tablets
    static void forever(final Tablet tablet) {
        for(int i=1;i<100;i++)
            drive(tablet,100);
    }
    static void drive(final Tablet tablet,int n) {
        drive(tablet,n,tablet.driveWait);
        try {
            Thread.sleep(100);
        } catch(InterruptedException e) {
            tablet.l.severe("caught: "+e);
        }
        tablet.l.severe("start histories()");
        tablet.l.severe(tablet.group.histories(tablet));
        tablet.l.severe("start histories()");
    }
    public static void driveRealTabletsFromPcTablet() throws InterruptedException {
        Map<Object,Info> infos=new Groups().groups.get("g0");
        infos.put(99,new Info("T99","localhost",Main.defaultReceivePort));
        Group group=new Group(1,infos,Model.mark1,false);
        Tablet tablet=group.create(99);
        group=null; // tablet has a clone of group;
        int n=100;
        tablet.group.reportPeriod=n;
        tablet.startListening();
        tablet.server.reportPeriod=n;
        if(!conrad) drive(tablet,n,Tablet.driveWait);
        else {
            tablet.group.reportPeriod=100;
            tablet.server.reportPeriod=100;
            while(true) {
                drive(tablet,5,Tablet.driveWait);
            }
        }
        tablet.stopListening();
        tablet.accumulateToAll();
        tablet.l.severe("start histories()");
        tablet.l.severe(tablet.group.histories(tablet));
        tablet.l.severe("start histories()");
        p("start histories()");
        p(tablet.group.histories(tablet));
        p("start histories()");
    }
    public static void drivePcTabletsFromPcTablet() throws InterruptedException {
        Map<Object,Info> infos=new Groups().groups.get("g6");
        infos.put(99,new Info("T99","localhost",Main.defaultReceivePort));
        Set<Tablet> tablets=Group.createGroupAndstartTablets(infos);
        int n=100;
        /*
        tablet.reportPeriod=n;
        tablet.startListening();
        tablet.server.reportPeriod=n;
        tablet.drive(n,tablet.driveWait);
        tablet.stopListening();
        tablet.accumulateToAll();
        tablet.l.severe("start histories()");
        tablet.l.severe(tablet.group.histories(tablet));
        tablet.l.severe("start histories()");
        p("start histories()");
        p(tablet.group.histories(tablet));
        p("start histories()");
        */
    }
    public static void main(String[] arguments) throws IOException,InterruptedException {
        LoggingHandler.init();
        SocketHandler socketHandler=LoggingHandler.startSocketHandler(Main.networkHost,LogServer.defaultService);
        LoggingHandler.addSocketHandler(socketHandler);
        LoggingHandler.setLevel(Level.WARNING);
        //tryConnect();
        //driveTabletsFromClient();
        int n=1;
        for(int i=1;i<=n;i++) {
            p("i: "+i);
            driveRealTabletsFromPcTablet();
            Thread.sleep(5_000);
        }
        printThreads();
        LoggingHandler.stopSocketHandler(socketHandler);
    }
    static boolean conrad=false;
}
