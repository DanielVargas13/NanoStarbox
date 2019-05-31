package box.star.shell.script;

import box.star.contract.NotNull;
import box.star.text.basic.Scanner;

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

  public static class CommandList extends List<Command> {}

  public static class ParameterList extends List<Parameter> {
    public static ParameterList parse(Scanner scanner){
      ParameterList parameters = new ParameterList();
      if (scanner.haveNext()) do {
        Parameter parameter = Interpreter.parse(Parameter.class, scanner);
        if (parameter.status.equals(Status.OK)) { parameters.add(parameter); }
        else { break; }
      } while (true);
      return parameters;
    }
  }

  public static class RedirectList extends List<Redirect> {}

  public static class EnvironmentOperationList extends List<EnvironmentOperation> {
    public static EnvironmentOperationList parse(Scanner scanner){
      EnvironmentOperationList operationList = new EnvironmentOperationList();
      while (! scanner.endOfSource()) {
        EnvironmentOperation op = Interpreter.parse(EnvironmentOperation.class, scanner);
        if (op.status == Status.OK) operationList.add(op);
        else break;
      }
      return operationList;
    }
  }
}

