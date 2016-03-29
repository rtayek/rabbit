package com.tayek.tablet;
import static com.tayek.io.IO.*;
import java.util.regex.Pattern;
import com.tayek.tablet.MessageReceiver.Model;
public class Messages {
    public enum Type { // add levels for logging?
        drive,forever,dummy,ping,ack,error,normal,reset,name,heartbeat,soundOn,soundOff,rolloverLogNow;
        public boolean isNormal() {
            return this.equals(normal);
        }
        public boolean isControl() {
            return !this.equals(normal);
        }
    }
    public Message normal(String groupId,String tabletId,int buttonId,Model model) {
        String string="";
        if(model!=null) string=model.toCharacters();
        else for(int i=0;i<Model.mark1.buttons;i++)
            string+='?';
        return new Message(Type.normal,groupId,tabletId,buttonId,string,++messages);
    }
    public Message normal(String groupId,String tabletId,int buttonId,String states) {
        return new Message(Type.normal,groupId,tabletId,buttonId,states,++messages);
    }
    public Message error(String string) {
        return new Message(Type.error,"0","error",0,string,++messages); // put in some id's?
    }
    public Message empty() {
        ++messages;
        return new Message();
    }
    public Message other(Type type,String groupId,String tabletId) {
        return new Message(type,groupId,tabletId,0,type.name(),++messages);
    }
    public class Message implements java.io.Serializable {
        // looks like i need to add a sequence number back in!
        // add message to add tablet to info, so he joins the group?
        private Message() {
            this.type=null;
            this.groupId=null;
            this.tabletId=null;
            this.button=null;
            this.string=null;
            this.number=null;
        }
        private Message(Type type,String groupId,String from,Integer button,String string,int number) {
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
            if(type==null)
                return empty; // hack, find a better way!
            // maybe have null type?
            String s=type.name()+delimiter+groupId+delimiter+tabletId+delimiter+button+delimiter+number+delimiter+string;
            if(longMessages) s+=delimiter+longPart;
            return s;
        }
        boolean longMessages; // for speed test
        public final Type type;
        public final String groupId;
        public final String tabletId;
        public final Integer button;
        public final Integer number;
        public final String string;
        public static final String empty="empty message";
        private static final long serialVersionUID=1L;
    }
    public Message from(String string) {
        if(string==null) {
            l.warning("string is null!");
            return null;
        }
        String[] parts=string.split(Pattern.quote(""+delimiter));
        if(parts.length<6) { return error("too short!"); }
        Type type=null;
        try {
            type=Type.valueOf(parts[0]);
            if(type.equals(Type.error)) {
                l.warning("from constructed and error message!");
                return error(parts[5]);
            }
        } catch(IllegalArgumentException e) {
            l.warning(string+" message caught: '"+e+"'");
            e.printStackTrace();
            return error(string+" threw: "+e);
        }
        String groupId=parts[1];
        String fromId=parts[2];
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
    static final String longPart;
    static {
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<1_024;i++)
            sb.append(' ');
        longPart=sb.toString();
    }
    public static final Character delimiter='|';
}
