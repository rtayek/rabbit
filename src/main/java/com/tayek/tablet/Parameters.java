package com.tayek.tablet;
import java.io.*;
import java.net.URL;
import java.util.*;
import static com.tayek.io.IO.*;
public enum Parameters { // properties
    tabletId(""),host("");
    Parameters(Object defaultValue) {
        this.defaultValue=currentValue=defaultValue;
    }
    public Object currentValue() {
        return currentValue;
    }
    public void setCurrentValue(Object currentValue) {
        this.currentValue=currentValue;
    }
    @Override public String toString() {
        return name()+"="+currentValue+"("+defaultValue+")";
    }
    public static void loadPropertiesFile(Properties properties,String filename) {
        URL url=Parameters.class.getResource(filename);
        p("from url: "+url);
        if(url!=null) try {
            InputStream in=url.openStream();
            if(in!=null) {
                l.config("before load: properties were: "+properties);
                properties.load(in);
                l.config("properties loaded from url: "+url);
                l.config("properties loaded were: "+properties);
            } else l.warning("properties stream is null!");
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        else l.warning("url is null for filename: "+filename);
    }
    public void loadPropertiesFile(Properties properties) {
        loadPropertiesFile(properties,Parameters.propertiesFilename);
    }
    public static void writePropertiesFile(Properties properties,String filename) {
        try {
            URL url=Parameters.class.getResource(filename);
            p(filename+" has url: "+url);
            String path=url.getPath();
            p("path: "+path);
            File file=new File(path);
            p("storing into: "+file);
            properties.store(new FileOutputStream(file),"initial");
        } catch(FileNotFoundException e) {
            l.warning("properties"+" "+"caught: "+e+" property file was not written!");
        } catch(IOException e) {
            l.warning("properties"+" "+"caught: "+e+" property file was not written!");
        }
    }
    public static void main(String[] arguments) throws IOException {
        p("default propeties: "+defaultProperties);
        File file=new File(".");
        p(".:"+file.getAbsolutePath());
        p(".:"+file.getCanonicalPath());
        Properties properties=new Properties();
        properties.setProperty("foo","bar "+System.currentTimeMillis());
        writePropertiesFile(properties,"test.properties");
        p("stored: "+properties);
        Properties properties2=new Properties();
        loadPropertiesFile(properties2,"test.properties");
        p("loaded: "+properties2);
    }
    public final Object defaultValue;
    private Object currentValue;
    public static final String propertiesFilename="tablet.properties";
    public static final Properties defaultProperties=new Properties();
    static {
        for(Parameters property:Parameters.values())
            defaultProperties.put(property.name(),property.defaultValue.toString());
        if(false) writePropertiesFile(defaultProperties,propertiesFilename);
        // only once to get the file created in the right place
    };
}
