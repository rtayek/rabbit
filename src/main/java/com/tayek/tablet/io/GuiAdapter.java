package com.tayek.tablet.io;
import java.util.*;
import com.tayek.*;
import com.tayek.Tablet.HasATablet;
import com.tayek.tablet.MessageReceiver.Model;
import static com.tayek.io.IO.*;
public interface GuiAdapter extends HasATablet,Observer {
    void setButtonState(int id,boolean state); // of the widget!
    void setButtonText(int id,String string); // of the widget!
    public abstract class GuiAdapterABC implements GuiAdapter {
        public GuiAdapterABC(Model model) {
            this.model=model;
        }
        public void processClick(int index) {
            int id=index+1;
            p("click: "+index+" in: "+this);
            if(tablet!=null) {
                if(1<=id&&id<=model.buttons) tablet.click(id);
                else p(tablet+",  "+id+" is bad button id!");
            } else {
                p("click with null tablet!");
            }
        }
        @Override public void update(Observable observable,Object hint) {
            for(Integer buttonId=1;buttonId<=model.buttons;buttonId++) {
                setButtonState(buttonId,model.state(buttonId));
                if(tablet!=null) setButtonText(buttonId,model.getButtonText(buttonId,tablet.tabletId()));
            }
        }
        @Override public Tablet tablet() {
            return tablet;
        }
        @Override public void setTablet(Tablet tablet) {
            p(this+" setting tablet to: "+tablet);
            this.tablet=tablet;
        }
        private final Model model;
        private Tablet tablet;
    }
}
