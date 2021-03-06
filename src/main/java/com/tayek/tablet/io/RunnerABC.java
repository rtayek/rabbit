package com.tayek.tablet.io;
import static com.tayek.io.IO.*;
import java.net.*;
import java.util.logging.Level;
import com.tayek.Tablet;
import com.tayek.Tablet.*;
import com.tayek.io.*;
import com.tayek.io.Audio.AudioObserver;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.TabletImpl2;
import com.tayek.tablet.Message.Type;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.io.GuiAdapter.GuiAdapterABC;
import com.tayek.utilities.*;
public class RunnerABC implements Runnable {
    // maybe change this so it finds the network interface first
    // and the picks the group
    // so a tablet could be in a different group
    // so maybe get the network interfaces first
    // should be only one, so use that to get the group
    // wifi should have only one interface
    // pc may gave more than one though
    public RunnerABC(Group group,String router,String routerPrefix) {
        pl("construct runner: "+this);
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
    private void setTablet(Tablet value) {
        tablet=value;
        if(hasATablet!=null) hasATablet.setTablet(value);
        if(guiAdapterABC!=null) guiAdapterABC.setTablet(value);
    }
    protected void createTabletAndStart(String tabletId) {
        restarts++;
        p("creating tablet and starting: "+restarts);
        tablet=Tablet.factory.create(type,group,tabletId,model);
        setTablet(tablet);
        p("config: "+tablet.config());
        boolean ok=tablet.startServer(); // don't forget to start accepting
        if(ok) l.warning("startServer() succeeded.");
        else l.severe("startServer() failed!");
    }
    public String status() {
        return "tabletId: "+tabletId+", host: "+host+", status: "+(tablet!=null?"on":"off");
    }
    protected void startSocetHandlersIfNoneAreOn() {
        if(isNetworkInterfaceUp()) {
            if(!LoggingHandler.areAnySockethandlersOn()) {
                l.info("trying to start socket handlers at: "+et);
                LoggingHandler.startSocketHandlers();
                // maybe stop and restart just in case the laptop cycled power or ?
            }
        } else LoggingHandler.stopSocketHandlers();
    }
    private void tryToStartTablet() {
        if(tabletId==null&&host==null) {
            l.warning("no tablet id and no host, gettin inetAddress from nic.");
            InetAddress inetAddress=addressWith(routerPrefix);
            if(inetAddress!=null) {
                l.warning("got inetAddress from nic: "+inetAddress);
                host=inetAddress.getHostAddress();
                prefs.put("host",host);
                tabletId=group.getTabletIdFromInetAddress(inetAddress,null);
                if(tabletId!=null) {
                    l.warning("got tabletId from group "+tabletId);
                    prefs.put("tabletId",tabletId);
                } else l.warning("can not get tabletId from group!");
            } else l.warning("can not get inetAddress despite network being up!");
        }
        if(tablet!=null) if(host==null) { // assume that they will all be the same!
            String host=group.required(tabletId).host;
            if(host!=null) {
                l.warning("found host from group: : "+host);
                prefs.put("host",host);
            } else l.warning("group can not find host for tableiId: "+tabletId);
        }
        if(host!=null) if(tabletId==null) { // maybe use network interface instead?
            tabletId=group.getTabletIdFromHost(host,null);
            if(tabletId!=null) {
                l.warning("got tabletId from host: "+tabletId);
                prefs.put("tabletId",tabletId);
            } else l.warning("can not get tabletId from host!");
        }
        if(tabletId!=null&&host!=null) if(tablet==null) {
            if(true||oldTablet==null) createTabletAndStart(tabletId);
            else {
                l.warning("using old tablet.");
                setTablet(oldTablet);
                tablet.config().logErrors=true;
                boolean ok=tablet.startServer(); // don't forget to start accepting
                if(!ok) l.severe("startServer() failed!");
            }
            if(tablet!=null) {
                if(false) startSocetHandlersIfNoneAreOn();
            }
        }
    }
    public void stop() {
        if(audioObserver.isChimimg()) audioObserver.stopChimer();
        if(tablet!=null) {
            l.warning("stopping tablet: "+tabletId);
            tablet.stopServer();
            tablet=null;
            if(hasATablet!=null) hasATablet.setTablet(null);
            if(guiAdapterABC!=null) guiAdapterABC.setTablet(null);
            if(LoggingHandler.areAnySockethandlersOn()) LoggingHandler.stopSocketHandlers();
        }
    }
    protected void loop(int n) {
        p("host: "+host+", tabletId: "+tabletId+", "+this+", model: "+model+", is shutting down: "+isShuttingDown+", is tablet null: "+(tablet==null));
        if(!model.areAnyButtonsOn()&&audioObserver.isChimimg()) {
            pl("had to stop chimer in runner loop!");
            audioObserver.stopChimer();
        }
        p(this+", base class loop iteration: "+n+", has: "+Thread.activeCount()+" threads.");
        if(Thread.activeCount()>=10) {
            printThreads();
            p(this+" base class loop iteration: "+n+", has: "+Thread.activeCount()+" threads.");
        }
        isNetworkInterfaceUp=isNetworkInterfaceUp();
        p("network interface is "+(isNetworkInterfaceUp?"up":"not up!"));
        isRouterOk=isRouterOk();
        p("router is "+(isRouterOk?"ok":"not ok!"));
        //p("socket handlers: "+LoggingHandler.socketHandlers());
        if(true) {
            LoggingHandler.printSocketHandlers();
            if(isNetworkInterfaceUp) startSocetHandlersIfNoneAreOn();
        }
        // mainActivity.waitForWifi(); // do we need this?
        // does not seem to work anymore :(
        if(tablet==null) {
            if(isNetworkInterfaceUp&&isRouterOk) tryToStartTablet();
            else if(hasATablet!=null) hasATablet.setStatusText("can not start tablet, check wifi and router!");
        } else {
            if(isNetworkInterfaceUp&&isRouterOk) {
                if(!tablet.isServerRunning()) {
                    l.severe("server is not running, trying to start.");
                    tablet.startServer();
                }
                if(tablet!=null&&heartbeatperiod!=0&&n%heartbeatperiod==0&&n>0) {
                    Message message=tablet.messageFactory().other(Type.heartbeat,tablet.group().groupId,tablet.tabletId());
                    tablet.broadcast(message);
                }
            } else {
                pl("tablet is up, but network interface or router is down.");
                stop();
                if(hasATablet!=null) hasATablet.setStatusText("tablet was stopped, check wifi and router!");
            }
        }
    }
    @Override public void run() {
        thread=Thread.currentThread();
        thread.setName(this.toString());
        l.warning("enter run() at: "+et+", tabletId: "+tabletId);
        init(model);
        l.warning("building gui");
        buildGui(model); // clone group if more than one tablet on this guy?
        l.warning("prefs: "+prefs);
        if(prefs.get("tabetId")!=null&&!prefs.get("tabetId").equals("")) tabletId=prefs.get("tabetId");
        if(prefs.get("host")!=null&&!prefs.get("host").equals("")) host=prefs.get("host");
        l.warning("before loop, host: "+host+", tabletId: "+tabletId);
        //isShuttingDown=false; // looks like a race condition!
        while(!isShuttingDown)
            try {
                l.info("start looping at: "+et+", "+isShuttingDown);
                if(hasATablet!=null) hasATablet.setStatusText(status());
                loop(n++);
                if(hasATablet!=null) hasATablet.setStatusText(status());
                try {
                    Thread.sleep(loopSleep);
                } catch(Exception e) {
                    l.warning("sleep was interrupted at: "+et);
                }
            } catch(Exception e) {
                l.severe("runner caught: "+e);
            }
        pl("exit runner loop");
        stop();
        pl(this+" is exiting run()");
    }
    @Override public String toString() {
        return "runner #"+instance;
    }
    public volatile Boolean isShuttingDown=false;
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
    public final int instance=++instances;
    public HasATablet hasATablet;
    protected GuiAdapterABC guiAdapterABC;
    protected boolean isNetworkInterfaceUp,isRouterOk;
    public int restarts,n;
    public int loopSleep=30_000;
    protected final int heartbeatperiod=10;
    static Integer instances=0;
    public Tablet.Type type=Tablet.Type.normal;
}
