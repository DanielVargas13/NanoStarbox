package box.star.net;

import box.star.content.MimeTypeDriver;
import box.star.net.http.IHTTPSession;
import box.star.net.tools.*;
import org.junit.jupiter.api.Test;

import java.io.File;

import static box.star.net.tools.RhinoMacroDriver.RHINO_MACRO_DRIVER_MIME_TYPE;

class WebServiceTest {

  WebService ws = new WebService();

  @Test void WebService(){

    ZipSiteDriver zipSiteDriver = new ZipSiteDriver(ws, "/jna", new File("site/jna-4.5.2.jar"));
    RhinoMacroDriver rhinoMacroDriver = new RhinoMacroDriver(ws);

    new ContentProvider(ws, "/") {
      File root = new File("site");
      @Override
      public ServerContent getContent(IHTTPSession session) {
        String uri = session.getUri();
        File data = new File(root, uri.substring(1));
        return new ServerContent(session, getMimeType(uri), data);
      }
    };

    ws.addMimeTypeDriver("text/html", new MimeTypeDriver() {
      @Override
      public ServerResult createMimeTypeResult(ServerContent content) {
        if (content.isOkay()){
          String mimeType = ws.readMimeTypeMagic(content.getStream());
          if (RHINO_MACRO_DRIVER_MIME_TYPE.equals(mimeType)){
            return rhinoMacroDriver.createMimeTypeResult(content);
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