package box.star.net;

import box.star.net.http.IHTTPSession;
import box.star.net.tools.*;
import org.junit.jupiter.api.Test;

import java.io.File;

import static box.star.net.tools.RhinoMacroDriver.RHINO_MACRO_DRIVER_MIME_TYPE;

class WebServiceTest {

  WebService ws = new WebService();

  @Test void WebService(){

    ws.mountContentProvider(new ZipSiteProvider(ws.mimeTypeMap, "/jna", new File("site/jna-4.5.2.jar")));

    ws.mountContentProvider(new ContentProvider(ws.mimeTypeMap, "/") {
      File root = new File("site");
      @Override
      public ServerContent getContent(IHTTPSession session) {
        String uri = session.getUri();
        File data = new File(root, uri.substring(1));
        return new ServerContent(session, getMimeType(uri), data);
      }
    });

    RhinoMacroDriver rhinoMacroDriver = new RhinoMacroDriver(ws.mimeTypeMap);
    ws.addMimeTypeDriver(RHINO_MACRO_DRIVER_MIME_TYPE, rhinoMacroDriver);

    // a driver that conditionally calls the above driver,
    ws.addMimeTypeDriver("text/html", new MimeTypeDriver<WebService>() {
      @Override
      public ServerResult createMimeTypeResult(WebService server, ServerContent content) {
        if (content.isOkay()){
          String mimeType = ws.scanMimeType(content.getStream());
          if (RHINO_MACRO_DRIVER_MIME_TYPE.equals(mimeType)){
            return rhinoMacroDriver.createMimeTypeResult(ws, content);
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