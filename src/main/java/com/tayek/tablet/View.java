package com.tayek.tablet;
import java.util.*;
import java.util.logging.Logger;
import com.tayek.tablet.Receiver.Model;
import static com.tayek.tablet.io.IO.*;
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
        public final Logger l=Logger.getLogger(getClass().getName());
        private static int n=0;
    }
}
