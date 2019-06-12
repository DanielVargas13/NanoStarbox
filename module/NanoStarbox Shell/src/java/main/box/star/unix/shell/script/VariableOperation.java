package box.star.unix.shell.script;

import box.star.contract.NotNull;
import box.star.text.basic.Scanner;

public class VariableOperation extends SourceElement {
  public String operation;
  public Parameter variable, value;
  public VariableOperation(@NotNull Scanner scanner) {
    super(scanner);
  }
}
