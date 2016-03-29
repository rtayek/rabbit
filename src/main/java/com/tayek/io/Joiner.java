package com.tayek.io;
import static com.tayek.utilities.Utility.*;
import java.io.IOException;
import java.util.Arrays;
import static com.tayek.io.IO.*;
public class Joiner {
    static void join() {
        Thread[] threads=getThreads();
        p("threads: "+Arrays.asList(threads));
    }
    public static void main(String[] arguments) {
        join();
    }

}
