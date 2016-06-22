package com.tayek.tablet;
import java.io.IOException;
import java.net.*;
import java.util.*;
import com.esotericsoftware.kryonet.*;
import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive;
import com.tayek.*;
import com.tayek.Tablet.Factory.FactoryImpl.TabletABC;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.utilities.Single;
import static com.tayek.io.IO.*;
public class TabletKryoImpl extends TabletABC {
    class MyListener extends Listener {
        public void received(Connection connection,Object object) {
            if(object instanceof KeepAlive) ;
            else if(object instanceof String) {
                p("server received: "+object+" from: "+object);
                Message message=messageFactory.from((String)object);
                model().receive(message);
            } else pl("server received non-string object: "+object);
            received++;
        }
        int received;
    }
    public TabletKryoImpl(Group group,String tabletId,Model model) {
        super(group,tabletId,model,group.required(tabletId).histories());
        messageFactory=Message.instance.create(required.host,required.service,new Single<Integer>(0));
        int n=group.keys().size();
        model().histories=histories();
    }
    @Override public com.tayek.tablet.Message.Factory messageFactory() {
        return messageFactory;
    }
    @Override public void broadcast(Object message) {
        for(Client client:idToClient.values())
            if(client!=null) {
                p(message+" sent to: "+client);
                client.sendTCP(message.toString());
            }
    }
    @Override public String report(String id) {
        // TODO Auto-generated method stub
        return null;
    }
    private void createClients() {
        for(final String key:group.keys()) {
            final Client client=new Client();
            client.start();
            p("update thread: "+client.getUpdateThread());
            try {
                Thread.sleep(500);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            final Required required=group.required(key);
            new Thread(new Runnable() {
                @Override public void run() {
                    p(key+": connect thread: "+Thread.currentThread());
                    try {
                        client.connect(5000,required.host,required.service,required.service+1000);
                        client.addListener(new Listener() {
                            public void received(Connection connection,Object object) {
                                if(object instanceof KeepAlive) ;
                                else if(object instanceof String) System.out.println("client received: "+object);
                                else System.out.println("client received: "+object);
                            }
                        });
                        idToClient.put(key,client);
                    } catch(IOException e) {
                        //e.printStackTrace();
                        p(key+": caught: "+e);
                        idToClient.put(key,null);
                    }
                }
            }).start();
        }
    }
    @Override public boolean startServer() {
        server.start();
        try {
            InetAddress inetAddress=InetAddress.getByName(required.host);
            InetSocketAddress tcp=new InetSocketAddress(inetAddress,required.service);
            InetSocketAddress udp=new InetSocketAddress(inetAddress,required.service+1_000);
            pl("server is binding to: "+tcp+", "+udp);
            server.bind(tcp,udp);
            pl("server is bound to: "+tcp+", "+udp);
            server.addListener(myListener);
            isServerRunning=true;
            createClients();
        } catch(IOException e) {
            e.printStackTrace();
            System.out.println("caught: "+e);
            isServerRunning=false;
        }
        return isServerRunning;
    }
    @Override public void stopServer() {
        server.stop();
        isServerRunning=false;
    }
    @Override public boolean isServerRunning() {
        return isServerRunning;
    }
    public static void main(String[] args) {
        // TODO Auto-generated method stub
    }
    public final Message.Factory messageFactory;
    final Server server=new Server();
    final MyListener myListener=new MyListener();
    boolean isServerRunning;
    private final Map<String,Client> idToClient=new TreeMap<>();
}
