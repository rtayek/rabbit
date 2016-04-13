package com.tayek.tablet;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.Map;
import com.tayek.Required;
import com.tayek.tablet.Message.Type;
import com.tayek.tablet.Main.Stuff;
import static com.tayek.tablet.Main.Stuff.*;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.io.Sender.Client;
import static com.tayek.io.IO.*;
public class Drive2 {
    public static void main(String[] args) throws IOException {
        Map<String,Required> requireds=new Groups().groups.get("g0");
        String tabletId=Stuff.aTabletId(99);
        requireds.put(tabletId,new Required(tabletId,"localhost",defaultReceivePort));
        Stuff stuff=new Stuff(1,requireds,Model.mark1);
        p("stuff: "+stuff);
        Tablet tablet=Tablet.create(stuff,tabletId);
        stuff=null; // tablet has a clone of group;
        BufferedReader r=new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            String string=r.readLine();
            Integer n=Integer.valueOf(string);
            String destinationId=null;
            if(n!=null) {
                if(n==0) ;
                else if(n>0) {
                    destinationId=Stuff.aTabletId(n);
                    Message message=tablet.stuff.messages.other(Type.drive,tablet.groupId,tablet.tabletId());
                    InetSocketAddress inetSocketAddress=tablet.stuff.socketAddress(destinationId);
                    Client.send(tablet.tabletId(),message,destinationId,inetSocketAddress,stuff,tablet.stuff.required(destinationId).histories());
                } else {
                    destinationId=Stuff.aTabletId(-n);
                    Message message=tablet.stuff.messages.other(Type.forever,tablet.groupId,tablet.tabletId());
                    InetSocketAddress inetSocketAddress=tablet.stuff.socketAddress(destinationId);
                    Client.send(tablet.tabletId(),message,destinationId,inetSocketAddress,stuff,tablet.stuff.required(destinationId).histories());
                }
            } else p("error: "+n);
        }
    }
}
