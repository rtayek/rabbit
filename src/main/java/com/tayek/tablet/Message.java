package com.tayek.tablet;
import static com.tayek.io.IO.l;
import java.util.regex.Pattern;
import com.tayek.tablet.Message.Factory.MetaFactory;
import com.tayek.utilities.Single;
public interface Message {
    public enum Type { // add levels for logging?
        drive,stopDriving,forever,dummy,ping,ack,error,normal,reset,name,heartbeat,soundOn,soundOff,rolloverLogNow;
        public boolean isNormal() {
            return this.equals(normal);
        }
        public boolean isControl() {
            return !this.equals(normal);
        }
    }
    public Type type();
    public String groupId();
    public String tabletId();
    public Integer button();
    public Integer number();
    public String string();
    public Boolean state(int buttonId);
    interface Factory {
        //Message create(String string);
        //Message from(String string);
        public Message normal(String groupId,String tabletId,int buttonId,String states);
        public Message error(String string);
        public Message empty();
        public Message other(Type type,String groupId,String tabletId);
        public Message from(String string);
        interface MetaFactory {
            Factory create(Single<Integer> single);
            static class FImpl implements MetaFactory {
                @Override public Factory create(Single<Integer> single) {
                    return new MessagesFactory(single);
                }
                private static class MessagesFactory implements Message.Factory {
                    MessagesFactory(Single<Integer> single) {
                        this.messages=single;
                    }
                    @Override public MessageImpl normal(String groupId,String tabletId,int buttonId,String states) {
                        return new MessageImpl(Type.normal,groupId,tabletId,buttonId,states,++messages.t);
                    }
                    @Override public MessageImpl error(String string) {
                        return new MessageImpl(Type.error,"0","error",0,string,++messages.t); // put in some id's?
                    }
                    @Override public MessageImpl empty() {
                        ++messages.t;
                        return new MessageImpl();
                    }
                    @Override public MessageImpl other(Type type,String groupId,String tabletId) {
                        return new MessageImpl(type,groupId,tabletId,0,type.name(),++messages.t);
                    }
                    private class MessageImpl implements Message,java.io.Serializable {
                        // add message to add tablet to required, so he joins the group?
                        private MessageImpl() {
                            this.type=null;
                            this.groupId=null;
                            this.tabletId=null;
                            this.button=null;
                            this.string=null;
                            this.number=null;
                        }
                        private MessageImpl(Type type,String groupId,String from,Integer button,String string,int number) {
                            this.type=type;
                            this.groupId=groupId;
                            this.tabletId=from;
                            this.button=button;
                            this.string=string;
                            this.number=number;
                        }
                        @Override public Type type() {
                            return type;
                        }
                        @Override public String groupId() {
                            return groupId;
                        }
                        @Override public String tabletId() {
                            return tabletId;
                        }
                        @Override public Integer button() {
                            return button;
                        }
                        @Override public Integer number() {
                            return number;
                        }
                        @Override public String string() {
                            return string;
                        }
                        @Override public Boolean state(int buttonId) {
                            return MessagesFactory.fromCharacter(string.charAt(buttonId-1));
                        }
                        @Override public String toString() {
                            if(type==null) return empty; // hack, find a better way!
                            // maybe have null type?
                            String s=type.name()+delimiter+groupId+delimiter+tabletId+delimiter+button+delimiter+number+delimiter+string;
                            if(longMessages) s+=delimiter+longPart;
                            return s;
                        }
                        boolean longMessages; // for speed test
                        private final Type type;
                        private final String groupId;
                        private final String tabletId;
                        private final Integer button;
                        private final Integer number;
                        private final String string;
                        private static final long serialVersionUID=1L;
                    }
                    @Override public MessageImpl from(String string) {
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
                        MessageImpl message=new MessageImpl(type,groupId,fromId,button,stringPart,number);
                        return message;
                    }
                    public static Boolean fromCharacter(Character character) {
                        return character==null?null:character=='T'?true:character=='F'?false:null;
                    }
                    private final Single<Integer> messages;
                    static final String longPart;
                    static {
                        StringBuffer sb=new StringBuffer();
                        for(int i=0;i<1_024;i++)
                            sb.append(' ');
                        longPart=sb.toString();
                    }
                    public static final Character delimiter='|';
                }
            }
        }
    }
    MetaFactory instance=new MetaFactory.FImpl();
    String empty="empty message";
}
