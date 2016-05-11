package com.tayek.io;
import static com.tayek.io.IO.l;
import java.net.URL;
import java.util.*;
import static com.tayek.io.IO.*;
import com.tayek.tablet.Parameters;
public interface Prefs {
    String get(String key);
    void put(String key,String value);
    Map<String,?> map();
    void clear();
    interface Factory {
        Prefs create();
        class FactoryImpl implements Factory {
            private FactoryImpl() {}
            @Override public Prefs create() {
                return isAndroid()?new AndroidPrefs():new WindowsPrefs();
            }
            private abstract static class PrefsABC implements Prefs {
                @Override public void clear() {
                    for(String x:map().keySet())
                        put(x,"");
                }
            }
            public static class AndroidPrefs extends PrefsABC {
                public void setDelegate(Prefs prefs) {
                    this.prefs=prefs;
                }
                @Override public String get(String key) {
                    return prefs.get(key);
                }
                @Override public void put(String key,String value) {
                    prefs.put(key,value);
                }
                @Override public Map<String,?> map() {
                    return prefs.map();
                }
                @Override public String toString() {
                    return prefs.toString();
                }
                private Prefs prefs;
            }
            private static class WindowsPrefs extends PrefsABC {
                WindowsPrefs() {
                    URL url=Parameters.class.getResource(filename);
                    if(url!=null) {
                        Parameters.loadPropertiesFile(properties,filename);
                    } else {
                        p("creating: "+filename);
                        Parameters.writePropertiesFile(properties,filename);
                    }
                }
                @Override public String get(String key) {
                    return properties.getProperty(key);
                }
                @Override public void put(String key,String value) {
                    properties.setProperty(key,value);
                    Parameters.writePropertiesFile(properties,filename);
                }
                @Override public Map<String,?> map() {
                    Map<String,Object> map=new TreeMap<>();
                    for(Map.Entry<Object,Object> entry:properties.entrySet())
                        if(entry.getKey() instanceof String) map.put((String)entry.getKey(),entry.getValue());
                    return map;
                }
                @Override public String toString() {
                    return filename+":"+properties;
                }
                private Properties properties=new Properties();
                final String filename=Parameters.propertiesFilename;
            }
        }
    }
    Factory factory=new Factory.FactoryImpl();
}
