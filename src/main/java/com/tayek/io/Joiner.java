package com.tayek.io;
import static com.tayek.utilities.Utility.*;
import java.io.IOException;
import java.util.Arrays;
import static com.tayek.io.IO.*;
public class Joiner implements Runnable {
    public Joiner(int n) {
        this.n=n;
    }
    @Override public void run() {
        while(Thread.activeCount()>n) {
            Thread[] threads=getThreads();
            p("threads:");
            for(Thread thread:threads)
                if(thread!=null) {
                    p("thread: "+thread+", threadinterrupted: "+thread.isInterrupted()+", alive: "+thread.isAlive());
                }
            try {
                Thread.sleep(1_000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] arguments) {
        new Joiner(1).run();
    }
    final int n;
}
