package com.tayek.tablet;
import static com.tayek.io.IO.l;
import java.util.regex.Pattern;
import com.tayek.Required;
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
    String host();
    Integer service();
    Type type();
    String groupId();
    String from();
    Integer button();
    Integer number();
    String string();
    Boolean state(int buttonId);
    interface Factory {
        Message normal(String groupId,String tabletId,int buttonId,String states);
        Message error(String string);
        Message empty();
        Message other(Type type,String groupId,String tabletId);
        Message from(String string);
        interface MetaFactory {
            Factory create(Required required,Single<Integer> single);
            class FImpl implements MetaFactory {
                @Override public Factory create(Required required,Single<Integer> single) {
                    return new MessageFactory(single,required);
                }
                private static class MessageFactory implements Message.Factory {
                    MessageFactory(Single<Integer> single,Required required) {
                        this.messages=single;
                        this.host=required.host;
                        this.service=required.service;
                    }
                    // has most things needed to construct a message
                    @Override public MessageImpl normal(String groupId,String tabletId,int buttonId,String states) {
                        return new MessageImpl(MessageFactory.this.host,MessageFactory.this.service,Type.normal,groupId,tabletId,buttonId,states,++messages.t);
                    }
                    @Override public MessageImpl error(String string) {
                        return new MessageImpl(MessageFactory.this.host,MessageFactory.this.service,Type.error,"0","error",0,string,++messages.t); // put in some id's?
                    }
                    @Override public MessageImpl empty() {
                        ++messages.t;
                        return new MessageImpl();
                    }
                    @Override public MessageImpl other(Type type,String groupId,String tabletId) {
                        return new MessageImpl(MessageFactory.this.host,MessageFactory.this.service,type,groupId,tabletId,0,type.name(),++messages.t);
                    }
                    private class MessageImpl implements Message,java.io.Serializable {
                        // add message to add tablet to required, so he joins the group?
                        private MessageImpl() {
                            this.host=null;
                            this.service=null;
                            this.type=null;
                            this.groupId=null;
                            this.tabletId=null;
                            this.button=null;
                            this.string=null;
                            this.number=null;
                        }
                        private MessageImpl(String host,Integer service,Type type,String groupId,String from,Integer button,String string,int number) {
                            this.host=host;
                            this.service=service;
                            this.type=type;
                            this.groupId=groupId;
                            this.tabletId=from;
                            this.button=button;
                            this.string=string;
                            this.number=number;
                        }
                        @Override public String host() {
                            return host;
                        }
                        @Override public Integer service() {
                            return service;
                        }
                        @Override public Type type() {
                            return type;
                        }
                        @Override public String groupId() {
                            return groupId;
                        }
                        @Override public String from() {
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
                            return MessageFactory.fromCharacter(string.charAt(buttonId-1));
                        }
                        @Override public String toString() {
                            if(type==null) return empty; // hack, find a better way!
                            // maybe have null type?
                            String s=host+delimiter+service+delimiter+type.name()+delimiter+groupId+delimiter+tabletId+delimiter+button+delimiter+number+delimiter+string;
                            if(longMessages) s+=delimiter+longPart;
                            return s;
                        }
                        boolean longMessages; // for speed test
                        private final String host;
                        private final Integer service;
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
                        if(parts.length<8) { return error("too short!"); }
                        String host=parts[0];
                        Integer service=parts[1].equals("null")?0:Integer.valueOf(parts[1]);
                        Type type=null;
                        try {
                            type=Type.valueOf(parts[2]);
                            if(type.equals(Type.error)) {
                                l.warning("from constructed and error message!");
                                return error(parts[7]);
                            }
                        } catch(IllegalArgumentException e) {
                            l.warning(string+" message caught: '"+e+"'");
                            e.printStackTrace();
                            return error(string+" threw: "+e);
                        }
                        String groupId=parts[3];
                        String fromId=parts[4];
                        Integer button=new Integer(parts[5]);
                        Integer number=new Integer(parts[6]);
                        String stringPart=parts[7];
                        if(type.equals(Type.normal)) for(int i=0;i<stringPart.length();i++)
                            if(!(stringPart.charAt(i)=='T'||stringPart.charAt(i)=='F')) return error(string+" has bad state value(s)!");
                        MessageImpl message=new MessageImpl(MessageFactory.this.host,MessageFactory.this.service,type,groupId,fromId,button,stringPart,number);
                        return message;
                    }
                    public static Boolean fromCharacter(Character character) {
                        return character==null?null:character=='T'?true:character=='F'?false:null;
                    }
                    private final Single<Integer> messages;
                    private final String host;
                    private final Integer service;
                    static final String longPart;
                    static {
                        StringBuffer sb=new StringBuffer();
                        for(int i=0;i<1_024;i++)
                            sb.append(' ');
                        longPart=sb.toString();
                    }
                }
            }
        }
    }
    MetaFactory instance=new MetaFactory.FImpl();
    Character delimiter='|';
    String empty="empty message";
}
