package box.star.net;

import box.star.net.tools.RhinoWebDriver;
import org.junit.jupiter.api.Test;

import java.io.File;

class RhinoWebDriverTest {


  WebServer ws = new WebServer();

  {
    ws.setDocumentRoot("src/java/resource/local/site/main");
    ws.makeSecure(new File("src/java/resource/test/Starbox.jks"), "Starbox");
    RhinoWebDriver.createInstance(ws);
  }

  @Test
  void p() throws Exception {

    ws.start();

    System.err.println("\nhttps://" + ws.getHost() + ":" + ws.getPort() + "/\n");

    System.err.println(

        "\tYou will get errors about certificates, because they are for development-purposes.\n" +
            "\tAccept them to continue development-testing.\n\n" +

            "\tPress stop to quit."
    );

    do {
      Thread.sleep(1000);
    } while (ws.isAlive());

  }
}