package com.tayek.tablet;
import static com.tayek.io.IO.*;
import static com.tayek.utilities.Utility.pad;
import java.util.*;
import com.tayek.*;
import com.tayek.io.Audio;
import com.tayek.io.Audio.Sound;
import com.tayek.utilities.Colors;
public interface MessageReceiver {
    void receive(Message message);
    public static class DummyReceiver implements MessageReceiver {
        @Override public void receive(Message message) {
            this.message=message;
        }
        public Message message;
        // maybe history belongs here?
    }
    public static class Model extends Observable implements MessageReceiver,Cloneable {
        public Model(int buttons,Integer resetButtonId) {
            this(buttons,resetButtonId,++ids);
        }
        private Model(int buttons,Integer resetButtonId,int serialNumber) {
            this.serialNumber=serialNumber; // so clones will have the same serial number
            this.buttons=buttons;
            states=new Boolean[buttons];
            this.resetButtonId=resetButtonId;
            this.colors=new Colors();
            reset();
        }
        @Override public Model clone() {
            return new Model(buttons,resetButtonId,serialNumber);
        }
        public void reset() {
            synchronized(states) {
                for(int i=1;i<=buttons;i++)
                    setState(i,false);
            }
        }
        public void setChangedAndNotify(Object object) {
            setChanged();
            notifyObservers(object);
        }
        public boolean areAnyButtonsOn() {
            boolean areAnyButtonsOn=false;
            for(int i=0;i<buttons;i++)
                areAnyButtonsOn|=states[i];
            return areAnyButtonsOn;
        }
        public void setState(Integer id,Boolean state) {
            if(1<=id&&id<=buttons) synchronized(states) {
                states[id-1]=state;
                setChangedAndNotify(id);
            }
            else l.warning("out of bounds: "+id);
        }
        public Boolean state(Integer id) {
            if(1<=id&&id<=buttons) synchronized(states) {
                return states[id-1];
            }
            else {
                l.warning("out of bounds: "+id);
                return null;
            }
        }
        public Boolean[] states() {
            Boolean[] copy=new Boolean[buttons];
            synchronized(states) {
                System.arraycopy(states,0,copy,0,buttons);
                return copy;
            }
        }
        boolean doingLastOnFrom=false;
        public String getButtonText(Integer buttonId,String tabletId) {
            String text=null;
            if(!doingLastOnFrom) {
                if(buttonId.equals(resetButtonId)) text="R";
                else if((buttonId-1)/colors.columns%2==0) text=""+(char)('0'+buttonId);
            } else {
                String ourName=tabletId+":B"+buttonId;
                if(state(buttonId).equals(true)) {
                    Object lastOnFrom=lastOnFrom(buttonId);
                    if(lastOnFrom!=null) {
                        String hisName=lastOnFrom+":B"+buttonId;
                        text=pad(ourName+" ("+hisName+")",length);
                    } else text=pad(ourName,length);
                } else text=pad(ourName,length);
            }
            return text;
        }
        public Object lastOnFrom(Integer id) {
            synchronized(idToLastOnFrom) {
                return idToLastOnFrom.get(id);
            }
        }
        public int check(String newStates,Integer button) {
            String currentStates=toCharacters();
            int n=0;
            for(int i=0;i<buttons;i++)
                if(newStates.charAt(i)!=currentStates.charAt(i)) if(button==null||i!=button) n++;
            return n;
        }
        public int check(String newStates) {
            return check(newStates,null);
        }
        private boolean normal(Message message) {
            // maybe just always assume that the message is correct
            // and save the state?
            int n=check(message.string(),message.button()-1);
            String failure=n+" other buttons are out of sync before";
            if(histories!=null) {
                String more="#"+histories.modelHistory.history.attempts()+", "+failure+", "+message.string()+"!="+toCharacters();
                if(n>0) histories.modelHistory.history.failure(failure);
                else histories.modelHistory.history.success();
                if(n>0) l.warning(more);
            } else {
                l.warning("histories is null!");
                String more="failure: "+failure+", "+message.string()+"!="+toCharacters();
                if(n>0) l.warning(more);
            }
            for(int i=1;i<=Math.min(buttons,message.string().length());i++) {
                // update will get called many times!
                if(i==message.button()) if(message.state(i)) { // turn on?
                    synchronized(idToLastOnFrom) {
                        idToLastOnFrom.put(i,message.from());
                    }
                    if(false) if(!state(i).equals(message.state(i))) { // will turn on?
                        setChangedAndNotify(Sound.electronic_chime_kevangc_495939803);
                    }
                }
                if(i==message.button()) setState(i,message.state(i));
            }
            // the above always set's the state from the message,
            // so check should never find an error with button.
            for(int i=1;i<=Math.min(buttons,message.string().length());i++)
                setState(i,message.state(i));
            if(histories!=null) l.info("#"+histories.modelHistory.history.attempts()+", new state: "+toCharacters());
            return n==0;
        }
        @Override public void receive(Message message) {
            if(message!=null) {
                l.fine("received message: "+message);
                messages++;
                switch(message.type()) {
                    case heartbeat:
                        l.info("received hreatbeat: "+messages);
                        break;
                    case normal:
                        normal(message);
                        break;
                    case dummy:
                        break;
                    case ping:
                        break;
                    case ack:
                        break;
                    case error:
                        l.severe("reciveived error message: "+message);
                        break;
                    case reset:
                        reset();
                        break;
                    case soundOn:
                        Audio.Instance.sound=true;
                        break;
                    case soundOff:
                        Audio.Instance.sound=false;
                        break;
                    default:
                        l.severe("message type: "+message.type()+" was not handled!");
                }
            } else l.warning(this+"received null message!");
        }
        public static Character toCharacter(Boolean state) {
            return state==null?null:state?'T':'F';
        }
        public String toCharacters() {
            String s="";
            for(boolean state:states())
                s+=toCharacter(state);
            return s;
        }
        @Override public String toString() {
            String s="(#"+serialNumber+"): {";
            synchronized(states) {
                s+=toCharacters();
                s+='}';
                return s;
            }
        }
        public static void main(String[] args) throws Exception {
            Model model=new Model(7,null);
            p(model.toString());
        }
        public final Integer serialNumber;
        public final Integer buttons;
        public final Integer resetButtonId;
        public final Colors colors;
        Histories histories;
        Integer messages=0;
        private final Boolean[] states;
        private final Map<Object,Object> idToLastOnFrom=new LinkedHashMap<>();
        private final Random random=new Random();
        static int ids=0;
        static final int length=10;
        public static final Model mark1=new Model(11,11);
    }
}
