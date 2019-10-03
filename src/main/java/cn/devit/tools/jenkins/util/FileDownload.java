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

import com.google.common.io.Closer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;

/**
 * Open remote url save to file.
 * <p>
 *
 * @author lxb
 */
public class FileDownload {

  private static Logger logger = LoggerFactory.getLogger(FileDownload.class);

  /**
   * 下载文件，尝试3次，设置IO超时为5分钟。成功后仍然返回保存的文件
   */
  public static File download(URL remote, File saveTo) throws IOException {
    Closer closer = Closer.create();
    File temp = new File(saveTo.getParentFile(), saveTo.getName() + ".downloading");
    //File file = File.createTempFile("wget", "download")
    int countDown = 3;
    while (countDown-- > 0) {
      logger.info("downloading {}", remote);
      Instant begin = Instant.now();
      try {
        final UrlByteSourceWithTimeout urlByteSource = new UrlByteSourceWithTimeout(remote, Duration.ofMinutes(5));
        urlByteSource.copyTo(closer.register(new FileOutputStream(temp)));
        countDown = -1;
        long seconds = Duration.between(begin, Instant.now()).getSeconds();
        logger.info("done {} bytes, {}s", urlByteSource.getContentLength(), seconds);
        break;
      } catch (IOException e) {
        //java.net.SocketTimeoutException
        logger.info("Error while downloading {}, retry {}", remote.getFile(), countDown, e);
      } finally {
        closer.close();
      }
    }
    if (countDown == -1) {
      Files.move(temp.toPath(),
              saveTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
      return saveTo;
    } else {
      throw new IOException("download " + remote + " failed.");
    }
  }

}
