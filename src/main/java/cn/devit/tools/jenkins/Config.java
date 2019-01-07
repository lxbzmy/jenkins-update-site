package cn.devit.tools.jenkins;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class Config {

    /** <code>http://updates.jenkins-ci.org/</code> */
    public static final URL jenkins_update_url = url(
            "http://updates.jenkins-ci.org/");

    private static URL url(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

//  static 

    /**
     * servlet doc root
     */
    File workingDir;

    /**
     * save download files from updates.jenkins-ci.org to here
     */
    File cacheDir;

    List<String> tools;

    /** used at download url, must be a public accessible host */
    String host;

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setTools(List<String> tools) {
        this.tools = tools;
    }

    public List<String> getTools() {
        return tools;
    }

    /**
     * Mirrored plugins hpi file stored here. path same as official url.
     */
    public File getDownloadDir() {
        final File download = new File(workingDir, "download");
        download.mkdir();
        return download;
    }

    public File getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    public File getCertificateDir() {
        return new File("certs");
    }

}
