package com.tayek.tablet.io;
import static com.tayek.io.IO.p;
import java.net.InetAddress;
import com.tayek.Tablet;
import com.tayek.Tablet.HasATablet;
import com.tayek.io.Audio;
import com.tayek.io.Audio.AudioObserver;
import com.tayek.tablet.Group;
import com.tayek.tablet.Group.TabletImpl2;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.io.GuiAdapter.GuiAdapterABC;
import com.tayek.utilities.Colors;
public abstract class RunnerABC implements Runnable {
    public RunnerABC(Group group) {
        p("group: "+group);
        for(String id:group.keys())
            p("required: "+group.required(id));
        this.group=group;
        this.model=group.getModelClone();
        this.colors=model.colors;
        audioObserver=new AudioObserver(model);
        model.addObserver(audioObserver);
    }
    public void init() {
        Audio.audio.play(Audio.Sound.glass_ping_go445_1207030150);
    }
    public abstract void buildGui();
    public void createTabletAndStart(String tabletId) {
        p("creating tablet and starting.");
        tablet=Tablet.factory.create2(tabletId,group,model);
        gui.setTablet(tablet);
        guiAdapterABC.setTablet(tablet);
        if(tablet instanceof TabletImpl2) ((TabletImpl2)tablet).startListening();
    }
    public void stop() {
        if(tablet!=null) {
            if(audioObserver.isChimimg())
                audioObserver.stopChimer();
            if(tablet instanceof TabletImpl2) ((TabletImpl2)tablet).stopListening();
            tablet=null;
            gui.setTablet(null);
            guiAdapterABC.setTablet(null);
        }
    }
    public final Model model;
    public final Colors colors;
    public final Group group;
    final AudioObserver audioObserver;
    public InetAddress inetAddress;
    public String tabletId;
    public Tablet tablet;
    protected HasATablet gui;
    protected GuiAdapterABC guiAdapterABC;
}
