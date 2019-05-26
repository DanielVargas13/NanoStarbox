package box.star.shell.runtime.io;

import java.io.*;

public class WritableStream implements Stream {
  private OutputStream stream;
  private String uri;
  public WritableStream(OutputStream stream){
    this.stream = stream;
  }
  public WritableStream(String uri, OutputStream stream){
    this(stream);
    this.uri = uri;
  }
  @Override
  final public boolean isWritable() {
    return true;
  }
  @Override
  final public boolean isReadable() {
    return false;
  }
  @Override
  final public boolean isTerminal() {
    return System.out.equals(stream) || System.err.equals(stream);
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
