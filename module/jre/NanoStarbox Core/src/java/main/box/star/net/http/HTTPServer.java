package box.star.net.http;

/*
 * #%L
 * HTTPServer-Core
 * %%
 * Copyright (C) 2012 - 2016 mime-type
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the mime-type nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import box.star.net.http.content.CookieHandler;
import box.star.net.http.response.Response;
import box.star.net.http.response.Status;
import box.star.net.http.sockets.DefaultServerSocketFactory;
import box.star.net.http.sockets.SecureServerSocketFactory;
import box.star.net.http.tempfiles.DefaultTempFileManagerFactory;
import box.star.net.http.tempfiles.ITempFileManager;
import box.star.net.http.threading.DefaultAsyncRunner;
import box.star.net.http.threading.IAsyncRunner;
import box.star.net.http.util.IFactory;
import box.star.net.http.util.IFactoryThrowing;
import box.star.net.http.util.IHandler;
import box.star.state.Configuration;

import javax.net.ssl.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A simple, tiny, nicely embeddable HTTP server in Java
 * <p/>
 * <p/>
 * HTTPServer
 * <p>
 * Copyright (c) 2012-2013 by Paul S. Hawke, 2001,2005-2013 by Jarno Elonen,
 * 2010 by Konstantinos Togias
 * </p>
 * <p/>
 * <p/>
 * <b>Features + limitations: </b>
 * <ul>
 * <p/>
 * <li>Only one Java file</li>
 * <li>Java 5 compatible</li>
 * <li>Released as open source, Modified BSD licence</li>
 * <li>No fixed config files, logging, authorization etc. (Implement yourself if
 * you need them.)</li>
 * <li>Supports parameter parsing of GET and POST methods (+ rudimentary PUT
 * support in 1.25)</li>
 * <li>Supports both dynamic content and file serving</li>
 * <li>Supports file upload (since version 1.2, 2010)</li>
 * <li>Supports partial content (streaming)</li>
 * <li>Supports ETags</li>
 * <li>Never caches anything</li>
 * <li>Doesn't limit bandwidth, request time or simultaneous connections</li>
 * <li>Default code serves files and shows all HTTP parameters and headers</li>
 * <li>File server supports directory listing, index.html and index.htm</li>
 * <li>File server supports partial content (streaming)</li>
 * <li>File server supports ETags</li>
 * <li>File server does the 301 redirection trick for directories without '/'</li>
 * <li>File server supports simple skipping for files (continue download)</li>
 * <li>File server serves also very long files without memory overhead</li>
 * <li>Contains a built-in list of most common MIME types</li>
 * <li>All header names are converted to lower case so they don't vary between
 * browsers/clients</li>
 * <p/>
 * </ul>
 * <p/>
 * <p/>
 * <b>How to use: </b>
 * <ul>
 * <p/>
 * <li>Subclass and implement serviceRequest() and embed to your own program</li>
 * <p/>
 * </ul>
 * <p/>
 * See the separate "LICENSE.md" file for the distribution license (Modified BSD
 * licence)
 */
public abstract class HTTPServer {

  public static final String
      CONFIG_HOST = "host",
      CONFIG_PORT = "port",
      CONFIG_DAEMON = "daemon",
      CONFIG_SOCKET_READ_TIMEOUT = "socket-read-timeout";

