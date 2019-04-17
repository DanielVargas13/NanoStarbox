package box.star.js;

import box.star.WebServer;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.*;

class RhinoWebDriverTest {
    WebServer ws = new WebServer();
    @Test
    void p() throws Exception {
        ws.setDocumentRoot("../../sample/site/rhino-web-driver");
        new RhinoWebDriver(ws);
        ws.makeSecure(new File("../../sample/Starbox.jks"), "Starbox");
        ws.start();
        System.err.println("\nhttps://"+ws.getHost()+":"+ws.getPort()+"/\n");
        System.err.println("\tYou will get errors about certificates, because they are for development-purposes.\n\tAccept them to continue development-testing.\n\n\tPress stop to quit.");

        do {
            Thread.sleep(1000);
        } while (ws.isAlive());

    }
}