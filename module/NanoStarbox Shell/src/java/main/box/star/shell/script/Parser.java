package box.star.shell.script;

import box.star.contract.NotNull;
import box.star.text.basic.Scanner;

import static box.star.text.Char.*;

/**
 * <p>The Shell Script Parser</p>
 * <br>
 *   <p>A script parser compiles and represents an optimized representation of
 *   a shell script, or one of it's constituent components.</p>
 *  <br>
 *   <h4>Class Design Goals</h4>
 *   <ol>
 *     <li>Consistent Syntax Handling</li>
 *     <li>Shell Context Modeling Struts</li>
 *   </ol>
 * <br>
 * <p>Each parser model forms or conforms with the struts about which each shell
 * execution context interface is built.</p>
 * <br>
 * <p>A script parser does not perform any expansions, evaluations, or interact
 * with any shell specific environment settings. In short, these parsers
 * interpret the script components, and the shell contexts interpret these
 * models.</p>
 * <br>
 * <p>{@link Type} lists a manually curated enumeration for each parser model
 * kind of this class and {@link ListType} lists a manually curated enumeration
 * for each parser list model kind of this class.</p>
 * <br>
 */
public class Parser extends box.star.text.basic.Parser {

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

  public Parser(@NotNull Scanner scanner) { super(scanner); }

  /**
   * <p>Main Parser</p>
   * <br>
   * <p>{@link Main} looks for a specific shell program word and processes that word,
   * using the associated {@link Parser} sub-class. Additionally, this
   * class is responsible for parsing white-space-characters and unidentified garbage.</p>
   * <br>
   * @see Parser#parse(Class, Scanner)  Parsing Text Records with the Parser class
   * @see box.star.text.basic.Scanner
   * @see Scanner.SourceDriver
   */
  public static class Main extends Parser 
      implements NewFuturePromise, Scanner.SourceDriver.WithBufferControlPort {
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
      if (mapContains(character, MAP_ASCII_ALL_WHITE_SPACE)){
        return true;
      }
      if (mapContains(character, MAP_ASCII_NUMBERS)){
        throw new SyntaxError(this, "expected command found digits");
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
          throw new SyntaxError(this, "expected shell command");
      }
      this.finish();
      return true;
    }
    /**
     * <p>Calls the scanner to begin main script parser assembly</p>
     * @see #collect(Scanner, StringBuilder, char)
     */
    @Override
    protected void start() { scanner.run(this); }
  }
  public static class Child extends Main {
    Child(Scanner scanner) {
      super(scanner);
    }
  }
  public static class CommandGroup extends Parser {
    CommandList commands;
    public CommandGroup(Scanner scanner) {
      super(scanner);
    }
  }
  public static class Comment extends Parser implements NewFuturePromise {
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
      this.text = c+scanner.nextLine();
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
  public static class EnvironmentOperation extends Parser {
    EnvironmentOperation(Scanner scanner) {
      super(scanner);
    }
  }
  public static class Command extends Parser implements NewFuturePromise {
    protected EnvironmentOperationList environmentOperations;
    protected ParameterList parameters;
    protected RedirectList redirects;
    protected Command pipe;
    public Command(Scanner scanner) {
      super(scanner);
    }
  }
  static abstract class Parameter extends Parser {
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
  public static class ParameterLiteral extends ParameterText implements NewFuturePromise {
    ParameterLiteral(Scanner scanner) {
      super(scanner, QuoteType.SINGLE_QUOTING);
    }
  }
  public static class ParameterQuoted extends ParameterText implements NewFuturePromise {
    ParameterQuoted(Scanner scanner) {
      super(scanner, QuoteType.DOUBLE_QUOTING);
    }
  }
  public static class Redirect extends Parameter implements NewFuturePromise {
    Redirect(Scanner scanner) {
      super(scanner, QuoteType.NOT_QUOTING);
    }
  }
  public static class HereDocument extends Parameter implements NewFuturePromise {
    HereDocument(Scanner scanner) {
      super(scanner);
    }
  }

  public static class EnvironmentOperationList extends List<EnvironmentOperation> {}
  public static class ParameterList extends List<Parameter> {}
  public static class RedirectList extends List<Redirect> {}
  public static class CommandList extends List<Command> {}

}
