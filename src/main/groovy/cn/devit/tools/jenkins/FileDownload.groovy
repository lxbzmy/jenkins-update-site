package cn.devit.tools.jenkins

import com.google.common.io.Resources
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * <p>
 *
 * @author lxb
 */
public class FileDownload {

    Logger logger = LoggerFactory.getLogger(FileDownload.classes);


    public void download(URL remote, File saveTo) {
        logger.info("downloading {}", remote)
        File temp = new File(saveTo.getParentFile(), saveTo.getName() + ".downloading");
        Resources.copy(remote, temp);
        temp.renameTo(saveTo);
    }

}
