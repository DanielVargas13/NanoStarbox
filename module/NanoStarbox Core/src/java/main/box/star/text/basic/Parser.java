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
 * implementation through it's {@link #parse(Class, Scanner) static parse}
 * method. Each subclass also has an {@link #parse(Class) instance parse} method, which uses the
 * parser's {@link #scanner} to create and start a new parser as a serial scanner pipeline task.</p>
 * <br>
 * <p>
 *   In a typical parser implementation, there is the notion of an elliptical
 *   list, which is a repeating sequence of inputs, following a particular type
 *   of pattern. To support that, this parser interface provides the
 *   {@link List} class, which can be used to store a list of parser instances
 *   as a list of parsed results.
 * </p>
 * <br>
 * @see Scanner
 */
public class Parser extends Scanner.CancellableOperation {

  protected final static String
      PARSER_DID_NOT_SYNC = ": parser did not synchronize its end result with the scanner state",
      PARSER_DID_NOT_FINISH = ": parser must call finish before it exits",
      PARSER_ALREADY_FINISHED = ": parsing already finished",
      PARSER_QA_BUG = " (parser quality assurance bug)",
      PARSER_CODE_QUALITY_BUG = " (code optimization bug)"
  ;

  protected static final Object[] NO_PARAMETERS = new Object[0];

  // Public Static Class Provisions
  public static class
    List<T extends Parser> extends ArrayList<T> {}

  public static enum
    Status {OK, FAILED}

  // Protected Properties
  protected Object[] parameters;

  // Private Properties
  private long end;
  private boolean finished;
  private Bookmark origin;

  // Public Properties
  public Status status;
  final public boolean successful(){return status.equals(OK);}
  final public boolean isFinished() { return finished; }
  final public long getStart() { return start; }
  final public long getEnd() { return end; }
  final public @NotNull Bookmark getOrigin() { return origin; }
  final public long length(){
    long start = Math.max(0, this.start);
    return (end==0)?scanner.getIndex()-start:end-start;
  }
  final public Scanner getScanner(){return scanner;}

  // Public Methods
  final public @NotNull Bookmark cancel() {
    Bookmark bookmark = scanner.createBookmark();
    if (scanner.getHistoryLength() > 0)
      scanner.walkBack(this.end = this.start);
    this.status = FAILED;
    return bookmark;
  }

  // Public Static Class Provisions
  /**
   * This Interface allows a Parser to specify that upon successful
   * completion the scanner history should be synchronized (flushed) with the
   * current position.
   */
  public static interface NewFuturePromise {}

  // Public Constructors
  public Parser(@NotNull Scanner scanner){
    super(scanner);
    if (scanner.endOfSource()){ status = FAILED; return; }
    this.origin = scanner.createBookmark();
    status = OK;
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
    return scanner.getIndex() != this.end;
  }

  /**
   * A parser must call finish before it exits {@link #start()}
   */
  final protected void finish(){
    if (finished)
      throw new RuntimeException(Parser.class.getName()+PARSER_CODE_QUALITY_BUG, new IllegalStateException(this.getClass().getName()+PARSER_ALREADY_FINISHED));
    this.end = scanner.getIndex();
    this.finished = true;
  }

  /**
   * <p>A class implements this method to begin parsing</p>
   * <br>
   * <p>Each parser is executed using it's {@link #start()} method. The parser
   * is then in command of what to do with that scanner, at the current point
   * within it's stream. It may use this method to start other parsers, or
   * may do any number of things, but it must synchronize with the scanner by the
   * end of it's execution pipe-line.</p>
   */
  protected void start(){
    throw new RuntimeException("this operation is not yet implemented by "+this.getClass().getName());
  };
  protected void start(Object[] parameters){
    if (this instanceof WithParameterPort) throw new RuntimeException("this operation is not yet implemented by "+this.getClass().getName());
    throw new RuntimeException(this.getClass().getName()+" does not host the "+ WithParameterPort.class.getName()+" control port");
  };

  /**
   * <p>A subclass of {@link Parser} implements this control port to directly receive
   * procedure call parameters through {@link #start(Object[])}</p>
   * <br>
   * <p>
   *   If an object doesn't host this control port, the parameters given
   *   may be accessed through {@link #parameters}. This is a backwards
   *   compatibility feature. Parser parameters were not a part of the original
   *   parser design specification. A parser could be designed as a method call
   *   with extensions beyond this specification, and that will require parameter
   *   handling.
   * </p>
   * <br>
   *   <p>Using this control port has the effect that {@link #start()} will
   *   not be called.</p>
   * <br>
   */
  public interface WithParameterPort {}

  protected void call(Object[] parameters){
    if (this instanceof WithParameterPort) start(parameters);
    else {
      this.parameters = parameters;
      start();
    }
  }

  // Public Static Methods

  /**
   * <p>Factory Parse Method</p>
   * <br>
   * <p>This method constructs parsers with a given class
   * The method then executes the parser for it's results. This
   * setup provides between-parser-call scanner method synchronization. A parser
   * cannot return to this method if it's end point is not consistent with the
   * parser's current position, which provides a stream-synchronization-sanity-check
   * </p>
   * <br>
   * @param parserSubclass the parser class reference
   * @param scanner the source scanner
   * @param <T> the subclass specification
   * @return the result of the parser's execution (which may not be successful)
   * @throws IllegalStateException if the parser succeeds but does not correctly finish it's session with the scanner
   */
  public static <T extends Parser> @NotNull T parse(@NotNull Class<T> parserSubclass, @NotNull Scanner scanner) throws IllegalStateException {
    T parser;
    try {
      Constructor<T> classConstructor = parserSubclass.getConstructor(Scanner.class);
      classConstructor.setAccessible(true);
      parser = classConstructor.newInstance(scanner);
    } catch (Exception e){throw new RuntimeException(Parser.class.getName()+PARSER_CODE_QUALITY_BUG, e);}
    if (parser.successful()) {
      parser.call(NO_PARAMETERS);
      if (parser.successful()) {
        if (! parser.isFinished())
          throw new RuntimeException(Parser.class.getName()+PARSER_QA_BUG, new IllegalStateException(parserSubclass.getName()+PARSER_DID_NOT_FINISH));
        else if (parser.isNotSynchronized())
          throw new RuntimeException(Parser.class.getName()+PARSER_QA_BUG, new IllegalStateException(parserSubclass.getName()+PARSER_DID_NOT_SYNC));
        if (parser instanceof NewFuturePromise) scanner.flushHistory();
      }
    }
    return parser;
  }

  /**
   * <p>Factory Parse Method</p>
   * <br>
   * <p>This method constructs parsers with a given class
   * The method then executes the parser for it's results. This
   * setup provides between-parser-call scanner method synchronization. A parser
   * cannot return to this method if it's end point is not consistent with the
   * parser's current position, which provides a stream-synchronization-sanity-check
   * </p>
   * <br>
   * @param parserSubclass the parser class reference
   * @param scanner the source scanner
   * @param parameters the parameters for the subclass
   * @param <T> the subclass specification
   * @return the result of the parser's execution (which may not be successful)
   * @throws IllegalStateException if the parser succeeds but does not correctly finish it's session with the scanner
   */
  public static <T extends Parser> @NotNull T parse(@NotNull Class<T> parserSubclass, @NotNull Scanner scanner, Object... parameters) throws IllegalStateException {
    T parser;
    try {
      Constructor<T> classConstructor = parserSubclass.getConstructor(Scanner.class);
      classConstructor.setAccessible(true);
      parser = classConstructor.newInstance(scanner);
    } catch (Exception e){throw new RuntimeException(Parser.class.getName()+PARSER_CODE_QUALITY_BUG, e);}
    if (parser.successful()) {
      parser.call(parameters);
      if (parser.successful()) {
        if (! parser.isFinished())
          throw new RuntimeException(Parser.class.getName()+PARSER_QA_BUG, new IllegalStateException(parserSubclass.getName()+PARSER_DID_NOT_FINISH));
        else if (parser.isNotSynchronized())
          throw new RuntimeException(Parser.class.getName()+PARSER_QA_BUG, new IllegalStateException(parserSubclass.getName()+PARSER_DID_NOT_SYNC));
        if (parser instanceof NewFuturePromise) scanner.flushHistory();
      }
    }
    return parser;
  }

  // Protected Method (located here for mirroring with static method above)

  /**
   * <p>Factory Parse Method: serial scanner pipeline task</p>
   * <br>
   * <p>This method constructs serial pipeline [co]parsers with a given class.
   * The method then executes the parser for it's results. This
   * setup provides between-parser-call scanner method synchronization. A parser
   * cannot return to this method if it's end point is not consistent with the
   * parser's current position, which provides a stream-synchronization-sanity-check
   * </p>
   * <br>
   * @param parserSubclass the parser class reference
   * @param <T> the subclass specification
   * @return the result of the parser's execution (which may not be successful)
   * @throws IllegalStateException if the parser succeeds but does not correctly finish it's session with the scanner
   */
  protected <T extends Parser> @NotNull T parse(@NotNull Class<T> parserSubclass){
    T parser;
    try {
      Constructor<T> classConstructor = parserSubclass.getConstructor(Scanner.class);
      classConstructor.setAccessible(true);
      parser = classConstructor.newInstance(scanner);
    } catch (Exception e){throw new RuntimeException(this.getClass().getName()+PARSER_CODE_QUALITY_BUG, e);}
    if (parser.successful()) {
      parser.call(NO_PARAMETERS);
      if (parser.successful()) {
        if (! parser.isFinished())
          throw new RuntimeException(this.getClass().getName()+PARSER_QA_BUG, new IllegalStateException(parserSubclass.getName()+PARSER_DID_NOT_FINISH));
        else if (parser.isNotSynchronized())
          throw new RuntimeException(this.getClass().getName()+PARSER_QA_BUG, new IllegalStateException(parserSubclass.getName()+PARSER_DID_NOT_SYNC));
        if (parser instanceof NewFuturePromise) scanner.flushHistory();
      }
    }
    return parser;
  }

  /**
   * <p>Factory Parse Method: serial scanner pipeline task</p>
   * <br>
   * <p>This method constructs serial pipeline [co]parsers with a given class.
   * The method then executes the parser for it's results. This
   * setup provides between-parser-call scanner method synchronization. A parser
   * cannot return to this method if it's end point is not consistent with the
   * parser's current position, which provides a stream-synchronization-sanity-check
   * </p>
   * <br>
   * @param parserSubclass the parser class reference
   * @param parameters the parameters for the subclass
   * @param <T> the subclass specification
   * @return the result of the parser's execution (which may not be successful)
   * @throws IllegalStateException if the parser succeeds but does not correctly finish it's session with the scanner
   */
  protected <T extends Parser> @NotNull T parse(@NotNull Class<T> parserSubclass, Object... parameters){
    T parser;
    try {
      Constructor<T> classConstructor = parserSubclass.getConstructor(Scanner.class);
      classConstructor.setAccessible(true);
      parser = classConstructor.newInstance(scanner);
    } catch (Exception e){throw new RuntimeException(this.getClass().getName()+PARSER_CODE_QUALITY_BUG, e);}
    if (parser.successful()) {
      parser.call(parameters);
      if (parser.successful()) {
        if (! parser.isFinished())
          throw new RuntimeException(this.getClass().getName()+PARSER_QA_BUG, new IllegalStateException(parserSubclass.getName()+PARSER_DID_NOT_FINISH));
        else if (parser.isNotSynchronized())
          throw new RuntimeException(this.getClass().getName()+PARSER_QA_BUG, new IllegalStateException(parserSubclass.getName()+PARSER_DID_NOT_SYNC));
        if (parser instanceof NewFuturePromise) scanner.flushHistory();
      }
    }
    return parser;
  }

}
