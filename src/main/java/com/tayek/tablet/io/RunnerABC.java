package com.tayek.tablet.io;
import static com.tayek.io.IO.*;
import java.net.*;
import com.tayek.Tablet;
import com.tayek.Tablet.HasATablet;
import com.tayek.io.*;
import com.tayek.io.Audio.AudioObserver;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.TabletImpl2;
import com.tayek.tablet.Message.Type;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.io.GuiAdapter.GuiAdapterABC;
import com.tayek.utilities.*;
public class RunnerABC implements Runnable {
    public RunnerABC(Group group,String router,String routerPrefix) {
        if(isAndroid()) printSystemProperties();
        else; // printSystemProperties(); // usually not, too long
        this.router=router;
        this.routerPrefix=routerPrefix;
        p("group: "+group);
        for(String id:group.keys())
            p("required: "+group.required(id));
        this.group=group;
        this.model=group.getModelClone(); // blema here!
        this.colors=group.getModelClone().colors; // same for all instances (we hope)
        audioObserver=new AudioObserver(model);
        model.addObserver(audioObserver); // needs to be the model in the tablet (same instance)
        prefs=Prefs.factory.create();
    }
    public void init(Model model) {
        Audio.audio.play(Audio.Sound.glass_ping_go445_1207030150);
    }
    public void buildGui(Model model) {}
    public boolean isRouterOk() {
        return Exec.canWePing(router,1_000);
    }
    public boolean isNetworkInterfaceUp() {
        InetAddress inetAddress=addressWith(routerPrefix);
        return inetAddress!=null;
    }
    // change some of the p's to l's as we want the info!
    protected void createTabletAndStart(String tabletId) {
        restarts++;
        p("creating tablet and starting: "+restarts);
        tablet=Tablet.factory.create2(group,tabletId,model);
        if(hasATablet!=null) hasATablet.setTablet(tablet);
        if(guiAdapterABC!=null) guiAdapterABC.setTablet(tablet);
        if(tablet instanceof TabletImpl2) ((TabletImpl2)tablet).startServer();
    }
    public String status() {
        return "tabletId: "+tabletId+", host: "+host+", status: "+(tablet!=null?"on":"off");
    }
    private void tryToStartTablet() {
        if(tabletId==null&&host==null) {
            p("no tablet id and no host, gettin inetAddress from nic.");
            InetAddress inetAddress=addressWith(routerPrefix);
            if(inetAddress!=null) {
                p("got inetAddress from nic: "+inetAddress);
                host=inetAddress.getHostAddress();
                prefs.put("host",host);
                tabletId=group.getTabletIdFromInetAddress(inetAddress,null);
                if(tabletId!=null) {
                    p("got tabletId from group: "+tabletId);
                    prefs.put("tabletId",tabletId);
                } else p("can not get tabletId from group!");
            } else p("can not get inetAddress despite network being up!");
        }
        if(tablet!=null) if(host==null) { // assume that they will all be the same!
            String host=group.required(tabletId).host;
            if(host!=null) {
                p("found host from group: : "+host);
                prefs.put("host",host);
            } else p("group can not find host for tableiId: "+tabletId);
        }
        if(host!=null) if(tabletId==null) { // maybe use network interface instead?
            tabletId=group.getTabletIdFromHost(host,null);
            if(tabletId!=null) {
                p("got tabletId from group: "+tabletId);
                prefs.put("tabletId",tabletId);
            } else p("can not get tabletId from group!");
        }
        if(tabletId!=null&&host!=null) {
            if(oldTablet!=null) {
                p("using old tablet.");
                tablet=oldTablet;
            }
            p("calling createTabletAndStart. "+Thread.currentThread());
            createTabletAndStart(tabletId);
        }
    }
    protected void stop() {
        if(tablet!=null) {
            p("stopping tablet: "+tabletId);
            if(audioObserver.isChimimg()) audioObserver.stopChimer();
            if(tablet instanceof TabletImpl2) ((TabletImpl2)tablet).stopServer();
            oldTablet=tablet;
            tablet=null;
            if(hasATablet!=null) hasATablet.setTablet(null);
            if(guiAdapterABC!=null) guiAdapterABC.setTablet(null);
        }
    }
    protected void loop(int n) {
        isNetworkInterfaceUp=isNetworkInterfaceUp();
        p("network interface is "+(isNetworkInterfaceUp?"up":"not up!"));
        isRouterOk=isRouterOk();
        p("router is "+(isRouterOk?"ok":"not ok!"));
        // mainActivity.waitForWifi(); // do we need this?
        // does not seem to work anymore :(
        if(tablet==null) if(isNetworkInterfaceUp&&isRouterOk) tryToStartTablet();
        else if(hasATablet!=null) hasATablet.setStatusText("can not start tablet, check wifi and router!");
        else {
            if(isNetworkInterfaceUp&&isRouterOk) {
                if(tablet!=null) {
                    if(heartbeatperiod!=0) if(n%heartbeatperiod==0) if(n>0) {
                        Message message=tablet.messageFactory().other(Type.heartbeat,tablet.group().groupId,tablet.tabletId());
                        tablet.broadcast(message);
                    }
                }
            } else {
                stop();
                if(hasATablet!=null) hasATablet.setStatusText("tablet was stopped, check wifi and router!");
            }
        }
    }
    @Override public void run() {
        thread=Thread.currentThread();
        p("enter run() at: "+et+", tabletId: "+tabletId);
        init(model);
        p("building gui");
        buildGui(model); // clone group if more than one tablet on this guy?
        p("prefs: "+prefs);
        if(prefs.get("tabetId")!=null&&!prefs.get("tabetId").equals("")) tabletId=prefs.get("tabetId");
        if(prefs.get("host")!=null&&!prefs.get("host").equals("")) host=prefs.get("host");
        p("before loop, host: "+host+", tabletId: "+tabletId);
        while(true)
            try {
                p("loop at: "+et+" -------------------");
                if(hasATablet!=null) hasATablet.setStatusText(status());
                loop(n++);
                if(hasATablet!=null) hasATablet.setStatusText(status());
                try {
                    Thread.sleep(loopSleep);
                } catch(Exception e) {
                    p("sleep was interrupted at: "+et);
                }
            } catch(Exception e) {
                l.severe("runner caught: "+e);
            }
    }
    /*
    if(false) {
    if(!Exec.canWePing("127.0.0.1",1_000))
        l.severe("can not ping 127.0.0.1!");
    if(!Exec.canWePing("localhost",1_000))
        l.severe("can not ping localhost!");
    if(!Exec.canWePing(tabletRouter,2_000))
        l.severe("can not ping tabletRouter!");
    if(Exec.canWePing("google.com",5_000))
        l.severe("oops, we seem to be on the internet!");
    }
    if(false)
    new Thread(new Runnable() { // does not seem to find 192.168.0.x
        @Override
        public void run() {
            Nics.main(new String[0]);
        }
    }).start();
    try {
    Thread.sleep(5_000);
    } catch(Exception e) {
    }
    Timer timer=new Timer();
    if(false)
    timer.schedule(new TimerTask() {
        @Override
        public void run() {
            printThreads();
        }
    },60_000,60_000);
    Timer timer2=new Timer();
    */
    public final String router,routerPrefix;
    public final Prefs prefs;
    public final Model model;
    final AudioObserver audioObserver;
    public final Colors colors;
    public final Group group;
    public String tabletId,host;
    public Tablet tablet,oldTablet;
    public final Et et=new Et();
    public Thread thread; // kill this thread when the app quits!
    public HasATablet hasATablet;
    protected GuiAdapterABC guiAdapterABC;
    protected boolean isNetworkInterfaceUp,isRouterOk;
    public int restarts,n;
    public int loopSleep=10_000;
    final int heartbeatperiod=10;
}
