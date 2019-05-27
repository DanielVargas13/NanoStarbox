package box.star.shell.runtime;

import box.star.text.Char;
import box.star.text.basic.Bookmark;
import box.star.text.basic.Scanner;
import box.star.text.basic.ScannerDriver;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

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
   * The Final Interface allows a TextRecord to specify that navigating backward
   * from it's completion is not possible
   */
  public interface Final {}

  public enum Type {
    TEXT_RECORD_TYPE_SHEBANG,
    TEXT_RECORD_TYPE_MAIN,
    TEXT_RECORD_TYPE_CHILD,
    TEXT_RECORD_TYPE_COMMENT,
    TEXT_RECORD_TYPE_ENVIRONMENT_OPERATION,
    TEXT_RECORD_TYPE_COMMAND,
    TEXT_RECORD_TYPE_PARAMETER,
    TEXT_RECORD_TYPE_PARAMETER_TEXT,
    TEXT_RECORD_TYPE_PARAMETER_LITERAL,
    TEXT_RECORD_TYPE_PARAMETER_QUOTED,
    TEXT_RECORD_TYPE_PARAMETER_LIST,
    TEXT_RECORD_TYPE_REDIRECT,
    TEXT_RECORD_TYPE_COMMAND_LIST,
    TEXT_RECORD_TYPE_HERE_DOCUMENT
  }

  public enum Status {OK, FAILED}

  protected Scanner scanner;
  private Bookmark origin;
  private long start, end;
  private Status status;

  public TextRecord(Scanner scanner){
    if (scanner.endOfSource()){ status = FAILED; return; }
    this.scanner = scanner;
    this.origin = scanner.nextBookmark();
    start = origin.index - 1;
    status = OK;
  }

  final public boolean success(){return status.equals(OK);}

  final protected Bookmark cancel() {
    if (this instanceof Final){
      throw new IllegalStateException("cannot cancel " + getClass().getName() +
          "; task is Final");
    }
    Bookmark bookmark = scanner.createBookmark();
    scanner.walkBack(this.end = this.start);
    this.status = FAILED;
    return bookmark;
  }

  final protected boolean hasEnding(){
    return end == 0;
  }

  final protected void finish(){
    this.end = scanner.getIndex();
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
    return (end==0)?scanner.getIndex()-start:end-start;
  }

  public final static <T extends TextRecord> T parse(Class<T> recordClass, Scanner scanner) {
    T textRecord;
    try {
      Constructor<T> ctor = recordClass.getConstructor(Scanner.class);
      ctor.setAccessible(true);
      textRecord = ctor.newInstance(scanner);
    } catch (Exception e){throw new RuntimeException(e);}
    if (textRecord.success()) {
      textRecord.start();
      if (textRecord.success() && textRecord.hasEnding()){
        throw new IllegalStateException("text record acquisition for "+recordClass.getName()+" did not complete");
      }
      if (textRecord instanceof Final) scanner.flushHistory();
    }
    return textRecord;
  }

  protected void start(){}

  static public class Main extends TextRecord implements ScannerDriver.WithBufferControlPort, Final {
    List<TextRecord> records = new ArrayList<>();
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
            if (line.success()) records.add(line);
          } else {
            Comment comment = parse(Comment.class, scanner);
            if (comment.success()) records.add(comment);
          }
          break;
        }
        case '(': {
          Child child = parse(Child.class, scanner);
          if (child.success()) records.add(child);
          break;
        }
        case '{': {
          CommandList list = parse(CommandList.class, scanner);
          if (list.success()) records.add(list);
          break;
        }
        default:
          scanner.flagThisCharacterSyntaxError("shell command");
      }
      this.finish();
      return true;
    }
    @Override
    protected void start() { scanner.assemble(this); }
  }
  static public class Child extends Main {
    Child(Scanner scanner) {
      super(scanner);
    }
  }
  static public class Comment extends TextRecord implements Final {
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
  static public class Shebang extends Comment {
    public Shebang(Scanner scanner) {
      super(scanner);
    }
    String getParameterString(){
      String[] data = text.split("\\s", 2);
      return data[data.length - 1];
    }
  }
  static public class EnvironmentOperation extends TextRecord {
    EnvironmentOperation(Scanner scanner) {
      super(scanner);
    }
  }
  static public class Command extends TextRecord implements Final {
    Command(Scanner scanner) {
      super(scanner);
    }
  }
  static public abstract class Parameter extends TextRecord {
    Parameter(Scanner scanner) {
      super(scanner);
    }
  }
  public static class ParameterText extends Parameter {
    ParameterText(Scanner scanner) {
      super(scanner);
    }
  }
  public static class ParameterLiteral extends Parameter implements Final {
    ParameterLiteral(Scanner scanner) {
      super(scanner);
    }
  }
  public static class ParameterQuoted extends Parameter implements Final {
    ParameterQuoted(Scanner scanner) {
      super(scanner);
    }
  }
  public static class ParameterList extends TextRecord implements Final {
    ParameterList(Scanner scanner) {
      super(scanner);
    }
  }
  public static class Redirect extends ParameterText implements Final {
    Redirect(Scanner scanner) {
      super(scanner);
    }
  }
  public static class CommandList extends TextRecord {
    CommandList(Scanner scanner) {
      super(scanner);
    }
  }
  public static class HereDocument extends TextRecord implements Final {
    HereDocument(Scanner scanner) {
      super(scanner);
    }
  }
}
