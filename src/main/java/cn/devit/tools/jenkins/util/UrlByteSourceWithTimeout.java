package cn.devit.tools.jenkins.util;

import com.google.common.io.ByteSource;
import org.eclipse.jdt.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;

public final class UrlByteSourceWithTimeout extends ByteSource {

  private final URL url;
  private final Duration timeout;

  public UrlByteSourceWithTimeout(@NonNull URL url, @NonNull Duration timeout) {
    this.url = checkNotNull(url);
    this.timeout = timeout;
  }

  @Override
  public InputStream openStream() throws IOException {
    final URLConnection urlConnection = url.openConnection();
    urlConnection.setConnectTimeout((int) (timeout.toMillis()));
    urlConnection.setReadTimeout((int)(timeout.toMillis()));
    return urlConnection.getInputStream();
  }

  @Override
  public String toString() {
    return "Resources.asByteSource(" + url + ")";
  }
}