package box.star.shell.runtime;

import box.star.text.Char;
import box.star.text.basic.Bookmark;
import box.star.text.basic.Scanner;
import box.star.text.basic.ScannerDriver;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import static box.star.shell.runtime.TextRecord.Status.*;

/**
 * <p>The Shell Runtime TextRecord</p>
 * <br>
 *   <p>A text record contains an optimized representation of a shell script, or
 * one of it's constituent components. </p>
 *  <br>
 *   <h4>Class Design Goals</h4>
 *   <ol>
 *     <li>Consistent Syntax Handling</li>
 *     <li>Shell Context Modeling Struts</li>
 *   </ol>
 * <br>
 * <p>Each text model forms or conforms with the struts about which each shell
 * execution context interface is built.</p>
 * <br>
 * <p>A text record does not perform any expansions, evaluations, or interact
 * with any shell specific environment settings.</p>
 * <br>
 * <p>{@link Type} lists a manually curated enumeration for each script element
 * kind of this class.</p>
 * <br>
 */
public abstract class TextRecord {

  /**
   * This Interface allows a TextRecord to specify that upon successful
   * completion the scanner history should be synchronized (flushed) with the
   * current position.
   */
  public interface WithAutoFlush {}

  public static class List<T extends TextRecord> extends ArrayList<T>{}
  
  public enum Type {
    TEXT_RECORD_TYPE_SHEBANG,
    TEXT_RECORD_TYPE_MAIN,
    TEXT_RECORD_TYPE_CHILD,
    TEXT_RECORD_TYPE_COMMAND_GROUP,
    TEXT_RECORD_TYPE_COMMENT,
    TEXT_RECORD_TYPE_ENVIRONMENT_OPERATION,
    TEXT_RECORD_TYPE_COMMAND,
    TEXT_RECORD_TYPE_PARAMETER,
    TEXT_RECORD_TYPE_PARAMETER_TEXT,
    TEXT_RECORD_TYPE_PARAMETER_LITERAL,
    TEXT_RECORD_TYPE_PARAMETER_QUOTED,
    TEXT_RECORD_TYPE_REDIRECT,
    TEXT_RECORD_TYPE_HERE_DOCUMENT
  }
  
  public enum ListType {
    TEXT_RECORD_LIST_TYPE_ENVIRONMENT_OPERATION_LIST,
    TEXT_RECORD_LIST_TYPE_PARAMETER_LIST,
    TEXT_RECORD_LIST_TYPE_REDIRECT_LIST,
    TEXT_RECORD_LIST_TYPE_COMMAND_LIST
  }

  public enum Status {OK, FAILED}

  protected Scanner scanner;
  private Bookmark origin;
  private long start, end;
  private Status status;
  private boolean finished;

  public TextRecord(Scanner scanner){
    if (scanner.endOfSource()){ status = FAILED; return; }
    this.scanner = scanner;
    this.origin = scanner.nextBookmark();
    start = origin.index - 1;
    status = OK;
  }

  final public boolean successful(){return status.equals(OK);}

