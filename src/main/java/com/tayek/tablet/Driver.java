package com.tayek.tablet;
import static com.tayek.io.IO.*;
import static com.tayek.utilities.Utility.*;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.*;
import java.util.logging.*;
import com.tayek.*;
import com.tayek.io.*;
import com.tayek.tablet.Main.Stuff;
import static com.tayek.tablet.Main.Stuff.*;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.io.*;
import com.tayek.utilities.Et;
public class Driver {
    public static void driveRealTabletsFromPcTablet() throws InterruptedException {
        Map<String,Required> requireds=new Groups().groups.get("g0");
        String tabletId=Stuff.aTabletId(99);
        requireds.put(tabletId,new Required(tabletId,"localhost",defaultReceivePort));
        Stuff stuff=new Stuff(1,requireds,Model.mark1);
        Tablet tablet=Tablet.create(stuff,tabletId);
        int n=100;
        tablet.histories().reportPeriod=n;
        SocketAddress socketAddress=tablet.stuff.socketAddress(tablet.tabletId());
        tablet.startListening(socketAddress);
        tablet.server.reportPeriod=n;
        tablet.drive(n,Stuff.defaultDriveWait);
        tablet.stopDriving=false;
        tablet.stopListening();
        tablet.accumulateToAll();
        l.severe("drive from pc: "+tablet.histories());
        p("drive from pc: "+tablet.histories());
    }
    public static void drivePcTabletsFromPcTablet() throws InterruptedException {
        Map<String,Required> requireds=new Groups().groups.get("g6");
        requireds.put("T99",new Required("T99","localhost",defaultReceivePort));
        Set<Tablet> tablets=Tablet.createGroupAndstartTablets(requireds);
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
