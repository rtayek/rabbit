package com.tayek.tablet;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.Map;
import com.tayek.*;
import com.tayek.Sender.Client;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.Message.Type;
import com.tayek.tablet.MessageReceiver.Model;
import static com.tayek.io.IO.*;
public class Drive2 {
    public static void main(String[] args) throws IOException {
        Map<String,Required> requireds=new Groups().groups.get("g0");
        String tabletId=Groups.add("localhost",defaultReceivePort,requireds);
        Group group=new Group("1",requireds,Model.mark1);
        p("group: "+group);
        TabletImpl2 tablet=(TabletImpl2)Tablet.factory.create2(group,tabletId,group.getModelClone());
        group=null; // tablet has a clone of group;
        BufferedReader r=new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            String string=r.readLine();
            Integer n=Integer.valueOf(string);
            String destinationId=null;
            if(n!=null) {
                if(n==0) ;
                else if(n>0) {
                    destinationId=aTabletId(n);
                    // broken, maybe use n as index into the table if it's in build order?
                    Message message=tablet.messageFactory().other(Type.drive,tablet.group().groupId,tablet.tabletId());
                    InetSocketAddress inetSocketAddress=tablet.group().socketAddress(destinationId);
                    Client.send(tablet.tabletId(),message,destinationId,inetSocketAddress,tablet.group().required(destinationId).histories());
                } else {
                    destinationId=aTabletId(-n);
                    Message message=tablet.messageFactory().other(Type.forever,tablet.group().groupId,tablet.tabletId());
                    InetSocketAddress inetSocketAddress=tablet.group().socketAddress(destinationId);
                    Client.send(tablet.tabletId(),message,destinationId,inetSocketAddress,tablet.group().required(destinationId).histories());
                }
            } else p("error: "+n);
        }
    }
}
