package cn.devit.tools.jenkins;

import static org.junit.Assert.assertEquals;

import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

import org.junit.Test;

import com.google.common.net.InetAddresses;

import cn.devit.tools.jenkins.util.Host;

/**
 * <p>
 *
 * @author lxb
 */
public class UrlReplaceTest {

    @Test
    public void noSlash() throws MalformedURLException {
        final URL url = new URL("http://updates.jenkins-ci.org");
        assertEquals(url.toString(), "http://updates.jenkins-ci.org");
        assertEquals("", url.getPath());
    }

    @Test
    public void endWithSlash() throws MalformedURLException {
        final URL url = new URL("http://updates.jenkins-ci.org/");
        assertEquals(url.toString(), "http://updates.jenkins-ci.org/");
    }

}
