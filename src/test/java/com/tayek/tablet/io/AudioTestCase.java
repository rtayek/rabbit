package com.tayek.tablet.io;
import org.junit.Test;
import com.tayek.tablet.io.Audio.Sound;
import static com.tayek.io.IO.*;
//import junit.framework.TestCase;
//https://svn.apache.org/repos/asf/harmony/enhanced/java/trunk/classlib/modules/sound/src/test/java/org/apache/harmony/sound/tests/javax/sound/sampled/AudioSystemTest.java
public class AudioTestCase /*extends TestCase*/ {
    protected void setUp() throws Exception {}
    protected void tearDown() throws Exception {
        //super.tearDown();
    }
    @Test public void test() throws InterruptedException {
        Audio.Instance.sound=true;
        p("sound: "+Audio.Instance.sound);
        for(Sound sound:Sound.values()) {
            p("sound: "+sound);
            Audio.audio.play(sound);
        }
        Thread.sleep(5_000);
    }
}
