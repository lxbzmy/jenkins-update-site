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
package cn.devit.tools.jenkins;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jvnet.hudson.crypto.SignatureOutputStream;

import java.io.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Collection;

import static java.security.Security.addProvider;

/**
 * Modified version of https://github.com/jenkins-infra/update-center2/blob/master/src/main/java/org/jvnet/hudson/update_center/Signer.java
 * license MIT
 */
public class Signer {

    /**
     * 证书链
     */
    Collection<X509Certificate> certificates;

    /**
     * 签名用的秘钥对
     */
    KeyPair keyPair;
    // debug option. spits out the canonical update center file used to compute the signature
    public File canonical = null;

    public void setCertificates(Collection<X509Certificate> certificates) {
        this.certificates = certificates;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    /**
     * Generates a canonicalized JSON format of the given object, and put the signature in it.
     * Because it mutates the signed object itself, validating the signature needs a bit of work,
     * but this enables a signature to be added transparently.
     *
     * @return The same value passed as the argument so that the method can be used like a filter.
     */
    public void sign(JSONObject o) throws GeneralSecurityException, IOException {

        o.remove("signature");

        JSONObject sign = new JSONObject();

        PrivateKey key = keyPair.getPrivate();

        // first, backward compatible signature for <1.433 Jenkins that forgets to flush the stream.
        // we generate this in the original names that those Jenkins understands.
        SignatureGenerator sg = new SignatureGenerator(keyPair.getPublic(), key);
        o.writeCanonical(new OutputStreamWriter(sg.getOut(), "UTF-8"));
        sg.addRecord(sign, "");

        // then the correct signature, into names that don't collide.
        OutputStream raw = new NullOutputStream();
        if (canonical != null) {
            raw = new FileOutputStream(canonical);
        }
        sg = new SignatureGenerator(keyPair.getPublic(), key);
        try (OutputStreamWriter osw = new OutputStreamWriter(new TeeOutputStream(sg.getOut(), raw), "UTF-8")) {
            o.writeCanonical(osw);
        }
        sg.addRecord(sign, "correct_");

        // and certificate chain
        JSONArray a = new JSONArray();
        for (X509Certificate cert : certificates)
            // the first one is the signer, and the rest is the chain to a root CA.
            a.add(new String(Base64.encodeBase64(cert.getEncoded()), "UTF-8"));
        sign.put("certificates", a);

        o.put("signature", sign);

    }

    /**
     * Generates a digest and signature. Can be only used once, and then it needs to be thrown away.
     */
    static class SignatureGenerator {
        private final MessageDigest sha1;
        private final Signature sha1sig;
        private final MessageDigest sha512;
        private final Signature sha512sig;
        private final TeeOutputStream out;
        private final Signature verifier1;
        private final Signature verifier512;

        SignatureGenerator(PublicKey publicKey, PrivateKey key) throws GeneralSecurityException, IOException {
            // this is for computing a digest
            sha1 = DigestUtils.getSha1Digest();
            sha512 = DigestUtils.getSha512Digest();
            DigestOutputStream dos1 = new DigestOutputStream(new NullOutputStream(), sha1);
            DigestOutputStream dos512 = new DigestOutputStream(new NullOutputStream(), sha512);

            // this is for computing a signature
            sha1sig = Signature.getInstance("SHA1withRSA");
            sha1sig.initSign(key);
            SignatureOutputStream sos1 = new SignatureOutputStream(sha1sig);

            sha512sig = Signature.getInstance("SHA512withRSA");
            sha512sig.initSign(key);
            SignatureOutputStream sos512 = new SignatureOutputStream(sha512sig);

            // this is for verifying that signature validates
            verifier1 = Signature.getInstance("SHA1withRSA");
            verifier1.initVerify(publicKey);
            SignatureOutputStream vos1 = new SignatureOutputStream(verifier1);

            verifier512 = Signature.getInstance("SHA512withRSA");
            verifier512.initVerify(publicKey);
            SignatureOutputStream vos512 = new SignatureOutputStream(verifier512);

            out = new TeeOutputStream(new TeeOutputStream(new TeeOutputStream(new TeeOutputStream(new TeeOutputStream(dos1, sos1), vos1), dos512), sos512), vos512);
        }

        public TeeOutputStream getOut() {
            return out;
        }

        public void addRecord(JSONObject sign, String prefix) throws GeneralSecurityException, IOException {
            // digest
            byte[] digest = sha1.digest();
            sign.put(prefix + "digest", new String(Base64.encodeBase64(digest), "UTF-8"));
            sign.put(prefix + "digest512", Hex.encodeHexString(sha512.digest()));

            // signature
            byte[] s1 = sha1sig.sign();
            byte[] s512 = sha512sig.sign();
            sign.put(prefix + "signature", new String(Base64.encodeBase64(s1), "UTF-8"));
            sign.put(prefix + "signature512", Hex.encodeHexString(s512));

            // did the signature validate?
            if (!verifier1.verify(s1))
                throw new GeneralSecurityException("Signature (SHA-1) failed to validate. Either the certificate and the private key weren't matching, or a bug in the program.");
            if (!verifier512.verify(s512))
                throw new GeneralSecurityException("Signature (SHA-512) failed to validate. Either the certificate and the private key weren't matching, or a bug in the program.");
        }
    }

    static {
        addProvider(new BouncyCastleProvider());
    }
}