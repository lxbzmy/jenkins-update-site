package cn.devit.tools.jenkins;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import groovy.json.StringEscapeUtils;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * groovy 2.5 JsonGenerator 才具备了 disableUnicodeEscaping 的能力。
 * <p>
 * <p>
 *
 * @author lxb
 */
public class CanonicalJson {

    @Test
    public void case1() throws Exception {
//        String content = Files.toString(new File("update-center-root.json"),
//                Charsets.UTF_8);
////
//        String jsonString = content.substring(content.indexOf("{"),
//                content.lastIndexOf("}") + 1);
        String jsonString = "{\"z\":\"国\\\"家\",\"a\":[\"中\",\"国\"]}";
        JSONObject json = JSONObject.fromObject(jsonString);

        String left = canonical(json);
        Files.write(left, new File("left.txt"), Charsets.UTF_8);

        Map groovyJson = (Map) new JsonSlurper().parseText(jsonString);
        String right = canonical(JSONObject.fromObject(groovyJson))
        //StringEscapeUtils.unescapeJavaScript(JsonOutput.toJson(groovyJson));
//        System.out.println(StringEscapeUtils.unescapeJavaScript(JsonOutput.toJson(groovyJson)));
        ;
        Files.write(right, new File("right.txt"), Charsets.UTF_8);
        assertEquals(left, right);
    }

    String canonical(JSONObject json) throws IOException {
        StringWriter writer = new StringWriter();
        json.writeCanonical(writer);
        writer.close();
        return writer.toString();
    }

}
