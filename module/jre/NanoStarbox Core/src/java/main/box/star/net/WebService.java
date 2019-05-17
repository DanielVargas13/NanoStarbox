package box.star.net;

import box.star.content.MimeTypeMap;
import box.star.net.http.HTTPServer;
import box.star.net.http.IHTTPSession;
import box.star.net.http.response.Response;
import box.star.net.tools.*;

import java.io.File;
import java.util.*;

public class WebService extends HTTPServer {

  /**
   * <p>{@link ContentProvider}s provide content for URIs.</p>
   * <br>
   * <p>Specifically: Response, byte[], File, String, or InputStream</p>
   * <br>
   */
  public final List<ContentProvider> contentProviders = new ArrayList<ContentProvider>();
  /**
   * <p>{@link MimeTypeDriver}s manipulate {@link ServerContent}.</p>
   * <br>
   * <p>All of the types that content providers can provide, can be read by a Mime Type
   * driver. Drivers can call other drivers and perform custom-driver-chaining.</p>
   * <br>
   * <p>The return type of a driver ({@link ServerResult}) inherits from {@link ServerContent},
   * and uses the same internal fields; such that a ServerResult object may function
   * as a ServerContent object.</p>
   * <br>
   */
  public final Map<String, MimeTypeDriver<WebService>> mimeTypeDrivers = new Hashtable<>();
  /**
   * See the {@link MimeTypeMap} for details
   */
  public final MimeTypeMap mimeTypeMap = new MimeTypeMap();

  public WebService() { super(); }

  public WebService mount(ContentProvider contentProvider){
    contentProviders.add(contentProvider);
    contentProvider.setMimeTypeMap(mimeTypeMap);
    return this;
  }

  public WebService(String host, int port){
    super();
    configuration.set(CONFIG_HOST, host);
    configuration.set(CONFIG_PORT, port);
  }

  /**
   * <p>Content Providers provide ServerContent, and ServerContent
   * can access the data needed through this method since NSR7.</p>
   */
  @Deprecated final public File getFile(String uri){
    // first: uri-equality
    for (ContentProvider provider:contentProviders){
      if (! (provider instanceof NativeContentProvider)) continue;
      String path = provider.getBaseUri();
      if (path.equals(uri)) {
        File c = ((NativeContentProvider)provider).getFile(uri);
        if (c != null && c.exists()) return c;
      }
    }
    String u = uri;
    // second: parent-uri-equality
    while (! u.equals("/") ) {
      u = u.substring(0, Math.max(0, u.lastIndexOf('/')));
      if (u.equals("")) u = "/";
      for (ContentProvider provider:contentProviders){
        if (! (provider instanceof NativeContentProvider)) continue;
        String path = provider.getBaseUri();
        if (path.equals(u)){
          File f = ((NativeContentProvider)provider).getFile(uri);
          if (f != null && f.exists()) return f;
        }
      }
    }
    // third: fail-silently
    return null;
  }

  final public void addMimeTypeDriver(String mimeType, MimeTypeDriver<WebService> driver) {
    mimeTypeDrivers.put(mimeType, driver);
    if (driver instanceof MimeTypeDriver.WithMediaMapControlPort)
      ((MimeTypeDriver.WithMediaMapControlPort)driver).configureMimeTypeController(mimeTypeMap);
    if (driver instanceof MimeTypeDriver.WithIndexFileListControlPort) {
      ((MimeTypeDriver.WithIndexFileListControlPort)driver).configureIndexFileList(getIndexFileList());
    }
  }

  @Override
  public String getMimeTypeForPath(String path) {
    return mimeTypeMap.get(mimeTypeMap.scanFileExtension(path));
  }

  public ServerContent getContent(IHTTPSession session){
    String uri = session.getUri();
    // first: uri-equality
    for (ContentProvider provider:contentProviders){
      String path = provider.getBaseUri();
      if (path.equals(uri)) return provider.getContent(session);
    }
    String u = uri;
    // second: parent-uri-equality
    while (! u.equals("/") ) {
      u = u.substring(0, Math.max(0, u.lastIndexOf('/')));
      if (u.equals("")) u = "/";
      for (ContentProvider provider:contentProviders){
        String path = provider.getBaseUri();
        if (path.equals(u)){
          ServerContent content = provider.getContent(session);
          if (content != null) return content;
        }
      }
    }
    // third: fail-silently
    return null;
  }

  protected ServerResult getResult(ServerContent content) {
    if (content == null) return null;
    if (content.isOkay()){
      MimeTypeDriver<WebService> driver = mimeTypeDrivers.get(content.mimeType);
      if (driver != null) return driver.createMimeTypeResult(this, content);
    }
    return new ServerResult(content);
  }

  private boolean quiet = true;

  @Override
  protected Response serviceRequest(IHTTPSession session) {

    if (!this.quiet) {
      Map<String, String> header = session.getHeaders();
      Map<String, String> parms = session.getParms();
      String uri = session.getUri();
      System.out.println(session.getMethod() + " '" + uri + "' ");

      Iterator<String> e = header.keySet().iterator();
      while (e.hasNext()) {
        String value = e.next();
        System.out.println("  HDR: '" + value + "' = '" + header.get(value) + "'");
      }
      e = parms.keySet().iterator();
      while (e.hasNext()) {
        String value = e.next();
        System.out.println("  PRM: '" + value + "' = '" + parms.get(value) + "'");
      }
    }

    try {
      ServerResult serverResult = getResult(getContent(session));
      if (serverResult == null) return Response.notFoundResponse();
      return serverResult.getResponse();
    } catch (Exception e){
      return serverExceptionResponse(e);
    }
  }

}
