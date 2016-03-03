package com.tayek.tablet;
import static com.tayek.tablet.io.IO.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;
public class Message implements java.io.Serializable {
    // looks like i need to add a sequence number back in!
    public enum Type { // add levels for logging?
        dummy,ping,ack,error,normal,reset,name,heartbeat,soundOn,soundOff,rolloverLogNow;
        public boolean isNormal() {
            return this.equals(normal);
        }
        public boolean isControl() {
            return !this.equals(normal);
        }
    }
    private Message(Type type,Integer groupId,Integer from,Integer button,String string) {
        this.type=type;
        this.groupId=groupId;
        this.tabletId=from;
        this.button=button;
        this.string=string;
    }
    public static Boolean fromCharacter(Character character) {
        return character==null?null:character=='T'?true:character=='F'?false:null;
    }
    public Boolean state(int buttonId) {
        return fromCharacter(string.charAt(buttonId-1));
    }
    @Override public String toString() {
        return type.name()+delimiter+groupId+delimiter+tabletId+delimiter+button+delimiter+number+delimiter+string;
    }
    public static Message staticFrom(String string) {
        if(string==null) {
            staticLogger.warning("string is null!");
            return null;
        }
        String[] parts=string.split(Pattern.quote(""+delimiter));
        if(parts.length<6) { return error("too short!"); }
        Type type=null;
        try {
            type=Type.valueOf(parts[0]);
            if(type.equals(Type.error)) {
                staticLogger.warning("from constructed and error message!");
                return error(parts[5]);
            }
        } catch(IllegalArgumentException e) {
            staticLogger.warning(string+" message caught: '"+e+"'");
            e.printStackTrace();
            return error(string+" threw: "+e);
        }
        Integer groupId=new Integer(parts[1]);
        Integer fromId=new Integer(parts[2]);
        Integer button=new Integer(parts[3]);
        Integer number=new Integer(parts[4]);
        String stringPart=parts[5];
        if(type.equals(Type.normal)) for(int i=0;i<stringPart.length();i++)
            if(!(stringPart.charAt(i)=='T'||stringPart.charAt(i)=='F')) return error(string+" has bad state value(s)!");
        Message message=new Message(type,groupId,fromId,button,stringPart);
        message.number=number;
        return message;
    }
    public static Message normal(int groupId,int tabletId,int buttonId,Model model) {
        return new Message(Message.Type.normal,groupId,tabletId,buttonId,model.toCharacters());
    }
    public static Message normal(int groupId,int tabletId,int buttonId,String states) {
        return new Message(Message.Type.normal,groupId,tabletId,buttonId,states);
    }
    public static Message reset(int groupId,int tabletId,int buttonId) {
        return new Message(Type.reset,groupId,tabletId,buttonId,Type.reset.name()); // put in some id's?
    }
    public static Message error(String string) {
        return new Message(Type.error,0,0,0,string); // put in some id's?
    }
    public static Message dummy(int groupId,int tabletId) {
        return new Message(Type.dummy,groupId,tabletId,0,Type.dummy.name());
    }
    public static Message ping(int groupId,int tabletId) {
        return new Message(Type.ping,groupId,tabletId,0,Type.ping.name());
    }
    public static Message ack(int groupId,int tabletId) {
        return new Message(Type.ack,groupId,tabletId,0,Type.ack.name());
    }
    public static Message heartbeat(int groupId,int tabletId) {
        return new Message(Type.heartbeat,groupId,tabletId,0,Type.heartbeat.name());
    }
    public static Message soundOn(int groupId,int tabletId) {
        return new Message(Type.soundOn,groupId,tabletId,0,Type.soundOn.name());
    }
    public static Message soundOff(int groupId,int tabletId) {
        return new Message(Type.soundOff,groupId,tabletId,0,Type.soundOff.name());
    }
    public static Message rolloverLogNow(int groupId,int tabletId) {
        return new Message(Type.rolloverLogNow,groupId,tabletId,0,"rollover");
    }
    public final Type type;
    public final Integer groupId;
    public final Integer tabletId;
    public final Integer button;
    public Integer number=0;
    public final String string;
    public static final Character delimiter='|';
    public static final Set<Class<?>> set=new LinkedHashSet<>();
    private static final long serialVersionUID=1L;
    public final Logger l=Logger.getLogger(getClass().getName());
}
