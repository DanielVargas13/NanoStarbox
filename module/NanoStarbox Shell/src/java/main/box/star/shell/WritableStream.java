package box.star.shell;

import java.io.*;

public class WritableStream implements Stream {
  OutputStream stream;
  String uri;
  public WritableStream(OutputStream stream){
    this.stream = stream;
  }
  public WritableStream(String uri, OutputStream stream){
    this(stream);
    this.uri = uri;
  }
  @Override
  public boolean isWritable() {
    return true;
  }
  @Override
  public boolean isReadable() {
    return false;
  }
  @Override
  public boolean isTerminal() {
    return false;
  }
  @Override
  public boolean isPipe() {
    return stream instanceof PipedOutputStream;
  }
  @Override
  public boolean isFile() {
    return stream instanceof FileOutputStream;
  }
  @Override
  public boolean isBuffered() {
    return stream instanceof BufferedOutputStream;
  }
  @Override
  public boolean isRandom() {
    return false;
  }
  @Override
  public boolean hasURI() {
    return uri != null;
  }
  @Override
  public String getURI() {
    return uri;
  }
  @Override
  public <STREAM> STREAM getController() {
    return (STREAM) stream;
  }
}
