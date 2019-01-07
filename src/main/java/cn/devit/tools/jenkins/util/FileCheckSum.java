package cn.devit.tools.jenkins.util;

import com.google.common.io.ByteProcessor;
import com.google.common.io.Files;
import org.apache.commons.codec.binary.Hex;

import java.io.File;
import java.io.IOException;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>
 *
 * @author lxb
 */
public class FileCheckSum {

    /**
     * like 60d52df83d1cc6bcfcb4bf7da3d5b3b912e82d8b
     */
    public String sha1sum(File file) throws NoSuchAlgorithmException, IOException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-1");
        return Files.readBytes(file, new ByteProcessor<String>() {
            @Override
            public boolean processBytes(byte[] buf, int off, int len) throws IOException {
                try {
                    digest.digest(buf, off, len);
                } catch (DigestException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }

            @Override
            public String getResult() {
                return Hex.encodeHexString(digest.digest());
            }
        });
    }
}
