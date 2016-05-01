package com.tayek.tablet;
import static com.tayek.io.IO.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.*;
import com.tayek.*;
import com.tayek.io.*;
import com.tayek.tablet.Group.*;
import com.tayek.io.Audio.AudioObserver;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.io.*;
public class Controller {
    Controller(Group group,boolean other) throws UnknownHostException {
        this(group,other,System.in,System.out);
    }
    Controller(Group group,boolean other,InputStream in,PrintStream out) throws UnknownHostException {
        this.group=group;
        Iterator<String> i=group.keys().iterator();
        String id=i.next();
        if(other) id=i.next();
        Required required=group.required(id);
        String host=required.host;
        Integer service=other?group.required(id).service:null; // hack to get second tablet
        String tabletId=group.getTabletIdFromInetAddress(InetAddress.getByName(host),service);
        model=group.getModelClone();
        Tablet tablet=Tablet.factory.create2(tabletId,group,model);
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
                    audioObserver=new AudioObserver(model);
                    model.addObserver(audioObserver);
                } else {
                    model.deleteObserver(audioObserver);
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
                    commandLineView=new CommandLine(model);
                    model.addObserver(commandLineView);
                    p(out,"added command line view: "+commandLineView);
                } else {
                    model.deleteObserver(commandLineView);
                    p(out,"removed command line view: "+commandLineView);
                    commandLineView=null;
                }
                break;
            case 'H':
                if(tablet.isHeatbeatOn()) tablet.startHeatbeat();
                else tablet.stopHeartbeat();
                break;
            case 'l':
                if(IO.l.getLevel()==Level.OFF) LoggingHandler.setLevel(Level.ALL);
                else LoggingHandler.setLevel(Level.OFF);
                break;
            case 'L':
                LoggingHandler.toggleSockethandlers();
                break;
            case 'p':
                p(out,model.toString());
                break;
            case 'r':
                model.reset();
                break;
            case 's':
                boolean ok=((TabletImpl2)tablet).startListening();
                if(!ok) p(out,"badness");
                break;
            case 't':
                ((TabletImpl2)tablet).stopListening();
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
    public static void main(String[] arguments) throws UnknownHostException,InterruptedException,ExecutionException {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.OFF);
        String host=InetAddress.getLocalHost().getHostName();
        p("host: "+host);
        Group group=new Group("1",new Groups().groups.get("g2OnPc"),Model.mark1);
        new Controller(group,false).run();
    }
    protected final Group group;
    protected final Model model;
    protected final Tablet tablet;
    protected final InputStream in;
    protected final PrintStream out;
    protected SocketHandler socketHandler;
    private CommandLine commandLineView;
    private Observer audioObserver;
    public static final String lineSeparator=System.getProperty("line.separator");
}
