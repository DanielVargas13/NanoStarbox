package box.star.shell.io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PipedInputStream;

public class ReadableStream implements Stream {
  private InputStream stream;
  private String uri;
  public ReadableStream(InputStream stream){
    this.stream = stream;
  }
  public ReadableStream(String uri, InputStream stream){
    this(stream);
    this.uri = uri;
  }
  @Override
  final public boolean isWritable() {
    return false;
  }
  @Override
  final public boolean isReadable() {
    return true;
  }
  @Override
  final public boolean isTerminal() {
    return System.in.equals(stream);
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
  final public boolean isRandom() {
    return false;
  }
  @Override
  final public boolean hasURI() {
    return uri != null;
  }
  @Override
  final public String getURI() {
    return uri;
  }
  @Override
  final public <STREAM> STREAM getController() {
    return (STREAM) stream;
  }
}
