package com.tayek.io;
import java.net.*;
import java.util.Enumeration;
import static com.tayek.io.IO.*;
public class Nics {
    public static void main(String[] args) {
        try {
            InetAddress localhost=InetAddress.getLocalHost();
            p(" IP Addr: "+localhost.getHostAddress());
            // Just in case this host has multiple IP addresses....
            InetAddress[] allMyIps=InetAddress.getAllByName(localhost.getCanonicalHostName());
            p("cannonical: "+localhost.getCanonicalHostName());
            if(allMyIps!=null&&allMyIps.length>1) {
                p(" Full list of IP addresses:");
                for(int i=0;i<allMyIps.length;i++) {
                    p("    '"+allMyIps[i]+"'");
                }
            }
        } catch(UnknownHostException e) {
            p(" (error retrieving server host name)");
        }
        try {
            p("Full list of Network Interfaces:");
            for(Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();en.hasMoreElements();) {
                NetworkInterface intf=en.nextElement();
                p("    "+intf.getName()+" "+intf.getDisplayName());
                for(Enumeration<InetAddress> enumIpAddr=intf.getInetAddresses();enumIpAddr.hasMoreElements();) {
                    p("        "+enumIpAddr.nextElement().toString());
                }
            }
        } catch(SocketException e) {
            p(" (error retrieving network interface list)");
        }
    }
}
