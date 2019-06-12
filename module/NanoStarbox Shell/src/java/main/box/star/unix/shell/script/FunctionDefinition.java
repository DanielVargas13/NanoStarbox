package box.star.unix.shell.script;

import box.star.contract.NotNull;
import box.star.text.basic.Scanner;

public class FunctionDefinition extends SourceElement {
  public String commandName;
  public CommandGroup body;
  public RedirectionList redirects;
  public FunctionDefinition(@NotNull Scanner scanner) {
    super(scanner);
  }
}
