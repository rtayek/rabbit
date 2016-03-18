package com.tayek.tablet.io;
import static com.tayek.utilities.Utility.*;
import java.util.logging.Logger;
import com.tayek.tablet.io.IO.Callback;
public interface Toaster {
    void toast(String string);
    Toaster toaster=Factory_.Implementation.instance().create();
    static class Android_ implements Toaster {
        Android_() {}
        @Override public void toast(String string) {
            if(callback!=null) callback.call(string);
            else {
                l.warning("callback is not set: "+string);
                p("set callback!");
            }
            p(string);
        }
        public void setCallback(Callback<String> callback) {
            this.callback=callback;
        }
        public Callback<String> callback;
    }
    Logger l=Logger.getLogger(Toaster.class.getName());
}
interface Factory_ {
    abstract Toaster create();
    static class Implementation implements Factory_ {
        private Implementation() {}
        @Override public Toaster create() {
            // says linux, so look for something that says android!
            if(System.getProperty("os.name").contains("indows")) return new Windows_();
            else return new Toaster.Android_();
        }
        static Factory_ instance() {
            return factory;
        }
        private static Factory_ factory=new Implementation();
    }
}
class Windows_ implements Toaster {
    Windows_() {}
    @Override public void toast(String string) {
        //p(string);
    }
    public static void main(String[] args) throws InterruptedException {
        Toaster toaster=Toaster.toaster;
        toaster.toast("hello");
    }
}
