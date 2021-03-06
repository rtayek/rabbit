package com.tayek.io;
import static com.tayek.io.IO.*;
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
}
interface Factory_ {
    abstract Toaster create();
    static class Implementation implements Factory_ {
        private Implementation() {}
        @Override public Toaster create() {
            return isAndroid()?new Toaster.Android_():new WindowsToaster();
        }
        static Factory_ instance() {
            return factory;
        }
        private static Factory_ factory=new Implementation();
    }
}
class WindowsToaster implements Toaster {
    WindowsToaster() {}
    @Override public void toast(String string) {
        //p(string);
    }
    public static void main(String[] args) throws InterruptedException {
        Toaster toaster=Toaster.toaster;
        toaster.toast("hello");
    }
}
