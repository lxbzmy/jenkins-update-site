package cn.devit.tools.jenkins;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import cn.devit.tools.jenkins.util.Host;
import cn.devit.tools.jenkins.util.KeyTool;

@SpringBootApplication
@Configuration
//@EnableConfigurationProperties
//@EnableScheduling
//@EnableAsync
public class Application extends WebMvcConfigurerAdapter
        implements ServletContextInitializer {

    static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Override
    public void onStartup(ServletContext servletContext)
            throws ServletException {
    }

    //    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        try {
//            registry.addResourceHandler("/**")
//            .addResourceLocations(new File("./target/cache2/").toURI().toURL().toString());
//        } catch (MalformedURLException e) {
//            throw new RuntimeException(e);
//        }
//    }
    @Bean
    public Config staticConfig() {
        File currentDirectory = new File(".");

        File pwd;

//        File target = new File("target");
//        if (target.exists()) {
//            pwd = target;
//        } else {
        pwd = new File(currentDirectory, "jenkins-update-site");
//        }
        pwd.mkdir();
        File cache = new File(pwd, "cache");
        cache.mkdir();
        Config config = new Config();
        config.setWorkingDir(pwd);
        config.setCacheDir(cache);

        config.setTools(JsonFileOutput.getToolsDownload());

        config.setHost(Host.getIpv4Address().toString());
        return config;
    }

    public void chekcHostName() {

    }

    @Bean
    public Signer jsonSigner() throws NoSuchAlgorithmException,
            InvalidKeySpecException, IOException {

        logger.info("读取x509证书和私钥。");
        File certificateDir = staticConfig().getCertificateDir();
        if (!certificateDir.exists()) {
            certificateDir.mkdirs();
            logger.info("创建证书存储文件夹：{}", certificateDir);
        }
        File x509 = new File(certificateDir, "jenkins-update-site-cert.pem");
        File keyFile = new File(certificateDir, "jenkins-update-site-key.pem");
        if (!x509.exists()) {
            logger.info("证书缺失，生成新的自签名证书");
            KeyTool.createSelfCertificateAndPrivateKey(x509, keyFile);
            logger.info("保存文件{}", x509);
            logger.warn("你需要将{}存放在${JENKINS_HOME}/update-center-rootCAs/文件夹里面",
                    x509);
            logger.info("保存文件{}", keyFile);
        }
        if (!keyFile.exists()) {
            logger.info("私钥缺失，生成新的自签名证书");
            KeyTool.createSelfCertificateAndPrivateKey(x509, keyFile);
            logger.info("保存文件{}", x509);
            logger.warn("你需要将{}存放在${JENKINS_HOME}/update-center-rootCAs/文件夹里面",
                    x509);
            logger.info("保存文件{}", keyFile);
        }

        Collection<X509Certificate> certs = KeyTool.readCertificatePem(x509);
        PrivateKey key = KeyTool.readPrivateKey(keyFile.toURI().toURL());
        KeyPair keyPair = KeyTool.findKeyPair(certs, key);

        Signer signer = new Signer();
        signer.setCertificates(certs);
        signer.setKeyPair(keyPair);
        return signer;
    }

    public static void main(String[] args) {
//        SpringApplication.run(Application.class, args);
        File pwd = new File(".").getAbsoluteFile();
        logger.info("当前目录：{}", pwd);

        if (args.length > 0) {
            for (String item : args) {
                //TODO get yaml earlier.
                if ("pull".equals(item)) {
                    logger.info("downloading updates.jenkins-ci.org meta.");
                    File cacheDir = new Application().staticConfig()
                            .getCacheDir();
                    AllVersionParse bean = new AllVersionParse();
                    bean.parseAndDownload(cacheDir, Config.jenkins_update_url);
                    logger.info("finish.");
                    return;
                }
                if ("update".equals(item)) {
                    logger.info("update plugins.txt");
                    new CollectPluginsList().collectAndSaveFile(
                            new Application().staticConfig());
                    new CollectToolsList()
                            .collect(new Application().staticConfig());
                    logger.info("finish.");
                    return;
                }
                if ("clean".equals(item)) {
                    logger.info("cleaning out date files.");
                    new CollectPluginsList().discardOutDatePlugins(
                            new Application().staticConfig());
                    logger.info("finish.");
                }
                if ("server".equals(item)) {
                    logger.info("web server模式");
                    SpringApplication.run(Application.class, args);
                    logger.info("finish.");
                    return;
                }
            }
        }
        System.out.println("参数：\n" + "pull\t从updates.jenkins-ci.org获取插件列表\n"
                + "update\t生成下载清单\n" + "clean\t清理过期插件"
                + "server\tweb server模式\n" + "");
    }

}
