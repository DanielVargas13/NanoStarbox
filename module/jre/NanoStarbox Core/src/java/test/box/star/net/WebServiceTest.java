package box.star.net;

import box.star.net.tools.*;
import org.junit.jupiter.api.Test;

import java.io.File;

import static box.star.net.RhinoPageDriver.NANO_STARBOX_JAVASCRIPT_SERVER_PAGE;

class WebServiceTest {

  WebService ws = new WebService();

  @Test void WebService(){

    ws.mount(new ZipSiteProvider("/jna", new File("site/jna-4.5.2.jar")));
    ws.mount(new ZipSiteProvider("/test", new File("site/site.zip")));

    ws.mount(new FileContentProvider("/", new File("site")));

    RhinoPageDriver rhinoPageDriver = new RhinoPageDriver();

    // this enables automatic *.jsp to text/html
    ws.addMimeTypeDriver(NANO_STARBOX_JAVASCRIPT_SERVER_PAGE, rhinoPageDriver);

    /*
      JavaScriptPageBuilder includes a mime-type-scanner, but we don't need to register it,
      because we only use its functionality to parse specialized text/html content-types:

      ws.mimeTypeMap.addMimeTypeScanner(serverPageBuilder);

      using this method instead of the above code, ensures we will only parse server content
      that is specified as text/html, and includes our mime-type-directive at the head of the
      content:

      "<!MIME "+ NANO_STARBOX_JAVASCRIPT_SERVER_PAGE+">"

     */
    // a driver that conditionally calls the above driver
    ws.addMimeTypeDriver("text/html", new MimeTypeDriver<WebService>() {
      @Override
      public ServerResult createMimeTypeResult(WebService server, ServerContent content) {
        if (content.isOkay()){
          String mimeType = rhinoPageDriver.scanMimeType(content.getStream());
          if (NANO_STARBOX_JAVASCRIPT_SERVER_PAGE.equals(mimeType)){
            return rhinoPageDriver.createMimeTypeResult(ws, content);
          }
        }
        return new ServerResult(content);
      }
    });

    try {
      ws.start();
      while (ws.isAlive()) Thread.sleep(5000);
    }
    catch (Exception e) {
      e.printStackTrace();
    } finally {
      ws.stop();
    }

  }

}