  public static final String CONTENT_DISPOSITION_REGEX = "([ |\t]*Content-Disposition[ |\t]*:)(.*)";
  public static final Pattern CONTENT_DISPOSITION_PATTERN = Pattern.compile(CONTENT_DISPOSITION_REGEX, Pattern.CASE_INSENSITIVE);
  public static final String CONTENT_TYPE_REGEX = "([ |\t]*content-type[ |\t]*:)(.*)";
  public static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile(CONTENT_TYPE_REGEX, Pattern.CASE_INSENSITIVE);
  public static final String CONTENT_DISPOSITION_ATTRIBUTE_REGEX = "[ |\t]*([a-zA-Z]*)[ |\t]*=[ |\t]*['|\"]([^\"^']*)['|\"]";
  public static final Pattern CONTENT_DISPOSITION_ATTRIBUTE_PATTERN = Pattern.compile(CONTENT_DISPOSITION_ATTRIBUTE_REGEX);
  /**
   * Common MIME type for dynamic content: plain text
   */
  public static final String MIME_PLAINTEXT = "text/plain";
  /**
   * Common MIME type for dynamic content: html
   */
  public static final String MIME_HTML = "text/html";
  /**
   * logger to log to.
   */
  public static final Logger LOG = Logger.getLogger(HTTPServer.class.getName());
  /**
   * Pseudo-Parameter to use to store the actual query string in the
   * parameters map for later re-processing.
   */
  private static final String QUERY_STRING_PARAMETER = "HTTPServer.QUERY_STRING";

  private final Configuration.Manager<String, Serializable> configurationManager = new Configuration.Manager<>(getClass().getName());
  public final Configuration<String, Serializable> configuration = configurationManager.getConfiguration();

  protected List<IHandler<IHTTPSession, Response>> interceptors = new ArrayList<IHandler<IHTTPSession, Response>>(4);
  /**
   * Pluggable strategy for asynchronously executing requests.
   */
  protected IAsyncRunner asyncRunner;
  private volatile ServerSocket myServerSocket;
  private IFactoryThrowing<ServerSocket, IOException> serverSocketFactory = new DefaultServerSocketFactory();
  private Thread myThread;
  private IHandler<IHTTPSession, Response> httpHandler;
  /**
   * Pluggable strategy for creating and cleaning up temporary files.
   */
  private IFactory<ITempFileManager> tempFileManagerFactory;

  private long lastAccessTime = 0;

  /**
   * Constructs an HTTP server on given hostname and port.
   */
  public HTTPServer() {
    configurationManager.set(CONFIG_HOST, "localhost");
    configurationManager.set(CONFIG_PORT, 8080);
    configurationManager.set(CONFIG_DAEMON, true);
    configurationManager.set(CONFIG_SOCKET_READ_TIMEOUT, 80000);
    setTempFileManagerFactory(new DefaultTempFileManagerFactory());
    setAsyncRunner(new DefaultAsyncRunner());
    // creates a default handler that redirects to deprecated serviceRequest();
    this.httpHandler = new IHandler<IHTTPSession, Response>() {
      @Override
      public Response handle(IHTTPSession input) {
        return HTTPServer.this.serviceRequest(input);
      }
    };
  }

  /**
   * Creates an SSLSocketFactory for HTTPS. Pass a loaded KeyStore and an
   * array of loaded KeyManagers. These objects must properly
   * loaded/initialized by the caller.
   */
  public static SSLServerSocketFactory makeSSLSocketFactory(KeyStore loadedKeyStore, KeyManager[] keyManagers) throws IOException {
    SSLServerSocketFactory res = null;
    try {
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(loadedKeyStore);
      SSLContext ctx = SSLContext.getInstance("TLS");
      ctx.init(keyManagers, trustManagerFactory.getTrustManagers(), null);
      res = ctx.getServerSocketFactory();
    }
    catch (Exception e) {
      throw new IOException(e.getMessage());
    }
    return res;
  }

