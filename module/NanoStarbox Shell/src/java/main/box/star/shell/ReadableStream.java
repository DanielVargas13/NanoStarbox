package box.star.shell;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PipedInputStream;

public class ReadableStream implements Stream {
  InputStream stream;
  String uri;
  public ReadableStream(InputStream stream){
    this.stream = stream;
  }
  public ReadableStream(String uri, InputStream stream){
    this(stream);
    this.uri = uri;
  }
  @Override
  public boolean isWritable() {
    return false;
  }
  @Override
  public boolean isReadable() {
    return true;
  }
  @Override
  public boolean isTerminal() {
    return false;
  }
  @Override
  public boolean isPipe() {
    return stream instanceof PipedInputStream;
  }
  @Override
  public boolean isFile() {
    return stream instanceof FileInputStream;
  }
  @Override
  public boolean isBuffered() {
    return stream instanceof BufferedInputStream;
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
