package box.star.shell;

import java.io.RandomAccessFile;

/**
 * Basic Random Stream Provider
 */
public class RandomStream implements Stream {
  private String uri;
  private RandomAccessFile randomAccessFile;
  public RandomStream(String uri, RandomAccessFile randomAccessFile){
    this.uri = uri;
    this.randomAccessFile = randomAccessFile;
  }
  @Override
  final public boolean isWritable() {
    return true;
  }
  @Override
  final public boolean isReadable() {
    return true;
  }
  @Override
  public boolean isTerminal() {
    return false;
  }
  @Override
  public boolean isPipe() {
    return false;
  }
  @Override
  public boolean isFile() {
    return false;
  }
  @Override
  public boolean isBuffered() {
    return false;
  }
  @Override
  final public boolean isRandom() {
    return true;
  }
  @Override
  final public boolean hasURI() {
    return uri == null;
  }
  @Override
  final public String getURI() {
    return uri;
  }
  @Override
  final public RandomAccessFile getController() {
    return randomAccessFile;
  }
}
