package box.star.shell.script;

import box.star.text.basic.Scanner;

public class CommandGroup extends Interpreter {
  CommandList commands;
  public CommandGroup(Scanner scanner) {
    super(scanner);
  }
}