  /**
   * Creates an SSLSocketFactory for HTTPS. Pass a loaded KeyStore and a
   * loaded KeyManagerFactory. These objects must properly loaded/initialized
   * by the caller.
   */
  public static SSLServerSocketFactory makeSSLSocketFactory(KeyStore loadedKeyStore, KeyManagerFactory loadedKeyFactory) throws IOException {
    try {
      return makeSSLSocketFactory(loadedKeyStore, loadedKeyFactory.getKeyManagers());
    }
    catch (Exception e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Creates an SSLSocketFactory for HTTPS. Pass a KeyStore resource with your
   * certificate and passphrase
   */
  public static SSLServerSocketFactory makeSSLSocketFactory(String keyAndTrustStoreClasspathPath, char[] passphrase) throws IOException {
    try {
      KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
      InputStream keystoreStream = HTTPServer.class.getResourceAsStream(keyAndTrustStoreClasspathPath);

      if (keystoreStream == null) {
        throw new IOException("Unable to load keystore from classpath: " + keyAndTrustStoreClasspathPath);
      }

      keystore.load(keystoreStream, passphrase);
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(keystore, passphrase);
      return makeSSLSocketFactory(keystore, keyManagerFactory);
    }
    catch (Exception e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Creates an SSLSocketFactory for HTTPS. Pass a KeyStore resource with your
   * certificate and passphrase
   */
  public static SSLServerSocketFactory makeSSLSocketFactory(File keyAndTrustStoreClasspathPath, char[] passphrase) throws IOException {
    try {
      KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
      InputStream keystoreStream = new FileInputStream(keyAndTrustStoreClasspathPath);
      if (keystoreStream == null) {
        throw new IOException("Unable to load keystore from path: " + keyAndTrustStoreClasspathPath);
      }
      keystore.load(keystoreStream, passphrase);
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(keystore, passphrase);
      return makeSSLSocketFactory(keystore, keyManagerFactory);
    }
    catch (Exception e) {
      throw new IOException(e.getMessage());
    }
  }

  public static final void safeClose(Object closeable) {
    try {
      if (closeable != null) {
        if (closeable instanceof Closeable) {
          ((Closeable) closeable).close();
        } else if (closeable instanceof Socket) {
          ((Socket) closeable).close();
        } else if (closeable instanceof ServerSocket) {
          ((ServerSocket) closeable).close();
        } else {
          throw new IllegalArgumentException("Unknown object to close");
        }
      }
    }
    catch (IOException e) {
      HTTPServer.LOG.log(Level.SEVERE, "Could not close", e);
    }
  }

  /**
   * Decode parameters from a URL, handing the case where a single parameter
   * name might have been supplied several times, by return lists of values.
   * In general these lists will contain a single element.
   *
   * @param parms original <b>HTTPServer</b> parameters values, as passed to the
   *              <code>serviceRequest()</code> method.
   * @return a map of <code>String</code> (parameter name) to
   * <code>List&lt;String&gt;</code> (a list of the values supplied).
   */
  protected static Map<String, List<String>> decodeParameters(Map<String, String> parms) {
    return decodeParameters(parms.get(HTTPServer.QUERY_STRING_PARAMETER));
  }

  /**
   * Decode parameters from a URL, handing the case where a single parameter
   * name might have been supplied several times, by return lists of values.
   * In general these lists will contain a single element.
   *
   * @param queryString a query string pulled from the URL.
   * @return a map of <code>String</code> (parameter name) to
   * <code>List&lt;String&gt;</code> (a list of the values supplied).
   */
  protected static Map<String, List<String>> decodeParameters(String queryString) {
    Map<String, List<String>> parms = new HashMap<String, List<String>>();
    if (queryString != null) {
      StringTokenizer st = new StringTokenizer(queryString, "&");
      while (st.hasMoreTokens()) {
        String e = st.nextToken();
        int sep = e.indexOf('=');
        String propertyName = sep >= 0 ? decodePercent(e.substring(0, sep)).trim() : decodePercent(e).trim();
        if (!parms.containsKey(propertyName)) {
          parms.put(propertyName, new ArrayList<String>());
        }
        String propertyValue = sep >= 0 ? decodePercent(e.substring(sep + 1)) : null;
        if (propertyValue != null) {
          parms.get(propertyName).add(propertyValue);
        }
      }
    }
    return parms;
  }

  /**
   * Decode percent encoded <code>String</code> values.
   *
   * @param str the percent encoded <code>String</code>
   * @return expanded form of the input, for example "foo%20bar" becomes
   * "foo bar"
   */
  public static String decodePercent(String str) {
    String decoded = null;
    try {
      decoded = URLDecoder.decode(str, "UTF8");
    }
    catch (UnsupportedEncodingException ignored) {
      HTTPServer.LOG.log(Level.WARNING, "Encoding not supported, ignored", ignored);
    }
    return decoded;
  }

  public static String getErrorStackText(Exception e) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    return sw.toString();
  }

  public ServerSocket getMyServerSocket() {
    return myServerSocket;
  }

  public void setHTTPHandler(IHandler<IHTTPSession, Response> handler) {
    this.httpHandler = handler;
  }

  public void addHTTPInterceptor(IHandler<IHTTPSession, Response> interceptor) {
    interceptors.add(interceptor);
  }

  /**
   * Forcibly closes all connections that are open.
   */
  public synchronized void closeAllConnections() {
    stop();
  }

  // -------------------------------------------------------------------------------
  // //

  /**
   * create a instance of the client handler, subclasses can return a subclass
   * of the HTTPClient.
   *
   * @param finalAccept the socket the cleint is connected to
   * @param inputStream the input stream
   * @return the client handler
   */
  protected HTTPClient createClientHandler(final Socket finalAccept, final InputStream inputStream) {
    return new HTTPClient(this, inputStream, finalAccept);
  }

  /**
   * Instantiate the server runnable, can be overwritten by subclasses to
   * provide a subclass of the HTTPService.
   *
   * @param timeout the socet timeout to use.
   * @return the server runnable.
   */
  protected HTTPService createServerRunnable(final int timeout) {
    return new HTTPService(this, timeout);
  }

  public final int getPort() {
    return this.myServerSocket == null ? configuration.get(CONFIG_PORT) : this.myServerSocket.getLocalPort();
  }

  public final boolean isAlive() {
    return wasStarted() && !this.myServerSocket.isClosed() && this.myThread.isAlive();
  }

  public IFactoryThrowing<ServerSocket, IOException> getServerSocketFactory() {
    return serverSocketFactory;
  }

  public void setServerSocketFactory(IFactoryThrowing<ServerSocket, IOException> serverSocketFactory) {
    this.serverSocketFactory = serverSocketFactory;
  }

  public String getHost() {
    return configuration.get(CONFIG_HOST);
  }

  public IFactory<ITempFileManager> getTempFileManagerFactory() {
    return tempFileManagerFactory;
  }

  /**
   * Pluggable strategy for creating and cleaning up temporary files.
   *
   * @param tempFileManagerFactory new strategy for handling temp files.
   */
  public void setTempFileManagerFactory(IFactory<ITempFileManager> tempFileManagerFactory) {
    this.tempFileManagerFactory = tempFileManagerFactory;
  }

  public void makeSecure(File jks, String password) {
    try {
      SSLServerSocketFactory socketFactory =
          makeSSLSocketFactory(jks, password.toCharArray());
      makeSecure(socketFactory, null);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Call before start() to serve over HTTPS instead of HTTP
   */
  public void makeSecure(SSLServerSocketFactory sslServerSocketFactory, String[] sslProtocols) {
    this.serverSocketFactory = new SecureServerSocketFactory(sslServerSocketFactory, sslProtocols);
  }

  public long getLastAccessTime() {
    return lastAccessTime;
  }

  public long getIdleTime() {
    if (lastAccessTime == 0) return 0;
    long now = new Date().getTime();
    return now - lastAccessTime;
  }

  /**
   * This is the "master" method that delegates requests to handlers and makes
   * sure there is a response to every request. You are not supposed to call
   * or override this method in any circumstances. But no one will stop you if
   * you do. I'm a Javadoc, not Code Police.
   *
   * @param session the incoming session
   * @return a response to the incoming session
   */
  public Response handle(IHTTPSession session) {

    lastAccessTime = new Date().getTime();
    CookieHandler cookies = session.getCookies();

    for (IHandler<IHTTPSession, Response> interceptor : interceptors) {
      Response response = interceptor.handle(session);
      if (response != null)
        return cookies.unloadQueue(response);
    }
    return cookies.unloadQueue(httpHandler.handle(session));
  }

  /**
   * Override this to customize the server.
   * <p/>
   * <p/>
   * (By default, this returns a 404 "Not Found" plain text error response.)
   *
   * @param session The HTTP session
   * @return HTTP response, see class Response for details
   */
  @SuppressWarnings("Deprecated")
  protected Response serviceRequest(IHTTPSession session) {
    return Response.plainTextResponse(Status.NOT_FOUND, "Not Found");
  }

  /**
   * Pluggable strategy for asynchronously executing requests.
   *
   * @param asyncRunner new strategy for handling threads.
   */
  public void setAsyncRunner(IAsyncRunner asyncRunner) {
    this.asyncRunner = asyncRunner;
  }

  /**
   * Start the server.
   *
   * @throws IOException if the socket is in use.
   */
  public void start() throws IOException {

    configurationManager.get(CONFIG_HOST).setWritable(false);
    configurationManager.get(CONFIG_PORT).setWritable(false);
    configurationManager.get(CONFIG_SOCKET_READ_TIMEOUT).setWritable(false);
    configurationManager.get(CONFIG_DAEMON).setWritable(false);

    this.myServerSocket = this.getServerSocketFactory().create();
    this.myServerSocket.setReuseAddress(true);

    HTTPService httpService = createServerRunnable(configuration.get(CONFIG_SOCKET_READ_TIMEOUT));
    this.myThread = new Thread(httpService);
    this.myThread.setDaemon(configuration.get(CONFIG_DAEMON));
    this.myThread.setName("HTTPServer Run Listener");
    this.myThread.start();
    while (!httpService.hasBoundSocketConnection() && httpService.getSocketBindingException() == null) {
      try {
        Thread.sleep(10L);
      }
      catch (Throwable e) {
        // on android this may not be allowed, that's why we
        // catch throwable the wait should be very short because we are
        // just waiting for the bind of the socket
      }
    }
    if (httpService.getSocketBindingException() != null) {
      throw httpService.getSocketBindingException();
    }
  }

  /**
   * Stop the server.
   */
  public void stop() {
    try {
      safeClose(this.myServerSocket);
      this.asyncRunner.closeAll();
      if (this.myThread != null) {
        this.myThread.join();
      }
    }
    catch (Exception e) {
      HTTPServer.LOG.log(Level.SEVERE, "Could not stop all connections", e);
    }
  }

  public final boolean wasStarted() {
    return this.myServerSocket != null && this.myThread != null;
  }

  public Response staticFileResponse(File file, String mimeType, IHTTPSession query) {

    if (!file.exists()) return Response.notFoundResponse();

    Response res;
    Map<String, String> header = query.getHeaders();
    try {
      // Calculate etag
      String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());

      // Support (simple) skipping:
      long startFrom = 0;
      long endAt = -1;
      String range = header.get("range");
      if (range != null) {
        if (range.startsWith("bytes=")) {
          range = range.substring("bytes=".length());
          int minus = range.indexOf('-');
          try {
            if (minus > 0) {
              startFrom = Long.parseLong(range.substring(0, minus));
              endAt = Long.parseLong(range.substring(minus + 1));
            }
          }
          catch (NumberFormatException ignored) {
          }
        }
      }

      // get if-range header. If present, it must match etag or else we
      // should ignore the range request
      String ifRange = header.get("if-range");
      boolean headerIfRangeMissingOrMatching = (ifRange == null || etag.equals(ifRange));

      String ifNoneMatch = header.get("if-none-match");
      boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null && ("*".equals(ifNoneMatch) || ifNoneMatch.equals(etag));

      // Change return code and add Content-Range header when skipping is
      // requested
      long fileLen = file.length();

      if (headerIfRangeMissingOrMatching && range != null && startFrom >= 0 && startFrom < fileLen) {
        // range request that matches current etag
        // and the startFrom of the range is satisfiable
        if (headerIfNoneMatchPresentAndMatching) {
          // range request that matches current etag
          // and the startFrom of the range is satisfiable
          // would return range from file
          // respond with not-modified
          res = Response.newFixedLengthResponse(Status.NOT_MODIFIED, mimeType, "");
          res.addHeader("ETag", etag);
        } else {
          if (endAt < 0) {
            endAt = fileLen - 1;
          }
          long newLen = endAt - startFrom + 1;
          if (newLen < 0) {
            newLen = 0;
          }

          InputStream fis = new FileInputStream(file);
          fis.skip(startFrom);

          res = Response.newFixedLengthResponse(Status.PARTIAL_CONTENT, mimeType, fis, newLen);
          res.addHeader("Accept-Ranges", "bytes");
          res.addHeader("Content-Length", "" + newLen);
          res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
          res.addHeader("ETag", etag);
        }
      } else {

        if (headerIfRangeMissingOrMatching && range != null && startFrom >= fileLen) {
          // return the size of the file
          // 4xx responses are not trumped by if-none-match
          res = Response.newFixedLengthResponse(Status.RANGE_NOT_SATISFIABLE, MIME_PLAINTEXT, "");
          res.addHeader("Content-Range", "bytes */" + fileLen);
          res.addHeader("ETag", etag);
        } else if (range == null && headerIfNoneMatchPresentAndMatching) {
          // full-file-fetch request
          // would return entire file
          // respond with not-modified
          res = Response.newFixedLengthResponse(Status.NOT_MODIFIED, mimeType, "");
          res.addHeader("ETag", etag);
        } else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
          // range request that doesn't match current etag
          // would return entire (different) file
          // respond with not-modified

          res = Response.newFixedLengthResponse(Status.NOT_MODIFIED, mimeType, "");
          res.addHeader("ETag", etag);
        } else {
          // supply the file
          res = Response.newFixedLengthResponse(Status.OK, mimeType, new FileInputStream(file), (int) fileLen);
          res.addHeader("Accept-Ranges", "bytes");
          res.addHeader("Content-Length", "" + fileLen);
          res.addHeader("ETag", etag);
        }
      }
    }
    catch (IOException ioe) {
      res = Response.plainTextResponse(Status.INTERNAL_ERROR, getErrorStackText(ioe));
    }

    return res;
  }

  public static Response serverExceptionResponse(Exception e) {
    return Response.plainTextResponse(Status.INTERNAL_ERROR, getErrorStackText(e));
  }

  /**
   * URL-encodes everything between "/"-characters. Encodes spaces as '%20'
   * instead of '+'.
   */
  private String encodeUri(String uri) {
    String newUri = "";
    StringTokenizer st = new StringTokenizer(uri, "/ ", true);
    while (st.hasMoreTokens()) {
      String tok = st.nextToken();
      if ("/".equals(tok)) {
        newUri += "/";
      } else if (" ".equals(tok)) {
        newUri += "%20";
      } else {
        try {
          newUri += URLEncoder.encode(tok, "UTF-8");
        }
        catch (UnsupportedEncodingException ignored) {
        }
      }
    }
    return newUri;
  }

  public static final class ResponseException extends Exception {

    private static final long serialVersionUID = 6569838532917408380L;

    private final Status status;

    public ResponseException(Status status, String message) {
      super(message);
      this.status = status;
    }

    public ResponseException(Status status, String message, Exception e) {
      super(message, e);
      this.status = status;
    }

    public Status getStatus() {
      return this.status;
    }
  }

}
