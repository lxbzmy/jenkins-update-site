package cn.devit.tools.jenkins

import com.google.common.io.Closer
import com.google.common.io.Resources

/**
 * Parse one jenkins update json.
 * <p>
 *
 * @author lxb
 */
class ParseOneVersion {


    File pwd;

    /** Input index url and save to file*/
    public void parseAndSave(File saveHere, URL index) {
        this.pwd = saveHere;

        //what if 302 happened?
        List<IndexFileParser.IndexItem> elements = new IndexFileParser().parseIndexFile(index);
        Closer closer = Closer.create();
        try {
            //TODO memoto
            elements.each { it ->
                String name = it.name;
                String href = it.href;
                try {
                    if (name == 'latest/') {
                        //skip latest is just Permalinks to latest files

                    } else if (name == 'updates/') {

                    } else if (isDirectory(name)) {
                        //warning .
                    } else if (name.endsWith(".json.html")) {
                        println "Skip ${name}"
                    } else {
                        //save file to directory.
                        File file = File.createTempFile("wget", "download")
                        println "Downloading ${name}"
                        Resources.copy(new URL(index, name),
                                closer.register(new FileOutputStream(file)))
                        file.renameTo(new File(pwd, name));
                    }
                } catch (IOException e) {
                    //continue.
                    println "Error while downloading ${name}"

                }
            }
        } finally {
            closer.close();
        }
    }

    boolean isDirectory(String name) {
        return name.endsWith("/")
    }
}
