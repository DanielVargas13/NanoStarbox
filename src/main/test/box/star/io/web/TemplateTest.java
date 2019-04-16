package box.star.io.web;

import box.star.util.Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

class TemplateTest {

    private File templateSource = new File("sample/site/static-page.html");
    Template template = new Template(templateSource);
    Map<String, String> testMap;

    @BeforeEach
    void setup(){
        testMap = new HashMap<>();
        testMap.put("server.getHost()", "localhost");
        testMap.put("server.getPort()", "8080");
    }

    @Test
    void map(){
        System.err.println(template.map(testMap));
    }

    @Test
    void fill(){
        System.err.println(template.fill(null));
    }

}