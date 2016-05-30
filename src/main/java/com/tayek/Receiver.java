package com.tayek;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import com.tayek.*;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.TabletImpl2;
import com.tayek.tablet.Message.*;
import com.tayek.utilities.Et;
import static com.tayek.io.IO.*;
public interface Receiver { // Consumer<Object>
    void receive(Object message);
    public class DummyReceiver implements Receiver {
        @Override public void receive(Object message) {
            this.message=message;
        }
        public Object message;
    }

}