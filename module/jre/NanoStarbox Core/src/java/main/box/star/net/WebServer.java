package box.star.net;

import box.star.Command;
import box.star.chron.ITimerMethod;
import box.star.chron.Timer;
import box.star.content.MimeTypeMap;
import box.star.content.MimeTypeReader;
import box.star.contract.NotNull;
import box.star.net.http.HTTPServer;
import box.star.net.http.IHTTPSession;
import box.star.net.http.response.Response;
import box.star.net.http.response.Status;
import box.star.net.tools.RhinoWebDriver;
import box.star.state.TokenCache;

import java.io.*;
import java.util.*;

public class WebServer extends HTTPServer {

  private TokenCache<Object> tokenCache;
  private Timer timer = new Timer();

  public WebServer() {

    Stack<String> staticIndexFiles;

    configuration.set("mimeTypeMap", new MimeTypeMap());
    configuration.set("staticIndexFiles", staticIndexFiles = new Stack<>());
    configuration.set("mimeTypeReaders", new Stack<>());
    configuration.set("mimeTypeDriverTable", new Hashtable<>());
    configuration.set("virtualDirectoryHandlers", new Hashtable<>());
    configuration.set("virtualFileLinks", new Hashtable<>());
    configuration.set("documentRoot", new File("."));

    // this field is public...
    staticIndexFiles.add("index.html");
    staticIndexFiles.add("index.htm");
    staticIndexFiles.add("index.xml");

    getMimeTypeReaders().add(new MimeTypeReader() {
      @Override
      public String getMimeTypeMagic(RandomAccessFile data) {
        String line;
        try {
          line = data.readLine();
          if (line.startsWith("//->mime-type: "))
            return line.split(": ")[1];
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    });

    new RhinoWebDriver(this);

  }

  public TimerTask createTimeout(int time, ITimerMethod<Object> callback, Object... parameter) {
    return timer.createTimeout(time, callback, parameter);
  }

  public TimerTask createPulse(int time, ITimerMethod<Object> callback, Object... parameter) {
    return timer.createPulse(time, callback, parameter);
  }

  public TimerTask createAlarm(Date time, ITimerMethod<Object> callback, Object... parameter) {
    return timer.createAlarm(time, callback, parameter);
  }

  public MimeTypeMap getMimeTypeMap() {
    return (MimeTypeMap) configuration.get("mimeTypeMap");
  }

  public Stack<String> getStaticIndexFiles() {
    return (Stack<String>) configuration.get("staticIndexFiles");
  }

  public Stack<MimeTypeReader> getMimeTypeReaders() {
    return (Stack<MimeTypeReader>) configuration.get("mimeTypeReaders");
  }

  /**
   * Gets a map the user can control to configure virtual file links.
   * @return
   */
  public Map<String, File> getVirtualFileLinks(){
    return configuration.get("virtualFileLinks");
  }

  /**
   * Gets a list copy of the current virtual directories registered.
   *
   * @return
   */
  public List<String> getVirtualDirectories(){
    Map<String, IResponseHandler> virtualDirectoryHandlers = configuration.get("virtualDirectoryHandlers");
    return new ArrayList<>(virtualDirectoryHandlers.keySet());
  }

  /**
   * Adds a virtual directory handler to the mount handlers.
   * 
   * All paths inheriting from this path, will be resolved by the virtualDirectoryHandler.
   * 
   * @param path
   * @param virtualDirectoryHandler
   */
  public void addVirtualDirectory(String path, IResponseHandler virtualDirectoryHandler){
    Map<String, IResponseHandler> virtualDirectoryHandlers = configuration.get("virtualDirectoryHandlers");
    virtualDirectoryHandlers.put(path, virtualDirectoryHandler);
  }
  
  public IResponseHandler getVirtualDirectoryHandler(String key){
    Map<String, IResponseHandler> virtualDirectoryHandlers = configuration.get("virtualDirectoryHandlers");
    return virtualDirectoryHandlers.get(key);
  }
  
  public void addStaticIndexFile(String filename) {
    getStaticIndexFiles().push(filename);
  }

  @Override
  public void stop() {
    if (tokenCache != null && tokenCache.isConfiguredForDiskSynchronization())tokenCache.synchronize();
    timer.cancelTimers();
    super.stop();
  }

  public Hashtable<String, MimeTypeDriver> getMimeTypeDriverTable() {
    return (Hashtable<String, MimeTypeDriver>) configuration.get("mimeTypeDriverTable");
  }

  /**
   * Registers a custom-mime-type-driver.
   * <p>
   * Drivers are loaded in update=override order.
   * <p>
   * The mime-type does not have to exist in the server's-mime-type-map.
   * This allows custom back-ends to be called, by each plugin that can access
   * the server's getMimeTypeResponse method.
   * <p>
   * This method does not affect the server's-mime-type-map.
   *
   * @param mimeType
   * @param driver
   */
  public final void registerMimeTypeDriver(String mimeType, MimeTypeDriver driver) {
    Hashtable<String, MimeTypeDriver> mimeTypeDriverTable = getMimeTypeDriverTable();
    if (mimeTypeDriverTable.containsKey(mimeType)) {
      driver.next = mimeTypeDriverTable.get(mimeType);
      mimeTypeDriverTable.put(mimeType, driver);
      return;
    }
    mimeTypeDriverTable.put(mimeType, driver);
  }

  /**
   * Configures the token cache duration for the server.
   *
   * Must be called once and only once before {@link #getTokenCache()}
   *
   * @param duration milliseconds which must pass before token expiration.
   * @return this {@link WebServer}
   */
  public WebServer setTokenCacheDuration(long duration){
    if (tokenCache == null){
      configuration.set("tokenCacheDuration", duration);
      return this;
    }
    throw new IllegalStateException("Token cache is already running");
  }

  /**
   * Gets the token cache for the server.
   *
   * If the token cache does not exist, it is created with a default duration of 1 hour.
   *
   * The token cache allows applications, scripts and pages to store, share and
   * recall runtime data with a serializable key known as a token. A token can
   * then be stored in an object such as a user-cookie, to enable visitation
   * based sessions.
   *
   * @return
   */
  public TokenCache<Object>getTokenCache(){
    if (tokenCache == null){
      tokenCache = new TokenCache<Object>((long)configuration.getOrDefault("tokenCacheDuration", 60 * 10000), 8);
    }
    return tokenCache;
  }

  public void start(String hostname, int port) throws IOException {
    configuration.set(HOST_KEY, hostname);
    configuration.set(PORT_KEY, port);
    start();
  }

  public File getDocumentRoot() {
    return (File) configuration.get("documentRoot");
  }

  public void setDocumentRoot(String documentRoot) {
    setDocumentRoot(new File(documentRoot));
  }

  public void setDocumentRoot(File documentRoot) {
    if (this.wasStarted())
      throw new IllegalStateException("cannot change the web-server-document-root after starting the service");
    configuration.set("documentRoot", documentRoot);
  }

  public boolean blacklistRequest(String uri, File file, String mimeType, IHTTPSession session) {
    return false;
  }

  public File locateServerFile(String uri) {
    // support virtual file links
    Map<String, File> vfl = getVirtualFileLinks();
    File file;
    if (vfl.containsKey(uri)) {
      file = vfl.get(uri);
    } else {
      file = new File(getDocumentRoot(), uri);
    }
    return file;
  }

  public String getFileExtensionMimeType(String extension) {
    return getMimeTypeMap().get(extension);
  }

  public String getFileExtension(File file) {
    return getFileExtension(file.getName());
  }

  public String getFileExtension(String path) {
    return getMimeTypeMap().getFileExtension(path);
  }

  public String mimeTypeMagic(@NotNull File check) {
    String mimeType = null;
    if (check.isDirectory()) return "text/directory";
    else if (check.exists()) {
      RandomAccessFile stream = null;
      try {
        stream = new RandomAccessFile(check, "r");
        for (MimeTypeReader reader : getMimeTypeReaders()) {
          mimeType = reader.getMimeTypeMagic(stream);
          if (mimeType != null) { break; }
          stream.seek(0);
        }
        stream.close();
      }
      catch (FileNotFoundException infinity) {}
      catch (IOException e) {
        throw new RuntimeException("Stream seek failed on: " + check, e);
      }
    }
    return (mimeType != null) ?
        mimeType : getFileExtensionMimeType(getFileExtension(check));
  }

  public String listDirectory(File file, IHTTPSession query) {
    ByteArrayOutputStream listing = new ByteArrayOutputStream();
    Command shell;
    String program;
    if (System.getProperty("os.name").startsWith("Windows")) {
      shell = new Command("cmd", "/c");
      program = "dir";
    } else {
      shell = new Command("sh", "-c");
      program = "ls";
    }
    shell.setDirectory(file.getPath())
        .writeOutputTo(listing)
        .writeErrorTo(listing);
    try {
      shell.start(program);
    }
    catch (Exception e) {e.printStackTrace(new PrintStream(listing));}

    return listing.toString();
  }

  /**
   * Override this method, to serve custom index files.
   *
   * @param directory the existing-file-directory to serve
   * @param query     the user's query
   * @return the server's response
   */
  public Response serveDirectory(File directory, IHTTPSession query) {
    for (String indexType : getStaticIndexFiles()) {
      File test = new File(directory, indexType);
      if (test.exists()) return serveFile(test, mimeTypeMagic(test), query);
    }
    return plainTextResponse(Status.OK, listDirectory(directory, query));
  }

  public Response getMimeTypeResponse(InputStream file, String mimeType, IHTTPSession query) {
    Hashtable<String, MimeTypeDriver> mimeTypeDriverTable = getMimeTypeDriverTable();
    MimeTypeDriver mimeTypeDriver = mimeTypeDriverTable.get(mimeType);
    Response out = null;

    while (out == null) {
      if (mimeTypeDriver == null) break;
      out = mimeTypeDriver.generateServiceResponse(this, query.getUri().substring(1), file, mimeType, query);
      if (out != null) return out;
      mimeTypeDriver = mimeTypeDriver.next;
    }

    if (mimeTypeDriverTable.containsKey(mimeType)) {
      // all processors failed
      // we don't know at this point if information within the file is
      // sensitive, or capable of transmission, so we won't serve anything.
      return blankResponse(Status.UNSUPPORTED_MEDIA_TYPE);
    }

    return null;

  }

  /**
   * Serves an existing file.
   *
   * @param file     the server-file requested
   * @param mimeType the server's knowledge of the file's type
   * @param query    the user's query
   * @return the server's response
   */
  public Response serveFile(File file, String mimeType, IHTTPSession query) {

    Response magic = null;
    try {
      magic = getMimeTypeResponse(new FileInputStream(file), mimeType, query);
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    if (magic != null) return magic;

    return staticFileResponse(file, mimeType, query);

  }

  /**
   * Critical preliminary response logic.
   *
   * @param query the user's query
   * @return the server's response
   */
  @Override
  protected Response serviceRequest(IHTTPSession query) {
    try {
      
      String uri = query.getUri().substring(1);
      File file = locateServerFile(uri);

      String mimeType = mimeTypeMagic(file);

      if (blacklistRequest(uri, file, mimeType, query)) return forbiddenResponse();

      // support virtual directory
      for (String p: getVirtualDirectories()){
        if (uri.startsWith(p)){
          IResponseHandler virtualDirectoryHandler = getVirtualDirectoryHandler(p);
          return virtualDirectoryHandler.generateServiceResponse(this, uri, null, mimeType, query);
        }
      }

      // support server's root file system links
      return (file.isDirectory()) ?
          serveDirectory(file, query) : serveFile(file, mimeType, query);
    }
    catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      return plainTextResponse(Status.OK, sw.toString());
    }
  }

  public interface IResponseHandler {
    Response generateServiceResponse(WebServer webServer, String uri, InputStream fileStream, String mimeType, IHTTPSession ihttpSession);
  }

  /**
   * Provides a basic-mime-type-driver-system.
   * <p>
   * Drivers work with existing, or non-existing files, and custom implementations.
   * <p>
   * Some mime-types may have multiple formats, thus, multiple-processors.
   * if a driver doesn't handle a format, it returns null, and the server-backend,
   * selects the next driver in the linked list, repeating the process until
   * the request is completed or no drivers succeed.
   * <p>
   * The server's response to a failed mime type driver chain is MEDIA_NOT_SUPPORTED
   * <p>
   * execution-order: first come, first serve, per request.
   * circular references are not checked.
   * driver's can't be unloaded, because no method is provided.
   */
  public static class MimeTypeDriver implements IResponseHandler {
    private MimeTypeDriver next;
    public Response generateServiceResponse(WebServer webServer, String uri, InputStream fileStream, String mimeType, IHTTPSession ihttpSession) {
      return null;
    }
  }

}
