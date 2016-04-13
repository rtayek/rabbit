package com.tayek.io;
import static com.tayek.io.IO.*;
import static com.tayek.utilities.Utility.*;
import java.io.BufferedInputStream;
import java.util.concurrent.*;
import java.util.logging.*;
import javax.sound.sampled.*;
import com.tayek.utilities.Et;
public interface Audio {
    enum Sound {
        electronic_chime_kevangc_495939803,glass_ping_go445_1207030150,store_door_chime_mike_koenig_570742973;
    }
    void play(Sound sound);
    static class Instance {
        public static void main(String[] args) throws InterruptedException {
            LoggingHandler.init();
            Audio.Instance.sound=true;
            for(Sound sound:Sound.values()) {
                Et et=new Et();
                Audio.audio.play(sound);
                p("play took: "+et);
                //Thread.sleep(1_000);
            }
            Thread.sleep(10000);
            p("exit main");
            printThreads();
        }
        public static boolean sound=false;
    }
    Audio audio=Factory.Implementation.instance().create();
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
    private static void play_(final Sound sound) {
        try {
            Et et=new Et();
            String filename=sound.name()+".wav";
            Clip clip=AudioSystem.getClip();
            AudioInputStream inputStream=AudioSystem.getAudioInputStream(new BufferedInputStream(Audio.class.getResourceAsStream(filename)));
            if(inputStream!=null) {
                clip.open(inputStream);
                l.info(filename+" is open.");
                FloatControl gainControl=(FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(-25.0f); // ?
                clip.start();
                l.info("clip started at: "+et);
                // maybe do not wait?
                while(clip.getMicrosecondLength()!=clip.getMicrosecondPosition())
                    Thread.sleep(1); // wait
                // or at least don't wait here?
                //Thread.sleep(500);
                clip.close();
                l.info("clip done at: "+et);
            }
        } catch(Exception e) {
            l.warning("failed to play: "+sound);
        }
        l.info("exit playit");
    }
    private static class AudioCallable implements Callable<Void>,Runnable {
        private AudioCallable(Sound sound) {
            this.sound=sound;
        }
        @Override public void run() {
            Thread.currentThread().setName(getClass().getName());
            play_(sound);
            l.info("exit call()");
        }
        @Override public Void call() throws Exception {
            run();
            return null;
        }
        final Sound sound;
    }
    @Override public void play(final Sound sound) {
        if(Audio.Instance.sound) if(runOnSeparateThread) {
            l.info("starting audio thread for: "+sound);
            if(useFuture) executorService.submit((Callable<Void>)new AudioCallable(sound));
            else new Thread(new AudioCallable(sound)).start();
        } else play_(sound);
    }
    boolean runOnSeparateThread=true,useFuture=false;
    ExecutorService executorService=Executors.newSingleThreadExecutor();
}
