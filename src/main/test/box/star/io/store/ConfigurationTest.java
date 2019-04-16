package box.star.io.store;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {

    File dataFile = new File("data.cfg");

    @Test void all() throws Throwable {
        dataFile.deleteOnExit();
        test_save();
        test_load();
    }

    void test_save() {
        Configuration data = new Configuration();
        data.setFile(dataFile);
        data.put("test", "hello world");
        data.save();
    }

    void test_load() {
        Configuration data = Configuration.load(dataFile);
        assertEquals("hello world", data.getString("test"));
    }

}