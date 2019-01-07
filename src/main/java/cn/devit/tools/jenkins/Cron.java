package cn.devit.tools.jenkins;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * <p>
 *
 * @author lxb
 */
public class Cron {


    File pwd;

    File memo;

    public Cron() throws CertificateException {
        pwd = new File(".");
    }

    public void run() throws Exception {
        readKeypair();

        memo = new File(pwd, "memo.txt");

        //read index
        //create directorys
        //create temp directory
//        Resources.rea
        //read memo
        //pull update site index stable and latest
        //pull plugin-json
        //pull updates folder
        //save new plugin.json and sign
        // download each plugin and run check sum, write memo
        //pull tools list
        // save new tools json
        // download each tools zip and save to local file
        // rewrite to use local nexus repository
        // or put zip on nexus.
    }

    Signer signer;

    public void readKeypair() throws CertificateException, IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        URL pub = getClass().getResource("/cert/ca.pem");

    }


}
