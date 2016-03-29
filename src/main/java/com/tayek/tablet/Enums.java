package com.tayek.tablet;
import java.net.SocketAddress;
import java.util.logging.Level;
import com.tayek.io.IO;
import com.tayek.tablet.Messages.Type;
import static com.tayek.io.IO.*;
import com.tayek.tablet.io.*;
import com.tayek.utilities.Utility;
public class Enums {
    public enum LevelSubMenuItem {
        all(Level.ALL),finest(Level.FINEST),finer(Level.FINER),fine(Level.FINE),config(Level.CONFIG),info(Level.INFO),warning(Level.WARNING),sever(Level.SEVERE),none(Level.OFF);
        LevelSubMenuItem(Level level) {
            this.level=level;
        }
        public void doItem(Tablet tablet) {
            doItem(this,tablet);
        }
        public static boolean isItem(int ordinal) {
            return item(ordinal)!=null;
        }
        public static LevelSubMenuItem item(int ordinal) {
            return 0<=ordinal&&ordinal<values().length?values()[ordinal]:null;
        }
        public static void doItem(int ordinal,Tablet tablet) { // used by android
            if(tablet!=null) if(0<=ordinal&&ordinal<values().length) values()[ordinal].doItem(tablet);
            else l.warning(ordinal+" is invalid ordinal for!");
            else l.warning("tablet is null in do item!");
        }
        public static void doItem(LevelSubMenuItem levelSubMenuItem,final Tablet tablet) {
            LoggingHandler.setLevel(levelSubMenuItem.level);
        }
        private final Level level;
    }
    public enum MenuItem {
        ToggleLogging,Reset,Ping,Heartbeat,Connect,Disconnect,Log,Sound,Simulate,Quit,Drive,Forever,Level;
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
            switch(tabletMenuItem) {
                case ToggleLogging:
                    LoggingHandler.toggleSockethandlers();
                    break;
                case Reset:
                    tablet.model.reset();
                    break;
                case Ping:
                    tablet.broadcast(tablet.stuff.messages.other(Type.ping,tablet.groupId,tablet.tabletId()),tablet.stuff);
                    break;
                case Heartbeat:
                    if(tablet.heartbeatTimer!=null) Tablet.startHeatbeat(tablet);
                    else tablet.stopHeartbeat();
                    break;
                case Disconnect:
                    tablet.stopListening();
                    break;
                case Connect:
                    SocketAddress socketAddress=tablet.stuff.socketAddress(tablet.tabletId());
                    if(!tablet.startListening(socketAddress)) l.info(Utility.method()+" startListening() failed!");
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
                    if(tablet.simulationTimer==null) Tablet.startSimulating(tablet);
                    else tablet.stopSimulating();
                    break;
                case Quit:
                    // System.exit(0); // how to test this?
                    break;
                case Drive:
                    new Thread(new Runnable() {
                        @Override public void run() {
                            Driver.drive(tablet,100,Tablet.driveWait);
                            l.severe("start drive histories.");
                            l.severe("drive: "+tablet.histories());
                            l.severe("end drive histories.");

                        }
                    }).start();
                    break;
                case Forever:
                    // maybe check to see if thread is already running
                    new Thread(new Runnable() {
                        @Override public void run() {
                            Driver.forever(tablet);
                        }
                    }).start();
                    break;
                default:
                    l.severe(tabletMenuItem+" was not handled!");
            }
        }
    }
}
