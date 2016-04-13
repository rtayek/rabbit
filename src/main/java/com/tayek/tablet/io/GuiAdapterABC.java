package com.tayek.tablet.io;
import java.util.Observable;
import com.tayek.tablet.*;
public abstract class GuiAdapterABC implements GuiAdapter,View {
    public GuiAdapterABC(Tablet tablet) {
        this.tablet=tablet;
    }
    public void processClick(int index) {
        tablet.click(index+1); // button id is index+1
    }
    @Override public void update(Observable o,Object hint) {
        for(Integer buttonId=1;buttonId<=tablet.model.buttons;buttonId++) {
            setButtonState(buttonId,tablet.model.state(buttonId));
            setButtonText(buttonId,tablet.getButtonText(buttonId));
        }
    }
    public final Tablet tablet;
}
