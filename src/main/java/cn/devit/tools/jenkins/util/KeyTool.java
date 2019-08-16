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

import static java.security.Security.addProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

/**
 * cert and key for jenkins.
 * <p>
 *
 * @author lxb
 */
@SuppressWarnings("restriction")
public class KeyTool {
    
    static {
        addProvider(new BouncyCastleProvider());
    }

    public static class CertAndKeyPair {
        public X509Certificate x509Certificate;
        public KeyPair keyPair;
    }

    /**
     * Create a self signed certificate
     */
    public static CertAndKeyPair createSelfSignedKeyPair() {
        //keytool -genkeypair -alias rsakey -keyalg rsa -storepass passphrase -keystore mytestkeys.jks -storetype JKS -dname "CN=ROOT"
        try {
            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA1WithRSA",
                    null);
            keyGen.generate(2048);

            //Generate self signed certificate
            X509Certificate cert = keyGen.getSelfCertificate(
                    new X500Name("CN=jenkins-update-site-mirror,OU=devit"),
                    (long) 3600 * 24 * 365 * 10);

            CertAndKeyPair bean = new CertAndKeyPair();
            bean.x509Certificate = cert;
            bean.keyPair = new KeyPair(keyGen.getPublicKey(),
                    keyGen.getPrivateKey());
            return bean;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static CertAndKeyPair createSelfCertificateAndPrivateKey(File cert,
            File privateKey) throws IOException {
        CertAndKeyPair certAndKey = createSelfSignedKeyPair();
        saveCertToFile(certAndKey.x509Certificate, cert);
        savePrivateKeyToFile(certAndKey.keyPair.getPrivate(), privateKey);
        return certAndKey;
    }

    static void saveCertToFile(X509Certificate cert, File file)
            throws IOException {
        try (PEMWriter pemWriter = new PEMWriter(new FileWriter(file))) {
            pemWriter.writeObject(cert);
        }
    }

    static void savePrivateKeyToFile(PrivateKey key, File file)
            throws IOException {
        try (PEMWriter pemWriter = new PEMWriter(new FileWriter(file))) {
            pemWriter.writeObject(key);
        }
    }

    @SuppressWarnings("unchecked")
    public static Collection<X509Certificate> readCertificatePem(URL file) {
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X509");
            Collection<X509Certificate> certs = null;
            certs = (Collection<X509Certificate>) cf
                    .generateCertificates(file.openStream());
            for (X509Certificate item : certs) {
                item.checkValidity();
            }
            return certs;
        } catch (CertificateException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    @SuppressWarnings("unchecked")
    public static Collection<X509Certificate> readCertificatePem(File file) {
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X509");
            Collection<X509Certificate> certs = null;
            certs = (Collection<X509Certificate>) cf
                    .generateCertificates(new FileInputStream(file));
            for (X509Certificate item : certs) {
                item.checkValidity();
            }
            return certs;
        } catch (CertificateException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static PrivateKey readPrivateKey(URL url) throws IOException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        final List<String> lines = Resources.readLines(url,
                Charsets.ISO_8859_1);
        String firstLine = "";
        if (lines.get(0).contains("-----BEGIN PRIVATE KEY-----")) {
            firstLine = lines.get(0);
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains("-----END PRIVATE KEY-----")) {
                    break;
                }
                sb.append(line);
            }
            byte[] pkcs8KeyByte = Base64.getDecoder().decode(sb.toString());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            KeySpec ks = new PKCS8EncodedKeySpec(pkcs8KeyByte);
            return keyFactory.generatePrivate(ks);
        } else {
            try (PEMReader reader = new PEMReader(
                    new InputStreamReader(url.openStream()))) {
                Object guess = reader.readObject();
                if (guess instanceof KeyPair) {
                    return ((KeyPair) guess).getPrivate();
                }
            }
            throw new IllegalArgumentException(
                    "not a private key pem file, expect: -----BEGIN PRIVATE KEY-----,but was: "
                            + firstLine);
        }
    }

    public static KeyPair findKeyPair(Collection<X509Certificate> certs,
            PrivateKey privateKey) {
        List<X509Certificate> list = new ArrayList<>();
        list.addAll(certs);
        if (certs.size() == 1) {
            return new KeyPair(list.get(0).getPublicKey(), privateKey);
        } else {
            //TODO
        }

        throw new RuntimeException("Can not find matching key pair.");
    }
}
