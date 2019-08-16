/*
 * Copyright 2017-2019 lxb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
