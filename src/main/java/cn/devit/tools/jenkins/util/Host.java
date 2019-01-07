package cn.devit.tools.jenkins.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Host {

    public static InetAddress getIpv4Address() {
        Enumeration<NetworkInterface> eths;
        try {
            eths = NetworkInterface.getNetworkInterfaces();
            while (eths.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) eths.nextElement();
                if (n.isLoopback()) {
                    continue;
                }
                if (n.isUp()) {
                    //utun0 utun0 is For "Back to My Mac"
//                awdl0 AWDL (Apple Wireless Direct Link) 
//                en0
//                    System.out.println(n.getName());
                    Enumeration<InetAddress> ee = n.getInetAddresses();
                    while (ee.hasMoreElements()) {
                        InetAddress i = ee.nextElement();
                        if (i instanceof Inet4Address) {
                            return i;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        //work on linux
//        try (final DatagramSocket socket = new DatagramSocket()) {
//            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
//            String ip = socket.getLocalAddress().getHostAddress();
//            System.out.println(ip);
//        }
        return null;
    }
}
