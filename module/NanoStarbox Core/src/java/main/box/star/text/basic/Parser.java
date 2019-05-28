package box.star.text.basic;

import box.star.contract.NotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import static box.star.text.basic.Parser.Status.*;

/**
 * <p>Provides an interface to parse text with a {@link Scanner}</p>
 * <br>
 * <p>A parser implementation is a subclass of this class. In addition to
 * handling the scanner, a parser implementation can store properties in it's
 * own fields and create discrete (pure) object types.</p>
 * <br>
 * <p>This class and it's subclasses can be used to execute any parser
 * implementation through it's {@link #parse(Class, Scanner) static: parse method}.
 * </p>
 * <br>
 * <p>
 *   In a typical parser implementation, there is the notion of an elliptical
 *   list, which is a repeating sequence of inputs, following a particular type
 *   of pattern. To support that, this parser interface provides the
 *   {@link List} class, which can be used to store a list of parser instances
 *   as a list of parsed results. Every subclass of Parser has an associated
 *   list class.
 * </p>
 * @see Scanner
 */
public abstract class Parser {

  // Public Static Class Provisions
  public static class
    List<T extends Parser> extends ArrayList<T> {}

  public static enum
    Status {OK, FAILED}

  // Protected Properties
  protected Status status;
  protected Scanner scanner;

  // Private Properties
  private long start, end;
  private boolean finished;
  private Bookmark origin;

  // Public Properties
  final public boolean successful(){return status.equals(OK);}
  final public boolean isFinished() { return finished; }
  final public long getStart() { return start; }
  final public long getEnd() { return end; }
  final public @NotNull Bookmark getOrigin() { return origin; }
  final public long length(){
    long start = Math.max(0, this.start);
    return (end==0)?scanner.getIndex()-start:end-start;
  }

  // Protected Static Class Provisions
  /**
   * This Interface allows a Parser to specify that upon successful
   * completion the scanner history should be synchronized (flushed) with the
   * current position.
   */
  protected static interface NewFuturePromise {}

  // Public Constructors
  public Parser(@NotNull Scanner scanner){
    if (scanner.endOfSource()){ status = FAILED; return; }
    this.scanner = scanner;
    this.origin = scanner.nextBookmark();
    start = origin.index - 1;
    status = OK;
  }

  // Protected Methods
  final protected @NotNull Bookmark cancel() {
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

  final protected void finish(){
    if (finished) throw new IllegalStateException("this task is already finished (code optimization bug)");
    this.end = scanner.getIndex();
    this.finished = true;
  }

  protected abstract void start();

  // Public Static Methods

  final public static <T extends Parser> @NotNull T parse(@NotNull Class<T> parserClass, @NotNull Scanner scanner) throws IllegalStateException {
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
      if (parser instanceof NewFuturePromise) scanner.flushHistory();
    }
    return parser;
  }

}
