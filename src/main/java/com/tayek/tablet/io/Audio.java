package com.tayek.tablet.io;
import static com.tayek.tablet.io.IO.*;
import java.io.BufferedInputStream;
import java.util.concurrent.*;
import java.util.logging.Logger;
import javax.sound.sampled.*;
public interface Audio {
    enum Sound {
        electronic_chime_kevangc_495939803,glass_ping_go445_1207030150,store_door_chime_mike_koenig_570742973;
    }
    void play(Sound sound);
    static class Instance {
        public static boolean sound=false;
    }
    Audio audio=Factory.Implementation.instance().create();
    Logger l=Logger.getLogger(audio.getClass().getName());
    public static class Bndroid implements Audio {
        Bndroid() {}
        @Override public void play(Sound sound) {
            if(Audio.Instance.sound) if(callback!=null) callback.call(sound);
            else l.warning("callback is not set: "+sound);
        }
        public void setCallback(Callback<Sound> callback) {
            this.callback=callback;
        }
        public Callback<Sound> callback;
    }
}
interface Factory {
    abstract Audio create();
    static class Implementation implements Factory {
        private Implementation() {}
        @Override public Audio create() {
            // says linux, so look for something that says android!
            if(System.getProperty("os.name").contains("indows")) return new Windows();
            else return new Audio.Bndroid();
        }
        static Factory instance() {
            return factory;
        }
        private static Factory factory=new Implementation();
    }
}
class Windows implements Audio {
    Windows() {}
    static void playit(final Sound sound) {
        try {
            String filename=sound.name()+".wav";
            Clip clip=AudioSystem.getClip();
            AudioInputStream inputStream=AudioSystem.getAudioInputStream(new BufferedInputStream(Audio.class.getResourceAsStream(filename)));
            if(inputStream!=null) {
                clip.open(inputStream);
                l.info(filename+" is open.");
                FloatControl gainControl=(FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(+6.0f); // ?
                clip.start();
                // maybe do not wait?
                while(clip.getMicrosecondLength()!=clip.getMicrosecondPosition())
                    Thread.yield(); // wait
                // or at least don't wait here?
                Thread.sleep(500);
                clip.close();
            }
        } catch(Exception e) {
            l.warning("failed to play: "+sound);
        }
        l.info("exit playit");
    }
    private static class AudioCallable implements Callable<Void> {
        private AudioCallable(Sound sound) {
            this.sound=sound;
        }
        @Override public Void call() throws Exception {
            Thread.currentThread().setName(getClass().getName());
            playit(sound);
            l.info("exit call()");
            return null;
        }
        final Sound sound;
    }
    @Override public void play(final Sound sound) {
        if(Audio.Instance.sound) {
            l.info("starting audio thread for: "+sound);
            // pass in executor service
            executorService.submit(new AudioCallable(sound));
        }
    }
    public static void main(String[] args) throws InterruptedException {
        Audio.Instance.sound=true;
        for(Sound sound:Sound.values()) {
            Audio.audio.play(sound);
            Thread.sleep(1000);
        }
        Thread.sleep(10000);
        p("exit main");
        printThreads();
    }
    ExecutorService executorService=Executors.newSingleThreadExecutor();
}
