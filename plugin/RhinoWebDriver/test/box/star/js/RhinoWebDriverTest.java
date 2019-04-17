package box.star.js;

import box.star.WebServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RhinoWebDriverTest {
    WebServer ws = new WebServer();
    @Test
    void p() throws Exception {
        ws.setDocumentRoot("../../sample/site");
        new RhinoWebDriver(ws);
        ws.start();
        do {
             Thread.sleep(1000);
        } while (ws.isAlive());
    }
}