package box.star.net;

//import box.star.net.tools.RhinoWebDriver;
import box.star.text.basic.Scanner;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.InputStreamReader;

import static box.star.text.Char.LINE_FEED;

class WebServerTest {


  WebServer ws = new WebServer();

  {
    ws.setDocumentRoot("src/java/resource/local/site/main");
    ws.makeSecure(new File("src/java/resource/test/Starbox.jks"), "Starbox");
  }

  @Test
  void WebServer() throws Exception {

    ws.start();

    System.err.println("\nhttps://" + ws.getHost() + ":" + ws.getPort() + "/\n");

    System.err.println(

        "\tYou will get errors about certificates, because they are for development-purposes.\n" +
            "\tAccept them to continue development-testing.\n\n" +

            "\tPress stop to quit."
    );


System.in.read();
  }
}