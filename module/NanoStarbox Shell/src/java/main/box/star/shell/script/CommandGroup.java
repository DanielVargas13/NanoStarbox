package box.star.shell.script;

import box.star.text.basic.Scanner;

public class CommandGroup extends Command {

  public CommandSet commands;

  public CommandGroup(Scanner scanner) {
    super(scanner);
  }

  @Override
  protected void start() {
    commands = Interpreter.parseCommandList(scanner);
    parseEnding();
    finish();
  }
}
