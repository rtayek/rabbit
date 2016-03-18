package com.tayek.tablet;
import static com.tayek.tablet.io.IO.*;
import static com.tayek.utilities.Utility.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.*;
import com.tayek.tablet.Group.Groups;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.io.*;
import javafx.scene.control.Toggle;
public class Controller {
    Controller(Tablet tablet) {
        this(tablet,System.in,System.out);
    }
    Controller(Tablet tablet,InputStream in,PrintStream out) {
        this.tablet=tablet;
        this.in=in;
        this.out=out;
    }
    protected void help() {
        p(out,"help:");
        p(out,"a add/remove audio observer");
        p(out,"b <buttonId> - click on button");
        p(out,"c - add/remove a command line view");
        p(out,"H - toggle heartbeat");
        p(out,"h - help");
        p(out,"l - toggle logging");
        p(out,"L - toggle logging socket handler");
        p(out,"p - print view");
        p(out,"q - quit");
        p(out,"r - reset");
        p(out,"s - start client");
        p(out,"t - stop client");
    }
    private String[] splitNext(String command,int i) {
        while(command.charAt(i)==' ')
            i++;
        String[] tokens=command.substring(i).split(" ");
        return tokens;
    }
    protected boolean process(String command) {
        if(command.length()==0) return true;
        String[] tokens=null;
        switch(command.charAt(0)) {
            case 'h':
                help();
                break;
            case 'a':
                if(audioObserver==null) {
                    audioObserver=new AudioObserver(tablet.model);
                    tablet.model.addObserver(audioObserver);
                } else {
                    tablet.model.deleteObserver(audioObserver);
                    audioObserver=null;
                }
                break;
            case 'b':
                if(command.charAt(1)==' ') {
                    tokens=splitNext(command,2);
                    if(tokens.length==1) try {
                        int buttonId=Integer.valueOf(tokens[0]);
                        tablet.click(buttonId);
                    } catch(Exception e) {
                        p(out,"controller split caught: '"+e+"'");
                        p(out,"syntax error: "+command);
                    }
                    else p(out,"too many tokens!");
                } else p(out,"syntax error: "+command);
                break;
            case 'o': // send start form foreign group
                // tablet.send(Message.dummy,0);
                break;
            case 'c':
                if(commandLineView==null) {
                    commandLineView=new View.CommandLine(tablet.model);
                    tablet.model.addObserver(commandLineView);
                    p(out,"added command line view: "+commandLineView);
                } else {
                    tablet.model.deleteObserver(commandLineView);
                    p(out,"removed command line view: "+commandLineView);
                    commandLineView=null;
                }
                break;
            case 'H':
                if(tablet.heartbeatTimer!=null) Tablet.startHeatbeat(tablet);
                else tablet.stopHeartbeat();
                break;
            case 'l':
                if(IO.staticLogger.getLevel()==Level.OFF) LoggingHandler.setLevel(Level.ALL);
                else LoggingHandler.setLevel(Level.OFF);
                break;
            case 'L': 
                LoggingHandler.toggleSockethandlers();
                break;
            case 'p':
                p(out,tablet.model.toString());
                break;
            case 'r':
                tablet.model.reset();
                break;
            case 's':
                boolean ok=tablet.startListening();
                if(!ok) p(out,"badness");
                break;
            case 't':
                tablet.stopListening();
                break;
            case 'q':
                return false;
            default:
                p(out,"unimplemented: "+command.charAt(0));
                help();
                break;
        }
        return true;
    }
    void run() {
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(in));
        String string=null;
        help();
        prompt();
        try {
            while((string=bufferedReader.readLine())!=null) {
                if(!process(string)) {
                    p(out,"quitting.");
                    return;
                }
                prompt();
            }
        } catch(IOException e) {
            p(out,"controller readln caught: '"+e+"'");
            p(out,"quitting.");
            return;
        }
        p(out,"end of file.");
    }
    void prompt() {
        out.print(lineSeparator+">");
        out.flush();
    }
    static Tablet initialize(String[] arguments) throws UnknownHostException,InterruptedException,ExecutionException {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.OFF);
        String host=InetAddress.getLocalHost().getHostName();
        p("host: "+host);
        Group group=new Group(1,new Groups().groups.get("g2"),Model.mark1,false);
        Integer service=arguments.length==0?null:group.info(5).service; // hack to get second tablet
        Tablet tablet=group.getTablet(InetAddress.getByName(host),service);
        p("tablet: "+tablet);
        return tablet;
    }
    public static void main(String[] arguments) throws UnknownHostException,InterruptedException,ExecutionException {
        Tablet tablet=initialize(arguments);
        new Controller(tablet).run();
    }
    protected final Tablet tablet;
    protected final InputStream in;
    protected final PrintStream out;
    protected SocketHandler socketHandler;
    private View.CommandLine commandLineView;
    private Observer audioObserver;
    public static final String lineSeparator=System.getProperty("line.separator");
}