  final protected Bookmark cancel() {
    if (this instanceof WithAutoFlush){
      throw new IllegalStateException("cannot cancel " + getClass().getName() +
          "; task is Final");
    }
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
  protected final boolean isNotSynchronized(){
    return ((scanner.endOfSource()?-1:0))+scanner.getIndex() != this.end;
  }

  public boolean isFinished() {
    return finished;
  }

  final protected void finish(){
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

  /**
   * <p>Common TextRecord Parser Factory Parse Method</p>
   * <br>
   * <p>This method constructs TextRecord parsers with a given TextRecord class
   * and scanner. The method then executes the parser for it's results. This
   * setup provides between-parser-call scanner method synchronization. A parser
   * cannot return to this method if it's end point is not consistent with the
   * parser's current position, which provides a boundary over-read-sanity-check
   * </p>
   * <br>
   * <p>Each parser is executed using it's {@link #start()} method. The parser
   * is then in command of what to do with that scanner, at the current point
   * within it's stream. It may use this method to start other parsers, or
   * may do any number of things, but it must synchronize with the parser by the
   * end of its execution pipe-line.</p>
   *
   * @param subclass the TextRecord parser class reference
   * @param scanner the source scanner
   * @param <T> the TextRecord subclass specification
   * @return the result of the parser's execution (which may not be successful)
   * @throws IllegalStateException if the parser succeeds but does not correctly finish it's session with the scanner
   */
  public final static <T extends TextRecord> T parse(Class<T> subclass, Scanner scanner) throws IllegalStateException {
    T textRecord;
    try {
      Constructor<T> ctor = subclass.getConstructor(Scanner.class);
      ctor.setAccessible(true);
      textRecord = ctor.newInstance(scanner);
    } catch (Exception e){throw new RuntimeException(e);}
    if (textRecord.successful()) {
      textRecord.start();
      if (! textRecord.isFinished())
        throw new IllegalStateException(subclass.getName()+" did not finish parsing");
      else if (textRecord.isNotSynchronized())
        throw new IllegalStateException(subclass.getName()+" did not synchronize its end result with the scanner state");
      if (textRecord instanceof WithAutoFlush) scanner.flushHistory();
    }
    return textRecord;
  }

  /**
   * <p>A class implements this method to begin parsing</p>
   * @see Main#start()
   */
  protected void start(){}

  // Types

  /**
   * <p>Main TextRecord</p>
   * <br>
   * <p>{@link Main} looks for a specific shell program word and processes that word,
   * using the associated {@link TextRecord} sub-class. Additionally, this
   * class is responsible for parsing white-space-characters and unidentified garbage.</p>
   * <br>
   * @see box.star.shell.runtime.TextRecord#parse(Class, Scanner)  Parsing Text Records with the TextRecord class
   * @see box.star.text.basic.Scanner
   * @see box.star.text.basic.ScannerDriver
   */
  public static class Main extends TextRecord implements ScannerDriver.WithBufferControlPort, WithAutoFlush {
    List records = new List();
    public Main(Scanner scanner) {
      super(scanner);
    }
    @Override
    public boolean collect(Scanner scanner, StringBuilder buffer, char character) {
      if (scanner.endOfSource()){
        finish();
        return false;
      }
      if (Char.mapContains(character, Char.MAP_ASCII_ALL_WHITE_SPACE)){
        return true;
      }
      if (Char.mapContains(character, Char.MAP_ASCII_NUMBERS)){
        scanner.flagThisCharacterSyntaxError("identifier or command");
      }
      switch (character){
        case '#': {
          boolean bang = scanner.getLine() == 1 && scanner.getColumn() == 1;
          if (bang){
            Shebang line = parse(Shebang.class, scanner);
            if (line.successful()) records.add(line);
          } else {
            Comment comment = parse(Comment.class, scanner);
            if (comment.successful()) records.add(comment);
          }
          break;
        }
        case '(': {
          Child child = parse(Child.class, scanner);
          if (child.successful()) records.add(child);
          break;
        }
        case '{': {
          CommandGroup list = parse(CommandGroup.class, scanner);
          if (list.successful()) records.add(list);
          break;
        }
        default:
          scanner.flagThisCharacterSyntaxError("shell command");
      }
      this.finish();
      return true;
    }
    /**
     * <p>Calls the scanner to begin main text record assembly</p>
     * @see #collect(Scanner, StringBuilder, char)
     */
    @Override
    protected void start() { scanner.assemble(this); }
  }
  public static class Child extends Main {
    Child(Scanner scanner) {
      super(scanner);
    }
  }
  public static class CommandGroup extends TextRecord {
    CommandList commands;
    public CommandGroup(Scanner scanner) {
      super(scanner);
    }
  }
  public static class Comment extends TextRecord implements WithAutoFlush {
    protected String text;
    public Comment(Scanner scanner) {
      super(scanner);
    }
    public String getText() {
      return text;
    }
    @Override
    public void start() {
      char c = scanner.current();
      if (c != '#') { cancel(); return; }
      this.text = c+scanner.nextField('\0', '\n');
      scanner.nextCharacter("comment line ending", '\n');
      finish();
      return;
    }
  }
  public static class Shebang extends Comment {
    public Shebang(Scanner scanner) {
      super(scanner);
    }
    String getParameterString(){
      String[] data = text.split("\\s", 2);
      return data[data.length - 1];
    }
  }
  public static class EnvironmentOperation extends TextRecord {
    EnvironmentOperation(Scanner scanner) {
      super(scanner);
    }
  }
  public static class Command extends TextRecord implements WithAutoFlush {
    protected EnvironmentOperationList environmentOperations;
    protected ParameterList parameters;
    protected RedirectList redirects;
    protected Command pipe;
    public Command(Scanner scanner) {
      super(scanner);
    }
  }
  static abstract class Parameter extends TextRecord {
    public static enum QuoteType {
      NOT_QUOTING, SINGLE_QUOTING, DOUBLE_QUOTING
    }
    protected QuoteType quoteType;
    protected String text;
    Parameter(Scanner scanner) {
      this(scanner, QuoteType.NOT_QUOTING);
    }
    Parameter(Scanner scanner, QuoteType quoteType){
      super(scanner);
      this.quoteType = quoteType;
    }
    private final String extractText(){
      return text.substring(1, text.length()-1);
    }
    public String getText() {
      switch (quoteType) {
        case NOT_QUOTING: return text;
        default: return extractText();
      }
    }
    @Override
    public String toString() {
      return text;
    }
  }
  public static class ParameterText extends Parameter {
    public ParameterText(Scanner scanner, QuoteType quoting) {
      super(scanner, quoting);
    }
  }
  public static class ParameterLiteral extends ParameterText implements WithAutoFlush {
    ParameterLiteral(Scanner scanner) {
      super(scanner, QuoteType.SINGLE_QUOTING);
    }
  }
  public static class ParameterQuoted extends ParameterText implements WithAutoFlush {
    ParameterQuoted(Scanner scanner) {
      super(scanner, QuoteType.DOUBLE_QUOTING);
    }
  }
  public static class Redirect extends Parameter implements WithAutoFlush {
    Redirect(Scanner scanner) {
      super(scanner, QuoteType.NOT_QUOTING);
    }
  }
  public static class HereDocument extends Parameter implements WithAutoFlush {
    HereDocument(Scanner scanner) {
      super(scanner);
    }
  }

  // Lists
  public static class EnvironmentOperationList extends TextRecord.List<EnvironmentOperation> {}
  public static class ParameterList extends TextRecord.List<Parameter> {}
  public static class RedirectList extends TextRecord.List<Redirect> {}
  public static class CommandList extends TextRecord.List<Command> {}

}
