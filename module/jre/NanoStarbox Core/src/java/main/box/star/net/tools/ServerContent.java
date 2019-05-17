package box.star.net.tools;

import box.star.Tools;
import box.star.contract.NotNull;
import box.star.io.Streams;
import box.star.net.http.HTTPServer;
import box.star.net.http.IHTTPSession;
import box.star.net.http.response.Response;
import box.star.net.http.response.Status;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;

public class ServerContent {

  public IHTTPSession session;
  public Object data;
  private Object directory;
  private boolean serverGenerated;
  public Status status;
  public String mimeType;
  public long length, lastModified;

  final private void setModificationTime(long time){
    if (time == 0){
      time = System.currentTimeMillis();
      serverGenerated = true;
    }
    this.lastModified = time;
  }
  final public ServerContent AsGenerated(){
    this.serverGenerated = true;
    return this;
  }
  final public boolean directoryIsURI(){ return directory instanceof URI;}
  final public boolean directoryIsFile(){ return directory instanceof File; }
  final public boolean hasDirectory(){ return directory != null; }
  final public ServerContent setDirectory(URI directory){
    this.directory = Tools.arrestIsNull(directory, "cannot set server content directory to null");
    return this;
  }
  final private void setDirectory(IHTTPSession<HTTPServer> session){
    setDirectory(URI.create(session.getAddress() + session.getServer().getParentUri(session.getUri())));
  }
  final private ServerContent setDirectory(File directory){
    this.directory = Tools.arrestIsNull(directory, "cannot set server content directory to null");
    return this;
  }
  @NotNull public <URL_OR_FILE> URL_OR_FILE getDirectory() {
    if (directory instanceof URI) {
      try {
        return (URL_OR_FILE) ((URI)directory).toURL();
      }
      catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    return Tools.arrestIsNull((URL_OR_FILE) directory);
  }
  /**
   * <p></p>Allows {@link ServerResult} and the like to emulate {@link ServerContent} with it's
   * own initialization parameters.</p>
   */
  protected ServerContent(){}

  public ServerContent(IHTTPSession session, String mimeType, File path){
    this.session = session;
    this.mimeType = mimeType;
    this.data = path;
    if (path == null) {
      setModificationTime(0);
      this.status = Status.NO_CONTENT;
    } else {
      if (path.exists()) {
        setDirectory(path.getParentFile());
        this.length = path.length();
        setModificationTime(path.lastModified());
        this.status = Status.OK;
      } else {
        setModificationTime(0);
        this.status = Status.NOT_FOUND;
      }
    }
  }

  public ServerContent(IHTTPSession session, String mimeType, String content){
    this(session, mimeType, content, 0);
  }

  public ServerContent(IHTTPSession session, String mimeType, String content, long lastModified){
    this.session = session;
    this.mimeType = mimeType;
    this.data = content;
    setModificationTime(lastModified);
    this.status = (content == null)?Status.NO_CONTENT:Status.OK;
    setDirectory(session);
  }

  public ServerContent(IHTTPSession session, String mimeType, byte[] content){
    this(session, mimeType, content, 0);
  }

  public ServerContent(IHTTPSession session, String mimeType, byte[] content, long lastModified){
    this.session = session;
    this.mimeType = mimeType;
    this.data = content;
    setModificationTime(lastModified);
    this.status = (content == null)?Status.NO_CONTENT:Status.OK;
    setDirectory(session);
  }

  public ServerContent(IHTTPSession session, String mimeType, InputStream stream, long length, long lastModified){
    this.session = session;
    this.mimeType = mimeType;
    this.length = length;
    setModificationTime(lastModified);
    if (stream == null) this.status = Status.NO_CONTENT;
    else {
      this.data = (stream instanceof BufferedInputStream)?stream:new BufferedInputStream(stream);
      this.status = Status.OK;
    }
    setDirectory(session);
  }

  public ServerContent(IHTTPSession session, String mimeType, InputStream stream, long length){
    this(session, mimeType, stream, length, 0);
  }

  public ServerContent(IHTTPSession session, String mimeType, InputStream stream){
    this(session, mimeType, stream, 0, 0);
  }

  public ServerContent(Response response){
    this.data = response;
    this.mimeType = response.getMimeType();
    this.status = (Status) response.getStatus();
    setModificationTime(0);
  }

  public BufferedInputStream getStream(){
    if (!isOkay())
      throw new IllegalStateException("status is: "+status);
    if (isFile()) {
      data = new BufferedInputStream(Streams.getInputStream(get()));
      return (BufferedInputStream) data;
    }
    else if (isBufferedInputStream()) return get();
    else if (isString()) {
      data = new BufferedInputStream(new ByteArrayInputStream(((String) data).getBytes()));
      return (BufferedInputStream) data;
    }
    else if (isByteArray()) {
      data = new BufferedInputStream(new ByteArrayInputStream((byte[]) data));
      return (BufferedInputStream) data;
    }
    throw new RuntimeException("unknown data type: "+data.getClass());
  }

  final public boolean isOkay(){ return status == Status.OK; }
  final public boolean isResponse() { return data instanceof Response; }
  final public boolean isEmpty(){ return status == Status.NO_CONTENT; }
  final public boolean isRedirect() { return status == Status.REDIRECT; }
  final public boolean isNotFound(){ return status == Status.NOT_FOUND; }
  final public boolean isVirtual(){ return ! isFile(); }
  final public boolean isFile(){ return data instanceof File; }
  final public boolean isBufferedInputStream(){ return data instanceof BufferedInputStream; }
  final public boolean isString(){ return data instanceof String; }
  final public boolean isByteArray(){ return data instanceof byte[]; }
  final public boolean isServerGenerated(){ return serverGenerated; }
  final public boolean isUnknownType(){
    return ! (isResponse() || isFile() || isBufferedInputStream() || isString() || isByteArray());
  }
  final public boolean isServerResult(){ return this instanceof ServerResult; }
  final public <ANY> ANY get(){ return (ANY) data; }

}
