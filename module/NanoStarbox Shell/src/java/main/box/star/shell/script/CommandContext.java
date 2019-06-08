package box.star.shell.script;

import box.star.text.basic.Scanner;

public class CommandContext extends Command {

  public CommandList commandList;

  public CommandContext(Scanner scanner) {
    super(scanner);
  }

  @Override
  protected void start() {
    commandList = Interpreter.parseSubShell(scanner);
    parseEnding();
    finish();
  }

}
