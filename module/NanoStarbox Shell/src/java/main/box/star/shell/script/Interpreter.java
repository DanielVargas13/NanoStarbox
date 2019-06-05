package box.star.shell.script;

import box.star.Tools;
import box.star.contract.NotNull;
import box.star.lang.Char;
import box.star.lang.SyntaxError;
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

  public static ParameterList parseParameterList(Scanner scanner){
    ParameterList parameters = new ParameterList();
    if (scanner.haveNext()) do {
      Parameter parameter = parse(Parameter.class, scanner);
      if (parameter.status.equals(Status.OK)) { parameters.add(parameter); }
      else { break; }
    } while (true);
    return parameters;
  }

  public static CommandList parseCommandList(Scanner scanner){
    CommandList parameters = new CommandList();
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

  public static CommandList parseSubShell(Scanner scanner){
    CommandList parameters = new CommandList();
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

  public static EnvironmentOperationList parseEnvironmentOperationList(Scanner scanner){
    EnvironmentOperationList operationList = new EnvironmentOperationList();
    while (! scanner.endOfSource()) {
      EnvironmentOperation op = Interpreter.parse(EnvironmentOperation.class, scanner);
      if (op.status == Status.OK) operationList.add(op);
      else break;
    }
    return operationList;
  }

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

  public static class ParameterList extends List<Parameter> {}
  public static class CommandList extends List<Command> {}
  public static class RedirectList extends List<Redirect> {}
  public static class EnvironmentOperationList extends List<EnvironmentOperation> {}

}

