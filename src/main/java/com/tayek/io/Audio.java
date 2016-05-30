package com.tayek.io;
import static com.tayek.io.IO.*;
import static com.tayek.utilities.Utility.*;
import java.io.BufferedInputStream;
import java.util.*;
import java.util.Observable;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.*;
import javax.sound.sampled.*;
import com.tayek.io.Audio.Sound;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.utilities.Et;
import javafx.beans.*;
public interface Audio {
    enum Sound {
        electronic_chime_kevangc_495939803,glass_ping_go445_1207030150,store_door_chime_mike_koenig_570742973;
    }
    void play(Sound sound);
    class AudioObserver implements Observer {
        public AudioObserver(Model model) {
            this.model=model;
        }
        public synchronized boolean isChimimg() {
            return timer!=null;
        }
        public synchronized void startChimer() {
            if(!isChimimg()) {
                timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override public void run() {
                        p("start chimer.");
                        Audio.audio.play(Sound.electronic_chime_kevangc_495939803);
                    }
                },0,10_000);
            }
        }
        public synchronized void stopChimer() {
            if(isChimimg()) {
                p("stop chimer.");
                timer.cancel();
                timer=null;
            }
        }
        @Override public void update(Observable observable,Object hint) {
            l.fine("hint: "+hint);
            if(observable instanceof Model) if(this.model.equals(observable)) {
                if(hint instanceof Sound) Audio.audio.play((Sound)hint);
                if(model.areAnyButtonsOn()) startChimer();
                else stopChimer();
            } else l.warning("not our model!");
            else l.warning("not a model!");
        }
        private final Model model;
        volatile Timer timer;
    }
    class Instance {
        public static void main(String[] args) throws InterruptedException {
            LoggingHandler.init();
            LoggingHandler.setLevel(Level.INFO);
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
        public static boolean sound=true;
    }
    interface Factory {
        Audio create();
        class FactoryImpl implements Factory {
            private FactoryImpl() {}
            @Override public Audio create() {
                return isAndroid()?new AndroidAudio():new WindowsAudio();
            }
            public static class AndroidAudio implements Audio {
                AndroidAudio() {}
                @Override public void play(Sound sound) {
                    if(Audio.Instance.sound) if(callback!=null) callback.call(sound);
                    else l.warning("callback is not set: "+sound);
                }
                public void setCallback(Callback<Sound> callback) {
                    //Consumer<String> c;
                    this.callback=callback;
                }
                public Callback<Sound> callback;
            }
            private static class WindowsAudio implements Audio {
                WindowsAudio() {}
                private static void play_(final Sound sound) {
                    try {
                        Et et=new Et();
                        String filename=sound.name()+".wav";
                        Clip clip=AudioSystem.getClip();
                        AudioInputStream inputStream=AudioSystem.getAudioInputStream(new BufferedInputStream(Audio.class.getResourceAsStream(filename)));
                        if(inputStream!=null) {
                            l.info("stream is not null.");
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
                        } else l.warning("input stream is null!");
                    } catch(Exception e) {
                        e.printStackTrace();
                        l.warning("caught: "+e);
                        l.warning("failed to play: "+sound);
                    }
                    l.info("exit playit");
                }
                private class AudioCallable implements Callable<Void>,Runnable {
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
                        new Thread(new AudioCallable(sound)).start();
                    } else play_(sound);
                }
                boolean runOnSeparateThread=true;
            }
        }
    }
    Factory factory=new Factory.FactoryImpl();
    //Audio audio=Factory.FactoryImpl.instance().create();
    Audio audio=factory.create();
}
