package com.tayek.speed;

import static com.tayek.io.IO.p;
import static org.junit.Assert.*;
import java.util.Map;
import java.util.logging.Level;
import org.junit.*;
import org.junit.rules.TestRule;
import com.tayek.*;
import com.tayek.Tablet.Factory.FactoryImpl.TabletImpl1;
import com.tayek.io.LoggingHandler;
import static com.tayek.io.IO.*;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.Groups;
import com.tayek.tablet.Message.Type;
import com.tayek.tablet.MessageReceiver.Model;

public class TabletTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void test() {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.ALL);
        //LoggingHandler.toggleSockethandlers();
        Map<String,Required> requireds=null;
        if(true) requireds=new Groups().groups.get("g0");
        else requireds=new Groups().groups.get("g2OnPc");
        p("requireds: "+requireds);
        Group group=new Group("1",requireds,Model.mark1);
        String id=group.keys().iterator().next(); // 
        Tablet tablet=Tablet.factory.create1(group,id);
        TabletImpl1 t=(TabletImpl1)tablet;
        p("tablet: "+t.server.maps());
        // need to add connections!
        Message message=tablet.messageFactory().other(Type.heartbeat,tablet.group().groupId,tablet.tabletId());
        tablet.broadcast(message);

        
    }
}
