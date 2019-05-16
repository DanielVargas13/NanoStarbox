package box.star.net.tools;

import box.star.Tools;
import box.star.contract.NotNull;
import box.star.io.Streams;
import box.star.net.http.IHTTPSession;
import box.star.net.http.response.Response;
import box.star.net.http.response.Status;

import java.io.*;
import java.net.URI;

public class ServerContent {

  public IHTTPSession session;
  public Object data;
  private Object directory;
  public Status status;
  public String mimeType;
  public long length, lastModified;

  final public boolean directoryIsURI(){ return directory instanceof URI;}
  final public boolean directoryIsFile(){ return directory instanceof File; }
  final public boolean hasDirectory(){ return directory != null; }
  final public ServerContent setDirectory(URI directory){
    this.directory = Tools.arrestIsNull(directory, "cannot set server content directory to null");
    return this;
  }
  final private void setDirectory(IHTTPSession session){
    setDirectory(URI.create(session.getAddress() +"/"+ session.getUri()));
  }
  final private ServerContent setDirectory(File directory){
    this.directory = Tools.arrestIsNull(directory, "cannot set server content directory to null");
    return this;
  }
  @NotNull public <URI_OR_FILE> URI_OR_FILE getDirectory() {
    return Tools.arrestIsNull((URI_OR_FILE) directory);
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
      this.status = Status.NO_CONTENT;
    } else {
      if (path.exists()) {
        setDirectory(path.getParentFile());
        this.length = path.length();
        this.lastModified = path.lastModified();
        this.status = Status.OK;
      } else {
        this.status = Status.NOT_FOUND;
      }
    }
  }

  public ServerContent(IHTTPSession session, String mimeType, String content){
    this.session = session;
    this.mimeType = mimeType;
    this.data = content;
    this.status = (content == null)?Status.NO_CONTENT:Status.OK;
    setDirectory(session);
  }

  public ServerContent(IHTTPSession session, String mimeType, byte[] content){
    this.session = session;
    this.mimeType = mimeType;
    this.data = content;
    this.status = (content == null)?Status.NO_CONTENT:Status.OK;
    setDirectory(session);
  }

  public ServerContent(IHTTPSession session, String mimeType, InputStream stream, long length, long lastModified){
    this.session = session;
    this.mimeType = mimeType;
    this.length = length;
    this.lastModified = lastModified;
    if (stream == null) this.status = Status.NO_CONTENT;
    else {
      this.data = (stream instanceof BufferedInputStream)?stream:new BufferedInputStream(stream);
      this.status = Status.OK;
    }
    setDirectory(session);
  }

  public ServerContent(IHTTPSession session, String mimeType, InputStream stream, long length){
    this(session, mimeType, stream, length, System.currentTimeMillis());
  }

  public ServerContent(IHTTPSession session, String mimeType, InputStream stream){
    this(session, mimeType, stream, 0, System.currentTimeMillis());
  }

  public ServerContent(Response response){
    this.data = response;
    this.mimeType = response.getMimeType();
    this.status = (Status) response.getStatus();
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

  public boolean isOkay(){
    return status == Status.OK;
  }

  public boolean isResponse() { return data instanceof Response; }

  public boolean isEmpty(){
    return status == Status.NO_CONTENT;
  }

  public boolean isRedirect() { return status == Status.REDIRECT; }

  public boolean isNotFound(){
    return status == Status.NOT_FOUND;
  }

  final public boolean isVirtual(){ return ! isFile(); }

  public boolean isFile(){
    return data instanceof File;
  }

  public boolean isBufferedInputStream(){
    return data instanceof BufferedInputStream;
  }

  public boolean isString(){
    return data instanceof String;
  }

  public boolean isByteArray(){
    return data instanceof byte[];
  }

  public boolean isUnknown(){
    return ! (isResponse() || isFile() || isBufferedInputStream() || isString() || isByteArray());
  }

  public <ANY> ANY get(){ return (ANY) data; }

}
