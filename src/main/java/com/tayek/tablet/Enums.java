package com.tayek.tablet;
import java.net.SocketAddress;
import java.util.logging.Level;
import com.tayek.Tablet;
import com.tayek.io.*;
import com.tayek.tablet.Group.TabletImpl2;
import com.tayek.tablet.Message.Type;
import static com.tayek.io.IO.*;
import com.tayek.tablet.io.*;
import com.tayek.utilities.Utility;
public class Enums {
    public enum LevelSubMenuItem {
        all(Level.ALL),finest(Level.FINEST),finer(Level.FINER),fine(Level.FINE),config(Level.CONFIG),info(Level.INFO),warning(Level.WARNING),sever(Level.SEVERE),none(Level.OFF);
        LevelSubMenuItem(Level level) {
            this.level=level;
        }
        public void doItem() {
            doItem(this);
        }
        public static boolean isItem(int ordinal) {
            return item(ordinal)!=null;
        }
        public static LevelSubMenuItem item(int ordinal) {
            return 0<=ordinal&&ordinal<values().length?values()[ordinal]:null;
        }
        public static void doItem(int ordinal) { // used by android
            if(0<=ordinal&&ordinal<values().length) values()[ordinal].doItem();
            else l.warning(ordinal+" is invalid ordinal for!");
        }
        public static void doItem(LevelSubMenuItem levelSubMenuItem) {
            LoggingHandler.setLevel(levelSubMenuItem.level);
        }
        private final Level level;
    }
    public enum MenuItem {
        ToggleLogging,Reset,Ping,Heartbeat,Connect,Disconnect,Log,Sound,Simulate,Quit,Drive,StopDriving,Forever,Level;
        public void doItem(Tablet tablet) {
            doItem(this,tablet);
        }
        public static boolean isItem(int ordinal) {
            return item(ordinal)!=null;
        }
        public static MenuItem item(int ordinal) {
            return 0<=ordinal&&ordinal<values().length?values()[ordinal]:null;
        }
        public static void doItem(int ordinal,Tablet tablet) { // used by android
            if(tablet!=null) if(0<=ordinal&&ordinal<values().length) values()[ordinal].doItem(tablet);
            else l.warning(ordinal+" is invalid ordinal for!");
            else l.warning("tablet is null in do item!");
        }
        public static void doItem(MenuItem tabletMenuItem,final Tablet tablet) {
            if(tablet==null) l.warning("tablet is null in doItem: "+tabletMenuItem);
            switch(tabletMenuItem) {
                case ToggleLogging:
                    LoggingHandler.toggleSockethandlers();
                    break;
                case Reset:
                    if(tablet!=null) tablet.model().reset();
                    break;
                case Ping:
                    if(tablet!=null) tablet.broadcast(tablet.messageFactory().other(Type.ping,tablet.groupId(),tablet.tabletId()));
                    break;
                case Heartbeat:
                    if(tablet!=null) {
                        if(((TabletImpl2)tablet).heartbeatTimer!=null) ((TabletImpl2)tablet).startHeatbeat();
                        else((TabletImpl2)tablet).stopHeartbeat();
                    }
                    break;
                case Disconnect:
                    if(tablet!=null) ((TabletImpl2)tablet).stopListening();
                    break;
                case Connect:
                    if(tablet!=null) if(!((TabletImpl2)tablet).startListening()) l.info(Utility.method()+" startListening() failed!");
                    break;
                case Log:
                    // gui.textView.setVisible(!gui.textView.isVisible());
                    break;
                //case Level: // handled by submenu 
                //    break; // no, it's not
                case Sound:
                    Audio.Instance.sound=!Audio.Instance.sound;
                    l.info("sound: "+Audio.Instance.sound);
                    break;
                case Simulate:
                    if(tablet!=null) {
                        if(((TabletImpl2)tablet).simulationTimer==null) ((TabletImpl2)tablet).startSimulating();
                        else((TabletImpl2)tablet).stopSimulating();
                    }
                    break;
                case Quit:
                    if(!System.getProperty("os.name").contains("indows")) {
                        p("calling System.exit().");
                        System.exit(0);
                    }
                    break;
                case Drive:
                    if(tablet!=null) ((TabletImpl2)tablet).driveInThread(true);
                    break;
                case StopDriving:
                    if(tablet!=null) ((TabletImpl2)tablet).stopDriving=true;
                    break;
                case Forever:
                    if(tablet!=null) ((TabletImpl2)tablet).foreverInThread();
                    break;
                default:
                    l.severe(tabletMenuItem+" was not handled!");
            }
        }
    }
}
