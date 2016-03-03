package com.tayek.tablet;
import static com.tayek.tablet.io.IO.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import com.tayek.tablet.Group.Info;
import com.tayek.tablet.Group.Info.Histories;
import com.tayek.tablet.Message.Type;
import com.tayek.tablet.io.*;
public class Group implements Cloneable {
    public static class Groups {
        public Groups() {
            boolean old=false;
            if(!old) {
                int tablets=6;
                for(int tabletId=1;tabletId<=6;tabletId++)
                    g0.put(tabletId,new Info("T"+(char)('0'+tabletId),Main.networkPrefix+(10+tabletId),Main.defaultReceivePort));
            } else {
                g0.put(1,new Info("fire 1",Main.networkPrefix+21,Main.defaultReceivePort));
                g0.put(2,new Info("fire 2",Main.networkPrefix+22,Main.defaultReceivePort));
                g0.put(3,new Info("nexus 7",Main.networkPrefix+70,Main.defaultReceivePort));
                g0.put(4,new Info("pc-4",Main.networkPrefix+100,Main.defaultReceivePort+4));
                g0.put(5,new Info("pc-5",Main.networkPrefix+100,Main.defaultReceivePort+5));
                g0.put(6,new Info("azpen",Main.networkPrefix+33,Main.defaultReceivePort));
                g0.put(7,new Info("at&t",Main.networkPrefix+77,Main.defaultReceivePort));
                g0.put(8,new Info("conrad",Main.networkPrefix+88,Main.defaultReceivePort));
                //g0.put(99,new Info("nexus 4",99,IO.defaultReceivePort)));
            }
            groups.put("g0",g0);
            g2.put(4,new Info("pc-4",Main.defaultTestingHost,Main.defaultReceivePort+4));
            g2.put(5,new Info("pc-5",Main.defaultTestingHost,Main.defaultReceivePort+5));
            groups.put("g2",g2);
            //g1each.put(3,new Info("nexus 7",Main.networkPrefix+70,Main.defaultReceivePort));
            // two fake tablets on pc, but on different networks.
            // the 100 is dhcp'ed, so it may change once in a while.
            g1each.put(4,new Info("pc-4",Main.networkPrefix+100,Main.defaultReceivePort+4));
            g1each.put(5,new Info("pc-5",Main.defaultTestingHost,Main.defaultReceivePort+4));
            groups.put("g1each",g1each);
            Map<Integer,Info> g32OnPc=new TreeMap<>();
            int n=6;
            for(int i=1;i<=n;i++)
                g6OnPc.put(i,new Info(""+i,Main.defaultTestingHost,Main.defaultReceivePort+i));
            groups.put("g6OnPc",g6OnPc);
            n=32;
            for(int i=1;i<=n;i++)
                g32OnPc.put(i,new Info(""+i,Main.defaultTestingHost,Main.defaultReceivePort+i));
            groups.put("g32OnPc",g32OnPc);
        }
        private final Map<Integer,Info> g2=new TreeMap<>();
        private final Map<Integer,Info> g0=new TreeMap<>();
        private final Map<Integer,Info> g6OnPc=new TreeMap<>();
        private final Map<Integer,Info> g1each=new TreeMap<>();
        public final Map<String,Map<Integer,Info>> groups=new TreeMap<>();
    }
    public static class Info {
        public static class Histories {
            public Histories() {}
            public int failures() {
                return client.client.failures()+server.server.failures();
                // need to add reply stats if doing replies!
            }
            public boolean anyAttempts() {
                if(client.client.attempts()!=0) return true;
                else if(client.replies.attempts()!=0) return true;
                else if(server.server.attempts()!=0) return true;
                else if(server.replies.attempts()!=0) return true;
                return false;
            }
            public boolean anyFailures() {
                if(client.client.failures()!=0) return true;
                else if(client.replies.failures()!=0) return true;
                else if(server.server.failures()!=0) return true;
                else if(server.replies.failures()!=0) return true;
                return false;
            }
            // could be just: H client,clientReplies,server,serverReplies; ?
            public final Client.History client=new Client.History();
            public final Server.History server=new Server.History();
            public final Model.History model=new Model.History();
            @Override public String toString() {
                return "\nclient: "+client+"\nserver: "+server+"\nmodel: "+model;
            }
        }
        public Info(String name,String host,int port) {
            this.name=name;
            this.host=host;
            this.service=port;
            history=new Histories();
        }
        @Override public String toString() {
            return name+" "+host+" "+service;
        }
        public final String name;
        public final String host;
        public final Integer service;
        public final Histories history;
        // add colors and other stuff;
    }
    public Group(Integer groupId,Map<Integer,Info> info,Model model,boolean replying) {
        this(groupId,info,++serialNumbers,model,replying);
    }
    private Group(Integer groupId,Map<Integer,Info> info,Integer serialNumber,Model model,boolean replying) {
        this.serialNumber=serialNumber;
        this.groupId=groupId;
        this.info=new TreeMap<>();
        this.info.putAll(info);
        this.replying=replying;
        prototype=model;
    }
    @Override public Group clone() { // for testing
        return new Group(groupId,info,serialNumber,getModelClone(),replying);
    }
    public Tablet create(int tabletId) {
        return create(this,tabletId);
    }
    public static Tablet create(Group group,int tabletId) {
        return new Tablet(group.clone(),tabletId);
    }
    public Set<Tablet> create() { // mostly for testing
        Set<Tablet> tablets=new LinkedHashSet<>();
        for(int tabletId:tabletIds())
            tablets.add(create(tabletId));
        return tablets;
    }
    public static Set<Tablet> createGroupAndstartTablets(Map<Integer,Info> info) {
        Set<Tablet> tablets=new LinkedHashSet<>();
        Group group=new Group(1,info,Model.mark1,false);
        for(Iterator<Integer> i=group.tabletIds().iterator();i.hasNext();) {
            int tabletId=i.next();
            group=group.clone();
            Tablet tablet=new Tablet(group,tabletId);
            tablets.add(tablet);
            tablet.model.addObserver(new AudioObserver(tablet.model)); // maybe not if testing?
            tablet.startListening();
        }
        return tablets;
    }
    public Tablet getTablet(InetAddress inetAddress,Integer service) {
        p("host address: "+inetAddress.getHostAddress());
        for(int i:tabletIds()) {
            p("info: "+info(i));
            if(info(i)!=null) if(inetAddress.getHostAddress().equals(info(i).host)&&(service==null||service.equals(info(i).service))) return new Tablet(this,i);
            else p("info("+i+") is not us.");
            else l.severe("info("+i+") is null!");
        }
        return null;
    }
    public InetSocketAddress socketAddress(int destinationTabletId) {
        Info info=info(destinationTabletId);
        if(info!=null) return new InetSocketAddress(info.host,info.service);
        return null;
    }
    public Model getModelClone() {
        return prototype.clone();
    }
    public Set<Integer> tabletIds() {
        synchronized(info) {
            return info.keySet();
        }
    }
    public Info info(int tabletId) {
        synchronized(info) {
            return info.get(tabletId);
        }
    }
    public String name(int tabletId) { // threw npe on nexus 4?
        Info info=info(tabletId);
        return info!=null?info(tabletId).name:"null";
    }
    public void print(Integer tabletId) {
        l.info("group: "+groupId+"("+serialNumber+"):"+tabletId);
        Map<Integer,Info> copy=info();
        for(int i:copy.keySet())
            l.info("\t"+i+": "+copy.get(i));
    }
    public String histories(Tablet tablet/* may be null*/) {
        // need to find the tablet to get the right history?
        // looks like the server history only makes sense for the driving tablet when testing.
        // so maybe check for that when driving and omit the print.
        StringBuffer sb=new StringBuffer();
        sb.append("histories from tablet: "+tablet);
        for(int i:info.keySet()) {
            Histories history=info.get(i).history;
            if(history.anyFailures()) sb.append("\nfor group ("+serialNumber+") tablet: "+i+" history: "+history);
            else sb.append("\nfor group ("+serialNumber+") tablet: "+i+" history: no failures.");
        }
        Map<Integer,Float> failures=new TreeMap<>();
        double sends=0;
        for(int i:info.keySet()) {
            Histories history=info.get(i).history;
            sends+=history.client.client.attempts();
            failures.put(i,(float)(1.*history.client.client.failures()/history.client.client.attempts()));
            sb.append("\nhistory for tablet: "+i+", group ("+serialNumber+") tablet: "+i+" send failure rate: "+history.client.client.failures()+"/"+history.client.client.attempts()+"="
                    +(1.*history.client.client.failures()/history.client.client.attempts()));
        }
        sb.append("\ngroup ("+serialNumber+") send ("+(sends/info.size())+") failure rates: "+failures);
        return sb.toString();
    }
    public Map<Integer,Info> info() {
        Map<Integer,Info> copy=new TreeMap<>();
        synchronized(info) { // may not need to sync anymore?
            copy.putAll(info);
        }
        return Collections.unmodifiableMap(copy);
    }
    public static Message random(Tablet tablet) {
        int buttonId=random.nextInt(tablet.model.buttons)+1;
        return Message.normal(tablet.group.groupId,tablet.tabletId(),buttonId,tablet.model);
    }
    public static Message randomToggle(Tablet tablet) {
        int buttonId=random.nextInt(tablet.model.buttons)+1;
        //boolean state=!tablet.model.state(buttonId);
        //String string=tablet.model.toCharacters();
        //int index=buttonId-1;
        //Character c=Model.toCharacter(state);
        //String newString=string.substring(0,index)+c+string.substring(index+1,string.length());
        return Message.normal(tablet.group.groupId,tablet.tabletId(),buttonId,tablet.model);
        //return new Message(Type.normal,tablet.group.groupId,tablet.tabletId(),buttonId,newString);
    }
    public static final Random random=new Random();
    @Override public String toString() {
        return "group: "+groupId+"("+serialNumber+"): "+info().keySet();
    }
    private Model prototype;
    private final Map<Integer,Info> info; // usually 1-n
    public final Integer serialNumber;
    public final Integer groupId;
    public final boolean replying;
    public Toaster toaster;
    private static int serialNumbers;
    public static final Integer defaultButtons=11;
    public static final Integer defaultTablets=defaultButtons+2;
    public static final Integer maxTablets=50;
    public static final Integer minTabletId=1,maxTabletId=99;
    public static final Integer maxButtons=20;
    public final Logger l=Logger.getLogger(getClass().getName());
}
