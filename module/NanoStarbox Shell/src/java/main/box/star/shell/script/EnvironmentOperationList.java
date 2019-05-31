package box.star.shell.script;

import box.star.text.basic.Parser;
import box.star.text.basic.Scanner;

public class EnvironmentOperationList extends Parser.List<EnvironmentOperation> {
  public static EnvironmentOperationList parse(Scanner scanner){
    EnvironmentOperationList operationList = new EnvironmentOperationList();
    while (! scanner.endOfSource()) {
      EnvironmentOperation op = Interpreter.parse(EnvironmentOperation.class, scanner);
      if (op.status == Parser.Status.OK) operationList.add(op);
      else break;
    }
    return operationList;
  }
}
