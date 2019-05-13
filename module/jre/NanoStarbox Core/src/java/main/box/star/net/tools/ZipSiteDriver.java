package box.star.net.tools;

import box.star.net.WebServer;
import box.star.net.http.IHTTPSession;
import box.star.net.http.response.Response;
import box.star.net.http.response.Status;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Load the zip archive at the given virtual directory.
 */
public class ZipSiteDriver implements WebServer.IResponseHandler {

  protected ZipFile zipFile;
  public final String virtualDirectory;
  public Map<String, ZipEntry> vfs;

  public ZipSiteDriver(String virtualDirectory, File zipFile){
    try {
      this.zipFile = new ZipFile(zipFile);
      this.virtualDirectory = new File(virtualDirectory).getPath();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    Enumeration<? extends ZipEntry> entries = this.zipFile.entries();
    vfs = new Hashtable<>();
    while(entries.hasMoreElements()){
      ZipEntry entry = entries.nextElement();
      vfs.put(virtualDirectory + "/" + entry.getName(), entry);
    }
  }

  @Override
  public Response generateServiceResponse(WebServer webServer, InputStream file, String mimeType, IHTTPSession ihttpSession) {
    String target = ihttpSession.getUri().substring(1);
    try {
      if (vfs.containsKey(target)){
        InputStream inputStream = this.zipFile.getInputStream(vfs.get(target));
        return Response.newChunkedResponse(Status.OK, mimeType, inputStream);
      }
      return webServer.notFoundResponse();
    }
    catch (Exception e) {
      throw new RuntimeException(target, e);
    }
  }

}
