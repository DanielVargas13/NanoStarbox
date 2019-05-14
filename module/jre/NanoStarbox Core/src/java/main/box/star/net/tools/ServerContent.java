package box.star.net.tools;

import box.star.io.Streams;
import box.star.net.http.IHTTPSession;
import box.star.net.http.response.Status;

import java.io.*;

public class ServerContent {

  public IHTTPSession session;
  public Object data;
  public Status status;
  public String mimeType;
  public long length, lastModified;

  ServerContent(){}

  public ServerContent(IHTTPSession session, String mimeType, File path){
    this.session = session;
    this.mimeType = mimeType;
    this.data = path;
    if (path == null) {
      this.status = Status.NO_CONTENT;
    } else {
      if (path.exists()) {
        this.length = path.length();
        this.lastModified = path.lastModified();
        this.status = Status.OK;
      } else {
        this.status = Status.NOT_FOUND;
      }
    }
  }

  public ServerContent(IHTTPSession session, String mimeType, InputStream stream, long length, long lastModified){
    this.session = session;
    this.mimeType = mimeType;
    this.length = length;
    this.lastModified = lastModified;
    if (stream == null) this.status = Status.NO_CONTENT;
    else {
      this.data = new BufferedInputStream(stream);
      this.status = Status.OK;
    }
  }

  public ServerContent(IHTTPSession session, String mimeType, InputStream stream, long length){
    this(session, mimeType, stream, length, System.currentTimeMillis());
  }

  public ServerContent(IHTTPSession session, String mimeType, InputStream stream){
    this(session, mimeType, stream, 0, System.currentTimeMillis());
  }

  public BufferedInputStream getStream(){
    if (!isOkay())
      throw new IllegalStateException("status is not okay: "+status);
    if (isFile()) {
      data = new BufferedInputStream(Streams.getInputStream(get()));
      return (BufferedInputStream) data;
    }
    else if (isBufferedInputStream()) return get();
    else if (isString()) {
      data = new BufferedInputStream(new ByteArrayInputStream(String.class.cast(data).getBytes()));
      return (BufferedInputStream) data;
    }
    else if (isByteArray()) {
      data = new BufferedInputStream(new ByteArrayInputStream(byte[].class.cast(data)));
      return (BufferedInputStream) data;
    }
    throw new RuntimeException("unknown data type: "+data.getClass());
  }

  public boolean isOkay(){
    return status == Status.OK;
  }

  public boolean isEmpty(){
    return status == Status.NO_CONTENT;
  }

  public boolean isNotFound(){
    return status == Status.NOT_FOUND;
  }

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
    return ! (isFile() || isBufferedInputStream() || isString() || isByteArray());
  }

  public <ANY> ANY get(){ return (ANY) data; }

}
