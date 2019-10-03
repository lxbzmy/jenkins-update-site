package cn.devit.tools.jenkins;

import groovy.text.Template;
import groovy.text.markup.MarkupTemplateEngine;
import org.junit.Test;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.web.servlet.view.groovy.GroovyMarkupView;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * @author lxb
 */
public class TemplateTest {

  @Test
  public void index() throws Exception{
    final MarkupTemplateEngine engine = new MarkupTemplateEngine();
    final Template template = engine.createTemplate(new File("src/main/resources/templates/index.tpl"));
    Map<String,Object> map = new HashMap<>();
    map.put("url","127.0.0.1");
    map.put("list", Collections.EMPTY_LIST);
    template.make(map).writeTo(new PrintWriter(System.out));
  }
}
