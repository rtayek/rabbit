package com.tayek.tablet;
import static com.tayek.tablet.io.IO.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import com.tayek.tablet.Receiver.Model;
public class Messages {
    public enum Type { // add levels for logging?
        dummy,ping,ack,error,normal,reset,name,heartbeat,soundOn,soundOff,rolloverLogNow;
        public boolean isNormal() {
            return this.equals(normal);
        }
        public boolean isControl() {
            return !this.equals(normal);
        }
    }
    public Message normal(int groupId,int tabletId,int buttonId,Model model) {
        return new Message(Type.normal,groupId,tabletId,buttonId,model.toCharacters(),++messages);
    }
    public Message normal(int groupId,int tabletId,int buttonId,String states) {
        return new Message(Type.normal,groupId,tabletId,buttonId,states,++messages);
    }
    public Message reset(int groupId,int tabletId,int buttonId) {
        return new Message(Type.reset,groupId,tabletId,buttonId,Type.reset.name(),++messages); // put in some id's?
    }
    public Message error(String string) {
        return new Message(Type.error,0,0,0,string,++messages); // put in some id's?
    }
    public Message dummy(int groupId,int tabletId) {
        return new Message(Type.dummy,groupId,tabletId,0,Type.dummy.name(),++messages);
    }
    public Message ping(int groupId,int tabletId) {
        return new Message(Type.ping,groupId,tabletId,0,Type.ping.name(),++messages);
    }
    public Message ack(int groupId,int tabletId) {
        return new Message(Type.ack,groupId,tabletId,0,Type.ack.name(),++messages);
    }
    public Message heartbeat(int groupId,int tabletId) {
        return new Message(Type.heartbeat,groupId,tabletId,0,Type.heartbeat.name(),++messages);
    }
    public Message soundOn(int groupId,int tabletId) {
        return new Message(Type.soundOn,groupId,tabletId,0,Type.soundOn.name(),++messages);
    }
    public Message soundOff(int groupId,int tabletId) {
        return new Message(Type.soundOff,groupId,tabletId,0,Type.soundOff.name(),++messages);
    }
    public Message rolloverLogNow(int groupId,int tabletId) {
        return new Message(Type.rolloverLogNow,groupId,tabletId,0,"rollover",++messages);
    }
    public class Message implements java.io.Serializable {
        // looks like i need to add a sequence number back in!
        // add message to add tablet to info, so he joins the group?
        private Message(Type type,Integer groupId,Integer from,Integer button,String string,int number) {
            this.type=type;
            this.groupId=groupId;
            this.tabletId=from;
            this.button=button;
            this.string=string;
            this.number=number;
        }
        public Boolean state(int buttonId) {
            return Messages.fromCharacter(string.charAt(buttonId-1));
        }
        @Override public String toString() {
            return type.name()+delimiter+groupId+delimiter+tabletId+delimiter+button+delimiter+number+delimiter+string;
        }
        public final Type type;
        public final Integer groupId;
        public final Integer tabletId;
        public final Integer button;
        public final Integer number;
        public final String string;
        private static final long serialVersionUID=1L;
        public final Logger l=Logger.getLogger(getClass().getName());
    }
    public Message from(String string) {
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
        Message message=new Message(type,groupId,fromId,button,stringPart,number);
        return message;
    }
    public static Boolean fromCharacter(Character character) {
        return character==null?null:character=='T'?true:character=='F'?false:null;
    }
    private Integer messages=0;
    public static final Character delimiter='|';
}
