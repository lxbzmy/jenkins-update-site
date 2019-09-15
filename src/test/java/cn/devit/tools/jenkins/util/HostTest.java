package cn.devit.tools.jenkins.util;

import org.junit.Test;

import java.net.InetAddress;

import static org.junit.Assert.*;

/**
 * <p>
 *
 * @author lxb
 */
public class HostTest {

  @Test
  public void name() {
    final InetAddress address = Host.getIpv4Address();
    System.out.println(address.toString());
    String address2 = Host.getIpv4Address().toString();
    if(address2.startsWith("/")){
      address2 = address2.substring(1);
    }
    System.out.println(address2);
  }
}