package box.star.shell;

import box.star.Tools;
import box.star.contract.NotNull;
import box.star.lang.Char;
import box.star.shell.script.Command;
import box.star.shell.script.Parameter;
import box.star.shell.script.Redirect;
import box.star.shell.script.content.DataOperation;
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
 * <br>
 */
public class ScriptParser extends Parser {

  public ScriptParser(@NotNull Scanner scanner) { super(scanner); }

  public static ParameterList parseParameterList(Scanner scanner){
    ParameterList parameters = new ParameterList();
    if (scanner.haveNext()) do {
      Parameter parameter = parse(Parameter.class, scanner);
      if (parameter.status.equals(Status.OK)) { parameters.add(parameter); }
      else { break; }
    } while (true);
    return parameters;
  }

  public static CommandSet parseCommandGroup(Scanner scanner){
    CommandSet parameters = new CommandSet();
    if (scanner.current() != '{')
      throw new Scanner.SyntaxError(parameters, scanner, "expected command group symbol");
    if (scanner.haveNext()) do {
      String space = scanner.nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
      if (Char.mapContains(scanner.next(), ')', '}')) {
        if (scanner.current() == '}') {
          if (space.indexOf('\n') < 0) {
            if (space.equals(Tools.EMPTY_STRING)) {
              scanner.back();
              scanner.next(' ');
            }
            scanner.back(space.length() + 2);
            scanner.nextMap(1, 1, ';', '&');
            break;
          }
          scanner.back();
          break;
        } else {
          throw new Scanner.SyntaxError(parameters, scanner, "illegal symbol: "+scanner.current());
        }
      } else scanner.escape();
      Command command = parse(Command.class, scanner);
      if (command.status.equals(Status.OK)) {
        parameters.add(command);
        if ("\n".equals(command.terminator)) scanner.back();
      }
      else { break; }
    } while (true);
    scanner.nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
    scanner.next('}');
    return parameters;
  }

  public static CommandSet parseCommandShell(Scanner scanner){
    CommandSet parameters = new CommandSet();
    if (scanner.current() != '(')
      throw new Scanner.SyntaxError(parameters, scanner, "expected command shell symbol");
    if (scanner.haveNext()) do {
      String space = scanner.nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
      if (Char.mapContains(scanner.next(), ')', '}')) {
        if (scanner.current() == ')') {
          scanner.back();
          break;
        } else {
          throw new Scanner.SyntaxError(parameters, scanner, "illegal symbol: "+scanner.current());
        }
      } else scanner.escape();
      Command command = parse(Command.class, scanner);
      if (command.status.equals(Status.OK)) {
        parameters.add(command);
        if ("\n".equals(command.terminator)) scanner.back();
      }
      else { break; }
    } while (true);
    scanner.nextMap(Char.MAP_ASCII_ALL_WHITE_SPACE);
    scanner.next(')');
    return parameters;
  }

  public static RedirectList parseParameterRedirectList(Scanner scanner){
    RedirectList redirects = new RedirectList();
    if (scanner.haveNext()) do {
      Redirect redirect = parse(Redirect.class, scanner);
      if (redirect.status.equals(Status.OK)) { redirects.add(redirect); }
      else { break; }
    } while (true);
    return redirects;
  }

  public static DataOperationList parseDataOperationList(Scanner scanner){
    DataOperationList operationList = new DataOperationList();
    while (! scanner.endOfSource()) {
      DataOperation op = ScriptParser.parse(DataOperation.class, scanner);
      if (op.status == Status.OK) operationList.add(op);
      else break;
    }
    return operationList;
  }

  /**
   * <p>Factory Parse Method</p>
   * <br>
   * <p>This method constructs parsers with a given class
   * and scanner. The method then {@link #start() executes} the parser for it's results. This
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
  public static <T extends box.star.text.basic.Parser> @NotNull T parse(@NotNull Class<T> parserSubclass, @NotNull Scanner scanner) throws IllegalStateException {
    ScriptParser parser;
    try {
      Constructor<T> classConstructor = parserSubclass.getConstructor(Scanner.class);
      classConstructor.setAccessible(true);
      parser = (ScriptParser) classConstructor.newInstance(scanner);
    } catch (Exception e){throw new RuntimeException(ScriptParser.class.getName()+PARSER_CODE_QUALITY_BUG, e);}
    if (parser.successful()) {
      parser.call(NO_PARAMETERS);
      if (parser.successful()) {
        if (! parser.isFinished())
          throw new RuntimeException(ScriptParser.class.getName()+PARSER_QA_BUG, new IllegalStateException(parserSubclass.getName()+PARSER_DID_NOT_FINISH));
        else if (parser.isNotSynchronized())
          throw new RuntimeException(ScriptParser.class.getName()+PARSER_QA_BUG, new IllegalStateException(parserSubclass.getName()+PARSER_DID_NOT_SYNC));
        if (parser instanceof NewFuturePromise) scanner.flushHistory();
      }
    }
    return (T) parser;
  }

  /**
   * <p>Factory Parse Method</p>
   * <br>
   * <p>This method constructs parsers with a given class
   * and scanner. The method then {@link #start() executes} the parser for it's results. This
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
  public static <T extends box.star.text.basic.Parser> @NotNull T parse(@NotNull Class<T> parserSubclass, @NotNull Scanner scanner, Object... parameters) throws IllegalStateException {
    ScriptParser parser;
    try {
      Constructor<T> classConstructor = parserSubclass.getConstructor(Scanner.class);
      classConstructor.setAccessible(true);
      parser = (ScriptParser) classConstructor.newInstance(scanner);
    } catch (Exception e){throw new RuntimeException(ScriptParser.class.getName()+PARSER_CODE_QUALITY_BUG, e);}
    if (parser.successful()) {
      parser.call(parameters);
      if (parser.successful()) {
        if (! parser.isFinished())
          throw new RuntimeException(ScriptParser.class.getName()+PARSER_QA_BUG, new IllegalStateException(parserSubclass.getName()+PARSER_DID_NOT_FINISH));
        else if (parser.isNotSynchronized())
          throw new RuntimeException(ScriptParser.class.getName()+PARSER_QA_BUG, new IllegalStateException(parserSubclass.getName()+PARSER_DID_NOT_SYNC));
        if (parser instanceof NewFuturePromise) scanner.flushHistory();
      }
    }
    return (T) parser;
  }

  public static class ParameterList extends List<Parameter> {}
  public static class CommandSet extends List<Command> {}
  public static class RedirectList extends List<Redirect> {}
  public static class DataOperationList extends List<DataOperation> {}

}

