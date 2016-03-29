package com.tayek.tablet;
import java.util.*;
import com.tayek.tablet.MessageReceiver.Model;
import static com.tayek.io.IO.*;
public interface View extends Observer {
    public class CommandLine implements View {
        public CommandLine(Model model) {
            this.model=model;
        }
        @Override public void update(Observable observable,Object hint) {
            l.info(observable+" "+hint);
            if(observable instanceof Model) {
                if(observable==model) {
                    l.fine(id+" received update: "+observable+" "+hint);
                    p("model: "+model);
                } else l.warning(this+" "+id+" not our model!");
            } else l.warning(this+" "+id+" not a model!");
        }
        private final int id=++n;
        private final Model model;
        private static int n=0;
    }
}
