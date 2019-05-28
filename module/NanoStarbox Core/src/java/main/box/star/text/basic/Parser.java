package box.star.text.basic;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import static box.star.text.basic.Parser.Status.*;

public abstract class Parser {

  /**
   * This Interface allows a Parser to specify that upon successful
   * completion the scanner history should be synchronized (flushed) with the
   * current position.
   */
  public static interface WithAutoFlush {}
  public static enum Status {OK, FAILED}


  public static class List<T extends Parser> extends ArrayList<T> {}

  // Properties
  protected Status status = OK;
  protected Scanner scanner;

  private long start, end;
  private boolean finished;
  private Bookmark origin;

  final public boolean successful(){return status.equals(OK);}

  final protected Bookmark cancel() {
    Bookmark bookmark = scanner.createBookmark();
    scanner.walkBack(this.end = this.start);
    this.status = FAILED;
    return bookmark;
  }

  /**
   * @return true if end is not zero
   */
  final protected boolean hasEnding(){
    return end != 0;
  }
  /**
   * @return true if the current scanner position is NOT synchronized with this record's ending
   */
  final protected boolean isNotSynchronized(){
    return ((scanner.endOfSource()?-1:0))+scanner.getIndex() != this.end;
  }

  final public boolean isFinished() {
    return finished;
  }

  final protected void finish(){
    if (finished) throw new IllegalStateException("this task is already finished (code optimization bug)");
    this.end = scanner.getIndex();
    this.finished = true;
  }

  final public long getStart() {
    return start;
  }

  final public long getEnd() {
    return end;
  }

  final public Bookmark getOrigin() {
    return origin;
  }

  final protected long length(){
    long start = Math.max(0, this.start);
    return (end==0)?scanner.getIndex()-start:end-start;
  }

  protected abstract void start();

  final public static <T extends Parser> T parse(Class<T> parserClass, Scanner scanner) throws IllegalStateException {
    T parser;
    try {
      Constructor<T> classConstructor = parserClass.getConstructor(Scanner.class);
      classConstructor.setAccessible(true);
      parser = classConstructor.newInstance(scanner);
    } catch (Exception e){throw new RuntimeException(e);}
    if (parser.successful()) { parser.start();
      if (! parser.isFinished())
        throw new IllegalStateException(parserClass.getName()+
            " did not finish parsing");
      else if (parser.isNotSynchronized())
        throw new IllegalStateException(parserClass.getName()+
            " did not synchronize its end result with the scanner state");
      if (parser instanceof WithAutoFlush) scanner.flushHistory();
    }
    return parser;
  }

}
