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
    public RunnerABC(Group group,String networkPrefix) {
        this.networkPrefix=networkPrefix;
        p("group: "+group);
        for(String id:group.keys())
            p("required: "+group.required(id));
        this.group=group;
        this.model=group.getModelClone();
        this.colors=model.colors;
        audioObserver=new AudioObserver(model);
        model.addObserver(audioObserver);
        prefs=Prefs.factory.create();
    }
    public void init() {
        Audio.audio.play(Audio.Sound.glass_ping_go445_1207030150);
    }
    public void buildGui() {}
    public boolean isRouterOk() {
        return Exec.canWePing(tabletRouter,1_000);
    }
    public boolean isNetworkInterfaceUp() {
        InetAddress inetAddress=addressWith(networkPrefix);
        return inetAddress!=null;
    }
    protected void createTabletAndStart(String tabletId) {
        n++;
        p("creating tablet and starting: "+n);
        tablet=Tablet.factory.create2(tabletId,group,model);
        if(gui!=null) gui.setTablet(tablet);
        if(guiAdapterABC!=null) guiAdapterABC.setTablet(tablet);
        if(tablet instanceof TabletImpl2) ((TabletImpl2)tablet).startListening();
    }
    public String status() {
        return "tabletId: "+tabletId+", host: "+host+", status: "+(tablet!=null?"on":"off");
    }
    private void tryToStartTablet() {
        p("try to start tablet. host: "+host+", tabletId: "+tabletId);
        if(tabletId==null&&host==null) {
            p("no tablet id and no host, gettin inetAddress from nic.");
            InetAddress inetAddress=addressWith(networkPrefix);
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
        p("try to start tablet. host: "+host+", tabletId: "+tabletId);
        if(tablet!=null) if(host==null) { // assume that they will all be the same!
            String host=group.required(tabletId).host;
            if(host!=null) {
                p("found host from group: : "+host);
                prefs.put("host",host);
            } else p("group can not find host for tableiId: "+tabletId);
        }
        p("try to start tablet. host: "+host+", tabletId: "+tabletId);
        if(host!=null) if(tabletId==null) { // maybe use network interface instead?
            tabletId=group.getTabletIdFromHost(host,null);
            if(tabletId!=null) {
                p("got tabletId from group: "+tabletId);
                prefs.put("tabletId",tabletId);
            } else p("can not get tabletId from group!");
        }
        p("try to start tablet. host: "+host+", tabletId: "+tabletId);
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
            if(tablet instanceof TabletImpl2) ((TabletImpl2)tablet).stopListening();
            oldTablet=tablet;
            tablet=null;
            if(gui!=null) gui.setTablet(null);
            if(guiAdapterABC!=null) guiAdapterABC.setTablet(null);
        }
    }
    protected void loop() {
        isNetworkInterfaceUp=isNetworkInterfaceUp();
        p("network interface is "+(isNetworkInterfaceUp?"up":"not up!"));
        isRouterOk=isRouterOk();
        p("router is "+(isRouterOk?"ok":"not ok!"));
        // mainActivity.waitForWifi(); // do we need this?
        // does not seem to work anymore :(
        if(tablet==null) {
            if(isNetworkInterfaceUp&&isRouterOk) {
                p("calling try to start tablet. host: "+host+", tabletId: "+tabletId);
                tryToStartTablet();
            } else {
                if(gui!=null) gui.setStatusText("can not start tablet, check wifi and router!");
            }
        } else {
            if(isNetworkInterfaceUp&&isRouterOk) {
                Message message=tablet.messageFactory().other(Type.heartbeat,tablet.groupId(),tablet.tabletId());
                tablet.broadcast(message);
            } else {
                stop();
                if(gui!=null) gui.setStatusText("tablet was stopped, check wifi and router!");
            }
        }
    }
    @Override public void run() { // maybe put this in a forever loop?
        thread=Thread.currentThread();
        p("enter run() at: "+et+", tabletId: "+tabletId);
        init();
        p("building gui");
        buildGui();
        // Integer x:=2; y:=3; x+y
        p("prefs: "+prefs);
        if(prefs.get("tabetId")!=null&&!prefs.get("tabetId").equals("")) tabletId=prefs.get("tabetId");
        if(prefs.get("host")!=null&&!prefs.get("host").equals("")) host=prefs.get("host");
        p("before loop, host: "+host+", tabletId: "+tabletId);
        while(true) {
            p("loop at: "+et+" -------------------");
            if(gui!=null) gui.setStatusText(status());
            loop();
            if(gui!=null) gui.setStatusText(status());
            try {
                p("at: "+et+", sleeping: "+tabletId+" "+host+" "+tablet);
                Thread.sleep(10_000);
            } catch(Exception e) {
                p("sleep was interrupted");
            }
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
    public final String networkPrefix;
    public final Prefs prefs;
    public final Model model;
    public final Colors colors;
    public final Group group;
    final AudioObserver audioObserver;
    public String tabletId,host;
    public Tablet tablet,oldTablet;
    public final Et et=new Et();
    public Thread thread; // kill this thread when the app quits!
    protected HasATablet gui;
    protected GuiAdapterABC guiAdapterABC;
    protected boolean isNetworkInterfaceUp,isRouterOk;
    public int n;
}
