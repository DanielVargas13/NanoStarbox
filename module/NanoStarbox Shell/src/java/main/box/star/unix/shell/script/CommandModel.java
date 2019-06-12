package box.star.unix.shell.script;

import box.star.contract.NotNull;
import box.star.shell.script.Command;
import box.star.text.basic.Scanner;

abstract class CommandModel extends SourceElement {

  public VariableOperationList environmentOperations;
  public RedirectionList redirects;
  public String terminator;
  public Command next;

  CommandModel(@NotNull Scanner scanner) {
    super(scanner);
  }

}
