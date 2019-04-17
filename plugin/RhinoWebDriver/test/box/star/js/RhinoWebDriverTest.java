package box.star.js;

import box.star.WebServer;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class RhinoWebDriverTest {
    WebServer ws = new WebServer();
    @Test
    void p() throws Exception {
        ws.setDocumentRoot("../../sample/site/rhino-web-driver");
        new RhinoWebDriver(ws);
        ws.makeSecure(new File("../../sample/Starbox.jks"), "Starbox");
        ws.start();
        do {
             Thread.sleep(1000);
        } while (ws.isAlive());
    }
}