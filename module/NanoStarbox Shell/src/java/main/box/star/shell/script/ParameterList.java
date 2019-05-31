package box.star.shell.script;

import box.star.text.basic.Parser;
import box.star.text.basic.Scanner;

public class ParameterList extends Parser.List<Parameter> {
  public static ParameterList parse(Scanner scanner){
    ParameterList parameters = new ParameterList();
    if (scanner.haveNext()) do {
      Parameter parameter = Interpreter.parse(Parameter.class, scanner);
      if (parameter.status.equals(Parser.Status.OK)) { parameters.add(parameter); }
      else { break; }
    } while (true);
    return parameters;
  }
}
