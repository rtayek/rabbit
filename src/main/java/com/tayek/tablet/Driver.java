package com.tayek.tablet;
import static com.tayek.io.IO.*;
import static com.tayek.utilities.Utility.*;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.*;
import java.util.logging.*;
import com.tayek.*;
import com.tayek.io.*;
import com.tayek.tablet.Group.*;
import com.tayek.Tablet.*;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.io.*;
import com.tayek.utilities.Et;
public class Driver {
    public static TabletImpl2 driveRealTablets() throws InterruptedException {
        Map<String,Required> requireds=new Groups().groups.get("g0");
        String tabletId=aTabletId(99);
        requireds.put(tabletId,new Required(tabletId,"localhost",defaultReceivePort));
        Group group=new Group("1",requireds,Model.mark1);
        Tablet tablet=(TabletImpl2)com.tayek.Tablet.factory.create2(group,tabletId);
        int n=3;
        tablet.histories().reportPeriod=n<100?100:n;;
        ((TabletImpl2)tablet).startListening();
        ((TabletImpl2)tablet).server.reportPeriod=n<100?100:n;
        if(false)
            ((TabletImpl2)tablet).drive(n,Config.defaultDriveWait,false);
        else ((TabletImpl2)tablet).forever();
        ((TabletImpl2)tablet).stopDriving=false;
        ((TabletImpl2)tablet).stopListening();
        ((TabletImpl2)tablet).accumulateToAll();
        l.severe("drive from pc: "+tablet.histories());
        p("drive from pc: "+tablet.histories());
        return (TabletImpl2)tablet;
    }
    public static void drivePcTablets() throws InterruptedException {
        Map<String,Required> requireds=new Groups().groups.get("g6");
        requireds.put("T99",new Required("T99","localhost",defaultReceivePort));
        Group group=new Group("1",requireds,Model.mark1);
        Set<Tablet> tablets=Group.createGroupAndstartTablets(group.groupId,requireds);
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
        SocketHandler socketHandler=LoggingHandler.startSocketHandler(defaultHost,LogServer.defaultService);
        LoggingHandler.addSocketHandler(socketHandler);
        LoggingHandler.setLevel(Level.WARNING);
        //tryConnect();
        //driveTabletsFromClient();
        int n=100;
        for(int i=1;i<=n;i++) {
            p("i: "+i);
            TabletImpl2 tablet=driveRealTablets();
            Thread.sleep(2_000);
            p(tablet.report(tablet.tabletId()));
            p("tablet: "+tablet.histories());
        }
        printThreads();
        LoggingHandler.stopSocketHandler(socketHandler);
    }
}
