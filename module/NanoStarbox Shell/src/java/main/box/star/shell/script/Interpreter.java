package box.star.shell.script;

import box.star.contract.NotNull;
import box.star.text.basic.Parser;
import box.star.text.basic.Scanner;

import java.lang.reflect.Constructor;

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
public class Interpreter extends box.star.text.basic.Parser {

  public Interpreter(@NotNull Scanner scanner) { super(scanner); }

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

  /**
   * <p>Factory Parse Method</p>
   * <br>
   * <p>This method constructs parsers with a given class
   * and scanner. The method then {@link #start() executes} the parser for it's results. This
   * setup provides between-parser-call scanner method synchronization. A parser
   * cannot return to this method if it's end point is not consistent with the
   * parser's current position, which provides a boundary over-read-sanity-check
   * </p>
   * <br>
   * @param parserSubclass the parser class reference
   * @param scanner the source scanner
   * @param <T> the subclass specification
   * @return the result of the parser's execution (which may not be successful)
   * @throws IllegalStateException if the parser succeeds but does not correctly finish it's session with the scanner
   */
  public static <T extends Parser> @NotNull T parse(@NotNull Class<T> parserSubclass, @NotNull Scanner scanner) throws IllegalStateException {
    Interpreter parser;
    try {
      Constructor<T> classConstructor = parserSubclass.getConstructor(Scanner.class);
      classConstructor.setAccessible(true);
      parser = (Interpreter) classConstructor.newInstance(scanner);
    } catch (Exception e){throw new RuntimeException(Parser.class.getName()+PARSER_CODE_QUALITY_BUG, e);}
    if (parser.successful()) {
      parser.start();
      if (parser.successful()) {
        if (! parser.isFinished())
          throw new RuntimeException(Parser.class.getName()+PARSER_QA_BUG, new IllegalStateException(parserSubclass.getName()+PARSER_DID_NOT_FINISH));
        else if (parser.isNotSynchronized())
          throw new RuntimeException(Parser.class.getName()+PARSER_QA_BUG, new IllegalStateException(parserSubclass.getName()+PARSER_DID_NOT_SYNC));
        if (parser instanceof NewFuturePromise) scanner.flushHistory();
      }
    }
    return (T) parser;
  }

}

