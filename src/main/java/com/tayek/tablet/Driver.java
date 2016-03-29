package com.tayek.tablet;
import static com.tayek.io.IO.*;
import static com.tayek.utilities.Utility.*;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.*;
import java.util.logging.*;
import com.tayek.tablet.Main.Stuff;
import static com.tayek.tablet.Main.Stuff.*;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.Messages.*;
import com.tayek.tablet.io.*;
import com.tayek.utilities.Et;
public class Driver {
    public static void drive(Tablet tablet,int n,int wait) {
        // try sending 3 real fast then waiting a while
        // repeat a lot.
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
        int j0=2;
        for(int j=j0;j<=n;j++) {
            if(lastToggle!=Double.NaN) ; //p((et.etms()-lastToggle)+" between toggles.");
            if(sequential) i=(j-j0)%(tablet.model.buttons-1);
            else i=random.nextInt(tablet.model.buttons-1); // omit reset button if any
            tablet.toggle(i+1);
            try {
                Thread.sleep(wait);
            } catch(InterruptedException e) {
                l.warning("drive caught: '"+e+"'");
                e.printStackTrace();
            }
            lastToggle=et.etms();
        }
        try {
            Thread.sleep(5_000);
        } catch(InterruptedException e1) {
            e1.printStackTrace();
        }

    }
    static void forever(final Tablet tablet) {
        for(int k=1;k<100;k++) {
            for(int i=1;i<100;i++)
                drive(tablet,100,Tablet.driveWait);
            tablet.stuff.report(tablet.tabletId());
            Message message=tablet.stuff.messages.other(Type.rolloverLogNow,tablet.groupId,tablet.tabletId());
            tablet.broadcast(message,tablet.stuff);
            try {
                Thread.sleep(1_000);
            } catch(InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }
    public static void driveRealTabletsFromPcTablet() throws InterruptedException {
        Map<String,Info> infos=new Groups().groups.get("g0");
        String tabletId=Stuff.aTabletId(99);
        infos.put(tabletId,new Info(tabletId,"localhost",Main.defaultReceivePort));
        Stuff stuff=new Stuff(1,infos,Model.mark1);
        Tablet tablet=Tablet.create(stuff,tabletId);
        int n=100;
        tablet.stuff.reportPeriod=n;
        SocketAddress socketAddress=tablet.stuff.socketAddress(tablet.tabletId());
        tablet.startListening(socketAddress);
        tablet.server.reportPeriod=n;
        drive(tablet,n,Tablet.driveWait);
        tablet.stopListening();
        tablet.accumulateToAll();
        l.severe("drive from pc: "+tablet.histories());
        p("drive from pc: "+tablet.histories());
    }
    public static void drivePcTabletsFromPcTablet() throws InterruptedException {
        Map<String,Info> infos=new Groups().groups.get("g6");
        infos.put("T99",new Info("T99","localhost",Main.defaultReceivePort));
        Set<Tablet> tablets=Tablet.createGroupAndstartTablets(infos);
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
}
