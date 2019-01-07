package cn.devit.tools.jenkins;

import org.junit.Test;

import cn.devit.tools.jenkins.util.Host;

import java.io.File;
import java.net.Inet4Address;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * <p>
 *
 * @author lxb
 */
public class Study {

    @Test
    public void hostname() throws Exception {
        System.out.println(Inet4Address.getLocalHost().getHostAddress());
        System.out.println(Host.getIpv4Address());
    }
}
